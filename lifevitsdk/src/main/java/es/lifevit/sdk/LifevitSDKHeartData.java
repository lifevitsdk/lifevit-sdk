package es.lifevit.sdk;

/**
 * Created by aescanuela on 2/8/17.
 */

public class LifevitSDKHeartData {

    private double systolic;
    private double diastolic;
    private double pulse;
    private long date;
    private int errorCode;
    private boolean isIHB;

    public double getSystolic() {
        return systolic;
    }

    public void setSystolic(double systolic) {
        this.systolic = systolic;
    }

    public double getDiastolic() {
        return diastolic;
    }

    public void setDiastolic(double diastolic) {
        this.diastolic = diastolic;
    }

    public double getPulse() {
        return pulse;
    }

    public void setPulse(double pulse) {
        this.pulse = pulse;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isIHB() {
        return isIHB;
    }

    public void setIHB(boolean IHB) {
        isIHB = IHB;
    }

}
