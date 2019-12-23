package es.lifevit.sdk.listeners;

import es.lifevit.sdk.LifevitSDKOximeterData;

/**
 * Created by aescanuela on 4/8/17.
 */

public interface LifevitSDKOximeterListener {

    void oximeterDeviceOnProgressMeasurement(int pleth);

    void oximeterDeviceOnResult(LifevitSDKOximeterData data);
}
