package es.lifevit.sdk.listeners;

import java.util.List;

import es.lifevit.sdk.bracelet.LifevitSDKStepData;
import es.lifevit.sdk.bracelet.LifevitSDKSummarySleepData;
import es.lifevit.sdk.bracelet.LifevitSDKSummaryStepData;
import es.lifevit.sdk.bracelet.LifevitSDKBraceletData;

/**
 * Created by aescanuela on 4/8/17.
 */

public interface LifevitSDKBraceletAT2019Listener {

    void braceletCurrentStepsReceived(LifevitSDKStepData stepData);

    void braceletStepsReceived(List<LifevitSDKStepData> stepData);

    void braceletSummaryStepsReceived(LifevitSDKSummaryStepData stepData);

    void braceletSummarySleepReceived(LifevitSDKSummarySleepData sleepData);

    void braceletInformation(Object message);

    void braceletError(int errorCode);

    void braceletCurrentBattery(int battery);

    void braceletDataReceived(LifevitSDKBraceletData braceletData);
}
