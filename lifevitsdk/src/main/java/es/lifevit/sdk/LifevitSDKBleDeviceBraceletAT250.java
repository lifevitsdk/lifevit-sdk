package es.lifevit.sdk;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import es.lifevit.sdk.bracelet.LifevitSDKAT250TimeRange;
import es.lifevit.sdk.bracelet.LifevitSDKHeartbeatData;
import es.lifevit.sdk.bracelet.LifevitSDKSleepData;
import es.lifevit.sdk.bracelet.LifevitSDKStepData;
import es.lifevit.sdk.utils.HexUtils;
import es.lifevit.sdk.utils.LogUtils;
import es.lifevit.sdk.utils.Utils;


/**
 * Created by aescanuela on 26/1/16.
 */
public class LifevitSDKBleDeviceBraceletAT250 extends LifevitSDKBleDevice {

    private final static String CLASS_TAG = LifevitSDKBleDeviceBraceletAT250.class.getSimpleName();

    private static final String DEVICE_NAME = "J-Style";
    private static final String DEVICE_NAME_2 = "LifeVit";
    private static final String DEVICE_NAME_3 = "HR1638";


    /**
     * Attributes
     */

    private byte[] deviceId = null;

    private int totalRunning;
    List<LifevitSDKStepData> resultList;
    List<LifevitSDKSleepData> sleepResultList;
    List<LifevitSDKHeartbeatData> heartData = new ArrayList<>();

    private static BraceletAT250SendQueue sendingThread;

    boolean[] daysWithData = new boolean[32];

    private int currentPacket = 0;
    private boolean isCheckingVersion = false;


    /**
     * Service descriptor
     */

    // Custom service (primary service)
    private static final String UUID_SERVICE = "0000FFF0-0000-1000-8000-00805F9B34FB";

    // Properties: NOTIFY
    // Descriptors:
    // 1. Client Characteristic Configuration, UUID: 0x2902 (defines how the characteristic may be configured by a specific client)
    private static final String UUID_CHARACTERISTIC_NOTIFY = "0000FFF7-0000-1000-8000-00805F9B34FB";

    // Properties: READ, WRITE, WRITE_NO_RESPONS (client can read, write, and write with response, on this characteristic)
    // Write Type: WRITE REQUEST (will give you a response back telling you the write was successful)
    // Descriptors:
    private static final String UUID_CHARACTERISTIC_SEND = "0000FFF6-0000-1000-8000-00805F9B34FB";


    /**
     * Actions to send to BLE device
     */

    static final int NEW_BRACELET_ACTION_GET_DATE = 0;
    static final int NEW_BRACELET_ACTION_SET_DATE = 1;
    static final int NEW_BRACELET_ACTION_GET_PERSONAL_INFO = 2;
    static final int NEW_BRACELET_ACTION_SET_PERSONAL_INFO = 3;
    static final int NEW_BRACELET_ACTION_GET_ACTIVITY_DATA = 4;
    static final int NEW_BRACELET_ACTION_SET_TARGET_STEPS = 5;
    static final int NEW_BRACELET_ACTION_GET_TARGET_STEPS = 6;
    static final int NEW_BRACELET_ACTION_GET_HISTORY_DATA = 7;
    static final int NEW_BRACELET_ACTION_GET_TODAY_DATA = 8;
    static final int NEW_BRACELET_ACTION_GET_HEART_RATE_VALUE = 9;
    static final int NEW_BRACELET_ACTION_SET_MONITORING_HR_ENABLED = 10;
    static final int NEW_BRACELET_ACTION_SET_REALTIME_HR_ENABLED = 11;
    static final int NEW_BRACELET_ACTION_SET_MONITORING_HR_AUTO_ENABLED = 12;
    static final int NEW_BRACELET_ACTION_SET_MONITORING_HR_AUTO_DISABLED = 13;
    static final int NEW_BRACELET_ACTION_UPDATE_FIRMWARE = 14;
    static final int NEW_BRACELET_ACTION_GET_FIRMWARE_VERSION_NUMBER = 15;


    /**
     * Creator
     */

    protected LifevitSDKBleDeviceBraceletAT250(BluetoothDevice dev, LifevitSDKManager manager) {

        this.mBluetoothDevice = dev;
        this.mLifevitSDKManager = manager;
    }


    @Override
    protected int getType() {
        return LifevitSDKConstants.DEVICE_BRACELET_AT250;
    }

    /**
     * Receivers
     */

    protected void connectGatt(Context context, boolean firstTime) {

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[connection] CONNECT: " + mBluetoothDevice.getAddress());

        mBluetoothGatt = mBluetoothDevice.connectGatt(context, false, mGattCallback);
        mContext = context;
        mFirstTime = firstTime;

        sendingThread = new BraceletAT250SendQueue(this);
        sendingThread.start();
    }


    protected void disconnectGatt() {

        if (sendingThread != null) {
            sendingThread.queueFinished();
            sendingThread.interrupt();
        }
        super.disconnectGatt();
    }


    @Override
    protected void startReceiver(String action, Intent intent) {
    }


    /**
     * "Public" methods
     */

    void getDeviceDate() {
        sendingThread.addToQueue(NEW_BRACELET_ACTION_GET_DATE);
    }

    void setDeviceDate(Date date) {
        sendingThread.addToQueue(NEW_BRACELET_ACTION_SET_DATE, date);
    }

    void getPersonalInfo() {
        sendingThread.addToQueue(NEW_BRACELET_ACTION_GET_PERSONAL_INFO);
    }

    void setPersonalInfo(int userHeight, int userWeight, int userGender, int userAge) {
        PreferenceUtil.setBraceletAT250UserHeight(mContext, userHeight);
        PreferenceUtil.setBraceletAT250UserWeight(mContext, userWeight);
        PreferenceUtil.setBraceletAT250UserGender(mContext, userGender);
        PreferenceUtil.setBraceletAT250UserAge(mContext, userAge);
        sendingThread.addToQueue(NEW_BRACELET_ACTION_SET_PERSONAL_INFO);
    }

    void setTargetSteps(int targetSteps) {
        sendingThread.addToQueue(NEW_BRACELET_ACTION_SET_TARGET_STEPS, targetSteps);
    }

    void getTargetSteps() {
        sendingThread.addToQueue(NEW_BRACELET_ACTION_GET_TARGET_STEPS);
    }

    void getHistoryData(int numberDays) {
        sendingThread.addToQueue(NEW_BRACELET_ACTION_GET_ACTIVITY_DATA, numberDays);
    }

    void getTodayData() {
        sendingThread.addToQueue(NEW_BRACELET_ACTION_GET_TODAY_DATA);
    }

    void getHeartRateValue(int packet) {
        sendingThread.addToQueue(NEW_BRACELET_ACTION_GET_HEART_RATE_VALUE, packet);
    }

    void setRealtimeHREnabled(boolean enabled) {
        sendingThread.addToQueue(NEW_BRACELET_ACTION_SET_MONITORING_HR_ENABLED, enabled);
    }

