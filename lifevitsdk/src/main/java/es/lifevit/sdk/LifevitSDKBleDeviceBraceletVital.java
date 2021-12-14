package es.lifevit.sdk;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import es.lifevit.sdk.bracelet.LifevitSDKDistanceTimeData;
import es.lifevit.sdk.bracelet.LifevitSDKHeartbeatData;
import es.lifevit.sdk.bracelet.LifevitSDKResponse;
import es.lifevit.sdk.bracelet.LifevitSDKSleepData;
import es.lifevit.sdk.bracelet.LifevitSDKStepData;
import es.lifevit.sdk.bracelet.LifevitSDKSummaryStepData;
import es.lifevit.sdk.bracelet.LifevitSDKTemperatureData;
import es.lifevit.sdk.bracelet.LifevitSDKVitalActivityPeriod;
import es.lifevit.sdk.bracelet.LifevitSDKVitalAlarm;
import es.lifevit.sdk.bracelet.LifevitSDKVitalECGConstantsData;
import es.lifevit.sdk.bracelet.LifevitSDKVitalECGStatus;
import es.lifevit.sdk.bracelet.LifevitSDKVitalECGWaveform;
import es.lifevit.sdk.bracelet.LifevitSDKVitalExerciseRecord;
import es.lifevit.sdk.bracelet.LifevitSDKVitalHRVData;
import es.lifevit.sdk.bracelet.LifevitSDKVitalNotification;
import es.lifevit.sdk.bracelet.LifevitSDKVitalPeriod;
import es.lifevit.sdk.bracelet.LifevitSDKVitalScreenNotification;
import es.lifevit.sdk.bracelet.LifevitSDKVitalWeather;
import es.lifevit.sdk.utils.ByteUtils;
import es.lifevit.sdk.utils.HexUtils;
import es.lifevit.sdk.utils.LogUtils;


/**
 * Created by aescanuela on 26/1/16.
 */
public class LifevitSDKBleDeviceBraceletVital extends LifevitSDKBleDevice {

    private int lastHealthConstant;

    public static class Constants {

        public static class Hand {
            public static final byte LEFT = (byte) 0x81;
            public static final byte RIGHT = (byte) 0x80;
        }

        //2025E COMANDS
        public static final byte DATA_SET_MILE = (byte) 0x81;
        public static final byte DATA_SET_KM = (byte) 0x80;
        public static final byte DATA_GET_MILE = (byte) 0x01;
        public static final byte DATA_GET_KM = (byte) 0x00;

        public static final byte DATA_SET_HOUR_12_DISPLAY = (byte) 0x81;
        public static final byte DATA_SET_HOUR_24_DISPLAY = (byte) 0x80;
        public static final byte DATA_GET_HOUR_12_DISPLAY = (byte) 0x01;
        public static final byte DATA_GET_HOUR_24_DISPLAY = (byte) 0x00;

        public static final byte DATA_SET_ENABLE = (byte) 0x81;
        public static final byte DATA_SET_DISABLE = (byte) 0x80;
        public static final byte DATA_GET_ENABLED = (byte) 0x01;
        public static final byte DATA_GET_DISABLED = (byte) 0x00;

        public static final byte DATA_SET_FAHRENHEIT = (byte) 0x81;
        public static final byte DATA_SET_CELSIUS = (byte) 0x80;
        public static final byte DATA_GET_FAHRENHEIT = (byte) 0x01;
        public static final byte DATA_GET_CELSIUS = (byte) 0x00;

        public static final byte DATA_SET_CHINESE = (byte) 0x81;
        public static final byte DATA_SET_ENGLISH = (byte) 0x80;
        public static final byte DATA_GET_CHINESE = (byte) 0x01;
        public static final byte DATA_GET_ENGLISH = (byte) 0x00;

        public static final byte DATA_OPERATION_DELETE = (byte) 0x99;
        public static final byte DATA_OPERATION_READ_MOST_RECENT = (byte) 0x00;
        public static final byte DATA_OPERATION_READ_SPECIFIED = (byte) 0x01;
        public static final byte DATA_OPERATION_NEXT = (byte) 0x02;

        public static class MediaControl {
            public static final byte START = (byte) 0x01;
            public static final byte PAUSE = (byte) 0x02;
            public static final byte CONTINUE = (byte) 0x03;
            public static final byte END = (byte) 0x04;
        }

        public static final byte MULTIMEDIA_CONTROL_PLAY = (byte) 0x01;
        public static final byte MULTIMEDIA_CONTROL_PAUSE = (byte) 0x00;
        public static final byte MULTIMEDIA_CONTROL_PREVIOUS = (byte) 0x02;
        public static final byte MULTIMEDIA_CONTROL_NEXT = (byte) 0x03;
        public static final byte MULTIMEDIA_CONTROL_VOLUME_DOWN = (byte) 0x04;
        public static final byte MULTIMEDIA_CONTROL_VOLUME_UP = (byte) 0x05;

        public static final byte FIRMWARE_COMMAND_CALL_OPERATION = (byte) 0x01;
        public static final byte FIRMWARE_COMMAND_TAKE_PICTURE = (byte) 0x02;
        public static final byte FIRMWARE_COMMAND_MUSIC_CONTROL = (byte) 0x03;
        public static final byte FIRMWARE_COMMAND_FIND_PHONE = (byte) 0x04;

        public static final byte HEALTH_MEASUREMENT_HRV = (byte) 0x01;
        public static final byte HEALTH_MEASUREMENT_HEARTRATE = (byte) 0x02;
        public static final byte HEALTH_MEASUREMENT_BLOODOXYGEN = (byte) 0x03;

        public static final byte UNLOCK_UNLOCK_CODE = (byte) 0x01;
        public static final byte UNLOCK_ENTER_BINDING_PAGE = (byte) 0x02;

        public static final int REQUEST_ECG_STATUS_MEASUREMENT_NOT_STARTED = 0;
        public static final int REQUEST_ECG_STATUS_MEASUREMENT_IN_PROGRESS = 1;
        public static final int REQUEST_ECG_STATUS_MEASUREMENT_TIME_OUT = 2;
        public static final int REQUEST_ECG_STATUS_MEASUREMENT_COMPLETED = 3;
        public static final int REQUEST_ECG_STATUS_MEASUREMENT_LOW_POWER = 4;
        public static final int REQUEST_ECG_STATUS_MEASUREMENT_CHARGING_OFF = 5;
        public static final int REQUEST_ECG_STATUS_MEASUREMENT_CLOSED_IN_ADVANCE = 6;
        public static final int REQUEST_ECG_STATUS_MEASUREMENT_CLOSED_FACTORY_RESET = 7;
        public static final int REQUEST_ECG_STATUS_MEASUREMENT_CLOSED_SPORT_MODE = 8;
        public static final int REQUEST_ECG_STATUS_MEASUREMENT_CLOSED_SOS_MODE = 9;
        public static final int REQUEST_ECG_STATUS_MEASUREMENT_WEAK_SIGNAL = 10;
        public static final int REQUEST_ECG_STATUS_MEASUREMENT_NO_SKIN_CONTACT = 11;
        public static final int REQUEST_ECG_STATUS_MEASUREMENT_SKIN_CONTACT = 12;
        public static final int REQUEST_ECG_STATUS_MEASUREMENT_CALIBRATION_COMPLETE = 13;
        public static final int REQUEST_ECG_STATUS_MEASUREMENT_CALIBRATION_FAILED = 14;
        public static final int REQUEST_ECG_STATUS_MEASUREMENT_DONT_MOVE_ALERT = 255;

        public static final byte REQUEST_SET_TIME = (byte) 0x01;
        public static final byte REQUEST_GET_TIME = (byte) 0x41;
        public static final byte REQUEST_SET_USER_PERSONAL_INFORMATION = (byte) 0x02;
        public static final byte REQUEST_GET_USER_PERSONAL_INFORMATION = (byte) 0x42;
        public static final byte REQUEST_SET_DEVICE_PARAMETERS = (byte) 0x03;
        public static final byte REQUEST_GET_DEVICE_PARAMETERS = (byte) 0x04;
        public static final byte REQUEST_SET_DEVICE_NEW_PARAMETERS = (byte) 0x06;
        public static final byte REQUEST_GET_DEVICE_NEW_PARAMETERS = (byte) 0x07;
        public static final byte REQUEST_SET_REALTIME_STEP_COUNTING = (byte) 0x09;
        public static final byte REQUEST_GET_BLOOD_OXYGEN_DATA = (byte) 0x60;
        public static final byte REQUEST_GET_AUTOMATIC_BLOOD_OXYGEN_DATA = (byte) 0x66;
        public static final byte REQUEST_GET_TEMPERATURE_DATA = (byte) 0x62;
        public static final byte REQUEST_GET_AUTOMATIC_TEMPERATURE_DATA = (byte) 0x65;
        public static final byte REQUEST_GET_AUTOMATIC_HEART_RATE_DATA = (byte) 0x54;
        public static final byte REQUEST_GET_HEART_RATE_DATA = (byte) 0x55;
        public static final byte REQUEST_GET_ECG_START_DATA_UPLOADING = (byte) 0x99;
        public static final byte REQUEST_GET_ECG_STOP_DATA_UPLOADING = (byte) 0x98;
        public static final byte REQUEST_GET_ECG_MEASUREMENT_STATUS = (byte) 0x9C;
        public static final byte REQUEST_GET_ECG_MEASUREMENT = (byte) 0xAA;
        public static final byte REQUEST_GET_ECG_WAVEFORM_SAVED = (byte) 0x71;
        public static final byte REQUEST_SET_AUTOMATIC_BLOOD_OXYGEN_DETECTION = (byte) 0x29;
        public static final byte REQUEST_SET_AUTOMATIC_DETECTION = (byte) 0x2A;
        public static final byte REQUEST_GET_AUTOMATIC_DETECTION = (byte) 0x2B;
        public static final byte REQUEST_GET_HRV_DATA = (byte) 0x56;
        public static final byte REQUEST_GET_TOTAL_STEPS_DATA = (byte) 0x51;
        public static final byte REQUEST_GET_DETAILED_STEPS_DATA = (byte) 0x52;
        public static final byte REQUEST_GET_DETAILED_SLEEP_DATA = (byte) 0x53;
        public static final byte REQUEST_SET_ACTIVITY_PERIOD = (byte) 0x25;
        public static final byte REQUEST_GET_ACTIVITY_PERIOD = (byte) 0x26;
        public static final byte REQUEST_SPORT_MODE_CONTROL_ENABLE = (byte) 0x19;
        public static final byte REQUEST_APP_HEART_BEAT_PACKET = (byte) 0x17;
        public static final byte REQUEST_BRACELET_HEART_BEAT_PACKET = (byte) 0x18;
        public static final byte REQUEST_BRACELET_HEALTH_MEASUREMENT_CONTROL = (byte) 0x28;
        public static final byte REQUEST_FIRMWARE_COMMAND = (byte) 0x16;
        public static final byte REQUEST_SOS_FUNCTION = (byte) 0xFE;
        public static final byte REQUEST_GET_DEVICE_BATTERY = (byte) 0x13;
        public static final byte REQUEST_UNLOCK_QR_CODE = (byte) 0xB0;
        public static final byte REQUEST_GET_SPORTS_DATA = (byte) 0x5C;
        public static final byte REQUEST_SET_TARGET_STEPS = (byte) 0x0B;
        public static final byte REQUEST_GET_TARGET_STEPS = (byte) 0x4B;
        public static final byte REQUEST_GET_MAC_ADDRESS = (byte) 0x22;
        public static final byte REQUEST_SET_ALARMS = (byte) 0x23;
        public static final byte REQUEST_SCREEN_NOTIF = (byte) 0x4D;
        public static final byte REQUEST_SET_WEATHER = (byte) 0x15;
        public static final byte REQUEST_GET_ECG_MEASUREMENT_HEART_RATE = (byte) 0x83;


        public static final byte ERROR_SET_TIME = (byte) 0x81;
        public static final byte ERROR_GET_TIME = (byte) 0xC1;
        public static final byte ERROR_SET_USER_PERSONAL_INFORMATION = (byte) 0x82;
        public static final byte ERROR_GET_USER_PERSONAL_INFORMATION = (byte) 0xC2;
        public static final byte ERROR_SET_DEVICE_PARAMETERS = (byte) 0x83;
        public static final byte ERROR_GET_DEVICE_PARAMETERS = (byte) 0x84;
        public static final byte ERROR_SET_DEVICE_NEW_PARAMETERS = (byte) 0x86;
        public static final byte ERROR_GET_DEVICE_NEW_PARAMETERS = (byte) 0x87;
        public static final byte ERROR_SET_REALTIME_STEP_COUNTING = (byte) 0x89;
        public static final byte ERROR_SET_AUTOMATIC_BLOOD_OXYGEN_DETECTION = (byte) 0xAA;
        public static final byte ERROR_GET_AUTOMATIC_HEART_RATE_DETECTION = (byte) 0xAB;
        public static final byte ERROR_SET_ACTIVITY_PERIOD = (byte) 0xA5;
        public static final byte ERROR_GET_ACTIVITY_PERIOD = (byte) 0xA6;
        public static final byte ERROR_GET_DEVICE_BATTERY = (byte) 0x93;
        public static final byte ERROR_SET_TARGET_STEPS = (byte) 0x8B;
        public static final byte ERROR_GET_TARGET_STEPS = (byte) 0xCB;
        public static final byte ERROR_GET_MAC_ADDRESS = (byte) 0xA2;

        public static final byte WORKING_MODE_OFF = 0x00;
        public static final byte WORKING_MODE_TIME_PERIOD = 0x01;
        public static final byte WORKING_MODE_TIME_INTERVAL = 0x02;
    }


    private final static String CLASS_TAG = LifevitSDKBleDeviceBraceletVital.class.getSimpleName();

    private static final String DEVICE_NAME = "J2025E";
    private static final String DEVICE_SECOND_NAME = "J2025E";

    private ArrayList<LifevitSDKHeartbeatData> heartRateDataArray = new ArrayList<>();
    private ArrayList<LifevitSDKStepData> stepDataArray = new ArrayList<>();
    private ArrayList<LifevitSDKSleepData> sleepDataArray = new ArrayList<>();
    private ArrayList<LifevitSDKOximeterData> oximeterDataArray = new ArrayList<>();
    private ArrayList<LifevitSDKTemperatureData> temperatureDataArray = new ArrayList<>();
    private ArrayList<LifevitSDKSummaryStepData> sportsDataArray = new ArrayList<>();
    private ArrayList<LifevitSDKVitalHRVData> vitalsArray = new ArrayList<>();
    private LifevitSDKVitalECGWaveform ecgWaveformData = null;

    private LifevitSDKVitalParams parameters = new LifevitSDKVitalParams();
    private boolean pendingToUpdateParams = true;

    private boolean isSyncronizingSteps = false;

    private byte currentRequestedCommand = 0x00;
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

