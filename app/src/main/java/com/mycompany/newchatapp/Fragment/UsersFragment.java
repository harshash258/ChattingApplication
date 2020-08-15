package com.mycompany.newchatapp.Fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mycompany.newchatapp.Adapter.UsersAdapter;
import com.mycompany.newchatapp.Model.BlockList;
import com.mycompany.newchatapp.Model.Users;
import com.mycompany.newchatapp.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    RecyclerView recyclerView;
    UsersAdapter adapter;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    List<String> contactsList;
    List<String> blockLists;
    String blockedNumber;

    DatabaseReference reference;
    EditText search;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = view.findViewById(R.id.userRecyclerView);
        search = view.findViewById(R.id.search_user);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        contactsList = new ArrayList<>();
        blockLists = new ArrayList<>();

        getBlocList();
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = search.getText().toString().trim();
                if (text.equals("")){
                    getActualUser();
                }else {
                    reference = FirebaseDatabase.getInstance().getReference().child("Users");
                    Query query = reference.orderByChild("username").startAt(text).endAt(text + "\uf8ff");
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            List<Users> searchList = new ArrayList<>();
                            searchList.clear();
                            if (snapshot.exists()) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    Users user = dataSnapshot.getValue(Users.class);
                                    for (String number : contactsList) {
                                        if (user.getPhoneNumber().trim().equals(number) ||
                                                user.getFullPhoneNumber().equals(number)) {
                                            searchList.add(user);
                                            Log.d("sie", String.valueOf(searchList));
                                        }
                                    }
                                    for (String number : blockLists){
                                        if (user.getFullPhoneNumber().equals(number)){
                                            searchList.remove(user);
                                        }
                                    }
                                }
                                adapter = new UsersAdapter(getContext(), searchList);
                                adapter.notifyDataSetChanged();
                                recyclerView.setAdapter(adapter);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }

    private void getBlocList() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("BlockList")
                .child(user.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        blockedNumber = dataSnapshot.child("phoneNumber").getValue().toString();
                        blockLists.add(blockedNumber);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getContactList() {
        Cursor cursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        while (cursor.moveToNext()) {
            String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if (contactsList.contains(phone)) {
                cursor.moveToNext();
            } else {
                contactsList.add(phone);

            }
        }
        cursor.close();
        getActualUser();
    }

    private void getActualUser() {
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Users> actualuserList = new ArrayList<>();
                actualuserList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Users user = dataSnapshot.getValue(Users.class);
                        for (String number : contactsList) {
                            if (user.getPhoneNumber().trim().equals(number) ||
                                    user.getFullPhoneNumber().equals(number)) {
                                actualuserList.add(user);
                                Log.d("sie", String.valueOf(actualuserList));
                            }
                        }
                        for (String number : blockLists){
                            if (user.getFullPhoneNumber().equals(number)){
                                actualuserList.remove(user);
                            }
                        }
                    }
                    adapter = new UsersAdapter(getContext(), actualuserList);
                    adapter.notifyDataSetChanged();
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getContactList();

    }
}