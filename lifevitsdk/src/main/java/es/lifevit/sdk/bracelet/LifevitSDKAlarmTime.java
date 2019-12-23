package es.lifevit.sdk.bracelet;


public class LifevitSDKAlarmTime {

    private boolean isSecondaryAlarm = false;

    private int hour = 0;
    private int minute = 0;

    private boolean monday = false;
    private boolean tuesday = false;
    private boolean wednesday = false;
    private boolean thursday = false;
    private boolean friday = false;
    private boolean saturday = false;
    private boolean sunday = false;


    public LifevitSDKAlarmTime() {

    }

    public LifevitSDKAlarmTime(boolean isSecondaryAlarm, int hour, int minute, boolean monday, boolean tuesday, boolean wednesday,
                               boolean thursday, boolean friday, boolean saturday, boolean sunday) {
        this.isSecondaryAlarm = isSecondaryAlarm;

        this.hour = hour;
        this.minute = minute;

        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.sunday = sunday;
    }


    public void setOnlyWeekDays() {

        monday = true;
        tuesday = true;
        wednesday = true;
        thursday = true;
        friday = true;
        saturday = false;
        sunday = false;
    }

    public void setOnlyWeekend() {

        monday = false;
        tuesday = false;
        wednesday = false;
        thursday = false;
        friday = false;
        saturday = true;
        sunday = true;
    }

    public boolean isSecondaryAlarm() {
        return isSecondaryAlarm;
    }

    public void setSecondaryAlarm(boolean secondaryAlarm) {
        isSecondaryAlarm = secondaryAlarm;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int h) {
        this.hour = h;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int m) {
        this.minute = m;
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
}
