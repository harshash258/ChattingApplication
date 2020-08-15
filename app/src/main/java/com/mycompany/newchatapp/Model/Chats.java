package com.mycompany.newchatapp.Model;

public class Chats {
    String senderId, message, messageID, receiverID, type;
    boolean seen;

    public Chats() {
    }

    public Chats(String senderId, String messageID, String message, String receiverID,
                 String type, boolean seen) {
        this.senderId = senderId;
        this.receiverID = receiverID;
        this.message = message;
        this.seen = seen;
        this.messageID = messageID;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getMessageID() {
        return messageID;
    }

    public boolean isSeen() {
        return seen;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public String getMessage() {
        return message;
    }
}
