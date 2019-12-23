package es.lifevit.sdk.listeners;

import es.lifevit.sdk.LifevitSDKOximeterData;

/**
 * Created by aescanuela on 4/8/17.
 */

public interface LifevitSDKWeightScaleListener {

    void onScaleMeasurementOnlyWeight(double weight, int unit);

    void onScaleMeasurementAllValues(double weight, int unit, double fatPercentage, double waterPercentage, double musclePercent, double bmrKcal, double visceralPercentage, double boneWeight);
}
