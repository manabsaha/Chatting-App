package auth.pkg;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.infiam.firstbottomnav.MainActivity;
import com.infiam.firstbottomnav.R;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout code;
    private String phnNum;
    private Button submitBtn;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;  //verifyPhoneNumber parameter.
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog mProgressDialog = null;
    //for checking registered user from database.
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mUsersReference; //for storing device token.

    @Override
    protected void onStart() {
        super.onStart();
        phnNum = getIntent().getStringExtra("user_number");
        showProgress("Checking number");
        sendAuthCodeTryAutoVerify();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent i = new Intent(getApplicationContext(), RegisterDetailsActivity.class);
        startActivity(i);
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //referencing to the XML.
        code = (TextInputLayout) findViewById(R.id.code_reg);
        submitBtn = (Button) findViewById(R.id.submit_reg);
        mAuth = FirebaseAuth.getInstance();
        code.setEnabled(false);
        submitBtn.setEnabled(false);

        //callback variable. Refer docs.
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);  //function defined outside onCreate.
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(RegisterActivity.this, "Invalid Number", Toast.LENGTH_LONG).show();
                mProgressDialog.dismiss();
            }

            //on-code sent.
            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                Toast.makeText(RegisterActivity.this, "Code Sent", Toast.LENGTH_SHORT).show();
                mProgressDialog.dismiss();
                code.setEnabled(true);
                submitBtn.setEnabled(true);
            }
        };
        phnNum = getIntent().getStringExtra("user_number");
        sendAuthCodeTryAutoVerify();

        //extracts code from edit_text and call signInWithPhoneAuthCredential.
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (code.getEditText().length() == 0) {
                    code.setError("Invalid code");
                    code.requestFocus();
                } else {
                    String vCode = code.getEditText().getText().toString();
                    showProgress("Verifying code");
                    verifyManually(vCode);
                }
            }

        });

    }//end of onCreate.


    //function: creates a progress bar and shows.
    private void showProgress(String myMessage) {
        mProgressDialog = new ProgressDialog(RegisterActivity.this);
        mProgressDialog.setMessage(myMessage);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    //function: verifies the code manually.
    private void verifyManually(String vCode) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, vCode);
        signInWithPhoneAuthCredential(credential);  //function defined outside onCreate.
    }

    //function: sends auth code and auto-verify or throws exception.
    private void sendAuthCodeTryAutoVerify() {
        //sends the verification code.
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phnNum,
                60,
                TimeUnit.SECONDS,
                RegisterActivity.this,
                mCallbacks
        );
    }


    //function: called after code is entered. checks code: correct/incorrect.
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    checkRegistered();
                    mProgressDialog.dismiss();
                }
                else {
                    // Sign in failed, display a message and update the UI
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid.
                        mProgressDialog.dismiss();
                        code.setError("Invalid Code");
                    }
                }
            }
        });
    }

    //UDF: Checks if user is already registered & stores the device token id.
    public void checkRegistered(){

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Registered");
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        //storing the device token id.
        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        mUsersReference.child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //do nothing.
                }
                else{
                    Toast.makeText(getApplicationContext(),"Failed storing token id",Toast.LENGTH_LONG).show();
                }
            }
        });

        //checks the user.
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(phnNum)){
                    Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }
                else{
                    mDatabaseReference.child(phnNum).setValue("true");
                    Intent mainIntent = new Intent(RegisterActivity.this, RegisterDetails2Activity.class);
                    mainIntent.putExtra("number", phnNum);
                    startActivity(mainIntent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(),"Error in checking",Toast.LENGTH_LONG).show();
            }
        });
    }
}
