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
public class LifevitSDKBleDeviceBraceletVital extends LifevitSDKBleDevice {

    public static class Constants {
        //2025E COMANDS
        public static byte DATA_SET_MILE = (byte) 0x81;
        public static byte DATA_SET_KM = (byte) 0x80;
        public static byte DATA_GET_MILE = (byte) 0x01;
        public static byte DATA_GET_KM = (byte) 0x00;

        public static byte DATA_SET_HOUR_12_DISPLAY = (byte) 0x81;
        public static byte DATA_SET_HOUR_24_DISPLAY = (byte) 0x80;
        public static byte DATA_GET_HOUR_12_DISPLAY = (byte) 0x01;
        public static byte DATA_GET_HOUR_24_DISPLAY = (byte) 0x00;

        public static byte DATA_SET_ENABLE = (byte) 0x81;
        public static byte DATA_SET_DISABLE = (byte) 0x80;
        public static byte DATA_GET_ENABLED = (byte) 0x01;
        public static byte DATA_GET_DISABLED = (byte) 0x00;

        public static byte DATA_SET_FAHRENHEIT = (byte) 0x81;
        public static byte DATA_SET_CELSIUS = (byte) 0x80;
        public static byte DATA_GET_FAHRENHEIT = (byte) 0x01;
        public static byte DATA_GET_CELSIUS = (byte) 0x00;

        public static byte DATA_SET_CHINESE = (byte) 0x81;
        public static byte DATA_SET_ENGLISH = (byte) 0x80;
        public static byte DATA_GET_CHINESE = (byte) 0x01;
        public static byte DATA_GET_ENGLISH = (byte) 0x00;

        public static byte DATA_OPERATION_DELETE = (byte) 0x99;
        public static byte DATA_OPERATION_READ_MOST_RECENT = (byte) 0x00;
        public static byte DATA_OPERATION_READ_SPECIFIED = (byte) 0x01;
        public static byte DATA_OPERATION_NEXT = (byte) 0x02;

        public static byte SPORT_RUN = (byte) 0x00;
        public static byte SPORT_CYCLING = (byte) 0x01;
        public static byte SPORT_BADMINTON = (byte) 0x02;
        public static byte SPORT_FOOTBALL = (byte) 0x03;
        public static byte SPORT_TENNIS = (byte) 0x04;
        public static byte SPORT_YOGA = (byte) 0x05;
        public static byte SPORT_BREATH = (byte) 0x06;
        public static byte SPORT_DANCE = (byte) 0x07;
        public static byte SPORT_BASKETBALL = (byte) 0x08;
        public static byte SPORT_WALK = (byte) 0x09;
        public static byte SPORT_GYM = (byte) 0x10;
        public static byte SPORT_CRICKET = (byte) 0x11;
        public static byte SPORT_HIKING = (byte) 0x12;
        public static byte SPORT_AEROBICS = (byte) 0x13;
        public static byte SPORT_PINGPONG = (byte) 0x14;
        public static byte SPORT_ROPEJUMP = (byte) 0x15;
        public static byte SPORT_SITUPS = (byte) 0x16;
        public static byte SPORT_VOLLEYBALL = (byte) 0x17;


        public static byte CONTROL_START = (byte) 0x01;
        public static byte CONTROL_PAUSE = (byte) 0x02;
        public static byte CONTROL_CONTINUE = (byte) 0x03;
        public static byte CONTROL_END = (byte) 0x04;

        public static byte MULTIMEDIA_CONTROL_PLAY = (byte) 0x01;
        public static byte MULTIMEDIA_CONTROL_PAUSE = (byte) 0x00;
        public static byte MULTIMEDIA_CONTROL_PREVIOUS = (byte) 0x02;
        public static byte MULTIMEDIA_CONTROL_NEXT = (byte) 0x03;
        public static byte MULTIMEDIA_CONTROL_VOLUME_DOWN = (byte) 0x04;
        public static byte MULTIMEDIA_CONTROL_VOLUME_UP = (byte) 0x05;

        public static byte FIRMWARE_COMMAND_CALL_OPERATION = (byte) 0x01;
        public static byte FIRMWARE_COMMAND_TAKE_PICTURE = (byte) 0x02;
        public static byte FIRMWARE_COMMAND_MUSIC_CONTROL = (byte) 0x03;
        public static byte FIRMWARE_COMMAND_FIND_PHONE = (byte) 0x04;

        public static byte HEALTH_MEASUREMENT_HRV = (byte) 0x01;
        public static byte HEALTH_MEASUREMENT_HEARTRATE = (byte) 0x02;
        public static byte HEALTH_MEASUREMENT_BLOODOXYGEN = (byte) 0x03;

        public static byte UNLOCK_UNLOCK_CODE = (byte) 0x01;
        public static byte UNLOCK_ENTER_BINDING_PAGE = (byte) 0x02;

        public static int REQUEST_ECG_STATUS_MEASUREMENT_NOT_STARTED = 0;
        public static int REQUEST_ECG_STATUS_MEASUREMENT_IN_PROGRESS = 1;
        public static int REQUEST_ECG_STATUS_MEASUREMENT_TIME_OUT = 2;
        public static int REQUEST_ECG_STATUS_MEASUREMENT_COMPLETED = 3;
        public static int REQUEST_ECG_STATUS_MEASUREMENT_LOW_POWER = 4;
        public static int REQUEST_ECG_STATUS_MEASUREMENT_CHARGING_OFF = 5;
        public static int REQUEST_ECG_STATUS_MEASUREMENT_CLOSED_IN_ADVANCE = 6;
        public static int REQUEST_ECG_STATUS_MEASUREMENT_CLOSED_FACTORY_RESET = 7;
        public static int REQUEST_ECG_STATUS_MEASUREMENT_CLOSED_SPORT_MODE = 8;
        public static int REQUEST_ECG_STATUS_MEASUREMENT_CLOSED_SOS_MODE = 9;
        public static int REQUEST_ECG_STATUS_MEASUREMENT_WEAK_SIGNAL = 10;
        public static int REQUEST_ECG_STATUS_MEASUREMENT_NO_SKIN_CONTACT = 11;
        public static int REQUEST_ECG_STATUS_MEASUREMENT_SKIN_CONTACT = 12;
        public static int REQUEST_ECG_STATUS_MEASUREMENT_CALIBRATION_COMPLETE = 13;
        public static int REQUEST_ECG_STATUS_MEASUREMENT_CALIBRATION_FAILED = 14;
        public static int REQUEST_ECG_STATUS_MEASUREMENT_DONT_MOVE_ALERT = 255;

