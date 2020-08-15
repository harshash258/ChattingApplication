package com.mycompany.newchatapp.Model;

public class RoomInfo {

    String roomName, roomCode, createdBy, roomID;

    public RoomInfo() {
    }

    public RoomInfo(String groupName, String groupCode, String createby, String groupId) {
        this.roomName = groupName;
        this.roomCode = groupCode;
        this.createdBy = createby;
        this.roomID = groupId;
    }

    public String getRoomID() {
        return roomID;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}
