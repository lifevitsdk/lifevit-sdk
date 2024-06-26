package es.lifevit.sdk.bracelet;

public class LifevitSDKVitalActivityPeriod {
    private int exerciseReminderPeriod = 60;
    private int minimumNumberSteps = 20;
    private boolean motionEnabled = true;

    private boolean monday = false;
    private boolean tuesday = false;
    private boolean wednesday = false;
    private boolean thursday = false;
    private boolean friday = false;
    private boolean saturday = false;
    private boolean sunday = false;


    private int startHour = 0;
    private int startMinute = 0;
    private int endHour = 23;
    private int endMinute = 59;

    public LifevitSDKVitalActivityPeriod(){
    }

    public void setAllDay(){
        startHour = 0;
        startMinute = 0;
        endHour = 23;
        endMinute = 59;
    }
    public void setWeekDays(boolean enabled){

        monday = enabled;
        tuesday = enabled;
        wednesday = enabled;
        thursday = enabled;
        friday = enabled;
    }
    public void setWeekend(boolean enabled){
        monday = enabled;
        sunday = enabled;
    }

    public int getExerciseReminderPeriod() {
        return exerciseReminderPeriod;
    }

    public void setExerciseReminderPeriod(int exerciseReminderPeriod) {
        this.exerciseReminderPeriod = exerciseReminderPeriod;
    }

    public int getMinimumNumberSteps() {
        return minimumNumberSteps;
    }

    public void setMinimumNumberSteps(int minimumNumberSteps) {
        this.minimumNumberSteps = minimumNumberSteps;
    }

    public boolean isMotionEnabled() {
        return motionEnabled;
    }

    public void setMotionEnabled(boolean motionEnabled) {
        this.motionEnabled = motionEnabled;
    }

    public boolean isMonday() {
        return monday;
    }

    public void setMonday(boolean monday) {
        this.monday = monday;
    }

    public boolean isTuesday() {
        return tuesday;
    }

    public void setTuesday(boolean tuesday) {
        this.tuesday = tuesday;
    }

    public boolean isWednesday() {
        return wednesday;
    }

    public void setWednesday(boolean wednesday) {
        this.wednesday = wednesday;
    }

    public boolean isThursday() {
        return thursday;
    }

    public void setThursday(boolean thursday) {
        this.thursday = thursday;
    }

    public boolean isFriday() {
        return friday;
    }

    public void setFriday(boolean friday) {
        this.friday = friday;
    }

    public boolean isSaturday() {
        return saturday;
    }

    public void setSaturday(boolean saturday) {
        this.saturday = saturday;
    }

    public boolean isSunday() {
        return sunday;
    }

    public void setSunday(boolean sunday) {
        this.sunday = sunday;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    @Override
    public String toString() {
        return "LifevitSDKVitalActivityPeriod{" +
                "exerciseReminderPeriod=" + exerciseReminderPeriod +
                ", minimumNumberSteps=" + minimumNumberSteps +
                ", motionEnabled=" + motionEnabled +
                ", monday=" + monday +
                ", tuesday=" + tuesday +
                ", wednesday=" + wednesday +
                ", thursday=" + thursday +
                ", friday=" + friday +
                ", saturday=" + saturday +
                ", sunday=" + sunday +
                ", startHour=" + startHour +
                ", startMinute=" + startMinute +
                ", endHour=" + endHour +
                ", endMinute=" + endMinute +
                '}';
    }
}
