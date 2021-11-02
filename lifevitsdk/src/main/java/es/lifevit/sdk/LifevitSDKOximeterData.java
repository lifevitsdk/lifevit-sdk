package es.lifevit.sdk;

/**
 * Created by aescanuela on 2/8/17.
 */

public class LifevitSDKOximeterData {

    private long date;
    private Integer spO2;
    private Integer pi;
    private Integer rpm;
    private Integer lpm;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public Integer getSpO2() {
        return spO2;
    }

    public void setSpO2(Integer spO2) {
        this.spO2 = spO2;
    }

    public Integer getPi() {
        return pi;
    }

    public void setPi(Integer pi) {
        this.pi = pi;
    }

    public Integer getRpm() {
        return rpm;
    }

    public void setRpm(Integer rpm) {
        this.rpm = rpm;
    }

    public Integer getLpm() {
        return lpm;
    }

    public void setLpm(Integer lpm) {
        this.lpm = lpm;
    }
}
