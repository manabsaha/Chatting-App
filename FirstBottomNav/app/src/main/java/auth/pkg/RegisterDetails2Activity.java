/* This stores the number,name,email,gender,status,image,thumb_image value to the FireBase RealTime database.*/
package auth.pkg;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.infiam.firstbottomnav.MainActivity;
import com.infiam.firstbottomnav.R;

import java.util.HashMap;
import java.util.Map;

public class RegisterDetails2Activity extends AppCompatActivity {

    private String phone;
    private TextInputLayout userName;
    private TextInputLayout userEmail;
    private RadioGroup gender;
    private Button submitDetailsBtn;
    private String userGender;
    private DatabaseReference mDatabase;
    private ProgressDialog mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_details2);

        phone = getIntent().getStringExtra("number");
        userName = findViewById(R.id.user_name);
        userEmail = findViewById(R.id.user_email);
        gender = findViewById(R.id.user_gender);
        submitDetailsBtn = findViewById(R.id.submit_details);

        //Radio buttons listeners.
        gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.user_gender_male: userGender = "Male"; break;
                    case R.id.user_gender_female: userGender  = "Female"; break;
                    case R.id.user_gender_unspecified: userGender = "Unspecified"; break;
                }
            }
        });
        //Submit button listener.
        submitDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeToFirebase();  //UDF: check::stores to fire base on click.
            }
        });

    }
    //UDF: check::stores to fire base on click.
    private void storeToFirebase() {
        if(userName.getEditText().length()!=0 && userGender!=null){
            String number = phone;
            String name = userName.getEditText().getText().toString();
            String email = userEmail.getEditText().getText().toString();
            if(email.length()==0){
                email = "Unspecified";
            }
            String gender = userGender;
            /*Toast.makeText(this, String.format("Submitted: %s %s %s %s %s", name, number, email, gender, status),Toast.LENGTH_SHORT).show();*/
            updateDataToFirebase(number,name,email,gender);  //UDF: Finally  stores data to the fire base.
        }
        else{
            if(userName.getEditText().length()==0){
                userName.setError("Enter your name");
                userName.requestFocus();
            }
            if(userGender==null){
                Toast.makeText(this,"Select Gender",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Stores defaults to fire base on back pressed.
    @Override
    public void onBackPressed() {
        String number = phone;
        String name = "CrossWords";
        String email = "Unspecified";
        String gender = "Unspecified";
        updateDataToFirebase(number,name,email,gender);
        //super.onBackPressed();
    }

    //UDF: Finally  stores data to the fire base.
    private void updateDataToFirebase(String number, String name, String email, String gender) {
        /*Setting up a progress dialog while process continues in backend*/
        mProgressDialog = new ProgressDialog(RegisterDetails2Activity.this);
        mProgressDialog.setMessage("Signing in");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        //getting the firebase instance of the registered user.
        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = current_user.getUid();

        //pointing to the root-Users-uid directory of the database.
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        //Hashmap for storing the data inside the child nodes of UID.
        Map userMap = new HashMap();
        userMap.put("number",number);
        userMap.put("name",name);
        userMap.put("email",email);
        userMap.put("gender",gender);
        userMap.put("status","Hey there! I am using CrossWords");
        userMap.put("image","default");
        userMap.put("thumb_image","default");

        //adding the hashmap to the child node of uid.
        mDatabase.updateChildren(userMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    mProgressDialog.dismiss();
                    Intent welcome = new Intent(getApplicationContext(), MainActivity.class);
                    welcome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(welcome);
                    finish();
                }
                else{
                   //if firebase fails to store the data into the database.
                    Log.d("error5","Firebase failed to add data");
                }
            }
        });
    }

}
