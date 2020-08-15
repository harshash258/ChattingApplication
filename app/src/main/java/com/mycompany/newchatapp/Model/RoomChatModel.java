package com.mycompany.newchatapp.Model;

public class RoomChatModel {
    String message, senderId, senderName, type, messageID;

    public RoomChatModel() {
    }

    public RoomChatModel(String message, String senderId, String senderName, String type, String messageID) {
        this.message = message;
        this.senderId = senderId;
        this.senderName = senderName;
        this.type = type;
        this.messageID = messageID;
    }

    public String getMessageID() {
        return messageID;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getType() {
        return type;
    }
}
