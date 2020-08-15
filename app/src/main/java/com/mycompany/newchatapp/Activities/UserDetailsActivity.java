package com.mycompany.newchatapp.Activities;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mycompany.newchatapp.Model.Users;
import com.mycompany.newchatapp.R;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.mycompany.newchatapp.Activities.VerifyOTP.NORMAL_NUMBER;

public class UserDetailsActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 100;
    CircleImageView imageView;
    Button choose, upload;
    EditText username;
    Uri image_uri;
    DatabaseReference db, reference;
    StorageReference storageReference;
    ProgressDialog progressDialog;
    String phoneNumber, download_url, userID, normalNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        imageView = findViewById(R.id.profilepic);
        username = findViewById(R.id.userName);
        choose = findViewById(R.id.choose);
        upload = findViewById(R.id.upload);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        phoneNumber = user.getPhoneNumber();
        userID = user.getUid();

        Intent intent = getIntent();
        normalNumber = intent.getStringExtra(NORMAL_NUMBER);

        progressDialog = new ProgressDialog(this);

        db = FirebaseDatabase.getInstance().getReference().child("Users");
        reference = FirebaseDatabase.getInstance().getReference().child("Contacts");
        storageReference = FirebaseStorage.getInstance().getReference().child("Users");


        choose.setOnClickListener(v -> chooseImage());
        upload.setOnClickListener(v -> uploadDetails());
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            image_uri = data.getData();
            imageView.setImageURI(image_uri);
        }
    }


    public String GetFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadDetails() {
        String userName = username.getText().toString();

        if (userName.isEmpty() || userName.length() <= 3) {
            final AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                    this);
            alertDialog2.setTitle("Error");
            alertDialog2.setCancelable(false);
            alertDialog2.setMessage("Please Enter a username or Username should be minimum  charaters");
            alertDialog2.setPositiveButton("YES",
                    (dialog, which) -> dialog.cancel());
            alertDialog2.show();
        } else {
            progressDialog.setTitle("Please Wait...");
            progressDialog.show();

            if (image_uri != null) {
                final StorageReference reference = storageReference.child(userID).child("Profile Picture").
                        child(System.currentTimeMillis() + "." + GetFileExtension(image_uri));

                final UploadTask uploadTask = reference.putFile(image_uri);
                uploadTask.addOnFailureListener(e ->
                        Toast.makeText(UserDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show()).
                        addOnSuccessListener(taskSnapshot -> {
                            Task<Uri> uriTask = uploadTask.continueWithTask(task -> {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                download_url = reference.getDownloadUrl().toString();
                                return reference.getDownloadUrl();
                            }).addOnCompleteListener(task -> {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    download_url = task.getResult().toString();

                                    Users user = new Users(username.getText().toString(), normalNumber.trim(),
                                            userID, download_url, "offline", "", "", phoneNumber);
                                    db.child(userID).setValue(user);

                                    Intent intent = new Intent(UserDetailsActivity.this, MainScreen.class);
                                    startActivity(intent);
                                }
                            });
                        });
            } else {
                Users user = new Users(username.getText().toString(), normalNumber, userID,
                        "", "offline", "", "", phoneNumber);
                db.child(userID).setValue(user);

                Intent intent = new Intent(UserDetailsActivity.this, MainScreen.class);
                startActivity(intent);
            }
        }
    }
}
