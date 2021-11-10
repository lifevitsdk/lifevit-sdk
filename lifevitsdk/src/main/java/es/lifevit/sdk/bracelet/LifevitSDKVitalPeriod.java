package es.lifevit.sdk.bracelet;

import es.lifevit.sdk.LifevitSDKBleDeviceBraceletVital;
import es.lifevit.sdk.LifevitSDKConstants;

public class LifevitSDKVitalPeriod{
    private LifevitSDKConstants.BraceletVitalDataType type;
    private LifevitSDKConstants.BraceletVitalPeriodWorkingMode workingMode;
    private int intervalTime;

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

    public LifevitSDKVitalPeriod(){
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

    public void setEnabled(boolean enabled) {
        this.workingMode = LifevitSDKConstants.BraceletVitalPeriodWorkingMode.OFF;
    }

    public boolean isEnabled() {
        return this.workingMode.value != LifevitSDKConstants.BraceletVitalPeriodWorkingMode.OFF.value;
    }

    public LifevitSDKConstants.BraceletVitalDataType getType() {
        return type;
    }

    public void setType(LifevitSDKConstants.BraceletVitalDataType type) {
        this.type = type;
    }

    public void setWorkingMode(LifevitSDKConstants.BraceletVitalPeriodWorkingMode workingMode) {
        this.workingMode = workingMode;

    }

    public LifevitSDKConstants.BraceletVitalPeriodWorkingMode getWorkingMode() {
        return workingMode;
    }

    public int getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(int intervalTime) {
        this.intervalTime = intervalTime;
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
        return "LifevitSDKVitalPeriod{" +
                ", workingMode=" + workingMode +
                ", intervalTime=" + intervalTime +
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
