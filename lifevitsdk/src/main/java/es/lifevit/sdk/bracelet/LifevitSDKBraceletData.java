package es.lifevit.sdk.bracelet;

        import java.util.ArrayList;


public class LifevitSDKBraceletData {

    private ArrayList<LifevitSDKHeartbeatData> heartData = new ArrayList<>();
    private ArrayList<LifevitSDKStepData> stepsData = new ArrayList<>();
    private ArrayList<LifevitSDKSleepData> sleepData = new ArrayList<>();

    public ArrayList<LifevitSDKHeartbeatData> getHeartData() {
        return heartData;
    }

    public void setHeartData(ArrayList<LifevitSDKHeartbeatData> heartData) {
        this.heartData = heartData;
    }

    public ArrayList<LifevitSDKStepData> getStepsData() {
        return stepsData;
    }

    public void setStepsData(ArrayList<LifevitSDKStepData> stepsData) {
        this.stepsData = stepsData;
    }

    public ArrayList<LifevitSDKSleepData> getSleepData() {
        return sleepData;
    }

    public void setSleepData(ArrayList<LifevitSDKSleepData> sleepData) {
        this.sleepData = sleepData;
    }


}
