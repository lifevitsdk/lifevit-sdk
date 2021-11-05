package es.lifevit.sdk.bracelet;

public class LifevitSDKVitalAlarm extends LifevitSDKAlarmTime {
    public class Type {
        public static final int ALARM = 1, MEDICATION = 2, DRINK_WATER = 3;
    }

    private boolean enabled = false;
    private int type = Type.ALARM;

    private String text = "Alarm";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * The bracelet would only print the first 30 characters of this alarm text
     */
    public String getText() {
        return text;
    }

    /**
     * The bracelet would only print the first 30 characters
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "LifevitSDKVitalAlarm{" +
                "enabled=" + enabled +
                ", type=" + type +
                ", hour=" + hour +
                ", minute=" + minute +
                ", monday=" + monday +
                ", tuesday=" + tuesday +
                ", wednesday=" + wednesday +
                ", thursday=" + thursday +
                ", friday=" + friday +
                ", saturday=" + saturday +
                ", sunday=" + sunday +
                ", text='" + text + '\'' +
                '}';
    }
}
