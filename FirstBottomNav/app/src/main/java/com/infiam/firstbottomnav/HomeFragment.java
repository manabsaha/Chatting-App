package com.infiam.firstbottomnav;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import friends.account.FriendProfile;

/**
 * Created by Manab on 09-03-2019.
 */

public class HomeFragment extends Fragment {

    private View view;

    private Button viewRequests;
    private RecyclerView friendList;
    private FirebaseAuth mAuth;
    private String currentUid;
    private DatabaseReference friendsDatabase;
    private DatabaseReference usersDatabase;

    public HomeFragment(){
        //empty constructor required.
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        viewRequests = view.findViewById(R.id.friend_request_view);
        friendList = view.findViewById(R.id.all_friends);

        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();

        friendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUid);
        usersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        //offline cap
        friendsDatabase.keepSynced(true);
        usersDatabase.keepSynced(true);

        friendList.setHasFixedSize(true);
        friendList.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>
                (Friends.class,R.layout.users_single_layout,FriendsViewHolder.class,friendsDatabase) {

            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {
                    //String date = model.getDate();

                    final String current_uid = getRef(position).getKey();
                    usersDatabase.child(current_uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String userName = dataSnapshot.child("name").getValue().toString();
                            final String userStatus = dataSnapshot.child("status").getValue().toString();
                            final String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setDetails(userName,userStatus,userOnline,userThumb);

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    CharSequence options[] = new CharSequence[]{"Open Profile","Send Message"};
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setTitle(userName);
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                //click event for each item on dialog box.
                                                switch (i){
                                                    case 0:
                                                        Intent profile_intent = new Intent(getActivity(), FriendProfile.class);
                                                        profile_intent.putExtra("user_id", current_uid);
                                                        startActivity(profile_intent);
                                                        break;
                                                    case 1:
                                                        Intent chat_intent = new Intent(getActivity(), ChatActivity.class);
                                                        chat_intent.putExtra("user_id", current_uid);
                                                        chat_intent.putExtra("user_name",userName);
                                                        chat_intent.putExtra("user_thumb",userThumb);
                                                        startActivity(chat_intent);
                                                        break;
                                                }
                                        }
                                    });
                                    builder.show();

                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
            }
        };

        friendList.setAdapter(friendsRecyclerViewAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDetails(String name,String status,String online,String thumb){

            TextView mName = mView.findViewById(R.id.user_single_name);
            mName.setText(name);

            TextView mStatus = mView.findViewById(R.id.user_single_status);
            mStatus.setText(status);

            CircleImageView mOnline = mView.findViewById(R.id.online_status);
            if(online.equals("Online")){
                mOnline.setVisibility(View.VISIBLE);
            }
            else{
                mOnline.setVisibility(View.INVISIBLE);
            }

            CircleImageView mThumb = mView.findViewById(R.id.user_single_image);
            Picasso.get().load(thumb).into(mThumb);
        }


    }
}
