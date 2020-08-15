package com.mycompany.newchatapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mycompany.newchatapp.Model.RoomChatModel;
import com.mycompany.newchatapp.R;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

    public static final int MSG_LEFT = 0;
    public static final int MSG_RIGHT = 1;
    Context mContext;
    List<RoomChatModel> mList;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public RoomAdapter(Context mContext, List<RoomChatModel> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.room_chat_item_right, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.room_chat_item_left, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RoomChatModel messages = mList.get(position);
        holder.userName.setText(messages.getSenderName());
        holder.message.setText(messages.getMessage());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mList.get(position).getSenderId().equals(user.getUid()))
            return MSG_RIGHT;
        else
            return MSG_LEFT;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        ImageView messageImage;
        TextView userName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.show_message);
            messageImage = itemView.findViewById(R.id.messageImage);
            userName = itemView.findViewById(R.id.roomUserName);
        }
    }
}
