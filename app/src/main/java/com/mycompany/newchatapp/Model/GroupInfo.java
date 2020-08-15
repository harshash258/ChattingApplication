package com.mycompany.newchatapp.Model;

public class GroupInfo {
    String admin, groupName, createdOn, groupIcon, groupId;

    public GroupInfo() {
    }

    public GroupInfo(String admin, String groupName, String createdOn, String groupIcon, String groupId) {
        this.admin = admin;
        this.groupName = groupName;
        this.createdOn = createdOn;
        this.groupIcon = groupIcon;
        this.groupId = groupId;
    }

    public String getGroupIcon() {
        return groupIcon;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getAdmin() {
        return admin;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getCreatedOn() {
        return createdOn;
    }
}
