package es.lifevit.sdk.bracelet;

public class LifevitSDKVitalExerciseRecord {

    private long date;
    private int heartRate;
    private int steps;
    private double calories;
    private int exerciseTime;


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

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public int getExerciseTime() {
        return exerciseTime;
    }

    public void setExerciseTime(int exerciseTime) {
        this.exerciseTime = exerciseTime;
    }

    @Override
    public String toString() {
        return "LifevitSDKBraceletECGWaveformData{" +
                "date=" + date +
                ", heartRate=" + heartRate +
                ", steps=" + steps +
                ", calories=" + calories +
                ", exerciseTime=" + exerciseTime +
                '}';
    }

}
