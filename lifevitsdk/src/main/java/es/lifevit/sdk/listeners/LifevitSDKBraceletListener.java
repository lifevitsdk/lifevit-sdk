package es.lifevit.sdk.listeners;

import java.util.List;

import es.lifevit.sdk.bracelet.LifevitSDKHeartbeatData;
import es.lifevit.sdk.bracelet.LifevitSDKSleepData;
import es.lifevit.sdk.bracelet.LifevitSDKStepData;

/**
 * Created by aescanuela on 4/8/17.
 */

public interface LifevitSDKBraceletListener {

    void braceletBeepReceived();

    void braceletParameterSet(int parameter);

    void braceletSyncStepsReceived(List<LifevitSDKStepData> data);

    void braceletSyncSleepReceived(List<LifevitSDKSleepData> data);

    void braceletSyncHeartReceived(List<LifevitSDKHeartbeatData> data);

    void braceletActivityStarted();

    void braceletActivityFinished();

    void braceletActivityStepsReceived(int steps);

    void braceletCurrentStepsReceived(LifevitSDKStepData stepData);

    void braceletCurrentBattery(int battery);

    void braceletHeartDataReceived(int heartrate);

    void braceletInfoReceived(String info);

    void braceletError(int errorCode);

}
