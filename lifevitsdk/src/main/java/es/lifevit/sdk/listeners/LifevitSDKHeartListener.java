package es.lifevit.sdk.listeners;

import es.lifevit.sdk.LifevitSDKHeartData;

/**
 * Created by aescanuela on 4/8/17.
 */

public interface LifevitSDKHeartListener {

    void heartDeviceOnProgressMeasurement(int pulse);

    void heartDeviceOnBatteryResult(int battery);

    void heartDeviceOnResult(LifevitSDKHeartData result);

}
