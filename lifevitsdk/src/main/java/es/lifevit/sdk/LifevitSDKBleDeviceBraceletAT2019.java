package es.lifevit.sdk;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import es.lifevit.sdk.bracelet.LifevitSDKAlarmTime;
import es.lifevit.sdk.bracelet.LifevitSDKAppNotification;
import es.lifevit.sdk.bracelet.LifevitSDKBraceletData;
import es.lifevit.sdk.bracelet.LifevitSDKHeartbeatData;
import es.lifevit.sdk.bracelet.LifevitSDKMonitoringAlarm;
import es.lifevit.sdk.bracelet.LifevitSDKSedentaryAlarm;
import es.lifevit.sdk.bracelet.LifevitSDKSleepData;
import es.lifevit.sdk.bracelet.LifevitSDKStepData;
import es.lifevit.sdk.bracelet.LifevitSDKSummarySleepData;
import es.lifevit.sdk.bracelet.LifevitSDKSummaryStepData;
import es.lifevit.sdk.utils.HexUtils;
import es.lifevit.sdk.utils.LogUtils;
import es.lifevit.sdk.utils.Utils;


/**
 * Created by aescanuela on 26/1/16.
 */
public class LifevitSDKBleDeviceBraceletAT2019 extends LifevitSDKBleDevice {

    private final static String CLASS_TAG = LifevitSDKBleDeviceBraceletAT2019.class.getSimpleName();

    private static final String DEVICE_NAME = "ID132Color HR";
    private static final String DEVICE_SECOND_NAME = "ID130Plus Color HR";

    private ArrayList<LifevitSDKHeartbeatData> heartRateDataArray = new ArrayList<>();
    private ArrayList<LifevitSDKStepData> stepDataArray = new ArrayList<>();
    private ArrayList<LifevitSDKSleepData> sleepDataArray = new ArrayList<>();

    private long recordDate;
    private int packetDurationInMinutes;
    private int totalPackages;
    private int totalItems;

    /**
     * Attributes
     */

    // Sending queue, to send instructions to device
    static BraceletAT2019SendQueue sendingThread;


    Calendar sportsDataPacketDate;
    int sportsDataPacketDuration;
    int sportsDataTotalPackages;

    List<LifevitSDKStepData> sportsDataList = new ArrayList<>();


    /**
     * Service descriptor 4
     */

    // Custom service (primary service)
    private static final String UUID_SERVICE = "00000af0-0000-1000-8000-00805f9b34fb";

    // Properties: WRITE, WRITE_NO_RESPONS (client can write, and write with response, on this characteristic)
    // Write Type: WRITE REQUEST (will give you a response back telling you the write was successful)
    // Descriptors:
    // 1. Characteristic user description, UUID: 0x2901 (read-only, provides a textual user description for a characteristic value)
    protected static final String UUID_WRITE_CHARACTERISTIC = "00000AF6-0000-1000-8000-00805f9b34fb";

    // Properties: NOTIFY (allows the server to use the Handle Value Notification ATT operation on this characteristic )
    // Descriptors:
    // 1. Client Characteristic Configuration, UUID: 0x2902 (defines how the characteristic may be configured by a specific client)
    protected static final String UUID_NOTIFY_CHARACTERISTIC_READ = "00000AF7-0000-1000-8000-00805f9b34fb";


    protected static final String UUID_WRITE_CHARACTERISTIC_2 = "00000AF1-0000-1000-8000-00805f9b34fb";

    protected static final String UUID_NOTIFY_CHARACTERISTIC_READ_2 = "00000AF2-0000-1000-8000-00805f9b34fb";


    /**
     * Actions to send to BLE device
     */

    public static final int ACTION_GET_BASIC_INFO = 0;
    public static final int ACTION_GET_FEATURE_LIST = 1;
    public static final int ACTION_SET_TIME = 2;
    public static final int ACTION_GET_DEVICE_TIME = 3;
    public static final int ACTION_SYNC_DATA = 4;
    public static final int ACTION_SYNC_SPORTS_DATA = 5;
    public static final int ACTION_SYNCHRONIZE_SLEEP_DATA = 6;
    public static final int ACTION_SYNCHRONIZE_HEART_RATE_DATA = 7;
    public static final int ACTION_SYNCHRONIZE_HISTORIC_SPORT_DATA = 8;
    public static final int ACTION_SYNCHRONIZE_HISTORIC_SLEEP_DATA = 9;
    public static final int ACTION_SYNCHRONIZE_HISTORIC_HEART_RATE_DATA = 10;
    public static final int ACTION_CONFIGURE_ALARM = 11;
    public static final int ACTION_SET_GOALS = 12;

    public static final int ACTION_SET_USER_INFORMATION = 13;
    public static final int ACTION_CONFIGURE_BRACELET_SEDENTARY_ALARM = 14;
    public static final int ACTION_ANTITHEFT = 15;
    public static final int ACTION_RISE_HAND = 16;
    public static final int ACTION_ANDROID_PHONE = 17;
    public static final int ACTION_HEART_RATE_INTERVAL_SETTING = 18;
    public static final int ACTION_HEART_RATE_MONITORING = 19;
    public static final int ACTION_FIND_PHONE = 20;
    public static final int ACTION_ACNS = 21;
    public static final int ACTION_SLEEP_MONITORING = 22;

    public static final int ACTION_BATTERY = 23;

    //public static final int ACTION_GET_REAL_TIME_DATA = 3;
    //public static final int ACTION_END_SYNC_DATA = 5;
    //public static final int ACTION_BIND = 8;
    //public static final int ACTION_UNBIND = 9;
    //public static final int ACTION_GET_MAC = 12;
    //public static final int ACTION_SERIAL_NUMBER = 14;
    //public static final int ACTION_GET_NOTIFICATION_STATUS = 19;


    /**
     * Creator
     */

    protected LifevitSDKBleDeviceBraceletAT2019(BluetoothDevice dev, LifevitSDKManager manager) {

        this.mBluetoothDevice = dev;
        this.mLifevitSDKManager = manager;
    }


    @Override
    protected int getType() {
        return LifevitSDKConstants.DEVICE_BRACELET_AT2019;
    }

    /**
     * Receivers
     */

