package es.lifevit.sdk.bracelet;


public class LifevitSDKStepData {
    private long date;
    private int steps;
    private float calories, distance;

    public LifevitSDKStepData(long date, int steps, float calories, float distance) {
        this.date = date;
        this.steps = steps;
        this.calories = calories;
        this.distance = distance;
    }


    public long getDate() {
        return date;
    }

    public void setDate(long date) {
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
}
