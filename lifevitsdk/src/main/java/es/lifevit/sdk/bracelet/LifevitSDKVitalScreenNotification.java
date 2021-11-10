package es.lifevit.sdk.bracelet;

import es.lifevit.sdk.LifevitSDKConstants;

public class LifevitSDKVitalScreenNotification {

    private LifevitSDKConstants.BraceletVitalNotification type = LifevitSDKConstants.BraceletVitalNotification.CALL;
    private String text = "";
    private String contact = "";

    public LifevitSDKConstants.BraceletVitalNotification getType() {
        return type;
    }

    public void setType(LifevitSDKConstants.BraceletVitalNotification type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    @Override
    public String toString() {
        return "LifevitVitalANCSNotif{" +
                "type=" + type.value +
                ", text='" + text + '\'' +
                ", contact='" + contact + '\'' +
                '}';
    }
}
