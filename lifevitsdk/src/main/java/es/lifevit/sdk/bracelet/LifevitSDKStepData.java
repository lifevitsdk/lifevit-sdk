package es.lifevit.sdk.bracelet;


import java.util.Date;

public class LifevitSDKStepData {
    protected Long date, time, activeTime;
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

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public void setActiveTime(Long activeTime) {
        this.activeTime = activeTime;
    }

    public Long getActiveTime() {
        return activeTime;
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
                ", time=" + time +
                ", activeTime=" + activeTime +
                ", steps=" + steps +
                ", calories=" + calories +
                ", distance=" + distance +
                '}';
    }
}
