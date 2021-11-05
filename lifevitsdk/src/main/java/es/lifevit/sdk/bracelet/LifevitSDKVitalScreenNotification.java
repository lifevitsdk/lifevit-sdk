package es.lifevit.sdk.bracelet;

public class LifevitSDKVitalScreenNotification {
    public static class Type{
        public static final int STOP_CALL = 0xff, CALL = 0, INFO = 1, WECHAT = 2, FACEBOOK = 3, INSTAGRAM = 4, SKYPE = 5, TELEGRAM = 6, TWITTER = 7, VKCLIENT = 8, WHATSAPP = 9, QQ = 10, LINKEDIN = 11;
    }

    private int type = Type.CALL;
    private String text = "";
    private String contact = "";

    public int getType() {
        return type;
    }

    public void setType(int type) {
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
                "type=" + type +
                ", text='" + text + '\'' +
                ", contact='" + contact + '\'' +
                '}';
    }
}
