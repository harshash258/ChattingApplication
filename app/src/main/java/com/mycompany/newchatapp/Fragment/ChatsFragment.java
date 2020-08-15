package com.mycompany.newchatapp.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mycompany.newchatapp.Adapter.ChatsAdapter;
import com.mycompany.newchatapp.Adapter.GroupChatsAdapter;
import com.mycompany.newchatapp.Model.ChatList;
import com.mycompany.newchatapp.Model.GroupInfo;
import com.mycompany.newchatapp.Model.Users;
import com.mycompany.newchatapp.R;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    RecyclerView recyclerView;
    DatabaseReference reference, groupRefernce;
    ChatsAdapter chatsAdapter;
    List<Users> mUser;
    List<GroupInfo> mGroup;
    GroupChatsAdapter groupChatsAdapter;
    List<ChatList> userList;
    FirebaseUser user;
    String userId;
    TextView textView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        textView = view.findViewById(R.id.textview);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();

        mUser = new ArrayList<>();
        userList = new ArrayList<>();
        mGroup = new ArrayList<>();


        reference = FirebaseDatabase.getInstance().getReference().child("ChatsList").child(userId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatList chatList = dataSnapshot.getValue(ChatList.class);
                    userList.add(chatList);
                }
                readUser();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference groupsRef = rootRef.child("Groups");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mGroup.clear();
                for(DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    GroupInfo info = groupSnapshot.getValue(GroupInfo.class);
                    for(DataSnapshot memberSnapshot : groupSnapshot.child("Members").getChildren()) {
                        String id = memberSnapshot.child("id").getValue(String.class);
                        if (id.equals(userId)){
                            mGroup.add(info);
                        }
                    }
                }
                groupChatsAdapter = new GroupChatsAdapter(mGroup, getContext());
                recyclerView.setAdapter(groupChatsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("TAG", databaseError.getMessage()); //Don't ignore errors!
            }
        };
        groupsRef.addListenerForSingleValueEvent(valueEventListener);
        return view;
    }

    private void readUser() {
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUser.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    for (ChatList chatList : userList) {
                        if (users.getUserId().equals(chatList.getId())) {
                            mUser.add(users);
                            textView.setVisibility(View.GONE);
                        }
                    }

                }
                chatsAdapter = new ChatsAdapter(getContext(), mUser);
                recyclerView.setAdapter(chatsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}