    static final class Action {

        static final int SHOW_QR = 0, SET_TIME = 1, GET_TIME = 2, SET_USER_INFO = 3, GET_USER_INFO = 4, SET_DEVICE_PARAMS = 5, GET_DEVICE_PARAMS = 6, GET_MAC_ADDRESS = 7, SET_STEP_GOAL = 8,
                GET_STEP_GOAL = 9, GET_DEVICE_BATTERY = 10, SET_DEVICE_NEW_PARAMS = 11, GET_DEVICE_NEW_PARAMS = 37,
                GET_BLOOD_OXY = 12, GET_BLOOD_OXY_AUTO = 13, SET_OXY_PERIOD = 14, GET_OXY_PERIOD = 15,
                GET_TEMPERATURE = 16, GET_TEMPERATURE_AUTO = 17, GET_HR = 18, GET_HR_PERIOD = 19, GET_HR_AUTO = 20,
                ECG_START = 21, ECG_STATUS = 22, ECG_WAVEFORM = 23, GET_HRV = 24,
                SET_REALTIME = 25, GET_STEPS = 26, STEP_SYNC = 27, SLEEP_SYNC = 28,
                SET_ACTIVITY_PERIOD = 29, GET_ACTIVITY_PERIOD = 30, SET_SPORT_MODE = 31,
                HRV_START = 32, HR_START = 33, OXY_START = 34, GET_SPORT_DATA = 35, SET_HR_PERIOD = 36,
                SET_ALARMS = 38, SET_REMINDERS = 39, SET_WEATHER = 40, GET_TEMPERATURE_PERIOD = 41,
                SET_TEMPERATURE_PERIOD = 42,
                ECG_STOP = 44,
                SPORT_HEATBEAT_PACKET = 46;
    }
    // region --- Delegate methods ---

    private void sendSuccessfulCommand(LifevitSDKConstants.BraceletVitalCommand command, boolean release) {
        sendSuccessfulCommand(command, null, null, release);
    }

    private void sendSuccessfulCommandWithData(LifevitSDKConstants.BraceletVitalCommand command, Object data, boolean release) {
        sendSuccessfulCommand(command, null, data, release);
    }

    private void sendSuccessfulCommandWithType(LifevitSDKConstants.BraceletVitalCommand command, LifevitSDKConstants.BraceletVitalDataType type, boolean release) {
        sendSuccessfulCommand(command, type, null, release);
    }


    private void sendSuccessfulCommand(LifevitSDKConstants.BraceletVitalCommand command, LifevitSDKConstants.BraceletVitalDataType type, Object data, boolean release) {
        LifevitSDKResponse response = new LifevitSDKResponse(command, type, data);

        String deviceAddress = "";
        if (mBluetoothDevice != null) {
            deviceAddress = mBluetoothDevice.getAddress();
        }

        if (this.mLifevitSDKManager.getBraceletVitalListener() != null) {
            this.mLifevitSDKManager.getBraceletVitalListener().braceletVitalInformation(deviceAddress, response);
        }

        if (release && sendingThread != null) {
            sendingThread.taskFinished();
        }
    }

    private void sendSOS() {
        if (this.mLifevitSDKManager.getBraceletVitalListener() != null) {
            this.mLifevitSDKManager.getBraceletVitalListener().braceletVitalSOS(mBluetoothDevice.getAddress());
        }
    }

    private void sendError(LifevitSDKConstants.BraceletVitalError error, LifevitSDKConstants.BraceletVitalCommand command) {
        if (this.mLifevitSDKManager.getBraceletVitalListener() != null) {
            this.mLifevitSDKManager.getBraceletVitalListener().braceletVitalError(mBluetoothDevice.getAddress(), error, command);
        }
    }

