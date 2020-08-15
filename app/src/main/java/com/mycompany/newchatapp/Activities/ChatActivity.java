package com.mycompany.newchatapp.Activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mycompany.newchatapp.Adapter.MessageAdapter;
import com.mycompany.newchatapp.Model.BlockList;
import com.mycompany.newchatapp.Model.ChatList;
import com.mycompany.newchatapp.Model.Chats;
import com.mycompany.newchatapp.OfflineCapabilities;
import com.mycompany.newchatapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.mycompany.newchatapp.Activities.UserDetailsActivity.PICK_IMAGE;

public class ChatActivity extends AppCompatActivity {

    TextView username, status;
    CircleImageView profilepic;
    ImageView gallery, document;
    ImageButton send, chooseImage;
    EditText editTextMessage;
    RecyclerView recyclerView;
    MessageAdapter adapter;
    List<Chats> mList;
    Uri imageURI;
    NotificationManagerCompat compat;
    CardView cardView;

    String name, friendPhoneNumber, friendId, userId, imageurl, download_url, myPhone;
    boolean isShown = false;

    RelativeLayout relativeLayout;
    FirebaseUser user;
    DatabaseReference databaseReference, chat, dbStatus, reverse, userChatList, friendChatList, db;
    StorageReference sendMedia;
    NotificationCompat.Builder notification;

    ValueEventListener seenListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");


