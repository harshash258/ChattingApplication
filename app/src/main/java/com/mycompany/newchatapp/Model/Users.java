package com.mycompany.newchatapp.Model;

public class Users {

    String username, phoneNumber, userId, profilephotoURL, status, email, aboutMe, fullPhoneNumber;
    Boolean selected;

    public Users() {
    }

    public Users(String username, String phoneNumber, String userId, String profilephotoURL,
                 String status, String email, String aboutMe, String fullPhoneNumber) {
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.userId = userId;
        this.profilephotoURL = profilephotoURL;
        this.status = status;
        this.email = email;
        this.aboutMe = aboutMe;
        this.fullPhoneNumber = fullPhoneNumber;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public String getFullPhoneNumber() {
        return fullPhoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public String getUsername() {
        return username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getUserId() {
        return userId;
    }

    public String getProfilephotoURL() {
        return profilephotoURL;
    }
}