    protected void connectGatt(Context context, boolean firstTime) {

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[connection] CONNECT: " + mBluetoothDevice.getAddress());

        mBluetoothGatt = mBluetoothDevice.connectGatt(context, false, mGattCallback);
        mContext = context;
        mFirstTime = firstTime;

        sendingThread = new BraceletAT2019SendQueue(this);
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
     * Public methods
     */

    protected void getBasicInfo() {
        sendingThread.addToQueue(ACTION_GET_BASIC_INFO);
    }

    protected void getFeatureList() {
        sendingThread.addToQueue(ACTION_GET_FEATURE_LIST);
    }

    protected void setTime(Long datetime) {
        sendingThread.addToQueue(ACTION_SET_TIME, datetime);
    }

    protected void getDeviceTime() {
        sendingThread.addToQueue(ACTION_GET_DEVICE_TIME);
    }

    protected void synchronizeData() {
        sendingThread.addToQueue(ACTION_SYNC_DATA);
    }

    /*
    protected void endSynchronizeData() {
        sendingThread.addToQueue(ACTION_END_SYNC_DATA);
    }
    */


    protected void synchronizeSportsData() {
        sendingThread.addToQueue(ACTION_SYNC_SPORTS_DATA);
    }

    /*
    protected void cancelOp() {
        sendingThread.taskFinished();
    }
    */


    protected void synchronizeSleepData() {
        sendingThread.addToQueue(ACTION_SYNCHRONIZE_SLEEP_DATA);
    }

    protected void synchronizeHeartRateData() {
        sendingThread.addToQueue(ACTION_SYNCHRONIZE_HEART_RATE_DATA);
    }

    protected void synchronizeHistoricSportData() {
        sendingThread.addToQueue(ACTION_SYNCHRONIZE_HISTORIC_SPORT_DATA);
    }

    protected void synchronizeHistoricSleepData() {
        sendingThread.addToQueue(ACTION_SYNCHRONIZE_HISTORIC_SLEEP_DATA);
    }

    protected void synchronizeHistoricHeartRateData() {
        sendingThread.addToQueue(ACTION_SYNCHRONIZE_HISTORIC_HEART_RATE_DATA);
    }

    protected void configureAlarm(LifevitSDKAlarmTime alarm) {
        sendingThread.addToQueue(ACTION_CONFIGURE_ALARM, alarm);
    }

    protected void setGoals(int steps, LifevitSDKAlarmTime alarm) {
        sendingThread.addToQueue(ACTION_SET_GOALS, steps, alarm);
    }

    protected void setUserInformation(LifevitSDKUserData user) {
        sendingThread.addToQueue(ACTION_SET_USER_INFORMATION, user);
    }

    protected void configureBraceletSedentaryAlarm(LifevitSDKMonitoringAlarm alarm) {
        sendingThread.addToQueue(ACTION_CONFIGURE_BRACELET_SEDENTARY_ALARM, alarm);
    }

    protected void configureAntitheft(Boolean active) {
        sendingThread.addToQueue(ACTION_ANTITHEFT, active);
    }

    protected void configureRiseHand(Boolean leftHand) {
        sendingThread.addToQueue(ACTION_RISE_HAND, leftHand);
    }

    protected void configureAndroidPhone() {
        sendingThread.addToQueue(ACTION_ANDROID_PHONE);
    }

    protected void configureHeartRateIntervalSetting(int burnFatThreshold, int aerobicExercise, int limitExercise, int userMaxHR) {
        sendingThread.addToQueue(ACTION_HEART_RATE_INTERVAL_SETTING, burnFatThreshold, aerobicExercise, limitExercise, userMaxHR);
    }

    protected void configureHeartRateMonitoring(Boolean enabled, Boolean timeIntervalEnabled, LifevitSDKSedentaryAlarm monitoringTime) {
        sendingThread.addToQueue(ACTION_HEART_RATE_MONITORING, enabled, timeIntervalEnabled, monitoringTime);
    }

    protected void configureFindPhone(Boolean enabled) {
        sendingThread.addToQueue(ACTION_FIND_PHONE, enabled);
    }

    protected void configureACNS(LifevitSDKAppNotification appNotification) {
        sendingThread.addToQueue(ACTION_ACNS, appNotification);
    }

    protected void configureSleepMonitoring(Boolean enabled, LifevitSDKMonitoringAlarm monitoringTime) {
        sendingThread.addToQueue(ACTION_SLEEP_MONITORING, enabled, monitoringTime);
    }




/*
    protected void getMAC() {
        sendingThread.addToQueue(ACTION_GET_MAC);
    }

    protected void getBattery() {
        sendingThread.addToQueue(ACTION_BATTERY);
    }

    protected void getSerialNumberAT2019() {
        sendingThread.addToQueue(ACTION_SERIAL_NUMBER);
    }

   protected void getNotificationStatus() {
        sendingThread.addToQueue(ACTION_GET_NOTIFICATION_STATUS);
    }
*/


    /**
     * Other methods
     */

    protected static boolean isAt2019BraceletDevice(String name) {
        return DEVICE_NAME.equalsIgnoreCase(name) || DEVICE_SECOND_NAME.equalsIgnoreCase(name);
    }

    protected static boolean matchDevice(BluetoothDevice device) {
        return DEVICE_NAME.equalsIgnoreCase(device.getName()) || DEVICE_SECOND_NAME.equalsIgnoreCase(device.getName());
    }


    protected static UUID[] getUUIDs() {
        UUID[] uuidArray = new UUID[1];
        uuidArray[0] = UUID.fromString(UUID_SERVICE);
        return uuidArray;
    }


    protected void sendDeviceStatus() {
        mLifevitSDKManager.deviceOnConnectionChanged(LifevitSDKConstants.DEVICE_BRACELET_AT2019, mDeviceStatus, true);

//        if (mDeviceStatus == LifevitSDKConstants.STATUS_CONNECTED) {
//            sendUserHeight();
//            sendUserWeight();
//        }
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

        // --- Second characteristic

        BluetoothGattCharacteristic txCharacteristic2 = RxService.getCharacteristic(UUID.fromString(UUID_NOTIFY_CHARACTERISTIC_READ_2));

        if (txCharacteristic2 == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "Tx charateristic2 not found!");
            return;
        }

        mBluetoothGatt.setCharacteristicNotification(txCharacteristic2, true);

        for (BluetoothGattDescriptor descriptor : txCharacteristic2.getDescriptors()) {

            // It's a notify characteristic
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                write(descriptor);
            }
        }
    }


    protected void sendMessage(byte[] data) {
        sendMessage(data, 1);
    }


    protected void sendMessage(byte[] data, int characteristic) {

        if (mBluetoothGatt == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "sendMessage: mBluetoothGatt is null");
            return;
        }
        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(UUID_SERVICE));
        if (RxService == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "Rx service not found!");
            return;
        }

        BluetoothGattCharacteristic RxChar;

        if (characteristic == 1) {
            RxChar = RxService.getCharacteristic(UUID.fromString(UUID_WRITE_CHARACTERISTIC));
        } else {
            RxChar = RxService.getCharacteristic(UUID.fromString(UUID_WRITE_CHARACTERISTIC_2));
        }

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

    private void processBasicInfo(byte[] rx) {
        int deviceId = Utils.bytesToInt(new byte[]{0, 0, rx[3], rx[2]});
        int firmwareVersion = rx[4];

        // running mode（0x00：sports mode， 0x01：sleep mode）
        int runMode = rx[5];

        // battery status （0x00： normal， 0x01：charging，0x02：fully charged， 0x03：low
        //battery）
        int batteryStatus = rx[6];

        int batteryPower = rx[7];

        // binding flag（0x01： Already bound， 0x00：Unbound）
        int bindingFlag = rx[8];

        // reboot flag（0x00：No restart， 0x01：Have a reboot）
        // it needs to synchronize the setting information when the restart flag is 1.
        int restartFlag = rx[9];

        // detailed version（0x00：NO， 0x01：YES）
        int hasDetailVersion = rx[10];

        LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> Received get basic info. Data received:");
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - deviceId: " + deviceId);
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - firmwareVersion: " + firmwareVersion);
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - runMode: " + runMode);
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - batteryStatus: " + batteryStatus);
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - batteryPower: " + batteryPower);
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - bindingFlag: " + bindingFlag);
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - restartFlag: " + restartFlag);
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - hasDetailVersion: " + hasDetailVersion);

        String message = ">>> Received get basic info. Data received:";
        message = message + "    - deviceId: " + deviceId;
        message = message + "    - firmwareVersion: " + firmwareVersion;
        message = message + "    - runMode: " + runMode;
        message = message + "    - batteryStatus: " + batteryStatus;
        message = message + "    - batteryPower: " + batteryPower;
        message = message + "    - bindingFlag: " + bindingFlag;
        message = message + "    - restartFlag: " + restartFlag;
        message = message + "    - hasDetailVersion: " + hasDetailVersion;

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {
            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
        }


        sendingThread.taskFinished();
    }


    private void processGetFeatureList(byte[] rx) {

        int main_function = rx[2];
        int alarm_count = rx[3];
        int alarm_type = rx[4];
        int control = rx[5];
        int call_notify = rx[6];
        int msg_notify1 = rx[7];
        int other = rx[8];
        int msg_cfg = rx[9];
        int msg_notify2 = rx[10];
        int other2 = rx[11];
        int sport_type0 = rx[12];
        int sport_type1 = rx[13];
        int sport_type2 = rx[14];
        int sport_type3 = rx[15];
        int main1 = rx[16];
        int msg_notify3 = rx[17];
        int sport_num_show = rx[18];
        int lang_type = rx[19];


        LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> Received get feature list. Data received");
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - main_function: " + String.format("%8s", Integer.toBinaryString(main_function & 0xFF)).replace(' ', '0'));
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - main1: " + String.format("%8s", Integer.toBinaryString(main1 & 0xFF)).replace(' ', '0'));
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - alarm_type: " + String.format("%8s", Integer.toBinaryString(alarm_type & 0xFF)).replace(' ', '0'));
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - alarm_count: " + String.format("%8s", Integer.toBinaryString(alarm_count & 0xFF)).replace(' ', '0'));
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - control: " + String.format("%8s", Integer.toBinaryString(control & 0xFF)).replace(' ', '0'));
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - call_notify: " + String.format("%8s", Integer.toBinaryString(call_notify & 0xFF)).replace(' ', '0'));
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - msg_notify1: " + String.format("%8s", Integer.toBinaryString(msg_notify1 & 0xFF)).replace(' ', '0'));
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - msg_notify2: " + String.format("%8s", Integer.toBinaryString(msg_notify2 & 0xFF)).replace(' ', '0'));
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - msg_notify3: " + String.format("%8s", Integer.toBinaryString(msg_notify3 & 0xFF)).replace(' ', '0'));
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - msg_cfg: " + String.format("%8s", Integer.toBinaryString(msg_cfg & 0xFF)).replace(' ', '0'));
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - sport_type0: " + String.format("%8s", Integer.toBinaryString(sport_type0 & 0xFF)).replace(' ', '0'));
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - sport_type1: " + String.format("%8s", Integer.toBinaryString(sport_type1 & 0xFF)).replace(' ', '0'));
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - sport_type2: " + String.format("%8s", Integer.toBinaryString(sport_type2 & 0xFF)).replace(' ', '0'));
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - sport_type3: " + String.format("%8s", Integer.toBinaryString(sport_type3 & 0xFF)).replace(' ', '0'));
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - sport_num_show: " + String.format("%8s", Integer.toBinaryString(sport_num_show & 0xFF)).replace(' ', '0'));
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - other: " + String.format("%8s", Integer.toBinaryString(other & 0xFF)).replace(' ', '0'));
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - other2: " + String.format("%8s", Integer.toBinaryString(other2 & 0xFF)).replace(' ', '0'));
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - lang_type: " + String.format("%8s", Integer.toBinaryString(lang_type & 0xFF)).replace(' ', '0'));

        String message = ">>> Received get feature list. Data received:";
        message = message + "    - main_function: " + String.format("%8s", Integer.toBinaryString(main_function & 0xFF)).replace(' ', '0');
        message = message + "    - main1: " + String.format("%8s", Integer.toBinaryString(main1 & 0xFF)).replace(' ', '0');
        message = message + "    - alarm_type: " + String.format("%8s", Integer.toBinaryString(alarm_type & 0xFF)).replace(' ', '0');
        message = message + "    - alarm_count: " + String.format("%8s", Integer.toBinaryString(alarm_count & 0xFF)).replace(' ', '0');
        message = message + "    - control: " + String.format("%8s", Integer.toBinaryString(control & 0xFF)).replace(' ', '0');
        message = message + "    - call_notify: " + String.format("%8s", Integer.toBinaryString(call_notify & 0xFF)).replace(' ', '0');
        message = message + "    - msg_notify1: " + String.format("%8s", Integer.toBinaryString(msg_notify1 & 0xFF)).replace(' ', '0');
        message = message + "    - msg_notify2: " + String.format("%8s", Integer.toBinaryString(msg_notify2 & 0xFF)).replace(' ', '0');
        message = message + "    - msg_notify3: " + String.format("%8s", Integer.toBinaryString(msg_notify3 & 0xFF)).replace(' ', '0');
        message = message + "    - msg_cfg: " + String.format("%8s", Integer.toBinaryString(msg_cfg & 0xFF)).replace(' ', '0');
        message = message + "    - sport_type0: " + String.format("%8s", Integer.toBinaryString(sport_type0 & 0xFF)).replace(' ', '0');
        message = message + "    - sport_type1: " + String.format("%8s", Integer.toBinaryString(sport_type1 & 0xFF)).replace(' ', '0');
        message = message + "    - sport_type2: " + String.format("%8s", Integer.toBinaryString(sport_type2 & 0xFF)).replace(' ', '0');
        message = message + "    - sport_type3: " + String.format("%8s", Integer.toBinaryString(sport_type3 & 0xFF)).replace(' ', '0');
        message = message + "    - sport_num_show: " + String.format("%8s", Integer.toBinaryString(sport_num_show & 0xFF)).replace(' ', '0');
        message = message + "    - other: " + String.format("%8s", Integer.toBinaryString(other & 0xFF)).replace(' ', '0');
        message = message + "    - other2: " + String.format("%8s", Integer.toBinaryString(other2 & 0xFF)).replace(' ', '0');
        message = message + "    - lang_type: " + String.format("%8s", Integer.toBinaryString(lang_type & 0xFF)).replace(' ', '0');

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {
            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
        }

        //sendingThread.taskFinished();

        sendGetExtendedFeatures();
    }

    private void processGetExtendedFeatureList(byte[] rx) {

        int langTypes2 = rx[2];
        int other = rx[3];

        String message = ">>> Received get extended feature list. Data received:";
        message = message + "    - other: " + String.format("%8s", Integer.toBinaryString(other & 0xFF)).replace(' ', '0');
        message = message + "    - langTypes2: " + String.format("%8s", Integer.toBinaryString(langTypes2 & 0xFF)).replace(' ', '0');

        LogUtils.log(Log.DEBUG, CLASS_TAG, message);

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {
            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
        }

        sendingThread.taskFinished();
    }

    private void processEndData() {

        recordDate = 0;

        int packetDurationInMinutes = 0;
        int totalPackages = 0;

        LifevitSDKBraceletData braceletData = new LifevitSDKBraceletData();

        if (stepDataArray.size() > 0) {
            braceletData.setStepsData(stepDataArray);
        }

        if (sleepDataArray.size() > 0) {
            braceletData.setSleepData(sleepDataArray);
        }

        if (heartRateDataArray.size() > 0) {
            braceletData.setHeartData(heartRateDataArray);
        }


        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {
            mLifevitSDKManager.getBraceletAT2019Listener().braceletDataReceived(braceletData);
        }

        sendingThread.taskFinished();

    }

    private void processSynchronizeHeartRateData(byte[] rx) {

        int serial = rx[2];
        int length = rx[3];

        LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> LifevitSDK:HR Data serial: " + serial + " / total: " + totalPackages);

        int i = 4;

        if (serial == 0x01) {

            heartRateDataArray.clear();
            stepDataArray.clear();
            sleepDataArray.clear();

            int header1_date_year = Utils.bytesToInt(new byte[]{0, 0, rx[i], rx[i + 1]});
            i += 2;
            int header1_date_month = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});
            int header1_date_day = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});

            int minutesOffset = Utils.bytesToInt(new byte[]{0, 0, rx[i], rx[i + 1]});
            i += 2;
            int silentHeart = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});
            int items = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});
            int totalPackets = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});
            int mxHR = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});


            LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> Headers");
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_date_year: " + header1_date_year);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_date_month: " + header1_date_month);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_date_day: " + header1_date_day);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- minutesOffset: " + minutesOffset);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- silentHeart: " + silentHeart);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- items: " + items);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- totalPackets: " + totalPackets);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- mxHR: " + mxHR);


            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, header1_date_year);
            cal.set(Calendar.MONTH, header1_date_month - 1);
            cal.set(Calendar.YEAR, header1_date_day);

            cal.set(Calendar.HOUR, minutesOffset / 60);
            cal.set(Calendar.MINUTE, minutesOffset % 60);
            cal.set(Calendar.SECOND, 0);

            recordDate = cal.getTimeInMillis();
            totalPackages = totalPackets;
            totalItems = items;

        } else if (serial == 0x02) {
            int burn_fat_threshold = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});
            int aerobic_threshold = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});
            int limit_threshold = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});
            int burn_fat_mins = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});
            int aerobic_mins = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});
            int limit_mins = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});

            LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> DATA");
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- burn_fat_threshold: " + burn_fat_threshold);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- aerobic_threshold: " + aerobic_threshold);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- limit_threshold: " + limit_threshold);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- burn_fat_mins: " + burn_fat_mins);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- aerobic_mins: " + aerobic_mins);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- limit_mins: " + limit_mins);
        } else {
            int sIndex = 0;
            while (sIndex < length && totalItems > 0) {
                int distanceToLastMeasurement = rx[i + (sIndex++)];
                int hr = rx[i + (sIndex++)];

                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(recordDate);
                cal.add(Calendar.MINUTE, distanceToLastMeasurement * 60);

                Date rDate = cal.getTime();

                recordDate = cal.getTimeInMillis();

                LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> DATA date: " + rDate + ",distanceToLastMeasurement: " + distanceToLastMeasurement + ", Heart rate: " + hr);

                LifevitSDKHeartbeatData data = new LifevitSDKHeartbeatData();
                data.setHeartrate(hr);
                data.setDate(recordDate);

                heartRateDataArray.add(data);
                totalItems--;
            }
        }
    }


    private void processSynchronizeSleepData(byte[] rx) {

        int serial = rx[2];
        int length = rx[3];

        String message = ">>> LifevitSDK:Sleep Data serial: " + serial + " / total: " + totalPackages;

        int i = 4;

        if (serial == 0x01) {

            heartRateDataArray.clear();
            stepDataArray.clear();
            sleepDataArray.clear();

            int header1_date_year = Utils.bytesToInt(new byte[]{0, 0, rx[i], rx[i + 1]});
            i += 2;
            int header1_date_month = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});
            int header1_date_day = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});

            int hour = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});
            int minutes = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});

            int totalMinutes0 = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});
            int totalMinutes1 = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});

            int totalMinutes = Utils.bytesToInt(new byte[]{0, 0, (byte) totalMinutes1, (byte) totalMinutes0});

            int sleepItems = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});
            int totalPackets = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});

            LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> Headers");
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_date_year: " + header1_date_year);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_date_month: " + header1_date_month);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_date_day: " + header1_date_day);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- hour: " + hour);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- minutes: " + minutes);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- totalMinutes: " + totalMinutes);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- sleepItems: " + sleepItems);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- totalPackets: " + totalPackets);


            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, header1_date_year);
            cal.set(Calendar.MONTH, header1_date_month - 1);
            cal.set(Calendar.YEAR, header1_date_day);

            cal.set(Calendar.HOUR, hour);
            cal.set(Calendar.MINUTE, minutes);
            cal.set(Calendar.SECOND, 0);

            recordDate = cal.getTimeInMillis();
            totalPackages = totalPackets;
            totalItems = sleepItems;

        } else if (serial == 0x02) {
            int lightSleepCount = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});
            int deepSleepCount = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});
            int awakes = Utils.bytesToInt(new byte[]{0, 0, 0, rx[i++]});

            byte totalMinutesLight0 = rx[i++];
            byte totalMinutesLight1 = rx[i++];
            int totalLightMinutes = Utils.bytesToInt(new byte[]{0, 0, totalMinutesLight1, totalMinutesLight0});

            byte totalMinutesDeep0 = rx[i++];
            byte totalMinutesDeep1 = rx[i++];
            int totalDeepMinutes = Utils.bytesToInt(new byte[]{0, 0, totalMinutesDeep1, totalMinutesDeep0});


            LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> Sleep Headers 2 :");
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- lightSleepCount: " + lightSleepCount);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- deepSleepCount: " + deepSleepCount);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- awakes: " + awakes);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- totalLightMinutes: " + totalLightMinutes);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- totalDeepMinutes: " + totalDeepMinutes);

            if ((mLifevitSDKManager.getBraceletAT2019Listener() != null) && (totalItems > 0)) {

                LifevitSDKSummarySleepData sleepData = new LifevitSDKSummarySleepData(recordDate, awakes, totalLightMinutes, totalDeepMinutes);
                mLifevitSDKManager.getBraceletAT2019Listener().braceletSummarySleepReceived(sleepData);
            }


        } else {
            int sIndex = 0;
            while (sIndex < length && totalItems > 0) {
                int status = rx[i + (sIndex++)];
                int duration = rx[i + (sIndex++)];
                totalItems--;

                LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> DATA: status: " + status + ", duration: " + duration);

                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(recordDate);
                cal.add(Calendar.MINUTE, duration * 60);

                LifevitSDKSleepData data = new LifevitSDKSleepData();

                data.setSleepDuration(duration);
                switch (status) {
                    case 1:
                        continue;

                    case 2:
                        data.setSleepDeepness(LifevitSDKConstants.LIGHT_SLEEP);
                        break;

                    case 3:
                        data.setSleepDeepness(LifevitSDKConstants.DEEP_SLEEP);
                        break;

                    default:
                        continue;

                }

                recordDate = cal.getTimeInMillis();
                data.setDate(recordDate);

                sleepDataArray.add(data);
            }
        }
    }


    private void processSynchronizeSportData(byte[] rx) {

        int serial = rx[2];
        int length = rx[3];

        String message = ">>> LifevitSDK: Sport Data serial: " + serial + " / total: " + totalPackages;

        int i = 4;

        if (serial == 0x01) {

            heartRateDataArray.clear();
            stepDataArray.clear();
            sleepDataArray.clear();

            int header1_date_year = Utils.bytesToInt(new byte[]{0, 0, rx[i], rx[i + 1]});
            int header1_date_month = rx[i + 2];
            int header1_date_day = rx[i + 3];

            int header1_offset_minutes_from_day_start = Utils.bytesToInt(new byte[]{0, 0, rx[i + 5], rx[i + 4]});
            int header1_how_many_minutes_every_package = rx[i + 6];

            int header1_sport_item = rx[i + 7];
            int header1_total_packages = rx[i + 8];

            LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> Headers");
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_date_year: " + header1_date_year);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_date_month: " + header1_date_month);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_date_day: " + header1_date_day);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_offset_minutes_from_day_start: " + header1_offset_minutes_from_day_start);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_how_many_minutes_every_package: " + header1_how_many_minutes_every_package);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_sport_item: " + header1_sport_item);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_total_packages: " + header1_total_packages);


            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, header1_date_year);
            cal.set(Calendar.MONTH, header1_date_month - 1);
            cal.set(Calendar.YEAR, header1_date_day);

            cal.set(Calendar.HOUR, header1_offset_minutes_from_day_start / 60);
            cal.set(Calendar.MINUTE, header1_offset_minutes_from_day_start % 60);
            cal.set(Calendar.SECOND, 0);

            recordDate = cal.getTimeInMillis();

            packetDurationInMinutes = header1_how_many_minutes_every_package;
            totalPackages = header1_total_packages;
            totalItems = header1_sport_item;

        } else if (serial == 0x02) {
            int header2_total_steps = Utils.bytesToInt(new byte[]{rx[i + 3], rx[i + 2], rx[i + 1], rx[i]});
            int header2_total_calories = Utils.bytesToInt(new byte[]{rx[i + 7], rx[i + 6], rx[i + 5], rx[i + 4]});
            int header2_total_distance = Utils.bytesToInt(new byte[]{rx[i + 11], rx[i + 10], rx[i + 9], rx[i + 8]});
            int header2_total_active_time = Utils.bytesToInt(new byte[]{rx[i + 15], rx[i + 14], rx[i + 13], rx[i + 12]});


            LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> DATA:");
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- DATA_total_steps: " + header2_total_steps);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- DATA_total_calories: " + header2_total_calories);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- DATA_total_distance: " + header2_total_distance);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- DATA_total_active_time: " + header2_total_active_time);


            if ((mLifevitSDKManager.getBraceletAT2019Listener() != null) && (totalItems > 0)) {

                LifevitSDKSummaryStepData stepData = new LifevitSDKSummaryStepData(recordDate, header2_total_steps, header2_total_calories, header2_total_distance, header2_total_active_time, 0);
                mLifevitSDKManager.getBraceletAT2019Listener().braceletSummaryStepsReceived(stepData);
            }


        } else {
            int sIndex = 0;
            while (sIndex < length) {

                byte b0 = rx[i + (sIndex++)];
                byte b1 = rx[i + (sIndex++)];
                byte b2 = rx[i + (sIndex++)];
                byte b3 = rx[i + (sIndex++)];
                byte b4 = rx[i + (sIndex++)];

                int mode = b0 & 0x03;
                int steps = ((b0 & 0xFC) >> 2) + ((b1 & 0x3F) << 6);
                int activeTime = ((b1 & 0xC0) >> 6) + ((b2 & 0x03) << 2);
                int calories = ((b2 & 0xFC) >> 2) + ((b3 & 0x0F) << 6);
                int distance = ((b3 & 0xF0) >> 4) + ((b4 & 0xFF) << 4);


                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(recordDate);
                cal.add(Calendar.MINUTE, ((packetDurationInMinutes * 60) * stepDataArray.size()));

                //recordDate = cal.getTimeInMillis() / 1000;
                ///data.setDate(recordDate);

                LifevitSDKStepData data = new LifevitSDKStepData(cal.getTimeInMillis(), steps, calories, distance);

                if (steps != 0 && calories != 0 && distance != 0) {
                    stepDataArray.add(data);
                }

                LogUtils.log(Log.DEBUG, CLASS_TAG, "    === Current === "
                        + "rDate: " + cal.getTimeInMillis()
                        + ", steps: " + steps
                        + ", calories: " + calories
                        + ", distance: " + distance);
            }
        }

    }


    private void processGetDeviceDatetime(byte[] rx) {


        int header1_date_year = Utils.bytesToInt(new byte[]{0, 0, rx[2], rx[3]});
        int header1_date_month = rx[4];
        int header1_date_day = rx[5];

        int header1_date_hour = rx[6];
        int header1_date_minute = rx[7];
        int header1_date_second = rx[8];
        int header1_date_weekday = rx[9];

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, header1_date_year);
        cal.set(Calendar.MONTH, header1_date_month - 1);
        cal.set(Calendar.DAY_OF_MONTH, header1_date_day);
        cal.set(Calendar.HOUR_OF_DAY, header1_date_hour);
        cal.set(Calendar.MINUTE, header1_date_minute);
        cal.set(Calendar.SECOND, header1_date_second);
        //cal.set(Calendar.DAY_OF_WEEK, header1_date_weekday);

        /*
        TimeZone timeZone;
        timeZone = TimeZone.getTimeZone("GMT+0:00");
        TimeZone.setDefault(timeZone);

        cal.setTimeZone(timeZone);
         */

        Long date = cal.getTimeInMillis();
