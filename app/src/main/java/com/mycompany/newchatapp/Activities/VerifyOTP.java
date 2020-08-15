package com.mycompany.newchatapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mycompany.newchatapp.R;

import java.util.concurrent.TimeUnit;

import static com.mycompany.newchatapp.Activities.RegisterPhoneNumber.PHONE_NUMBER;

public class VerifyOTP extends AppCompatActivity {

    public static final String NORMAL_NUMBER = "normalNumber";
    FirebaseAuth mAuth;

    EditText editTextOTP;
    Button verify;
    TextView textView;

    String phoneNumber, normalNumber;
    String mVerificationId, code;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    code = phoneAuthCredential.getSmsCode();

                    if (code != null) {
                        editTextOTP.setText(code);
                        verifyVerificationCode(code);
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(VerifyOTP.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    mVerificationId = s;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_o_t_p);

        textView = findViewById(R.id.texviewPhoneNumber);
        editTextOTP = findViewById(R.id.editTextOtp);
        verify = findViewById(R.id.verifyOTP);


        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        normalNumber = intent.getStringExtra("phone");
        phoneNumber = intent.getStringExtra(PHONE_NUMBER);
        Log.d("notmalNumber", normalNumber);

        textView.setText("We have Send a Verification Code to " + phoneNumber);
        sendVerificationCode(phoneNumber);

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String otp = editTextOTP.getText().toString().trim();
                final ProgressDialog progress = new ProgressDialog(VerifyOTP.this);
                progress.setTitle("Verifying");
                progress.setMessage("Please wait...");
                progress.show();
                Runnable progressRunnable = new Runnable() {

                    @Override
                    public void run() {
                        progress.cancel();
                        verifyVerificationCode(otp);
                    }
                };
                Handler pdCanceller = new Handler();
                pdCanceller.postDelayed(progressRunnable, 3000);

            }
        });
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    private void verifyVerificationCode(String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(final PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(VerifyOTP.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            profileIntent();

                        } else {
                            String message = "Something is wrong, we will fix it soon...";
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                            }
                            Toast.makeText(VerifyOTP.this, message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private void profileIntent() {
        Intent intent = new Intent(VerifyOTP.this, UserDetailsActivity.class);
        intent.putExtra(PHONE_NUMBER, phoneNumber);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(NORMAL_NUMBER, normalNumber);
        startActivity(intent);
    }

}
