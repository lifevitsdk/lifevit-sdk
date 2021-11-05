package es.lifevit.sdk;


import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

import es.lifevit.sdk.bracelet.LifevitSDKAlarmTime;
import es.lifevit.sdk.bracelet.LifevitSDKAppNotification;
import es.lifevit.sdk.bracelet.LifevitSDKMonitoringAlarm;
import es.lifevit.sdk.bracelet.LifevitSDKSedentaryAlarm;
import es.lifevit.sdk.bracelet.LifevitSDKVitalActivityPeriod;
import es.lifevit.sdk.bracelet.LifevitSDKVitalAlarms;
import es.lifevit.sdk.bracelet.LifevitSDKVitalPeriod;
import es.lifevit.sdk.bracelet.LifevitSDKVitalScreenNotification;
import es.lifevit.sdk.bracelet.LifevitSDKVitalWeather;
import es.lifevit.sdk.utils.LogUtils;


public class BraceletVitalSendQueue extends Thread {

    private final static String TAG = BraceletVitalSendQueue.class.getSimpleName();

    private volatile LinkedBlockingQueue<BraceletVitalQueueItem> sendQueue = new LinkedBlockingQueue<>();
    private boolean queueIsBusy = false;
    private LifevitSDKBleDeviceBraceletVital dgBleDeviceBracelet;
    private boolean finished;
    private long timeLaunched = 0;


    public BraceletVitalSendQueue(LifevitSDKBleDeviceBraceletVital bleDevice) {
        this.dgBleDeviceBracelet = bleDevice;
        finished = false;
    }


    /**
     * Inner class
     */
    protected static class BraceletVitalQueueItem {
        public int action;
        public Object[] object;

        public BraceletVitalQueueItem(int action, Object... object) {
            this.action = action;
            this.object = object;
        }
    }


    protected void addToQueue(int action) {
        addToQueue(action, 0);
    }

