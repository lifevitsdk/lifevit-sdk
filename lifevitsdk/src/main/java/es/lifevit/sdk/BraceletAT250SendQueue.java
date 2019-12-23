package es.lifevit.sdk;

/**
 * Created by aescanuela on 26/2/16.
 */


import android.util.Log;

import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import es.lifevit.sdk.bracelet.LifevitSDKAT250TimeRange;
import es.lifevit.sdk.utils.LogUtils;


public class BraceletAT250SendQueue extends Thread {

    private final static String TAG = BraceletAT250SendQueue.class.getSimpleName();

    private volatile LinkedBlockingQueue<BraceletAT250QueueItem> sendQueue = new LinkedBlockingQueue<>();
    private boolean queueIsBusy = false;
    private LifevitSDKBleDeviceBraceletAT250 dgBleDeviceBracelet;
    private boolean finished;
    private long timeLaunched = 0;


    public BraceletAT250SendQueue(LifevitSDKBleDeviceBraceletAT250 bleDevice) {
        this.dgBleDeviceBracelet = bleDevice;
        finished = false;
    }


    /**
     * Inner class
     */
    protected class BraceletAT250QueueItem {
        public int action;
        public Object object;

        public BraceletAT250QueueItem(int action, Object object) {
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
                sendQueue.add(new BraceletAT250QueueItem(action, object));
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


    @Override
    public void run() {

        while (!finished) {
            try {
                if (sendQueue.size() > 0 && !finished) {
                    if (!queueIsBusy && !finished) {

                        BraceletAT250QueueItem bqi = sendQueue.take();
                        queueIsBusy = true;
                        timeLaunched = System.currentTimeMillis();

                        LogUtils.log(Log.DEBUG, TAG, "Sending action to bracelet: " + bqi.action);

                        switch (bqi.action) {
                            case LifevitSDKBleDeviceBraceletAT250.NEW_BRACELET_ACTION_SET_DATE:
                                if (bqi.object != null && bqi.object instanceof Date) {
                                    dgBleDeviceBracelet.sendSetTime((Date) bqi.object);
                                }
                                break;
                            case LifevitSDKBleDeviceBraceletAT250.NEW_BRACELET_ACTION_GET_DATE:
                                dgBleDeviceBracelet.sendGetTime();
                                break;
                            case LifevitSDKBleDeviceBraceletAT250.NEW_BRACELET_ACTION_GET_PERSONAL_INFO:
                                dgBleDeviceBracelet.sendGetPersonalInfo();
                                break;
                            case LifevitSDKBleDeviceBraceletAT250.NEW_BRACELET_ACTION_SET_PERSONAL_INFO:
                                dgBleDeviceBracelet.sendPersonalInfo();
                                break;
                            case LifevitSDKBleDeviceBraceletAT250.NEW_BRACELET_ACTION_GET_ACTIVITY_DATA:
                                if (bqi.object != null && bqi.object instanceof Integer) {
                                    dgBleDeviceBracelet.sendGetActivityData((Integer) bqi.object);
                                }
                                break;
                            case LifevitSDKBleDeviceBraceletAT250.NEW_BRACELET_ACTION_SET_TARGET_STEPS:
                                if (bqi.object != null && bqi.object instanceof Integer) {
                                    dgBleDeviceBracelet.sendTargetSteps((Integer) bqi.object);
                                }
                                break;
                            case LifevitSDKBleDeviceBraceletAT250.NEW_BRACELET_ACTION_GET_TARGET_STEPS:
                                dgBleDeviceBracelet.sendGetTargetSteps();
                                break;
                            case LifevitSDKBleDeviceBraceletAT250.NEW_BRACELET_ACTION_GET_HISTORY_DATA:
                                dgBleDeviceBracelet.sendGetActivityDataDistribution();
                                break;
                            case LifevitSDKBleDeviceBraceletAT250.NEW_BRACELET_ACTION_GET_TODAY_DATA:
                                dgBleDeviceBracelet.sendStartRealTimeActivityData();
                                break;
                            case LifevitSDKBleDeviceBraceletAT250.NEW_BRACELET_ACTION_GET_HEART_RATE_VALUE:
                                if (bqi.object != null && bqi.object instanceof Integer) {
                                    dgBleDeviceBracelet.sendGetHeartRateValue((Integer) bqi.object);
                                }
                                break;
                            case LifevitSDKBleDeviceBraceletAT250.NEW_BRACELET_ACTION_SET_MONITORING_HR_ENABLED:
                                if (bqi.object != null && bqi.object instanceof Boolean) {
                                    dgBleDeviceBracelet.sendSetMonitoringHREnabled((Boolean) bqi.object);
                                }
                                break;
                            case LifevitSDKBleDeviceBraceletAT250.NEW_BRACELET_ACTION_SET_REALTIME_HR_ENABLED:
                                if (bqi.object != null && bqi.object instanceof Boolean) {
                                    dgBleDeviceBracelet.sendSetRealtimeHREnabled((Boolean) bqi.object);
                                }
                                break;
                            case LifevitSDKBleDeviceBraceletAT250.NEW_BRACELET_ACTION_SET_MONITORING_HR_AUTO_ENABLED:
                                if (bqi.object != null && bqi.object instanceof LifevitSDKAT250TimeRange) {
                                    dgBleDeviceBracelet.sendSetMonitoringHRAuto(true, (LifevitSDKAT250TimeRange) bqi.object);
                                }
                                break;
                            case LifevitSDKBleDeviceBraceletAT250.NEW_BRACELET_ACTION_SET_MONITORING_HR_AUTO_DISABLED:
                                if (bqi.object != null && bqi.object instanceof LifevitSDKAT250TimeRange) {
                                    dgBleDeviceBracelet.sendSetMonitoringHRAuto(false, (LifevitSDKAT250TimeRange) bqi.object);
                                }
                                break;
                            case LifevitSDKBleDeviceBraceletAT250.NEW_BRACELET_ACTION_UPDATE_FIRMWARE:
                                dgBleDeviceBracelet.sendUpdateFirmware();
                                break;
                            case LifevitSDKBleDeviceBraceletAT250.NEW_BRACELET_ACTION_GET_FIRMWARE_VERSION_NUMBER:
                                dgBleDeviceBracelet.sendGetFirmwareVersionNumber();
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