        public static byte REQUEST_SET_TIME = (byte) 0x01;
        public static byte REQUEST_GET_TIME = (byte) 0x41;
        public static byte REQUEST_SET_USER_PERSONAL_INFORMATION = (byte) 0x02;
        public static byte REQUEST_GET_USER_PERSONAL_INFORMATION = (byte) 0x42;
        public static byte REQUEST_SET_DEVICE_PARAMETERS = (byte) 0x03;
        public static byte REQUEST_GET_DEVICE_PARAMETERS = (byte) 0x04;
        public static byte REQUEST_SET_DEVICE_NEW_PARAMETERS = (byte) 0x06;
        public static byte REQUEST_GET_DEVICE_NEW_PARAMETERS = (byte) 0x07;
        public static byte REQUEST_SET_REALTIME_STEP_COUNTING = (byte) 0x09;
        public static byte REQUEST_GET_BLOOD_OXYGEN_DATA = (byte) 0x60;
        public static byte REQUEST_GET_AUTOMATIC_BLOOD_OXYGEN_DATA = (byte) 0x66;
        public static byte REQUEST_GET_TEMPERATURE_DATA = (byte) 0x62;
        public static byte REQUEST_GET_AUTOMATIC_TEMPERATURE_DATA = (byte) 0x65;
        public static byte REQUEST_GET_HEART_RATE_DATA = (byte) 0x54;
        public static byte REQUEST_GET_SINGLE_HEART_RATE_DATA = (byte) 0x55;
        public static byte REQUEST_GET_ECG_START_DATA_UPLOADING = (byte) 0x99;
        public static byte REQUEST_GET_ECG_STOP_DATA_UPLOADING = (byte) 0x98;
        public static byte REQUEST_GET_ECG_MEASUREMENT_STATUS = (byte) 0x9C;
        public static byte REQUEST_GET_ECG_MEASUREMENT = (byte) 0xAA;
        public static byte REQUEST_GET_ECG_WAVEFORM_SAVED = (byte) 0x71;
        public static byte REQUEST_SET_AUTOMATIC_BLOOD_OXYGEN_DETECTION = (byte) 0x29;
        public static byte REQUEST_SET_AUTOMATIC_HEART_RATE_DETECTION = (byte) 0x2A;
        public static byte REQUEST_GET_AUTOMATIC_HEART_RATE_DETECTION = (byte) 0x2B;
        public static byte REQUEST_GET_HRV_DATA = (byte) 0x56;
        public static byte REQUEST_GET_TOTAL_STEPS_DATA = (byte) 0x51;
        public static byte REQUEST_GET_DETAILED_STEPS_DATA = (byte) 0x52;
        public static byte REQUEST_GET_DETAILED_SLEEP_DATA = (byte) 0x53;
        public static byte REQUEST_SET_ACTIVITY_PERIOD = (byte) 0x25;
        public static byte REQUEST_GET_ACTIVITY_PERIOD = (byte) 0x26;
        public static byte REQUEST_SPORT_MODE_CONTROL_ENABLE = (byte) 0x19;
        public static byte REQUEST_APP_HEART_BEAT_PACKET = (byte) 0x17;
        public static byte REQUEST_BRACELET_HEART_BEAT_PACKET = (byte) 0x18;
        public static byte REQUEST_BRACELET_HEALTH_MEASUREMENT_CONTROL = (byte) 0x28;
        public static byte REQUEST_FIRMWARE_COMMAND = (byte) 0x16;
        public static byte REQUEST_SOS_FUNCTION = (byte) 0xFE;
        public static byte REQUEST_GET_DEVICE_BATTERY = (byte) 0x13;
        public static byte REQUEST_UNLOCK_QR_CODE = (byte) 0xB0;
        public static byte REQUEST_GET_SPORTS_DATA = (byte) 0x5C;
        public static byte REQUEST_SET_TARGET_STEPS = (byte) 0x0B;
        public static byte REQUEST_GET_TARGET_STEPS = (byte) 0x4B;
        public static byte REQUEST_GET_MAC_ADDRESS = (byte) 0x22;

        public static byte ERROR_SET_TIME = (byte) 0x81;
        public static byte ERROR_GET_TIME = (byte) 0xC1;
        public static byte ERROR_SET_USER_PERSONAL_INFORMATION = (byte) 0x82;
        public static byte ERROR_GET_USER_PERSONAL_INFORMATION = (byte) 0xC2;
        public static byte ERROR_SET_DEVICE_PARAMETERS = (byte) 0x83;
        public static byte ERROR_GET_DEVICE_PARAMETERS = (byte) 0x84;
        public static byte ERROR_SET_DEVICE_NEW_PARAMETERS = (byte) 0x86;
        public static byte ERROR_GET_DEVICE_NEW_PARAMETERS = (byte) 0x87;
        public static byte ERROR_SET_REALTIME_STEP_COUNTING = (byte) 0x89;
        public static byte ERROR_SET_AUTOMATIC_BLOOD_OXYGEN_DETECTION = (byte) 0xAA;
        public static byte ERROR_GET_AUTOMATIC_HEART_RATE_DETECTION = (byte) 0xAB;
        public static byte ERROR_SET_ACTIVITY_PERIOD = (byte) 0xA5;
        public static byte ERROR_GET_ACTIVITY_PERIOD = (byte) 0xA6;
        public static byte ERROR_GET_DEVICE_BATTERY = (byte) 0x93;
        public static byte ERROR_SET_TARGET_STEPS = (byte) 0x8B;
        public static byte ERROR_GET_TARGET_STEPS = (byte) 0xCB;
        public static byte ERROR_GET_MAC_ADDRESS = (byte) 0xA2;
    }


    private final static String CLASS_TAG = LifevitSDKBleDeviceBraceletVital.class.getSimpleName();

    private static final String DEVICE_NAME = "J2025E";
    private static final String DEVICE_SECOND_NAME = "J2025E";

    private ArrayList<LifevitSDKHeartbeatData> heartRateDataArray = new ArrayList<>();
    private ArrayList<LifevitSDKStepData> stepDataArray = new ArrayList<>();
    private ArrayList<LifevitSDKSleepData> sleepDataArray = new ArrayList<>();
    private ArrayList<LifevitSDKOximeterData> oximeterDataArray = new ArrayList<>();
    private ArrayList<LifevitSDKSleepData> bloodPressureDataArray = new ArrayList<>();
    private ArrayList<LifevitSDKSleepData> temperatureDataArray = new ArrayList<>();
    private ArrayList<LifevitSDKSleepData> sportsDataArray = new ArrayList<>();

    private long recordDate;
    private int packetDurationInMinutes;
    private int totalPackages;
    private int totalItems;

    private String mDeviceType = "";


    /**
     * Attributes
     */

    // Sending queue, to send instructions to device
    static BraceletVitalSendQueue sendingThread;


    /**
     * Service descriptor 4
     */

    // Custom service (primary service)
    private static final String UUID_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";

    // Properties: WRITE, WRITE_NO_RESPONS (client can write, and write with response, on this characteristic)
    // Write Type: WRITE REQUEST (will give you a response back telling you the write was successful)
    // Descriptors:
    // 1. Characteristic user description, UUID: 0x2901 (read-only, provides a textual user description for a characteristic value)
    protected static final String UUID_WRITE_CHARACTERISTIC = "0000fff6-0000-1000-8000-00805f9b34fb";

