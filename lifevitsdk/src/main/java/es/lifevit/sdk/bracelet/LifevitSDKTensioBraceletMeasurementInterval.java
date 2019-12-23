package es.lifevit.sdk.bracelet;


public class LifevitSDKTensioBraceletMeasurementInterval {

    public enum StartingEndingMinutes {O_CLOCK, HALF_PAST}

    public enum MinutesIntervals {
        INTERVAL_30_MIN, INTERVAL_60_MIN, INTERVAL_90_MIN, INTERVAL_120_MIN,
        INTERVAL_150_MIN, INTERVAL_180_MIN, INTERVAL_210_MIN, INTERVAL_240_MIN
    }


    private int startHour = 0;

    private StartingEndingMinutes startMinute = StartingEndingMinutes.O_CLOCK;

    private int finishHour = 0;

    private StartingEndingMinutes finishMinute = StartingEndingMinutes.O_CLOCK;

    private int currentIntervalNumber;
    private int totalIntervals;

    private MinutesIntervals measurementInterval;


    public LifevitSDKTensioBraceletMeasurementInterval() {
    }

    public LifevitSDKTensioBraceletMeasurementInterval(int startHour, StartingEndingMinutes startMinute, int finishHour, StartingEndingMinutes finishMinute,
                                                       int currentIntervalNumber, int totalIntervals, MinutesIntervals measurementInterval) {
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.finishHour = finishHour;
        this.finishMinute = finishMinute;
        this.currentIntervalNumber = currentIntervalNumber;
        this.totalIntervals = totalIntervals;
        this.measurementInterval = measurementInterval;
    }


    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public StartingEndingMinutes getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(StartingEndingMinutes startMinute) {
        this.startMinute = startMinute;
    }

    public int getFinishHour() {
        return finishHour;
    }

    public void setFinishHour(int finishHour) {
        this.finishHour = finishHour;
    }

    public StartingEndingMinutes getFinishMinute() {
        return finishMinute;
    }

    public void setFinishMinute(StartingEndingMinutes finishMinute) {
        this.finishMinute = finishMinute;
    }

    public int getCurrentIntervalNumber() {
        return currentIntervalNumber;
    }

    public void setCurrentIntervalNumber(int currentIntervalNumber) {
        this.currentIntervalNumber = currentIntervalNumber;
    }

    public int getTotalIntervals() {
        return totalIntervals;
    }

    public void setTotalIntervals(int totalIntervals) {
        this.totalIntervals = totalIntervals;
    }

    public MinutesIntervals getMeasurementInterval() {
        return measurementInterval;
    }

    public void setMeasurementInterval(MinutesIntervals measurementInterval) {
        this.measurementInterval = measurementInterval;
    }


    public int getMeasurementIntervalValue() {
        return (measurementInterval.ordinal() + 1) * 30;
    }


}
