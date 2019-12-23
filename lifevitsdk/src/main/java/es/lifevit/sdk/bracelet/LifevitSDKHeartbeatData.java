package es.lifevit.sdk.bracelet;


public class LifevitSDKHeartbeatData {
    private long date;
    private int heartrate;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getHeartrate() {
        return heartrate;
    }

    public void setHeartrate(int heartrate) {
        this.heartrate = heartrate;
    }
}
