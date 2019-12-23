package es.lifevit.sdk.listeners;

/**
 * Created by aescanuela on 4/8/17.
 */

public interface LifevitSDKDeviceListener {

    void deviceOnConnectionError(int deviceType, int errorCode);

    void deviceOnConnectionChanged(int deviceType, int status);

}
