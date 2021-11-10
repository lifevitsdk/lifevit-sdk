package es.lifevit.sdk.bracelet;

import es.lifevit.sdk.LifevitSDKConstants;

public class LifevitSDKVitalAlarm extends LifevitSDKAlarmTime {

    private boolean enabled = false;
    private LifevitSDKConstants.BraceletVitalAlarmType type = LifevitSDKConstants.BraceletVitalAlarmType.ALARM;

    private String text = "Alarm";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LifevitSDKConstants.BraceletVitalAlarmType getType() {
        return type;
    }

    public void setType(LifevitSDKConstants.BraceletVitalAlarmType type) {
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
