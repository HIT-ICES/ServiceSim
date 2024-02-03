package org.enduser;

public class EndUserInformation {

    private int enduserId;

    private int userLevel;


    public EndUserInformation(int enduserId, int userLevel) {
        this.enduserId = enduserId;
        this.userLevel = userLevel;
    }

    public int getEnduserId() {
        return enduserId;
    }

    public void setEnduserId(int enduserId) {
        this.enduserId = enduserId;
    }

    public int getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(int userLevel) {
        this.userLevel = userLevel;
    }
}
