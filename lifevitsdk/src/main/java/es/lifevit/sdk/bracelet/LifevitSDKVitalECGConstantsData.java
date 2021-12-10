package es.lifevit.sdk.bracelet;

public class LifevitSDKVitalECGConstantsData {

    private long date = System.currentTimeMillis();
    private int identifier;
    private Integer hrv;
    private Integer heartRate;
    private Integer breathRate;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public Integer getHrv() {
        return hrv;
    }

    public void setHrv(Integer hrv) {
        this.hrv = hrv;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public Integer getBreathRate() {
        return breathRate;
    }

    public void setBreathRate(Integer breathRate) {
        this.breathRate = breathRate;
    }

    @Override
    public String toString() {
        return "LifevitSDKECGConstantsData{" +
                "date=" + date +
                ", identifier=" + identifier +
                ", hrv=" + hrv +
                ", heartrate=" + heartRate +
                ", breathRate=" + breathRate +
                '}';
    }
}
