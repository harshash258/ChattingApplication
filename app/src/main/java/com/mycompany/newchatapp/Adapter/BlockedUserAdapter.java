package com.mycompany.newchatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mycompany.newchatapp.Activities.ManageBlockedUser;
import com.mycompany.newchatapp.Model.Users;
import com.mycompany.newchatapp.R;

import java.util.List;

public class BlockedUserAdapter extends RecyclerView.Adapter<BlockedUserAdapter.ViewHolder> {
    Context mContext;
    List<Users> mUsers;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String userId = user.getUid();
    DatabaseReference databaseReference;

    public BlockedUserAdapter(Context mContext, List<Users> mUsers) {
        this.mContext = mContext;
        this.mUsers = mUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.block_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users users = mUsers.get(position);
        holder.name.setText(users.getUsername());
        holder.number.setText(users.getFullPhoneNumber());
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference = FirebaseDatabase.getInstance().getReference("BlockList").
                        child(userId);
                databaseReference.child(users.getUserId()).removeValue();
                databaseReference = FirebaseDatabase.getInstance().getReference("BlockList").
                        child(users.getUserId());
                databaseReference.child(userId).removeValue();
                Toast.makeText(mContext, "User Un-Blocked", Toast.LENGTH_SHORT).show();
                /*Intent intent = new Intent(mContext, ManageBlockedUser.class);
                mContext.startActivity(intent);*/

            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    static public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, number;
        Button button;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.contactName);
            number = itemView.findViewById(R.id.contactNumber);
            button = itemView.findViewById(R.id.approve);
        }
    }
}
