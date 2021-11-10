package es.lifevit.sdk.bracelet;

import java.util.ArrayList;

public class LifevitSDKVitalECGWaveform {
    private long date;
    private int totalPoints;
    private int hrv, heartrate;
    //private int mood; //DEPRECATED
    private ArrayList<Integer> ecgData = new ArrayList<>();

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public int getHrv() {
        return hrv;
    }

    public void setHrv(int hrv) {
        this.hrv = hrv;
    }

    public int getHeartrate() {
        return heartrate;
    }

    public void setHeartrate(int heartrate) {
        this.heartrate = heartrate;
    }

    public ArrayList<Integer> getEcgData() {
        return ecgData;
    }

    public void setEcgData(ArrayList<Integer> ecgData) {
        this.ecgData = ecgData;
    }

    @Override
    public String toString() {
        return "LifevitSDKBraceletECGWaveformData{" +
                "date=" + date +
                ", totalPoints=" + totalPoints +
                ", hrv=" + hrv +
                ", heartrate=" + heartrate +
                ", ecgData=" + ecgData +
                '}';
    }
}
