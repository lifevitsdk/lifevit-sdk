package es.lifevit.sdk.listeners;

import es.lifevit.sdk.weightscale.LifevitSDKWeightScaleData;

/**
 * Created by aescanuela on 4/8/17.
 */

public interface LifevitSDKWeightScaleListener {

    void onScaleMeasurementOnlyWeight(double weight, int unit);

    void onScaleTypeDetected(int type);

    void onScaleMeasurementAllValues(LifevitSDKWeightScaleData data);
}
