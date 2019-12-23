package es.lifevit.sdk.listeners;

import java.util.List;

import es.lifevit.sdk.bracelet.LifevitSDKHeartbeatData;
import es.lifevit.sdk.bracelet.LifevitSDKSleepData;
import es.lifevit.sdk.bracelet.LifevitSDKStepData;

/**
 * Created by aescanuela on 4/8/17.
 */

public interface LifevitSDKBraceletAT250Listener {

    void braceletCurrentStepsReceived(LifevitSDKStepData stepData);

    void braceletSyncReceived(List<LifevitSDKStepData> stepsResultList, List<LifevitSDKSleepData> sleepResultList);

    void operationFinished(boolean finishedOk);

    void braceletHeartRateReceived(int value);

    void braceletHeartRateSyncReceived(List<LifevitSDKHeartbeatData> data);

    void firmwareVersion(String firmwareVersion);

    void isGoingToUpdateFirmware(boolean isGoingToUpdate);

}
