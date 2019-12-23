package es.lifevit.sdk.pillreminder;

public class LifevitSDKPillReminderPerformanceData extends LifevitSDKPillReminderData {

    private long dateTaken;
    private int statusTaken;

    public void setDateTaken(long dateTaken) {
        this.dateTaken = dateTaken;
    }

    public long getDateTaken() {
        return dateTaken;
    }

    public void setStatusTaken(int statusTaken) {
        this.statusTaken = statusTaken;
    }

    public int getStatusTaken() {
        return statusTaken;
    }
}