    protected void addToQueue(int action, Object... object) {
        if (!finished) {
            // Check if operation is already in queue
            boolean found = false;
           /* for(BraceletAT2019QueueItem queueItem : sendQueue){
                if(action.equals(queueItem.action) && daysAgo == queueItem.daysAgo){
                    found = true;
                    break;
                }
            }*/
            if (!found) {

                sendQueue.add(new BraceletVitalQueueItem(action, object));

                LogUtils.log(Log.DEBUG, TAG, "Action in queue: " + action + ", " + object);
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

                        BraceletVitalQueueItem bqi = sendQueue.take();
                        queueIsBusy = true;
                        timeLaunched = System.currentTimeMillis();

                        LogUtils.log(Log.DEBUG, TAG, "Sending action to bracelet: " + bqi.action);

                        switch (bqi.action) {
                            case LifevitSDKBleDeviceBraceletVital.Action.SHOW_QR:
                                dgBleDeviceBracelet.sendQRCode((Boolean) bqi.object[0]);
                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.SET_REALTIME:
                                dgBleDeviceBracelet.sendRealtimeCounting((Boolean) bqi.object[0], (Boolean) bqi.object[1]);
                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.GET_TIME:
                                dgBleDeviceBracelet.sendBasicCommand(LifevitSDKBleDeviceBraceletVital.Constants.REQUEST_SET_TIME);
                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.SET_TIME:
                                dgBleDeviceBracelet.sendSetTime((Long) bqi.object[0]);
                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.GET_MAC_ADDRESS:
                                dgBleDeviceBracelet.sendBasicCommand(LifevitSDKBleDeviceBraceletVital.Constants.REQUEST_GET_MAC_ADDRESS);

                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.GET_DEVICE_BATTERY:
                                dgBleDeviceBracelet.sendBasicCommand(LifevitSDKBleDeviceBraceletVital.Constants.REQUEST_GET_DEVICE_BATTERY);

                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.GET_USER_INFO:
                                dgBleDeviceBracelet.sendBasicCommand(LifevitSDKBleDeviceBraceletVital.Constants.REQUEST_GET_USER_PERSONAL_INFORMATION);

                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.SET_USER_INFO:
                                dgBleDeviceBracelet.sendSetUserInfo((LifevitSDKUserData) bqi.object[0]);
                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.SET_DEVICE_PARAMS:
                                dgBleDeviceBracelet.sendSetParameters((LifevitSDKBraceletParams) bqi.object[0]);
                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.SET_DEVICE_NEW_PARAMS:
                                dgBleDeviceBracelet.sendSetNewParameters((LifevitSDKBraceletParams) bqi.object[0]);
                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.GET_DEVICE_PARAMS:
                                dgBleDeviceBracelet.sendBasicCommand(LifevitSDKBleDeviceBraceletVital.Constants.REQUEST_GET_DEVICE_PARAMETERS);

                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.GET_DEVICE_NEW_PARAMS:
                                dgBleDeviceBracelet.sendBasicCommand(LifevitSDKBleDeviceBraceletVital.Constants.REQUEST_GET_DEVICE_NEW_PARAMETERS);

                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.SET_STEP_GOAL:
                                dgBleDeviceBracelet.sendSetTargetSteps((Integer) bqi.object[0]);
                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.GET_STEP_GOAL:
                                dgBleDeviceBracelet.sendBasicCommand(LifevitSDKBleDeviceBraceletVital.Constants.REQUEST_GET_TARGET_STEPS);

                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.GET_BLOOD_OXY:
                                dgBleDeviceBracelet.sendGetOxymeterData();

                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.GET_BLOOD_OXY_AUTO:

                                dgBleDeviceBracelet.sendGetPeriodicOxymeterData();
                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.GET_ACTIVITY_PERIOD:
                                dgBleDeviceBracelet.sendBasicCommand(LifevitSDKBleDeviceBraceletVital.Constants.REQUEST_GET_ACTIVITY_PERIOD);

                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.SET_ACTIVITY_PERIOD:
                                dgBleDeviceBracelet.sendSetActivityPeriod((LifevitSDKVitalActivityPeriod) bqi.object[0]);
                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.GET_HR:

                                dgBleDeviceBracelet.sendGetHeartRateData();
                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.GET_HR_AUTO:

                                dgBleDeviceBracelet.sendGetPeriodicHeartRateData();
                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.GET_HRV:
                                dgBleDeviceBracelet.sendGetVitals();
                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.SET_SPORT_MODE:
                                dgBleDeviceBracelet.sendStartSport((Integer)bqi.object[0],(Integer)bqi.object[1],(Integer)bqi.object[2],(Integer)bqi.object[3]);
                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.GET_OXY_PERIOD:
                                dgBleDeviceBracelet.sendBasicCommand(LifevitSDKBleDeviceBraceletVital.Constants.REQUEST_GET_AUTOMATIC_HEART_RATE_DETECTION);
                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.SET_OXY_PERIOD:
                                dgBleDeviceBracelet.sendSetBloodPressurePeriod((LifevitSDKVitalPeriod) bqi.object[0]);
                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.GET_HR_PERIOD:
                                dgBleDeviceBracelet.sendBasicCommand(LifevitSDKBleDeviceBraceletVital.Constants.REQUEST_GET_AUTOMATIC_HEART_RATE_DETECTION);
                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.SET_HR_PERIOD:
                                dgBleDeviceBracelet.sendSetHeartRatePeriod((LifevitSDKVitalPeriod) bqi.object[0]);
                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.GET_SPORT_DATA:
                                dgBleDeviceBracelet.sendGetSportsData();

                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.GET_STEPS:
                                dgBleDeviceBracelet.sendGetTotalDaySteps();

                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.STEP_SYNC:

                                dgBleDeviceBracelet.sendGetDetailedStepsData();
                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.SLEEP_SYNC:

                                dgBleDeviceBracelet.sendGetDetailedSleepData();
                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.GET_TEMPERATURE:

                                dgBleDeviceBracelet.sendGetTemperatureData();
                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.GET_TEMPERATURE_AUTO:
                                dgBleDeviceBracelet.sendGetPeriodicTemperatureData();

                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.ECG_START:
                                dgBleDeviceBracelet.sendBasicCommand(LifevitSDKBleDeviceBraceletVital.Constants.REQUEST_GET_ECG_START_DATA_UPLOADING);

                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.ECG_STATUS:
                                dgBleDeviceBracelet.sendBasicCommand(LifevitSDKBleDeviceBraceletVital.Constants.REQUEST_GET_ECG_MEASUREMENT_STATUS);

                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.ECG_WAVEFORM:
                                dgBleDeviceBracelet.sendGetECGWaveform();

                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.HRV_START:
                                dgBleDeviceBracelet.sendSetHealthControl(LifevitSDKBleDeviceBraceletVital.Data.VITALS, true);

                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.HR_START:
                                dgBleDeviceBracelet.sendSetHealthControl(LifevitSDKBleDeviceBraceletVital.Data.HR, true);

                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.OXY_START:
                                dgBleDeviceBracelet.sendSetHealthControl(LifevitSDKBleDeviceBraceletVital.Data.OXYMETER, true);

                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.SET_ALARMS:
                                dgBleDeviceBracelet.sendSetAlarms((LifevitSDKVitalAlarms) bqi.object[0]);

                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.SET_REMINDERS:
                                dgBleDeviceBracelet.sendSetNotification((LifevitSDKVitalScreenNotification) bqi.object[0]);

                                break;
                            case LifevitSDKBleDeviceBraceletVital.Action.SET_WEATHER:
                                dgBleDeviceBracelet.sendSetWeather((LifevitSDKVitalWeather) bqi.object[0]);

                                break;
                            default:

                                LogUtils.log(Log.DEBUG, TAG, "ACTION NOT IN QUEUE: " + bqi.action);
                                taskFinished();
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
