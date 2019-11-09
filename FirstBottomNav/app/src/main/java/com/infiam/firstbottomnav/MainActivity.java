package com.infiam.firstbottomnav;

//import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
//import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import auth.pkg.RegisterActivity;
import auth.pkg.RegisterDetailsActivity;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private boolean signinflag = false;
    private DatabaseReference mDatabaseReference;

    private ProgressDialog mProgress;

    private MenuItem prevItem = null;

    //check-if user is logged in.
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if(currentUser==null) {
            Intent i = new Intent(this, RegisterDetailsActivity.class);
            startActivity(i);
            finish();
        }
        else{
            signinflag = true;
            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mDatabaseReference.child("online").setValue("Online");
        }
    }

    //----------------------------------

    @Override
    protected void onStop() {
        super.onStop();
        if(currentUser!=null) {
            mDatabaseReference.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }



    /*-----------0----------------------------------------------------------------------------------------------------------------------------*/

    //UDF: Links and create the toolbar.
    private void createToolbar() {
        //toolbar custom
        Toolbar mToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);
        setTitle("CrossWords");
    }
    //toolbar-menu show code
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_home_options,menu);
        return true;
    }
    //toolbar-menu listener
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.friend :
                Intent i = new Intent(this,FriendActivity.class);
                startActivity(i);
                return true;

            case R.id.signOut :
                showProgress("Signing out");
                //mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
                mDatabaseReference.child("device_token").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mAuth.signOut();
                            mProgress.dismiss();
                            Intent reg = new Intent(MainActivity.this, RegisterDetailsActivity.class);
                            startActivity(reg);
                            finish();
                        }
                        else{
                            mProgress.dismiss();
                            Toast.makeText(getApplicationContext(),"Failed to sign out. Retry",Toast.LENGTH_LONG).show();
                        }
                    }
                });

        }
        return super.onOptionsItemSelected(item);
    }

    private void showProgress(String msg) {
        mProgress = new ProgressDialog(this);
        mProgress.setMessage(msg);
        mProgress.setCancelable(false);
        mProgress.show();
    }
    /*--------0-----toolbar---------------------------------------------------------------------------------------------------------*/

    //onCreate activity method.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        createToolbar();


        //referencing the bottom nav_view.
        BottomNavigationView mBottomNav = findViewById(R.id.bottom_navigation);

        //passing the nav_listener on item selected.
        mBottomNav.setOnNavigationItemSelectedListener(mNavListener);

        //this fragment isn't complemenary here, unless you want to see changes as soon as app starts. Else it will keep the fragment blank.
        if(mAuth.getCurrentUser()!=null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mNavListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    if(prevItem != item) {
                        android.support.v4.app.Fragment selectedFragment = null;
                        switch (item.getItemId()) {
                            //switch id's from menu layout
                            case R.id.home:
                                prevItem = item;
                                selectedFragment = new HomeFragment();
                                break;
                            case R.id.chat:
                                prevItem = item;
                                selectedFragment = new ChatFragment();
                                break;
                            case R.id.profile:
                                prevItem = item;
                                selectedFragment = new ProfileFragment();
                                break;
                        }

                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                    }
                        return true;

                }
            };




}