    // Properties: NOTIFY (allows the server to use the Handle Value Notification ATT operation on this characteristic )
    // Descriptors:
    // 1. Client Characteristic Configuration, UUID: 0x2902 (defines how the characteristic may be configured by a specific client)
    protected static final String UUID_NOTIFY_CHARACTERISTIC_READ = "0000fff7-0000-1000-8000-00805f9b34fb";

    // region --- Device methods ---


    protected LifevitSDKBleDeviceBraceletVital(BluetoothDevice dev, LifevitSDKManager manager) {

        this.mBluetoothDevice = dev;
        this.mLifevitSDKManager = manager;
    }


    @Override
    protected int getType() {
        return LifevitSDKConstants.DEVICE_BRACELET_VITAL;
    }


    protected void connectGatt(Context context, boolean firstTime) {

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[connection] CONNECT: " + mBluetoothDevice.getAddress());

        mBluetoothGatt = mBluetoothDevice.connectGatt(context, false, mGattCallback);
        mContext = context;
        mFirstTime = firstTime;

        mDeviceType = getDevice().getName();

        sendingThread = new BraceletVitalSendQueue(this);
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


    protected static boolean matchDevice(String name) {
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
        mLifevitSDKManager.deviceOnConnectionChanged(LifevitSDKConstants.DEVICE_BRACELET_VITAL, mDeviceStatus, true);

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

    }


    // endregion --- Device methods ---


    // region --- Public methods to give orders to bracelet ---

    /*void getBasicInfo() {
        sendingThread.addToQueue(ACTION_GET_BASIC_INFO);
    }

    void getFeatureList() {
        sendingThread.addToQueue(ACTION_GET_FEATURE_LIST);
    }

    void setTime(Long datetime) {
        sendingThread.addToQueue(ACTION_SET_TIME, datetime);
    }

    void getDeviceTime() {
        sendingThread.addToQueue(ACTION_GET_DEVICE_TIME);
    }

    void synchronizeData() {
        sendingThread.addToQueue(ACTION_SYNC_DATA);
    }

    void synchronizeSportsData() {
        sendingThread.addToQueue(ACTION_SYNC_SPORTS_DATA);
    }

    void synchronizeSleepData() {
        sendingThread.addToQueue(ACTION_SYNCHRONIZE_SLEEP_DATA);
    }

    void synchronizeHeartRateData() {
        sendingThread.addToQueue(ACTION_SYNCHRONIZE_HEART_RATE_DATA);
    }

    void synchronizeHistoricSportData() {
        sendingThread.addToQueue(ACTION_SYNCHRONIZE_HISTORIC_SPORT_DATA);
    }

    void synchronizeHistoricSleepData() {
        sendingThread.addToQueue(ACTION_SYNCHRONIZE_HISTORIC_SLEEP_DATA);
    }

    void synchronizeHistoricHeartRateData() {
        sendingThread.addToQueue(ACTION_SYNCHRONIZE_HISTORIC_HEART_RATE_DATA);
    }

    void configureAlarm(LifevitSDKAlarmTime alarm) {
        sendingThread.addToQueue(ACTION_CONFIGURE_ALARM, alarm);
    }

    void removeAlarm(boolean isPrimaryAlarm) {
        sendingThread.addToQueue(ACTION_CONFIGURE_ALARM, isPrimaryAlarm);
    }

    void setGoals(int steps, int sleepHour, int sleepMinute) {
        sendingThread.addToQueue(ACTION_SET_GOALS, steps, sleepHour, sleepMinute);
    }

    void setUserInformation(LifevitSDKUserData user) {
        sendingThread.addToQueue(ACTION_SET_USER_INFORMATION, (int) user.getHeight(), (int) user.getWeight(), user.getGender(), user.getBirthdate());
    }

    void configureBraceletSedentaryAlarm(LifevitSDKMonitoringAlarm alarm) {
        sendingThread.addToQueue(ACTION_CONFIGURE_BRACELET_SEDENTARY_ALARM, alarm);
    }

    void disableBraceletSedentaryAlarm() {
        sendingThread.addToQueue(ACTION_DISABLE_BRACELET_SEDENTARY_ALARM);
    }

    void configureAntitheft(Boolean active) {
        sendingThread.addToQueue(ACTION_ANTITHEFT, active);
    }

    void configureRiseHand(Boolean leftHand) {
        sendingThread.addToQueue(ACTION_RISE_HAND, leftHand);
    }

    void configureAndroidPhone() {
        sendingThread.addToQueue(ACTION_ANDROID_PHONE);
    }

    void configureHeartRateIntervalSetting(int burnFatThreshold, int aerobicExercise, int limitExercise, int userMaxHR) {
        sendingThread.addToQueue(ACTION_HEART_RATE_INTERVAL_SETTING, burnFatThreshold, aerobicExercise, limitExercise, userMaxHR);
    }

    void configureHeartRateMonitoring(Boolean enabled, Boolean timeIntervalEnabled, LifevitSDKSedentaryAlarm monitoringTime) {
        sendingThread.addToQueue(ACTION_HEART_RATE_MONITORING, enabled, timeIntervalEnabled, monitoringTime);
    }

    void configureFindPhone(Boolean enabled) {
        sendingThread.addToQueue(ACTION_FIND_PHONE, enabled);
    }

    void configureACNS(LifevitSDKAppNotification appNotification) {
        sendingThread.addToQueue(ACTION_ACNS, appNotification);
    }

    void configureSleepMonitoring(Boolean enabled, LifevitSDKMonitoringAlarm monitoringTime) {
        sendingThread.addToQueue(ACTION_SLEEP_MONITORING, enabled, monitoringTime);
    }

    void getBattery() {
        sendingThread.addToQueue(ACTION_BATTERY);
    }

    void startSynchronization() {
        sendingThread.addToQueue(ACTION_START_SYNCHRONIZATION);
    }

    void replyLastSynchronization() {
        sendingThread.addToQueue(ACTION_REPLY_LAST_SYNCHRONIZATION);
    }

    void messageReceived() {
        sendingThread.addToQueue(ACTION_MESSAGE_RECEIVED);
    }
*/
    // endregion --- "Public methods" ---

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

        BluetoothGattCharacteristic RxChar;

        RxChar = RxService.getCharacteristic(UUID.fromString(UUID_WRITE_CHARACTERISTIC));


        if (RxChar == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "Rx charateristic not found!");
            return;
        }

        LogUtils.log(Log.DEBUG, CLASS_TAG, "SENDING Message (complete): " + HexUtils.getStringToPrint(data));

        dividePacketsAndSendMessage(RxChar, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
    }


    // region --- Processing responses methods ---


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