/*
        Date dateD = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(dateD);
*/
        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {
            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(date);
        }

        sendingThread.taskFinished();

    }

    private void processGetBatteryLevel(byte[] rx) {

        int battery = rx[6];

        LogUtils.log(Log.DEBUG, CLASS_TAG, "Battery level: " + battery);

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {
            mLifevitSDKManager.getBraceletAT2019Listener().braceletCurrentBattery(battery);
        }

        sendingThread.taskFinished();
    }

    private void processGetMAC(byte[] rx) {

        sendingThread.taskFinished();
    }

    private void processSerialNumber(byte[] rx) {

        byte sn0 = rx[6];
        byte sn1 = rx[7];
        byte sn2 = rx[8];
        byte sn3 = rx[9];

        LogUtils.log(Log.DEBUG, CLASS_TAG, "Serial number");

        int serialNumberString = Utils.bytesToInt(new byte[]{sn3, sn2, sn1, sn0});
        String str1 = Integer.toString(serialNumberString);

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {
            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(str1);
        }

    }

    private void processNotificationStatus(byte[] rx) {

        byte notify_switch = rx[2];
        byte notify_item1 = rx[3];
        byte notify_item2 = rx[4];
        byte call_switch = rx[5];
        byte call_delay = rx[6];


        LogUtils.log(Log.DEBUG, CLASS_TAG, "Notification status ");

        String message = ">>> Received get feature list. Data received:";
        message = message + "    - notify_switch: " + String.format("%8s", Integer.toBinaryString(notify_switch & 0xFF)).replace(' ', '0');
        message = message + "    - notify_item1: " + String.format("%8s", Integer.toBinaryString(notify_item1 & 0xFF)).replace(' ', '0');
        message = message + "    - notify_item2: " + String.format("%8s", Integer.toBinaryString(notify_item2 & 0xFF)).replace(' ', '0');
        message = message + "    - call_switch: " + String.format("%8s", Integer.toBinaryString(call_switch & 0xFF)).replace(' ', '0');
        message = message + "    - call_delay: " + String.format("%8s", Integer.toBinaryString(call_delay & 0xFF)).replace(' ', '0');


        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {
            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
        }

    }

    private void processCurrentDayData(byte[] rx) {

        int totalSteps = Utils.bytesToInt(new byte[]{rx[5], rx[4], rx[3], rx[2]});
        int totalCalories = Utils.bytesToInt(new byte[]{rx[9], rx[8], rx[7], rx[6]});
        int totalDistance = Utils.bytesToInt(new byte[]{rx[13], rx[12], rx[11], rx[10]});
        int totalActiveTime = Utils.bytesToInt(new byte[]{rx[17], rx[16], rx[15], rx[14]});
        int heartRate = rx[18];


        LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> processCurrentDayData:");
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - totalSteps: " + totalSteps);
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - totalCalories: " + totalCalories);
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - totalDistance: " + totalDistance);
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - totalActiveTime: " + totalActiveTime);
        LogUtils.log(Log.DEBUG, CLASS_TAG, "    - heartRate: " + heartRate);

        Long date = System.currentTimeMillis() / 1000;

        LifevitSDKSummaryStepData data = new LifevitSDKSummaryStepData(date, totalSteps, totalCalories, totalDistance, totalActiveTime, heartRate);

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {
            mLifevitSDKManager.getBraceletAT2019Listener().braceletSummaryStepsReceived(data);
        }

        sendingThread.taskFinished();

    }

    private void processSetDeviceDatetime(byte[] rx) {
        String message = "Datetime updated";

        LogUtils.log(Log.DEBUG, CLASS_TAG, message);

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {
            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
        }

        sendingThread.taskFinished();
    }

    private void processConfigureAlarm(byte[] rx) {

        int success = rx[2];

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {

            String message;

            switch (success) {
                case 0:
                    message = "Succeed configuring alarm";
                    break;

                case 1:
                    message = "Error configuring alarm: fail more than maxium value";
                    break;

                case 2:
                    message = "Error configuring alarm: fail-id was occupier";
                    break;

                case 3:
                    message = "Error configuring alarm: fail-id is invalid";
                    break;

                default:
                    message = "Unknown error";
                    break;
            }


            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
        }

        sendingThread.taskFinished();


    }

    private void processStepTargetAndSleepTime(byte[] rx) {

        int success = rx[2];

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {

            String message;

            switch (success) {
                case 0:
                    message = "Succeed setting target and sleep time";
                    break;

                case 1:
                    message = "Error setting target and sleep time: fail-type do not supported";
                    break;

                default:
                    message = "Unknown error";
                    break;
            }


            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
        }

        sendingThread.taskFinished();

    }

    private void processSetUserInformation(byte[] rx) {

        int success = rx[2];

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {

            String message;

            switch (success) {
                case 0:
                    message = "Succeed setting user information";
                    break;

                case 1:
                    message = "Error setting user information: fail-incorrect parameter";
                    break;

                default:
                    message = "Unknown error";
                    break;
            }


            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
        }

        sendingThread.taskFinished();
    }


    private void processConfigureSedentaryAlarm(byte[] rx) {
        int success = rx[2];

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {

            String message;

            switch (success) {
                case 0:
                    message = "Succeed configuring sedentary alarm";
                    break;

                case 1:
                    message = "Error configuring sedentary alarm: fail-wrong parameter";
                    break;

                default:
                    message = "Unknown error";
                    break;
            }


            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
        }

        sendingThread.taskFinished();
    }


    private void processConfigureAntiTheft(byte[] rx) {

        int success = rx[2];

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {

            String message;

            switch (success) {
                case 0:
                    message = "Succeed configuring antitheft";
                    break;

                case 1:
                    message = "Error configuring antitheft: fail-not support";
                    break;

                default:
                    message = "Unknown error";
                    break;
            }


            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
        }

        sendingThread.taskFinished();

    }


    private void processRiseHand(byte[] rx) {

        int success = rx[2];

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {

            String message;

            switch (success) {
                case 0:
                    message = "Succeed setting hand";
                    break;

                default:
                    message = "Unknown error";
                    break;
            }


            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
        }

        sendingThread.taskFinished();
    }

    private void processiOSPhone(byte[] rx) {

        int success = rx[2];

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {

            String message;

            switch (success) {
                case 0:
                    message = "Succeed setting iOS Phone";
                    break;

                default:
                    message = "Unknown error";
                    break;
            }


            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
        }

        sendingThread.taskFinished();

    }

    private void processHeartRateIntervalSetting(byte[] rx) {

        int success = rx[2];

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {

            String message;

            switch (success) {
                case 0:
                    message = "Succeed setting heart rate intervals";
                    break;

                case 1:
                    message = "Error setting heart rate intervals";
                    break;


                default:
                    message = "Unknown error";
                    break;
            }


            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
        }

        sendingThread.taskFinished();

    }

    private void processHeartRateMonitoring(byte[] rx) {

        int success = rx[2];

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {

            String message;

            switch (success) {
                case 0:
                    message = "Succeed setting heart rate monitoring";
                    break;

                case 1:
                    message = "Error setting heart rate monitoring";
                    break;


                default:
                    message = "Unknown error";
                    break;
            }


            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
        }

        sendingThread.taskFinished();

    }

    private void processFindPhone(byte[] rx) {

        int success = rx[2];

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {

            String message;

            switch (success) {
                case 0:
                    message = "Succeed setting find phone functionality";
                    break;

                case 1:
                    message = "Error setting find phone functionality";
                    break;


                default:
                    message = "Unknown error";
                    break;
            }


            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
        }

        sendingThread.taskFinished();

    }

    private void processConfigureReminder(byte[] rx) {

        int success = rx[2];

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {

            String message;

            switch (success) {
                case 0:
                    message = "Error setting notification reminder: unknown timeout";
                    mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
                    break;

                case 1:
                    message = "Succeed setting notification reminder";
                    mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
                    break;

                case 2:
                    message = "Error setting notification reminder: fail";
                    mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
                    break;

                case -120:
                    sendConfigureACNSActivate();
                    break;

                default:
                    message = "Unknown error";
                    mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
                    break;
            }

        }

        sendingThread.taskFinished();

    }

    private void processSleepMonitoring(byte[] rx) {

        int success = rx[2];

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {

            String message;

            switch (success) {
                case 0:
                    message = "Succeed setting sleep monitoring";
                    break;

                case 1:
                    message = "Error setting sleep monitoring: fail";
                    break;

                case 2:
                    message = "Error setting sleep monitoring: fail - parameter error";
                    break;

                default:
                    message = "Unknown error";
                    break;
            }


            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
        }

        sendingThread.taskFinished();
    }


    protected void characteristicReadProcessData(BluetoothGattCharacteristic characteristic) {

        LogUtils.log(Log.DEBUG, CLASS_TAG, "RECEIVED from characteristic: " + characteristic.getUuid().toString());

        if (characteristic.getUuid().equals(UUID.fromString(UUID_NOTIFY_CHARACTERISTIC_READ))) {

            byte[] rx = characteristic.getValue();

            String resultsStr = new String(rx);

            LogUtils.log(Log.DEBUG, CLASS_TAG, "characteristicReadProcessData - RECEIVED (byte format): " + HexUtils.getStringToPrint(rx));
            LogUtils.log(Log.DEBUG, CLASS_TAG, "characteristicReadProcessData - RECEIVED (string format): " + resultsStr);

            if (rx[0] == 0x02 && rx[1] == 0x01) {

                processBasicInfo(rx);

            } else if (rx[0] == 0x02 && rx[1] == 0x02) {

                processGetFeatureList(rx);

            } else if (rx[0] == 0x02 && rx[1] == 0x07) {

                processGetExtendedFeatureList(rx);

            } else if (rx[0] == 0x02 && rx[1] == 0x03) {

                processGetDeviceDatetime(rx);

            } else if (rx[0] == 0x02 && rx[1] == 0x05) {

                processGetBatteryLevel(rx);

            } else if (rx[0] == 0x02 && rx[1] == 0x04) {

                processGetMAC(rx);

            } else if (rx[0] == 0x02 && rx[1] == 0x06) {

                processSerialNumber(rx);

            } else if (rx[0] == 0x02 && rx[1] == 0x10) {

                processNotificationStatus(rx);

            } else if (rx[0] == 0x02 && rx[1] == (byte) 0xA0) {

                processCurrentDayData(rx);

            } else if (rx[0] == 0x03 && rx[1] == 0x01) {

                processSetDeviceDatetime(rx);

            } else if (rx[0] == 0x03 && rx[1] == 0x02) {

                processConfigureAlarm(rx);

            } else if (rx[0] == 0x03 && rx[1] == 0x03) {

                processStepTargetAndSleepTime(rx);

            } else if (rx[0] == 0x03 && rx[1] == 0x10) {

                processSetUserInformation(rx);

            } else if (rx[0] == 0x03 && rx[1] == 0x20) {

                processConfigureSedentaryAlarm(rx);

            } else if (rx[0] == 0x03 && rx[1] == 0x21) {

                processConfigureAntiTheft(rx);

            } else if (rx[0] == 0x03 && rx[1] == 0x22) {

                processRiseHand(rx);

            } else if (rx[0] == 0x03 && rx[1] == 0x23) {

                processiOSPhone(rx);

            } else if (rx[0] == 0x03 && rx[1] == 0x24) {

                processHeartRateIntervalSetting(rx);

            } else if (rx[0] == 0x03 && rx[1] == 0x25) {

                processHeartRateMonitoring(rx);

            } else if (rx[0] == 0x02 && rx[1] == 0x26) {

                processFindPhone(rx);

            } else if (rx[0] == 0x02 && rx[1] == 0x30) {

                processConfigureReminder(rx);

            } else if (rx[0] == 0x03 && rx[1] == 0x31) {

                processSleepMonitoring(rx);

            } else if (rx[0] == 0x08 && rx[1] == (byte) 0xEE) {

                processEndData();

            } else {

                LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> WRONG COMMAND");
                sendingThread.taskFinished();
            }


            /*

            else if (rx[0] == 0x02 && rx[1] == 0x03) {

                int year = Utils.bytesToInt(new byte[]{0, 0, rx[2], rx[3]});
                int month = rx[4];
                int day = rx[5];
                int hour = rx[6];
                int minute = rx[7];
                int second = rx[8];
                int weekday = rx[9];

                LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> Received get time. Date received: " + day + "/" + month + "/" + year
                        + ", " + hour + ":" + minute + ":" + second);

                sendingThread.taskFinished();

            } else if (rx[0] == 0x02 && rx[1] == (byte) 0xA0) {

                int totalSteps = Utils.bytesToInt(new byte[]{rx[5], rx[4], rx[3], rx[2]});
                int totalCalories = Utils.bytesToInt(new byte[]{rx[9], rx[8], rx[7], rx[6]});
                int totalDistance = Utils.bytesToInt(new byte[]{rx[13], rx[12], rx[11], rx[10]});
                int totalActiveTime = Utils.bytesToInt(new byte[]{rx[17], rx[16], rx[15], rx[14]});
                int heartRate = rx[18];
                int invalid = -1;

                try {
                    if (rx.length >= 20) {
                        invalid = rx[19];
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> Received Sync data. Data received:");
                LogUtils.log(Log.DEBUG, CLASS_TAG, "    - totalSteps: " + totalSteps);
                LogUtils.log(Log.DEBUG, CLASS_TAG, "    - totalCalories: " + totalCalories);
                LogUtils.log(Log.DEBUG, CLASS_TAG, "    - totalDistance: " + totalDistance);
                LogUtils.log(Log.DEBUG, CLASS_TAG, "    - totalActiveTime: " + totalActiveTime);
                LogUtils.log(Log.DEBUG, CLASS_TAG, "    - heartRate: " + heartRate);
                LogUtils.log(Log.DEBUG, CLASS_TAG, "    - invalid: " + invalid);

                sendingThread.taskFinished();

            } else if (rx[0] == 0x03 && rx[1] == 0x01) {

                LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> Received Set Time!");

                sendingThread.taskFinished();

            } else if (rx[0] == 0x04 && rx[1] == 0x01) {

                int status = rx[2];
                int bindingCodeLength = rx[3];

                LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> Received Bind:");
                LogUtils.log(Log.DEBUG, CLASS_TAG, "    - status: " + status);
                LogUtils.log(Log.DEBUG, CLASS_TAG, "    - bindingCodeLength: " + bindingCodeLength);

            } else if (rx[0] == 0x04 && rx[1] == 0x02) {

                int status = rx[2];

                LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> Received UNBind:");
                LogUtils.log(Log.DEBUG, CLASS_TAG, "    - status: " + status);
            }


            */

        } else if (characteristic.getUuid().equals(UUID.fromString(UUID_NOTIFY_CHARACTERISTIC_READ_2))) {

            byte[] rx = characteristic.getValue();

            String resultsStr = new String(rx);

            LogUtils.log(Log.DEBUG, CLASS_TAG, "characteristicReadProcessData(2) - RECEIVED (byte format): " + HexUtils.getStringToPrint(rx));
            LogUtils.log(Log.DEBUG, CLASS_TAG, "characteristicReadProcessData(2) - RECEIVED (string format): " + resultsStr);


            if (rx[0] == 0x08 && rx[1] == 0x01) {

                int totalPackets = Utils.bytesToInt(new byte[]{0, 0, rx[3], rx[2]});
                int activeDays = rx[4];
                int sleepDays = rx[5];
                int heartDays = rx[6];

                LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> Data received:");
                LogUtils.log(Log.DEBUG, CLASS_TAG, "    - totalPackets: " + totalPackets);
                LogUtils.log(Log.DEBUG, CLASS_TAG, "    - activeDays: " + activeDays);
                LogUtils.log(Log.DEBUG, CLASS_TAG, "    - sleepDays: " + sleepDays);
                LogUtils.log(Log.DEBUG, CLASS_TAG, "    - heartDays: " + heartDays);

                sendingThread.taskFinished();

            } else if ((rx[0] == 0x08 && rx[1] == 0x03) || (rx[0] == 0x08 && rx[1] == 0x05)) {

                processSynchronizeSportData(rx);

            } else if ((rx[0] == 0x08 && rx[1] == 0x04) || (rx[0] == 0x08 && rx[1] == 0x06)) {

                processSynchronizeSleepData(rx);

            } else if ((rx[0] == 0x08 && rx[1] == 0x07) || (rx[0] == 0x08 && rx[1] == 0x08)) {

                processSynchronizeHeartRateData(rx);

            } else if (rx[0] == 0x08 && rx[1] == (byte) 0xEE) {

                processEndData();

            } else {

                LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> NO CASE BRACELET AT2019");
                sendingThread.taskFinished();

            }

            /*

            if (rx[0] == 0x08 && rx[1] == 0x01) {

                int totalPackets = Utils.bytesToInt(new byte[]{0, 0, rx[3], rx[2]});
                int activeDays = rx[4];
                int sleepDays = rx[5];
                int heartDays = rx[6];

                LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> Data received:");
                LogUtils.log(Log.DEBUG, CLASS_TAG, "    - totalPackets: " + totalPackets);
                LogUtils.log(Log.DEBUG, CLASS_TAG, "    - activeDays: " + activeDays);
                LogUtils.log(Log.DEBUG, CLASS_TAG, "    - sleepDays: " + sleepDays);
                LogUtils.log(Log.DEBUG, CLASS_TAG, "    - heartDays: " + heartDays);

                sendingThread.taskFinished();

            } else if (rx[0] == 0x08 && rx[1] == 0x02) {

                LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> Received finish synchronization!");

                sendingThread.taskFinished();

            } else if (rx[0] == 0x08 && rx[1] == (byte) 0xEE) {

                LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> Finished Receiving Sports data.");

                sendStepsData();
                resetSportsData();

                sendingThread.taskFinished();

            } else if (rx[0] == 0x08 && rx[1] == 0x03) {

                int serial = rx[2];
                int length = rx[3];

                LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> Received Sports data:");
                LogUtils.log(Log.DEBUG, CLASS_TAG, "    - serial: " + serial);
                LogUtils.log(Log.DEBUG, CLASS_TAG, "    - length: " + length);

                int i = 4;

                // 08:03:01:10:E3:07:08:09:00:00:0F:60:22:00
                // 08:03:02:10:62:00:00:00:04:00:00:00:45:00:00:00:60:00:00:00
                // 08:03:03:0F:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00
                // 08:03:22:0F:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00
                // 08:EE:00:00:00:00

                if (serial == 0x01) {

                    // Is header

                    // 08:03:01:10:E3:07:08:09:00:00:0F:60:22:00

                    int header1_date_year = Utils.bytesToInt(new byte[]{0, 0, rx[i + 1], rx[i]});
                    int header1_date_month = rx[i + 2];
                    int header1_date_day = rx[i + 3];

                    int header1_offset_minutes_from_day_start = Utils.bytesToInt(new byte[]{0, 0, rx[i + 4], rx[i + 5]});
                    int header1_how_many_minutes_every_package = rx[i + 6];
                    int header1_sport_item = rx[i + 7];
                    int header1_total_packages = rx[i + 8];

                    LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> Headers:");
                    LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_date_year: " + header1_date_year);
                    LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_date_month: " + header1_date_month);
                    LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_date_day: " + header1_date_day);
                    LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_offset_minutes_from_day_start: " + header1_offset_minutes_from_day_start);
                    LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_how_many_minutes_every_package: " + header1_how_many_minutes_every_package);
                    LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_sport_item: " + header1_sport_item);
                    LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_total_packages: " + header1_total_packages);


                    sportsDataPacketDate = Calendar.getInstance();
                    sportsDataPacketDate.set(Calendar.YEAR, header1_date_year);
                    sportsDataPacketDate.set(Calendar.MONTH, header1_date_month - 1);
                    sportsDataPacketDate.set(Calendar.YEAR, header1_date_day);

                    sportsDataPacketDate.set(Calendar.HOUR, header1_offset_minutes_from_day_start / 60);
                    sportsDataPacketDate.set(Calendar.MINUTE, header1_offset_minutes_from_day_start % 60);

                    sportsDataPacketDuration = header1_how_many_minutes_every_package;
                    sportsDataTotalPackages = header1_total_packages;

                } else {

                    // Is normal packet

                    // 08:03:02:10 : 62:00:00:00 : 04:00:00:00 : 45:00:00:00 : 60:00:00:00

                    int header2_total_steps = Utils.bytesToInt(new byte[]{rx[i + 3], rx[i + 2], rx[i + 1], rx[i]});
                    int header2_total_calories = Utils.bytesToInt(new byte[]{rx[i + 7], rx[i + 6], rx[i + 5], rx[i + 4]});
                    int header2_total_distance = Utils.bytesToInt(new byte[]{rx[i + 11], rx[i + 10], rx[i + 9], rx[i + 8]});
                    int header2_total_active_time = Utils.bytesToInt(new byte[]{rx[i + 15], rx[i + 14], rx[i + 13], rx[i + 12]});

                    LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> Headers:");
                    LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header2_total_steps: " + header2_total_steps);
                    LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header2_total_calories: " + header2_total_calories);
                    LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header2_total_distance: " + header2_total_distance);
                    LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header2_total_active_time: " + header2_total_active_time);

                    long packetDate = sportsDataPacketDate.getTimeInMillis() + sportsDataPacketDuration * (serial - 2);

                    addToSportsData(header2_total_steps, header2_total_calories, header2_total_distance, packetDate);

                }
            }

             */
        }
    }


    /*********************************************************************************************/
    /*****************************  Methods to send info to activity  ****************************/
    /*********************************************************************************************/

