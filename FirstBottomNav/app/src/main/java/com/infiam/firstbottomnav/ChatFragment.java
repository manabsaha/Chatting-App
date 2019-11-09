package com.infiam.firstbottomnav;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import chat.pkg.ChatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Manab on 09-03-2019.
 */

public class ChatFragment extends Fragment {

    private RecyclerView chatView;
    private FirebaseAuth mAuth;
    private String currentUid;
    private DatabaseReference chatDatabase,userDatabase;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        chatView = view.findViewById(R.id.chat_users_recycler_view);

        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();
        chatDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(currentUid);
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        chatView.setHasFixedSize(true);
        chatView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Chats,ChatsViewHolder> chatsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Chats, ChatsViewHolder>
                (Chats.class,R.layout.users_single_layout,ChatsViewHolder.class,chatDatabase){

            @Override
            protected void populateViewHolder(final ChatsViewHolder viewHolder, Chats model, int position) {
                final String chat_key = getRef(position).getKey();
                userDatabase.child(chat_key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        final String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                        viewHolder.setChatDetails(userName,userThumb);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent chat_intent = new Intent(getActivity(), ChatActivity.class);
                                chat_intent.putExtra("user_id", chat_key);
                                chat_intent.putExtra("user_name",userName);
                                chat_intent.putExtra("user_thumb",userThumb);
                                startActivity(chat_intent);

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        chatView.setAdapter(chatsRecyclerViewAdapter);
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public ChatsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setChatDetails(String userName, String userThumb) {

            TextView mName = mView.findViewById(R.id.user_single_name);
            mName.setText(userName);

            TextView mLastMsg = mView.findViewById(R.id.user_single_status);
            mLastMsg.setText("Last message");

            CircleImageView mThumb = mView.findViewById(R.id.user_single_image);
            Picasso.get().load(userThumb).into(mThumb);
        }
    }
}
