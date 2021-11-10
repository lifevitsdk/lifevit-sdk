package es.lifevit.sdk.bracelet;

public class LifevitSDKDistanceTimeData {
    private int distance;
    private int time;
    private int gpsSignal;

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getGpsSignal() {
        return gpsSignal;
    }

    public void setGpsSignal(int gpsSignal) {
        this.gpsSignal = gpsSignal;
    }

    @Override
    public String toString() {
        return "LifevitSDKDistanceTimeData{" +
                "distance=" + distance +
                ", time=" + time +
                ", gpsSignal=" + gpsSignal +
                '}';
    }
}
