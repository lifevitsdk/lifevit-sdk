package es.lifevit.sdk.bracelet;

import es.lifevit.sdk.utils.BraceletUtils;

public class LifevitSDKSedentaryAlarm {

    public enum SedentaryIntervals {PERIOD_30_MIN, PERIOD_60_MIN, PERIOD_90_MIN, PERIOD_120_MIN}

    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;
    private SedentaryIntervals intervalCode;

    public LifevitSDKSedentaryAlarm() {
    }

    public LifevitSDKSedentaryAlarm(int startHour, int startMinute, int endHour, int endMinute, SedentaryIntervals intervalCode) {
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
        this.intervalCode = intervalCode;
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

    public SedentaryIntervals getIntervalCode() {
        return intervalCode;
    }

    public void setIntervalCode(SedentaryIntervals intervalCode) {
        this.intervalCode = intervalCode;
    }

    public String formatStartTime() {
        return BraceletUtils.formatTime(startHour, startMinute);
    }

    public String formatEndTime() {
        return BraceletUtils.formatTime(endHour, endMinute);
    }

}
