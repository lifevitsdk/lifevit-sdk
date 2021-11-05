package es.lifevit.sdk.bracelet;

import java.util.Date;

public class LifevitSDKSummaryStepData extends LifevitSDKStepData {

    private int type = -1;
    private Integer heartRate;
    private Double temperature;

    public LifevitSDKSummaryStepData(){

    }

    public LifevitSDKSummaryStepData(long date, int steps, float calories, float distance, int activeTime, int heartRate) {

        super(date, steps, calories, distance);

        this.setActiveTime((long) activeTime);
        this.heartRate = heartRate;

    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    @Override
    public String toString() {
        Date d = new Date(date);
        return "LifevitSDKSummaryStepData{" +
                "date=" + d +
                ", time=" + time +
                ", activeTime=" + activeTime +
                ", steps=" + steps +
                ", calories=" + calories +
                ", distance=" + distance +
                ", type=" + type +
                ", heartRate=" + heartRate +
                ", temperature=" + temperature +
                '}';
    }
}
