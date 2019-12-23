package es.lifevit.sdk.pillreminder;

public class LifevitSDKPillReminderData {

    private int alarmNumber;
    private long date;
    private int request;

    public int getAlarmNumber() {
        return alarmNumber;
    }

    public void setAlarmNumber(int alarmNumber) {
        this.alarmNumber = alarmNumber;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getDate() {
        return date;
    }

    public void setRequest(int request) {
        this.request = request;
    }

    public int getRequest() {
        return request;
    }
}
