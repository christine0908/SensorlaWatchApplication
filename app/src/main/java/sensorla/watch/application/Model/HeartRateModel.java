package sensorla.watch.application.Model;

public class HeartRateModel {

    public HeartRateModel() {
    }

    public HeartRateModel(String userId, String value, String datetime, String env) {
        this.UserId = userId;
        this.value = value;
        this.datetime = datetime;
        this.env = env;
    }

    private String UserId;
    private String value;
    private String datetime;
    private String env;
    private int HeartRateID;

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public int getHeartRateID() {
        return HeartRateID;
    }

    public void setHeartRateID(int heartRateID) {
        HeartRateID = heartRateID;
    }
}
