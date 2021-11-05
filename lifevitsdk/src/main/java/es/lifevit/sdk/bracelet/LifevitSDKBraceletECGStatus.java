package es.lifevit.sdk.bracelet;

public class LifevitSDKBraceletECGStatus {

    private int status;
    private LifevitSDKECGConstantsData data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LifevitSDKECGConstantsData getData() {
        return data;
    }

    public void setData(LifevitSDKECGConstantsData data) {
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