/*
    private void addToSportsData(int steps, float calories, float distance, long date) {

        // create new record
        LifevitSDKStepData record = new LifevitSDKStepData(date, steps, calories, distance);

        sportsDataList.add(record);
    }


    private void sendStepsData() {
        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {
            mLifevitSDKManager.getBraceletAT2019Listener().braceletStepsReceived(sportsDataList);
        }
    }


    private void resetSportsData() {

        sportsDataPacketDate = null;
        sportsDataPacketDuration = 0;
        sportsDataTotalPackages = 0;
        sportsDataList = new ArrayList<>();
    }
*/

    /*********************************************************************************************/
    /**************************  Methods to send instruction to device  **************************/
    /*********************************************************************************************/


    byte[] getEmptyArray() {
        byte[] syncparamsArray = new byte[20];

        syncparamsArray[0] = (byte) 0;
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
        syncparamsArray[15] = (byte) 0;
        syncparamsArray[16] = (byte) 0;
        syncparamsArray[17] = (byte) 0;
        syncparamsArray[18] = (byte) 0;
        syncparamsArray[19] = (byte) 0;

        return syncparamsArray;
    }


    protected void sendEndSynchronizeData() {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x08;
        syncparamsArray[1] = (byte) 0x02;

        // Safe mode: (0x01: safe mode, other values: normal mode)
        syncparamsArray[2] = (byte) 0x01;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendEndSynchronizeData] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray, 2);
    }


    protected void sendGetBasicInfo() {

        byte[] syncparamsArray = getEmptyArray();


        syncparamsArray[0] = (byte) 0x02;
        syncparamsArray[1] = (byte) 0x01;


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send Get basic info] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendGetFeatureList() {

        byte[] syncparamsArray = getEmptyArray();


        syncparamsArray[0] = (byte) 0x02;
        syncparamsArray[1] = (byte) 0x02;


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send Get feature list] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendGetExtendedFeatures() {

        byte[] syncparamsArray = getEmptyArray();


        syncparamsArray[0] = (byte) 0x02;
        syncparamsArray[1] = (byte) 0x07;


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send Extended features] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }

