package com.mycompany.newchatapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mycompany.newchatapp.Model.ChatList;
import com.mycompany.newchatapp.Model.RoomInfo;
import com.mycompany.newchatapp.R;

import java.util.Random;

public class CreateRoom extends AppCompatActivity {

    DatabaseReference databaseReference;
    TextInputLayout groupName;
    Button create;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        groupName = findViewById(R.id.groupName);
        create = findViewById(R.id.createRoom);

        databaseReference = FirebaseDatabase.getInstance().getReference("Rooms");
        final String groupId = databaseReference.push().getKey();

        Random rn = new Random();
        int groupCode = rn.nextInt(900000) + 100000;

        create.setOnClickListener(v -> {
            String name = groupName.getEditText().getText().toString();
            if (name.length() > 20)
                Toast.makeText(this, "Room Name Cannot be more than 20 characters", Toast.LENGTH_SHORT).show();
            else if (name.trim().equals(""))
                Toast.makeText(this, "Room Name Cannot be Empty", Toast.LENGTH_SHORT).show();
            else {
                RoomInfo info = new RoomInfo(name, String.valueOf(groupCode), user.getUid(), groupId);
                ChatList list = new ChatList(user.getUid());
                databaseReference.child(groupId).child("GroupInfo").setValue(info);
                databaseReference.child(groupId).child("Members").child(user.getUid()).setValue(list);
                Intent intent = new Intent(this, ChatRoom.class);
                intent.putExtra("roomId", groupId);
                Log.d("roomId", groupId);
                startActivity(intent);
            }
        });
    }
}