package es.lifevit.sdk;

/**
 * Created by aescanuela on 4/8/17.
 */

public interface LifevitSDKListener {

    void heartDeviceOnConnectionError(int errorCode);

    void heartDeviceOnConnectionChanged(int status);

    void heartDeviceOnProgressMeasurement(int pulse);

    void heartDeviceOnBatteryResult(int battery);

    void heartDeviceOnResult(LifevitSDKHeartData result);

}
