package es.lifevit.sdk;

/**
 * Created by aescanuela on 26/2/16.
 */


import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

import es.lifevit.sdk.bracelet.LifevitSDKTensioBraceletMeasurementInterval;
import es.lifevit.sdk.utils.LogUtils;


public class TensiobraceletSendQueue extends Thread {

    private final static String TAG = TensiobraceletSendQueue.class.getSimpleName();

    private volatile LinkedBlockingQueue<TensiobraceletQueueItem> sendQueue = new LinkedBlockingQueue<>();
    private boolean queueIsBusy = false;
    private LifevitSDKBleDeviceTensiobracelet dgBleDeviceBracelet;
    private boolean finished;
    private long timeLaunched = 0;
    private int lastCommandSent = -1;


    public TensiobraceletSendQueue(LifevitSDKBleDeviceTensiobracelet bleDevice) {
        this.dgBleDeviceBracelet = bleDevice;
        finished = false;
    }


    /**
     * Inner class
     */
    protected class TensiobraceletQueueItem {
        public int action;
        public Object object;

        public TensiobraceletQueueItem(int action, Object object) {
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
                sendQueue.add(new TensiobraceletQueueItem(action, object));
            } else {
                LogUtils.log(Log.ERROR, TAG, "Action repeated in queue: " + action + ", " + object);
            }
        }
    }


    protected void taskFinished() {
        LogUtils.log(Log.DEBUG, TAG, "TensioBracelet action finished.");
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

                        TensiobraceletQueueItem bqi = sendQueue.take();
                        queueIsBusy = true;
                        timeLaunched = System.currentTimeMillis();

                        LogUtils.log(Log.DEBUG, TAG, "Sending action to bracelet: " + bqi.action);

                        switch (bqi.action) {
                            case LifevitSDKBleDeviceTensiobracelet.ACTION_SET_DATE:
                                if (bqi.object instanceof Long) {
                                    dgBleDeviceBracelet.sendSetDate((Long) bqi.object);
                                }
                                break;
                            case LifevitSDKBleDeviceTensiobracelet.ACTION_START_MEASUREMENT:
                                dgBleDeviceBracelet.sendStartMeasurement();
                                break;
                            case LifevitSDKBleDeviceTensiobracelet.ACTION_GET_BLOOD_PRESSURE_HISTORY_DATA:
                                dgBleDeviceBracelet.sendGetBloodPressureHistoryData();
                                break;
                            case LifevitSDKBleDeviceTensiobracelet.ACTION_RETURN:
                                dgBleDeviceBracelet.sendReturn();
                                break;
                            case LifevitSDKBleDeviceTensiobracelet.ACTION_PROGRAM_AUTOMATIC_MEASUREMENTS:
                                if (bqi.object instanceof LifevitSDKTensioBraceletMeasurementInterval) {
                                    dgBleDeviceBracelet.sendProgramAutomaticMeasurements((LifevitSDKTensioBraceletMeasurementInterval) bqi.object);
                                }
                                break;
                            case LifevitSDKBleDeviceTensiobracelet.ACTION_DEACTIVATE_AUTOMATIC_MEASUREMENTS:
                                dgBleDeviceBracelet.sendDeactivateAutomaticMeasurements();
                                break;
                        }

                        lastCommandSent = bqi.action;

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

    public int getLastCommandSent() {
        return lastCommandSent;
    }

    public void setLastCommandSent(int lastCommandSent) {
        this.lastCommandSent = lastCommandSent;
    }

}
