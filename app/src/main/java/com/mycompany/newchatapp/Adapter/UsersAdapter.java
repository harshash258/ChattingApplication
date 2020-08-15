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
import com.mycompany.newchatapp.Activities.ChatActivity;
import com.mycompany.newchatapp.Model.Users;
import com.mycompany.newchatapp.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    Context mContext;
    List<Users> mList;

    public UsersAdapter(Context mContext, List<Users> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.display_contacts, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Users users = mList.get(position);
        holder.name.setText(users.getUsername());
        holder.number.setText(users.getFullPhoneNumber());
        String url = users.getProfilephotoURL();
        if (url.equals(""))
            holder.circleImageView.setImageResource(R.drawable.ic_user);
        else
            Glide.with(mContext).load(url).into(holder.circleImageView);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, ChatActivity.class);
            intent.putExtra("name", users.getUsername());
            intent.putExtra("firendPhoneNumber", users.getPhoneNumber());
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, number;
        CircleImageView circleImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.contactName);
            number = itemView.findViewById(R.id.contactNumber);
            circleImageView = itemView.findViewById(R.id.profilePic);
        }
    }
}
