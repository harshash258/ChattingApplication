package com.mycompany.newchatapp.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.hbb20.CountryCodePicker;
import com.mycompany.newchatapp.R;

public class RegisterPhoneNumber extends AppCompatActivity {

    public static final String PHONE_NUMBER = "phonenumber";
    CountryCodePicker ccp;
    EditText phoneNumber;
    Button verify;
    AlertDialog.Builder builder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_phone_number);

        ccp = findViewById(R.id.ccp);
        verify = findViewById(R.id.verifyNumber);
        phoneNumber = findViewById(R.id.editTextphoneNumber);


        builder = new AlertDialog.Builder(RegisterPhoneNumber.this);
        ccp.registerCarrierNumberEditText(phoneNumber);

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPhoneNumber();
            }
        });

    }

    private void sendPhoneNumber() {
        String number = phoneNumber.getText().toString().trim();

        if (number.isEmpty() || number.length() < 10) {

            builder.setTitle("Invalid Phone Number");
            builder.setMessage("Please Enter a valid Phone Number");
            builder.setCancelable(false);

            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            final Intent intent = new Intent(RegisterPhoneNumber.this, VerifyOTP.class);
            intent.putExtra(PHONE_NUMBER, ccp.getFullNumberWithPlus());
            String normalNumber = phoneNumber.getText().toString().trim();
            normalNumber.replace(" ", "");
            intent.putExtra("phone", normalNumber);
            final ProgressDialog progress = new ProgressDialog(this);
            progress.setTitle("Connecting");
            progress.setMessage("Please wait...");
            progress.show();
            Runnable progressRunnable = new Runnable() {

                @Override
                public void run() {
                    progress.cancel();
                    startActivity(intent);
                }
            };
            Handler pdCanceller = new Handler();
            pdCanceller.postDelayed(progressRunnable, 4000);
        }
    }
}