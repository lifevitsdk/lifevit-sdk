package es.lifevit.sdk.listeners;

import es.lifevit.sdk.LifevitSDKOximeterData;
import es.lifevit.sdk.weightscale.WeightScaleData;

/**
 * Created by aescanuela on 4/8/17.
 */

public interface LifevitSDKWeightScaleListener {

    void onScaleMeasurementOnlyWeight(double weight, int unit);

    void onWeightScaleDetected(int type);

    void onScaleMeasurementAllValues(WeightScaleData data);
}
