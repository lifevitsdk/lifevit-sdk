package es.lifevit.sdk;

import es.lifevit.sdk.bracelet.LifevitSDKAppNotification;
import es.lifevit.sdk.bracelet.LifevitSDKVitalNotification;
import es.lifevit.sdk.utils.Utils;

public class LifevitSDKVitalParams {

    public boolean checkValidDeviceParameters() {
        return true;
    }

    public class Language {
        public static final int CHINESE = 0, ENGLISH = 1;
    }

    boolean distanceUnitKm = false;
    boolean hourDisplay24h = true;
    boolean wristSenseEnabled = true;
    boolean temperatureUnitCelsius = true;
    boolean nightMode = true;
    boolean ANCSEnabled = false;
    int basicHeartRateSetting= 1;
    int screenBrightness = 1;
    int dialInterface = 0;
    int language = Language.ENGLISH;
    int hand = LifevitSDKConstants.BRACELET_HAND_LEFT;

    private LifevitSDKVitalNotification notifications = new LifevitSDKVitalNotification();

    public LifevitSDKVitalParams(){

    }

    public boolean isDistanceUnitKm() {
        return distanceUnitKm;
    }

    public void setDistanceUnitKm(boolean distanceUnitKm) {
        this.distanceUnitKm = distanceUnitKm;
    }

    public boolean isHourDisplay24h() {
        return hourDisplay24h;
    }

    public void setHourDisplay24h(boolean hourDisplay24h) {
        this.hourDisplay24h = hourDisplay24h;
    }

    public boolean isWristSenseEnabled() {
        return wristSenseEnabled;
    }

    public void setWristSenseEnabled(boolean wristSenseEnabled) {
        this.wristSenseEnabled = wristSenseEnabled;
    }

    public boolean isTemperatureUnitCelsius() {
        return temperatureUnitCelsius;
    }

    public void setTemperatureUnitCelsius(boolean temperatureUnitCelsius) {
        this.temperatureUnitCelsius = temperatureUnitCelsius;
    }

    public boolean isNightMode() {
        return nightMode;
    }

    public void setNightMode(boolean nightMode) {
        this.nightMode = nightMode;
    }

    public boolean isANCSEnabled() {
        return ANCSEnabled;
    }

    public void setANCSEnabled(boolean ANCSEnabled) {
        this.ANCSEnabled = ANCSEnabled;
    }

    public int getBasicHeartRateSetting() {
        return basicHeartRateSetting;
    }

    public void setBasicHeartRateSetting(int basicHeartRateSetting) {
        this.basicHeartRateSetting = basicHeartRateSetting;
    }

    public int getScreenBrightness() {
        return screenBrightness;
    }

    public void setScreenBrightness(int screenBrightness) {
        this.screenBrightness = screenBrightness;
    }

    public int getDialInterface() {
        return dialInterface;
    }

    public void setDialInterface(int dialInterface) {
        this.dialInterface = dialInterface;
    }

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int language) {
        this.language = language;
    }

    public int getHand() {
        return hand;
    }

    public void setHand(int hand) {
        this.hand = hand;
    }

    public void setNotifications(LifevitSDKVitalNotification notifications) {
        this.notifications = notifications;
    }

    public LifevitSDKVitalNotification getNotifications() {
        return notifications;
    }

    @Override
    public String toString() {
        return "LifevitSDKBraceletParams{" +
                "distanceUnitKm=" + distanceUnitKm +
                ", hourDisplay24h=" + hourDisplay24h +
                ", wristSenseEnabled=" + wristSenseEnabled +
                ", temperatureUnitCelsius=" + temperatureUnitCelsius +
                ", nightMode=" + nightMode +
                ", ANCSEnabled=" + ANCSEnabled +
                ", basicHeartRateSetting=" + basicHeartRateSetting +
                ", screenBrightness=" + screenBrightness +
                ", dialInterface=" + dialInterface +
                ", language=" + language +
                ", hand=" + hand +
                ", notifications=" + notifications +
                '}';
    }
}
