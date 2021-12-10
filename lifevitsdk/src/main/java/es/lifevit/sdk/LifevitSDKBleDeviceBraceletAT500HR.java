package es.lifevit.sdk;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import es.lifevit.sdk.bracelet.LifevitSDKAT500SedentaryReminderTimeRange;
import es.lifevit.sdk.bracelet.LifevitSDKAt500HrAlarmTime;
import es.lifevit.sdk.bracelet.LifevitSDKHeartbeatData;
import es.lifevit.sdk.bracelet.LifevitSDKSleepData;
import es.lifevit.sdk.bracelet.LifevitSDKStepData;
import es.lifevit.sdk.utils.BraceletUtils;
import es.lifevit.sdk.utils.HexUtils;
import es.lifevit.sdk.utils.LogUtils;


/**
 * Created by aescanuela on 26/1/16.
 */
public class LifevitSDKBleDeviceBraceletAT500HR extends LifevitSDKBleDevice {

    private final static String CLASS_TAG = LifevitSDKBleDeviceBraceletAT500HR.class.getSimpleName();

    private static final String DEVICE_NAME = "B521A";
    private static final String DEVICE_NAME_2 = "AT-500HR";


    /**
     * Attributes
     */

    // Sending queue, to send instructions to device
    static BraceletAT500HRSendQueue sendingThread;


    // Variables used to read data from device
    List<Byte> receivedBytes = new ArrayList<>();
    boolean isHeaderStringComplete = false;
    boolean isReceivingData = false;
    String receivedString = "";

    // Header of the last package received
    int header_dataType;
    int header_currentPackageInfoLength;
    int header_currentPackageTotalLength;
    int header_currentPackageNumber;
    int header_totalPackages;
    int header_packageCRC;
    int header_packageEencrypted;

    int mLastMessageTypeReceived = -1;

    // All measurements received in current measurement
    List<ParserData> mStepsData = new ArrayList();
    List<ParserData> mSleepData = new ArrayList();
    List<ParserData> mHeartRateData = new ArrayList();


    // Sleep types
    protected int SLEEP_TYPE_UNKNOWN = 13;
    protected int SLEEP_TYPE_DEEP_SLEEP = 12;
    protected int SLEEP_TYPE_LIGHT_SLEEP = 11;
    protected int SLEEP_TYPE_SLEEP_BREAK = 1;
    protected int SLEEP_TYPE_SLEEP_NODE = 0;

    // Sport types?
    protected static final int SPORTS_TYPE_RUN = 3;
    protected static final int SPORTS_TYPE_WALK = 2;


    /**
     * Service descriptor 4
     */

    // Custom service (primary service)
    private static final String UUID_SERVICE = "0000190a-0000-1000-8000-00805f9b34fb";

    // Properties: WRITE, WRITE_NO_RESPONS (client can write, and write with response, on this characteristic)
    // Write Type: WRITE REQUEST (will give you a response back telling you the write was successful)
    // Descriptors:
    // 1. Characteristic user description, UUID: 0x2901 (read-only, provides a textual user description for a characteristic value)
    protected static final String UUID_WRITE_CHARACTERISTIC = "00000001-0000-1000-8000-00805f9b34fb";

    // Properties: NOTIFY (allows the server to use the Handle Value Notification ATT operation on this characteristic )
    // Descriptors:
    // 1. Client Characteristic Configuration, UUID: 0x2902 (defines how the characteristic may be configured by a specific client)
    protected static final String UUID_NOTIFY_CHARACTERISTIC_READ = "00000002-0000-1000-8000-00805f9b34fb";

    protected static final String UUID_SERVICE_TO_SCAN = "0000fee7-0000-1000-8000-00805f9b34fb";


    /**
     * Actions to send to BLE device
     */

    static final int ACTION_TEST_INSTRUCTION = 0;
    static final int ACTION_QUERY_SEND_DAY_STEPS = 1;
    static final int ACTION_GET_ACTIVITY_DATA = 2;
    static final int ACTION_START_CURRENT_HEART_RATE = 3;
    static final int ACTION_END_CURRENT_HEART_RATE = 4;
    static final int ACTION_START_EXERCISE = 5;
    static final int ACTION_END_EXERCISE = 6;
    static final int ACTION_START_AUTO_STEPS = 7;
    static final int ACTION_END_AUTO_STEPS = 8;
    static final int ACTION_CONFIGURE_USER_HEIGHT = 9;
    static final int ACTION_CONFIGURE_USER_WEIGHT = 10;
    static final int ACTION_CONFIGURE_CURRENT_DATE_TIME = 11;
    static final int ACTION_CONFIGURE_DATE_TIME = 12;
    static final int ACTION_FIND_BRACELET = 13;
    static final int ACTION_SETTINGS_FIND_PHONE_ON = 14;
    static final int ACTION_SETTINGS_FIND_PHONE_OFF = 15;
    static final int ACTION_SETTINGS_ANTI_THEFT_ON = 16;
    static final int ACTION_SETTINGS_ANTI_THEFT_OFF = 17;
    static final int ACTION_SETTINGS_MONITOR_HEART_RATE_ON = 18;
    static final int ACTION_SETTINGS_MONITOR_HEART_RATE_OFF = 19;
    static final int ACTION_SETTINGS_SHOW_HOUR_WHEN_RISING_ARM_ON = 20;
    static final int ACTION_SETTINGS_SHOW_HOUR_WHEN_RISING_ARM_OFF = 21;
    static final int ACTION_MESSAGE_RECEIVED = 22;
    static final int ACTION_SETTINGS_UPDATE_TARGET_STEPS = 23;
    static final int ACTION_SETTINGS_UPDATE_HAND = 24;
    static final int ACTION_GET_VERSION = 25;
    static final int ACTION_GET_BATTERY = 26;
    static final int ACTION_BIND_DEVICE = 27;
    static final int ACTION_ACTIVATE_DEVICE = 28;
    static final int ACTION_SETTINGS_DISTANCE_UNIT = 29;
    static final int ACTION_ENABLE_SEDENTARY_REMINDER = 30;
    static final int ACTION_DISABLE_SEDENTARY_REMINDER = 31;
    static final int ACTION_SET_ALARM = 32;
    static final int ACTION_DISABLE_ALARM = 33;
    static final int ACTION_SETTINGS_CAMERA_ENABLED_OFF = 34;
    static final int ACTION_SETTINGS_CAMERA_ENABLED_ON = 35;

