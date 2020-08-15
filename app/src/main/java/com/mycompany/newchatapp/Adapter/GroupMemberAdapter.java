package com.mycompany.newchatapp.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mycompany.newchatapp.Model.Users;
import com.mycompany.newchatapp.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {
    public List<String> userIDs = new ArrayList<>();
    Context mContext;
    List<Users> mList;

    public GroupMemberAdapter(List<String> userIDs, Context mContext, List<Users> mList) {
        this.userIDs = userIDs;
        this.mContext = mContext;
        this.mList = mList;
    }

    public List<String> getUserIDs() {
        return userIDs;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.display_group_contacts, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users users = mList.get(position);
        holder.name.setText(users.getUsername());
        holder.number.setText(users.getFullPhoneNumber());
        holder.checkBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                users.setSelected(true);
                userIDs.add(users.getUserId());
                Log.d("UserIds:", String.valueOf(userIDs.size()));
            } else {
                users.setSelected(false);
                userIDs.remove(users.getUserId());
                Log.d("UserIds:", String.valueOf(userIDs.size()));
            }

        });
        String url = users.getProfilephotoURL();
        if (url.equals(""))
            holder.circleImageView.setImageResource(R.drawable.ic_user);
        else
            Glide.with(mContext).load(url).into(holder.circleImageView);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, number;
        CircleImageView circleImageView;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.contactName);
            number = itemView.findViewById(R.id.contactNumber);
            circleImageView = itemView.findViewById(R.id.profilePic);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }
}
