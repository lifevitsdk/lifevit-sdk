package es.lifevit.sdk;

/**
 * Created by aescanuela on 26/2/16.
 */


import android.util.Log;

import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import es.lifevit.sdk.bracelet.LifevitSDKAT500SedentaryReminderTimeRange;
import es.lifevit.sdk.bracelet.LifevitSDKAt500HrAlarmTime;
import es.lifevit.sdk.utils.LogUtils;


public class BraceletAT500HRSendQueue extends Thread {

    private final static String TAG = BraceletAT500HRSendQueue.class.getSimpleName();

    private volatile LinkedBlockingQueue<BraceletAT500HRQueueItem> sendQueue = new LinkedBlockingQueue<>();
    private boolean queueIsBusy = false;
    private LifevitSDKBleDeviceBraceletAT500HR dgBleDeviceBracelet;
    private boolean finished;
    private long timeLaunched = 0;


    public BraceletAT500HRSendQueue(LifevitSDKBleDeviceBraceletAT500HR bleDevice) {
        this.dgBleDeviceBracelet = bleDevice;
        finished = false;
    }


    /**
     * Inner class
     */
    protected class BraceletAT500HRQueueItem {
        public int action;
        public Object object;

        public BraceletAT500HRQueueItem(int action, Object object) {
            this.action = action;
            this.object = object;
        }
    }


    protected void addToQueue(int action) {
        addToQueue(action, 0);
    }

