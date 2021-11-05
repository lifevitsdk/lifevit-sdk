package es.lifevit.sdk.bracelet;


import java.util.Date;

public class LifevitSDKHeartbeatData {
    private long date = System.currentTimeMillis();
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

    @Override
    public String toString() {

        Date d = new Date(date);
        return "LifevitSDKHeartbeatData{" +
                "date=" + d +
                ", heartrate=" + heartrate +
                '}';
    }
}
