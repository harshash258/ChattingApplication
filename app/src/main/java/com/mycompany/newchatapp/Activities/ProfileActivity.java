package com.mycompany.newchatapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mycompany.newchatapp.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    DatabaseReference databaseReference;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    CircleImageView profilePic;
    TextView textViewname, textViewemail, textViewaboutme;
    Button edit;

    String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profilePic = findViewById(R.id.userProfilePic);
        textViewname = findViewById(R.id.userName);
        textViewemail = findViewById(R.id.userEmail);
        textViewaboutme = findViewById(R.id.aboutMe);
        edit = findViewById(R.id.editProfile);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").
                child(user.getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    textViewname.setText(snapshot.child("username").getValue().toString());
                    textViewemail.setText(snapshot.child("email").getValue().toString());
                    textViewaboutme.setText(snapshot.child("aboutMe").getValue().toString());
                    imageUrl = snapshot.child("profilephotoURL").getValue().toString();

                    if (imageUrl.equals(""))
                        profilePic.setImageResource(R.drawable.ic_user);
                    else
                        Glide.with(getApplicationContext()).load(imageUrl)
                                .fitCenter().into(profilePic);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        edit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfile.class);
            startActivity(intent);
        });
    }
}