        String message = ">>> Received get basic info. Data received:\n";
        message = message + "    - deviceId: " + deviceId + "\n";
        message = message + "    - firmwareVersion: " + firmwareVersion + "\n";
        message = message + "    - runMode: " + runMode + "\n";
        message = message + "    - batteryStatus: " + batteryStatus + "\n";
        message = message + "    - batteryPower: " + batteryPower + "\n";
        message = message + "    - bindingFlag: " + bindingFlag + "\n";
        message = message + "    - restartFlag: " + restartFlag + "\n";
        message = message + "    - hasDetailVersion: " + hasDetailVersion;

        LogUtils.log(Log.DEBUG, CLASS_TAG, message);

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {
            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
        }

        sendingThread.taskFinished();
    }


    private String processFeatureByte(byte rx, String title, String func1, String func2, String func3,
                                      String func4, String func5, String func6, String func7, String func8) {

        StringBuilder message = new StringBuilder();

        if (title != null) {
            message.append("    - " + title + ": " + String.format("%8s\n", Integer.toBinaryString(rx & 0xFF)).replace(' ', '0'));
        }

        if (func1 != null) {
            if ((rx & 0b00000001) > 0) {
                message.append("        - " + func1 + ": YES\n");
            } else {
                message.append("        - " + func1 + ": NO\n");
            }
        }
        if (func2 != null) {
            if ((rx & 0b00000010) > 0) {
                message.append("        - " + func2 + ": YES\n");
            } else {
                message.append("        - " + func2 + ": NO\n");
            }
        }
        if (func3 != null) {
            if ((rx & 0b00000100) > 0) {
                message.append("        - " + func3 + ": YES\n");
            } else {
                message.append("        - " + func3 + ": NO\n");
            }
        }
        if (func4 != null) {
            if ((rx & 0b00001000) > 0) {
                message.append("        - " + func4 + ": YES\n");
            } else {
                message.append("        - " + func4 + ": NO\n");
            }
        }
        if (func5 != null) {
            if ((rx & 0b00010000) > 0) {
                message.append("        - " + func5 + ": YES\n");
            } else {
                message.append("        - " + func5 + ": NO\n");
            }
        }
        if (func6 != null) {
            if ((rx & 0b00100000) > 0) {
                message.append("        - " + func6 + ": YES\n");
            } else {
                message.append("        - " + func6 + ": NO\n");
            }
        }
        if (func7 != null) {
            if ((rx & 0b01000000) > 0) {
                message.append("        - " + func7 + ": YES\n");
            } else {
                message.append("        - " + func7 + ": NO\n");
            }
        }
        if (func8 != null) {
            if ((rx & 0b10000000) > 0) {
                message.append("        - " + func8 + ": YES\n");
            } else {
                message.append("        - " + func8 + ": NO\n");
            }
        }
        return message.toString();
    }


    private void processGetFeatureList(byte[] rx) {

        byte main_function = rx[2];
        int alarm_count = rx[3];
        byte alarm_type = rx[4];
        byte control = rx[5];
        byte call_notify = rx[6];
        byte msg_notify1 = rx[7];
        byte other = rx[8];
        byte msg_cfg = rx[9];
        byte msg_notify2 = rx[10];
        byte other2 = rx[11];
        byte sport_type0 = rx[12];
        byte sport_type1 = rx[13];
        byte sport_type2 = rx[14];
        byte sport_type3 = rx[15];
        byte main1 = rx[16];
        byte msg_notify3 = rx[17];
        int sport_num_show = rx[18];
        byte lang_type = rx[19];

        StringBuilder message = new StringBuilder(">>> Received get feature list. Data received:\n");

        message.append("    - Alarms count: " + alarm_count + "\n");
        message.append("    - Sports count: " + sport_num_show + "\n");

        message.append(processFeatureByte(main_function, "Main functions",
                "step counting", "sleep monitoring", "single motion", "real-time data",
                "device upgrade", "heart rate monitoring", "message center", "timeline"));

        message.append(processFeatureByte(main1, "Main functions (1)",
                "log in", "hid service", "dial1 setting", "shortcut key1 setting",
                "Separate unit setting", "blood pressure", null, null));

        message.append(processFeatureByte(alarm_type, "Alarm types",
                "get-up", "sleep", "training", "take medicine",
                "date", "party", "meeting", "customize"));

        message.append(processFeatureByte(control, "Control functions",
                "photo shooting", "music", "hid shooting control", "5 heart rate intervals",
                "Binding timeout confirmation", "Fast sync", "Extended functions", null));

        message.append(processFeatureByte(call_notify, "Call alert",
                "Call contact", "Call number", null, null,
                null, null, null, null));

        message.append(processFeatureByte(sport_type0, "Sport types",
                "walking", "running", "riding", "hiking",
                "swimming", "mountain climbing", "badminton", "others"));

        message.append(processFeatureByte(sport_type1, "Sports (2)",
                "fitness", "spinning bike", "Oval ball", "Running machine",
                "Sit-up", "Push-up", "Dumbbell", "Weight lifting"));

        message.append(processFeatureByte(sport_type2, "Sports (3)",
                "Aerobics", "Yoga", "rope skipping", "Table tennis",
                "Basketball", "Football", "Volleyball", "Tennis"));

        message.append(processFeatureByte(sport_type3, "Sports (4)",
                "Golf", "Baseball", "Skiing", "Roller Skating",
                "Dancing", "Gym", null, null));

        message.append(processFeatureByte(other, "Other functions",
                "Sedentary alert", "Anti-lost reminder", "Calling", "Find phone",
                "Find band", "One-click setting restore", "Wrist sense", "Weather forecast"));

        message.append(processFeatureByte(other2, "Other functions (2)",
                "static heart rate", "Do not disturb mode", "Display mode", "Heart rate monitoring mode control",
                "Two-way anti-lost", "All smart reminders", "Display flip 180 degrees", "Do not display heart rate zone values"));

        message.append(processFeatureByte(msg_cfg, "Message functions",
                "message contact remind", "message number remind", "message remind", null,
                null, null, null, null));

        message.append(processFeatureByte(msg_notify1, "Messages (2)",
                "SMS", "Email", null, null,
                null, "Facebook", "Twitter", null));

        message.append(processFeatureByte(msg_notify2, "Messages (3)",
                "WhatsApp", "Messenger", "Instagram", "LinkedIn",
                "Calendar", "Skype", "Alarm event", "Pokemon"));

        message.append(processFeatureByte(msg_notify3, "Messages (4)",
                null, "Line", "Viber", null,
                "Gmail", "Outlook", "Snapchat", "Telegram"));

        message.append(processFeatureByte(lang_type, "Languages",
                "Chinese", "English", "French", "German",
                "Italian", "Spanish", "Japanese", "Czech"));

        String messageStr = message.toString();

        LogUtils.log(Log.DEBUG, CLASS_TAG, messageStr);

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {
            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(messageStr);
        }

        //sendingThread.taskFinished();

        sendGetExtendedFeatures();
    }

    private void processGetExtendedFeatureList(byte[] rx) {

        int langTypes2 = rx[2];
        int other = rx[3];

        String message = ">>> Received get extended feature list. Data received:\n";
        message = message + "    - other: " + String.format("%8s\n", Integer.toBinaryString(other & 0xFF)).replace(' ', '0');
        message = message + "    - langTypes2: " + String.format("%8s", Integer.toBinaryString(langTypes2 & 0xFF)).replace(' ', '0');

        LogUtils.log(Log.DEBUG, CLASS_TAG, message);

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {
            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
        }

        sendingThread.taskFinished();
    }

    private void processEndData() {

        recordDate = 0;

//        int packetDurationInMinutes = 0;
//        int totalPackages = 0;

        LifevitSDKBraceletData braceletData = new LifevitSDKBraceletData();

        if (stepDataArray.size() > 0) {
            ArrayList<LifevitSDKStepData> stepDataToSend = new ArrayList<>();
            for (LifevitSDKStepData oneStepData : stepDataArray) {
                if (oneStepData.getSteps() > 0) {
                    stepDataToSend.add(oneStepData);
                }
            }
            if (stepDataToSend.size() > 0) {
                braceletData.setStepsData(stepDataToSend);
            }
        }

        if (sleepDataArray.size() > 0) {
            ArrayList<LifevitSDKSleepData> sleepDataToSend = new ArrayList<>();
            for (LifevitSDKSleepData oneSleepData : sleepDataArray) {
                if (oneSleepData.getSleepDuration() > 0) {
                    sleepDataToSend.add(oneSleepData);
                }
            }
            if (sleepDataToSend.size() > 0) {
                braceletData.setSleepData(sleepDataToSend);
            } else {
                // Send at least 1 element
                braceletData.setSleepData(sleepDataArray);
            }
        }

        if (heartRateDataArray.size() > 0) {
            ArrayList<LifevitSDKHeartbeatData> hrDataToSend = new ArrayList<>();
            for (LifevitSDKHeartbeatData oneHrData : heartRateDataArray) {
                if (oneHrData.getHeartrate() > 0) {
                    hrDataToSend.add(oneHrData);
                }
            }
            if (hrDataToSend.size() > 0) {
                braceletData.setHeartData(hrDataToSend);
            }
        }

        if (mLifevitSDKManager.getBraceletVitalListener() != null) {
            mLifevitSDKManager.getBraceletVitalListener().braceletDataReceived(braceletData);
        }

        //replyLastSynchronization();

        sendingThread.taskFinished();
    }

    private void processSynchronizeHeartRateData(byte[] rx) {

        int serial = rx[2];
        int length = rx[3];

        LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> LifevitSDK:HR Data serial: " + serial + " / total: " + totalPackages);

        if (serial == 0x01) {

            heartRateDataArray.clear();
            stepDataArray.clear();
            sleepDataArray.clear();

            int header1_date_year = getHeaderYear(rx);
            int header1_date_month = Utils.bytesToInt(new byte[]{0, 0, 0, rx[6]});
            int header1_date_day = Utils.bytesToInt(new byte[]{0, 0, 0, rx[7]});

            int minutesOffset = Utils.bytesToInt(new byte[]{0, 0, rx[8], rx[9]});

            int silentHeart = Utils.bytesToInt(new byte[]{0, 0, 0, rx[10]});
            int items = Utils.bytesToInt(new byte[]{0, 0, 0, rx[11]});
            int totalPackets = Utils.bytesToInt(new byte[]{0, 0, 0, rx[12]});
            int mxHR = Utils.bytesToInt(new byte[]{0, 0, 0, rx[13]});


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
            cal.set(Calendar.DAY_OF_MONTH, header1_date_day);

            cal.set(Calendar.HOUR_OF_DAY, minutesOffset / 60);
            cal.set(Calendar.MINUTE, minutesOffset % 60);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            recordDate = cal.getTimeInMillis();
            totalPackages = totalPackets;
            totalItems = items;

        } else if (serial == 0x02) {

            int burn_fat_threshold = Utils.bytesToInt(new byte[]{0, 0, 0, rx[4]});
            int aerobic_threshold = Utils.bytesToInt(new byte[]{0, 0, 0, rx[5]});
            int limit_threshold = Utils.bytesToInt(new byte[]{0, 0, 0, rx[6]});
            int burn_fat_mins = Utils.bytesToInt(new byte[]{0, 0, 0, rx[7]});
            int aerobic_mins = Utils.bytesToInt(new byte[]{0, 0, 0, rx[8]});
            int limit_mins = Utils.bytesToInt(new byte[]{0, 0, 0, rx[9]});

            LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> DATA");
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- burn_fat_threshold: " + burn_fat_threshold);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- aerobic_threshold: " + aerobic_threshold);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- limit_threshold: " + limit_threshold);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- burn_fat_mins: " + burn_fat_mins);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- aerobic_mins: " + aerobic_mins);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- limit_mins: " + limit_mins);
        } else {
            int sIndex = 4;
            while (sIndex < (length + 4) && totalItems > 0) {
                int distanceToLastMeasurement = byteToUnsignedInt(rx[sIndex]);
                int hr = rx[sIndex + 1];

                LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> DATA date: " + new Date(recordDate) + ",distanceToLastMeasurement: " + distanceToLastMeasurement + ", Heart rate: " + hr);

                LifevitSDKHeartbeatData data = new LifevitSDKHeartbeatData();
                data.setHeartrate(hr);
                data.setDate(recordDate);

                // Update date for next measurement
                if (distanceToLastMeasurement > 0) {
                    recordDate = recordDate + distanceToLastMeasurement * 60 * 1000;
                }

                heartRateDataArray.add(data);
                totalItems--;
                sIndex = sIndex + 2;
            }
        }
    }


    public static int byteToUnsignedInt(byte b) {
        return b & 0xff;
    }


    private void processSynchronizeSleepData(byte[] rx) {

        int serial = rx[2];
        int length = rx[3];

        String message = ">>> LifevitSDK:Sleep Data serial: " + serial + " / total: " + totalPackages;
        LogUtils.log(Log.DEBUG, CLASS_TAG, message);

        if (serial == 0x01) {

            heartRateDataArray.clear();
            stepDataArray.clear();
            sleepDataArray.clear();

            int header1_date_year = getHeaderYear(rx);

            int header1_date_month = rx[6];
            int header1_date_day = rx[7];

            int hour = rx[8];
            int minutes = rx[9];

            int totalMinutes = Utils.bytesToInt(new byte[]{0, 0, rx[11], rx[10]});

            int sleepItems = rx[12];
            int totalPackets = rx[13];

            LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> Headers");
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_date_year: " + header1_date_year);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_date_month: " + header1_date_month);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- header1_date_day: " + header1_date_day);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- sleep end hour: " + hour);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- sleep end minutes: " + minutes);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- totalMinutes: " + totalMinutes);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- sleepItems: " + sleepItems);
            LogUtils.log(Log.DEBUG, CLASS_TAG, "    --- totalPackets: " + totalPackets);


            Calendar calEndTime = Calendar.getInstance();
            calEndTime.set(Calendar.YEAR, header1_date_year);
            calEndTime.set(Calendar.MONTH, header1_date_month - 1);
            calEndTime.set(Calendar.DAY_OF_MONTH, header1_date_day);
            calEndTime.set(Calendar.HOUR_OF_DAY, hour);
            calEndTime.set(Calendar.MINUTE, minutes);
            calEndTime.set(Calendar.SECOND, 0);
            calEndTime.set(Calendar.MILLISECOND, 0);

            Calendar calStartTime = Calendar.getInstance();
            calStartTime.setTimeInMillis(calEndTime.getTimeInMillis());
            calStartTime.add(Calendar.MINUTE, -totalMinutes);

            recordDate = calStartTime.getTimeInMillis();
            totalPackages = totalPackets;
            totalItems = sleepItems;

            if (sleepItems == 0) {
                // If there are no elements, add one empty element to know we finished synchronizing sleep data
                LifevitSDKSleepData data = new LifevitSDKSleepData();
                data.setDate(Calendar.getInstance().getTimeInMillis());
                data.setSleepDuration(0);
                sleepDataArray.add(data);
            }

        } else if (serial == 0x02) {

            int lightSleepCount = rx[4];
            int deepSleepCount = rx[5];
            int awakes = rx[6];

            int totalLightMinutes = Utils.bytesToInt(new byte[]{0, 0, rx[8], rx[7]});
            int totalDeepMinutes = Utils.bytesToInt(new byte[]{0, 0, rx[10], rx[9]});

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
            int sIndex = 4;
            while (sIndex < (length + 4) && totalItems > 0) {
                int status = rx[sIndex];
                int duration = rx[sIndex + 1];

                LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> DATA: status: " + status + ", duration: " + duration);


                LifevitSDKSleepData data = new LifevitSDKSleepData();

                data.setSleepDuration(duration);
                switch (status) {
                    case 2:
                        data.setSleepDeepness(LifevitSDKConstants.LIGHT_SLEEP);
                        break;
                    case 3:
                        data.setSleepDeepness(LifevitSDKConstants.DEEP_SLEEP);
                        break;
                    default:
                        data.setSleepDeepness(-1);
                }

                if (data.getSleepDeepness() != -1) {
                    data.setDate(recordDate);
                    sleepDataArray.add(data);
                }

                // Update next record date
                recordDate = recordDate + duration * 60 * 1000;

                totalItems--;
                sIndex = sIndex + 2;
            }
        }
    }


    private void processSynchronizeGeneralData(byte[] rx) {

        int totalPackets = Utils.bytesToInt(new byte[]{0, 0, rx[3], rx[2]});
        int activeDays = rx[4];
        int sleepDays = rx[5];
        int heartDays = rx[6];

        String message = ">>> Data received:\n"
                + "    - Total days: " + totalPackets + "\n"
                + "    - Days with Sport data: " + activeDays + "\n"
                + "    - Days with Sleep data: " + sleepDays + "\n"
                + "    - Days with Heart Rate data: " + heartDays;

        LogUtils.log(Log.DEBUG, CLASS_TAG, message);

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {
            mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
        }

        sendingThread.taskFinished();
    }


    private void processSynchronizeSportData(byte[] rx) {

        int serial = rx[2];
        int length = rx[3];

        String message = ">>> LifevitSDK: Sport Data serial: " + serial + " / total: " + totalPackages;
        LogUtils.log(Log.DEBUG, CLASS_TAG, message);

        if (serial == 0x01) {

            heartRateDataArray.clear();
            stepDataArray.clear();
            sleepDataArray.clear();

            int header1_date_year = getHeaderYear(rx);
            int header1_date_month = rx[6];
            int header1_date_day = rx[7];

            int header1_offset_minutes_from_day_start = Utils.bytesToInt(new byte[]{0, 0, rx[9], rx[8]});
            int header1_how_many_minutes_every_package = rx[10];

            int header1_sport_item = rx[11];
            int header1_total_packages = rx[12];

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
            cal.set(Calendar.DAY_OF_MONTH, header1_date_day);

            cal.set(Calendar.HOUR_OF_DAY, header1_offset_minutes_from_day_start / 60);
            cal.set(Calendar.MINUTE, header1_offset_minutes_from_day_start % 60);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            recordDate = cal.getTimeInMillis();

            packetDurationInMinutes = header1_how_many_minutes_every_package;
            totalPackages = header1_total_packages;
            totalItems = header1_sport_item;

        } else if (serial == 0x02) {

            if (rx.length < 20) {
                return;
            }

            int header2_total_steps = Utils.bytesToInt(new byte[]{rx[7], rx[6], rx[5], rx[4]});
            int header2_total_calories = Utils.bytesToInt(new byte[]{rx[11], rx[10], rx[9], rx[8]});
            int header2_total_distance = Utils.bytesToInt(new byte[]{rx[15], rx[14], rx[13], rx[12]});
            int header2_total_active_time = Utils.bytesToInt(new byte[]{rx[19], rx[18], rx[17], rx[16]});

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
            int sIndex = 4;
            while (sIndex < length + 4) {

                byte b0 = rx[sIndex];
                byte b1 = rx[sIndex + 1];
                byte b2 = rx[sIndex + 2];
                byte b3 = rx[sIndex + 3];
                byte b4 = rx[sIndex + 4];

                int mode = b0 & 0x03;
                int steps = ((b0 & 0xFC) >> 2) + ((b1 & 0x3F) << 6);
                int activeTime = ((b1 & 0xC0) >> 6) + ((b2 & 0x03) << 2);
                int calories = ((b2 & 0xFC) >> 2) + ((b3 & 0x0F) << 6);
                int distance = ((b3 & 0xF0) >> 4) + ((b4 & 0xFF) << 4);


                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(recordDate);
                cal.add(Calendar.MINUTE, packetDurationInMinutes * stepDataArray.size());

                //recordDate = cal.getTimeInMillis() / 1000;
                ///data.setDate(recordDate);

                LifevitSDKStepData data = new LifevitSDKStepData(cal.getTimeInMillis(), steps, calories, distance);

//                if (steps != 0 && calories != 0 && distance != 0) {
                stepDataArray.add(data);
//                }

                LogUtils.log(Log.DEBUG, CLASS_TAG, "    === Current === "
                        + "rDate: " + cal.getTimeInMillis()
                        + ", steps: " + steps
                        + ", calories: " + calories
                        + ", distance: " + distance);

                sIndex += 5;
            }
        }

    }

    private int getHeaderYear(byte[] rx) {
        if (mDeviceType.equals(DEVICE_NAME)) {
            return Utils.bytesToInt(new byte[]{0, 0, rx[4], rx[5]});
        } else {
            return Utils.bytesToInt(new byte[]{0, 0, rx[4], rx[5]});
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
        cal.set(Calendar.MILLISECOND, 0);

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

        String message = ">>> Received get feature list. Data received:\n";
        message = message + "    - notify_switch: " + String.format("%8s\n", Integer.toBinaryString(notify_switch & 0xFF)).replace(' ', '0');
        message = message + "    - notify_item1: " + String.format("%8s\n", Integer.toBinaryString(notify_item1 & 0xFF)).replace(' ', '0');
        message = message + "    - notify_item2: " + String.format("%8s\n", Integer.toBinaryString(notify_item2 & 0xFF)).replace(' ', '0');
        message = message + "    - call_switch: " + String.format("%8s\n", Integer.toBinaryString(call_switch & 0xFF)).replace(' ', '0');
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
        parseSettingsResponse(rx, "Datetime");
    }

    private void processConfigureAlarm(byte[] rx) {
        parseSettingsResponse(rx, "Alarm");
    }

    private void processStepTargetAndSleepTime(byte[] rx) {
        parseSettingsResponse(rx, "Target and sleep time");
    }

    private void processSetUserInformation(byte[] rx) {
        parseSettingsResponse(rx, "User information");
    }

    private void processConfigureSedentaryAlarm(byte[] rx) {
        parseSettingsResponse(rx, "Sedentary alarm");
    }

    private void processConfigureAntiTheft(byte[] rx) {
        parseSettingsResponse(rx, "Antitheft");
    }

    private void processRiseHand(byte[] rx) {
        parseSettingsResponse(rx, "Rise hand");
    }

    private void processAndroidPhone(byte[] rx) {
        parseSettingsResponse(rx, "Android Phone");
    }

    private void processHeartRateIntervalSetting(byte[] rx) {
        parseSettingsResponse(rx, "Heart rate intervals");
    }


    private void processHeartRateMonitoring(byte[] rx) {
        parseSettingsResponse(rx, "Heart rate monitoring");
    }

    private void processFindPhone(byte[] rx) {
        parseSettingsResponse(rx, "Find phone functionality");
    }

    private void processConfigureReminder(byte[] rx) {
        if (rx.length > 2 && rx[2] == -120) {
            // sendConfigureACNSActivate();
        }
        parseSettingsResponse(rx, "Notification reminder");
    }

    private void processSleepMonitoring(byte[] rx) {
        parseSettingsResponse(rx, "Sleep monitoring");
    }

    private void parseSettingsResponse(byte[] rx, String operation) {

        if (mLifevitSDKManager.getBraceletAT2019Listener() != null) {
            if (rx.length > 2) {
                int success = rx[2];
                String message;
                if (success == 0) {
                    message = "Succeed setting: " + operation;
                } else {
                    message = "Error setting: " + operation + " (error code " + success + ")";
                }
                mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation(message);
            } else if (rx.length == 2) {
                mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation("Succeed setting (2 bytes): " + operation);
            } else {
                mLifevitSDKManager.getBraceletAT2019Listener().braceletInformation("Unknown error setting: " + operation);
            }
        }

        sendingThread.taskFinished();
    }

    // endregion --- Processing responses methods ---


    protected void characteristicReadProcessData(BluetoothGattCharacteristic characteristic) {

        LogUtils.log(Log.DEBUG, CLASS_TAG, "RECEIVED from characteristic: " + characteristic.getUuid().toString());

        if (characteristic.getUuid().equals(UUID.fromString(UUID_NOTIFY_CHARACTERISTIC_READ))) {

            byte[] rx = characteristic.getValue();

            String resultsStr = new String(rx);

            LogUtils.log(Log.DEBUG, CLASS_TAG, "characteristicReadProcessData - RECEIVED (byte format): " + HexUtils.getStringToPrint(rx));

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

                processAndroidPhone(rx);

            } else if (rx[0] == 0x03 && rx[1] == 0x24) {

                processHeartRateIntervalSetting(rx);

            } else if (rx[0] == 0x03 && rx[1] == 0x25) {

                processHeartRateMonitoring(rx);

            } else if (rx[0] == 0x03 && rx[1] == 0x26) {

                processFindPhone(rx);

            } else if (rx[0] == 0x03 && rx[1] == 0x30) {

                processConfigureReminder(rx);

            } else if (rx[0] == 0x03 && rx[1] == 0x31) {

                processSleepMonitoring(rx);

            } else if (rx[0] == 0x05 && rx[1] == (byte) 0x03) {

                parseSettingsResponse(rx, "Message Received");

            } else if (rx[0] == 0x08 && rx[1] == (byte) 0xEE) {

                processEndData();

            } else {

                LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> WRONG COMMAND");
                sendingThread.taskFinished();
            }

        }
    }


    // region --- Send commands to bracelet ---


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


    protected void sendStartSynchronizeData() {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x08;
        syncparamsArray[1] = (byte) 0x01;

        // Synchronization flag: (0x01: manual synchronization, 0x02: automatic synchronization in the background)
        syncparamsArray[2] = (byte) 0x02;

        // Safe mode: (0x01: safe mode, other values: normal mode)
        syncparamsArray[3] = (byte) 0x01;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendStartSynchronizeData] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendReplyLastSynchronization() {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x08;
        syncparamsArray[1] = (byte) 0xEE;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendReplyLastSynchronization] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);

        sendingThread.taskFinished();
    }


    protected void sendEndSynchronizeData() {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x08;
        syncparamsArray[1] = (byte) 0x02;

        // Safe mode: (0x01: safe mode, other values: normal mode)
        syncparamsArray[2] = (byte) 0x01;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendEndSynchronizeData] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
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
        syncparamsArray[3] = (byte) 0x00;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSynchronizeSportsData] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }

    protected void sendSynchronizeSleepData(int mode) {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x08;
        syncparamsArray[1] = (byte) 0x04;

        // Status (0x01: start, 0x02: end, 0x03: resend)
        syncparamsArray[2] = (byte) mode;

        // The serial number of the package (1~255), valid only when resending
        syncparamsArray[3] = (byte) 0x00;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSynchronizeSleepData] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendSynchronizeHeartRateData(int mode) {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x08;
        syncparamsArray[1] = (byte) 0x07;

        // Status (0x01: start, 0x02: end, 0x03: resend)
        syncparamsArray[2] = (byte) mode;

        // The serial number of the package (1~255), valid only when resending
        syncparamsArray[3] = (byte) 0x00;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSynchronizeHeartRateData] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendSetTime(Long time) {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x03;
        syncparamsArray[1] = (byte) 0x01;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);

        int currentYear = cal.get(Calendar.YEAR);
        syncparamsArray[2] = (byte) (currentYear & 0xFF);
        syncparamsArray[3] = (byte) ((currentYear >> 8) & 0xFF);

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
        syncparamsArray[3] = (byte) 0x00;


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send sendSynchronizeHistoricSportData] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }

    protected void sendSynchronizeHistoricSleepData() {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x08;
        syncparamsArray[1] = (byte) 0x06;
        syncparamsArray[2] = (byte) 0x01;
        syncparamsArray[3] = (byte) 0x01;


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send sendSynchronizeHistoricSleepData] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }

    protected void sendSynchronizeHistoricHeartRateData() {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x08;
        syncparamsArray[1] = (byte) 0x08;
        syncparamsArray[2] = (byte) 0x01;
        syncparamsArray[3] = (byte) 0x01;


        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send sendSynchronizeHistoricHeartRateData] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
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

        String weekDays = "";

        weekDays += alarm.isSunday() ? "1" : "0";
        weekDays += alarm.isSaturday() ? "1" : "0";
        weekDays += alarm.isFriday() ? "1" : "0";
        weekDays += alarm.isThursday() ? "1" : "0";
        weekDays += alarm.isWednesday() ? "1" : "0";
        weekDays += alarm.isTuesday() ? "1" : "0";
        weekDays += alarm.isMonday() ? "1" : "0";

        // Enabled
        weekDays += "1";

        syncparamsArray[7] = (byte) Integer.parseInt(weekDays, 2);
        syncparamsArray[8] = (byte) 0x00;

        // Send command

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[Send configureAlarm] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendRemoveAlarm(boolean isPrimaryAlarm) {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x03;
        syncparamsArray[1] = (byte) 0x02;

        if (isPrimaryAlarm) {
            syncparamsArray[2] = (byte) 0x01; //alarm (alarm number)
        } else {
            syncparamsArray[2] = (byte) 0x02; //alarm (alarm number)
        }

        syncparamsArray[3] = (byte) 0x00; //show status (show)
        syncparamsArray[4] = (byte) 0x00; //wake up (type)
        syncparamsArray[5] = (byte) 0x00;
        syncparamsArray[6] = (byte) 0x00;

        syncparamsArray[7] = (byte) 0x00; // 0 is OFF
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

        sendMessage(syncparamsArray);
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


    protected void sendSetGoals(int steps, int sleepHour, int sleepMinute) {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x03;
        syncparamsArray[1] = (byte) 0x03;

        // type (0x00:step， 0x01：calorie， 0x02：distance)
        syncparamsArray[2] = (byte) 0x00;

        syncparamsArray[3] = (byte) (steps & 0xFF);
        syncparamsArray[4] = (byte) ((steps >> 8) & 0xFF);
        syncparamsArray[5] = (byte) ((steps >> 16) & 0xFF);
        syncparamsArray[6] = (byte) ((steps >> 24) & 0xFF);

        syncparamsArray[7] = (byte) sleepHour;
        syncparamsArray[8] = (byte) sleepMinute;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[setGoals] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendSetUserInformation(int userHeight, int userWeight, int userGender, long userBirthday) {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x03;
        syncparamsArray[1] = (byte) 0x10;

        syncparamsArray[2] = (byte) userHeight;

        syncparamsArray[3] = (byte) (userWeight * 100 & 0xFF);
        syncparamsArray[4] = (byte) ((userWeight * 100 >> 8) & 0xFF);

        syncparamsArray[5] = (byte) userGender;


        Date d = new Date(userBirthday);

        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        syncparamsArray[6] = (byte) (cal.get(Calendar.YEAR) & 0xFF);
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


        String weekDays = "1"; // Enabled
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


    protected void sendDisableBraceletSedentaryAlarm() {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x03;
        syncparamsArray[1] = (byte) 0x20;

        syncparamsArray[7] = (byte) 0; // All 0 is disabled

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
        syncparamsArray[2] = (byte) 0x55; // ALL

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[configureACNSActivate] " + HexUtils.getStringToPrint(syncparamsArray));

        sendMessage(syncparamsArray);
    }


    protected void sendMessageReceived() {

        byte[] syncparamsArray = getEmptyArray();

        syncparamsArray[0] = (byte) 0x05;
        syncparamsArray[1] = (byte) 0x03;

        syncparamsArray[2] = (byte) 0x01; // total packages
        syncparamsArray[3] = (byte) 0x01; // package number

        syncparamsArray[4] = (byte) 0x01; // 1 = SMS

        syncparamsArray[5] = (byte) 0x00; // length of information content
        syncparamsArray[6] = (byte) 0x00; // length of telephone number
        syncparamsArray[7] = (byte) 0x00; // length of contact

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendMessageReceived] " + HexUtils.getStringToPrint(syncparamsArray));

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

            // Switch selectively
            syncparamsArray[2] = (byte) 0x88;
        } else {
            // Switch off
            syncparamsArray[2] = (byte) 0xAA;
        }

        if (appNotification.isSms()) {
            syncparamsArray[3] = (byte) (syncparamsArray[3] | 0b00000010);
        }

        if (appNotification.isEmail()) {
            syncparamsArray[3] = (byte) (syncparamsArray[3] | 0b00000100);
        }

        if (appNotification.isWechat()) {
            syncparamsArray[3] = (byte) (syncparamsArray[3] | 0b00001000);
        }

        if (appNotification.isQq()) {
            syncparamsArray[3] = (byte) (syncparamsArray[3] | 0b00010000);
        }

        if (appNotification.isWeibo()) {
            syncparamsArray[3] = (byte) (syncparamsArray[3] | 0b00100000);
        }

        if (appNotification.isFacebook()) {
            syncparamsArray[3] = (byte) (syncparamsArray[3] | 0b01000000);
        }

        if (appNotification.isTwitter()) {
            syncparamsArray[3] = (byte) (syncparamsArray[3] | 0b10000000);
        }

        if (appNotification.isWhatsApp()) {
            syncparamsArray[4] = (byte) (syncparamsArray[4] | 0b00000001);
        }

        if (appNotification.isMessenger()) {
            syncparamsArray[4] = (byte) (syncparamsArray[4] | 0b00000010);
        }

        if (appNotification.isInstagram()) {
            syncparamsArray[4] = (byte) (syncparamsArray[4] | 0b00000100);
        }

        if (appNotification.isLinkedIn()) {
            syncparamsArray[4] = (byte) (syncparamsArray[4] | 0b00001000);
        }

        if (appNotification.isCalendar()) {
            syncparamsArray[4] = (byte) (syncparamsArray[4] | 0b00010000);
        }

        if (appNotification.isSkype()) {
            syncparamsArray[4] = (byte) (syncparamsArray[4] | 0b00100000);
        }

        if (appNotification.isAlarm()) {
            syncparamsArray[4] = (byte) (syncparamsArray[4] | 0b01000000);
        }

        if (appNotification.isPokeman()) {
            syncparamsArray[4] = (byte) (syncparamsArray[4] | 0b10000000);
        }

        syncparamsArray[5] = (byte) 0x55; // incoming call switch - 85???
        syncparamsArray[6] = (byte) 0x00; // incoming call delay (in seconds)

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

        //Flag bit (0x00: no function, 0x01: forced heart rate monitoring, 0x02: forced open blood pressure monitoring)
        // Este comando solo devuelve el pulso si el usuario tiene ese apartado abierto.
        syncparamsArray[2] = (byte) 0x00;

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

    // endregion --- Send commands to bracelet ---


}