package com.mycompany.newchatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mycompany.newchatapp.Model.BlockList;
import com.mycompany.newchatapp.Model.Users;
import com.mycompany.newchatapp.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewFriendProfile extends AppCompatActivity {

    TextView name, about, phoneNumber;
    Button blockUser;
    CircleImageView profilepic;
    DatabaseReference databaseReference;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String friendId, friendPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_friend_profile);

        profilepic = findViewById(R.id.friendProfilePic);
        name = findViewById(R.id.friendProfileName);
        about = findViewById(R.id.friendAboutMe);
        phoneNumber = findViewById(R.id.friendPhoneNumber);
        blockUser = findViewById(R.id.blockUser);

        Intent intent = getIntent();
        friendId = intent.getStringExtra("friendId");

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(friendId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Users users = snapshot.getValue(Users.class);
                    name.setText(users.getUsername());
                    about.setText(users.getAboutMe());
                    phoneNumber.setText(users.getFullPhoneNumber());
                    Glide.with(ViewFriendProfile.this).load(users.getProfilephotoURL())
                        .fitCenter().placeholder(R.drawable.ic_user).into(profilepic);
                    friendPhoneNumber = users.getFullPhoneNumber();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        blockUser.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Block User");
            builder.setIcon(R.drawable.ic_block);
            builder.setCancelable(false);
            builder.setMessage("Do you wan to block User?");
            builder.setPositiveButton("Yes", (dialog, which) -> {

                BlockList blockList = new BlockList(friendPhoneNumber);
                DatabaseReference blockUser = FirebaseDatabase.getInstance().
                        getReference("BlockList").child(user.getUid());
                blockUser.child(friendId).setValue(blockList);
                blockUser.child(friendId).child("power").setValue("true");
                blockUser.child(friendId).child("friendId").setValue(friendId);

                BlockList blockList1 = new BlockList(user.getPhoneNumber() );
                blockUser = FirebaseDatabase.getInstance().
                        getReference("BlockList").child(friendId);
                blockUser.child(user.getUid()).setValue(blockList1);
                blockUser.child(user.getUid()).child("power").setValue("false");
                blockUser.child(user.getUid()).child("userId").setValue(user.getUid());

                DatabaseReference me = FirebaseDatabase.getInstance().getReference().child("ChatsList")
                        .child(user.getUid());
                me.child(friendId).removeValue();
                DatabaseReference friendChatList = FirebaseDatabase.getInstance().getReference().child("ChatsList")
                        .child(friendId);
                friendChatList.child(user.getUid()).removeValue();
                Intent intent1 = new Intent(ViewFriendProfile.this, MainScreen.class);
                startActivity(intent1);

            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }

}