package es.lifevit.sdk.listeners;

import java.util.List;

import es.lifevit.sdk.LifevitSDKHeartData;

/**
 * Created by aescanuela on 4/8/17.
 */

public interface LifevitSDKTensiobraceletListener {

    void tensiobraceletOnMeasurement(int pulse);

    void tensiobraceletResult(LifevitSDKHeartData result);

    void tensiobraceletHistoricResults(List<LifevitSDKHeartData> result);

    void tensiobraceletError(int errorCode);

    void tensiobraceletCommandReceived();

}
