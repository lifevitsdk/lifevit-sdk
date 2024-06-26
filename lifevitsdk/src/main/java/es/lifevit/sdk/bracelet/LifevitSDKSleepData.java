package es.lifevit.sdk.bracelet;


import java.util.Date;

public class LifevitSDKSleepData {
    private long date;
    private int sleepDuration, sleepDeepness;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getSleepDuration() {
        return sleepDuration;
    }

    public void setSleepDuration(int sleepDuration) {
        this.sleepDuration = sleepDuration;
    }

    public int getSleepDeepness() {
        return sleepDeepness;
    }

    public void setSleepDeepness(int sleepDeepness) {
        this.sleepDeepness = sleepDeepness;
    }

    @Override
    public String toString() {

        Date d = new Date(date);
        return "LifevitSDKSleepData{" +
                "date=" + d +
                ", sleepDuration=" + sleepDuration +
                ", sleepDeepness=" + sleepDeepness +
                '}';
    }
}
