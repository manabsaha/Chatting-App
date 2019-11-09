package friends.account;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.infiam.firstbottomnav.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class FriendProfile extends AppCompatActivity {

    private String user_id;

    private TextView mFriendStatus, mFriendName, mFriendCount;
    private Button mSendRequest, mDeclineRequest;
    private ImageView mFriendImage;

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationReference;

    private FirebaseUser mCurrentUser;

    private String mCurrent_state;
    private String friend_state;

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        mFriendImage = findViewById(R.id.friend_image);
        mFriendName = findViewById(R.id.friend_name);
        mFriendStatus = findViewById(R.id.friend_status);
        mFriendCount = findViewById(R.id.friend_friends_count);
        mSendRequest = findViewById(R.id.friend_request);
        mDeclineRequest = findViewById(R.id.friend_request_decline);

        user_id = getIntent().getStringExtra("user_id");

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_requests");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationReference = FirebaseDatabase.getInstance().getReference().child("notifications");

        //this indicates the state for the request button.
        mCurrent_state = "not_friends";


        //hides the send and decline request button from self profile.
        if (mCurrentUser.getUid().equals(user_id)) {
            mSendRequest.setEnabled(false);
            mDeclineRequest.setEnabled(false);
            mSendRequest.setVisibility(View.INVISIBLE);
            mDeclineRequest.setVisibility(View.INVISIBLE);
        }
        //gets the current state with the user.
        else {
            mFriendReqDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //-------------------
                    if (dataSnapshot.hasChild(mCurrentUser.getUid())) {
                        if (dataSnapshot.child(mCurrentUser.getUid()).hasChild(user_id)) {
                            friend_state = dataSnapshot.child(mCurrentUser.getUid()).child(user_id).child("request_type").getValue().toString();
                        } else {
                            //Toast.makeText(getApplicationContext(),"Lorem Ipsum 2",Toast.LENGTH_LONG).show();
                            mCurrent_state = "not_friends";
                            friend_state = "not_friends";
                            mSendRequest.setText("SEND FRIEND REQUEST");
                            mDeclineRequest.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        //Toast.makeText(getApplicationContext(),"Lorem Ipsum 1",Toast.LENGTH_LONG).show();
                        mCurrent_state = "not_friends";
                        friend_state = "not_friends";
                        mSendRequest.setText("SEND FRIEND REQUEST");
                        mDeclineRequest.setVisibility(View.INVISIBLE);
                    }
                    //------------------
                    if (friend_state.equals("friends")) {
                        mCurrent_state = friend_state;
                        mSendRequest.setText("UNFRIEND");
                    } else if (friend_state.equals("sent") || friend_state.equals("received")) {
                        mCurrent_state = friend_state;
                        if (mCurrent_state.equals("sent")) {
                            mSendRequest.setText("CANCEL REQUEST");
                        } else {
                            mSendRequest.setText("ACCEPT REQUEST");
                            mDeclineRequest.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), "Failed connecting server", Toast.LENGTH_LONG).show();
                }
            });
        }

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String fImage = dataSnapshot.child("image").getValue().toString();
                String fName = dataSnapshot.child("name").getValue().toString();
                String fStatus = dataSnapshot.child("status").getValue().toString();

                mFriendName.setText(fName);
                mFriendStatus.setText(fStatus);
                Picasso.get().load(fImage).placeholder(R.drawable.default_image).into(mFriendImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed connecting server", Toast.LENGTH_LONG).show();
            }
        });


        //OnClickListener for Request button.
        mSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mSendRequest.setEnabled(false);

                if (mCurrent_state.equals("not_friends")) {
                    //sending friend request
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).child("request_type")
                                        .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Request sent", Toast.LENGTH_LONG).show();
                                            mSendRequest.setEnabled(true);
                                            mCurrent_state = "sent";
                                            mSendRequest.setText("CANCEL REQUEST");

                                            Map notificationMap = new HashMap();
                                            notificationMap.put("from", mCurrentUser.getUid());
                                            notificationMap.put("type", 0);
                                            mNotificationReference.child(user_id).push().setValue(notificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                    } else {
                                                        Toast.makeText(getApplicationContext(), "Failed sending notification", Toast.LENGTH_LONG).show();
                                                        mSendRequest.setEnabled(true);
                                                    }
                                                }
                                            });
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Failed sending request", Toast.LENGTH_LONG).show();
                                            mSendRequest.setEnabled(true);
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed sending request", Toast.LENGTH_LONG).show();
                                mSendRequest.setEnabled(true);
                            }
                        }
                    });
                } else if (mCurrent_state.equals("sent")) {
                    //cancelling friend request
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Request Cancelled", Toast.LENGTH_LONG).show();
                                            mCurrent_state = "not_friends";
                                            mSendRequest.setText("SEND FRIEND REQUEST");
                                            mSendRequest.setEnabled(true);
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Task Failed", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(getApplicationContext(), "Task Failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else if (mCurrent_state.equals("received")) {
                    //accepting friend request
                    mDeclineRequest.setEnabled(false);
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).child("request_type")
                            .setValue("friends").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).child("request_type")
                                        .setValue("friends").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            //setting up friends-database on accepting request.
                                            mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).child("date").setValue("ud").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).child("date").setValue("ud").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    mDeclineRequest.setVisibility(View.INVISIBLE);
                                                                    Toast.makeText(getApplicationContext(), "Request Accepted", Toast.LENGTH_LONG).show();
                                                                    mCurrent_state = "friends";
                                                                    mSendRequest.setText("UNFRIEND");
                                                                    mSendRequest.setEnabled(true);

                                                                    Map notificationMap = new HashMap();
                                                                    notificationMap.put("from", mCurrentUser.getUid());
                                                                    notificationMap.put("type", 1);
                                                                    mNotificationReference.child(user_id).push().setValue(notificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                            } else {
                                                                                Toast.makeText(getApplicationContext(), "Failed sending notification", Toast.LENGTH_LONG).show();
                                                                            }
                                                                        }
                                                                    });
                                                                } else {
                                                                    Toast.makeText(getApplicationContext(), "Failed accepting the request", Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        Toast.makeText(getApplicationContext(), "Failed accepting the request", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Failed accepting the request", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed accepting the request", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else if (mCurrent_state.equals("friends")) {
                    //un-friend the user
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            //removing friend from friends-database.
                                            mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).child("date").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).child("date").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(getApplicationContext(), "Unfriend done", Toast.LENGTH_LONG).show();
                                                                mCurrent_state = "not_friends";
                                                                mSendRequest.setText("SEND FRIEND REQUEST");
                                                                mSendRequest.setEnabled(true);
                                                            } else {
                                                                Toast.makeText(getApplicationContext(), "Task Failed", Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Task Failed", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(getApplicationContext(), "Task Failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        mDeclineRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Request Declined", Toast.LENGTH_LONG).show();
                                        mDeclineRequest.setVisibility(View.INVISIBLE);
                                        mCurrent_state = "not_friends";
                                        mSendRequest.setText("SEND FRIEND REQUEST");
                                        mSendRequest.setEnabled(true);
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Task Failed", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getApplicationContext(), "Task Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

    }
}
