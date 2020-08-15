package com.mycompany.newchatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mycompany.newchatapp.Activities.ViewMedia;
import com.mycompany.newchatapp.Model.Chats;
import com.mycompany.newchatapp.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_LEFT = 0;
    public static final int MSG_RIGHT = 1;

    Context mContext;
    List<Chats> mList;


    FirebaseUser user;

    public MessageAdapter(Context mContext, List<Chats> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Chats messages = mList.get(position);
        switch (messages.getType()) {
            case "text":
                holder.message.setText(messages.getMessage());
                break;
            case "image": {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                params.addRule(RelativeLayout.BELOW, R.id.messageImage);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                holder.seen.setLayoutParams(params);

                holder.messageImage.setVisibility(View.VISIBLE);
                holder.message.setVisibility(View.GONE);
                Glide.with(mContext).load(messages.getMessage()).fitCenter().into(holder.messageImage);
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(mContext, ViewMedia.class);
                    intent.putExtra("url", messages.getMessage());
                    mContext.startActivity(intent);
                });
                break;
            }
            case "document": {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                params.addRule(RelativeLayout.BELOW, R.id.messageDocument);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                holder.seen.setLayoutParams(params);

                holder.messageDocument.setVisibility(View.VISIBLE);
                holder.message.setVisibility(View.GONE);
                holder.messageDocument.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mList.get(position).getMessage()));
                    mContext.startActivity(intent);
                });
                break;
            }
        }
        if (position == mList.size()-1){
            if (messages.isSeen()){
                holder.seen.setText("seen");
            }else {
                holder.seen.setText("delivered");
            }
        }else
            holder.seen.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (mList.get(position).getSenderId().equals(user.getUid()))
            return MSG_RIGHT;
        else
            return MSG_LEFT;

    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        TextView seen;
        ImageView messageImage, messageDocument;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.show_message);
            seen = itemView.findViewById(R.id.seen);
            messageImage = itemView.findViewById(R.id.messageImage);
            messageDocument = itemView.findViewById(R.id.messageDocument);
        }
    }
}
