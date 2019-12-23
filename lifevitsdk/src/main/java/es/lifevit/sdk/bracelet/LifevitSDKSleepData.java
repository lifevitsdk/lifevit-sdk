package es.lifevit.sdk.bracelet;


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
}
