package es.lifevit.sdk.bracelet;

public class LifevitSDKSummaryStepData extends LifevitSDKStepData {

    private int activeTime = 0;
    private int heartRate = 0;


    public LifevitSDKSummaryStepData(long date, int steps, float calories, float distance, int activeTime, int heartRate) {

        super(date, steps, calories, distance);

        this.activeTime = activeTime;
        this.heartRate = heartRate;

    }

    public int getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(int activeTime) {
        this.activeTime = activeTime;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

}
