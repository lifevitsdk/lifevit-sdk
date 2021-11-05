package es.lifevit.sdk.listeners;

import java.util.List;

import es.lifevit.sdk.bracelet.LifevitSDKBraceletData;
import es.lifevit.sdk.bracelet.LifevitSDKStepData;
import es.lifevit.sdk.bracelet.LifevitSDKSummarySleepData;
import es.lifevit.sdk.bracelet.LifevitSDKSummaryStepData;

/**
 * Created by aescanuela on 4/8/17.
 */

public interface LifevitSDKBraceletVitalListener {

    void braceletSOS();

    void braceletOperation(int operation);

    void braceletInformation(Object message);

    void braceletError(int errorCode);

    void braceletCurrentBattery(int battery);
}