    // endregion

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
        return name != null && name.toLowerCase().contains(DEVICE_NAME.toLowerCase());
    }

    protected static boolean matchDevice(BluetoothDevice device) {
        return matchDevice(device.getName());
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

    public void showQR(boolean show) {
        sendingThread.addToQueue(Action.SHOW_QR, show);
    }

    public void setRealtimeCounting(boolean stepsEnabled, boolean temperatureEnabled) {
        sendingThread.addToQueue(Action.SET_REALTIME, stepsEnabled, temperatureEnabled);
    }

    public void getTime() {
        sendingThread.addToQueue(Action.GET_TIME);
    }

    public void setTime(long date) {
        sendingThread.addToQueue(Action.SET_TIME, date);
    }

    public void getBattery() {
        sendingThread.addToQueue(Action.GET_DEVICE_BATTERY);
    }

    public void getMACAddress() {
        sendingThread.addToQueue(Action.GET_MAC_ADDRESS);
    }

    public void getUserInformation() {
        sendingThread.addToQueue(Action.GET_USER_INFO);
    }

    public void updateUserInformation(LifevitSDKUserData data) {
        sendingThread.addToQueue(Action.SET_USER_INFO, data);
    }

    public void updateParamaters(LifevitSDKVitalParams data) {
        if (pendingToUpdateParams) {
            // Encolamos primero el getParams
            getParameters();
        }
        sendingThread.addToQueue(Action.SET_DEVICE_PARAMS, data);
        sendingThread.addToQueue(Action.SET_DEVICE_NEW_PARAMS, data);
    }

    public void getParameters() {
        sendingThread.addToQueue(Action.GET_DEVICE_PARAMS);
        sendingThread.addToQueue(Action.GET_DEVICE_NEW_PARAMS);
    }

    public void setTargetSteps(int steps) {
        sendingThread.addToQueue(Action.SET_STEP_GOAL, steps);
    }

    public void getTargetSteps() {
        sendingThread.addToQueue(Action.GET_STEP_GOAL);
    }

    public void getSteps() {
        sendingThread.addToQueue(Action.GET_STEPS);
    }

    public void getDetailedSteps() {
        sendingThread.addToQueue(Action.STEP_SYNC);
    }

    public void getDetailedSleep() {
        sendingThread.addToQueue(Action.SLEEP_SYNC);
    }

    public void getActivityPeriod() {
        sendingThread.addToQueue(Action.GET_ACTIVITY_PERIOD);
    }

    public void setActivityPeriod(LifevitSDKVitalActivityPeriod data) {
        sendingThread.addToQueue(Action.SET_ACTIVITY_PERIOD, data);
    }

    public void setSportsMode(Integer mode, LifevitSDKConstants.BraceletVitalSportType sport, LifevitSDKConstants.BraceletVitalMeditationLevel level, Integer period) {
        sendingThread.clearQueue();
        sendingThread.addToQueue(Action.SET_SPORT_MODE, mode, sport != null ? sport.value : null, level != null ? level.value : null, period);
    }

    public void setBloodPressurePeriod(LifevitSDKVitalPeriod data) {
        sendingThread.addToQueue(Action.SET_OXY_PERIOD, data);
    }

    public void getBloodPressurePeriod() {
        sendingThread.addToQueue(Action.GET_OXY_PERIOD);
    }

    public void setHeartRatePeriod(LifevitSDKVitalPeriod data) {
        sendingThread.addToQueue(Action.SET_HR_PERIOD, data);
    }

    public void getHeartRatePeriod() {
        sendingThread.addToQueue(Action.GET_HR_PERIOD);
    }

    public void setTemperaturePeriod(LifevitSDKVitalPeriod data) {
        sendingThread.addToQueue(Action.SET_TEMPERATURE_PERIOD, data);
    }

    public void getTemperaturePeriod() {
        sendingThread.addToQueue(Action.GET_TEMPERATURE_PERIOD);
    }

    public void getOximeterData() {
        sendingThread.addToQueue(Action.GET_BLOOD_OXY);
    }

    public void getOximeterPeriodicData() {
        sendingThread.addToQueue(Action.GET_BLOOD_OXY_AUTO);
    }

    public void getTemperatureData() {
        sendingThread.addToQueue(Action.GET_TEMPERATURE);
    }

    public void getTemperaturePeriodicData() {
        sendingThread.addToQueue(Action.GET_TEMPERATURE_AUTO);
    }

    public void getHeartRateData() {
        sendingThread.clearQueue();
        sendingThread.addToQueue(Action.GET_HR);
    }

    public void getHeartRatePeriodicData() {
        sendingThread.addToQueue(Action.GET_HR_AUTO);
    }

    public void getVitals() {
        sendingThread.addToQueue(Action.GET_HRV);
    }

    public void startECG() {
        resetFilterData();
        sendingThread.clearQueue();
        sendingThread.addToQueue(Action.ECG_START);
    }

    public void getECGStatus() {
        sendingThread.addToQueue(Action.ECG_STATUS);
    }

    public void getECGWaveform() {
        sendingThread.addToQueue(Action.ECG_WAVEFORM);
    }

    public void startOxymeter() {
        sendingThread.clearQueue();
        sendingThread.addToQueue(Action.OXY_START, true);
    }

    public void stopOxymeter() {
        sendingThread.addToQueue(Action.OXY_START, false);
    }

    public void startHeartRate() {
        sendingThread.addToQueue(Action.HR_START, true);
    }

    public void stopHeartRate() {
        sendingThread.addToQueue(Action.HR_START, false);
    }

    public void startVital() {
        sendingThread.addToQueue(Action.HRV_START, true);
    }

    public void stopVital() {
        sendingThread.addToQueue(Action.HRV_START, false);
    }

    public void getSportsData() {
        sendingThread.addToQueue(Action.GET_SPORT_DATA);
    }

    public void setAlarms(List<LifevitSDKVitalAlarm> data) {
        sendingThread.addToQueue(Action.SET_ALARMS, data);
    }

    public void setNotification(LifevitSDKVitalScreenNotification data) {
        sendingThread.addToQueue(Action.SET_REMINDERS, data);
    }

    public void setWeather(LifevitSDKVitalWeather data) {
        sendingThread.addToQueue(Action.SET_WEATHER, data);
    }

    public void setSportsAppHeartbeatPacket(Float distance, Integer paceSeconds, LifevitSDKConstants.BraceletVitalGPSStrengh gpsSignal) {
        sendingThread.clearQueue();
        sendingThread.addToQueue(Action.SPORT_HEATBEAT_PACKET, distance, paceSeconds, gpsSignal.value);
    }


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

        //Guardamos el comando actual...
        currentRequestedCommand = data[0];

        dividePacketsAndSendMessage(RxChar, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
    }


    // region --- Processing responses methods ---
    protected void characteristicReadProcessData(BluetoothGattCharacteristic characteristic) {

        LogUtils.log(Log.DEBUG, CLASS_TAG, "RECEIVED from characteristic: " + characteristic.getUuid().toString());

        if (characteristic.getUuid().equals(UUID.fromString(UUID_NOTIFY_CHARACTERISTIC_READ))) {

            byte[] rx = characteristic.getValue();

            String resultsStr = new String(rx);

            LogUtils.log(Log.DEBUG, CLASS_TAG, "characteristicReadProcessData - RECEIVED (byte format): " + HexUtils.getStringToPrint(rx));

            byte command = rx[0];
            if (command == Constants.REQUEST_GET_TIME) {
                processGetDeviceDatetime(rx);
            } else if (command == Constants.REQUEST_GET_DEVICE_BATTERY) {
                processGetBatteryLevel(rx);
            } else if (command == Constants.REQUEST_GET_MAC_ADDRESS) {
                processGetMAC(rx);
            } else if (command == Constants.REQUEST_GET_USER_PERSONAL_INFORMATION) {
                processGetUserInformation(rx);
            } else if (command == Constants.REQUEST_GET_DEVICE_PARAMETERS) {
                processGetParameters(rx);
            } else if (command == Constants.REQUEST_GET_DEVICE_NEW_PARAMETERS) {
                processGetNewParameters(rx);
            } else if (command == Constants.REQUEST_GET_TARGET_STEPS) {
                processGetTargetSteps(rx);
            } else if (command == Constants.REQUEST_GET_TOTAL_STEPS_DATA) {
                processGetDaySteps(rx);
            } else if (command == Constants.REQUEST_GET_ACTIVITY_PERIOD) {
                processGetActivityPeriod(rx);
            } else if (command == Constants.REQUEST_GET_AUTOMATIC_BLOOD_OXYGEN_DATA) {
                processGetOxymeterPeriodicData(rx);
            } else if (command == Constants.REQUEST_GET_BLOOD_OXYGEN_DATA) {
                processGetOxymeterData(rx);
            } else if (command == Constants.REQUEST_GET_AUTOMATIC_HEART_RATE_DATA) {
                processGetHeartRatePeriodicData(rx);
            } else if (command == Constants.REQUEST_GET_HEART_RATE_DATA) {
                processGetHeartRateData(rx);
            } else if (command == Constants.REQUEST_GET_AUTOMATIC_TEMPERATURE_DATA) {
                processGetTemperaturePeriodicData(rx);
            } else if (command == Constants.REQUEST_GET_TEMPERATURE_DATA) {
                processGetTemperatureData(rx);
            } else if (command == Constants.REQUEST_GET_DETAILED_STEPS_DATA) {
                processGetDetailedSteps(rx);
            } else if (command == Constants.REQUEST_GET_DETAILED_SLEEP_DATA) {
                processGetDetailedSleep(rx);
            } else if (command == Constants.REQUEST_GET_HRV_DATA) {
                processGetVitalsData(rx);
            } else if (command == Constants.REQUEST_GET_SPORTS_DATA) {
                processGetSportsData(rx);
            } else if (command == Constants.REQUEST_SET_REALTIME_STEP_COUNTING) {
                processRealtimeData(rx);
            } else if (command == Constants.REQUEST_GET_ECG_MEASUREMENT) {
                processECGMeasurementData(rx);
            } else if (command == Constants.REQUEST_GET_ECG_MEASUREMENT_STATUS) {
                processECGStatusData(rx);
            } else if (command == Constants.REQUEST_GET_ECG_START_DATA_UPLOADING) {
                processECGStart(rx);
            } else if (command == Constants.REQUEST_GET_ECG_WAVEFORM_SAVED) {
                processECGWaveformData(rx);
            } /*else if (command == Constants.REQUEST_SET_AUTOMATIC_BLOOD_OXYGEN_DETECTION) {
                processAutomaticOxygenPeriod(rx);
            } */ else if (command == Constants.REQUEST_SET_AUTOMATIC_DETECTION) {
                sendSuccessfulCommand(getActionForCommand(command), true);
            } else if (command == Constants.REQUEST_GET_AUTOMATIC_DETECTION) {
                processAutomaticPeriod(rx);
            } else if (command == Constants.REQUEST_BRACELET_HEALTH_MEASUREMENT_CONTROL) {
                processHealthMeasurementControlResponse(rx);
            } else if (command == Constants.REQUEST_FIRMWARE_COMMAND) {
                processBraceletCommand(rx);
            } else if (command == Constants.REQUEST_BRACELET_HEART_BEAT_PACKET) {
                processHeartBeatPacket(rx);
            } else if (command == Constants.REQUEST_APP_HEART_BEAT_PACKET) {
                processAppHeartbeatCommand(rx);
            } else if (command == Constants.REQUEST_SOS_FUNCTION) {
                sendSOS();
            } else if (command == Constants.ERROR_GET_MAC_ADDRESS) {
                sendError(LifevitSDKConstants.BraceletVitalError.ERROR_SENDING_COMMAND, getActionForCommand(command));
            } else if (command == Constants.REQUEST_GET_ECG_MEASUREMENT_HEART_RATE) {
                processGetECGHeartRateData(rx);
            } else if (command == Constants.REQUEST_SPORT_MODE_CONTROL_ENABLE) {
                processAppSportModeControlEnable(rx);
            } else {
                LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> COMMAND COMPLETED: " + command);
                sendSuccessfulCommand(getActionForCommand(command), true);
            }

        }
    }

    public LifevitSDKConstants.BraceletVitalCommand getActionForCommand(byte command) {

        switch (command) {

            case Constants.REQUEST_GET_ACTIVITY_PERIOD:
                return LifevitSDKConstants.BraceletVitalCommand.GET_ACTIVITY_PERIOD;
            case Constants.REQUEST_GET_AUTOMATIC_BLOOD_OXYGEN_DATA:
                return LifevitSDKConstants.BraceletVitalCommand.GET_OXY_AUTO;
            case Constants.REQUEST_GET_AUTOMATIC_DETECTION:
                return LifevitSDKConstants.BraceletVitalCommand.GET_AUTOMATIC_PERIOD;
            case Constants.REQUEST_GET_DEVICE_BATTERY:
                return LifevitSDKConstants.BraceletVitalCommand.GET_BATTERY;
            case Constants.REQUEST_GET_AUTOMATIC_TEMPERATURE_DATA:
                return LifevitSDKConstants.BraceletVitalCommand.GET_TEMPERATURE_AUTO;
            case Constants.REQUEST_GET_BLOOD_OXYGEN_DATA:
                return LifevitSDKConstants.BraceletVitalCommand.GET_OXY;
            case Constants.REQUEST_GET_DETAILED_SLEEP_DATA:
                return LifevitSDKConstants.BraceletVitalCommand.GET_SLEEP;
            case Constants.REQUEST_GET_DETAILED_STEPS_DATA:
                return LifevitSDKConstants.BraceletVitalCommand.GET_STEPS;
            case Constants.REQUEST_GET_DEVICE_NEW_PARAMETERS:
            case Constants.REQUEST_GET_DEVICE_PARAMETERS:
                return LifevitSDKConstants.BraceletVitalCommand.GET_DEVICE_PARAMETERS;
            case Constants.REQUEST_GET_ECG_MEASUREMENT:
                return LifevitSDKConstants.BraceletVitalCommand.GET_ECG_DATA;
            case Constants.REQUEST_GET_ECG_MEASUREMENT_STATUS:
                return LifevitSDKConstants.BraceletVitalCommand.GET_ECG_STATUS;
            case Constants.REQUEST_GET_ECG_START_DATA_UPLOADING:
                return LifevitSDKConstants.BraceletVitalCommand.START_ECG;
            case Constants.REQUEST_GET_ECG_WAVEFORM_SAVED:
                return LifevitSDKConstants.BraceletVitalCommand.GET_ECG_WAVEFORM;
            case Constants.REQUEST_GET_AUTOMATIC_HEART_RATE_DATA:
                return LifevitSDKConstants.BraceletVitalCommand.GET_HR_AUTO;
            case Constants.REQUEST_GET_HRV_DATA:
                return LifevitSDKConstants.BraceletVitalCommand.GET_VITALS;
            case Constants.REQUEST_GET_MAC_ADDRESS:
                return LifevitSDKConstants.BraceletVitalCommand.GET_MAC_ADDRESS;
            case Constants.REQUEST_GET_HEART_RATE_DATA:
                return LifevitSDKConstants.BraceletVitalCommand.GET_HR;
            case Constants.REQUEST_GET_SPORTS_DATA:
                return LifevitSDKConstants.BraceletVitalCommand.GET_SPORTS;
            case Constants.REQUEST_GET_TARGET_STEPS:
                return LifevitSDKConstants.BraceletVitalCommand.GET_STEPS_GOAL;
            case Constants.REQUEST_GET_TEMPERATURE_DATA:
                return LifevitSDKConstants.BraceletVitalCommand.GET_TEMPERATURE;
            case Constants.REQUEST_GET_TIME:
                return LifevitSDKConstants.BraceletVitalCommand.GET_TIME;
            case Constants.REQUEST_GET_TOTAL_STEPS_DATA:
                return LifevitSDKConstants.BraceletVitalCommand.GET_TOTAL_STEPS;
            case Constants.REQUEST_GET_USER_PERSONAL_INFORMATION:
                return LifevitSDKConstants.BraceletVitalCommand.GET_USER_INFO;
            case Constants.REQUEST_SPORT_MODE_CONTROL_ENABLE:
                return LifevitSDKConstants.BraceletVitalCommand.START_SPORT;
            case Constants.REQUEST_SET_ACTIVITY_PERIOD:
                return LifevitSDKConstants.BraceletVitalCommand.SET_ACTIVITY_PERIOD;
            case Constants.REQUEST_SET_ALARMS:
                return LifevitSDKConstants.BraceletVitalCommand.SET_ALARMS;
            case Constants.REQUEST_SET_AUTOMATIC_BLOOD_OXYGEN_DETECTION:
            case Constants.REQUEST_SET_AUTOMATIC_DETECTION:
                return LifevitSDKConstants.BraceletVitalCommand.SET_AUTOMATIC_PERIOD;
            case Constants.REQUEST_SET_REALTIME_STEP_COUNTING:
                return LifevitSDKConstants.BraceletVitalCommand.SET_REALTIME;
            case Constants.REQUEST_SET_DEVICE_NEW_PARAMETERS:
            case Constants.REQUEST_SET_DEVICE_PARAMETERS:
                return LifevitSDKConstants.BraceletVitalCommand.SET_DEVICE_PARAMETERS;
            case Constants.REQUEST_SET_TARGET_STEPS:
                return LifevitSDKConstants.BraceletVitalCommand.SET_STEPS_GOAL;
            case Constants.REQUEST_SET_TIME:
                return LifevitSDKConstants.BraceletVitalCommand.SET_TIME;
            case Constants.REQUEST_SET_USER_PERSONAL_INFORMATION:
                return LifevitSDKConstants.BraceletVitalCommand.SET_USER_INFO;
            case Constants.REQUEST_SET_WEATHER:
                return LifevitSDKConstants.BraceletVitalCommand.SET_WEATHER;
            case Constants.REQUEST_GET_ECG_MEASUREMENT_HEART_RATE:
                return LifevitSDKConstants.BraceletVitalCommand.ECG_MEASUREMENT_HEART_RATE;
            case Constants.REQUEST_BRACELET_HEART_BEAT_PACKET:
                return LifevitSDKConstants.BraceletVitalCommand.ECG_HEARTBEAT_PACKET;
            case Constants.REQUEST_BRACELET_HEALTH_MEASUREMENT_CONTROL:
                switch (lastHealthConstant) {
                    case Constants.HEALTH_MEASUREMENT_HRV:
                        return LifevitSDKConstants.BraceletVitalCommand.GET_VITALS_MEASURE;
                    case Constants.HEALTH_MEASUREMENT_HEARTRATE:
                        return LifevitSDKConstants.BraceletVitalCommand.GET_HR_MEASURE;
                    case Constants.HEALTH_MEASUREMENT_BLOODOXYGEN:
                        return LifevitSDKConstants.BraceletVitalCommand.GET_OXY_MEASURE;
                }
        }

        return LifevitSDKConstants.BraceletVitalCommand.UNKNOWN;
    }

    private void processGetActivityPeriod(byte[] bytes) {
        String hexString = HexUtils.getStringToPrint(bytes);
        String[] hxBytes = hexString.split(":");

        LifevitSDKVitalActivityPeriod period = new LifevitSDKVitalActivityPeriod();
        period.setStartHour(Integer.parseInt(hxBytes[1]));
        period.setStartMinute(Integer.parseInt(hxBytes[2]));
        period.setEndHour(Integer.parseInt(hxBytes[3]));
        period.setEndMinute(Integer.parseInt(hxBytes[4]));

        byte weekEnableByte = bytes[5];

        int exerciseReminder = ByteUtils.toUnsignedInt(bytes[6]);
        int minimumNumberSteps = ByteUtils.toUnsignedInt(bytes[6]);
        boolean motionEnabled = ByteUtils.toUnsignedInt(bytes[6]) != 0;

        period.setExerciseReminderPeriod(exerciseReminder);
        period.setMinimumNumberSteps(minimumNumberSteps);
        period.setMotionEnabled(motionEnabled);
        period.setMonday((weekEnableByte & 0x01) == 1);
        period.setTuesday((weekEnableByte >> 1 & 0x01) == 1);
        period.setWednesday((weekEnableByte >> 2 & 0x01) == 1);
        period.setThursday((weekEnableByte >> 3 & 0x01) == 1);
        period.setFriday((weekEnableByte >> 4 & 0x01) == 1);
        period.setSaturday((weekEnableByte >> 5 & 0x01) == 1);
        period.setSunday((weekEnableByte >> 6 & 0x01) == 1);


        sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), period, true);
    }

    private void processGetDaySteps(byte[] bytes) {

        if (!isSyncronizingSteps) {
            return;
        }
        int index = 0;

        String hexString = HexUtils.getStringToPrint(bytes);
        String[] hxBytes = hexString.split(":");

        while (index + 1 < bytes.length) {
            int identifier = bytes[index + 1];
            boolean end = hxBytes[index + 1].equalsIgnoreCase("FF");

            if (end) {

                isSyncronizingSteps = false;

                sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), new ArrayList(this.stepDataArray), true);
                return;
            }


            LogUtils.log(Log.DEBUG, CLASS_TAG, "Step counter: " + identifier);

            int header1_date_year = Integer.parseInt(hxBytes[index + 2]) + 2000;
            int header1_date_month = Integer.parseInt(hxBytes[index + 3]);
            int header1_date_day = Integer.parseInt(hxBytes[index + 4]);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, header1_date_year);
            cal.set(Calendar.MONTH, header1_date_month - 1);
            cal.set(Calendar.DAY_OF_MONTH, header1_date_day);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            Long date = cal.getTimeInMillis();

            byte[] bSteps = {bytes[index + 5], bytes[index + 6], bytes[index + 7], bytes[index + 8]};
            int steps = ByteUtils.bytesToInt(bSteps);
            byte[] bExercise = {bytes[index + 9], bytes[index + 10], bytes[index + 11], bytes[index + 12]};
            int exercise = ByteUtils.bytesToInt(bExercise);
            byte[] bDistance = {bytes[index + 13], bytes[index + 14], bytes[index + 15], bytes[index + 16]};
            float distance = ByteUtils.bytesToInt(bDistance) / 100.0f;
            byte[] bCalories = {bytes[index + 17], bytes[index + 18], bytes[index + 19], bytes[index + 20]};
            float calories = ByteUtils.bytesToInt(bCalories) / 100.0f;
            byte[] bTarget = {bytes[index + 21], bytes[index + 22], 0x00, 0x00};
            int target = ByteUtils.bytesToInt(bTarget);

            byte[] bRapidMovement = {bytes[index + 23], bytes[index + 24], bytes[index + 25], bytes[index + 26]};
            //minutes...
            int rapidMovement = ByteUtils.bytesToInt(bRapidMovement);


            LifevitSDKStepData stepData = new LifevitSDKStepData(date, steps, calories, distance);

            stepData.setActiveTime((long) exercise);
            stepData.setActiveFastTime((long) rapidMovement);

            LogUtils.debug("Step Data " + identifier + ": " + stepData.toString());

            this.stepDataArray.add(stepData);
            index += 27;

        }
        //sendGetTotalDaySteps(Constants.DATA_OPERATION_NEXT);


    }

    private void processGetSportsData(byte[] bytes) {

        int index = 0;

        String hexString = HexUtils.getStringToPrint(bytes);
        String[] hxBytes = hexString.split(":");

        while (index + 1 < bytes.length) {
            boolean end = (int) ByteUtils.toUnsignedInt(bytes[1]) == 255;

            if (end) {
                sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), new ArrayList(this.sportsDataArray), true);
                return;
            }

            int identifier = bytes[1];

            int header1_date_year = Integer.parseInt(hxBytes[3]) + 2000;
            int header1_date_month = Integer.parseInt(hxBytes[4]);
            int header1_date_day = Integer.parseInt(hxBytes[5]);
            int header1_date_hour = Integer.parseInt(hxBytes[6]);
            int header1_date_minute = Integer.parseInt(hxBytes[7]);
            int header1_date_second = Integer.parseInt(hxBytes[8]);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, header1_date_year);
            cal.set(Calendar.MONTH, header1_date_month - 1);
            cal.set(Calendar.DAY_OF_MONTH, header1_date_day);
            cal.set(Calendar.HOUR_OF_DAY, header1_date_hour);
            cal.set(Calendar.MINUTE, header1_date_minute);
            cal.set(Calendar.SECOND, header1_date_second);
            cal.set(Calendar.MILLISECOND, 0);

            Long date = cal.getTimeInMillis();

            int exercise = ByteUtils.toUnsignedInt(bytes[9]);
            int heartrate = ByteUtils.toUnsignedInt(bytes[10]);
            byte[] bMovement = {bytes[11], bytes[12], 0x00, 0x00};
            int movement = ByteUtils.bytesToInt(bMovement);
            byte[] bSteps = {bytes[13], bytes[14], 0x00, 0x00};
            int steps = ByteUtils.bytesToInt(bSteps);

            int minutes = ByteUtils.toUnsignedInt(bytes[15]);
            int seconds = ByteUtils.toUnsignedInt(bytes[16]);

            int time = minutes * 60 + seconds;

            byte[] bCalories = {bytes[17], bytes[18], bytes[19], bytes[20]};
            float calories = ByteUtils.bytesToInt(bCalories) / 100.0f;
            byte[] bDistance = {bytes[21], bytes[22], bytes[23], bytes[24]};
            float distance = ByteUtils.bytesToInt(bDistance) / 100.0f;

            LifevitSDKSummaryStepData stepData = new LifevitSDKSummaryStepData(date, steps, calories, distance, movement, heartrate);
            stepData.setActiveTime((long) time);
            stepData.setType(exercise);

            this.sportsDataArray.add(stepData);

            LogUtils.debug("Sport Data : " + stepData.toString());
            index += 25;
        }

        //sendGetSportsData(Constants.DATA_OPERATION_NEXT);

    }

    private void processBraceletCommand(byte[] bytes) {

        LifevitSDKConstants.BraceletVitalOperation operation = LifevitSDKConstants.BraceletVitalOperation.HANG_UP;

        int cmd = bytes[1];
        int subcmd = bytes[2];

        switch (cmd) {
            case 1:
                operation = LifevitSDKConstants.BraceletVitalOperation.HANG_UP;
                break;
            case 2:
                operation = LifevitSDKConstants.BraceletVitalOperation.PHOTO;
                break;
            case 3:
                switch (subcmd) {
                    case 0:
                        operation = LifevitSDKConstants.BraceletVitalOperation.MUSIC_PAUSE;
                        break;
                    case 1:
                        operation = LifevitSDKConstants.BraceletVitalOperation.MUSIC_PLAY;
                        break;
                    case 2:
                        operation = LifevitSDKConstants.BraceletVitalOperation.MUSIC_PREVIOUS;
                        break;
                    case 3:
                        operation = LifevitSDKConstants.BraceletVitalOperation.MUSIC_NEXT;
                        break;
                    case 4:
                        operation = LifevitSDKConstants.BraceletVitalOperation.MUSIC_VOLUME_DOWN;
                        break;
                    case 5:
                        operation = LifevitSDKConstants.BraceletVitalOperation.MUSIC_VOLUME_UP;
                        break;
                }
                break;
            case 4:
                operation = LifevitSDKConstants.BraceletVitalOperation.FIND_PHONE;
                break;
        }

        if (mLifevitSDKManager.getBraceletVitalListener() != null) {
            mLifevitSDKManager.getBraceletVitalListener().braceletVitalOperation(this.mBluetoothDevice.getAddress(), operation);
        }
    }

    private void processHealthMeasurementControlResponse(byte[] bytes) {

        int healthConstant = ByteUtils.toUnsignedInt(bytes[1]);
        int heartRate = ByteUtils.toUnsignedInt(bytes[2]);
        int bloodOxygen = ByteUtils.toUnsignedInt(bytes[3]);
        int hrv = ByteUtils.toUnsignedInt(bytes[4]);
        int fatigue = ByteUtils.toUnsignedInt(bytes[5]);
        int systolic = ByteUtils.toUnsignedInt(bytes[6]);
        int diastolic = ByteUtils.toUnsignedInt(bytes[7]);

        int type = healthConstant;
        if (type == 0) {
            type = lastHealthConstant;
        } else {
            lastHealthConstant = type;
        }

        switch (type) {
            case Constants.HEALTH_MEASUREMENT_HRV: {
                //if (heartRate > 0) {
                LifevitSDKVitalHRVData heartConstantsData = new LifevitSDKVitalHRVData();
                heartConstantsData.setHrv(hrv);
                heartConstantsData.setFatigue(fatigue);
                heartConstantsData.setSystolic(systolic);
                heartConstantsData.setDiastolic(diastolic);
                heartConstantsData.setHeartRate(heartRate);

                sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), heartConstantsData, false);

                //}
            }
            break;
            case Constants.HEALTH_MEASUREMENT_HEARTRATE: {
                //if (heartRate > 0) {
                LifevitSDKHeartbeatData heartRateData = new LifevitSDKHeartbeatData();

                heartRateData.setHeartRate(heartRate);

                sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), heartRateData, false);
                //}
            }
            break;
            case Constants.HEALTH_MEASUREMENT_BLOODOXYGEN: {
                //if (bloodOxygen > 0) {
                LifevitSDKOximeterData oxygenData = new LifevitSDKOximeterData();

                oxygenData.setSpO2(bloodOxygen);

                sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), oxygenData, false);
                //}
            }
            break;
            default:
                break;
        }
    }

    private void processBraceletHeartbeatCommand(byte[] bytes) {
        processRealtimeData(bytes);
    }

    private void processAppHeartbeatCommand(byte[] bytes) {

        byte[] bDistance = {bytes[1], bytes[2], bytes[3], bytes[4]};
        int distance = ByteUtils.bytesToInt(bDistance);
        int minutes = (int) bytes[5];
        int seconds = (int) bytes[6];

        int time = minutes * 60 + seconds;

        int gpsSignalStrength = (int) bytes[7];

        LifevitSDKDistanceTimeData data = new LifevitSDKDistanceTimeData();
        data.setDistance(distance);
        data.setTime(time);
        data.setGpsSignal(gpsSignalStrength);

        sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), data, true);

    }

    private void processAppSportModeControlEnable(byte[] bytes) {
        sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), (boolean) (bytes[1] == 1), true);
    }


    private void processRealtimeData(byte[] bytes) {

        byte[] bSteps = {bytes[1], bytes[2], bytes[3], bytes[4]};
        int steps = ByteUtils.bytesToInt(bSteps);
        byte[] bCalories = {bytes[5], bytes[6], bytes[7], bytes[8]};
        float calories = ByteUtils.bytesToInt(bCalories) / 100.0f;
        byte[] bDistance = {bytes[9], bytes[10], bytes[11], bytes[12]};
        float distance = ByteUtils.bytesToInt(bDistance) / 100.0f;
        byte[] bMovement = {bytes[13], bytes[14], 0x00, 0x00};
        int movementMinutes = ByteUtils.bytesToInt(bMovement);
        byte[] bMovement2 = {bytes[15], bytes[16], 0x00, 0x00};
        int movementSeconds = ByteUtils.bytesToInt(bMovement2);

        int duration = movementMinutes * 60 + movementSeconds;
        byte[] bExercise = {bytes[17], bytes[18], bytes[19], bytes[20]};
        int rapidMovement = ByteUtils.bytesToInt(bExercise);

        int heartrate = ByteUtils.toUnsignedInt(bytes[21]);
        byte[] bTemperature = {bytes[22], bytes[23], 0x00, 0x00};
        double temperature = ((double) ByteUtils.bytesToInt(bTemperature)) / 10.0;

        LifevitSDKSummaryStepData stepData = new LifevitSDKSummaryStepData(System.currentTimeMillis(), steps, calories, distance, rapidMovement, heartrate);
        stepData.setActiveTime((long) duration);
        stepData.setTemperature(temperature);

        sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), stepData, false);

    }


    private void processHeartBeatPacket(byte[] bytes) {

        int hr = ByteUtils.toUnsignedInt(bytes[1]);

        if (hr == 0xFF) {
            // Finished
            return;
        }

        byte[] bSteps = {bytes[2], bytes[3], bytes[4], bytes[5]};
        int steps = ByteUtils.bytesToInt(bSteps);

        byte[] bCalories = {bytes[9], bytes[8], bytes[7], bytes[6]};
//        float calories = ByteUtils.bytesToInt(bCalories) / 100.0f;

        double calories2 = ByteUtils.convertIEEE754BytesToFloat(bCalories);

        Log.d(CLASS_TAG, "float conversion: " + HexUtils.getStringToPrint(bCalories) + " is " + calories2);

        byte[] bExerciseTime = {bytes[10], bytes[11], bytes[12], bytes[13]};
        int exerciseTime = ByteUtils.bytesToInt(bExerciseTime);

        LifevitSDKVitalExerciseRecord stepData = new LifevitSDKVitalExerciseRecord();
        stepData.setDate(System.currentTimeMillis());
        stepData.setHeartRate(hr);
        stepData.setSteps(steps);
        stepData.setCalories(calories2);
        stepData.setExerciseTime(exerciseTime);

        sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), stepData, true);
    }


    private void processGetDetailedSteps(byte[] bytes) {


        if (!isSyncronizingSteps) {
            return;
        }
        int index = 0;

        String hexString = HexUtils.getStringToPrint(bytes);
        String[] hxBytes = hexString.split(":");

        //+1 Por el CRC
        while (index + 1 < bytes.length) {

            boolean end = (int) ByteUtils.toUnsignedInt(bytes[index + 1]) == 255;

            if (end) {

                isSyncronizingSteps = false;

                sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), new ArrayList(this.stepDataArray), true);

                return;
            }


            byte[] bIndex = {0x00, 0x00, bytes[index + 2], bytes[index + 1]};
            int identifier = ByteUtils.bytesToIntReversed(bIndex);

            int header1_date_year = Integer.parseInt(hxBytes[index + 3]) + 2000;
            int header1_date_month = Integer.parseInt(hxBytes[index + 4]);
            int header1_date_day = Integer.parseInt(hxBytes[index + 5]);
            int header1_date_hour = Integer.parseInt(hxBytes[index + 6]);
            int header1_date_minute = Integer.parseInt(hxBytes[index + 7]);
            int header1_date_second = Integer.parseInt(hxBytes[index + 8]);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, header1_date_year);
            cal.set(Calendar.MONTH, header1_date_month - 1);
            cal.set(Calendar.DAY_OF_MONTH, header1_date_day);
            cal.set(Calendar.HOUR_OF_DAY, header1_date_hour);
            cal.set(Calendar.MINUTE, header1_date_minute);
            cal.set(Calendar.SECOND, header1_date_second);
            cal.set(Calendar.MILLISECOND, 0);

            Long date = cal.getTimeInMillis();

            byte[] bSteps = {bytes[index + 9], bytes[index + 10], 0x00, 0x00};
            int steps = ByteUtils.bytesToInt(bSteps);
            byte[] bCalories = {bytes[index + 11], bytes[index + 12], 0x00, 0x00};
            float calories = ByteUtils.bytesToInt(bCalories) / 100.0f;
            byte[] bDistance = {bytes[index + 13], bytes[index + 14], 0x00, 0x00};
            float distance = ByteUtils.bytesToInt(bDistance) / 100.0f;


            LifevitSDKStepData stepData = new LifevitSDKStepData(date, steps, calories, distance);

            this.stepDataArray.add(stepData);

            LogUtils.debug("Step Data " + identifier + ": " + stepData.toString());
            index += 25;
        }

        //sendGetDetailedStepsData(Constants.DATA_OPERATION_NEXT);
    }

    private void processGetDetailedSleep(byte[] bytes) {
        int index = 0;

        String hexString = HexUtils.getStringToPrint(bytes);
        String[] hxBytes = hexString.split(":");

        boolean end = (int) ByteUtils.toUnsignedInt(bytes[bytes.length - 1]) == 255;

        if (end) {
            sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), new ArrayList(this.sleepDataArray), true);
            return;
        }

        byte[] bIndex = {0x00, 0x00, bytes[2 + index], bytes[1 + index]};
        int identifier = ByteUtils.bytesToIntReversed(bIndex);

        int header1_date_year = Integer.parseInt(hxBytes[3 + index]) + 2000;
        int header1_date_month = Integer.parseInt(hxBytes[4 + index]);
        int header1_date_day = Integer.parseInt(hxBytes[5 + index]);
        int header1_date_hour = Integer.parseInt(hxBytes[6 + index]);
        int header1_date_minute = Integer.parseInt(hxBytes[7 + index]);
        int header1_date_second = Integer.parseInt(hxBytes[8 + index]);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, header1_date_year);
        cal.set(Calendar.MONTH, header1_date_month - 1);
        cal.set(Calendar.DAY_OF_MONTH, header1_date_day);
        cal.set(Calendar.HOUR_OF_DAY, header1_date_hour);
        cal.set(Calendar.MINUTE, header1_date_minute);
        cal.set(Calendar.SECOND, header1_date_second);
        cal.set(Calendar.MILLISECOND, 0);

        Long date = cal.getTimeInMillis();

        int length = ByteUtils.toUnsignedInt(bytes[9]) - 1;

        int deepSleep = 0;
        int lightSleep = 0;
        for (int i = 0; i < length; i++) {
            int sleepQuality = ByteUtils.toUnsignedInt(bytes[11 + index + i]);
            if (sleepQuality == 1) {
                lightSleep += 1;
            } else if (sleepQuality == 2 || sleepQuality == 3) {
                deepSleep += 1;
            }
        }

        LifevitSDKSleepData data = new LifevitSDKSleepData();
        data.setDate(date);
        data.setSleepDuration(deepSleep);
        data.setSleepDeepness(LifevitSDKConstants.DEEP_SLEEP);
        this.sleepDataArray.add(data);

        LifevitSDKSleepData data2 = new LifevitSDKSleepData();
        // Desplazamos la fecha porque si coincide con la del Deep Sleep no se guarda en BBDD
        data2.setDate(date + deepSleep * 60 * 1000);
        data2.setSleepDuration(lightSleep);
        data2.setSleepDeepness(LifevitSDKConstants.LIGHT_SLEEP);
        this.sleepDataArray.add(data2);
    }

    private void processGetOxymeterData(byte[] bytes) {

        int index = 0;

        String hexString = HexUtils.getStringToPrint(bytes);
        String[] hxBytes = hexString.split(":");

        //+1 Por el CRC
        while (index + 1 < bytes.length) {
            boolean end = (int) ByteUtils.toUnsignedInt(bytes[index + 1]) == 255;

            if (end) {
                sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), new ArrayList(this.oximeterDataArray), true);
                return;
            }


            byte[] bIndex = {0x00, 0x00, bytes[index + 2], bytes[index + 1]};
            int identifier = ByteUtils.bytesToIntReversed(bIndex);

            int header1_date_year = Integer.parseInt(hxBytes[index + 3]) + 2000;
            int header1_date_month = Integer.parseInt(hxBytes[index + 4]);
            int header1_date_day = Integer.parseInt(hxBytes[index + 5]);
            int header1_date_hour = Integer.parseInt(hxBytes[index + 6]);
            int header1_date_minute = Integer.parseInt(hxBytes[index + 7]);
            int header1_date_second = Integer.parseInt(hxBytes[index + 8]);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, header1_date_year);
            cal.set(Calendar.MONTH, header1_date_month - 1);
            cal.set(Calendar.DAY_OF_MONTH, header1_date_day);
            cal.set(Calendar.HOUR_OF_DAY, header1_date_hour);
            cal.set(Calendar.MINUTE, header1_date_minute);
            cal.set(Calendar.SECOND, header1_date_second);
            cal.set(Calendar.MILLISECOND, 0);

            Long date = cal.getTimeInMillis();
            int spo2 = ByteUtils.toUnsignedInt(bytes[index + 9]);


            LifevitSDKOximeterData data = new LifevitSDKOximeterData();
            data.setDate(date);
            data.setSpO2(spo2);

            this.oximeterDataArray.add(data);
            index += 10;
        }

        //sendGetOxymeterData(Constants.DATA_OPERATION_NEXT);
    }

    private void processGetOxymeterPeriodicData(byte[] bytes) {

        int index = 0;

        String hexString = HexUtils.getStringToPrint(bytes);
        String[] hxBytes = hexString.split(":");

        //+1 Por el CRC
        while (index + 1 < bytes.length) {
            boolean end = (int) ByteUtils.toUnsignedInt(bytes[index + 1]) == 255;

            if (end) {
                sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), new ArrayList(this.oximeterDataArray), true);
                return;
            }


            byte[] bIndex = {0x00, 0x00, bytes[index + 2], bytes[index + 1]};
            int identifier = ByteUtils.bytesToIntReversed(bIndex);

            int header1_date_year = Integer.parseInt(hxBytes[index + 3]) + 2000;
            int header1_date_month = Integer.parseInt(hxBytes[index + 4]);
            int header1_date_day = Integer.parseInt(hxBytes[index + 5]);
            int header1_date_hour = Integer.parseInt(hxBytes[index + 6]);
            int header1_date_minute = Integer.parseInt(hxBytes[index + 7]);
            int header1_date_second = Integer.parseInt(hxBytes[index + 8]);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, header1_date_year);
            cal.set(Calendar.MONTH, header1_date_month - 1);
            cal.set(Calendar.DAY_OF_MONTH, header1_date_day);
            cal.set(Calendar.HOUR_OF_DAY, header1_date_hour);
            cal.set(Calendar.MINUTE, header1_date_minute);
            cal.set(Calendar.SECOND, header1_date_second);
            cal.set(Calendar.MILLISECOND, 0);

            int spo2 = ByteUtils.toUnsignedInt(bytes[index + 9]);


            LifevitSDKOximeterData data = new LifevitSDKOximeterData();
            data.setDate(cal.getTimeInMillis());
            data.setSpO2(spo2);

            this.oximeterDataArray.add(data);
            index += 10;
        }

        //sendGetPeriodicOxymeterData(Constants.DATA_OPERATION_NEXT);
    }

    private void processGetTemperatureData(byte[] bytes) {

        int index = 0;

        String hexString = HexUtils.getStringToPrint(bytes);
        String[] hxBytes = hexString.split(":");

        //+1 Por el CRC
        while (index + 1 < bytes.length) {
            boolean end = (int) ByteUtils.toUnsignedInt(bytes[index + 1]) == 255;

            if (end) {
                sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), new ArrayList(this.temperatureDataArray), true);
                return;
            }


            processAndAddTemperatureData(index, bytes);
            index += 11;
        }

        //sendGetTemperatureData(Constants.DATA_OPERATION_NEXT);
    }

    private void processGetTemperaturePeriodicData(byte[] bytes) {
        int index = 0;

        String hexString = HexUtils.getStringToPrint(bytes);
        String[] hxBytes = hexString.split(":");

        //+1 Por el CRC
        while (index + 1 < bytes.length) {
            boolean end = (int) ByteUtils.toUnsignedInt(bytes[index + 1]) == 255;
            if (end) {

                sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), new ArrayList(this.temperatureDataArray), true);
                return;
            }
            processAndAddTemperatureData(index, bytes);
            index += 11;
        }
        //sendGetPeriodicTemperatureData(Constants.DATA_OPERATION_NEXT);
    }

    private void processAndAddTemperatureData(int index, byte[] bytes) {
        byte[] bIndex = {0x00, 0x00, bytes[index + 2], bytes[index + 1]};
        int identifier = ByteUtils.bytesToIntReversed(bIndex);


        String hexString = HexUtils.getStringToPrint(bytes);
        String[] hxBytes = hexString.split(":");

        int header1_date_year = Integer.parseInt(hxBytes[index + 3]) + 2000;
        int header1_date_month = Integer.parseInt(hxBytes[index + 4]);
        int header1_date_day = Integer.parseInt(hxBytes[index + 5]);
        int header1_date_hour = Integer.parseInt(hxBytes[index + 6]);
        int header1_date_minute = Integer.parseInt(hxBytes[index + 7]);
        int header1_date_second = Integer.parseInt(hxBytes[index + 8]);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, header1_date_year);
        cal.set(Calendar.MONTH, header1_date_month - 1);
        cal.set(Calendar.DAY_OF_MONTH, header1_date_day);
        cal.set(Calendar.HOUR_OF_DAY, header1_date_hour);
        cal.set(Calendar.MINUTE, header1_date_minute);
        cal.set(Calendar.SECOND, header1_date_second);
        cal.set(Calendar.MILLISECOND, 0);


        byte[] temperatureArray = {bytes[index + 9], bytes[index + 10], 0x00, 0x00};
        int temperatureInt = ByteUtils.bytesToInt(temperatureArray);

        double temperature = (double) temperatureInt / 10.0;


        LifevitSDKTemperatureData data = new LifevitSDKTemperatureData();
        data.setDate(cal.getTimeInMillis());
        data.setValue(temperature);
        data.setUnit(parameters.isTemperatureUnitCelsius() ? LifevitSDKConstants.TEMPERATURE_UNIT_CELSIUS : LifevitSDKConstants.TEMPERATURE_UNIT_FAHRENHEIT);

        this.temperatureDataArray.add(data);
    }

    private void processECGStart(byte[] bytes) {

        int successful = ByteUtils.toUnsignedInt(bytes[1]);

        switch (successful) {
            case 0:
            case 1:

                break;
            default:

                break;
        }

        sendSuccessfulCommand(getActionForCommand(bytes[0]), true);
    }

    public void processECGWaveformData(byte[] bytes) {

        String hexString = HexUtils.getStringToPrint(bytes);
        String[] hxBytes = hexString.split(":");

        LogUtils.debug("processECGWaveformData: " + hexString);

        if (bytes.length == 3) {
            sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), this.ecgWaveformData, true);
            return;
        }

        byte[] bIndex = {0x00, 0x00, bytes[2], bytes[1]};
        int identifier = ByteUtils.bytesToIntReversed(bIndex);

        if (this.ecgWaveformData == null) {
            this.ecgWaveformData = new LifevitSDKVitalECGWaveform();
            int header1_date_year = Integer.parseInt(hxBytes[3]) + 2000;
            int header1_date_month = Integer.parseInt(hxBytes[4]);
            int header1_date_day = Integer.parseInt(hxBytes[5]);
            int header1_date_hour = Integer.parseInt(hxBytes[6]);
            int header1_date_minute = Integer.parseInt(hxBytes[7]);
            int header1_date_second = Integer.parseInt(hxBytes[8]);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, header1_date_year);
            cal.set(Calendar.MONTH, header1_date_month - 1);
            cal.set(Calendar.DAY_OF_MONTH, header1_date_day);
            cal.set(Calendar.HOUR_OF_DAY, header1_date_hour);
            cal.set(Calendar.MINUTE, header1_date_minute);
            cal.set(Calendar.SECOND, header1_date_second);
            cal.set(Calendar.MILLISECOND, 0);

            Long date = cal.getTimeInMillis();

            byte[] bDataLength = {bytes[9], bytes[10], 0x00, 0x00};
            int points = ByteUtils.bytesToInt(bDataLength);

            int hrv = ByteUtils.toUnsignedInt(bytes[11]);
            int heartRate = ByteUtils.toUnsignedInt(bytes[12]);
            int mood = ByteUtils.toUnsignedInt(bytes[13]);

            this.ecgWaveformData.setDate(date);
            this.ecgWaveformData.setHeartrate(heartRate);
            this.ecgWaveformData.setTotalPoints(points);
            this.ecgWaveformData.setHrv(hrv);
            this.ecgWaveformData.setBreath(mood);

            resetFilterData();

            for (int i = 27; i < bytes.length - 1; i += 2) {
                int point = getECGBytesValue(bytes[i + 1], bytes[i]);
                this.ecgWaveformData.getEcgData().add(point);
            }
        } else {
            for (int i = 3; i < bytes.length - 1; i += 2) {
                int point = getECGBytesValue(bytes[i + 1], bytes[i]);
                this.ecgWaveformData.getEcgData().add(point);
            }
        }
    }


    private int getECGBytesValue(byte byte0, byte byte1) {
        byte[] bPoints = {byte1, byte0, 0x00, 0x00};
        int point = ByteUtils.bytesToInt(bPoints);

        int value3 = resolveUtilGetValue(byte0, 1) + resolveUtilGetValue(byte1, 0);
        int value4 = value3;
        if (value4 >= 32768) {
            value4 -= 65536;
        }

//        Log.d(CLASS_TAG, "(b)" + HexUtils.getStringToPrint(bPoints) + " (a) " + point + " (b) " + value3 + " (c) " + value4);
        return (int) (filterEcgData((double) value4));
    }


    private void processECGMeasurementData(byte[] bytes) {
        ArrayList<Integer> ecgSets = new ArrayList();
        for (int i = 1; i < bytes.length - 1; i += 2) {
            ecgSets.add(getECGBytesValue(bytes[i], bytes[i + 1]));
        }

        sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), ecgSets, false);
    }


    private void processGetECGHeartRateData(byte[] bytes) {
        List<Integer> hrValues = new ArrayList<>();
        hrValues.add(ByteUtils.toUnsignedInt(bytes[1])); // HR
        hrValues.add(ByteUtils.toUnsignedInt(bytes[2])); // HRV
        hrValues.add(ByteUtils.toUnsignedInt(bytes[3])); // Mood?
        sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), hrValues, false);
    }


    public static int resolveUtilGetValue(byte b, int i) {
        double d = (double) (b & 255);
        double pow = Math.pow(256.0d, (double) i);
        return (int) (d * pow);
    }


    private void processECGStatusData(byte[] bytes) {

        int status = ByteUtils.toUnsignedInt(bytes[1]);

        String hexString = HexUtils.getStringToPrint(bytes);
        String[] hxBytes = hexString.split(":");

        LifevitSDKVitalECGStatus ecgStatus = new LifevitSDKVitalECGStatus();
        ecgStatus.setStatus(status);

        switch (status) {
            case Constants.REQUEST_ECG_STATUS_MEASUREMENT_COMPLETED: {
                int hrv = ByteUtils.toUnsignedInt(bytes[2]);
                int vascularAge = ByteUtils.toUnsignedInt(bytes[3]);
                int heartrate = ByteUtils.toUnsignedInt(bytes[4]);
                int fatigue = ByteUtils.toUnsignedInt(bytes[5]);
                int systolic = ByteUtils.toUnsignedInt(bytes[6]);
                int diastolic = ByteUtils.toUnsignedInt(bytes[7]);
                int mood = ByteUtils.toUnsignedInt(bytes[8]);
                int breathRate = ByteUtils.toUnsignedInt(bytes[9]);

                int header1_date_year = Integer.parseInt(hxBytes[10]) + 2000;
                int header1_date_month = Integer.parseInt(hxBytes[11]);
                int header1_date_day = Integer.parseInt(hxBytes[12]);
                int header1_date_hour = Integer.parseInt(hxBytes[13]);
                int header1_date_minute = Integer.parseInt(hxBytes[14]);
                int header1_date_second = Integer.parseInt(hxBytes[15]);

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, header1_date_year);
                cal.set(Calendar.MONTH, header1_date_month - 1);
                cal.set(Calendar.DAY_OF_MONTH, header1_date_day);
                cal.set(Calendar.HOUR_OF_DAY, header1_date_hour);
                cal.set(Calendar.MINUTE, header1_date_minute);
                cal.set(Calendar.SECOND, header1_date_second);
                cal.set(Calendar.MILLISECOND, 0);

                Long date = cal.getTimeInMillis();

                LifevitSDKVitalECGConstantsData data = new LifevitSDKVitalECGConstantsData();
                data.setHeartRate(heartrate);
                data.setHrv(hrv);
                data.setDate(date);

                ecgStatus.setData(data);

                sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), ecgStatus, false);
                break;
            }

            default:

                sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), ecgStatus, false);

                break;
        }

    }

    private void processGetHeartRateData(byte[] bytes) {

        int index = 0;

        String hexString = HexUtils.getStringToPrint(bytes);
        String[] hxBytes = hexString.split(":");

        //+1 Por el CRC
        while (index + 1 < bytes.length) {
            boolean end = (int) ByteUtils.toUnsignedInt(bytes[index + 1]) == 255;

            if (end) {
                sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), new ArrayList(this.heartRateDataArray), true);
                return;
            }


            byte[] bIndex = {0x00, 0x00, bytes[index + 2], bytes[index + 1]};
            int identifier = ByteUtils.bytesToIntReversed(bIndex);

            int header1_date_year = Integer.parseInt(hxBytes[index + 3]) + 2000;
            int header1_date_month = Integer.parseInt(hxBytes[index + 4]);
            int header1_date_day = Integer.parseInt(hxBytes[index + 5]);
            int header1_date_hour = Integer.parseInt(hxBytes[index + 6]);
            int header1_date_minute = Integer.parseInt(hxBytes[index + 7]);
            int header1_date_second = Integer.parseInt(hxBytes[index + 8]);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, header1_date_year);
            cal.set(Calendar.MONTH, header1_date_month - 1);
            cal.set(Calendar.DAY_OF_MONTH, header1_date_day);
            cal.set(Calendar.HOUR_OF_DAY, header1_date_hour);
            cal.set(Calendar.MINUTE, header1_date_minute);
            cal.set(Calendar.SECOND, header1_date_second);
            cal.set(Calendar.MILLISECOND, 0);

            Long date = cal.getTimeInMillis();
            int heartRate = ByteUtils.toUnsignedInt(bytes[index + 9]);


            LifevitSDKHeartbeatData data = new LifevitSDKHeartbeatData();
            data.setDate(date);
            data.setHeartRate(heartRate);

            this.heartRateDataArray.add(data);
            index += 10;
        }

        //  sendGetHeartRateData(Constants.DATA_OPERATION_NEXT);
    }


    private void processGetHeartRatePeriodicData(byte[] bytes) {

        int index = 0;

        String hexString = HexUtils.getStringToPrint(bytes);
        String[] hxBytes = hexString.split(":");

        //+1 Por el CRC
        while (index + 1 < bytes.length) {
            boolean end = (int) ByteUtils.toUnsignedInt(bytes[index + 1]) == 255;

            if (end) {
                sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), new ArrayList(this.heartRateDataArray), true);
                return;
            }


            byte[] bIndex = {0x00, 0x00, bytes[2], bytes[1]};
            int identifier = ByteUtils.bytesToIntReversed(bIndex);

            int header1_date_year = Integer.parseInt(hxBytes[index + 3]) + 2000;
            int header1_date_month = Integer.parseInt(hxBytes[index + 4]);
            int header1_date_day = Integer.parseInt(hxBytes[index + 5]);
            int header1_date_hour = Integer.parseInt(hxBytes[index + 6]);
            int header1_date_minute = Integer.parseInt(hxBytes[index + 7]);
            int header1_date_second = Integer.parseInt(hxBytes[index + 8]);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, header1_date_year);
            cal.set(Calendar.MONTH, header1_date_month - 1);
            cal.set(Calendar.DAY_OF_MONTH, header1_date_day);
            cal.set(Calendar.HOUR_OF_DAY, header1_date_hour);
            cal.set(Calendar.MINUTE, header1_date_minute);
            cal.set(Calendar.SECOND, header1_date_second);
            cal.set(Calendar.MILLISECOND, 0);

            for (int i = 9; i <= 23 - 1; i++) {
                int heartRate = ByteUtils.toUnsignedInt(bytes[index + i]);
                LifevitSDKHeartbeatData data = new LifevitSDKHeartbeatData();
                data.setDate(cal.getTimeInMillis());
                data.setHeartRate(heartRate);

                this.heartRateDataArray.add(data);
                cal.add(Calendar.MINUTE, 1);
            }

            index += 24;
        }

        // sendGetPeriodicHeartRateData(Constants.DATA_OPERATION_NEXT);
    }

    private void processGetVitalsData(byte[] bytes) {
        int index = 0;

        String hexString = HexUtils.getStringToPrint(bytes);
        String[] hxBytes = hexString.split(":");

        //+1 Por el CRC
        while (index + 1 < bytes.length) {
            boolean end = ByteUtils.toUnsignedInt(bytes[index + 1]) == 255;

            if (end) {
                sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), new ArrayList(this.vitalsArray), true);
                return;
            }

            byte[] bIndex = {0x00, 0x00, bytes[2], bytes[1]};
            int identifier = ByteUtils.bytesToIntReversed(bIndex);

            int year = Integer.parseInt(hxBytes[index + 3]) + 2000;
            int month = Integer.parseInt(hxBytes[index + 4]);
            int day = Integer.parseInt(hxBytes[index + 5]);

            int hour = Integer.parseInt(hxBytes[index + 6]);
            int minute = Integer.parseInt(hxBytes[index + 7]);
            int second = Integer.parseInt(hxBytes[index + 8]);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month - 1);
            cal.set(Calendar.DAY_OF_MONTH, day);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, second);
            cal.set(Calendar.MILLISECOND, 0);

            Long date = cal.getTimeInMillis();

            int hrv = ByteUtils.toUnsignedInt(bytes[index + 9]);
            int vascularAging = ByteUtils.toUnsignedInt(bytes[index + 10]);
            int heartRate = ByteUtils.toUnsignedInt(bytes[index + 11]);
            int fatigue = ByteUtils.toUnsignedInt(bytes[index + 12]);
            int systolic = ByteUtils.toUnsignedInt(bytes[index + 13]);
            int diastolic = ByteUtils.toUnsignedInt(bytes[index + 14]);


            LifevitSDKVitalHRVData mData = new LifevitSDKVitalHRVData();

            mData.setDate(date);
            mData.setHrv(hrv);
            mData.setVascularAging(vascularAging);
            mData.setFatigue(fatigue);
            mData.setSystolic(systolic);
            mData.setDiastolic(diastolic);
            mData.setHeartRate(heartRate);

            vitalsArray.add(mData);

            index += 15;
        }

        // sendGetVitals(Constants.DATA_OPERATION_NEXT);

    }

    private void processGetDeviceDatetime(byte[] rx) {

        String hexString = HexUtils.getStringToPrint(rx);
        String[] hxBytes = hexString.split(":");
        int header1_date_year = Integer.parseInt(hxBytes[1]) + 2000;
        int header1_date_month = Integer.parseInt(hxBytes[2]);
        int header1_date_day = Integer.parseInt(hxBytes[3]);

        int header1_date_hour = Integer.parseInt(hxBytes[4]);
        int header1_date_minute = Integer.parseInt(hxBytes[5]);
        int header1_date_second = Integer.parseInt(hxBytes[6]);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, header1_date_year);
        cal.set(Calendar.MONTH, header1_date_month - 1);
        cal.set(Calendar.DAY_OF_MONTH, header1_date_day);
        cal.set(Calendar.HOUR_OF_DAY, header1_date_hour);
        cal.set(Calendar.MINUTE, header1_date_minute);
        cal.set(Calendar.SECOND, header1_date_second);
        cal.set(Calendar.MILLISECOND, 0);

        Long date = cal.getTimeInMillis();


        sendSuccessfulCommandWithData(getActionForCommand(rx[0]), date, true);
    }

    private void processGetMAC(byte[] rx) {

        byte[] macBytes = {rx[1], rx[2], rx[3], rx[4], rx[5], rx[6]};
        String macAddress = HexUtils.getStringToPrint(macBytes);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "MAC: " + macAddress);

        sendSuccessfulCommandWithData(getActionForCommand(rx[0]), macAddress, true);
    }

    private void processGetBatteryLevel(byte[] rx) {
        int battery = rx[1];

        LogUtils.log(Log.DEBUG, CLASS_TAG, "Battery level: " + battery);

        sendSuccessfulCommandWithData(getActionForCommand(rx[0]), battery, true);

    }

    private void processGetUserInformation(byte[] rx) {

        int gender = LifevitSDKConstants.GENDER_FEMALE;
        int g = rx[1];
        if (g == 1) {
            gender = LifevitSDKConstants.GENDER_MALE;
        }
        int age = rx[2];
        long height = ByteUtils.toUnsignedLong(rx[3]);
        long weight = ByteUtils.toUnsignedLong(rx[4]);

        LifevitSDKUserData user = new LifevitSDKUserData(age, weight, height, gender);


        LogUtils.log(Log.DEBUG, CLASS_TAG, "User Info: " + user.toString());

        sendSuccessfulCommandWithData(getActionForCommand(rx[0]), user, true);

    }


    private void processGetParameters(byte[] bytes) {

        int basicHeartRateSetting;
        int screenBrightness;
        int dialInterface;
        int language;

        this.parameters.setDistanceUnitKm((int) bytes[1] == Constants.DATA_GET_KM);
        this.parameters.setHourDisplay24h((int) bytes[2] == Constants.DATA_GET_HOUR_24_DISPLAY);
        this.parameters.setWristSenseEnabled((int) bytes[3] == Constants.DATA_GET_ENABLED);
        this.parameters.setTemperatureUnitCelsius((int) bytes[4] == Constants.DATA_GET_CELSIUS);
        this.parameters.setNightMode((int) bytes[5] == Constants.DATA_GET_ENABLED);
        this.parameters.setANCSEnabled((int) bytes[5] == Constants.DATA_GET_ENABLED);


        byte firstANCSByte = (byte) bytes[8];
        byte secondANCSByte = (byte) bytes[7];

        this.parameters.getNotifications().setCall((firstANCSByte & 0x01) == 1);
        this.parameters.getNotifications().setMobileInformation((firstANCSByte >> 1 & 0x01) == 1);
        this.parameters.getNotifications().setWechat((firstANCSByte >> 2 & 0x01) == 1);
        this.parameters.getNotifications().setFacebook((firstANCSByte >> 3 & 0x01) == 1);
        this.parameters.getNotifications().setInstagram((firstANCSByte >> 4 & 0x01) == 1);
        this.parameters.getNotifications().setSkype((firstANCSByte >> 5 & 0x01) == 1);
        this.parameters.getNotifications().setTelegram((firstANCSByte >> 6 & 0x01) == 1);
        //this.parameters.getNotifications().setCall((firstANCSByte >> 7 & 0x01) == 1);
        this.parameters.getNotifications().setTwitter((secondANCSByte & 0x01) == 1);
        this.parameters.getNotifications().setVkclient((secondANCSByte >> 1 & 0x01) == 1);
        this.parameters.getNotifications().setWhatsapp((secondANCSByte >> 2 & 0x01) == 1);
        this.parameters.getNotifications().setQq((secondANCSByte >> 3 & 0x01) == 1);
        this.parameters.getNotifications().setLinkedin((secondANCSByte >> 4 & 0x01) == 1);

        this.parameters.setBasicHeartRateSetting((int) bytes[9]);
        this.parameters.setScreenBrightness((int) bytes[11]);
        this.parameters.setDialInterface((int) bytes[12]);

        int languageInt = (int) bytes[14];
        if (languageInt == Constants.DATA_GET_CHINESE) {
            language = LifevitSDKVitalParams.Language.CHINESE;
        } else {
            language = LifevitSDKVitalParams.Language.ENGLISH;
        }

        this.parameters.setLanguage(language);

        //No devolvemos estamos a la espera del new parameters
        sendingThread.taskFinished();

    }

    private void processGetNewParameters(byte[] rx) {

        this.parameters.hand = rx[1];
        sendSuccessfulCommandWithData(getActionForCommand(rx[0]), parameters, true);

        pendingToUpdateParams = false;
    }

    private void processGetTargetSteps(byte[] rx) {

        byte[] data = {rx[1], rx[2], rx[3], rx[4]};
        Integer target = ByteUtils.bytesToInt(data);


        sendSuccessfulCommandWithData(getActionForCommand(rx[0]), target, true);

    }

    /*private void processAutomaticOxygenPeriod(byte[] bytes) {

        String hexString = HexUtils.getStringToPrint(bytes);
        String[] hexBytes = hexString.split(":");

        int operation = ByteUtils.toUnsignedInt(bytes[1]);
        int workingMode = ByteUtils.toUnsignedInt(bytes[2]);
        int intervalTime = ByteUtils.toUnsignedInt(bytes[3]);

        int startHour = Integer.parseInt(hexBytes[4]);
        int startMinutes = Integer.parseInt(hexBytes[5]);
        int endHour = Integer.parseInt(hexBytes[6]);
        int endMinutes = Integer.parseInt(hexBytes[7]);

        byte weekEnableByte = bytes[8];

        //Period
        LifevitSDKVitalPeriod period = new LifevitSDKVitalPeriod();
        period.setOperation(operation);
        period.setWorkingMode(workingMode);
        period.setIntervalTime(intervalTime);
        period.setStartHour(startHour);
        period.setStartMinute(startMinutes);
        period.setEndHour(endHour);
        period.setEndMinute(endMinutes);

        period.setMonday((weekEnableByte & 0x01) == 1);
        period.setTuesday((weekEnableByte >> 1 & 0x01) == 1);
        period.setWednesday((weekEnableByte >> 2 & 0x01) == 1);
        period.setThursday((weekEnableByte >> 3 & 0x01) == 1);
        period.setFriday((weekEnableByte >> 4 & 0x01) == 1);
        period.setSaturday((weekEnableByte >> 5 & 0x01) == 1);
        period.setSunday((weekEnableByte >> 6 & 0x01) == 1);

        sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), period, true);
    }*/

    private void processAutomaticPeriod(byte[] bytes) {

        String hexString = HexUtils.getStringToPrint(bytes);
        String[] hexBytes = hexString.split(":");

        int workingMode = ByteUtils.toUnsignedInt(bytes[1]);

        LifevitSDKVitalPeriod period = new LifevitSDKVitalPeriod();
        int type = bytes[9];
        if (type == 3) {
            period.setType(LifevitSDKConstants.BraceletVitalDataType.TEMPERATURE);
        } else if (type == 2) {
            period.setType(LifevitSDKConstants.BraceletVitalDataType.OXIMETER);
        } else {
            period.setType(LifevitSDKConstants.BraceletVitalDataType.HR);
        }

        //Period
        switch (workingMode) {
            case Constants.WORKING_MODE_OFF:
                period.setWorkingMode(LifevitSDKConstants.BraceletVitalPeriodWorkingMode.OFF);
                break;
            case Constants.WORKING_MODE_TIME_INTERVAL:
                period.setWorkingMode(LifevitSDKConstants.BraceletVitalPeriodWorkingMode.TIME_INTERVAL);
                break;
            case Constants.WORKING_MODE_TIME_PERIOD:
                period.setWorkingMode(LifevitSDKConstants.BraceletVitalPeriodWorkingMode.TIME_PERIOD);
                break;
        }
        if (period.isEnabled()) {
            int startHour = Integer.parseInt(hexBytes[2]);
            int startMinutes = Integer.parseInt(hexBytes[3]);
            int endHour = Integer.parseInt(hexBytes[4]);
            int endMinutes = Integer.parseInt(hexBytes[5]);

            byte weekEnableByte = bytes[6];

            byte[] bInterval = {bytes[7], bytes[8], 0x00, 0x00};
            int intervalTime = ByteUtils.bytesToInt(bInterval);

            period.setIntervalTime(intervalTime);
            period.setStartHour(startHour);
            period.setStartMinute(startMinutes);
            period.setEndHour(endHour);
            period.setEndMinute(endMinutes);

            period.setMonday((weekEnableByte & 0x01) == 1);
            period.setTuesday((weekEnableByte >> 1 & 0x01) == 1);
            period.setWednesday((weekEnableByte >> 2 & 0x01) == 1);
            period.setThursday((weekEnableByte >> 3 & 0x01) == 1);
            period.setFriday((weekEnableByte >> 4 & 0x01) == 1);
            period.setSaturday((weekEnableByte >> 5 & 0x01) == 1);
            period.setSunday((weekEnableByte >> 6 & 0x01) == 1);
        }

        sendSuccessfulCommandWithData(getActionForCommand(bytes[0]), period, true);
    }


    // endregion --- Processing responses methods ---


    private byte calculateCRC(byte[] bytes) {
        int sum = 0;
        for (byte b : bytes) {
            sum += b;
        }
        return (byte) (sum & 0x00ff);
    }


    // region --- Send commands to bracelet ---

    byte[] getEmptyArray(int lenght) {
        byte[] syncparamsArray = new byte[lenght];

        for (int i = 0; i < lenght; i++) {
            syncparamsArray[i] = (byte) 0;
        }

        return syncparamsArray;
    }

    protected void sendQRCode(boolean showQR) {


        byte[] bytes = getEmptyArray(16);

        bytes[0] = Constants.REQUEST_UNLOCK_QR_CODE;
        if (showQR) {
            bytes[1] = (byte) 0x80;
        } else {
            bytes[1] = (byte) 0x81;
        }
        byte checksum = calculateCRC(bytes);
        bytes[15] = checksum;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendQRCode] " + HexUtils.getStringToPrint(bytes));

        sendMessage(bytes);
    }

    protected void sendRealtimeCounting(boolean steps, boolean temperature) {


        byte[] bytes = getEmptyArray(16);

        bytes[0] = Constants.REQUEST_SET_REALTIME_STEP_COUNTING;
        bytes[1] = steps ? (byte) 1 : (byte) 0;
        bytes[2] = temperature ? (byte) 1 : (byte) 0;
        byte checksum = calculateCRC(bytes);
        bytes[15] = checksum;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendRealtimeCounting] " + HexUtils.getStringToPrint(bytes));

        sendMessage(bytes);

        sendingThread.taskFinished();
    }

    protected void sendBasicCommand(byte command) {

        byte[] bytes = getEmptyArray(16);

        bytes[0] = command;

        byte checksum = calculateCRC(bytes);
        bytes[15] = checksum;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendGetTime] " + HexUtils.getStringToPrint(bytes));

        sendMessage(bytes);
    }

    protected void sendSetTargetSteps(int target) {
        byte[] bytes = getEmptyArray(16);

        bytes[0] = Constants.REQUEST_SET_TARGET_STEPS;

        byte[] tBytes = ByteUtils.intToBytesLittleIndian(target);

        for (int i = 0; i < tBytes.length; i++) {
            bytes[i + 1] = tBytes[i];
        }

        byte checksum = calculateCRC(bytes);
        bytes[15] = checksum;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSetTargetSteps] " + HexUtils.getStringToPrint(bytes));

        sendMessage(bytes);
    }

    protected void sendSetTime(long date) {

        byte[] bytes = getEmptyArray(16);

        bytes[0] = Constants.REQUEST_SET_TIME;

        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeInMillis(date);

        Integer year = cal.get(Calendar.YEAR) - 2000;
        Integer month = cal.get(Calendar.MONTH) + 1;
        Integer day = cal.get(Calendar.DAY_OF_MONTH);
        Integer hour = cal.get(Calendar.HOUR_OF_DAY);
        Integer minute = cal.get(Calendar.MINUTE);
        Integer second = cal.get(Calendar.SECOND);


        String hex = String.format(Locale.ENGLISH, "%2d%2d%2d%2d%2d%2d", year, month, day, hour, minute, second).replace(" ", "0");
        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSetTime date] " + hex);

        byte[] data = HexUtils.hexStringToByteArray(hex);

        for (int i = 0; i < data.length; i++) {
            bytes[i + 1] = data[i];
        }

        byte checksum = calculateCRC(bytes);
        bytes[15] = checksum;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSetTime] " + HexUtils.getStringToPrint(bytes));

        sendMessage(bytes);
    }

    protected void sendSetUserInfo(LifevitSDKUserData data) {

        byte[] bytes = getEmptyArray(16);

        bytes[0] = Constants.REQUEST_SET_USER_PERSONAL_INFORMATION;

        if (data.getGender() == LifevitSDKConstants.GENDER_FEMALE) {
            bytes[1] = (char) 0;
        } else {
            bytes[1] = (char) 1;
        }

        if (data.getAge() != null)
            bytes[2] = (byte) data.getAge().intValue();

        if (data.getHeight() > 255) {
            bytes[3] = (byte) 0xff;
        } else {
            bytes[3] = (byte) data.getHeight();
        }
        if (data.getWeight() > 255) {
            bytes[4] = (byte) 0xff;
        } else {
            bytes[4] = (byte) data.getWeight();
        }

        if (data.getGender() == LifevitSDKConstants.GENDER_MALE) {
            bytes[5] = 78;
        } else {
            bytes[5] = 70;
        }

        byte checksum = calculateCRC(bytes);
        bytes[15] = checksum;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSetUserInfo] " + HexUtils.getStringToPrint(bytes));

        sendMessage(bytes);
    }

    protected void sendSetParameters(LifevitSDKVitalParams deviceParameters) {


        if (deviceParameters.checkValidDeviceParameters()) {
            byte[] bytes = getEmptyArray(16);
            bytes[0] = Constants.REQUEST_SET_DEVICE_PARAMETERS;


            if (deviceParameters.distanceUnitKm) {
                bytes[1] = (byte) Constants.DATA_SET_KM;
            } else {
                bytes[1] = (byte) Constants.DATA_SET_MILE;
            }

            if (deviceParameters.hourDisplay24h) {
                bytes[2] = (byte) Constants.DATA_SET_HOUR_24_DISPLAY;
            } else {
                bytes[2] = (byte) Constants.DATA_SET_HOUR_12_DISPLAY;
            }

            if (deviceParameters.wristSenseEnabled) {
                bytes[3] = (byte) Constants.DATA_SET_ENABLE;
            } else {
                bytes[3] = (byte) Constants.DATA_SET_DISABLE;
            }

            if (deviceParameters.temperatureUnitCelsius) {
                bytes[4] = (byte) Constants.DATA_SET_CELSIUS;
            } else {
                bytes[4] = (byte) Constants.DATA_SET_FAHRENHEIT;
            }

            if (deviceParameters.nightMode) {
                bytes[5] = (byte) Constants.DATA_SET_ENABLE;
            } else {
                bytes[5] = (byte) Constants.DATA_SET_DISABLE;
            }

            if (deviceParameters.ANCSEnabled) {
                bytes[6] = (byte) Constants.DATA_SET_ENABLE;
            } else {
                bytes[6] = (byte) Constants.DATA_SET_DISABLE;
            }

            byte[] acnsBytes = getANCSBytes(deviceParameters.getNotifications());

            bytes[7] = acnsBytes[0];
            bytes[8] = acnsBytes[1];

            bytes[9] = (byte) (deviceParameters.basicHeartRateSetting | 0x80);
            bytes[10] = (byte) 0;
            bytes[11] = (byte) (128 + deviceParameters.screenBrightness);
            bytes[12] = (byte) (128 + deviceParameters.dialInterface);

            if (deviceParameters.language == LifevitSDKVitalParams.Language.CHINESE) {
                bytes[14] = (byte) Constants.DATA_SET_CHINESE;
            } else {
                bytes[14] = (byte) Constants.DATA_SET_ENGLISH;
            }

            byte checksum = calculateCRC(bytes);
            bytes[15] = checksum;

            LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSetParameters] " + HexUtils.getStringToPrint(bytes));

            sendMessage(bytes);
        } else {
            sendingThread.taskFinished();
        }
    }

    private byte[] getANCSBytes(LifevitSDKVitalNotification notifications) {
        byte[] bytes = {0x00, 0x00};

        //Configuramos el byte lowest...
        if (notifications.isCall()) {
            bytes[1] += 0x01;
        }
        if (notifications.isMobileInformation()) {
            bytes[1] += 0x02;
        }
        if (notifications.isWechat()) {
            bytes[1] += 0x04;
        }
        if (notifications.isFacebook()) {
            bytes[1] += 0x08;
        }
        if (notifications.isInstagram()) {
            bytes[1] += 0x10;
        }
        if (notifications.isSkype()) {
            bytes[1] += 0x20;
        }
        if (notifications.isTelegram()) {
            bytes[1] += 0x30;
        }

        bytes[1] += 0x80;

        //Configuramos el segundo byte highest
        if (notifications.isTwitter()) {
            bytes[1] += 0x01;
        }
        if (notifications.isVkclient()) {
            bytes[1] += 0x02;
        }
        if (notifications.isWhatsapp()) {
            bytes[1] += 0x04;
        }
        if (notifications.isQq()) {
            bytes[1] += 0x08;
        }
        if (notifications.isLinkedin()) {
            bytes[1] += 0x10;
        }

        return bytes;
    }

    protected void sendSetNewParameters(LifevitSDKVitalParams data) {

        if (data.checkValidDeviceParameters()) {
            byte[] bytes = getEmptyArray(16);

            bytes[0] = Constants.REQUEST_SET_DEVICE_NEW_PARAMETERS;

            switch (data.getHand()) {
                case LifevitSDKConstants.BRACELET_HAND_RIGHT:
                    bytes[1] = Constants.Hand.RIGHT;
                    break;

                default:
                    bytes[1] = Constants.Hand.LEFT;
                    break;
            }

            byte checksum = calculateCRC(bytes);
            bytes[15] = checksum;

            LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSetNewParameters] " + HexUtils.getStringToPrint(bytes));

            sendMessage(bytes);
        } else {
            sendingThread.taskFinished();
        }
    }

    protected void sendStartSport(Integer controlMode, Integer sport, Integer level, Integer time) {
        byte[] bytes = getEmptyArray(16);

        bytes[0] = Constants.REQUEST_SPORT_MODE_CONTROL_ENABLE;

        bytes[1] = (byte) controlMode.intValue();
        if (sport != null) {
            bytes[2] = (byte) sport.intValue();
        }
        if (level != null) {
            bytes[3] = (byte) level.intValue();
        }
        if (time != null) {
            bytes[4] = (byte) time.intValue();
        }
        byte checksum = calculateCRC(bytes);
        bytes[15] = checksum;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendStartSport] " + HexUtils.getStringToPrint(bytes));

        sendMessage(bytes);
    }


    public void sendSportAppHeartbeatPacket(Float distance, Integer paceSeconds, Integer gpsSignal) {
        byte[] bytes = getEmptyArray(16);

        Log.d(CLASS_TAG, "[DGBleDevice] preparado para enviar a dispositivo");

        bytes[0] = Constants.REQUEST_APP_HEART_BEAT_PACKET;

        if (distance != null) {
            byte[] distanceBytes = ByteUtils.convertIEEE754FloatToBytes(distance);

            bytes[1] = distanceBytes[3];
            bytes[2] = distanceBytes[2];
            bytes[3] = distanceBytes[1];
            bytes[4] = distanceBytes[0];
        }
        if (paceSeconds != null) {
            bytes[5] = (byte) (paceSeconds / 60);
            bytes[6] = (byte) (paceSeconds % 60);
        }
        if (gpsSignal != null) {
            bytes[7] = (byte) gpsSignal.intValue();
        }
        byte checksum = calculateCRC(bytes);
        bytes[15] = checksum;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendStartSport] " + HexUtils.getStringToPrint(bytes));

        sendMessage(bytes);
    }


    protected void sendSetNotification(LifevitSDKVitalScreenNotification data) {
        byte[] bytes = getEmptyArray(80);
        bytes[0] = Constants.REQUEST_SCREEN_NOTIF;
        bytes[1] = (byte) data.getType().value;
        byte[] bText = data.getText().getBytes();
        int length = Math.min(60, bText.length);
        bytes[2] = (byte) length;
        for (int i = 0; i < length; i++) {
            bytes[3 + i] = bText[i];
        }

        bText = data.getContact().getBytes();
        length = Math.min(15, bText.length);
        bytes[62] = (byte) length;
        for (int i = 0; i < length; i++) {
            bytes[63 + i] = bText[i];
        }

        byte checksum = calculateCRC(bytes);
        bytes[bytes.length - 1] = checksum;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSetNotification] " + HexUtils.getStringToPrint(bytes));

        sendMessage(bytes);
    }

    protected void sendSetAlarms(List<LifevitSDKVitalAlarm> data) {
        //byte[] bytes = getEmptyArray(390);
        int TOTAL_ALARMS = data.size();
        byte[] bytes = getEmptyArray(39 * TOTAL_ALARMS + 2);


        int index = 0;
        int i = 0;
        for (LifevitSDKVitalAlarm alarm : data
        ) {
            byte[] mAlarm = getAlarmBytes(index++, TOTAL_ALARMS, alarm);
            for (int j = 0; j < mAlarm.length; j++) {
                bytes[i++] = mAlarm[j];
            }
        }

        //byte checksum = calculateCRC(bytes);
        //bytes[40] = checksum;

        bytes[bytes.length - 2] = Constants.REQUEST_SET_ALARMS;
        bytes[bytes.length - 1] = (byte) 0xFF;


        //bytes = new byte[]{0x23, 0x01, 0x00, 0x01, 0x01, 0x18, 0x30, 0x1f, 0x05, 0x31, 0x32, 0x33, 0x34,
        //        0x35, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        //        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x23, (byte) 0xff};
        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSetAlarms] " + HexUtils.getStringToPrint(bytes));
        sendMessage(bytes);
    }

    private byte[] getAlarmBytes(int index, int total, LifevitSDKVitalAlarm data) {

        byte[] bytes = getEmptyArray(39);
        bytes[0] = Constants.REQUEST_SET_ALARMS;
        bytes[1] = (byte) total;
        bytes[2] = (byte) index;
        bytes[3] = (byte) (data.isEnabled() ? 0x01 : 0x00);
        bytes[4] = (byte) data.getType().value;
        bytes[5] = HexUtils.hexToByte(String.format("0x%2d", data.getHour()).replace(" ", "0"));
        bytes[6] = HexUtils.hexToByte(String.format("0x%2d", data.getMinute()).replace(" ", "0"));
        bytes[7] = ByteUtils.getWeekByte(data.isMonday(), data.isTuesday(), data.isWednesday(), data.isThursday(), data.isFriday(), data.isSaturday(), data.isSunday());

        /*byte[] bText = data.getText().getBytes();
        int length = Math.min(30, bText.length);
        bytes[8] = (byte) length;
        for (int i = 0; i < length; i++) {
            bytes[9 + i] = bText[i];
        }*/

        bytes[8] = 0x05;
        bytes[9] = 0x31;
        bytes[10] = 0x32;
        bytes[11] = 0x33;
        bytes[12] = 0x34;
        bytes[13] = 0x35;

        return bytes;
    }

    protected void sendSetWeather(LifevitSDKVitalWeather data) {
        byte[] bytes = getEmptyArray(41);
        bytes[0] = Constants.REQUEST_SET_WEATHER;
        bytes[1] = (byte) data.getStatus();
        bytes[2] = (byte) data.getTemperature();
        bytes[3] = (byte) data.getMaxTemperature();
        bytes[4] = (byte) data.getMinTemperature();
        bytes[5] = (byte) (data.getAirQuality() & 0x00ff);
        bytes[6] = (byte) (data.getAirQuality() >> 8 & 0x00ff);

        byte[] bText = data.getLocation().getBytes();
        int length = Math.min(32, bText.length);
        bytes[7] = (byte) length;
        for (int i = 0; i < length; i++) {
            bytes[8 + i] = bText[i];
        }
        byte checksum = calculateCRC(bytes);
        bytes[40] = checksum;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSetWeather] " + HexUtils.getStringToPrint(bytes));

        sendMessage(bytes);
    }

    protected void sendSetActivityPeriod(LifevitSDKVitalActivityPeriod data) {

        byte[] bytes = getEmptyArray(16);

        bytes[0] = Constants.REQUEST_SET_ACTIVITY_PERIOD;

        String times = String.format(Locale.ENGLISH, "0x%2d-0x%2d-0x%2d-0x%2d", data.getStartHour(), data.getStartMinute(), data.getEndHour(), data.getEndMinute()).replace(" ", "0");

        byte[] hBytes = HexUtils.hexToBytes(times, "-");
        bytes[1] = hBytes[0];
        bytes[2] = hBytes[1];
        bytes[3] = hBytes[2];
        bytes[4] = hBytes[3];

        bytes[5] = ByteUtils.getWeekByte(data.isMonday(), data.isTuesday(), data.isWednesday(), data.isThursday(), data.isFriday(), data.isSaturday(), data.isSunday());

        bytes[6] = (byte) data.getExerciseReminderPeriod();
        bytes[7] = (byte) data.getMinimumNumberSteps();
        bytes[8] = (byte) (data.isMotionEnabled() ? 0x01 : 0x00);

        byte checksum = calculateCRC(bytes);
        bytes[15] = checksum;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSetBloodPressurePeriod] " + HexUtils.getStringToPrint(bytes));

        sendMessage(bytes);
    }

    /*protected void sendSetBloodPressurePeriod(LifevitSDKVitalPeriod data) {

        byte[] bytes = getEmptyArray(16);

        bytes[0] = Constants.REQUEST_SET_AUTOMATIC_BLOOD_OXYGEN_DETECTION;

        bytes[1] = (byte) data.getWorkingMode();
        String times = String.format(Locale.ENGLISH, "%2d-%2d-%2d-%2d", data.getStartHour(), data.getStartMinute(), data.getEndHour(), data.getEndMinute()).replace(" ", "0");

        byte[] hBytes = HexUtils.hexToBytes(times, "-");
        bytes[2] = hBytes[0];
        bytes[3] = hBytes[1];
        bytes[4] = hBytes[2];
        bytes[5] = hBytes[3];

        bytes[6] = ByteUtils.getWeekByte(data.isMonday(), data.isTuesday(), data.isWednesday(), data.isThursday(), data.isFriday(), data.isSaturday(), data.isSunday());

        if (data.getWorkingMode() == Constants.WORKING_MODE_TIME_INTERVAL) {
            bytes[7] = (byte) (data.getIntervalTime() >> 8 & 0x00ff);
            bytes[8] = (byte) (data.getIntervalTime() & 0x00ff);
        }

        byte checksum = calculateCRC(bytes);
        bytes[15] = checksum;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSetBloodPressurePeriod] " + HexUtils.getStringToPrint(bytes));

        sendMessage(bytes);
    }*/

    protected void sendGetHealthPeriod(Integer type) {

        byte[] bytes = getEmptyArray(16);

        bytes[0] = Constants.REQUEST_GET_AUTOMATIC_DETECTION;
        if (type == LifevitSDKConstants.BraceletVitalDataType.HR.value) {
            bytes[1] = 0x01;
        }
        if (type == LifevitSDKConstants.BraceletVitalDataType.OXIMETER.value) {
            bytes[1] = 0x02;

        }
        if (type == LifevitSDKConstants.BraceletVitalDataType.TEMPERATURE.value) {
            bytes[1] = 0x03;
        }

        byte checksum = calculateCRC(bytes);
        bytes[15] = checksum;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendGetHealthPeriod] " + HexUtils.getStringToPrint(bytes));

        sendMessage(bytes);
    }

    protected void sendSetHealthPeriod(LifevitSDKVitalPeriod data) {

        byte[] bytes = getEmptyArray(16);

        bytes[0] = Constants.REQUEST_SET_AUTOMATIC_DETECTION;

        bytes[1] = (byte) data.getWorkingMode().value;
        if (data.isEnabled()) {
            String times = String.format(Locale.ENGLISH, "0x%2d-0x%2d-0x%2d-0x%2d", data.getStartHour(), data.getStartMinute(), data.getEndHour(), data.getEndMinute()).replace(" ", "0");

            byte[] hBytes = HexUtils.hexToBytes(times, "-");
            bytes[2] = hBytes[0];
            bytes[3] = hBytes[1];
            bytes[4] = hBytes[2];
            bytes[5] = hBytes[3];

            bytes[6] = ByteUtils.getWeekByte(data.isMonday(), data.isTuesday(), data.isWednesday(), data.isThursday(), data.isFriday(), data.isSaturday(), data.isSunday());

            if (data.getWorkingMode().value == Constants.WORKING_MODE_TIME_INTERVAL) {
                bytes[7] = (byte) (data.getIntervalTime() >> 8 & 0x00ff);
                bytes[8] = (byte) (data.getIntervalTime() & 0x00ff);
            }
        }

        if (data.getType().value == LifevitSDKConstants.BraceletVitalDataType.HR.value) {
            bytes[9] = 0x01;
        }
        if (data.getType().value == LifevitSDKConstants.BraceletVitalDataType.OXIMETER.value) {
            bytes[9] = 0x02;

        }
        if (data.getType().value == LifevitSDKConstants.BraceletVitalDataType.TEMPERATURE.value) {
            bytes[9] = 0x03;

        }

        byte checksum = calculateCRC(bytes);
        bytes[15] = checksum;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSetHealthPeriod] " + HexUtils.getStringToPrint(bytes));

        sendMessage(bytes);
    }

    /*protected void sendSetHeartRatePeriod(LifevitSDKVitalPeriod data) {

        byte[] bytes = getEmptyArray(16);

        bytes[0] = Constants.REQUEST_SET_AUTOMATIC_DETECTION;

        bytes[1] = (byte) data.getWorkingMode();
        String times = String.format(Locale.ENGLISH, "%2d-%2d-%2d-%2d", data.getStartHour(), data.getStartMinute(), data.getEndHour(), data.getEndMinute()).replace(" ", "0");
        ;

        byte[] hBytes = HexUtils.hexToBytes(times, "-");
        bytes[2] = hBytes[0];
        bytes[3] = hBytes[1];
        bytes[4] = hBytes[2];
        bytes[5] = hBytes[3];

        bytes[6] = ByteUtils.getWeekByte(data.isMonday(), data.isTuesday(), data.isWednesday(), data.isThursday(), data.isFriday(), data.isSaturday(), data.isSunday());

        if (data.getWorkingMode() == Constants.WORKING_MODE_TIME_INTERVAL) {
            bytes[7] = (byte) (data.getIntervalTime() >> 8 & 0x00ff);
            bytes[8] = (byte) (data.getIntervalTime() & 0x00ff);
        }

        byte checksum = calculateCRC(bytes);
        bytes[15] = checksum;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSetHeartRatePeriod] " + HexUtils.getStringToPrint(bytes));

        sendMessage(bytes);
    }*/


    protected void sendSetHealthControl(LifevitSDKConstants.BraceletVitalDataType type, boolean enable) {
        byte[] bytes = getEmptyArray(16);

        bytes[0] = Constants.REQUEST_BRACELET_HEALTH_MEASUREMENT_CONTROL;

        if (type.value == LifevitSDKConstants.BraceletVitalDataType.HR.value) {
            bytes[1] = Constants.HEALTH_MEASUREMENT_HEARTRATE;
        } else if (type.value == LifevitSDKConstants.BraceletVitalDataType.VITALS.value) {
            bytes[1] = Constants.HEALTH_MEASUREMENT_HRV;
        } else if (type.value == LifevitSDKConstants.BraceletVitalDataType.OXIMETER.value) {
            bytes[1] = Constants.HEALTH_MEASUREMENT_BLOODOXYGEN;
        } else {
            sendingThread.taskFinished();
        }

        bytes[2] = (byte) (enable ? 0x01 : 0x00);

        byte checksum = calculateCRC(bytes);
        bytes[15] = checksum;

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendSetHeartRatePeriod] " + HexUtils.getStringToPrint(bytes));

        sendMessage(bytes);
        //Finalizamos la tarea ya que vendran diversos resultados...
        //No finalizamos en el procesado de datos...
        sendingThread.taskFinished();
    }

    protected byte[] getStandardProcessingBytes(byte command, byte operation) {
        byte[] bytes = getEmptyArray(16);

        bytes[0] = command;
        bytes[1] = operation;

        if (operation == Constants.DATA_OPERATION_READ_MOST_RECENT) {
            //En caso de comenzar hacemos un clear de los arrays...
            this.resetArrays();
        }

        byte checksum = calculateCRC(bytes);
        bytes[15] = checksum;
        return bytes;
    }

    protected void sendGetSportsData() {
        sendGetSportsData(Constants.DATA_OPERATION_READ_MOST_RECENT);
    }

    protected void sendGetSportsData(byte operation) {
        byte[] bytes = getStandardProcessingBytes(Constants.REQUEST_GET_SPORTS_DATA, operation);
        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendGetSportsData] " + HexUtils.getStringToPrint(bytes));
        sendMessage(bytes);
    }

    protected void sendGetVitals() {
        sendGetVitals(Constants.DATA_OPERATION_READ_MOST_RECENT);
    }

    protected void sendGetVitals(byte operation) {
        byte[] bytes = getStandardProcessingBytes(Constants.REQUEST_GET_HRV_DATA, operation);
        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendGetVitals] " + HexUtils.getStringToPrint(bytes));
        sendMessage(bytes);
    }

    protected void sendGetTotalDaySteps() {
        sendGetTotalDaySteps(Constants.DATA_OPERATION_READ_MOST_RECENT);
    }

    protected void sendGetTotalDaySteps(byte operation) {
        isSyncronizingSteps = true;
        byte[] bytes = getStandardProcessingBytes(Constants.REQUEST_GET_TOTAL_STEPS_DATA, operation);
        LogUtils.log(Log.DEBUG, CLASS_TAG, "[getTotalDaySteps] " + HexUtils.getStringToPrint(bytes));
        sendMessage(bytes);
    }

    protected void sendGetOxymeterData() {
        sendGetOxymeterData(Constants.DATA_OPERATION_READ_MOST_RECENT);
    }

    protected void sendGetOxymeterData(byte operation) {
        byte[] bytes = getStandardProcessingBytes(Constants.REQUEST_GET_BLOOD_OXYGEN_DATA, operation);
        LogUtils.log(Log.DEBUG, CLASS_TAG, "[getOxymeterData] " + HexUtils.getStringToPrint(bytes));
        sendMessage(bytes);
    }

    protected void sendGetPeriodicOxymeterData() {
        sendGetPeriodicOxymeterData(Constants.DATA_OPERATION_READ_MOST_RECENT);
    }

    protected void sendGetPeriodicOxymeterData(byte operation) {
        byte[] bytes = getStandardProcessingBytes(Constants.REQUEST_GET_AUTOMATIC_BLOOD_OXYGEN_DATA, operation);
        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendGetPeriodicOxymeterData] " + HexUtils.getStringToPrint(bytes));
        sendMessage(bytes);
    }

    protected void sendGetTemperatureData() {
        sendGetTemperatureData(Constants.DATA_OPERATION_READ_MOST_RECENT);
    }

    protected void sendGetTemperatureData(byte operation) {
        byte[] bytes = getStandardProcessingBytes(Constants.REQUEST_GET_TEMPERATURE_DATA, operation);
        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendGetTemperatureData] " + HexUtils.getStringToPrint(bytes));
        sendMessage(bytes);
    }

    protected void sendGetPeriodicTemperatureData() {
        sendGetPeriodicTemperatureData(Constants.DATA_OPERATION_READ_MOST_RECENT);
    }

    protected void sendGetPeriodicTemperatureData(byte operation) {
        byte[] bytes = getStandardProcessingBytes(Constants.REQUEST_GET_AUTOMATIC_TEMPERATURE_DATA, operation);
        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendGetPeriodicTemperatureData] " + HexUtils.getStringToPrint(bytes));
        sendMessage(bytes);
    }


    protected void sendGetHeartRateData() {
        sendGetHeartRateData(Constants.DATA_OPERATION_READ_MOST_RECENT);
    }

    protected void sendGetHeartRateData(byte operation) {
        byte[] bytes = getStandardProcessingBytes(Constants.REQUEST_GET_HEART_RATE_DATA, operation);
        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendGetHeartRateData] " + HexUtils.getStringToPrint(bytes));
        sendMessage(bytes);
    }


    protected void sendGetPeriodicHeartRateData() {
        sendGetPeriodicHeartRateData(Constants.DATA_OPERATION_READ_MOST_RECENT);
    }

    protected void sendGetPeriodicHeartRateData(byte operation) {
        byte[] bytes = getStandardProcessingBytes(Constants.REQUEST_GET_AUTOMATIC_HEART_RATE_DATA, operation);
        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendGetPeriodicHeartRateData] " + HexUtils.getStringToPrint(bytes));
        sendMessage(bytes);
    }


    protected void sendGetDetailedStepsData() {
        sendGetDetailedStepsData(Constants.DATA_OPERATION_READ_MOST_RECENT);
    }

    protected void sendGetDetailedStepsData(byte operation) {
        isSyncronizingSteps = true;

        byte[] bytes = getStandardProcessingBytes(Constants.REQUEST_GET_DETAILED_STEPS_DATA, operation);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[getDetailedStepsData] " + HexUtils.getStringToPrint(bytes));

        sendMessage(bytes);
    }

    protected void sendGetDetailedSleepData() {
        sendGetDetailedSleepData(Constants.DATA_OPERATION_READ_MOST_RECENT);
    }

    protected void sendGetDetailedSleepData(byte operation) {

        byte[] bytes = getStandardProcessingBytes(Constants.REQUEST_GET_DETAILED_SLEEP_DATA, operation);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[getDetailedSleepData] " + HexUtils.getStringToPrint(bytes));

        sendMessage(bytes);
    }


    protected void sendGetECGWaveform() {
        this.ecgWaveformData = null;
        sendGetECGWaveform(Constants.DATA_OPERATION_READ_MOST_RECENT);
    }

    protected void sendGetECGWaveform(byte operation) {

        byte[] bytes = getStandardProcessingBytes(Constants.REQUEST_GET_ECG_WAVEFORM_SAVED, operation);

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendGetECGWaveform] " + HexUtils.getStringToPrint(bytes));

        sendMessage(bytes);
    }

    // endregion --- Send commands to bracelet ---


    private void resetArrays() {
        this.vitalsArray.clear();
        this.sleepDataArray.clear();
        this.stepDataArray.clear();
        this.oximeterDataArray.clear();
        this.temperatureDataArray.clear();
        this.sportsDataArray.clear();
        this.heartRateDataArray.clear();
    }


    static double[] A_HR = {1.0d, -3.658469528008591d, 5.026987876570873d, -3.078346646055655d, 0.709828779797188d};
    static double[] B_HR = {0.012493658738073d, 0.0d, -0.024987317476146d, 0.0d, 0.012493658738073d};
    static double[] inPut = {0.0d, 0.0d, 0.0d, 0.0d, 0.0d};
    static double[] outPut = {0.0d, 0.0d, 0.0d, 0.0d, 0.0d};

    public static void resetFilterData() {
        A_HR = new double[]{1.0d, -3.658469528008591d, 5.026987876570873d, -3.078346646055655d, 0.709828779797188d};
        B_HR = new double[]{0.012493658738073d, 0.0d, -0.024987317476146d, 0.0d, 0.012493658738073d};
        inPut = new double[]{0.0d, 0.0d, 0.0d, 0.0d, 0.0d};
        outPut = new double[]{0.0d, 0.0d, 0.0d, 0.0d, 0.0d};
    }

    public static double filterEcgData(double d) {
        double[] dArr = inPut;
        dArr[4] = ((d * 18.3d) / 128.0d) + 0.06d;
        double[] dArr2 = outPut;
        double[] dArr3 = B_HR;
        int i = 0;
        double[] dArr4 = A_HR;
        dArr2[4] = ((((((((dArr3[0] * dArr[4]) + (dArr3[1] * dArr[3])) + (dArr3[2] * dArr[2])) +
                (dArr3[3] * dArr[1])) + (dArr3[4] * dArr[0])) - (dArr4[1] * dArr2[3])) -
                (dArr4[2] * dArr2[2])) - (dArr4[3] * dArr2[1])) - (dArr4[4] * dArr2[0]);
        while (i < 4) {
            double[] dArr5 = inPut;
            int i2 = i + 1;
            dArr5[i] = dArr5[i2];
            double[] dArr6 = outPut;
            dArr6[i] = dArr6[i2];
            i = i2;
        }
        return -outPut[4];
    }


}