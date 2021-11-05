package es.lifevit.sdk.bracelet;

public class LifevitSDKBraceletECGData {

    private int identifier;
    private long date;
    private int value;

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "LifevitSDKBraceletECGData{" +
                "identifier=" + identifier +
                ", date=" + date +
                ", value=" + value +
                '}';
    }
}
