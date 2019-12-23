package es.lifevit.sdk.bracelet;

public class LifevitSDKMonitoringAlarm extends LifevitSDKSedentaryAlarm {

    private boolean monday = false;
    private boolean tuesday = false;
    private boolean wednesday = false;
    private boolean thursday = false;
    private boolean friday = false;
    private boolean saturday = false;
    private boolean sunday = false;


    public LifevitSDKMonitoringAlarm(int startHour, int startMinute, int endHour, int endMinute){
        this.setStartHour(startHour);
        this.setStartMinute(startMinute);
        this.setEndHour(endHour);
        this.setEndMinute(endMinute);
    }


    public LifevitSDKMonitoringAlarm(int startHour, int startMinute, int endHour, int endMinute, SedentaryIntervals intervalCode, boolean monday,
                                     boolean tuesday, boolean wednesday, boolean thursday, boolean friday, boolean saturday, boolean sunday) {

        super(startHour, startMinute, endHour, endMinute, intervalCode);

        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.sunday = sunday;
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
