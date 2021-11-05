package es.lifevit.sdk.bracelet;

import java.util.ArrayList;

public class LifevitSDKBraceletECGWaveformData {
    private long date;
    private int ecgPoint;
    private int hrv, heartrate;
    //private int mood; //DEPRECATED
    private ArrayList<Integer> ecgData;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getEcgPoint() {
        return ecgPoint;
    }

    public void setEcgPoint(int ecgPoint) {
        this.ecgPoint = ecgPoint;
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
}
