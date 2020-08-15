package com.mycompany.newchatapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mycompany.newchatapp.R;

public class SettingActivity extends AppCompatActivity {

    TextView block, profile;
    Button button;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        profile = findViewById(R.id.profile);
        block = findViewById(R.id.manageblockedUsers);
        button = findViewById(R.id.deleteAccount);


        button.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete Account");
            builder.setIcon(R.drawable.ic_block);
            builder.setCancelable(false);
            builder.setMessage("Are you sure You want to delete your account?");
            builder.setPositiveButton("Yes\uD83D\uDE14", (dialog, which) -> {
                ProgressDialog progress = new ProgressDialog(this);
                progress.setTitle("Deleting You Account");
                progress.setMessage("Please wait...");
                progress.show();
                Runnable progressRunnable = new Runnable() {

                    @Override
                    public void run() {
                        user.delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Intent i = new Intent(SettingActivity.this, RegisterPhoneNumber.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                            } else {
                                Toast.makeText(SettingActivity.this, "Error Occurred, Please try again Later",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                        progress.cancel();
                    }
                };
                Handler pdCanceller = new Handler();
                pdCanceller.postDelayed(progressRunnable, 4000);

            });
            builder.setNegativeButton("Just Kidding\uD83D\uDE03", (dialog, which) -> {
                dialog.dismiss();
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });


        profile.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
        block.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, ManageBlockedUser.class);
            startActivity(intent);
        });

    }
}