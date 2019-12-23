package es.lifevit.sdk.bracelet;

public class LifevitSDKSummarySleepData {

    private long date = 0;
    private int awakes = 0;
    private int totalLightMinutes = 0;
    private int totalDeepMinutes = 0;


    public LifevitSDKSummarySleepData(long date, int awakes, int totalLightMinutes, int totalDeepMinutes) {

        this.setDate(date);
        this.setAwakes(awakes);
        this.setTotalLightMinutes(totalLightMinutes);
        this.setTotalDeepMinutes(totalDeepMinutes);

    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getAwakes() {
        return awakes;
    }

    public void setAwakes(int awakes) {
        this.awakes = awakes;
    }

    public int getTotalLightMinutes() {
        return totalLightMinutes;
    }

    public void setTotalLightMinutes(int totalLightMinutes) {
        this.totalLightMinutes = totalLightMinutes;
    }

    public int getTotalDeepMinutes() {
        return totalDeepMinutes;
    }

    public void setTotalDeepMinutes(int totalDeepMinutes) {
        this.totalDeepMinutes = totalDeepMinutes;
    }
}