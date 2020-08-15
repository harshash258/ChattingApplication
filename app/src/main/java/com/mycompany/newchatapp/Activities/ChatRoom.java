package com.mycompany.newchatapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mycompany.newchatapp.Adapter.RoomAdapter;
import com.mycompany.newchatapp.Model.RoomChatModel;
import com.mycompany.newchatapp.R;

import java.util.ArrayList;
import java.util.List;

public class ChatRoom extends AppCompatActivity {

    TextView textviewRoomName;
    RecyclerView recyclerView;
    DatabaseReference databaseReference, userDetails, dbmessage;
    EditText editTextMessage;
    ImageButton sendMessage;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    List<RoomChatModel> messages;
    RoomAdapter adapter;

    String roomID, senderName, roomName, createrID;
    long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");

        intialiseVariables();

        Intent intent = getIntent();
        roomID = intent.getStringExtra("roomId");
        Log.d("roomId", roomID);

        databaseReference = FirebaseDatabase.getInstance().getReference("Rooms").
                child(roomID).child("GroupInfo");
        dbmessage = FirebaseDatabase.getInstance().getReference("Rooms").
                child(roomID);

        DatabaseReference check = FirebaseDatabase.getInstance().getReference("Rooms");

        check.child(roomID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("called", roomID);
                if (snapshot.exists()) {
                    Log.d("exist",  "true" );
                }
                else {
                    Toast.makeText(ChatRoom.this, "Room has Been Disbaned, Going back.",
                            Toast.LENGTH_LONG).show();
                    Intent intent1 = new Intent(ChatRoom.this, MainScreen.class);
                    startActivity(intent1);
                    Log.d("else", "called");
                }

                Log.d("Finised:","true");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        userDetails = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    roomName = snapshot.child("roomName").getValue().toString();
                    createrID = snapshot.child("createdBy").getValue().toString();
                    String roomCode = snapshot.child("roomCode").getValue().toString();
                    Log.d("Room Name:", roomName);
                    textviewRoomName.setText(roomName + " ( " + roomCode + " )");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        userDetails.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                senderName = snapshot.child("username").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        sendMessage.setOnClickListener(v -> {
            sendMessage();
        });

        readMessage();
    }

    private void intialiseVariables() {

        textviewRoomName = findViewById(R.id.roomName);
        editTextMessage = findViewById(R.id.messageText);
        sendMessage = findViewById(R.id.sendMessage);

        recyclerView = findViewById(R.id.RoomrecyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getApplicationContext());
        linearLayout.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayout);

        messages = new ArrayList<>();

    }

    private void sendMessage() {
        String message = editTextMessage.getText().toString();
        String messageId = databaseReference.push().getKey();
        RoomChatModel chatModel = new RoomChatModel(message, user.getUid(), senderName, "text", messageId);
        dbmessage.child("Messages").child(messageId).setValue(chatModel);
        editTextMessage.setText("");
    }

    private void readMessage()  {
        dbmessage.child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    RoomChatModel chatModel = dataSnapshot.getValue(RoomChatModel.class);
                    messages.add(chatModel);
                }
                adapter = new RoomAdapter(ChatRoom.this, messages);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (createrID.equals(user.getUid())) {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                dbmessage.removeValue();
                Intent intent = new Intent(this, MainScreen.class);
                startActivity(intent);
                Toast.makeText(this, "Room Closed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "You are The Admin of this room. If you leave room will" +
                        "be close", Toast.LENGTH_LONG).show();
            }
        } else {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                Intent intent = new Intent(this, MainScreen.class);
                startActivity(intent);
                dbmessage.child("Members").child(user.getUid()).removeValue();
            } else {
                Toast.makeText(this, "Press back again to Exit.", Toast.LENGTH_SHORT).show();
            }

            backPressedTime = System.currentTimeMillis();
        }


        backPressedTime = System.currentTimeMillis();
    }
}