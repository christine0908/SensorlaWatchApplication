package sensorla.watch.application.Model;

public class HeartRateModel {

    public HeartRateModel() {
    }

    public HeartRateModel(String userId, String value, String CreatedDate, String env) {
        this.User_Id = userId;
        this.Value = Value;
        this.CreatedDate = CreatedDate;
        this.env = env;
    }

    private String User_Id;
    private String Value;
    private String CreatedDate;
    private String env;
    private int HeartRateID;

    public String getUser_Id() {
        return User_Id;
    }

    public void setUser_Id(String User_Id) {
        this.User_Id = User_Id;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        this.Value = value;
    }

    public String getDatetime() {
        return CreatedDate;
    }

    public void setDatetime(String CreatedDate) {
        this.CreatedDate = CreatedDate;
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
