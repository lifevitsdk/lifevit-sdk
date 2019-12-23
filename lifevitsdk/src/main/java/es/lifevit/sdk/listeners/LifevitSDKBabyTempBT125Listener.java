package es.lifevit.sdk.listeners;


public interface LifevitSDKBabyTempBT125Listener {

    void onBabyTempDataReady(double bodyTemperature, double environmentTemperature);
}
