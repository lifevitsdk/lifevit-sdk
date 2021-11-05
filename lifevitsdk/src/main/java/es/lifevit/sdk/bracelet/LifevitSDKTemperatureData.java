package es.lifevit.sdk.bracelet;


import java.util.Date;

public class LifevitSDKTemperatureData {
    private long date;
    private double value;
    private int unit;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public int getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        Date d = new Date(date);
        return "LifevitSDKTemperatureData{" +
                "date=" + d +
                ", value=" + value +
                ", unit=" + unit +
                '}';
    }
}
