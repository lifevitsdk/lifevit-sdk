package es.lifevit.sdk.bracelet;

import java.util.ArrayList;

public class LifevitSDKVitalECGWaveform {
    private long date;
    private int totalPoints;
    private int hrv, heartrate, breath;
    private int pr, qt, qtc, trs;
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
        calculateExtraValues();
    }

    public int getBreath() {
        return breath;
    }

    public void setBreath(int breath) {
        this.breath = breath;
    }

    public int getPr() {
        return pr;
    }

    public void setPr(int pr) {
        this.pr = pr;
    }

    public int getQt() {
        return qt;
    }

    public void setQt(int qt) {
        this.qt = qt;
    }

    public int getQtc() {
        return qtc;
    }

    public void setQtc(int qtc) {
        this.qtc = qtc;
    }

    public int getTrs() {
        return trs;
    }

    public void setTrs(int trs) {
        this.trs = trs;
    }

    public ArrayList<Integer> getEcgData() {
        return ecgData;
    }

    public void setEcgData(ArrayList<Integer> ecgData) {
        this.ecgData = ecgData;
    }


    private void calculateExtraValues() {
        double d = (double) getHeartrate();
        double d2 = (double) (-((int) (((Math.random() * 11.0d) - 0.875d) + d)));
        double d3 = (0.75d * d2) + 470.0d;
        int i2 = (int) d3;
        int pow = (int) (d3 / Math.pow((double) (60.0f / ((float) getHeartrate())), 0.33000001311302185d));

        pr = (int) ((0.5d * d2) + 220.0d);
        qt = i2;
        qtc = pow;
        trs = (int) ((d2 * 0.25d) + 110.0d);
    }

    @Override
    public String toString() {
        return "LifevitSDKBraceletECGWaveformData{" +
                "\"date\":" + date +
                ", \"totalPoints\":" + totalPoints +
                ", \"hrv\":" + hrv +
                ", \"heartrate\":" + heartrate +
                ", \"breath\":" + breath +
                ", \"pr\":" + pr +
                ", \"qt\":" + qt +
                ", \"qtc\":" + qtc +
                ", \"trs\":" + trs +
                ", \"ecgData\":" + ecgData +
                '}';
    }
}
