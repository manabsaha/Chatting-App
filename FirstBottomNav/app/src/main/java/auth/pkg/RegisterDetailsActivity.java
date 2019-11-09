package auth.pkg;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.infiam.firstbottomnav.R;

public class RegisterDetailsActivity extends AppCompatActivity {

    TextInputLayout userNumber;
    Button mSubmitDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_details);

        userNumber = (TextInputLayout) findViewById(R.id.user_number);
        mSubmitDetails = (Button) findViewById(R.id.submit_button);

        mSubmitDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userNumber.getEditText().length() == 10) {
                    String num = userNumber.getEditText().getText().toString();
                    Intent verify = new Intent(getApplicationContext(), RegisterActivity.class);
                    verify.putExtra("user_number", num);
                    startActivity(verify);
                    finish();
                }
                else{
                    userNumber.setError("Enter a valid number");
                    userNumber.requestFocus();
                }
            }
        });
    }
}