    void setMonitoringHREnabled(boolean enabled) {
        sendingThread.addToQueue(NEW_BRACELET_ACTION_SET_REALTIME_HR_ENABLED, enabled);
    }

    void setMonitoringHRAuto(boolean enabled, LifevitSDKAT250TimeRange range) {
        if (enabled) {
            sendingThread.addToQueue(NEW_BRACELET_ACTION_SET_MONITORING_HR_AUTO_ENABLED, range);
        } else {
            sendingThread.addToQueue(NEW_BRACELET_ACTION_SET_MONITORING_HR_AUTO_DISABLED, range);
        }
    }

    void updateFirmware() {
        getFirmwareVersionNumber(true);
    }

    void getFirmwareVersionNumber(boolean checkingVersion) {
        isCheckingVersion = checkingVersion;
        sendingThread.addToQueue(NEW_BRACELET_ACTION_GET_FIRMWARE_VERSION_NUMBER);
    }


    /**
     * Other methods
     */

    protected static boolean isBraceletAT250Device(String name) {
        return DEVICE_NAME.equalsIgnoreCase(name) || DEVICE_NAME_2.equalsIgnoreCase(name) || DEVICE_NAME_3.equalsIgnoreCase(name);
    }

    public boolean hasHR() {
        return DEVICE_NAME_3.equalsIgnoreCase(getDevice().getName());
    }

    protected static boolean matchDevice(BluetoothDevice device) {
        return DEVICE_NAME.equalsIgnoreCase(device.getName()) || DEVICE_NAME_2.equalsIgnoreCase(device.getName()) || DEVICE_NAME_3.equalsIgnoreCase(device.getName());
    }


    protected static UUID[] getUUIDs() {
        UUID[] uuidArray = new UUID[1];
        uuidArray[0] = UUID.fromString(UUID_SERVICE);
        return uuidArray;
    }


    protected void sendDeviceStatus() {
        mLifevitSDKManager.deviceOnConnectionChanged(LifevitSDKConstants.DEVICE_BRACELET_AT250, mDeviceStatus, true);
        if (mDeviceStatus == LifevitSDKConstants.STATUS_CONNECTED) {
//            updateActivityData();
//            checkFirmwareVersion();
            updateFirmware();
        }
    }


    private void updateActivityData() {
        // Start and stop real time data to get total activity for day
        sendingThread.addToQueue(NEW_BRACELET_ACTION_GET_TODAY_DATA);
        // Check if there is data to download in the last 30 days
        sendingThread.addToQueue(NEW_BRACELET_ACTION_GET_HISTORY_DATA);
        // Get data for current day and previous day
        sendingThread.addToQueue(NEW_BRACELET_ACTION_GET_ACTIVITY_DATA, 1);
        sendingThread.addToQueue(NEW_BRACELET_ACTION_GET_ACTIVITY_DATA, 0);
    }


