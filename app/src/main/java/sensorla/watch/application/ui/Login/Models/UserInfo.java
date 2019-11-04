package sensorla.watch.application.ui.Login.Models;

public class UserInfo {
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public UserInfo() {}

    public UserInfo(String userId, String userName, String deviceId) {
        this.userId = userId;
        this.userName = userName;
        this.deviceId = deviceId;
    }

    private String userName;
    private String userId;
    private String deviceId;
}