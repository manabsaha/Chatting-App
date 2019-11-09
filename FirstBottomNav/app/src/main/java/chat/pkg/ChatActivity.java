package chat.pkg;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.infiam.firstbottomnav.ImageIntent;
import com.infiam.firstbottomnav.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String name,id,thumb;
    private Toolbar mToolbar;
    private TextView chatName,chatLastSeen;
    private CircleImageView chatThumb;
    private DatabaseReference rootReference;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private ImageButton chatAddFile,chatSendMsg;
    private EditText chatMsg;

    private RecyclerView messagesList;
    private List<Messages> chatMsgList = new ArrayList<>();
    private LinearLayoutManager mLinearLayoutManager;
    private MessageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        id = getIntent().getStringExtra("user_id");
        name = getIntent().getStringExtra("user_name");
        thumb = getIntent().getStringExtra("user_thumb");

        rootReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        chatAddFile = findViewById(R.id.chat_add_file);
        chatMsg = findViewById(R.id.chat_message);
        chatSendMsg = findViewById(R.id.chat_message_send);

        mAdapter = new MessageAdapter(chatMsgList);
        messagesList = findViewById(R.id.chat_recycler_view);

        mLinearLayoutManager = new LinearLayoutManager(this);
        messagesList.setHasFixedSize(true);
        messagesList.setLayoutManager(mLinearLayoutManager);
        mLinearLayoutManager.setStackFromEnd(true);
        messagesList.setAdapter(mAdapter);

        loadMessages();

        //-----------------Toolbar--------------

        mToolbar = findViewById(R.id.my_chat_toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View chat_bar_view = inflater.inflate(R.layout.chat_toolbar,null);

        actionBar.setCustomView(chat_bar_view);

        //----------------Items on custom action bar-------------

        chatName = findViewById(R.id.chat_name);
        chatLastSeen = findViewById(R.id.chat_last_seen);
        chatThumb = findViewById(R.id.chat_image);

        chatName.setText(name);
        Picasso.get().load(thumb).into(chatThumb);

        //For last seen & thumb image.
        rootReference.child("Users").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String last_seen = dataSnapshot.child("online").getValue().toString();
                if(last_seen.equals("Online")){
                    chatLastSeen.setText(last_seen);
                }
                else{
                    long last_seen_ms = Long.parseLong(last_seen);
                    chatLastSeen.setText(GetTimeAgo.getTimeAgo(last_seen_ms));
                }

                String image = dataSnapshot.child("thumb_image").getValue().toString();
                Picasso.get().load(image).into(chatThumb);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //For messages
        /*rootReference.child("Chat").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(id)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp",ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/"+currentUserId+"/"+id , chatAddMap);
                    chatUserMap.put("Chat/"+id+"/"+currentUserId , chatAddMap);

                    rootReference.updateChildren(chatUserMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(!task.isSuccessful()){
                                Log.d("Chat Error",task.toString());
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/


        chatSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        chatAddFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imageIntent = new Intent(getApplicationContext(), ImageIntent.class);
                imageIntent.putExtra("chat_id",id);
                startActivity(imageIntent);
            }
        });

    }

    private void loadMessages() {

        rootReference.child("messages").child(currentUserId).child(id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                chatMsgList.add(message);
                mAdapter.notifyDataSetChanged();
                messagesList.scrollToPosition(chatMsgList.size() - 1);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void sendMessage(){

        if(chatMsg.length()>0){
            String message = chatMsg.getText().toString();
            chatMsg.setText(null);

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",currentUserId);


            String current_user_ref = "messages/" + currentUserId + "/" + id;
            String chat_user_ref = "messages/" + id + "/" + currentUserId;

            DatabaseReference user_msg_push = rootReference.child("messages").child(currentUserId).child(id).push();
            String pushId = user_msg_push.getKey();

            Map putMessage = new HashMap();
            putMessage.put(current_user_ref + "/" + pushId ,messageMap);
            putMessage.put(chat_user_ref + "/" + pushId , messageMap);

            rootReference.updateChildren(putMessage).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(!task.isSuccessful()){
                        Log.d("Message Error",task.toString());
                    }
                    else{
                    }
                }
            });

        }

    }

}