    /**
     * Enable Device Notification
     *
     * @return
     */
    protected void enableDeviceNotification() {

        if (this.mBluetoothGatt == null) {
            return;
        }

        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(UUID_SERVICE));
        if (RxService == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "] Rx service not found!");
            return;
        }

        BluetoothGattCharacteristic txCharacteristic = RxService.getCharacteristic(UUID.fromString(UUID_CHARACTERISTIC_NOTIFY));

        if (txCharacteristic == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "] Tx charateristic not found!");
            return;
        }

        mBluetoothGatt.setCharacteristicNotification(txCharacteristic, true);

        for (BluetoothGattDescriptor descriptor : txCharacteristic.getDescriptors()) {

            // It's a notify characteristic
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                write(descriptor);
            }
        }
    }


    protected void sendMessage(byte[] data) {

        if (mBluetoothGatt == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "] sendMessage: mBluetoothGatt is null");
            return;
        }
        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(UUID_SERVICE));
        if (RxService == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "] Rx service not found!");
            return;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(UUID.fromString(UUID_CHARACTERISTIC_SEND));
        if (RxChar == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "] Rx charateristic not found!");
            return;
        }

        dividePacketsAndSendMessage(RxChar, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
    }


    /*********************************************************************************************/
    /***************************  Methods to receive data from device  ***************************/
    /*********************************************************************************************/


    protected void characteristicReadProcessData(BluetoothGattCharacteristic characteristic) {

        if (characteristic.getUuid().equals(UUID.fromString(UUID_CHARACTERISTIC_NOTIFY))) {

            byte[] rx = characteristic.getValue();

            LogUtils.log(Log.DEBUG, CLASS_TAG, "[RECEIVED]: " + HexUtils.getStringToPrint(rx));


            if (rx != null && rx.length > 3) {

                switch (rx[0]) {

                    case (byte) 0x41:
                        // GET TIME
                        sendingThread.taskFinished();
                        break;

                    case (byte) 0xC1:
                        // GET TIME
                        LogUtils.log(Log.ERROR, CLASS_TAG, "'Get time' returned error.");
                        sendingThread.taskFinished();
                        break;

                    case (byte) 0x42:
                        // GET PERSONAL INFO
                        // Synchronization finished, so now we have device id
                        deviceId = Arrays.copyOfRange(rx, 6, 12);
//                        mDeviceStatus = STATE_READY;
                        sendingThread.taskFinished();
                        break;

                    case (byte) 0xC2:
                        // GET PERSONAL INFO
                        LogUtils.log(Log.ERROR, CLASS_TAG, "'Get personal info' returned error.");
                        sendingThread.taskFinished();
                        break;

                    case (byte) 0x0B:
                        // SET TARGET STEPS
                        if (mLifevitSDKManager.getBraceletAT250Listener() != null) {
                            mLifevitSDKManager.getBraceletAT250Listener().operationFinished(true);
                        }
                        sendingThread.taskFinished();
                        break;

                    case (byte) 0x8B:
                        // SET TARGET STEPS
                        LogUtils.log(Log.ERROR, CLASS_TAG, "'Set target steps' returned error.");
                        if (mLifevitSDKManager.getBraceletAT250Listener() != null) {
                            mLifevitSDKManager.getBraceletAT250Listener().operationFinished(false);
                        }
                        sendingThread.taskFinished();
                        break;

                    case (byte) 0x02:
                        // SET PERSONAL INFO
                        if (mLifevitSDKManager.getBraceletAT250Listener() != null) {
                            mLifevitSDKManager.getBraceletAT250Listener().operationFinished(true);
                        }
                        sendingThread.taskFinished();
                        break;

                    case (byte) 0x82:
                        // SET PERSONAL INFO
                        LogUtils.log(Log.ERROR, CLASS_TAG, "'Set personal info' returned error.");
                        if (mLifevitSDKManager.getBraceletAT250Listener() != null) {
                            mLifevitSDKManager.getBraceletAT250Listener().operationFinished(false);
                        }
                        sendingThread.taskFinished();
                        break;

                    case (byte) 0x01:
                        // SET TIME
                        if (mLifevitSDKManager.getBraceletAT250Listener() != null) {
                            mLifevitSDKManager.getBraceletAT250Listener().operationFinished(true);
                        }
                        sendingThread.taskFinished();
                        break;

                    case (byte) 0x81:
                        // SET TIME
                        LogUtils.log(Log.ERROR, CLASS_TAG, "'Set time' returned error.");
                        if (mLifevitSDKManager.getBraceletAT250Listener() != null) {
                            mLifevitSDKManager.getBraceletAT250Listener().operationFinished(false);
                        }
                        sendingThread.taskFinished();
                        break;

                    case (byte) 0x4B:
                        // GET TARGET STEPS
                        sendingThread.taskFinished();
                        break;

                    case (byte) 0xCB:
                        // GET TARGET STEPS
                        LogUtils.log(Log.ERROR, CLASS_TAG, "'Get target steps' returned error.");
                        sendingThread.taskFinished();
                        break;

                    case (byte) 0x46:
                        // GET ACTIVITY DATA DISTRIBUTION
                        for (int i = 0; i < 4; i++) {
                            byte currentByte = rx[i + 1]; // bytes 1 to 4
                            int currentIndex = (3 - i) * 8; // 24, 16, 8 and 0
                            daysWithData[currentIndex + 0] = ((currentByte & 0b00000001) > 0);
                            daysWithData[currentIndex + 1] = ((currentByte & 0b00000010) > 0);
                            daysWithData[currentIndex + 2] = ((currentByte & 0b00000100) > 0);
                            daysWithData[currentIndex + 3] = ((currentByte & 0b00001000) > 0);
                            daysWithData[currentIndex + 4] = ((currentByte & 0b00010000) > 0);
                            daysWithData[currentIndex + 5] = ((currentByte & 0b00100000) > 0);
                            daysWithData[currentIndex + 6] = ((currentByte & 0b01000000) > 0);
                            daysWithData[currentIndex + 7] = ((currentByte & 0b10000000) > 0);
                        }

                        if (BuildConfig.DEBUG_MESSAGES) {
                            for (int j = 0; j < daysWithData.length; j++) {
                                LogUtils.log(Log.DEBUG, CLASS_TAG, "Hace " + j + " dias hubo datos: " + daysWithData[j]);
                            }
                        }

                        // Mark as finished
                        sendingThread.taskFinished();

                        // Update last 30 days, if needed
//                        Calendar currentDay = Calendar.getInstance();
//                        long currentDayMillis = currentDay.getTimeInMillis();
//                        long daysAgoMillis = PreferenceUtil.getNewBraceletLastUpdateDate(mContext);
//                        long differenceInDays = (currentDayMillis - daysAgoMillis) / (1000 * 60 * 60 * 24);


                        int differenceInDays = 30;
//                            Intent startedIntent = new Intent(AppConstants.BROADCAST_ACTION_BRACELET_STARTED_UPDATE);
//                            sendBroadcast(startedIntent);
                        for (int i = 2; i <= differenceInDays; i++) {
                            if (daysWithData[i]) {
                                sendingThread.addToQueue(NEW_BRACELET_ACTION_GET_ACTIVITY_DATA, i);
                            }
                        }

                        break;

                    case (byte) 0xC6:
                        // GET ACTIVITY DATA DISTRIBUTION
                        LogUtils.log(Log.ERROR, CLASS_TAG, "'Get target steps' returned error.");
                        sendingThread.taskFinished();
                        break;

                    case (byte) 0x09:
                        // START REAL TIME ACTIVITY DATA

                        int realTimeSteps = Utils.bytesToInt(new byte[]{0, rx[1], rx[2], rx[3]});
                        double realTimeCalories = Utils.bytesToInt(new byte[]{0, rx[7], rx[8], rx[9]}) / 100.0;
                        double realTimeDistance = Utils.bytesToInt(new byte[]{0, rx[10], rx[11], rx[12]}) / 100.0;

                        LogUtils.log(Log.DEBUG, CLASS_TAG, "steps: " + realTimeSteps);
                        LogUtils.log(Log.DEBUG, CLASS_TAG, "calories: " + realTimeCalories);
                        LogUtils.log(Log.DEBUG, CLASS_TAG, "distance: " + realTimeDistance);

                        // Send to Activity
                        if (mLifevitSDKManager.getBraceletAT250Listener() != null) {

                            LifevitSDKStepData stepData = new LifevitSDKStepData(Calendar.getInstance().getTimeInMillis(), realTimeSteps, (float) realTimeCalories, (float) realTimeDistance);
                            mLifevitSDKManager.getBraceletAT250Listener().braceletCurrentStepsReceived(stepData);
                        }


//                        Intent intent1 = new Intent(AppConstants.BROADCAST_ACTION_STEPS_RESULTS);
//                        intent1.putExtra(AppConstants.EXTRA_STEPS, realTimeSteps);
//                        intent1.putExtra(AppConstants.EXTRA_CALORIES, realTimeCalories);
//                        intent1.putExtra(AppConstants.EXTRA_DISTANCE, realTimeDistance);
//                        BluetoothLeService.getInstance().sendBroadcast(intent1);

                        // Save in preferences (JSON format)
//                        JSONObject stepsJson = null;
//                        try {
//                            stepsJson = new JSONObject();
//                            stepsJson.put(AppConstants.JSON_STEPS, realTimeSteps);
//                            stepsJson.put(AppConstants.JSON_CALORIES, realTimeCalories);
//                            stepsJson.put(AppConstants.JSON_DISTANCE, realTimeDistance);
//                            stepsJson.put(AppConstants.JSON_DATE, (new Date()).getTime());
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        if (stepsJson != null) {
//                            PreferenceUtil.setNewBraceletLastRealTimeData(mContext, stepsJson.toString());
//                        }

                        // Send Stop directly, do not queue. Stop will finish the task in the queue
                        sendStopRealTimeActivityData();
                        break;

                    case (byte) 0x89:
                        // START REAL TIME ACTIVITY DATA
                        LogUtils.log(Log.ERROR, CLASS_TAG, "'START REAL TIME ACTIVITY DATA' returned error.");
                        sendingThread.taskFinished();
                        break;

                    case (byte) 0x0A:
                        // STOP REAL TIME ACTIVITY DATA
                        LogUtils.log(Log.ERROR, CLASS_TAG, "'STOP REAL TIME ACTIVITY DATA' returned OK.");
                        sendingThread.taskFinished();
                        break;

                    case (byte) 0x8A:
                        // STOP REAL TIME ACTIVITY DATA
                        LogUtils.log(Log.ERROR, CLASS_TAG, "'STOP REAL TIME ACTIVITY DATA' returned error.");
                        sendingThread.taskFinished();
                        break;

                    case (byte) 0x43:
                        // GET ACTIVITY DATA

                        byte responseWithData = rx[1];

                        if (responseWithData == (byte) 0xF0) {

                            int year = rx[2];
                            int month = rx[3];
                            int day = rx[4];

                            int packetNumber = rx[5];
                            int minutesSinceDayStart = packetNumber * 15;

                            byte dataType = rx[6];
                            if (dataType == (byte) 0x00) {
                                // Activity (steps) data
                                int calories = Utils.bytesToInt(new byte[]{0, 0, rx[8], rx[7]});
                                int steps = Utils.bytesToInt(new byte[]{0, 0, rx[10], rx[9]});
                                int distance = Utils.bytesToInt(new byte[]{0, 0, rx[12], rx[11]});
                                int running = Utils.bytesToInt(new byte[]{0, 0, rx[14], rx[13]});

                                LogUtils.log(Log.DEBUG, CLASS_TAG, "---> STEPS: Calories: " + calories + ", Steps: " + steps + ", Distance: " + distance + ", Running: " + running);


                                Calendar cal = Calendar.getInstance();
                                cal.set(Calendar.YEAR, 2000 + hexaToInt(year));
                                cal.set(Calendar.MONTH, hexaToInt(month) - 1);
                                cal.set(Calendar.DAY_OF_MONTH, hexaToInt(day));
                                cal.set(Calendar.HOUR_OF_DAY, minutesSinceDayStart / 60);
                                cal.set(Calendar.MINUTE, minutesSinceDayStart % 60);
                                cal.set(Calendar.SECOND, 0);
                                cal.set(Calendar.MILLISECOND, 0);

                                LifevitSDKStepData stepsRecord = new LifevitSDKStepData(Calendar.getInstance().getTimeInMillis(), steps, (float) (calories / 100.0), (float) (distance / 100.0));



                                resultList.add(stepsRecord);
                                totalRunning += running;

                                if (packetNumber >= 95) {

                                    sendingThread.taskFinished();

                                    // Last packet - resume packets and send - steps and sleep packets
                                    resumePacketsAndSend();
                                }

                            } else if (dataType == (byte) 0xFF) {

                                // Sleep data
                                double deepnessSum = 0.0;

                                // Split 15 minutes in packets of 2 minutes (each byte represents 2 minutes)
                                for (int i = 7; i <= 14; i++) {

                                    int sleepDeepness = rx[i] & 0x00ff;
                                    deepnessSum += sleepDeepness;

                                    LogUtils.log(Log.DEBUG, CLASS_TAG, "* SLEEP: Byte: " + i + ", Deepness: " + (sleepDeepness));
                                }

                                LifevitSDKSleepData sleepRecord = new LifevitSDKSleepData();

                                double deepnessAverage = deepnessSum / 8;

                                // Ojo, en vez de guardar DEEP o LIGHT, guardo valor medio (temporalmente)

                                sleepRecord.setSleepDeepness((int) deepnessAverage <= 3 ? LifevitSDKConstants.DEEP_SLEEP : LifevitSDKConstants.LIGHT_SLEEP);
                                sleepRecord.setSleepDuration(15);

                                Calendar cal = Calendar.getInstance();
                                cal.set(Calendar.YEAR, 2000 + hexaToInt(year));
                                cal.set(Calendar.MONTH, hexaToInt(month) - 1);
                                cal.set(Calendar.DAY_OF_MONTH, hexaToInt(day));
                                cal.set(Calendar.HOUR_OF_DAY, minutesSinceDayStart / 60);
                                cal.set(Calendar.MINUTE, minutesSinceDayStart % 60);
                                cal.set(Calendar.SECOND, 0);
                                cal.set(Calendar.MILLISECOND, 0);
                                sleepRecord.setDate(cal.getTimeInMillis());

                                if (BuildConfig.DEBUG_MESSAGES) {
                                    DateFormat timeFormatter = new SimpleDateFormat("HH:mm");
                                    LogUtils.log(Log.DEBUG, CLASS_TAG, "---> SLEEP: Hora: " + timeFormatter.format(cal.getTimeInMillis()) + ", Minutes: " + 15 + ", Deepness: " + (int) deepnessAverage);
                                }

                                sleepResultList.add(sleepRecord);

                            } else {
                                LogUtils.log(Log.ERROR, CLASS_TAG, "Data type not recognized");
                            }
//                            PreferenceUtil.setNewBraceletLastUpdateDate(mContext, (new Date()).getTime());
                        } else if (responseWithData == (byte) 0xFF) {
                            // There is no data - send info and release queue
                            sendingThread.taskFinished();
                            resumePacketsAndSend();
                        }
                        break;

                    case (byte) 0xC3:
                        LogUtils.log(Log.ERROR, CLASS_TAG, "'Get activity data' returned error.");
                        sendingThread.taskFinished();
                        break;

                    case (byte) 0x19:
                        LogUtils.log(Log.DEBUG, CLASS_TAG, "Monitor HR from BRACELET AT250");
                        if (mLifevitSDKManager.getBraceletAT250Listener() != null) {
                            mLifevitSDKManager.getBraceletAT250Listener().operationFinished(true);
                        }
                        sendingThread.taskFinished();
                        break;
                    case (byte) 0xa9:
                        LogUtils.log(Log.ERROR, CLASS_TAG, "ERROR Monitor HR from BRACELET AT250");
                        if (mLifevitSDKManager.getBraceletAT250Listener() != null) {
                            mLifevitSDKManager.getBraceletAT250Listener().operationFinished(false);
                        }
                        sendingThread.taskFinished();
                        break;
                    case (byte) 0x2c:

                        int hr = rx[1];
                        if (hr == 0) {

                            LogUtils.log(Log.DEBUG, CLASS_TAG, "Realtime HR from BRACELET AT250");
                            sendingThread.taskFinished();
                            if (mLifevitSDKManager.getBraceletAT250Listener() != null) {
                                mLifevitSDKManager.getBraceletAT250Listener().operationFinished(true);
                            }
                        } else {
                            LogUtils.log(Log.DEBUG, CLASS_TAG, "Realtime VALUE HR from BRACELET AT250: " + hr);

                            if (mLifevitSDKManager.getBraceletAT250Listener() != null) {
                                mLifevitSDKManager.getBraceletAT250Listener().braceletHeartRateReceived(hr);
                            }
                        }

                        break;
                    case (byte) 0x6d:

                        int hrValue = rx[1];

                        LogUtils.log(Log.DEBUG, CLASS_TAG, "Get VALUE HR from BRACELET AT250: " + hrValue);

                        if (mLifevitSDKManager.getBraceletAT250Listener() != null) {
                            mLifevitSDKManager.getBraceletAT250Listener().braceletHeartRateReceived(hrValue);
                        }


                        break;
                    case (byte) 0xac:
                        LogUtils.log(Log.ERROR, CLASS_TAG, "ERROR Realtime HR from BRACELET AT250");
                        sendingThread.taskFinished();
                        if (mLifevitSDKManager.getBraceletAT250Listener() != null) {
                            mLifevitSDKManager.getBraceletAT250Listener().operationFinished(false);
                        }
                        break;

                    case (byte) 0x2a:
                        LogUtils.log(Log.DEBUG, CLASS_TAG, "Succesful setting time range HR from BRACELET AT250");
                        if (mLifevitSDKManager.getBraceletAT250Listener() != null) {
                            mLifevitSDKManager.getBraceletAT250Listener().operationFinished(true);
                        }
                        sendingThread.taskFinished();
                        break;
                    case (byte) 0xaa:
                        LogUtils.log(Log.ERROR, CLASS_TAG, "Wrong Setting time range HR BRACELET AT250");
                        if (mLifevitSDKManager.getBraceletAT250Listener() != null) {
                            mLifevitSDKManager.getBraceletAT250Listener().operationFinished(false);
                        }
                        sendingThread.taskFinished();
                        break;
                    case (byte) 0x2f:
                        LogUtils.log(Log.DEBUG, CLASS_TAG, "Getting info HR BRACELET AT250");
                        if (rx[1] == -1) {
                            //Hemos acabado de recibir paquetes
                            sendingThread.taskFinished();

                            if (mLifevitSDKManager.getBraceletAT250Listener() != null) {
                                mLifevitSDKManager.getBraceletAT250Listener().braceletHeartRateSyncReceived(heartData);
                            }
                            return;
                        } else if (rx[1] == 100) {
                            sendingThread.taskFinished();
                            //Pedimos otro paquete
                            getHeartRateValue(currentPacket + 1);

                        }

                        String date = getDate(rx[2], rx[3], rx[4]);
                        String time = getHeartTime(rx[5], rx[6], (byte) 0);

                        SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm:dd");
                        try {
                            Date dt = df.parse(date + " " + time);
                            for (int i = 0; i < 12; i++) {
                                LifevitSDKHeartbeatData d = new LifevitSDKHeartbeatData();
                                d.setHeartrate(getLowValue(rx[i + 7]));
                                d.setDate(dt.getTime() + (i * 10 * 1000l));
                                heartData.add(d);
                            }
                        } catch (Exception e) {

                            if (mLifevitSDKManager.getBraceletAT250Listener() != null) {
                                mLifevitSDKManager.getBraceletAT250Listener().operationFinished(false);
                            }
                        }
                        break;

                    case (byte) 0x47:
                        LogUtils.log(Log.DEBUG, CLASS_TAG, "'Update firmware' returned OK.");
                        sendingThread.taskFinished();
                        break;

                    case (byte) 0xC7:
                        LogUtils.log(Log.ERROR, CLASS_TAG, "'Update firmware' returned error.");
                        sendingThread.taskFinished();
                        break;

                    case (byte) 0x27:

                        String versionNumberStr = rx[1] + "." + rx[2] + "." + rx[3] + "." + rx[4];
                        LogUtils.log(Log.DEBUG, CLASS_TAG, "Version Number: " + versionNumberStr);

                        int day = rx[7];
                        int month = rx[6];
                        int year = rx[5];

                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.YEAR, 2000 + hexaToInt(year));
                        cal.set(Calendar.MONTH, hexaToInt(month) - 1);
                        cal.set(Calendar.DAY_OF_MONTH, hexaToInt(day));
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        LogUtils.log(Log.DEBUG, CLASS_TAG, "Real Date: " + dateFormat.format(cal.getTime()));

                        if (isCheckingVersion) {

                            boolean upgrade = false;

                            if (LifevitSDKConstants.FORCE_FIRMWARE_UPDATE
                                    || rx[1] != LifevitSDKConstants.AT250_VERSION_1
                                    || rx[2] != LifevitSDKConstants.AT250_VERSION_2
                                    || rx[3] != LifevitSDKConstants.AT250_VERSION_3
                                    || rx[4] != LifevitSDKConstants.AT250_VERSION_4) {
                                upgrade = true;
                            }

                            if (upgrade) {

                                if (mLifevitSDKManager.getBraceletAT250Listener() != null) {
                                    mLifevitSDKManager.getBraceletAT250Listener().isGoingToUpdateFirmware(true);
                                }

                                sendingThread.addToQueue(NEW_BRACELET_ACTION_UPDATE_FIRMWARE);

                            } else {
                                if (mLifevitSDKManager.getBraceletAT250Listener() != null) {
                                    mLifevitSDKManager.getBraceletAT250Listener().isGoingToUpdateFirmware(false);
                                }
                            }
                        } else {
                            if (mLifevitSDKManager.getBraceletAT250Listener() != null) {
                                mLifevitSDKManager.getBraceletAT250Listener().firmwareVersion(versionNumberStr);
                            }
                        }

                        sendingThread.taskFinished();
                        break;
                    case (byte) 0xA7:
                        LogUtils.log(Log.ERROR, CLASS_TAG, "'Get Firmware Version Number' returned error.");
                        sendingThread.taskFinished();
                        break;

                    default:
                        sendingThread.taskFinished();
                        LogUtils.log(Log.ERROR, CLASS_TAG, "Characteristic read but not treated");
                }
            }
        }
    }


    private void resumePacketsAndSend() {

        // Just log messages
        if (BuildConfig.DEBUG_MESSAGES) {
            DateFormat dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            // STEPS PACKETS
            for (LifevitSDKStepData stepsRecord : resultList) {
                LogUtils.log(Log.DEBUG, CLASS_TAG, "====== FINAL STEPS RECORD (single): Date: " + dateTimeFormatter.format(stepsRecord.getDate())
                        + ", Steps: " + stepsRecord.getSteps() + ", Calories: " + stepsRecord.getCalories() + ", Distance: " + stepsRecord.getDistance());
            }

            // SLEEP PACKETS
            for (LifevitSDKSleepData sleepRecord : sleepResultList) {
                LogUtils.log(Log.DEBUG, CLASS_TAG, "********* FINAL SLEEP RECORD (single): Date: " + dateTimeFormatter.format(sleepRecord.getDate())
                        + ", Deepness: " + sleepRecord.getSleepDeepness() + ", Duration: " + sleepRecord.getSleepDuration());
            }
        }

        // Return info
        if (mLifevitSDKManager.getBraceletAT250Listener() != null) {
            mLifevitSDKManager.getBraceletAT250Listener().braceletSyncReceived(resultList, sleepResultList);
        }
    }


    /*********************************************************************************************/
    /**************************************  Helper methods  *************************************/
    /*********************************************************************************************/


    void sendGetTime() {

        byte[] syncparamsArray;

        syncparamsArray = new byte[16];
        syncparamsArray[0] = (byte) 0x41;
        syncparamsArray[1] = (byte) 0;
        syncparamsArray[2] = (byte) 0;
        syncparamsArray[3] = (byte) 0;
        syncparamsArray[4] = (byte) 0;
        syncparamsArray[5] = (byte) 0;
        syncparamsArray[6] = (byte) 0;
        syncparamsArray[7] = (byte) 0;
        syncparamsArray[8] = (byte) 0;
        syncparamsArray[9] = (byte) 0;
        syncparamsArray[10] = (byte) 0;
        syncparamsArray[11] = (byte) 0;
        syncparamsArray[12] = (byte) 0;
        syncparamsArray[13] = (byte) 0;
        syncparamsArray[14] = (byte) 0;
        syncparamsArray[15] = calculateCRC(syncparamsArray);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send get Time] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    void sendGetPersonalInfo() {

        byte[] syncparamsArray;

        syncparamsArray = new byte[16];
        syncparamsArray[0] = (byte) 0x42;
        syncparamsArray[1] = (byte) 0;
        syncparamsArray[2] = (byte) 0;
        syncparamsArray[3] = (byte) 0;
        syncparamsArray[4] = (byte) 0;
        syncparamsArray[5] = (byte) 0;
        syncparamsArray[6] = (byte) 0;
        syncparamsArray[7] = (byte) 0;
        syncparamsArray[8] = (byte) 0;
        syncparamsArray[9] = (byte) 0;
        syncparamsArray[10] = (byte) 0;
        syncparamsArray[11] = (byte) 0;
        syncparamsArray[12] = (byte) 0;
        syncparamsArray[13] = (byte) 0;
        syncparamsArray[14] = (byte) 0;
        syncparamsArray[15] = calculateCRC(syncparamsArray);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send get Time] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    private byte hexaToInt(int year) {
        //
        int yearOnlyTwoDigits = year % 100;
        int tens = yearOnlyTwoDigits / 16;
        int ones = yearOnlyTwoDigits % 16;
        int result = tens * 10 + ones;
        return (byte) result;
    }


    private byte intToHexa(int number) {
        //
        int yearOnlyTwoDigits = number % 100;
        int tens = yearOnlyTwoDigits / 10;
        int ones = yearOnlyTwoDigits % 10;
        //byte result = (byte) (((tens & 0b00001111) << 4) + (ones & 0b00001111));
        int result = tens * 16 + ones;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "Entrada: " + number + ", Salida: " + result + ", tens: " + tens + ", ones: " + ones);

        return (byte) result;
    }


    void sendSetTime(Date date) {

        byte[] syncparamsArray;

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        syncparamsArray = new byte[16];
        syncparamsArray[0] = (byte) 0x01;
        syncparamsArray[1] = intToHexa(cal.get(Calendar.YEAR));
        syncparamsArray[2] = intToHexa(cal.get(Calendar.MONTH) + 1);
        syncparamsArray[3] = intToHexa(cal.get(Calendar.DAY_OF_MONTH));
        syncparamsArray[4] = intToHexa(cal.get(Calendar.HOUR_OF_DAY));
        syncparamsArray[5] = intToHexa(cal.get(Calendar.MINUTE));
        syncparamsArray[6] = intToHexa(cal.get(Calendar.SECOND));
        syncparamsArray[7] = (byte) 0;
        syncparamsArray[8] = (byte) 0;
        syncparamsArray[9] = (byte) 0;
        syncparamsArray[10] = (byte) 0;
        syncparamsArray[11] = (byte) 0;
        syncparamsArray[12] = (byte) 0;
        syncparamsArray[13] = (byte) 0;
        syncparamsArray[14] = (byte) 0;
        syncparamsArray[15] = calculateCRC(syncparamsArray);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send SET Time] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    void sendPersonalInfo() {

        byte[] syncparamsArray;

        syncparamsArray = new byte[16];
        syncparamsArray[0] = (byte) 0x02;


        int userHeight = PreferenceUtil.getBraceletAT250UserHeight(mContext);
        int userWeight = PreferenceUtil.getBraceletAT250UserWeight(mContext);
        int userGender = PreferenceUtil.getBraceletAT250UserGender(mContext);
        int userAge = PreferenceUtil.getBraceletAT250UserAge(mContext);


        // Gender
//        DGUser user = DagaApplication.getInstance().getUserData();
        // female is 0
        syncparamsArray[1] = (byte) 0;
        if (userGender == LifevitSDKConstants.WEIGHT_SCALE_GENDER_MALE) {
            // male is 1
            syncparamsArray[1] = (byte) 1;
        }

        // Age
        syncparamsArray[2] = (byte) userAge;

        // Height
        syncparamsArray[3] = (byte) userHeight;

        // Weight
        syncparamsArray[4] = (byte) userWeight;

        // Stride length - value taken from http://theprogressgroup.com/blog/once-again-size-matters/
        syncparamsArray[5] = (byte) (userHeight * 0.415);

        // Others
        syncparamsArray[6] = (byte) 0;
        syncparamsArray[7] = (byte) 0;
        syncparamsArray[8] = (byte) 0;
        syncparamsArray[9] = (byte) 0;
        syncparamsArray[10] = (byte) 0;
        syncparamsArray[11] = (byte) 0;
        syncparamsArray[12] = (byte) 0;
        syncparamsArray[13] = (byte) 0;
        syncparamsArray[14] = (byte) 0;
        syncparamsArray[15] = calculateCRC(syncparamsArray);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send Personal info] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    void sendTargetSteps(int targetSteps) {

        byte[] syncparamsArray;

        syncparamsArray = new byte[16];
        syncparamsArray[0] = (byte) 0x0B;

        // Target steps
        byte[] targetStepsBytes = Utils.intToByteArray(targetSteps);

        syncparamsArray[1] = targetStepsBytes[1];
        syncparamsArray[2] = targetStepsBytes[2];
        syncparamsArray[3] = targetStepsBytes[3];

        // Others
        syncparamsArray[4] = (byte) 0;
        syncparamsArray[5] = (byte) 0;
        syncparamsArray[6] = (byte) 0;
        syncparamsArray[7] = (byte) 0;
        syncparamsArray[8] = (byte) 0;
        syncparamsArray[9] = (byte) 0;
        syncparamsArray[10] = (byte) 0;
        syncparamsArray[11] = (byte) 0;
        syncparamsArray[12] = (byte) 0;
        syncparamsArray[13] = (byte) 0;
        syncparamsArray[14] = (byte) 0;
        syncparamsArray[15] = calculateCRC(syncparamsArray);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send Target Steps] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    void sendGetTargetSteps() {

        byte[] syncparamsArray;

        syncparamsArray = new byte[16];
        syncparamsArray[0] = (byte) 0x4B;

        syncparamsArray[1] = (byte) 0;
        syncparamsArray[2] = (byte) 0;
        syncparamsArray[3] = (byte) 0;
        syncparamsArray[4] = (byte) 0;
        syncparamsArray[5] = (byte) 0;
        syncparamsArray[6] = (byte) 0;
        syncparamsArray[7] = (byte) 0;
        syncparamsArray[8] = (byte) 0;
        syncparamsArray[9] = (byte) 0;
        syncparamsArray[10] = (byte) 0;
        syncparamsArray[11] = (byte) 0;
        syncparamsArray[12] = (byte) 0;
        syncparamsArray[13] = (byte) 0;
        syncparamsArray[14] = (byte) 0;
        syncparamsArray[15] = calculateCRC(syncparamsArray);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send Get Target Steps] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    void sendGetActivityData(int daysAgo) {

        byte[] syncparamsArray;

        syncparamsArray = new byte[16];
        syncparamsArray[0] = (byte) 0x43;
        syncparamsArray[1] = (byte) daysAgo;
        syncparamsArray[2] = (byte) 0;
        syncparamsArray[3] = (byte) 0;
        syncparamsArray[4] = (byte) 0;
        syncparamsArray[5] = (byte) 0;
        syncparamsArray[6] = (byte) 0;
        syncparamsArray[7] = (byte) 0;
        syncparamsArray[8] = (byte) 0;
        syncparamsArray[9] = (byte) 0;
        syncparamsArray[10] = (byte) 0;
        syncparamsArray[11] = (byte) 0;
        syncparamsArray[12] = (byte) 0;
        syncparamsArray[13] = (byte) 0;
        syncparamsArray[14] = (byte) 0;
        syncparamsArray[15] = calculateCRC(syncparamsArray);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendGetActivityData] " + HexUtils.getStringToPrint(syncparamsArray));

        // Restart result list:
        resultList = new ArrayList<>();
        sleepResultList = new ArrayList<>();

        sendMessage(syncparamsArray);
    }


    void sendGetActivityDataDistribution() {

        byte[] syncparamsArray;

        syncparamsArray = new byte[16];
        syncparamsArray[0] = (byte) 0x46;

        syncparamsArray[1] = (byte) 0;
        syncparamsArray[2] = (byte) 0;
        syncparamsArray[3] = (byte) 0;
        syncparamsArray[4] = (byte) 0;
        syncparamsArray[5] = (byte) 0;
        syncparamsArray[6] = (byte) 0;
        syncparamsArray[7] = (byte) 0;
        syncparamsArray[8] = (byte) 0;
        syncparamsArray[9] = (byte) 0;
        syncparamsArray[10] = (byte) 0;
        syncparamsArray[11] = (byte) 0;
        syncparamsArray[12] = (byte) 0;
        syncparamsArray[13] = (byte) 0;
        syncparamsArray[14] = (byte) 0;
        syncparamsArray[15] = calculateCRC(syncparamsArray);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send Get Activity Data Distribution] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    void sendStartRealTimeActivityData() {

        byte[] syncparamsArray;
        syncparamsArray = new byte[16];
        syncparamsArray[0] = (byte) 0x09;
        syncparamsArray[1] = (byte) 0;
        syncparamsArray[2] = (byte) 0;
        syncparamsArray[3] = (byte) 0;
        syncparamsArray[4] = (byte) 0;
        syncparamsArray[5] = (byte) 0;
        syncparamsArray[6] = (byte) 0;
        syncparamsArray[7] = (byte) 0;
        syncparamsArray[8] = (byte) 0;
        syncparamsArray[9] = (byte) 0;
        syncparamsArray[10] = (byte) 0;
        syncparamsArray[11] = (byte) 0;
        syncparamsArray[12] = (byte) 0;
        syncparamsArray[13] = (byte) 0;
        syncparamsArray[14] = (byte) 0;
        syncparamsArray[15] = calculateCRC(syncparamsArray);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send Get Real Time Activity Data] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    void sendStopRealTimeActivityData() {

        byte[] syncparamsArray;
        syncparamsArray = new byte[16];
        syncparamsArray[0] = (byte) 0x0A;
        syncparamsArray[1] = (byte) 0;
        syncparamsArray[2] = (byte) 0;
        syncparamsArray[3] = (byte) 0;
        syncparamsArray[4] = (byte) 0;
        syncparamsArray[5] = (byte) 0;
        syncparamsArray[6] = (byte) 0;
        syncparamsArray[7] = (byte) 0;
        syncparamsArray[8] = (byte) 0;
        syncparamsArray[9] = (byte) 0;
        syncparamsArray[10] = (byte) 0;
        syncparamsArray[11] = (byte) 0;
        syncparamsArray[12] = (byte) 0;
        syncparamsArray[13] = (byte) 0;
        syncparamsArray[14] = (byte) 0;
        syncparamsArray[15] = calculateCRC(syncparamsArray);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send Get Real Time Activity Data] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }

    /***HR***/

    void sendGetHeartRateBasicValue() {

        byte[] syncparamsArray;
        syncparamsArray = new byte[16];
        syncparamsArray[0] = (byte) 0x6d;
        syncparamsArray[1] = (byte) 0;
        syncparamsArray[2] = (byte) 0;
        syncparamsArray[3] = (byte) 0;
        syncparamsArray[4] = (byte) 0;
        syncparamsArray[5] = (byte) 0;
        syncparamsArray[6] = (byte) 0;
        syncparamsArray[7] = (byte) 0;
        syncparamsArray[8] = (byte) 0;
        syncparamsArray[9] = (byte) 0;
        syncparamsArray[10] = (byte) 0;
        syncparamsArray[11] = (byte) 0;
        syncparamsArray[12] = (byte) 0;
        syncparamsArray[13] = (byte) 0;
        syncparamsArray[14] = (byte) 0;
        syncparamsArray[15] = calculateCRC(syncparamsArray);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send Get HR Basic Data] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }

    void sendGetHeartRateValue(int packet) {

        currentPacket = packet;
        if (packet == 0) {
            heartData = new ArrayList<>();
        }
        byte[] syncparamsArray;
        syncparamsArray = new byte[16];
        syncparamsArray[0] = (byte) 0x2f;
        syncparamsArray[1] = (byte) packet;
        syncparamsArray[2] = (byte) 0;
        syncparamsArray[3] = (byte) 0;
        syncparamsArray[4] = (byte) 0;
        syncparamsArray[5] = (byte) 0;
        syncparamsArray[6] = (byte) 0;
        syncparamsArray[7] = (byte) 0;
        syncparamsArray[8] = (byte) 0;
        syncparamsArray[9] = (byte) 0;
        syncparamsArray[10] = (byte) 0;
        syncparamsArray[11] = (byte) 0;
        syncparamsArray[12] = (byte) 0;
        syncparamsArray[13] = (byte) 0;
        syncparamsArray[14] = (byte) 0;
        syncparamsArray[15] = calculateCRC(syncparamsArray);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send Get HR Data] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }

    void sendSetRealtimeHREnabled(boolean enabled) {

        byte[] syncparamsArray;
        syncparamsArray = new byte[16];
        syncparamsArray[0] = (byte) 0x2c;
        syncparamsArray[1] = enabled ? (byte) 1 : (byte) 0;
        syncparamsArray[2] = (byte) 0;
        syncparamsArray[3] = (byte) 0;
        syncparamsArray[4] = (byte) 0;
        syncparamsArray[5] = (byte) 0;
        syncparamsArray[6] = (byte) 0;
        syncparamsArray[7] = (byte) 0;
        syncparamsArray[8] = (byte) 0;
        syncparamsArray[9] = (byte) 0;
        syncparamsArray[10] = (byte) 0;
        syncparamsArray[11] = (byte) 0;
        syncparamsArray[12] = (byte) 0;
        syncparamsArray[13] = (byte) 0;
        syncparamsArray[14] = (byte) 0;
        syncparamsArray[15] = calculateCRC(syncparamsArray);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send Real Time HR] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }

    void sendSetMonitoringHREnabled(boolean enabled) {

        byte[] syncparamsArray;
        syncparamsArray = new byte[16];
        syncparamsArray[0] = (byte) 0x19;
        syncparamsArray[1] = enabled ? (byte) 1 : (byte) 0;
        syncparamsArray[2] = (byte) 0;
        syncparamsArray[3] = (byte) 0;
        syncparamsArray[4] = (byte) 0;
        syncparamsArray[5] = (byte) 0;
        syncparamsArray[6] = (byte) 0;
        syncparamsArray[7] = (byte) 0;
        syncparamsArray[8] = (byte) 0;
        syncparamsArray[9] = (byte) 0;
        syncparamsArray[10] = (byte) 0;
        syncparamsArray[11] = (byte) 0;
        syncparamsArray[12] = (byte) 0;
        syncparamsArray[13] = (byte) 0;
        syncparamsArray[14] = (byte) 0;
        syncparamsArray[15] = calculateCRC(syncparamsArray);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send Monitor HR] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }

    void sendSetMonitoringHRAuto(boolean enabled, LifevitSDKAT250TimeRange range) {

        byte daysEnabled = 0x00;

        if (range != null) {
            if (range.isSunday()) daysEnabled = (byte) (daysEnabled | 0x01);
            if (range.isMonday()) daysEnabled = (byte) (daysEnabled | 0x02);
            if (range.isTuesday()) daysEnabled = (byte) (daysEnabled | 0x04);
            if (range.isWednesday()) daysEnabled = (byte) (daysEnabled | 0x08);
            if (range.isThursday()) daysEnabled = (byte) (daysEnabled | 0x10);
            if (range.isFriday()) daysEnabled = (byte) (daysEnabled | 0x20);
            if (range.isSaturday()) daysEnabled = (byte) (daysEnabled | 0x40);
        }

        byte[] syncparamsArray;
        syncparamsArray = new byte[16];
        syncparamsArray[0] = (byte) 0x19;
        syncparamsArray[1] = enabled ? (byte) 1 : (byte) 0;
        syncparamsArray[2] = intValueToByte(range.getStartHour());
        syncparamsArray[3] = intValueToByte(range.getStartMinute());
        syncparamsArray[4] = intValueToByte(range.getEndHour());
        syncparamsArray[5] = intValueToByte(range.getEndMinute());
        syncparamsArray[6] = daysEnabled;
        syncparamsArray[7] = (byte) 0;
        syncparamsArray[8] = (byte) 0;
        syncparamsArray[9] = (byte) 0;
        syncparamsArray[10] = (byte) 0;
        syncparamsArray[11] = (byte) 0;
        syncparamsArray[12] = (byte) 0;
        syncparamsArray[13] = (byte) 0;
        syncparamsArray[14] = (byte) 0;
        syncparamsArray[15] = calculateCRC(syncparamsArray);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send set monitor HR time range] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }

    void sendUpdateFirmware() {

        byte[] syncparamsArray;
        syncparamsArray = new byte[16];
        syncparamsArray[0] = (byte) 0x47;
        syncparamsArray[1] = (byte) 0;
        syncparamsArray[2] = (byte) 0;
        syncparamsArray[3] = (byte) 0;
        syncparamsArray[4] = (byte) 0;
        syncparamsArray[5] = (byte) 0;
        syncparamsArray[6] = (byte) 0;
        syncparamsArray[7] = (byte) 0;
        syncparamsArray[8] = (byte) 0;
        syncparamsArray[9] = (byte) 0;
        syncparamsArray[10] = (byte) 0;
        syncparamsArray[11] = (byte) 0;
        syncparamsArray[12] = (byte) 0;
        syncparamsArray[13] = (byte) 0;
        syncparamsArray[14] = (byte) 0;
        syncparamsArray[15] = calculateCRC(syncparamsArray);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendUpdateFirmware] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);

        // Wait for command to be received and connect to DFU
        new Handler(mLifevitSDKManager.getmHandlerThread().getLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mBluetoothGatt != null) {
                    mLifevitSDKManager.connectDevice(LifevitSDKConstants.DEVICE_BRACELET_AT250_FIRMWARE_UPDATER, 10000);
                }
            }
        }, LifevitSDKConstants.DELAY_TO_WAIT_TO_SEND_FIRMWARE_UPDATE);
    }

    void sendGetFirmwareVersionNumber() {

        byte[] syncparamsArray;
        syncparamsArray = new byte[16];
        syncparamsArray[0] = (byte) 0x27;
        syncparamsArray[1] = (byte) 0;
        syncparamsArray[2] = (byte) 0;
        syncparamsArray[3] = (byte) 0;
        syncparamsArray[4] = (byte) 0;
        syncparamsArray[5] = (byte) 0;
        syncparamsArray[6] = (byte) 0;
        syncparamsArray[7] = (byte) 0;
        syncparamsArray[8] = (byte) 0;
        syncparamsArray[9] = (byte) 0;
        syncparamsArray[10] = (byte) 0;
        syncparamsArray[11] = (byte) 0;
        syncparamsArray[12] = (byte) 0;
        syncparamsArray[13] = (byte) 0;
        syncparamsArray[14] = (byte) 0;
        syncparamsArray[15] = calculateCRC(syncparamsArray);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendGetFirmwareVersionNumber] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    /****COMMON****/


    public byte intValueToByte(int value) {
        int b0 = value % 10;
        int b1 = value / 10;

        return (byte) (0x0000 | ((0x000f) & b0) | ((0x00f0) & (b1 << 4)));
    }

    private byte calculateCRC(byte[] bytes) {
        byte sum = (byte) 0;
        for (int i = 0; i < 15; i++) {
            sum = (byte) (sum + bytes[i]);
        }
        return (byte) (sum & 0xFF);
    }


    public static String ByteToHexString(byte a) {
        String s = "";
        s = Integer.toHexString(new Byte(a).intValue());
        if (s.length() == 1) {
            return "0" + s;
        }
        return s;
    }

    public static String getDate(byte year, byte month, byte day) {
        String date = "";
        return ByteToHexString(year) + "/" + ByteToHexString(month) + "/" + ByteToHexString(day);
    }

    public static String getHeartTime(byte hour, byte min, byte secend) {
        String date = "";
        return ByteToHexString(hour) + ":" + ByteToHexString(min) + ":" + ByteToHexString(secend);
    }


    public static int getLowValue(byte b) {
        return b & 255;
    }

}