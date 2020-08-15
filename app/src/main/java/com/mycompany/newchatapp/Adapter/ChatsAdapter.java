package com.mycompany.newchatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mycompany.newchatapp.Activities.ChatActivity;
import com.mycompany.newchatapp.Model.Users;
import com.mycompany.newchatapp.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.Viewholder> {


    Context mContext;
    List<Users> mList;

    public ChatsAdapter(Context mContext, List<Users> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.chat_layout, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        final Users user = mList.get(position);
        String imageUrl = user.getProfilephotoURL();
        if (imageUrl.equals(""))
            holder.profilepic.setImageResource(R.mipmap.ic_launcher_round);
        else
            Glide.with(mContext).load(imageUrl).fitCenter().
                    placeholder(R.drawable.ic_user)
                    .into(holder.profilepic);
        holder.username.setText(user.getUsername());
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, ChatActivity.class);
            intent.putExtra("name", user.getUsername());
            intent.putExtra("firendPhoneNumber", user.getPhoneNumber());
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static public class Viewholder extends RecyclerView.ViewHolder {
        CircleImageView profilepic;
        TextView username, lastMessage;

        public Viewholder(@NonNull View itemView) {
            super(itemView);

            profilepic = itemView.findViewById(R.id.chatsprofilepic);
            username = itemView.findViewById(R.id.chatsUsername);

        }
    }
}