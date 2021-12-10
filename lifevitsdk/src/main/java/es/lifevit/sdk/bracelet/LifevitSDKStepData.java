package es.lifevit.sdk.bracelet;


import java.util.Date;

public class LifevitSDKStepData {
    protected Long date, activeTime, activeFastTime;
    protected int steps;
    protected float calories, distance;

    public LifevitSDKStepData(){

    }

    public LifevitSDKStepData(long date, int steps, float calories, float distance) {
        this.date = date;
        this.steps = steps;
        this.calories = calories;
        this.distance = distance;
    }

    public Long getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(Long activeTime) {
        this.activeTime = activeTime;
    }

    public void setActiveFastTime(Long activeFastTime) {
        this.activeFastTime = activeFastTime;
    }

    public Long getActiveFastTime() {
        return activeFastTime;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public float getCalories() {
        return calories;
    }

    public void setCalories(float calories) {
        this.calories = calories;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        Date d = new Date(date);
        return "LifevitSDKStepData{" +
                "date=" + d +
                ", time=" + activeTime +
                ", activeTime=" + activeFastTime +
                ", steps=" + steps +
                ", calories=" + calories +
                ", distance=" + distance +
                '}';
    }
}
