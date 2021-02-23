package es.lifevit.sdk.listeners;

/**
 * Created by aescanuela on 4/8/17.
 */

public interface LifevitSDKGlucometerListener {

    void onGlucometerDeviceResult(long date, double value);

    void onGlucometerCommandSuccess(int command, final int data);

    void onGlucometerDeviceError(int errorCode);
}
