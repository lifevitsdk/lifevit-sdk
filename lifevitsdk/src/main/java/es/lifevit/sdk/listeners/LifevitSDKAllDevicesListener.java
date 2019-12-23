package es.lifevit.sdk.listeners;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import es.lifevit.sdk.LifevitSDKDeviceScanData;

/**
 * Created by aescanuela on 4/8/17.
 */

public interface LifevitSDKAllDevicesListener {

    void allDevicesDetected(ConcurrentHashMap<Integer, List<LifevitSDKDeviceScanData>> allResults);

}
