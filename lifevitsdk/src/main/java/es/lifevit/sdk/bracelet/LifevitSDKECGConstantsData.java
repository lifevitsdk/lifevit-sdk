package es.lifevit.sdk.bracelet;

public class LifevitSDKECGConstantsData {

    private long date = System.currentTimeMillis();
    private int identifier;
    private Integer hrv;

    private Integer vascularAging;
    private Integer heartrate;
    private Integer fatigue;
    private Integer systolic;
    private Integer diastolic;
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

    public Integer getVascularAging() {
        return vascularAging;
    }

    public void setVascularAging(Integer vascularAging) {
        this.vascularAging = vascularAging;
    }

    public Integer getHeartrate() {
        return heartrate;
    }

    public void setHeartrate(Integer heartrate) {
        this.heartrate = heartrate;
    }

    public Integer getFatigue() {
        return fatigue;
    }

    public void setFatigue(Integer fatigue) {
        this.fatigue = fatigue;
    }

    public Integer getSystolic() {
        return systolic;
    }

    public void setSystolic(Integer systolic) {
        this.systolic = systolic;
    }

    public Integer getDiastolic() {
        return diastolic;
    }

    public void setDiastolic(Integer diastolic) {
        this.diastolic = diastolic;
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
                ", vascularAging=" + vascularAging +
                ", heartrate=" + heartrate +
                ", fatigue=" + fatigue +
                ", systolic=" + systolic +
                ", diastolic=" + diastolic +
                ", breathRate=" + breathRate +
                '}';
    }
}
