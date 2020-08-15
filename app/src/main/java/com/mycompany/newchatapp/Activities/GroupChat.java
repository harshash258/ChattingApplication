package com.mycompany.newchatapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mycompany.newchatapp.Adapter.RoomAdapter;
import com.mycompany.newchatapp.Model.GroupInfo;
import com.mycompany.newchatapp.Model.RoomChatModel;
import com.mycompany.newchatapp.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChat extends AppCompatActivity {

    TextView textviewGroupName, totalparticipant;
    CircleImageView groupicon;
    RecyclerView recyclerView;
    DatabaseReference groupDetails, userDetails, dbmessage;
    EditText editTextMessage;
    ImageButton sendMessage;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    List<RoomChatModel> messages;
    RoomAdapter adapter;

    String groupId, senderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");

        intialiseVariables();

        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");
        Log.d("groupId", groupId);

        groupDetails = FirebaseDatabase.getInstance().getReference("Groups").child(groupId);
        groupDetails.child("GroupInfo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    GroupInfo info = snapshot.getValue(GroupInfo.class);
                    textviewGroupName.setText(info.getGroupName());
                    if (info.getGroupIcon().toString().equals(""))
                        groupicon.setImageResource(R.drawable.ic_group);
                    else
                        Glide.with(GroupChat.this).load(info.getGroupIcon()).fitCenter().
                                placeholder(R.drawable.ic_group).into(groupicon);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        groupDetails.child("Members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int size = (int) snapshot.getChildrenCount();
                totalparticipant.setText("No of Participants:" + size);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        userDetails = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        userDetails.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                senderName = snapshot.child("username").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dbmessage = FirebaseDatabase.getInstance().getReference("Groups").child(groupId);

        sendMessage.setOnClickListener(v -> {
            sendMessage();
        });
        readMessage();

    }

    private void intialiseVariables() {

        textviewGroupName = findViewById(R.id.roomName);
        editTextMessage = findViewById(R.id.messageText);
        sendMessage = findViewById(R.id.sendMessage);
        groupicon = findViewById(R.id.chatGroupIcon);
        totalparticipant = findViewById(R.id.totalParticipant);


        recyclerView = findViewById(R.id.RoomrecyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getApplicationContext());
        linearLayout.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayout);

        messages = new ArrayList<>();

    }

    private void sendMessage() {
        String message = editTextMessage.getText().toString();
        String messageId = groupDetails.push().getKey();
        RoomChatModel chatModel = new RoomChatModel(message, user.getUid(), senderName, "text", messageId);
        dbmessage.child("Messages").child(messageId).setValue(chatModel);
        editTextMessage.setText("");
    }

    private void readMessage() {

        dbmessage.child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    RoomChatModel chatModel = dataSnapshot.getValue(RoomChatModel.class);
                    messages.add(chatModel);
                }
                adapter = new RoomAdapter(GroupChat.this, messages);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainScreen.class);
        startActivity(intent);
    }
}