    protected void addToQueue(int action, Object object) {
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
                sendQueue.add(new BraceletAT500HRQueueItem(action, object));
            } else {
                LogUtils.log(Log.ERROR, TAG, "Action repeated in queue: " + action + ", " + object);
            }
        }
    }


    protected void taskFinished() {
        LogUtils.log(Log.DEBUG, TAG, "Bracelet action finished.");
        queueIsBusy = false;
    }

    protected void queueFinished() {
        finished = true;
    }


    protected boolean isQueueBusy() {
        return queueIsBusy;
    }


    @Override
    public void run() {

        while (!finished) {
            try {
                if (sendQueue.size() > 0 && !finished) {
                    if (!queueIsBusy && !finished) {

                        BraceletAT500HRQueueItem bqi = sendQueue.take();
                        queueIsBusy = true;
                        timeLaunched = System.currentTimeMillis();

                        LogUtils.log(Log.DEBUG, TAG, "Sending action to bracelet: " + bqi.action);

                        switch (bqi.action) {
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_TEST_INSTRUCTION:
                                dgBleDeviceBracelet.sendTestInstruction();
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_GET_BATTERY:
                                dgBleDeviceBracelet.sendGetBattery();
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_GET_VERSION:
                                dgBleDeviceBracelet.sendGetVersion();
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_BIND_DEVICE:
                                dgBleDeviceBracelet.sendBindDevice();
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_ACTIVATE_DEVICE:
                                dgBleDeviceBracelet.sendActivateDevice();
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_QUERY_SEND_DAY_STEPS:
                                dgBleDeviceBracelet.sendQueryDaySteps();
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_GET_ACTIVITY_DATA:
                                dgBleDeviceBracelet.sendRequestData(0);
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_START_CURRENT_HEART_RATE:
                                dgBleDeviceBracelet.sendCurrentHeartRate(true);
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_END_CURRENT_HEART_RATE:
                                dgBleDeviceBracelet.sendCurrentHeartRate(false);
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_START_EXERCISE:
                                dgBleDeviceBracelet.sendStartOrFinishExercise(true);
                                dgBleDeviceBracelet.sendStartOrFinishAutoSteps(true);
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_END_EXERCISE:
                                dgBleDeviceBracelet.sendStartOrFinishExercise(false);
                                dgBleDeviceBracelet.sendStartOrFinishAutoSteps(false);
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_START_AUTO_STEPS:
                                dgBleDeviceBracelet.sendStartOrFinishAutoSteps(true);
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_END_AUTO_STEPS:
                                dgBleDeviceBracelet.sendStartOrFinishAutoSteps(false);
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_CONFIGURE_USER_HEIGHT:
                                dgBleDeviceBracelet.sendUserHeight();
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_CONFIGURE_USER_WEIGHT:
                                dgBleDeviceBracelet.sendUserWeight();
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_CONFIGURE_CURRENT_DATE_TIME:
                                dgBleDeviceBracelet.sendCurrentDatetime();
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_CONFIGURE_DATE_TIME:
                                dgBleDeviceBracelet.sendDatetime((Date) bqi.object);
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_SETTINGS_FIND_PHONE_OFF:
                                dgBleDeviceBracelet.sendSettingFindPhone(false);
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_SETTINGS_FIND_PHONE_ON:
                                dgBleDeviceBracelet.sendSettingFindPhone(true);
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_SETTINGS_ANTI_THEFT_OFF:
                                dgBleDeviceBracelet.sendSettingAntiTheft(false);
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_SETTINGS_ANTI_THEFT_ON:
                                dgBleDeviceBracelet.sendSettingAntiTheft(true);
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_SETTINGS_DISTANCE_UNIT:
                                dgBleDeviceBracelet.sendSettingDistanceUnit((Integer) bqi.object);
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_SETTINGS_MONITOR_HEART_RATE_OFF:
                                dgBleDeviceBracelet.sendSettingMonitorHeartRate(false);
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_SETTINGS_MONITOR_HEART_RATE_ON:
                                dgBleDeviceBracelet.sendSettingMonitorHeartRate(true);
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_SETTINGS_CAMERA_ENABLED_OFF:
                                dgBleDeviceBracelet.sendEnableCamara(false);
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_SETTINGS_CAMERA_ENABLED_ON:
                                dgBleDeviceBracelet.sendEnableCamara(true);
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_FIND_BRACELET:
                                dgBleDeviceBracelet.sendFindDevice();
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_SETTINGS_SHOW_HOUR_WHEN_RISING_ARM_OFF:
                                dgBleDeviceBracelet.sendSettingArm(LifevitSDKConstants.BRACELET_HAND_NONE);
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_SETTINGS_SHOW_HOUR_WHEN_RISING_ARM_ON:
                                dgBleDeviceBracelet.sendSettingArm(LifevitSDKConstants.BRACELET_HAND_AUTO);
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_MESSAGE_RECEIVED:
                                dgBleDeviceBracelet.sendPushNotificationArrived((Integer) bqi.object);
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_SETTINGS_UPDATE_HAND:
                                dgBleDeviceBracelet.sendSettingArm((Integer) bqi.object);
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_SETTINGS_UPDATE_TARGET_STEPS:
                                if (bqi.object instanceof Integer) {
                                    dgBleDeviceBracelet.sendUpdateTargetSteps((Integer) bqi.object);
                                } else {
                                    dgBleDeviceBracelet.sendUpdateTargetSteps(10000);
                                }
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_ENABLE_SEDENTARY_REMINDER:
                                if (bqi.object instanceof LifevitSDKAT500SedentaryReminderTimeRange) {
                                    dgBleDeviceBracelet.sendSetBraceletSedentaryReminderEnabled(true, (LifevitSDKAT500SedentaryReminderTimeRange) bqi.object);
                                }
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_DISABLE_SEDENTARY_REMINDER:
                                dgBleDeviceBracelet.sendSetBraceletSedentaryReminderEnabled(false, new LifevitSDKAT500SedentaryReminderTimeRange());
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_SET_ALARM:
                                if (bqi.object instanceof LifevitSDKAt500HrAlarmTime) {
                                    dgBleDeviceBracelet.sendSetAlarm((LifevitSDKAt500HrAlarmTime) bqi.object);
                                }
                                break;
                            case LifevitSDKBleDeviceBraceletAT500HR.ACTION_DISABLE_ALARM:
                                if (bqi.object instanceof Boolean) {
                                    dgBleDeviceBracelet.sendDisableAlarm((Boolean) bqi.object);
                                }
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
