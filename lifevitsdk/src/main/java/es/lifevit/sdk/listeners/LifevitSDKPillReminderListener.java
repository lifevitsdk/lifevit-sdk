package es.lifevit.sdk.listeners;

import es.lifevit.sdk.pillreminder.LifevitSDKPillReminderMessageData;

public interface LifevitSDKPillReminderListener {

    void pillReminderOnResult(Object info);
    void pillReminderOnError(LifevitSDKPillReminderMessageData info);

}