/*
    protected void sendGetRealTimeData(boolean forceBloodPressure) {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x02;
        syncparamsArray[1] = (byte) 0xA0;

        if (forceBloodPressure) {
            syncparamsArray[2] = (byte) 0x01;
        } else {
            syncparamsArray[2] = (byte) 0x00;
        }

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendGetRealTimeData] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }
*/

    protected void sendSynchronizeSportsData(int mode) {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x08;
        syncparamsArray[1] = (byte) 0x03;

        // Status (0x01: start, 0x02: end, 0x03: resend)
        syncparamsArray[2] = (byte) mode;

        // The serial number of the package (1~255), valid only when resending
        syncparamsArray[3] = (byte) 0x01;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSynchronizeSportsData] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray, 2);
    }

    protected void sendSynchronizeSleepData(int mode) {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x08;
        syncparamsArray[1] = (byte) 0x04;

        // Status (0x01: start, 0x02: end, 0x03: resend)
        syncparamsArray[2] = (byte) mode;

        // The serial number of the package (1~255), valid only when resending
        syncparamsArray[3] = (byte) 0x01;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSynchronizeSleepData] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray, 2);
    }


    protected void sendSynchronizeHeartRateData(int mode) {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x08;
        syncparamsArray[1] = (byte) 0x07;

        // Status (0x01: start, 0x02: end, 0x03: resend)
        syncparamsArray[2] = (byte) mode;

        // The serial number of the package (1~255), valid only when resending
        syncparamsArray[3] = (byte) 0x01;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSynchronizeSleepData] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray, 2);
    }


    protected void sendSetTime(Long time) {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x03;
        syncparamsArray[1] = (byte) 0x01;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time * 1000);

        int currentYear = cal.get(Calendar.YEAR);
        byte lowerByte = (byte) (currentYear & 0xFF);
        byte upperByte = (byte) ((currentYear >> 8) & 0xFF);

        syncparamsArray[2] = upperByte;
        syncparamsArray[3] = lowerByte;

        syncparamsArray[4] = (byte) (cal.get(Calendar.MONTH) + 1);
        syncparamsArray[5] = (byte) cal.get(Calendar.DAY_OF_MONTH);
        syncparamsArray[6] = (byte) cal.get(Calendar.HOUR_OF_DAY);
        syncparamsArray[7] = (byte) cal.get(Calendar.MINUTE);
        syncparamsArray[8] = (byte) cal.get(Calendar.SECOND);
        syncparamsArray[9] = (byte) (cal.get(Calendar.DAY_OF_WEEK) - 2);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSetTime] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendGetDeviceTime() {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x02;
        syncparamsArray[1] = (byte) 0x03;


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send Get device time] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }

    protected void sendGetMAC() {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x02;
        syncparamsArray[1] = (byte) 0x04;


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send Get MAC] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendGetBattery() {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x02;
        syncparamsArray[1] = (byte) 0x05;


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send Get battery] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendGetSerialNumber() {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x02;
        syncparamsArray[1] = (byte) 0x06;


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send Serial number] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendSynchronizeHistoricSportData() {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x08;
        syncparamsArray[1] = (byte) 0x05;
        syncparamsArray[2] = (byte) 0x01;
        syncparamsArray[3] = (byte) 0x01;


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send sendSynchronizeHistoricSportData] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray, 2);
    }

    protected void sendSynchronizeHistoricSleepData() {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x08;
        syncparamsArray[1] = (byte) 0x06;
        syncparamsArray[2] = (byte) 0x01;
        syncparamsArray[3] = (byte) 0x01;


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send sendSynchronizeHistoricSleepData] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray, 2);
    }

    protected void sendSynchronizeHistoricHeartRateData() {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x08;
        syncparamsArray[1] = (byte) 0x08;
        syncparamsArray[2] = (byte) 0x01;
        syncparamsArray[3] = (byte) 0x01;


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send sendSynchronizeHistoricHeartRateData] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray, 2);
    }


    protected void sendConfigureAlarm(LifevitSDKAlarmTime alarm) {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x03;
        syncparamsArray[1] = (byte) 0x02;

        if (alarm.isSecondaryAlarm()) {
            syncparamsArray[2] = (byte) 0x02; //alarm (alarm number)
        } else {
            syncparamsArray[2] = (byte) 0x01; //alarm (alarm number)
        }


        syncparamsArray[3] = (byte) 0x55; //show status (show)
        syncparamsArray[4] = (byte) 0x00; //wake up (type)
        syncparamsArray[5] = (byte) alarm.getHour();
        syncparamsArray[6] = (byte) alarm.getMinute();


        String weekDays = "0";
        weekDays += alarm.isMonday() ? "1" : "0";
        weekDays += alarm.isTuesday() ? "1" : "0";
        weekDays += alarm.isWednesday() ? "1" : "0";
        weekDays += alarm.isThursday() ? "1" : "0";
        weekDays += alarm.isFriday() ? "1" : "0";
        weekDays += alarm.isSaturday() ? "1" : "0";
        weekDays += alarm.isSunday() ? "1" : "0";


        syncparamsArray[7] = (byte) Integer.parseInt(weekDays, 2);
        syncparamsArray[8] = (byte) 0x00;


        // Send command

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send configureAlarm] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendGetNotificationStatus() {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x02;
        syncparamsArray[1] = (byte) 0x10;


        // Send command

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send getNotificationStatus] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray, 2);
    }


    protected void sendBind() {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x04;
        syncparamsArray[1] = (byte) 0x01;

        // system：（0x01:ios，0x02:android）
        syncparamsArray[2] = (byte) 0x02;

        // TODO System version number
        syncparamsArray[3] = (byte) 8;

        // 0x01:clear， other：invaild
        syncparamsArray[4] = (byte) 0;


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendBind] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendSetGoals(int steps, LifevitSDKAlarmTime sleepAlarm) {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x03;
        syncparamsArray[1] = (byte) 0x03;

        syncparamsArray[2] = (byte) 0x00; //Steps goal


        syncparamsArray[3] = (byte) (steps & 0xFF);
        syncparamsArray[4] = (byte) ((steps << 8) & 0xFF);
        syncparamsArray[5] = (byte) ((steps << 16) & 0xFF);
        syncparamsArray[6] = (byte) ((steps << 24) & 0xFF);

        syncparamsArray[7] = (byte) sleepAlarm.getHour();
        syncparamsArray[8] = (byte) sleepAlarm.getMinute();


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[setGoals] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendSetUserInformation(int userHeight, int userWeight, int userGender, long userBirthday) {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x03;
        syncparamsArray[1] = (byte) 0x10;

        syncparamsArray[2] = (byte) userHeight;

        syncparamsArray[3] = (byte) (userWeight * 100 & 0xFF);
        syncparamsArray[4] = (byte) ((userWeight * 100 << 8) & 0xFF);

        syncparamsArray[5] = (byte) userGender;

        Date d = new Date(userBirthday * 1000);

        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        syncparamsArray[6] = (byte) ((cal.get(Calendar.YEAR)) & 0xFF);
        syncparamsArray[7] = (byte) ((cal.get(Calendar.YEAR) >> 8) & 0xFF);

        syncparamsArray[8] = (byte) cal.get(Calendar.MONTH);
        syncparamsArray[9] = (byte) cal.get(Calendar.DAY_OF_MONTH);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[setUserInformation] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendConfigureBraceletSedentaryAlarm(LifevitSDKMonitoringAlarm alarm) {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x03;
        syncparamsArray[1] = (byte) 0x20;

        syncparamsArray[2] = (byte) alarm.getStartHour();
        syncparamsArray[3] = (byte) alarm.getStartMinute();
        syncparamsArray[4] = (byte) alarm.getEndHour();
        syncparamsArray[5] = (byte) alarm.getEndMinute();

        switch (alarm.getIntervalCode()) {
            case PERIOD_60_MIN:
                syncparamsArray[6] = (byte) 60;
                break;

            case PERIOD_90_MIN:
                syncparamsArray[6] = (byte) 90;
                break;

            case PERIOD_120_MIN:
                syncparamsArray[6] = (byte) 120;
                break;

            default:
                syncparamsArray[6] = (byte) 30;
                break;
        }


        String weekDays = "0";
        weekDays += alarm.isMonday() ? "1" : "0";
        weekDays += alarm.isTuesday() ? "1" : "0";
        weekDays += alarm.isWednesday() ? "1" : "0";
        weekDays += alarm.isThursday() ? "1" : "0";
        weekDays += alarm.isFriday() ? "1" : "0";
        weekDays += alarm.isSaturday() ? "1" : "0";
        weekDays += alarm.isSunday() ? "1" : "0";


        syncparamsArray[7] = (byte) Integer.parseInt(weekDays, 2);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[configureBraceletSedentaryAlarm] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendConfigureAntitheft(Boolean enabled) {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x03;
        syncparamsArray[1] = (byte) 0x21;

        if (enabled) {
            syncparamsArray[2] = (byte) 0x01;
        } else {
            syncparamsArray[2] = (byte) 0x00;
        }


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[configureAntitheft] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendConfigureRiseHand(Boolean leftHand) {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x03;
        syncparamsArray[1] = (byte) 0x22;

        if (leftHand) {
            syncparamsArray[2] = (byte) 0x00;
        } else {
            syncparamsArray[2] = (byte) 0x01;
        }


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[configureRiseHand] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendConfigureAndroidPhone() {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x03;
        syncparamsArray[1] = (byte) 0x23;

        syncparamsArray[2] = (byte) 0x02;

        int iVersionInt = Build.VERSION.SDK_INT;
        syncparamsArray[3] = (byte) iVersionInt;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[configureAndroidPhone] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendConfigureHeartRateIntervalSetting(int fatBurningThreshold, int aerobicThreshold, int limitThreshold, int userMaxHR) {

        if (!((fatBurningThreshold < aerobicThreshold) && (aerobicThreshold < limitThreshold))) {

            if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {
                mLifevitSDKManager.getBraceletAT2019Listener().braceletError(LifevitSDKConstants.CODE_WRONG_PARAMETERS);
            }
            sendingThread.taskFinished();
        }

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x03;
        syncparamsArray[1] = (byte) 0x24;

        syncparamsArray[2] = (byte) fatBurningThreshold;
        syncparamsArray[3] = (byte) aerobicThreshold;
        syncparamsArray[4] = (byte) limitThreshold;
        syncparamsArray[5] = (byte) userMaxHR;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[configureHeartRateIntervalSetting] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendConfigureHeartRateMonitoring(Boolean enabled, Boolean timeIntervalEnabled, LifevitSDKSedentaryAlarm monitoringTime) {

        if (enabled && (monitoringTime == null)) {

            if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {
                mLifevitSDKManager.getBraceletAT2019Listener().braceletError(LifevitSDKConstants.CODE_WRONG_PARAMETERS);
            }
            sendingThread.taskFinished();
        }

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x03;
        syncparamsArray[1] = (byte) 0x25;

        if (enabled) {
            syncparamsArray[2] = (byte) 0x88;
        } else {
            syncparamsArray[2] = (byte) 0x55;
        }

        if (timeIntervalEnabled) {
            syncparamsArray[3] = (byte) 0x01;
        } else {
            syncparamsArray[3] = (byte) 0x00;
        }

        syncparamsArray[4] = (byte) monitoringTime.getStartHour();
        syncparamsArray[5] = (byte) monitoringTime.getStartMinute();
        syncparamsArray[6] = (byte) monitoringTime.getEndHour();
        syncparamsArray[7] = (byte) monitoringTime.getEndMinute();


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[configureHeartRateMonitoring] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendConfigureFindPhone(Boolean searchEnabled) {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x03;
        syncparamsArray[1] = (byte) 0x26;

        if (searchEnabled) {
            syncparamsArray[2] = (byte) 0xAA;
        } else {
            syncparamsArray[2] = (byte) 0x55;
        }

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[configureFindPhone] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }

    protected void sendConfigureACNSActivate() {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x03;
        syncparamsArray[1] = (byte) 0x30;
        syncparamsArray[2] = (byte) 0x55;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[configureACNSActivate] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendConfigureACNS(LifevitSDKAppNotification appNotification) {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x03;
        syncparamsArray[1] = (byte) 0x30;

        if ((appNotification.isSms()) || (appNotification.isEmail()) || (appNotification.isWechat())
                || (appNotification.isQq()) || (appNotification.isWeibo()) || (appNotification.isFacebook())
                || (appNotification.isFacebook()) || (appNotification.isWhatsApp()) || (appNotification.isMessenger())
                || (appNotification.isInstagram()) || (appNotification.isLinkedIn()) || (appNotification.isCalendar())
                || (appNotification.isSkype()) || (appNotification.isAlarm()) || (appNotification.isPokeman())
                || (appNotification.isvKontakte()) || (appNotification.isLine()) || (appNotification.isViber())
                || (appNotification.isKakaotalk()) || (appNotification.isGmail()) || (appNotification.isOutlook())
                || (appNotification.isSnapchat()) || (appNotification.isTelegram())) {

            syncparamsArray[2] = (byte) 0x88;
        } else {
            syncparamsArray[2] = (byte) 0xAA;
        }

        syncparamsArray[3] = (byte) 0x00;
        syncparamsArray[4] = (byte) 0x00;


        if (appNotification.isSms()) {
            syncparamsArray[3] = (byte) (syncparamsArray[3] | (byte) 0x40);
        }

        if (appNotification.isEmail()) {
            syncparamsArray[3] = (byte) (syncparamsArray[3] | 0x20);
        }

        if (appNotification.isWechat()) {
            syncparamsArray[3] = (byte) (syncparamsArray[3] | 0x10);
        }

        if (appNotification.isQq()) {
            syncparamsArray[3] = (byte) (syncparamsArray[3] | 0x08);
        }

        if (appNotification.isWeibo()) {
            syncparamsArray[3] = (byte) (syncparamsArray[3] | 0x04);
        }

        if (appNotification.isFacebook()) {
            syncparamsArray[3] = (byte) (syncparamsArray[3] | 0x02);
        }

        if (appNotification.isTwitter()) {
            syncparamsArray[3] = (byte) (syncparamsArray[3] | 0x01);
        }

        if (appNotification.isWhatsApp()) {
            syncparamsArray[4] = (byte) (syncparamsArray[4] | 0x80);
        }

        if (appNotification.isMessenger()) {
            syncparamsArray[4] = (byte) (syncparamsArray[4] | 0x40);
        }

        if (appNotification.isInstagram()) {
            syncparamsArray[4] = (byte) (syncparamsArray[4] | 0x20);
        }

        if (appNotification.isLinkedIn()) {
            syncparamsArray[4] = (byte) (syncparamsArray[4] | 0x10);
        }

        if (appNotification.isCalendar()) {
            syncparamsArray[4] = (byte) (syncparamsArray[4] | 0x08);
        }

        if (appNotification.isSkype()) {
            syncparamsArray[4] = (byte) (syncparamsArray[4] | 0x04);
        }

        if (appNotification.isAlarm()) {
            syncparamsArray[4] = (byte) (syncparamsArray[4] | 0x02);
        }

        if (appNotification.isPokeman()) {
            syncparamsArray[4] = (byte) (syncparamsArray[4] | 0x01);
        }

        syncparamsArray[5] = (byte) 0x55; //call_switch 85???
        syncparamsArray[6] = (byte) 0x05; //call_dlay

        syncparamsArray[7] = (byte) 0x00;
        if (appNotification.isvKontakte()) {
            syncparamsArray[7] = (byte) (syncparamsArray[7] | 0x80);
        }

        if (appNotification.isLine()) {
            syncparamsArray[7] = (byte) (syncparamsArray[7] | 0x40);
        }

        if (appNotification.isViber()) {
            syncparamsArray[7] = (byte) (syncparamsArray[7] | 0x20);
        }

        if (appNotification.isKakaotalk()) {
            syncparamsArray[7] = (byte) (syncparamsArray[7] | 0x10);
        }

        if (appNotification.isGmail()) {
            syncparamsArray[7] = (byte) (syncparamsArray[7] | 0x08);
        }

        if (appNotification.isOutlook()) {
            syncparamsArray[7] = (byte) (syncparamsArray[7] | 0x04);
        }

        if (appNotification.isSnapchat()) {
            syncparamsArray[7] = (byte) (syncparamsArray[7] | 0x02);
        }

        if (appNotification.isTelegram()) {
            syncparamsArray[7] = (byte) (syncparamsArray[7] | 0x01);
        }


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[configureACNS] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }

    protected void sendConfigureSleepMonitoring(Boolean enabled, LifevitSDKMonitoringAlarm monitoringAlarm) {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x03;
        syncparamsArray[1] = (byte) 0x31;

        if (enabled) {
            syncparamsArray[2] = (byte) 0xAA;
        } else {
            syncparamsArray[2] = (byte) 0x55;
        }

        syncparamsArray[3] = (byte) monitoringAlarm.getStartHour();
        syncparamsArray[4] = (byte) monitoringAlarm.getStartMinute();
        syncparamsArray[5] = (byte) monitoringAlarm.getEndHour();
        syncparamsArray[6] = (byte) monitoringAlarm.getEndMinute();


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[configureSleepMonitoring] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendSynchronizeData() {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x02;
        syncparamsArray[1] = (byte) 0xA0;

        // Synchronization flag: (0x01: manual synchronization, 0x02: automatic synchronization in the background)
        syncparamsArray[2] = (byte) 0x00;

        // Safe mode: (0x01: safe mode, other values: normal mode)
        //syncparamsArray[3] = (byte) 0x00;


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSynchronizeData] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendUnbind() {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x04;
        syncparamsArray[1] = (byte) 0x02;

        syncparamsArray[2] = (byte) 0x55;
        syncparamsArray[3] = (byte) 0xAA;
        syncparamsArray[4] = (byte) 0x55;
        syncparamsArray[5] = (byte) 0xAA;


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendUnbind] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


}