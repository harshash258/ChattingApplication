package com.mycompany.newchatapp.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mycompany.newchatapp.Activities.ChatRoom;
import com.mycompany.newchatapp.Activities.CreateRoom;
import com.mycompany.newchatapp.Model.ChatList;
import com.mycompany.newchatapp.R;

import java.util.ArrayList;
import java.util.List;

public class RoomFragment extends Fragment {

    Button createRoom, joinButton;
    EditText editTextRoomCode;
    String code, roomId;
    List<String> codes;
    DatabaseReference databaseReference;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    ProgressDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room, container, false);

        createRoom = view.findViewById(R.id.createRoom);
        joinButton = view.findViewById(R.id.joinRoom);
        editTextRoomCode = view.findViewById(R.id.editTextJoinRoom);
        dialog = new ProgressDialog(getContext());
        dialog.setTitle("Checking...");
        dialog.setCancelable(false);
        dialog.setMessage("Please Wait till we check if the Room exist or not");

        codes = new ArrayList<>();


        joinButton.setOnClickListener(v -> {
            if (editTextRoomCode.getText().equals(""))
                Toast.makeText(getContext(), "Room Code Cannot be Empty", Toast.LENGTH_SHORT).show();
            else {

                databaseReference = FirebaseDatabase.getInstance().getReference().child("Rooms");
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                code = dataSnapshot.child("GroupInfo/roomCode").getValue().toString();
                                codes.add(code);
                                dialog.dismiss();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                joinRoom();
            }
        });


        createRoom.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreateRoom.class);
            startActivity(intent);
        });

        return view;
    }

    private void joinRoom() {
        String roomCode = editTextRoomCode.getText().toString();

        for (String checkCode : codes) {
            if (checkCode.equals(roomCode)) {
                databaseReference.orderByChild("GroupInfo/roomCode").equalTo(roomCode).
                        addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        roomId = dataSnapshot.getKey();
                                        addToRoom(roomId);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            } else {
                Toast.makeText(getContext(), "OOPS!, No Room Exist with this Code",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void addToRoom(String roomId) {
        Intent intent = new Intent(getContext(), ChatRoom.class);
        intent.putExtra("roomId", roomId);
        startActivity(intent);
        ChatList chatList = new ChatList(user.getUid());
        databaseReference.child(roomId).child("Members").child(user.getUid()).setValue(chatList);
    }
}