        intialiseVariables();


        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getApplicationContext());
        linearLayout.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayout);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        mList = new ArrayList<>();


        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        friendPhoneNumber = intent.getStringExtra("firendPhoneNumber");
        username.setText(name);


        sendMedia = FirebaseStorage.getInstance().getReference().child(userId).child("Sent");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.orderByChild("phoneNumber").equalTo(friendPhoneNumber).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            friendId = childSnapshot.getKey();
                            displayDetails(friendId);
                            readMessage(userId, friendId);
                            seenMessage(friendId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        send.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString();
            sendMessage(message, "text");
            genrateChatList();
        });
        chooseImage.setOnClickListener(v -> {
            if (isShown) {
                cardView.setVisibility(View.GONE);
                isShown = false;
            } else {
                cardView.setVisibility(View.VISIBLE);
                isShown = true;
            }
        });
        gallery.setOnClickListener(v -> {
            chooseImage();
        });
        document.setOnClickListener(v -> {
            chooseDocument();
        });
        relativeLayout.setOnClickListener(v -> {
            Intent intent1 = new Intent(ChatActivity.this, ViewFriendProfile.class);
            intent1.putExtra("friendId", friendId);
            startActivity(intent1);
        });

        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {
                    updateTypingStatus("noOne");
                } else {
                    updateTypingStatus(friendId);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void intialiseVariables() {
        username = findViewById(R.id.friendname);
        profilepic = findViewById(R.id.friendpic);
        send = findViewById(R.id.sendMessage);
        status = findViewById(R.id.status);
        editTextMessage = findViewById(R.id.messageText);
        recyclerView = findViewById(R.id.recyclerView);
        chooseImage = findViewById(R.id.chooseImage);
        cardView = findViewById(R.id.cardView);
        gallery = findViewById(R.id.gallery);
        document = findViewById(R.id.docs);
        relativeLayout = findViewById(R.id.viewProfile);

        compat = NotificationManagerCompat.from(this);
    }

    // ------------------ Regarding Chating  ------------------------------------------
    // ------------------ Regarding Chating  ------------------------------------------
    // ------------------ Regarding Chating  ------------------------------------------
    private void displayDetails(String friendId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(friendId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imageurl = snapshot.child("profilephotoURL").getValue().toString();
                myPhone = snapshot.child("fullPhoneNumber").getValue().toString();
                String typing = snapshot.child("typingTo").getValue().toString();
                if (imageurl.equals(""))
                    profilepic.setImageResource(R.drawable.ic_user);
                else
                    Glide.with(ChatActivity.this).load(imageurl)
                            .placeholder(R.drawable.ic_user)
                            .fitCenter().into(profilepic);

                if (typing.equals(userId))
                    status.setText("typing...");
                else {

                    String userStatus = snapshot.child("status").getValue().toString();
                    status.setText(userStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void updateStatus(String status) {
        dbStatus = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(user.getUid());
        dbStatus.child("status").setValue(status);
    }

    private void readMessage(final String myId, final String friendId) {
        chat = FirebaseDatabase.getInstance().getReference().child("Message").child(myId + " - " + friendId);
        chat.keepSynced(true);
        chat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    mList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Chats chat = dataSnapshot.getValue(Chats.class);
                        mList.add(chat);
                        recyclerView.smoothScrollToPosition(mList.size() + 1);
                    }
                    adapter = new MessageAdapter(ChatActivity.this, mList);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference chat2 = FirebaseDatabase.getInstance().getReference().child("Message")
                .child(friendId + " - " + myId);
        chat2.keepSynced(true);
        chat2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    mList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Chats chat = dataSnapshot.getValue(Chats.class);
                        mList.add(chat);
                        recyclerView.smoothScrollToPosition(mList.size() + 1);
                    }
                    adapter = new MessageAdapter(ChatActivity.this, mList);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message, String type) {
        db = FirebaseDatabase.getInstance().getReference().child("Message")
                .child(userId + " - " + friendId);
        reverse = FirebaseDatabase.getInstance().getReference().child("Message")
                .child(friendId + " - " + userId);

        if (message.isEmpty()) {
            Toast.makeText(this, "Text cannot be empty", Toast.LENGTH_LONG).show();
        } else {
            String messageID = databaseReference.push().getKey();
            Log.d("messageId", messageID);
            Chats chat = new Chats(userId, messageID, message, friendId, type, false);
            reverse.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        reverse.child(messageID).setValue(chat);
                    } else {
                        db.child(messageID).setValue(chat);
                    }
                    editTextMessage.setText("");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            DatabaseReference notification = FirebaseDatabase.getInstance().getReference("Notifications");
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("from", userId);
            hashMap.put("type", "message");
            hashMap.put("typingTo", "noOne");
            String notificationkey = notification.push().getKey();
            notification.child(friendId).child(notificationkey).setValue(hashMap);
        }

    }

    private void updateTypingStatus(String status) {
        dbStatus = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(user.getUid());
        dbStatus.child("typingTo").setValue(status);
    }

    private void seenMessage(String friendId) {
        db = FirebaseDatabase.getInstance().getReference().child("Message")
                .child(userId + " - " + friendId);
        reverse = FirebaseDatabase.getInstance().getReference().child("Message")
                .child(friendId + " - " + userId);
        seenListener = db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Chats chat = dataSnapshot.getValue(Chats.class);
                        if (chat.getReceiverID().equals(userId) && chat.getSenderId().equals(friendId)) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("seen", true);
                            dataSnapshot.getRef().updateChildren(hashMap);
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        seenListener = reverse.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Chats chat = dataSnapshot.getValue(Chats.class);
                        if (chat.getReceiverID().equals(userId) && chat.getSenderId().equals(friendId)) {

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("seen", true);
                            dataSnapshot.getRef().updateChildren(hashMap);

                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void genrateChatList() {
        userChatList = FirebaseDatabase.getInstance().getReference().child("ChatsList")
                .child(userId);
        friendChatList = FirebaseDatabase.getInstance().getReference().child("ChatsList")
                .child(friendId);
        ChatList chatList = new ChatList(friendId);
        ChatList chatList1 = new ChatList(userId);
        userChatList.child(friendId).setValue(chatList);
        friendChatList.child(userId).setValue(chatList1);
    }

// ------------------ Regarding Chating  ------------------------------------------
// ------------------ Regarding Chating  ------------------------------------------

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
    }

    private void chooseDocument() {
        String[] mimeTypes =
                {"application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                        "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                        "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                        "text/plain",
                        "application/pdf",
                        "application/zip"};

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(Intent.createChooser(intent, "ChooseFile"), 101);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageURI = data.getData();
            uploadImage();
        } else if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            imageURI = data.getData();
            uploadDocument();
        }

    }


    public String GetFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadDocument() {
        if (imageURI != null) {
            final StorageReference reference = sendMedia.
                    child(System.currentTimeMillis() + "." + GetFileExtension(imageURI));

            final UploadTask uploadTask = reference.putFile(imageURI);
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    double max = taskSnapshot.getTotalByteCount();
                    createUploadMediaNotification();
                    updateUploadMediaNotification((int) progress, (int) max);
                }
            }).addOnFailureListener(e ->
                    Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show()).
                    addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> uriTask = uploadTask.continueWithTask(task -> {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            download_url = reference.getDownloadUrl().toString();
                            return reference.getDownloadUrl();
                        }).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                download_url = task.getResult().toString();
                                sendMessage(download_url, "document");
                                Toast.makeText(ChatActivity.this, "Sent Successfully",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
        } else {
            Toast.makeText(ChatActivity.this, "Nothing Selected",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage() {
        if (imageURI != null) {
            final StorageReference reference = sendMedia.
                    child(System.currentTimeMillis() + "." + GetFileExtension(imageURI));

            final UploadTask uploadTask = reference.putFile(imageURI);
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    double max = taskSnapshot.getTotalByteCount();
                    createUploadMediaNotification();
                    updateUploadMediaNotification((int) progress, (int) max);
                }
            }).addOnFailureListener(e ->
                    Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show()).
                    addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> uriTask = uploadTask.continueWithTask(task -> {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            download_url = reference.getDownloadUrl().toString();
                            return reference.getDownloadUrl();
                        }).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                download_url = task.getResult().toString();
                                sendMessage(download_url, "image");
                                Toast.makeText(ChatActivity.this, "Sent Successfully",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
        } else {
            Toast.makeText(ChatActivity.this, "Nothing Selected",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void createUploadMediaNotification() {
        notification = new NotificationCompat.Builder(this, OfflineCapabilities.CHANNEL_2_ID)
                .setSmallIcon(R.drawable.ic_baseline_cloud_upload_24)
                .setContentTitle("Uploading Media")
                .setContentText("Uploading in Progress")
                .setProgress(100, 0, false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        compat.notify(2, notification.build());
    }

    private void updateUploadMediaNotification(int progress, int maximum) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (progress != 100) {
                    notification.setProgress(100, progress, false);
                } else {
                    notification.setContentText("Uploading Finished")
                            .setProgress(0, 0, false)
                            .setOngoing(false);
                }
                compat.notify(2, notification.build());
                SystemClock.sleep(1000);
            }
        }).start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus("online");

    }

    @Override
    protected void onPause() {
        super.onPause();
        updateStatus("offline");
        updateTypingStatus("noOne");
        db.removeEventListener(seenListener);
        reverse.removeEventListener(seenListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.block_user:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Block User");
                builder.setIcon(R.drawable.ic_block);
                builder.setCancelable(false);
                builder.setMessage("Do you wan to block User?");
                builder.setPositiveButton("Yes", (dialog, which) -> {

                    BlockList blockList = new BlockList(myPhone);
                    DatabaseReference blockUser = FirebaseDatabase.getInstance().
                            getReference("BlockList").child(userId);
                    blockUser.child(friendId).setValue(blockList);
                    blockUser.child(friendId).child("power").setValue("true");
                    blockUser.child(friendId).child("friendId").setValue(friendId);

                    BlockList blockList1 = new BlockList(user.getPhoneNumber());
                    blockUser = FirebaseDatabase.getInstance().
                            getReference("BlockList").child(friendId);
                    blockUser.child(userId).setValue(blockList1);
                    blockUser.child(userId).child("power").setValue("false");
                    blockUser.child(userId).child("userId").setValue(userId);

                    databaseReference = FirebaseDatabase.getInstance().getReference().child("ChatsList")
                            .child(userId);
                    databaseReference.child(friendId).removeValue();
                    friendChatList = FirebaseDatabase.getInstance().getReference().child("ChatsList")
                            .child(friendId);
                    friendChatList.child(userId).removeValue();
                    Intent intent = new Intent(ChatActivity.this, MainScreen.class);
                    startActivity(intent);

                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                break;
            case R.id.scheduleMessage:
                Intent intent = new Intent(this, ScheduleMessage.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}