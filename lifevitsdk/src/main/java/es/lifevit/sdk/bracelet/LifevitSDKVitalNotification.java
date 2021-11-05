package es.lifevit.sdk.bracelet;

public class LifevitSDKVitalNotification {



    private boolean call = false;
    private boolean mobileInformation = false;
    private boolean wechat = false;
    private boolean facebook = false;
    private boolean instagram = false;
    private boolean skype = false;
    private boolean telegram = false;
    private boolean twitter = false;
    private boolean vkclient = false;
    private boolean whatsapp = false;
    private boolean qq = false;
    private boolean in = false;


    public LifevitSDKVitalNotification() {
    }

    public boolean isCall() {
        return call;
    }

    public void setCall(boolean call) {
        this.call = call;
    }

    public boolean isMobileInformation() {
        return mobileInformation;
    }

    public void setMobileInformation(boolean mobileInformation) {
        this.mobileInformation = mobileInformation;
    }

    public boolean isWechat() {
        return wechat;
    }

    public void setWechat(boolean wechat) {
        this.wechat = wechat;
    }

    public boolean isFacebook() {
        return facebook;
    }

    public void setFacebook(boolean facebook) {
        this.facebook = facebook;
    }

    public boolean isInstagram() {
        return instagram;
    }

    public void setInstagram(boolean instagram) {
        this.instagram = instagram;
    }

    public boolean isSkype() {
        return skype;
    }

    public void setSkype(boolean skype) {
        this.skype = skype;
    }

    public boolean isTelegram() {
        return telegram;
    }

    public void setTelegram(boolean telegram) {
        this.telegram = telegram;
    }

    public boolean isTwitter() {
        return twitter;
    }

    public void setTwitter(boolean twitter) {
        this.twitter = twitter;
    }

    public boolean isVkclient() {
        return vkclient;
    }

    public void setVkclient(boolean vkclient) {
        this.vkclient = vkclient;
    }

    public boolean isWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(boolean whatsapp) {
        this.whatsapp = whatsapp;
    }

    public boolean isQq() {
        return qq;
    }

    public void setQq(boolean qq) {
        this.qq = qq;
    }

    public boolean isIn() {
        return in;
    }

    public void setIn(boolean in) {
        this.in = in;
    }

    @Override
    public String toString() {
        return "LifevitSDKVitalNotification{" +
                "call=" + call +
                ", mobileInformation=" + mobileInformation +
                ", wechat=" + wechat +
                ", facebook=" + facebook +
                ", instagram=" + instagram +
                ", skype=" + skype +
                ", telegram=" + telegram +
                ", twitter=" + twitter +
                ", vkclient=" + vkclient +
                ", whatsapp=" + whatsapp +
                ", qq=" + qq +
                ", in=" + in +
                '}';
    }
}
