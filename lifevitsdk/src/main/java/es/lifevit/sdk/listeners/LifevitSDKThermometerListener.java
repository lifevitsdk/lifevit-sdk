package es.lifevit.sdk.listeners;

/**
 * Created by aescanuela on 4/8/17.
 */

public interface LifevitSDKThermometerListener {

    void onThermometerDeviceResult(int thermometerMode, int temperatureUnit, double temperatureValue);

    void onThermometerCommandSuccess(int command, final int data);

    void onThermometerDeviceError(int errorCode);
}
