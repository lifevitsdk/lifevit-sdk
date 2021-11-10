package es.lifevit.sdk.bracelet;

public class LifevitSDKVitalECGStatus {

    private int status;
    private LifevitSDKVitalECGConstantsData data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LifevitSDKVitalECGConstantsData getData() {
        return data;
    }

    public void setData(LifevitSDKVitalECGConstantsData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "LifevitSDKBraceletECGStatus{" +
                "status=" + status +
                ", data=" + data +
                '}';
    }
}
