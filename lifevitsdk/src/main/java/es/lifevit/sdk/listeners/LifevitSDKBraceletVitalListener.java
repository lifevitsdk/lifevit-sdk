package es.lifevit.sdk.listeners;

import java.util.List;

import es.lifevit.sdk.LifevitSDKConstants;
import es.lifevit.sdk.bracelet.LifevitSDKBraceletData;
import es.lifevit.sdk.bracelet.LifevitSDKResponse;
import es.lifevit.sdk.bracelet.LifevitSDKStepData;
import es.lifevit.sdk.bracelet.LifevitSDKSummarySleepData;
import es.lifevit.sdk.bracelet.LifevitSDKSummaryStepData;

/**
 * Created by aescanuela on 4/8/17.
 */

public interface LifevitSDKBraceletVitalListener {

    void braceletVitalSOS(String device);

    void braceletVitalOperation(String device, LifevitSDKConstants.BraceletVitalOperation operation);

    void braceletVitalInformation(String device, LifevitSDKResponse message);

    void braceletVitalError(String device, LifevitSDKConstants.BraceletVitalError error, LifevitSDKConstants.BraceletVitalCommand command);
}
