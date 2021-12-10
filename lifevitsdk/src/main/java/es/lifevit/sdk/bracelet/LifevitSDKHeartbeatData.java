package es.lifevit.sdk.bracelet;


import java.util.Date;

public class LifevitSDKHeartbeatData {
    private long date = System.currentTimeMillis();
    private int heartRate;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    @Override
    public String toString() {

        Date d = new Date(date);
        return "LifevitSDKHeartbeatData{" +
                "date=" + d +
                ", heartrate=" + heartRate +
                '}';
    }
}
