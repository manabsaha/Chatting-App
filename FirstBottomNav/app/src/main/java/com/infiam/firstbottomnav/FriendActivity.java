package com.infiam.firstbottomnav;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import friends.account.FriendProfile;

public class FriendActivity extends AppCompatActivity {

    Toolbar mToolbar;
    RecyclerView mFindUsers;
    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = findViewById(R.id.toolbar_friends);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFindUsers = findViewById(R.id.all_users);
        mFindUsers.setHasFixedSize(true);
        mFindUsers.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Users, UsersViewHolder> mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>
                (Users.class, R.layout.users_single_layout, UsersViewHolder.class, mUsersDatabase) {

            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                //viewHolder.setImage(model.getImage());
                viewHolder.setThumbImage(model.getThumb_image());

                final String user_id = getRef(position).getKey();  //This returns the user id of the clicked item.

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profile_intent = new Intent(FriendActivity.this, FriendProfile.class);
                        profile_intent.putExtra("user_id", user_id);
                        startActivity(profile_intent);
                    }
                });
            }
        };

        mFindUsers.setAdapter(mFirebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setStatus(String status) {
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
        }

        public void setThumbImage(String image) {
            CircleImageView userDisplayImage = mView.findViewById(R.id.user_single_image);
            Picasso.get().load(image).placeholder(R.drawable.default_image).into(userDisplayImage);
        }
    }
}
