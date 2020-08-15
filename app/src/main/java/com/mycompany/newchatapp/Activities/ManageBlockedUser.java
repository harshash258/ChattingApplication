package com.mycompany.newchatapp.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mycompany.newchatapp.Adapter.BlockedUserAdapter;
import com.mycompany.newchatapp.Model.Users;
import com.mycompany.newchatapp.R;

import java.util.ArrayList;
import java.util.List;

public class ManageBlockedUser extends AppCompatActivity {

    DatabaseReference databaseReference;
    BlockedUserAdapter adapter;
    List<Users> mUsers;
    List<String> blockList;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    RecyclerView recyclerView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_blocked_user);

        mUsers = new ArrayList<>();
        blockList = new ArrayList<>();
        textView = findViewById(R.id.textview1);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        databaseReference = FirebaseDatabase.getInstance().getReference("BlockList").child(user.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String power = dataSnapshot.child("power").getValue().toString();
                        String id = dataSnapshot.child("friendId").getValue().toString();
                        if (power.equals("true")) {
                            blockList.add(id);
                        }
                    }
                    Log.d("Size of:", String.valueOf(blockList.size()));
                    readBlockUser();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readBlockUser() {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.keepSynced(true);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    for (String id : blockList) {
                        if (users.getUserId().equals(id)) {
                            mUsers.add(users);
                        }
                    }
                    textView.setVisibility(View.GONE);
                }
                adapter = new BlockedUserAdapter(ManageBlockedUser.this, mUsers);

                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}