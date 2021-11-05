package es.lifevit.sdk.pillreminder;

/**
 * Created by aescanuela on 26/2/16.
 */


import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import es.lifevit.sdk.LifevitSDKBleDevicePillReminder;
import es.lifevit.sdk.LifevitSDKConstants;
import es.lifevit.sdk.utils.LogUtils;


public class PillReminderSendQueue extends Thread {

    private final static String TAG = PillReminderSendQueue.class.getSimpleName();

    private volatile LinkedBlockingQueue<PillReminderQueueItem> sendQueue = new LinkedBlockingQueue<>();
    private boolean queueIsBusy = false;
    private LifevitSDKBleDevicePillReminder reminder;
    private boolean finished;
    private long timeLaunched = 0;


    public PillReminderSendQueue(LifevitSDKBleDevicePillReminder bleDevice) {
        this.reminder = bleDevice;
        finished = false;
    }


    /**
     * Inner class
     */
    protected class PillReminderQueueItem {
        public int action;
        public Object object;

        public PillReminderQueueItem(int action, Object object) {
            this.action = action;
            this.object = object;
        }
    }


    public void addToQueue(int action) {
        addToQueue(action, 0);
    }

    public void addToQueue(int action, Object object) {
        if (!finished) {
            // Check if operation is already in queue
            boolean found = false;
           /* for(BraceletAT500HRQueueItem queueItem : sendQueue){
                if(action.equals(queueItem.action) && daysAgo == queueItem.daysAgo){
                    found = true;
                    break;
                }
            }*/
            if (!found) {
                sendQueue.add(new PillReminderQueueItem(action, object));
            } else {
                LogUtils.log(Log.ERROR, TAG, "Action repeated in queue: " + action + ", " + object);
            }
        }
    }


    public void taskFinished() {
        LogUtils.log(Log.DEBUG, TAG, "Pillreminder action finished.");
        queueIsBusy = false;
    }

    public void queueFinished() {
        finished = true;
    }


    @Override
    public void run() {

        while (!finished) {
            try {
                if (sendQueue.size() > 0 && !finished) {
                    if (!queueIsBusy && !finished) {

                        PillReminderQueueItem bqi = sendQueue.take();
                        queueIsBusy = true;
                        timeLaunched = System.currentTimeMillis();

                        LogUtils.log(Log.DEBUG, TAG, "Sending action to bracelet: " + bqi.action);

                        switch (bqi.action) {
                            case LifevitSDKConstants.PILLREMINDER_REQUEST_GET_DEVICETIME:
                                reminder.sendGetDeviceTime();
                                break;
                            case LifevitSDKConstants.PILLREMINDER_REQUEST_SET_DEVICETIME:
                                reminder.sendSetDeviceTime();
                                break;
                            case LifevitSDKConstants.PILLREMINDER_REQUEST_GET_DEVICETIMEZONE:
                                reminder.sendGetDeviceTimeZone();
                                break;
                            case LifevitSDKConstants.PILLREMINDER_REQUEST_SET_DEVICETIMEZONE:
                                if (bqi.object != null && bqi.object instanceof HashMap) {

                                    int hour = (int) ((HashMap) bqi.object).get("hour");
                                    int minute = (int) ((HashMap) bqi.object).get("minute");

                                    reminder.sendSetDeviceTimeZone(hour, minute);
                                }
                                break;
                            case LifevitSDKConstants.PILLREMINDER_REQUEST_GET_BATTERYLEVEL:
                                reminder.sendGetBatteryLevel();
                                break;
                            case LifevitSDKConstants.PILLREMINDER_REQUEST_GET_LATESTSYNCHRONIZATIONTIME:
                                reminder.sendGetLatestSynchronizationTime();
                                break;
                            case LifevitSDKConstants.PILLREMINDER_REQUEST_SET_SUCCESSFULSYNCHRONIZATIONSTATUS:
                                reminder.sendSetSuccessfulSynchronizationStatus();
                                break;
                            case LifevitSDKConstants.PILLREMINDER_REQUEST_GET_CLEARSCHEDULEPERFORMANCEHISTORY:
                                reminder.sendClearSchedulePerformanceHistory();
                                break;
                            case LifevitSDKConstants.PILLREMINDER_REQUEST_GET_ALARMSCHEDULE:
                                reminder.sendGetAlarmSchedule();
                                break;
                            case LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMSSCHEDULE:
                                if (bqi.object != null && bqi.object instanceof ArrayList) {
                                    reminder.sendSetAlarmsSchedule((ArrayList<LifevitSDKPillReminderAlarmData>) bqi.object);
                                }
                                break;
                            case LifevitSDKConstants.PILLREMINDER_REQUEST_GET_SCHEDULEPERFORMANCEHISTORY:
                                reminder.sendGetSchedulePerformanceHistory();
                                break;
                            case LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMDURATION:
                                if (bqi.object != null && bqi.object instanceof Integer) {
                                    reminder.sendSetAlarmDuration((Integer) bqi.object);
                                }
                                break;
                            case LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMCONFIRMATIONTIME:
                                if (bqi.object != null && bqi.object instanceof Integer) {
                                    reminder.sendSetAlarmConfirmationTime((Integer) bqi.object);
                                }
                                break;
                            case LifevitSDKConstants.PILLREMINDER_REQUEST_GET_CLEARALARMSCHEDULE:
                                reminder.sendClearAlarmSchedule();
                                break;

                        }
                    } else {
                        long timeNow = System.currentTimeMillis();
                        if (timeNow - timeLaunched > 60000) {
                            LogUtils.log(Log.DEBUG, TAG, "Interrupted action because it was taking too long!");

                            queueIsBusy = false;
                        }
                        Thread.sleep(200);
                    }
                } else {
                    Thread.sleep(200);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
