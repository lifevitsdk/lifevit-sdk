package es.lifevit.sdk.bracelet;

import es.lifevit.sdk.utils.ByteUtils;

public class LifevitSDKBraceletVitalsData {
    long date;
    int hrv;
    int vascularAging, heartRate, fatigue, systolic, diastolic;

    public void setDate(long date) {
        this.date = date;
    }

    public long getDate() {
        return date;
    }

    public int getHrv() {
        return hrv;
    }

    public void setHrv(int hrv) {
        this.hrv = hrv;
    }

    public int getVascularAging() {
        return vascularAging;
    }

    public void setVascularAging(int vascularAging) {
        this.vascularAging = vascularAging;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public int getFatigue() {
        return fatigue;
    }

    public void setFatigue(int fatigue) {
        this.fatigue = fatigue;
    }

    public int getSystolic() {
        return systolic;
    }

    public void setSystolic(int systolic) {
        this.systolic = systolic;
    }

    public int getDiastolic() {
        return diastolic;
    }

    public void setDiastolic(int diastolic) {
        this.diastolic = diastolic;
    }
}
