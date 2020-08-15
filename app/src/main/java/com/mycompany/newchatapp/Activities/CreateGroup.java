package com.mycompany.newchatapp.Activities;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mycompany.newchatapp.Adapter.GroupMemberAdapter;
import com.mycompany.newchatapp.Model.ChatList;
import com.mycompany.newchatapp.Model.GroupInfo;
import com.mycompany.newchatapp.Model.Users;
import com.mycompany.newchatapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.mycompany.newchatapp.Activities.UserDetailsActivity.PICK_IMAGE;

public class CreateGroup extends AppCompatActivity {

    RecyclerView recyclerView;
    GroupMemberAdapter adapter;
    CircleImageView groupIcon;

    String download_url, groupId;
    Uri image_uri;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String userId = user.getUid();
    List<String> contactsList;
    List<String> blockLists;
    List<String> groupMembers;
    String blockedNumber;

    EditText search;
    ProgressDialog progressDialog;
    Button createGroup;
    DatabaseReference databaseReference, reference;
    StorageReference storageReference;
    TextInputLayout groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        recyclerView = findViewById(R.id.groupRecyclerView);
        createGroup = findViewById(R.id.createGroup);
        search = findViewById(R.id.search_user);
        groupName = findViewById(R.id.groupName);
        groupIcon = findViewById(R.id.groupIcon);
        progressDialog = new ProgressDialog(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        storageReference = FirebaseStorage.getInstance().getReference("Groups");
        contactsList = new ArrayList<>();
        blockLists = new ArrayList<>();
        groupMembers = new ArrayList<>();

        getBlocList();
        getContactList();

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = search.getText().toString().trim();
                if (text.equals("")) {
                    getActualUser();
                } else {
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
                                    for (String number : blockLists) {
                                        if (user.getFullPhoneNumber().equals(number)) {
                                            searchList.remove(user);
                                        }
                                    }
                                }
                                adapter = new GroupMemberAdapter(groupMembers, getApplicationContext(),
                                        searchList);
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

        createGroup.setOnClickListener(v -> createGroup());
        groupIcon.setOnClickListener(v -> {
            chooseImage();
        });

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
            groupIcon.setImageURI(image_uri);
        }
    }

    public String GetFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void createGroup() {
        String name = groupName.getEditText().getText().toString();
        databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        groupId = databaseReference.push().getKey();
        if (name.trim().equals("")) {
            Toast.makeText(this, "Group Name cannot be empty", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.setTitle("Please Wait...");
            progressDialog.show();
            if (image_uri != null) {
                final StorageReference reference = storageReference.child(groupId).child("Group Icon").
                        child(System.currentTimeMillis() + "." + GetFileExtension(image_uri));

                final UploadTask uploadTask = reference.putFile(image_uri);
                uploadTask.addOnFailureListener(e ->
                        Toast.makeText(CreateGroup.this, e.getMessage(), Toast.LENGTH_SHORT).show()).
                        addOnSuccessListener(taskSnapshot -> {
                            Task<Uri> uriTask = uploadTask.continueWithTask(task -> {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                download_url = reference.getDownloadUrl().toString();
                                return reference.getDownloadUrl();
                            }).addOnCompleteListener(task -> {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    download_url = task.getResult().toString();
                                    Calendar calendar = Calendar.getInstance();
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
                                    String currentDate = dateFormat.format(calendar.getTime());


                                    GroupInfo info = new GroupInfo(userId, name, currentDate, download_url, groupId);
                                    databaseReference.child(groupId).child("GroupInfo").setValue(info);
                                    ChatList list = new ChatList(userId);
                                    databaseReference.child(groupId).child("Members").child(userId).setValue(list);
                                    List<String> members = adapter.getUserIDs();
                                    for (String id : members) {
                                        ChatList chatList = new ChatList(id);
                                        databaseReference.child(groupId).child("Members").child(id).setValue(chatList);
                                    }
                                    Intent intent = new Intent(this, GroupChat.class);
                                    intent.putExtra("groupId", groupId);
                                    startActivity(intent);
                                }
                            });
                        });
            } else {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
                String currentDate = dateFormat.format(calendar.getTime());

                GroupInfo info = new GroupInfo(userId, name, currentDate, "", groupId);
                databaseReference.child(groupId).child("GroupInfo").setValue(info);
                ChatList list = new ChatList(userId);
                databaseReference.child(groupId).child("Members").child(userId).setValue(list);
                List<String> members = adapter.getUserIDs();
                for (String id : members) {
                    ChatList chatList = new ChatList(id);
                    databaseReference.child(groupId).child("Members").child(id).setValue(chatList);
                }
                Intent intent = new Intent(this, GroupChat.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        }
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
                        Log.d("BlockedNumber: ", blockedNumber);
                    }
                    Log.d("BlockedSize:", String.valueOf(blockLists.size()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getContactList() {
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
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
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Users> actualuserList = new ArrayList<>();
                actualuserList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    for (String number : contactsList) {
                        assert user != null;
                        if (user.getPhoneNumber().trim().equals(number) ||
                                user.getFullPhoneNumber().equals(number)) {
                            actualuserList.add(user);
                            Log.d("sie", String.valueOf(actualuserList));
                        }
                    }
                    for (String number : blockLists) {
                        if (user.getFullPhoneNumber().equals(number)) {
                            actualuserList.remove(user);
                        }
                    }
                    adapter = new GroupMemberAdapter(groupMembers, CreateGroup.this, actualuserList);
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