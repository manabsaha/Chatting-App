package account.more;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.infiam.firstbottomnav.R;

public class NewStatus extends AppCompatActivity {

    TextInputLayout new_status;
    String status,newUserStatus;
    Button update_new_status;
    Toolbar mToolbar;

    //Firebase
    FirebaseUser mFirebaseCurrentUser;
    DatabaseReference mStatusDatbase;

    //Progress
    ProgressDialog mProgressDialogStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_status);

        //Toolbar
        mToolbar = findViewById(R.id.my_toolbar_2);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("ZapBie");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Firebase
        mFirebaseCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mFirebaseCurrentUser.getUid();

        mStatusDatbase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        //receives the status from intent.
        status = getIntent().getStringExtra("status");

        new_status = findViewById(R.id.new_status_et);
        new_status.getEditText().setText(status);

        update_new_status = findViewById(R.id.update_new_status_btn);
        update_new_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newUserStatus = new_status.getEditText().getText().toString();
                if(newUserStatus.length()==0){
                    newUserStatus = getResources().getString(R.string.default_status);
                }
                updateNewStatusToFirebase(newUserStatus);   //UDF: updates the new status.
            }
        });

    }

    //UDF: updates the new status.
    private void updateNewStatusToFirebase(final String newUserStatus) {
        //Toast.makeText(NewStatus.this,newUserStatus,Toast.LENGTH_LONG).show();
        //Progress Dialog
        mProgressDialogStatus  = new ProgressDialog(this);
        mProgressDialogStatus.setMessage("Updating your status");
        mProgressDialogStatus.setCancelable(false);
        mProgressDialogStatus.show();
        //Firebase
        mStatusDatbase.child("status").setValue(newUserStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    mProgressDialogStatus.dismiss();
                    Toast.makeText(NewStatus.this,"Status updated",Toast.LENGTH_SHORT).show();
                    /*new_status.getEditText().setText(newUserStatus);*/
                    finish();
                }
                else{
                    mProgressDialogStatus.dismiss();
                    Toast.makeText(NewStatus.this,"There was some error in updating your status",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
