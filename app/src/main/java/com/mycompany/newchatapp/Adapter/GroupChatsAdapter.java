package com.mycompany.newchatapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mycompany.newchatapp.Model.GroupInfo;
import com.mycompany.newchatapp.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatsAdapter extends RecyclerView.Adapter<GroupChatsAdapter.ViewHolder> {
    List<GroupInfo> mList;
    Context mContext;

    public GroupChatsAdapter(List<GroupInfo> mList, Context context) {
        this.mList = mList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.chat_layout, parent, false);
        return new GroupChatsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupInfo info = mList.get(position);
        holder.groupName.setText(info.getGroupName());
/*        if (info.getGroupIcon().equals(""))
            holder.groupIcon.setImageResource(R.drawable.ic_group);
        else
            Glide.with(mContext).load(info.getGroupIcon()).fitCenter().placeholder(R.drawable.ic_group)
                    .into(holder.groupIcon);*/
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView groupIcon;
        TextView groupName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupIcon = itemView.findViewById(R.id.chatsprofilepic);
            groupName = itemView.findViewById(R.id.chatsUsername);
        }
    }
}