    private boolean isRunning = false;


    /**
     * Creator
     */

    protected LifevitSDKBleDeviceBraceletAT500HR(BluetoothDevice dev, LifevitSDKManager manager) {

        this.mBluetoothDevice = dev;
        this.mLifevitSDKManager = manager;
    }


    @Override
    protected int getType() {
        return LifevitSDKConstants.DEVICE_BRACELET_AT500HR;
    }

    /**
     * Receivers
     */

    protected void connectGatt(Context context, boolean firstTime) {

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[connection] CONNECT: " + mBluetoothDevice.getAddress());

        mBluetoothGatt = mBluetoothDevice.connectGatt(context, false, mGattCallback);
        mContext = context;
        mFirstTime = firstTime;

        sendingThread = new BraceletAT500HRSendQueue(this);
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


    protected void getVersion() {
        sendingThread.addToQueue(ACTION_GET_VERSION);
    }

    protected void findDevice() {
        sendingThread.addToQueue(ACTION_FIND_BRACELET);
    }

    protected void activateDevice() {
        sendingThread.addToQueue(ACTION_ACTIVATE_DEVICE);
    }

    protected void bindDevice() {
        sendingThread.addToQueue(ACTION_BIND_DEVICE);
    }

    protected void getBattery() {
        sendingThread.addToQueue(ACTION_GET_BATTERY);
    }

    protected void getCurrentDaySteps() {
        sendingThread.addToQueue(ACTION_QUERY_SEND_DAY_STEPS);
    }

    protected void getSyncHistoryData() {
        sendingThread.addToQueue(ACTION_GET_ACTIVITY_DATA);
    }

    protected void getCurrentHeartBeat() {
        sendingThread.addToQueue(ACTION_START_CURRENT_HEART_RATE);
    }

    protected void startExercise() {
        sendingThread.addToQueue(ACTION_START_EXERCISE);
    }

    protected void endExercise() {
        sendingThread.addToQueue(ACTION_END_EXERCISE);
    }

    protected void updateParameter(int parameter, boolean enabled) {
        switch (parameter) {
            case LifevitSDKConstants.BRACELET_PARAM_ANTILOST:

                sendingThread.addToQueue(enabled ? ACTION_SETTINGS_ANTI_THEFT_ON : ACTION_SETTINGS_ANTI_THEFT_OFF);
                break;
            case LifevitSDKConstants.BRACELET_PARAM_DATE:

                sendingThread.addToQueue(ACTION_CONFIGURE_CURRENT_DATE_TIME);
                break;
            case LifevitSDKConstants.BRACELET_PARAM_FIND_DEVICE:
                sendingThread.addToQueue(ACTION_FIND_BRACELET);
                break;
            case LifevitSDKConstants.BRACELET_PARAM_FIND_PHONE:

                sendingThread.addToQueue(enabled ? ACTION_SETTINGS_FIND_PHONE_ON : ACTION_SETTINGS_FIND_PHONE_OFF);
                break;
            case LifevitSDKConstants.BRACELET_PARAM_HRMONITOR:

                sendingThread.addToQueue(enabled ? ACTION_SETTINGS_MONITOR_HEART_RATE_ON : ACTION_SETTINGS_MONITOR_HEART_RATE_OFF);
                break;
            case LifevitSDKConstants.BRACELET_PARAM_CAMERA:

                sendingThread.addToQueue(enabled ? ACTION_SETTINGS_CAMERA_ENABLED_ON : ACTION_SETTINGS_CAMERA_ENABLED_OFF);
                break;
        }
    }

    protected void updateHandParameter(Integer mode) {
        sendingThread.addToQueue(ACTION_SETTINGS_UPDATE_HAND, mode);
    }


    protected void updateDistanceUnit(Integer distanceUnit) {
        sendingThread.addToQueue(ACTION_SETTINGS_DISTANCE_UNIT, distanceUnit);
    }


    protected void updateDate(Date date) {
        sendingThread.addToQueue(ACTION_CONFIGURE_DATE_TIME, date);
    }


    protected void updateUserHeight(Integer height) {
        PreferenceUtil.setUserHeight(mContext, height);
        sendingThread.addToQueue(ACTION_CONFIGURE_USER_HEIGHT, height);
    }

    protected void updateUserWeigth(Integer weight) {
        PreferenceUtil.setUserWeight(mContext, weight);
        sendingThread.addToQueue(ACTION_CONFIGURE_USER_WEIGHT, weight);
    }

    protected void sendNotificationToDevice(Integer type) {
        sendingThread.addToQueue(ACTION_MESSAGE_RECEIVED, type);
    }

    protected void setBraceletSedentaryReminderEnabled(LifevitSDKAT500SedentaryReminderTimeRange period) {
        sendingThread.addToQueue(ACTION_ENABLE_SEDENTARY_REMINDER, period);
    }

    protected void setBraceletSedentaryReminderDisabled() {
        sendingThread.addToQueue(ACTION_DISABLE_SEDENTARY_REMINDER);
    }

    protected void setBraceletAlarm(LifevitSDKAt500HrAlarmTime period) {
        sendingThread.addToQueue(ACTION_SET_ALARM, period);
    }

    protected void disableBraceletAlarm(Boolean isSecondaryAlarm) {
        sendingThread.addToQueue(ACTION_DISABLE_ALARM, isSecondaryAlarm);
    }


    /**
     * Other methods
     */

    protected static boolean isAt500HrBraceletDevice(String name) {
        return DEVICE_NAME.equalsIgnoreCase(name) || DEVICE_NAME_2.equalsIgnoreCase(name);
    }

    protected static boolean matchDevice(BluetoothDevice device) {
        return DEVICE_NAME.equalsIgnoreCase(device.getName()) || DEVICE_NAME_2.equalsIgnoreCase(device.getName());
    }


    protected static UUID[] getUUIDs() {
        UUID[] uuidArray = new UUID[1];
        uuidArray[0] = UUID.fromString(UUID_SERVICE_TO_SCAN);
        return uuidArray;
    }


    protected void sendDeviceStatus() {
        mLifevitSDKManager.deviceOnConnectionChanged(LifevitSDKConstants.DEVICE_BRACELET_AT500HR, mDeviceStatus, true);

        if (mDeviceStatus == LifevitSDKConstants.STATUS_CONNECTED) {
            sendUserHeight();
            sendUserWeight();
        }
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
            LogUtils.log(Log.ERROR, CLASS_TAG, "Rx service not found!");
            return;
        }

        BluetoothGattCharacteristic txCharacteristic = RxService.getCharacteristic(UUID.fromString(UUID_NOTIFY_CHARACTERISTIC_READ));

        if (txCharacteristic == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "Tx charateristic not found!");
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
            LogUtils.log(Log.ERROR, CLASS_TAG, "sendMessage: mBluetoothGatt is null");
            return;
        }
        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(UUID_SERVICE));
        if (RxService == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "Rx service not found!");
            return;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(UUID.fromString(UUID_WRITE_CHARACTERISTIC));
        if (RxChar == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "Rx charateristic not found!");
            return;
        }

        LogUtils.log(Log.DEBUG, CLASS_TAG, "SENDING Message (complete): " + HexUtils.getStringToPrint(data));

        dividePacketsAndSendMessage(RxChar, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
    }


    /*********************************************************************************************/
    /***************************  Methods to receive data from device  ***************************/
    /*********************************************************************************************/


    protected void characteristicReadProcessData(BluetoothGattCharacteristic characteristic) {

        LogUtils.log(Log.DEBUG, CLASS_TAG, "RECEIVED from characteristic: " + characteristic.getUuid().toString());

        if (characteristic.getUuid().equals(UUID.fromString(UUID_NOTIFY_CHARACTERISTIC_READ))) {

            byte[] rx = characteristic.getValue();

            String resultsStr = new String(rx);

            LogUtils.log(Log.DEBUG, CLASS_TAG, "characteristicReadProcessData - RECEIVED (byte format): " + HexUtils.getStringToPrint(rx));
            LogUtils.log(Log.DEBUG, CLASS_TAG, "characteristicReadProcessData - RECEIVED (string format): " + resultsStr);

            if (resultsStr.startsWith("AT+RUN:")) {

                int beginIndex = resultsStr.indexOf(":") + 1;
                int endIndex = resultsStr.indexOf("\r\n");
                String partialStr = resultsStr.substring(beginIndex, endIndex);

                if ("OK".equals(partialStr)) {
                    isRunning = true;
                    // Exercise started
                    if (mLifevitSDKManager.getBraceletListener() != null) {
                        mLifevitSDKManager.getBraceletListener().braceletInfoReceived("Activity started");
                    }

                } else {
                    isRunning = false;
                    // If it is a number, the exercise is finished
                    int daySteps = -1;
                    try {

                        daySteps = Integer.valueOf(partialStr);
                        LogUtils.log(Log.DEBUG, CLASS_TAG, "---> Day steps: " + daySteps);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (daySteps >= 0) {
                        // Exercise finished

                        if (mLifevitSDKManager.getBraceletListener() != null) {
                            mLifevitSDKManager.getBraceletListener().braceletActivityStepsReceived(daySteps);
                        }
                    }
                    if (mLifevitSDKManager.getBraceletListener() != null) {
                        mLifevitSDKManager.getBraceletListener().braceletInfoReceived("Activity finished");
                    }
                }

                LogUtils.log(Log.DEBUG, CLASS_TAG, "### Task finished ###");
                sendingThread.taskFinished();

            } else if (resultsStr.startsWith("AT+TOPACE:")) {

                int beginIndex = resultsStr.indexOf(":") + 1;
                int endIndex = resultsStr.indexOf("\r\n");
                String partialStr = resultsStr.substring(beginIndex, endIndex);
                try {
                    /*int daySteps = Integer.valueOf(partialStr);
                    Log.e(LifevitSDKConstants.TAG, "[" + CLASS_TAG + "] ---> Day steps: " + daySteps);
                    if (mLifevitSDKManager.getBraceletListener() != null) {
                        mLifevitSDKManager.getBraceletListener().braceletActivityStepsReceived(daySteps);
                    }*/

                } catch (Exception e) {
                    e.printStackTrace();
                }
                LogUtils.log(Log.DEBUG, CLASS_TAG, "### Task finished ###");
                sendingThread.taskFinished();

            } else if (resultsStr.startsWith("AT+PACE:")) {

                int beginIndex = resultsStr.indexOf(":") + 1;
                int endIndex = resultsStr.indexOf("\r\n");
                String partialStr = resultsStr.substring(beginIndex, endIndex);
                try {
                    int daySteps = Integer.valueOf(partialStr);
                    LogUtils.log(Log.DEBUG, CLASS_TAG, "---> Day steps: " + daySteps);
                    if (mLifevitSDKManager.getBraceletListener() != null) {
                        mLifevitSDKManager.getBraceletListener().braceletCurrentStepsReceived(getStepDataForSteps(daySteps));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                LogUtils.log(Log.DEBUG, CLASS_TAG, "### Task finished ###");
                sendingThread.taskFinished();

            } else if (resultsStr.startsWith("AT+HEART:")) {

                int beginIndex = resultsStr.indexOf(":") + 1;
                int endIndex = resultsStr.indexOf("\r\n");
                String partialStr = resultsStr.substring(beginIndex, endIndex);
                try {

                    if ("ERR".equalsIgnoreCase(partialStr)) {

                        // Error measuring Heart Rate
                        // Do not try again?

                        // Tell to bracelet to finish measuring
                        sendingThread.addToQueue(ACTION_END_CURRENT_HEART_RATE);

                        // Send to screen

                    } else if ("OK".equalsIgnoreCase(partialStr)) {

                        // Measuring is correctly finished
                        // Do nothing

                    } else {

                        int resultHeartRate = Integer.valueOf(partialStr);
                        LogUtils.log(Log.DEBUG, CLASS_TAG, "---> Received Heart Rate: " + resultHeartRate);

                        if (mLifevitSDKManager.getBraceletListener() != null) {
                            mLifevitSDKManager.getBraceletListener().braceletHeartDataReceived(resultHeartRate);
                        }

                        // Tell to bracelet to finish measuring
                        sendingThread.addToQueue(ACTION_END_CURRENT_HEART_RATE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                LogUtils.log(Log.DEBUG, CLASS_TAG, "### Task finished ###");
                sendingThread.taskFinished();

            } else if (resultsStr.startsWith("AT+DATA:")) {

                // Start of the package
                resetHeaderData();
                isReceivingData = true;
                receivedString = resultsStr;

            } else if (isReceivingData) {

                if (!isHeaderStringComplete) {

                    // Still need to complete Header
                    receivedString += resultsStr;

                    if (resultsStr.contains("\r\n")) {

                        // Is the end of Header
                        isHeaderStringComplete = true;

                        // Parse Header
                        boolean releaseQueue = false;
                        int beginIndex = receivedString.indexOf(":") + 1;
                        int endIndex = receivedString.indexOf("\r\n");
                        String partialStr = receivedString.substring(beginIndex, endIndex);
                        String[] arr = partialStr.split(",");
                        if (arr.length != 7) {
                            LogUtils.log(Log.ERROR, CLASS_TAG, "Header not correct");
                            releaseQueue = true;
                        } else {
                            header_dataType = Integer.valueOf(arr[0]);
                            header_currentPackageInfoLength = Integer.valueOf(arr[1]);
                            header_currentPackageTotalLength = Integer.valueOf(arr[2]);
                            header_currentPackageNumber = Integer.valueOf(arr[3]);
                            header_totalPackages = Integer.valueOf(arr[4]);
                            header_packageCRC = Integer.valueOf(arr[5]);
                            header_packageEencrypted = Integer.valueOf(arr[6]);

                            if (header_totalPackages == 0) {
                                releaseQueue = true;
                            }
                        }

                        if (releaseQueue) {
                            finishReadingDataAndSend();
                        }
                    }
                } else {

                    // Header is complete, so we are reading normal data.

                    // Add bytes
                    for (byte bi : rx) {
                        receivedBytes.add(bi);
                    }

                    // Until we reach full length
                    if (receivedBytes.size() >= header_currentPackageTotalLength) {

                        // Finished receiving packages. Now read and parse data.

                        List<Byte> receivedBytesToRead = receivedBytes.subList(0, header_currentPackageInfoLength);

                        // Split data in packages of 6 bytes
                        List<int[]> l = splitData(receivedBytesToRead, 6);
                        List<ParserData> mParserDataList = new ArrayList();

                        // Then parse each package
                        for (int[] bsTmp : l) {
                            ParserData parserData = parseData(bsTmp, false);
                            if (parserData != null) {
                                mParserDataList.add(parserData);
                            }
                        }

                        // Finally, treat packages depending on data type (steps, sleep, heart rate)
                        switch (header_dataType) {
                            case 0:
                                mStepsData.addAll(mParserDataList);
                                break;
                            case 3:
                                mSleepData.addAll(mParserDataList);
                                break;
                            case 7:
                                mHeartRateData.addAll(mParserDataList);
                                break;
                            case 8:
                                for (ParserData data222 : mParserDataList) {
//                                    BLEContentProvider.addBloodData(new BloodPressureData(new BleDataTime(data222.flag, getRightTime(data222.time)), data222.value >> 8, data222.value & 255));
                                    LogUtils.log(Log.DEBUG, CLASS_TAG, "--- BloodPressureData ---");
                                    LogUtils.log(Log.DEBUG, CLASS_TAG, "DATA_BLOOD_PRESSURE dbp = " + data222.value + ",sdp = " + data222.value1);
                                }
                                break;
                        }

                        // Check if there are more packages
                        int nextPackageNumber = header_currentPackageNumber + 1;

                        // If you don't read last package, bracelet data is not deleted
                        if (nextPackageNumber > header_totalPackages /* - 1 */) {
                            finishReadingDataAndSend();
                        } else {
                            sendRequestData(nextPackageNumber);
                        }
                    }
                }

            } else if (resultsStr.startsWith("NT+HEART")) {

                isRunning = true;
                // Result sent during Exercise

                int beginIndex = resultsStr.indexOf(":") + 1;
                int endIndex = resultsStr.indexOf("\r\n");
                String partialStr = resultsStr.substring(beginIndex, endIndex);

                try {
                    int heartRate = Integer.valueOf(partialStr);

                    if (mLifevitSDKManager.getBraceletListener() != null) {
                        mLifevitSDKManager.getBraceletListener().braceletHeartDataReceived(heartRate);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Do not finish queue task, as this package is sent automatically by device

            } else if (resultsStr.startsWith("NT+TOPACE")) {

                // Result sent during Exercise
                isRunning = true;

                int beginIndex = resultsStr.indexOf(":") + 1;
                int endIndex = resultsStr.indexOf("\r\n");
                String partialStr = resultsStr.substring(beginIndex, endIndex);

                try {
                    int totalSteps = Integer.valueOf(partialStr);

                    if (mLifevitSDKManager.getBraceletListener() != null) {
                        mLifevitSDKManager.getBraceletListener().braceletActivityStepsReceived(totalSteps);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Do not finish queue task, as this package is sent automatically by device

            } else if (resultsStr.startsWith("NT+RUN")) {

                // Result sent during Exercise
                isRunning = true;

                int beginIndex = resultsStr.indexOf(":") + 1;
                int endIndex = resultsStr.indexOf("\r\n");
                String started = resultsStr.substring(beginIndex, endIndex);

                try {

                    if (mLifevitSDKManager.getBraceletListener() != null) {
                        if (started.equalsIgnoreCase("OK") || started.equalsIgnoreCase("1")) {
                            mLifevitSDKManager.getBraceletListener().braceletActivityStarted();
                        } else {
                            mLifevitSDKManager.getBraceletListener().braceletActivityFinished();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Do not finish queue task, as this package is sent automatically by device

            } else if (resultsStr.startsWith("NT+BEEP")) {

                // User is trying to find phone through the bracelet
                /*try {

                    // START VIBRATION AND SOUND
                    final MediaPlayer mp = MediaPlayer.create(DagaApplication.getInstance().getApplicationContext(), R.raw.alarm_sound);
                    mp.start();

                    // Vibrate for 2 seconds
                    Vibrator v = (Vibrator) DagaApplication.getInstance().getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(AppConstants.PURIFIT_FIND_PHONE_VIBRATION_MILLISECONDS);

                } catch (Exception e) {
                    e.printStackTrace();
                }*/

                if (mLifevitSDKManager.getBraceletListener() != null) {
                    mLifevitSDKManager.getBraceletListener().braceletBeepReceived();
                }
                // Do not finish queue task, as this package is sent automatically by device

            } else if (resultsStr.startsWith("OK")
                    || resultsStr.startsWith("AT+HEIGHT")
                    || resultsStr.startsWith("AT+WEIGHT")
                    || resultsStr.startsWith("AT+DT")
                    || resultsStr.startsWith("AT+PUSH")
                    || resultsStr.startsWith("AT+DEST")
                    || resultsStr.startsWith("AT+FINDPHONE")
                    || resultsStr.startsWith("AT+ANTI_LOST")
                    || resultsStr.startsWith("AT+HRMONITOR")
                    || resultsStr.startsWith("AT+FINDBT")
                    || resultsStr.startsWith("AT+HANDSUP")
                    || resultsStr.startsWith("AT+BOND")
                    || resultsStr.startsWith("AT+VER")
                    || resultsStr.startsWith("AT+RUN")
                    || resultsStr.startsWith("AT+BATT")
                    || resultsStr.startsWith("AT+UNITS")
                    || resultsStr.startsWith("AT+ACT")
                    || resultsStr.startsWith("AT+SIT")
                    || resultsStr.startsWith("AT+ALARM")) {

                // Instructions that don't need to check data, only release semaphore.
                if (mLifevitSDKManager.getBraceletListener() != null) {
                    if (resultsStr.startsWith("AT+HEIGHT")) {
                        mLifevitSDKManager.getBraceletListener().braceletParameterSet(LifevitSDKConstants.BRACELET_PARAM_HEIGHT);
                    } else if (resultsStr.startsWith("AT+WEIGHT")) {
                        mLifevitSDKManager.getBraceletListener().braceletParameterSet(LifevitSDKConstants.BRACELET_PARAM_WEIGHT);
                    } else if (resultsStr.startsWith("AT+DT")) {
                        mLifevitSDKManager.getBraceletListener().braceletParameterSet(LifevitSDKConstants.BRACELET_PARAM_DATE);
                    } else if (resultsStr.startsWith("AT+ANCS")) {
                        mLifevitSDKManager.getBraceletListener().braceletParameterSet(LifevitSDKConstants.BRACELET_PARAM_ACNS);
                    } else if (resultsStr.startsWith("AT+DEST")) {
                        mLifevitSDKManager.getBraceletListener().braceletParameterSet(LifevitSDKConstants.BRACELET_PARAM_TARGET);
                    } else if (resultsStr.startsWith("AT+HANDSUP")) {
                        mLifevitSDKManager.getBraceletListener().braceletParameterSet(LifevitSDKConstants.BRACELET_PARAM_HANDS);
                    } else if (resultsStr.startsWith("AT+FINDPHONE")) {
                        mLifevitSDKManager.getBraceletListener().braceletParameterSet(LifevitSDKConstants.BRACELET_PARAM_FIND_PHONE);
                    } else if (resultsStr.startsWith("AT+HRMONITOR")) {
                        mLifevitSDKManager.getBraceletListener().braceletParameterSet(LifevitSDKConstants.BRACELET_PARAM_HRMONITOR);
                    } else if (resultsStr.startsWith("AT+CAMERA")) {
                        mLifevitSDKManager.getBraceletListener().braceletParameterSet(LifevitSDKConstants.BRACELET_PARAM_CAMERA);
                    } else if (resultsStr.startsWith("AT+FINDBT")) {
                        mLifevitSDKManager.getBraceletListener().braceletParameterSet(LifevitSDKConstants.BRACELET_PARAM_FIND_DEVICE);
                    } else if (resultsStr.startsWith("AT+ANTI_LOST")) {
                        mLifevitSDKManager.getBraceletListener().braceletParameterSet(LifevitSDKConstants.BRACELET_PARAM_ANTILOST);
                    } else if (resultsStr.startsWith("AT+UNITS")) {
                        mLifevitSDKManager.getBraceletListener().braceletParameterSet(LifevitSDKConstants.BRACELET_PARAM_DISTANCE_UNIT);
                    } else if (resultsStr.startsWith("AT+SIT")) {
                        mLifevitSDKManager.getBraceletListener().braceletParameterSet(LifevitSDKConstants.BRACELET_PARAM_SIT);
                    } else if (resultsStr.startsWith("AT+ALARM2")) {
                        mLifevitSDKManager.getBraceletListener().braceletParameterSet(LifevitSDKConstants.BRACELET_PARAM_ALARM_2);
                    } else if (resultsStr.startsWith("AT+ALARM")) {
                        mLifevitSDKManager.getBraceletListener().braceletParameterSet(LifevitSDKConstants.BRACELET_PARAM_ALARM_1);
                    } else if (resultsStr.startsWith("AT+OFF")) {
                        // mLifevitSDKManager.getBraceletListener().braceletInfoReceived(LifevitSDKConstants."Bracelet shutdown"];
                    } else if (resultsStr.startsWith("AT+ACT")) {
                        mLifevitSDKManager.getBraceletListener().braceletInfoReceived("Activated");
                    } else if (resultsStr.startsWith("AT+BOND:OK")) {
                        mLifevitSDKManager.getBraceletListener().braceletInfoReceived("Bond ok");
                    } else if (resultsStr.startsWith("AT+BOND:ERR")) {
                        mLifevitSDKManager.getBraceletListener().braceletInfoReceived("Bond err");
                    } else if (resultsStr.startsWith("AT+VER")) {
                        String version = resultsStr.replace("AT+VER:", "Version: ");
                        mLifevitSDKManager.getBraceletListener().braceletInfoReceived(version);
                    } else if (resultsStr.startsWith("AT+BAT")) {
                        String version = resultsStr.replace("AT+BAT:", "Battery: ");
                        mLifevitSDKManager.getBraceletListener().braceletInfoReceived(version);
                        try {

                            int beginIndex = resultsStr.indexOf(":") + 1;
                            int endIndex = resultsStr.indexOf("\r\n");
                            String partialStr = resultsStr.substring(beginIndex, endIndex);
                            Integer battery = Integer.parseInt(partialStr);
                            mLifevitSDKManager.getBraceletListener().braceletCurrentBattery(battery);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }

                LogUtils.log(Log.DEBUG, CLASS_TAG, "### Task finished ###");
                sendingThread.taskFinished();
            }
        }
    }


    /*********************************************************************************************/
    /***************************  Helper methods to parse device data  ***************************/
    /*********************************************************************************************/


    private void resetHeaderData() {
        receivedBytes = new ArrayList<>();
        isHeaderStringComplete = false;
        isReceivingData = false;
        receivedString = "";

        header_dataType = 0;
        header_currentPackageInfoLength = 0;
        header_currentPackageTotalLength = 0;
        header_currentPackageNumber = 0;
        header_totalPackages = 0;
        header_packageCRC = 0;
        header_packageEencrypted = 0;
    }


    private void resetTotalData() {
        mStepsData = new ArrayList<>();
        mSleepData = new ArrayList<>();
        mHeartRateData = new ArrayList<>();
    }


    private void finishReadingDataAndSend() {

        // We are finished - Process and send all data
        sendStepsData(mStepsData);
        sendHeartRateData(mSleepData);
        sendSleepData(mHeartRateData);

        // reset data for next call
        resetHeaderData();
        resetTotalData();

        // Operation is finished
        LogUtils.log(Log.DEBUG, CLASS_TAG, "### Task finished ###");
        sendingThread.taskFinished();
    }


    private Date getRightTime(long original_time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+0:00"));
        // Convert to milliseconds
        long original_time_millis = (original_time) * 1000L;
        // Fix date offset
        original_time_millis = original_time_millis - calendar.getTimeZone().getOffset(original_time);

        calendar.setTimeInMillis(original_time_millis);
        // Add 40 years
        calendar.add(Calendar.YEAR, 40);
        return calendar.getTime();
    }


    protected static class ParserData {
        public int flag;
        public long time;
        public int value;
        public int value1;
    }


    public static List<int[]> splitData(List<Byte> bytes, int size) {
        int[] bs = null;
        List<int[]> l = new ArrayList();
        for (int i = 0; i < bytes.size(); i++) {
            if (i % size == 0) {
                bs = new int[size];
                l.add(bs);
            }
            bs[i % size] = bytes.get(i).byteValue() & 0xFF;
        }
        return l;
    }

    public boolean isRunning() {
        return isRunning;
    }


    public static ParserData parseData(int[] arr, boolean realTime) {


        if (!realTime) {
            boolean b = true;
            for (int i : arr) {
                if (i != 255) {
                    b = false;
                    break;
                }
            }
            if (b) {
                return null;
            }
        }
        int flag = 0;
        int s = 0;
        int time = 0;
        int temp = 0;
        int value = 0;
        int usetime = 0;
        flag = arr[0] >> 6 & 0x3;
        for (s = 0; s < 4; s++) {
            time <<= 16;
            temp = arr[(s++)] << 8;
            time |= temp | arr[s];
        }
        time &= 0x3FFFFFFF;
        temp = arr[(s++)] << 8;
        value = temp | arr[(s++)];
        if (realTime) {
            temp = arr[(s++)] << 8;
            usetime = temp | arr[(s++)];
        }

        ParserData parserData = new ParserData();
        parserData.flag = flag;
        parserData.time = time;
        parserData.value = value;
        parserData.value1 = usetime;
        return parserData;
    }


    /*********************************************************************************************/
    /*****************************  Methods to send info to activity  ****************************/
    /*********************************************************************************************/


    private void sendStepsData(List<ParserData> mParserDataList) {

        List<LifevitSDKStepData> resultList = new ArrayList<>();

        // Order records by date
        Collections.sort(mParserDataList, new Comparator<ParserData>() {
            @Override
            public int compare(ParserData parserData, ParserData t1) {
                Long a = parserData.time;
                Long b = t1.time;
                return a.compareTo(b);
            }
        });

        for (ParserData data : mParserDataList) {

            Date measurementDate = getRightTime(data.time);
            // Sports Time Offset
            measurementDate.setTime(measurementDate.getTime() - 35000);

            LogUtils.log(Log.DEBUG, CLASS_TAG, "--- SportsData --- Flag: " + data.flag + ", Time: " + measurementDate + ", Data: " + data.value);

            int steps = data.value;

            if (steps > 0) {

                // Distance Unit = 1 --> Meters
                float stepDistanceMeters = getDistanceByStep(steps, PreferenceUtil.getUserHeight(mContext), 1);
                float stepDistanceKMUnit = stepDistanceMeters / 1000.0f;
                float cal = getCalorie(PreferenceUtil.getUserWeight(mContext), stepDistanceMeters);

                // create new record
                LifevitSDKStepData record = new LifevitSDKStepData(measurementDate.getTime(), steps, cal, stepDistanceKMUnit);
                resultList.add(record);
            }
        }

        // Send response even if it is empty
//        if (resultList.size() > 0) {
        if (mLifevitSDKManager.getBraceletListener() != null) {
            mLifevitSDKManager.getBraceletListener().braceletSyncStepsReceived(resultList);
        }
//        }
    }


    private void sendSleepData(List<ParserData> mParserDataList) {

        List<LifevitSDKSleepData> resultList = new ArrayList<>();

        // Order records by date
        Collections.sort(mParserDataList, new Comparator<ParserData>() {
            @Override
            public int compare(ParserData parserData, ParserData t1) {
                Long a = parserData.time;
                Long b = t1.time;
                return a.compareTo(b);
            }
        });

        for (ParserData data : mParserDataList) {

            Date measurementDate = getRightTime(data.time);
            // Sleep Time Offset
            measurementDate.setTime(measurementDate.getTime() - 10000);

            LogUtils.log(Log.DEBUG, CLASS_TAG, "--- SleepData --- Flag: " + data.flag + ", Time: " + measurementDate + ", Sleep deepness: " + data.value);

            if (data.flag == 0) {
                // Start of a sleep block - Do nothing

            } else if (data.flag == 3) {
                // End of a sleep block, might be less than 10 minutes

            } else {
                // Sleep block of 10 minutes

                // Get starting range
                long lastHalfHourTime = measurementDate.getTime() - (measurementDate.getTime() % 10 * 60 * 1000);

                // create new record
                LifevitSDKSleepData record = new LifevitSDKSleepData();
                record.setSleepDeepness(data.value == SLEEP_TYPE_LIGHT_SLEEP ? LifevitSDKConstants.LIGHT_SLEEP : LifevitSDKConstants.DEEP_SLEEP);
                record.setSleepDuration(10);
                record.setDate(lastHalfHourTime);
                resultList.add(record);
            }
        }

        // Send response even if it is empty
//        if (resultList.size() > 0) {
        if (mLifevitSDKManager.getBraceletListener() != null) {
            mLifevitSDKManager.getBraceletListener().braceletSyncSleepReceived(resultList);
        }
//        }
    }


    private void sendHeartRateData(List<ParserData> mParserDataList) {

        List<LifevitSDKHeartbeatData> resultList = new ArrayList<>();

        // Order records by date
        Collections.sort(mParserDataList, new Comparator<ParserData>() {
            @Override
            public int compare(ParserData parserData, ParserData t1) {
                Long a = parserData.time;
                Long b = t1.time;
                return a.compareTo(b);
            }
        });

        for (ParserData data : mParserDataList) {

            LogUtils.log(Log.DEBUG, CLASS_TAG, "--- HeartRateData --- Flag: " + data.flag
                    + ", Time: " + getRightTime(data.time) + ", Heart rate: " + (data.value & 255) + ", Blood Oxygen: " + (data.value >> 8));

            Date measurementDate = getRightTime(data.time);

            // Minute without seconds nor milliseconds
            Calendar measurementCal = Calendar.getInstance();
            measurementCal.setTime(measurementDate);
            measurementCal.set(Calendar.SECOND, 0);
            measurementCal.set(Calendar.MILLISECOND, 0);

            // create new record
            LifevitSDKHeartbeatData record = new LifevitSDKHeartbeatData();
            record.setHeartRate(data.value);
            record.setDate(measurementCal.getTimeInMillis());
            resultList.add(record);
        }

        // Send response even if it is empty
//        if (resultList.size() > 0) {
        if (mLifevitSDKManager.getBraceletListener() != null) {
            mLifevitSDKManager.getBraceletListener().braceletSyncHeartReceived(resultList);
        }
//        }
    }


    private LifevitSDKStepData getStepDataForSteps(int steps) {

        float stepDistanceMeters = getDistanceByStep(steps, PreferenceUtil.getUserHeight(mContext), 1);
        float stepDistanceKMUnit = stepDistanceMeters / 1000.0f;
        float cal = getCalorie(PreferenceUtil.getUserWeight(mContext), stepDistanceMeters);

        // create new record
        LifevitSDKStepData record = new LifevitSDKStepData(Calendar.getInstance().getTimeInMillis(), steps, cal, stepDistanceKMUnit);

        return record;
    }


    /*********************************************************************************************/
    /**************************  Methods to send instruction to device  **************************/
    /*********************************************************************************************/

    protected void sendTestInstruction() {
        String instruction = "AT\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }

    protected void sendGetBattery() {
        String instruction = "AT+BATT\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }

    protected void sendGetVersion() {
        String instruction = "AT+VER\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }

    protected void sendBindDevice() {
        String instruction = "AT+BOND\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }

    protected void sendActivateDevice() {
        String instruction = "AT+ACT=1\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }


    protected void sendQueryDaySteps() {
        String instruction = "AT+PACE\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }


    protected void sendRequestData(int packageNumber) {
        String instruction = "AT+DATA=" + packageNumber + "\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }


    protected void sendCurrentHeartRate(boolean isStart) {

        // Send command to bracelet
        String instruction = "AT+HEART=" + (isStart ? "1" : "0") + "\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }


    protected void sendStartOrFinishExercise(boolean isStart) {
        String instruction = "AT+RUN=" + (isStart ? "1" : "0") + "\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }


    protected void sendStartOrFinishAutoSteps(boolean isStart) {
        String instruction = "AT+TOPACE=" + (isStart ? "1" : "0") + "\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }


    protected void sendUserHeight() {

        String height = String.valueOf(PreferenceUtil.getUserHeight(mContext));

        while (height.length() < 3) {
            height = "0" + height;
        }

        String instruction = "AT+HEIGHT=" + height + "\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }


    protected void sendUserWeight() {

        String weight = String.valueOf(PreferenceUtil.getUserWeight(mContext));

        while (weight.length() < 3) {
            weight = "0" + weight;
        }

        String instruction = "AT+WEIGHT=" + weight + "\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }


    protected void sendCurrentDatetime() {

        Calendar cal = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        String date = sdf.format(cal.getTime());

        String instruction = "AT+DT=" + date + "\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }


    protected void sendEnableCamara(Boolean enabled) {

        String instruction = "AT+CAMERA=" + (enabled ? "1" : "0") + "\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }


    protected void sendDatetime(Date date) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        String dateStr = sdf.format(date);

        String instruction = "AT+DT=" + dateStr + "\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }


    protected void sendSettingFindPhone(boolean enabled) {
        String instruction = "AT+FINDPHONE=" + (enabled ? "1" : "0") + "\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }


    protected void sendSettingAntiTheft(boolean enabled) {
        String instruction = "AT+ANTI_LOST=" + (enabled ? "1" : "0") + "\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }


    protected void sendSettingDistanceUnit(int distanceUnit) {
        String instruction = "AT+UNITS=" + distanceUnit + "\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }


    protected void sendSettingMonitorHeartRate(boolean enabled) {
        String instruction = "AT+HRMONITOR=" + (enabled ? "1" : "0") + "\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }


    protected void sendFindDevice() {
        String instruction = "AT+FINDBT\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }


    protected void sendSettingArm(Integer value) {
        String instruction = "AT+HANDSUP=" + value + "\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }


    protected void sendPushNotificationArrived(Integer mLastMessageTypeReceived) {

        if (mLastMessageTypeReceived != -1) {

            String languageType = "0";
            String str = "";
            String time_screen_on = "0";

            String instruction = "AT+PUSH=" + languageType + "," + str + "," + time_screen_on + "," + mLastMessageTypeReceived + "\r\n";
            LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
            sendMessage(instruction.getBytes());

            // Reset
            mLastMessageTypeReceived = -1;
        } else {

            // Do not send message, but release queue

            LogUtils.log(Log.DEBUG, CLASS_TAG, "### Task finished ###");
            sendingThread.taskFinished();
        }
    }


    protected void sendUpdateTargetSteps(int targetSteps) {

        // Convert target steps to 5 characters string
        String targetStepsStr = String.valueOf(targetSteps);
        while (targetStepsStr.length() < 5) {
            targetStepsStr = "0" + targetStepsStr;
        }

        // Send command
        String instruction = "AT+DEST=" + targetStepsStr + "\r\n";
        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);
        sendMessage(instruction.getBytes());
    }


    protected void sendSetBraceletSedentaryReminderEnabled(boolean enabled, LifevitSDKAT500SedentaryReminderTimeRange period) {

        String cycle = "000";
        switch (period.getIntervalCode()) {
            case PERIOD_30_MIN:
                cycle = "030";
                break;
            case PERIOD_60_MIN:
                cycle = "060";
                break;
            case PERIOD_90_MIN:
                cycle = "090";
                break;
            case PERIOD_120_MIN:
                cycle = "120";
                break;
        }

        String startTime = period.formatStartTime();
        String endTime = period.formatEndTime();

        String enabledStr = enabled ? "1" : "0";

        // Send command
        String instruction = "AT+SIT=" + cycle + "," + startTime + "," + endTime + "," + enabledStr + "\r\n";

        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);

        try {

            byte[] utfs = instruction.getBytes(Charset.forName("UTF-8"));
            LogUtils.log(Log.DEBUG, CLASS_TAG, "[Sent in UTF]: " + HexUtils.getStringToPrint(utfs));

            sendMessage(utfs);

        } catch (Exception e) {

            LogUtils.log(Log.DEBUG, CLASS_TAG, "[Sent in default Encoding]: ");

            sendMessage(instruction.getBytes());
            e.printStackTrace();
        }
    }


    protected void sendSetAlarm(LifevitSDKAt500HrAlarmTime period) {

        String alarmNumber = (period.isSecondaryAlarm() ? "2" : "");
        String enabled = "1";

        String weekDays = "";
        weekDays += period.isSunday() ? "1" : "0";
        weekDays += period.isMonday() ? "1" : "0";
        weekDays += period.isTuesday() ? "1" : "0";
        weekDays += period.isWednesday() ? "1" : "0";
        weekDays += period.isThursday() ? "1" : "0";
        weekDays += period.isFriday() ? "1" : "0";
        weekDays += period.isSaturday() ? "1" : "0";

        String time = BraceletUtils.formatTime(period.getHour(), period.getMinute());

        // Send command
        String instruction = "AT+ALARM" + alarmNumber + "=" + enabled + ",00," + weekDays + "1," + time + "\r\n";

        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);

        try {

            byte[] utfs = instruction.getBytes(Charset.forName("UTF-8"));
            LogUtils.log(Log.DEBUG, CLASS_TAG, "[Sent in UTF]: " + HexUtils.getStringToPrint(utfs));

            sendMessage(utfs);

        } catch (Exception e) {

            LogUtils.log(Log.DEBUG, CLASS_TAG, "[Sent in default Encoding]: ");

            sendMessage(instruction.getBytes());
            e.printStackTrace();
        }
    }


    protected void sendDisableAlarm(Boolean isSecondaryAlarm) {

        String alarmNumber = (isSecondaryAlarm ? "2" : "");
        String enabled = "0";

        // Send command
        String instruction = "AT+ALARM" + alarmNumber + "=" + enabled + ",00,00000000,0000\r\n";

        LogUtils.log(Log.DEBUG, CLASS_TAG, "SEND: " + instruction);

        try {

            byte[] utfs = instruction.getBytes(Charset.forName("UTF-8"));
            LogUtils.log(Log.DEBUG, CLASS_TAG, "[Sent in UTF]: " + HexUtils.getStringToPrint(utfs));

            sendMessage(utfs);

        } catch (Exception e) {

            LogUtils.log(Log.DEBUG, CLASS_TAG, "[Sent in default Encoding]: ");

            sendMessage(instruction.getBytes());
            e.printStackTrace();
        }
    }


    /**********************************************************************************************/
    /**********************  Original code calculations (SportsCalculation)  **********************/
    /**********************************************************************************************/

    protected static float getDistanceByStep(long step, int height, int unit) {
        float dis = (float) (((((double) height) / 3.0d) / 100.0d) * ((double) step));
        if (unit == 0) {
            return (float) (((double) dis) * 1.09d);
        }
        return dis;
    }

    protected static float getDistanceByStep(long step, int height) {
        return (float) (((((double) height) / 3.0d) / 100.0d) * ((double) step));
    }

    protected static float toCm(long height, int unit) {
        if (unit == 0) {
            return ((float) height) / 0.3937f;
        }
        return (float) height;
    }

    protected static float getCalorie(double weight, float dis) {
        return (float) (((2.2100000381469727d * weight) * 0.7080000042915344d) * ((double) (dis / 1000.0f)));
    }

    protected static boolean isAerobic(long step, long second) {
        return step / second <= 3;
    }

    protected static double getPaceMinutesPerKm(double minute, int distance) {
        if (distance == 0) {
            return 0.0d;
        }
        return (minute / ((double) distance)) * 1000.0d;
    }


}