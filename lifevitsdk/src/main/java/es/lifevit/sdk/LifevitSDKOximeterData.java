package es.lifevit.sdk;

/**
 * Created by aescanuela on 2/8/17.
 */

public class LifevitSDKOximeterData {

    private long date;
    private int spO2;
    private int pi;
    private int rpm;
    private int lpm;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getSpO2() {
        return spO2;
    }

    public void setSpO2(int spO2) {
        this.spO2 = spO2;
    }

    public int getPi() {
        return pi;
    }

    public void setPi(int pi) {
        this.pi = pi;
    }

    public int getRpm() {
        return rpm;
    }

    public void setRpm(int rpm) {
        this.rpm = rpm;
    }

    public int getLpm() {
        return lpm;
    }

    public void setLpm(int lpm) {
        this.lpm = lpm;
    }
    
}
