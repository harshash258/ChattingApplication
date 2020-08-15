package com.mycompany.newchatapp.Activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mycompany.newchatapp.Model.Users;
import com.mycompany.newchatapp.R;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.mycompany.newchatapp.Activities.UserDetailsActivity.PICK_IMAGE;

public class EditProfile extends AppCompatActivity {

    TextInputLayout name, email, about;
    CircleImageView imageView;
    Button change, apply;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    Uri image_uri;
    String userUrl, phoneNumber, userName, userEmail, userAbout, download_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        name = findViewById(R.id.editName);
        email = findViewById(R.id.editEmail);
        about = findViewById(R.id.editAboutMe);
        imageView = findViewById(R.id.userProfilePic);
        change = findViewById(R.id.channgeProfilePic);
        apply = findViewById(R.id.applyChange);

        storageReference = FirebaseStorage.getInstance().getReference().child("Users");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").
                child(user.getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userUrl = snapshot.child("profilephotoURL").getValue().toString();
                    phoneNumber = snapshot.child("phoneNumber").getValue().toString();
                    userName = snapshot.child("username").getValue().toString();
                    userEmail = snapshot.child("email").getValue().toString();
                    userAbout = snapshot.child("aboutMe").getValue().toString();
                    if (userUrl.equals(""))
                        imageView.setImageResource(R.drawable.ic_user);
                    else
                        Glide.with(getApplicationContext()).load(userUrl).centerCrop().into(imageView);
                    name.getEditText().setText(userName);
                    email.getEditText().setText(userEmail);
                    about.getEditText().setText(userAbout);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        change.setOnClickListener(v -> {
            chooseImage();
        });

        apply.setOnClickListener(v -> {
            applyChanges();
        });
    }

    private void applyChanges() {
        if (name.getEditText().getText().equals(""))
            name.setError("Name Should not be Empty");

        if (email.getEditText().getText().equals("")) {
            if (about.getEditText().getText().equals("")) {
                Users users = new Users(name.getEditText().getText().toString(), phoneNumber, user.getUid(),
                        userUrl, "online", "", "", user.getPhoneNumber());
                databaseReference.setValue(users);
            }
        } else {
            Users users = new Users(name.getEditText().getText().toString(), phoneNumber, user.getUid(),
                    userUrl, "online", email.getEditText().getText().toString(),
                    about.getEditText().getText().toString(), user.getPhoneNumber());
            databaseReference.setValue(users);
        }
        Intent intent = new Intent(EditProfile.this, ProfileActivity.class);
        startActivity(intent);

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
            updateImage();
        }
    }

    public String GetFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void updateImage() {
        if (image_uri != null) {
            final StorageReference reference = storageReference.child(user.getUid()).child("Profile Picture").
                    child(System.currentTimeMillis() + "." + GetFileExtension(image_uri));

            final UploadTask uploadTask = reference.putFile(image_uri);
            uploadTask.addOnFailureListener(e ->
                    Toast.makeText(EditProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show()).
                    addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> uriTask = uploadTask.continueWithTask(task -> {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            download_url = reference.getDownloadUrl().toString();
                            return reference.getDownloadUrl();
                        }).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                download_url = task.getResult().toString();

                                Users users = new Users(userName, phoneNumber, user.getUid(), download_url,
                                        "online", userEmail, userAbout, user.getPhoneNumber());
                                databaseReference.setValue(users);
                                imageView.setImageURI(image_uri);
                            }
                        });
                    });
        }
    }
}