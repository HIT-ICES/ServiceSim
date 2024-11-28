package org.enduser;

@SuppressWarnings("unused")
public class EndUserInformation {

    private int endUserId;

    private int userLevel;


    public EndUserInformation(int endUserId, int userLevel) {
        this.endUserId = endUserId;
        this.userLevel = userLevel;
    }

    public int getEndUserId() {
        return endUserId;
    }

    @SuppressWarnings("unused")
    public void setEndUserId(int endUserId) {
        this.endUserId = endUserId;
    }

    public int getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(int userLevel) {
        this.userLevel = userLevel;
    }
}
