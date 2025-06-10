package es.lifevit.sdk;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import es.lifevit.sdk.pillreminder.LifevitSDKPillReminderAlarmData;
import es.lifevit.sdk.pillreminder.LifevitSDKPillReminderAlarmListData;
import es.lifevit.sdk.pillreminder.LifevitSDKPillReminderData;
import es.lifevit.sdk.pillreminder.LifevitSDKPillReminderMessageData;
import es.lifevit.sdk.pillreminder.LifevitSDKPillReminderPerformanceData;
import es.lifevit.sdk.pillreminder.PillReminderSendQueue;
import es.lifevit.sdk.utils.HexUtils;
import es.lifevit.sdk.utils.LogUtils;


/**
 * Created by aescanuela on 26/1/16.
 */
public class LifevitSDKBleDevicePillReminder extends LifevitSDKBleDevice {

    private final static String TAG = LifevitSDKBleDevicePillReminder.class.getSimpleName();

    private static final String DEVICE_NAME = "iChoicePR1";

    private int currentRequest = -1;

    private static PillReminderSendQueue sendingThread;

    ArrayList<LifevitSDKPillReminderPerformanceData> records = new ArrayList<LifevitSDKPillReminderPerformanceData>();

    ArrayList<LifevitSDKPillReminderAlarmData> alarms = new ArrayList<LifevitSDKPillReminderAlarmData>();

    ArrayList<byte[]> packetsToSend = new ArrayList();

    byte[] getSchedulePerformanceHistoryArray = new byte[60];
    int getSchedulePerformanceHistoryIndex = 0;

    byte[] getAlarmsScheduleArray = new byte[28];
    int getAlarmsScheduleIndex = 0;

    /**
     * Service Descriptors
     */

    // UUIDs
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String ISSC_SERVICE_UUID = "ba11f08c-5f14-0b0d-10c0-00434d4d4544";
    public static String ISSC_CHAR_RX1_UUID = "0000cd01-0000-1000-8000-00805f9b34fb";
    public static String ISSC_CHAR_RX2_UUID = "0000cd02-0000-1000-8000-00805f9b34fb";
    public static String ISSC_CHAR_RX3_UUID = "0000cd03-0000-1000-8000-00805f9b34fb";
    public static String ISSC_CHAR_RX4_UUID = "0000cd04-0000-1000-8000-00805f9b34fb";
    public static String ISSC_CHAR_TX_UUID = "0000cd20-0000-1000-8000-00805f9b34fb";
    private boolean initialized = false;

    private ArrayList<UUID> descriptors = new ArrayList<>();


    protected LifevitSDKBleDevicePillReminder(BluetoothDevice dev, LifevitSDKManager ch) {
        this.mBluetoothDevice = dev;
        this.mLifevitSDKManager = ch;
    }

    @Override
    protected int getType() {
        return LifevitSDKConstants.DEVICE_PILL_REMINDER;
    }

    /**
     * Receivers
     */

    protected void connectGatt(Context context, boolean firstTime) {
        mBluetoothGatt = mBluetoothDevice.connectGatt(context, false, this.mGattCallback);
        mContext = context;
        mFirstTime = firstTime;

        sendingThread = new PillReminderSendQueue(this);
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
    public void onDescriptorWritten(BluetoothGattDescriptor descriptor) {
        super.onDescriptorWritten(descriptor);

        descriptors.remove(descriptor.getUuid());

        if (descriptors.isEmpty()) {
            new Thread(() -> {
                setDeviceDate();

                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                        Locale.getDefault());
                Date currentLocalTime = calendar.getTime();
                DateFormat date = new SimpleDateFormat("Z");
                String localTime = date.format(currentLocalTime);

                String sign = localTime.substring(0, 1);
                int hour = Integer.parseInt(localTime.substring(1, 3));

                if (!sign.equals("+")) {
                    hour = hour * -1;
                }

                int minute = Integer.parseInt(localTime.substring(3, 5));

                setDeviceTimeZone(hour, minute);
            }).start();

        }
    }


    protected void startReceiver(String action, Intent intent) {
    }


    /**
     * Other methods
     */

    protected static boolean matchDeviceName(String name) {
        return DEVICE_NAME.equalsIgnoreCase(name);
    }


    protected static boolean matchDevice(BluetoothDevice device) {
        return matchDeviceName(device.getName());
    }


    protected static UUID[] getUUIDs() {
        UUID[] uuidArray = new UUID[1];
        uuidArray[0] = UUID.fromString(ISSC_SERVICE_UUID);
        return uuidArray;
    }


    /**
     * "Public" methods
     */

    void getDeviceDate() {
        sendingThread.addToQueue(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_DEVICETIME);
    }

    void setDeviceDate() {
        sendingThread.addToQueue(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_DEVICETIME);
    }

    void getDeviceTimeZone() {
        sendingThread.addToQueue(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_DEVICETIMEZONE);
    }

    void setDeviceTimeZone(int hour, int minute) {
        HashMap<String, Integer> dic = new HashMap<String, Integer>();
        dic.put("hour", hour);
        dic.put("minute", minute);

        sendingThread.addToQueue(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_DEVICETIMEZONE, dic);
    }

    void getBatteryLevel() {
        sendingThread.addToQueue(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_BATTERYLEVEL);
    }

    void getLatestSynchronizationTime() {
        sendingThread.addToQueue(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_LATESTSYNCHRONIZATIONTIME);
    }

    void setSuccessfulSynchronizationStatus() {
        sendingThread.addToQueue(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_SUCCESSFULSYNCHRONIZATIONSTATUS);
    }

    void clearSchedulePerformanceHistory() {
        sendingThread.addToQueue(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_CLEARSCHEDULEPERFORMANCEHISTORY);
    }


    void setAlarmsSchedule(ArrayList<LifevitSDKPillReminderAlarmData> alarms) {
        sendingThread.addToQueue(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_CLEARALARMSCHEDULE);
        sendingThread.addToQueue(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMSSCHEDULE, alarms);
        sendingThread.addToQueue(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_SUCCESSFULSYNCHRONIZATIONSTATUS);
    }

    void getAlarmSchedule() {
        sendingThread.addToQueue(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_ALARMSCHEDULE);
    }

    void getSchedulePerformanceHistory() {
        sendingThread.addToQueue(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_SCHEDULEPERFORMANCEHISTORY);
    }

    void getSchedulePerformanceHistoryEnd() {
        sendingThread.addToQueue(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_SCHEDULEPERFORMANCEHISTORY_END);
    }

    void setAlarmDuration(int duration) {
        Integer iInteger = new Integer(duration);
        sendingThread.addToQueue(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMDURATION, iInteger);
    }

    void setAlarmConfirmationTime(int confirmationTime) {
        Integer iInteger = new Integer(confirmationTime);
        sendingThread.addToQueue(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMCONFIRMATIONTIME, iInteger);
    }

    void clearAlarmSchedule() {
        sendingThread.addToQueue(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_CLEARALARMSCHEDULE);
    }


    /**
     * Enable Device Notification
     *
     * @return
     */
    protected void enableDeviceNotification() {

        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(ISSC_SERVICE_UUID));
        if (RxService == null) {
            LogUtils.log(Log.ERROR, TAG, "Rx service not found!");
            return;
        }

//Guardamos los descriptors de notificacion..
        for (BluetoothGattCharacteristic txChar : RxService.getCharacteristics()
        ) {

            for (BluetoothGattDescriptor descriptor : txChar.getDescriptors()
            ) {
                descriptors.add(descriptor.getUuid());
            }
        }

        //Seteamos la lectura de características y descriptors.
        for (BluetoothGattCharacteristic txChar : RxService.getCharacteristics()
        ) {
            LogUtils.log(Log.DEBUG, TAG, "Tx charateristic found: " + txChar.getUuid().toString());
            mBluetoothGatt.setCharacteristicNotification(txChar, true);

            for (BluetoothGattDescriptor descriptor : txChar.getDescriptors()
            ) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                write(descriptor);
            }
        }

//        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(UUID.fromString(ISSC_CHAR_RX1_UUID));
//        if (TxChar == null) {
//            LogUtils.log(Log.ERROR, TAG, "Tx charateristic not found!");
//            return;
//        }
//        mBluetoothGatt.setCharacteristicNotification(TxChar, true);
//
//        TxChar = RxService.getCharacteristic(UUID.fromString(ISSC_CHAR_RX2_UUID));
//        if (TxChar == null) {
//            LogUtils.log(Log.ERROR, TAG, "Tx charateristic not found!");
//            return;
//        }
//        mBluetoothGatt.setCharacteristicNotification(TxChar, true);
//
//        TxChar = RxService.getCharacteristic(UUID.fromString(ISSC_CHAR_RX3_UUID));
//        if (TxChar == null) {
//            LogUtils.log(Log.ERROR, TAG, "Tx charateristic not found!");
//            return;
//        }
//        mBluetoothGatt.setCharacteristicNotification(TxChar, true);
//
//        TxChar = RxService.getCharacteristic(UUID.fromString(ISSC_CHAR_RX4_UUID));
//        if (TxChar == null) {
//            LogUtils.log(Log.ERROR, TAG, "Tx charateristic not found!");
//            return;
//        }
//        mBluetoothGatt.setCharacteristicNotification(TxChar, true);

        // This is specific to Heart Rate Measurement.
        // BluetoothGattDescriptor descriptor = TxChar.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
        //descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

    }


    protected synchronized void sendMessage(byte[] data) {

        if (mBluetoothGatt != null) {
            BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(ISSC_SERVICE_UUID));
            LogUtils.log(Log.DEBUG, TAG, "mBluetoothGatt: " + mBluetoothGatt);
            if (RxService == null) {
                LogUtils.log(Log.ERROR, TAG, "Rx service not found!");
                return;
            }
            BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(UUID.fromString(ISSC_CHAR_TX_UUID));
            if (RxChar == null) {
                LogUtils.log(Log.ERROR, TAG, "Rx charateristic not found!");
                return;
            }

            dividePacketsAndSendMessage(RxChar, data, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        }
    }


    protected synchronized void characteristicReadProcessData(BluetoothGattCharacteristic characteristic) {

        final byte[] bytes = characteristic.getValue();

        LogUtils.log(Log.DEBUG, TAG, "[RECEIVED]: " + HexUtils.getStringToPrint(bytes) + " [" + characteristic.getUuid() + "]");

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.

        if ((characteristic.getUuid().equals(UUID.fromString(ISSC_CHAR_RX1_UUID))) ||
                (characteristic.getUuid().equals(UUID.fromString(ISSC_CHAR_RX2_UUID))) ||
                (characteristic.getUuid().equals(UUID.fromString(ISSC_CHAR_RX3_UUID))) ||
                (characteristic.getUuid().equals(UUID.fromString(ISSC_CHAR_RX4_UUID)))) {

            if (bytes != null) {

                if (!processRealTimeAlarmStatusMessage(bytes)) {
                    switch (currentRequest) {
                        case LifevitSDKConstants.PILLREMINDER_REQUEST_SET_DEVICETIME:
                            processSetDeviceTime(bytes);
                            break;
                        case LifevitSDKConstants.PILLREMINDER_REQUEST_GET_DEVICETIME:
                            processGetDeviceTime(bytes);
                            break;
                        case LifevitSDKConstants.PILLREMINDER_REQUEST_SET_DEVICETIMEZONE:
                            processSetDeviceTimeZone(bytes);
                            break;
                        case LifevitSDKConstants.PILLREMINDER_REQUEST_GET_DEVICETIMEZONE:
                            processGetDeviceTimeZone(bytes);
                            break;
                        case LifevitSDKConstants.PILLREMINDER_REQUEST_GET_BATTERYLEVEL:
                            processGetBatteryLevel(bytes);
                            break;
                        case LifevitSDKConstants.PILLREMINDER_REQUEST_GET_LATESTSYNCHRONIZATIONTIME:
                            processGetLatestSynchronizationTime(bytes);
                            break;
                        case LifevitSDKConstants.PILLREMINDER_REQUEST_SET_SUCCESSFULSYNCHRONIZATIONSTATUS:
                            processSetLatestSynchronizationTime(bytes);
                            break;
                        case LifevitSDKConstants.PILLREMINDER_REQUEST_GET_CLEARSCHEDULEPERFORMANCEHISTORY:
                            processGetClearSchedulePerformanceHistory(bytes);
                            break;
                        case LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMSSCHEDULE:
                            processSetAlarmsSchedule(bytes);
                            break;
                        case LifevitSDKConstants.PILLREMINDER_REQUEST_GET_ALARMSCHEDULE:
                            processGetAlarmSchedule(bytes);
                            break;
                        case LifevitSDKConstants.PILLREMINDER_REQUEST_GET_SCHEDULEPERFORMANCEHISTORY:
                            processGetSchedulePerformanceHistory(bytes);
                            break;
                        case LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMDURATION:
                            processSetAlarmDuration(bytes);
                            break;
                        case LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMCONFIRMATIONTIME:
                            processSetAlarmConfirmationTime(bytes);
                            break;
                        case LifevitSDKConstants.PILLREMINDER_REQUEST_GET_CLEARALARMSCHEDULE:
                            processGetClearAlarmSchedule(bytes);
                            break;

                    }


                }

            }

        }
    }


    Boolean processRealTimeAlarmStatusMessage(byte[] bytes) {

        byte byte1 = bytes[0];
        byte byte2 = bytes[1];
        byte byte3 = bytes[2];
        byte byte4 = bytes[3];


        if ((byte2 == (byte) 0x00aa) && (byte4 == (byte) 0x00f1)) {

            byte byte5 = bytes[4];

            byte byte6 = bytes[5];
            byte byte7 = bytes[6];
            byte byte8 = bytes[7];
            byte byte9 = bytes[8];
            byte byte10 = bytes[9];

            byte byte11 = bytes[10];
            byte byte12 = bytes[11];

            byte byte13 = bytes[12];

            int alarmNumber = (int) byte5;

            if (alarmNumber == 0) {
                return true;
            }

            int year = (int) byte6;
            int month = (int) byte7 - 1;
            int day = (int) byte8;
            int hour = (int) byte9;
            int minute = (int) byte10;

            int hourPlanned = (int) byte11;
            int minutePlanned = (int) byte12;

            int taken = (int) byte13;

            LifevitSDKPillReminderPerformanceData alarmPerformanceItem = getAlarmPerformanceItem(alarmNumber, year, month, day, hour, minute, taken, hourPlanned, minutePlanned);

            if (alarmPerformanceItem != null) {
                if (mLifevitSDKManager.getPillReminderListener() != null) {
                    mLifevitSDKManager.getPillReminderListener().pillReminderOnResult(alarmPerformanceItem);
                    return true;
                }
            }
        }

        return false;
    }

    LifevitSDKPillReminderPerformanceData getAlarmPerformanceItem(int alarmNumber, int year, int month, int day,
                                                                  int hour, int minute, int takenStatus, int hourPlanned, int minutePlanned) {

        if (year == 0 && month == 0 && day == 0) {
            return null;
        }

        LifevitSDKPillReminderPerformanceData alarmItem = new LifevitSDKPillReminderPerformanceData();

        int year2000 = year + 2000;

        Calendar datePlanned = Calendar.getInstance();
        datePlanned.set(Calendar.YEAR, year2000);
        datePlanned.set(Calendar.MONTH, month);
        datePlanned.set(Calendar.DAY_OF_MONTH, day);
        datePlanned.set(Calendar.HOUR_OF_DAY, hourPlanned);
        datePlanned.set(Calendar.MINUTE, minutePlanned);
        datePlanned.set(Calendar.SECOND, 0);
        datePlanned.set(Calendar.MILLISECOND, 0);

        Calendar dateTaken = Calendar.getInstance();
        dateTaken.set(Calendar.YEAR, year2000);
        dateTaken.set(Calendar.MONTH, month);
        dateTaken.set(Calendar.DAY_OF_MONTH, day);
        dateTaken.set(Calendar.HOUR_OF_DAY, hour);
        dateTaken.set(Calendar.MINUTE, minute);
        dateTaken.set(Calendar.SECOND, 0);
        dateTaken.set(Calendar.MILLISECOND, 0);

        long dateP = datePlanned.getTimeInMillis();
        long dateT = dateTaken.getTimeInMillis();

        alarmItem.setAlarmNumber(alarmNumber);
        alarmItem.setDate(dateP);
        alarmItem.setDateTaken(dateT);
        alarmItem.setStatusTaken(takenStatus);

        return alarmItem;
    }


    void processSetDeviceTime(byte[] bytes) {


        byte byte3 = bytes[3];
        byte byte4 = bytes[4];

        if ((byte3 == (byte) 0x00f0) && (byte4 == (byte) 0x00f2)) {
            LifevitSDKPillReminderMessageData info = new LifevitSDKPillReminderMessageData();
            info.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_DEVICETIME);
            info.setMessageText("Device date setting successful");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnResult(info);
            }

        } else {
            LifevitSDKPillReminderMessageData message = new LifevitSDKPillReminderMessageData();
            message.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_DEVICETIME);
            message.setMessageText("Error setting device date");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnError(message);
            }
        }
        sendingThread.taskFinished();
        currentRequest = -1;
    }


    void processGetDeviceTime(byte[] bytes) {


        byte byte2 = bytes[2];
        byte byte3 = bytes[3];

        if ((byte2 == (byte) 0x08) && (byte3 == (byte) 0x00b2)) {

            int year = bytes[4] + 2000;
            int month = bytes[5];
            int day = bytes[6];
            int hour = bytes[7];
            int minutes = bytes[8];
            int seconds = bytes[9];


            Calendar date = Calendar.getInstance();
            date.set(Calendar.YEAR, year);
            date.set(Calendar.MONTH, month - 1);
            date.set(Calendar.DAY_OF_MONTH, day);
            date.set(Calendar.HOUR_OF_DAY, hour);
            date.set(Calendar.MINUTE, minutes);
            date.set(Calendar.SECOND, seconds);

            long dateL = date.getTimeInMillis();

            LifevitSDKPillReminderData info = new LifevitSDKPillReminderData();
            info.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_DEVICETIME);
            info.setDate(dateL);

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnResult(info);
            }

        } else {
            LifevitSDKPillReminderMessageData message = new LifevitSDKPillReminderMessageData();
            message.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_DEVICETIME);
            message.setMessageText("Error setting device date");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnError(message);
            }
        }
        sendingThread.taskFinished();
        currentRequest = -1;
    }

    void processGetDeviceTimeZone(byte[] bytes) {


        byte byte2 = bytes[2];
        byte byte3 = bytes[3];

        if ((byte2 == (byte) 0x04) && (byte3 == (byte) 0x00cd)) {

            int hour = bytes[4];
            int minute = bytes[5];

            String dateString;
            if (hour >= 0) {
                dateString = String.format("+%02d:%02d UTC", hour, minute);
            } else {
                dateString = String.format("-%02d:%02d UTC", hour, minute);
            }


            LifevitSDKPillReminderMessageData info = new LifevitSDKPillReminderMessageData();
            info.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_DEVICETIMEZONE);
            info.setMessageText(dateString);

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnResult(info);
            }

        } else {
            LifevitSDKPillReminderMessageData message = new LifevitSDKPillReminderMessageData();
            message.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_DEVICETIMEZONE);
            message.setMessageText("Error getting timezone");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnError(message);
            }
        }
        sendingThread.taskFinished();
        currentRequest = -1;
    }

    void processSetDeviceTimeZone(byte[] bytes) {


        byte byte3 = bytes[3];
        byte byte4 = bytes[4];

        if ((byte3 == (byte) 0x00f0) && (byte4 == (byte) 0x00f2)) {

            LifevitSDKPillReminderMessageData info = new LifevitSDKPillReminderMessageData();
            info.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_DEVICETIMEZONE);
            info.setMessageText("Timezone set successfully");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnResult(info);
            }

        } else {
            LifevitSDKPillReminderMessageData message = new LifevitSDKPillReminderMessageData();
            message.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_DEVICETIMEZONE);
            message.setMessageText("Error setting timezone");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnError(message);
            }
        }
        sendingThread.taskFinished();
        currentRequest = -1;
    }


    void processGetBatteryLevel(byte[] bytes) {


        byte byte2 = bytes[2];
        byte byte3 = bytes[3];


        if ((byte2 == (byte) 0x03) && (byte3 == (byte) 0x00b9)) {

            int level = bytes[4];

            LifevitSDKPillReminderMessageData info = new LifevitSDKPillReminderMessageData();
            info.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_BATTERYLEVEL);
            info.setMessageText(String.valueOf(level));

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnResult(info);
            }

        } else {
            LifevitSDKPillReminderMessageData message = new LifevitSDKPillReminderMessageData();
            message.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_BATTERYLEVEL);
            message.setMessageText("Error getting battery level");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnError(message);
            }
        }
        sendingThread.taskFinished();
        currentRequest = -1;
    }


    void processGetLatestSynchronizationTime(byte[] bytes) {


        byte byte2 = bytes[2];
        byte byte3 = bytes[3];

        if ((byte2 == (byte) 0x08) && (byte3 == (byte) 0x00cf)) {

            int year = bytes[4] + 2000;
            int month = bytes[5];
            int day = bytes[6];
            int hour = bytes[7];
            int minutes = bytes[8];
            int seconds = bytes[9];


            Calendar date = Calendar.getInstance();
            date.set(Calendar.YEAR, year);
            date.set(Calendar.MONTH, month - 1);
            date.set(Calendar.DAY_OF_MONTH, day);
            date.set(Calendar.HOUR_OF_DAY, hour);
            date.set(Calendar.MINUTE, minutes);
            date.set(Calendar.SECOND, seconds);

            long dateL = date.getTimeInMillis();

            LifevitSDKPillReminderData info = new LifevitSDKPillReminderData();
            info.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_LATESTSYNCHRONIZATIONTIME);
            info.setDate(dateL);

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnResult(info);
            }

        } else {
            LifevitSDKPillReminderMessageData message = new LifevitSDKPillReminderMessageData();
            message.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_LATESTSYNCHRONIZATIONTIME);
            message.setMessageText("Error getting latest synchronization time");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnError(message);
            }
        }
        sendingThread.taskFinished();
        currentRequest = -1;
    }


    void processSetLatestSynchronizationTime(byte[] bytes) {


        byte byte2 = bytes[2];
        byte byte3 = bytes[3];
        byte byte4 = bytes[4];

        if ((byte2 == (byte) 0x02) && (byte3 == (byte) 0x00f0) && (byte4 == (byte) 0x00f2)) {

            LifevitSDKPillReminderMessageData info = new LifevitSDKPillReminderMessageData();
            info.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_SUCCESSFULSYNCHRONIZATIONSTATUS);
            info.setMessageText("Device successfully synchronized");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnResult(info);
            }

        } else {
            LifevitSDKPillReminderMessageData message = new LifevitSDKPillReminderMessageData();
            message.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_SUCCESSFULSYNCHRONIZATIONSTATUS);
            message.setMessageText("Error synchronizing status");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnError(message);
            }
        }
        sendingThread.taskFinished();
        currentRequest = -1;
    }


    void processGetClearSchedulePerformanceHistory(byte[] bytes) {


        byte byte2 = bytes[2];
        byte byte3 = bytes[3];
        byte byte4 = bytes[4];

        if ((byte2 == (byte) 0x02) && (byte3 == (byte) 0x00f0) && (byte4 == (byte) 0x00f2)) {

            LifevitSDKPillReminderMessageData info = new LifevitSDKPillReminderMessageData();
            info.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_CLEARSCHEDULEPERFORMANCEHISTORY);
            info.setMessageText("Device successfully cleared schedule performance history");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnResult(info);
            }

        } else {
            LifevitSDKPillReminderMessageData message = new LifevitSDKPillReminderMessageData();
            message.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_CLEARSCHEDULEPERFORMANCEHISTORY);
            message.setMessageText("Error clearing schedule performance history");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnError(message);
            }
        }
        sendingThread.taskFinished();
        currentRequest = -1;
    }

    void processSetAlarmsSchedule(byte[] bytes) {


        byte byte2 = bytes[2];
        byte byte3 = bytes[3];
        byte byte4 = bytes[4];

        if ((byte2 == (byte) 0x02) && (byte3 == (byte) 0x00f0) && (byte4 == (byte) 0x00f2)) {

            LifevitSDKPillReminderMessageData info = new LifevitSDKPillReminderMessageData();
            info.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMSSCHEDULE);
            info.setMessageText("Alarms set successfully");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnResult(info);
            }

        } else if ((byte2 == (byte) 0x02) && (byte3 == (byte) 0x00fe)) {
            LifevitSDKPillReminderMessageData message = new LifevitSDKPillReminderMessageData();
            message.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMSSCHEDULE);
            message.setMessageText("Error in alarms schedule");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnError(message);
            }


        } else {
            LifevitSDKPillReminderMessageData message = new LifevitSDKPillReminderMessageData();
            message.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMSSCHEDULE);
            message.setMessageText("Hardware failure");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnError(message);
            }
        }

        //Eliminamos el primer paquete
        packetsToSend.remove(0);

        if (packetsToSend.size() == 0) {
            currentRequest = -1;
            sendingThread.taskFinished();
            //setSuccessfulSynchronizationStatus();
        } else {
            byte[] bArr = packetsToSend.get(0);
            sendMessage(bArr);
        }

    }

    LifevitSDKPillReminderAlarmData getAlarmItem(int year, int month, int day, int alarm, int alarm2) {

        if ((alarm == 255) && (alarm2 == 255)) {
            return null;
        }

        int hour = ((alarm >> 3) & 0x001f);

        byte minute00 = (byte) (((alarm & 0x07) << 3) & 0x0038);
        byte minute01 = (byte) ((alarm2 >> 5) & 0x07);


        int minutes = ((minute00 | minute01) & 0x003f);
        int color = (alarm2 & 0x001f);


        LifevitSDKPillReminderAlarmData alarmItem = new LifevitSDKPillReminderAlarmData();

        int year2000 = year + 2000;

        Calendar datePlanned = Calendar.getInstance();
        datePlanned.set(Calendar.YEAR, year2000);
        datePlanned.set(Calendar.MONTH, month - 1);
        datePlanned.set(Calendar.DAY_OF_MONTH, day);
        datePlanned.set(Calendar.HOUR_OF_DAY, hour);
        datePlanned.set(Calendar.MINUTE, minutes);
        datePlanned.set(Calendar.SECOND, 0);

        long dateP = datePlanned.getTimeInMillis();

        alarmItem.setDate(dateP);
        alarmItem.setColor(color);

        return alarmItem;
    }


    void processGetAlarmSchedule(byte[] bytes) {


        byte byte2 = bytes[2];
        byte byte3 = bytes[3];


        if (bytes.length == 5) {
            if ((byte2 == (byte) 0x02) && (byte3 == (byte) 0x00f0)) {
                //FINISHED HISTORY RECORDS
                LifevitSDKPillReminderAlarmListData info = new LifevitSDKPillReminderAlarmListData();

                info.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_ALARMSCHEDULE);
                info.setAlarmList(alarms);

                if (mLifevitSDKManager.getPillReminderListener() != null) {
                    mLifevitSDKManager.getPillReminderListener().pillReminderOnResult(info);
                }
                currentRequest = -1;
                sendingThread.taskFinished();
                alarms = null;
            }
        } else if ((bytes.length == 20) || (bytes.length == 8)) {

            for (int i = 0; i <= bytes.length - 1; i++) {
                getAlarmsScheduleArray[getAlarmsScheduleIndex] = bytes[i];
                getAlarmsScheduleIndex++;
            }

            if (getAlarmsScheduleIndex == 28) {

                byte byte3a = getAlarmsScheduleArray[3];

                if (byte3a == (byte) 0x00d3) {

                    byte date1 = getAlarmsScheduleArray[4];
                    byte date2 = getAlarmsScheduleArray[5];

                    int year = ((date1 >> 1) & 0x007f);
                    int month = ((((date1 & 0x01) << 3) + (date2 >> 5 & 0x07)) & 0x000f);
                    int day = (date2 & 0x001f);


                    int numberAlarms = getAlarmsScheduleArray[6];

                    for (int i = 7; i < 27; i = i + 2) {

                        byte alarm1 = getAlarmsScheduleArray[i];
                        byte alarm1b = getAlarmsScheduleArray[i + 1];

                        if ((alarm1 == (byte) 0x00ff) && (alarm1b == (byte) 0x00ff)) {
                            continue;
                        }

                        LifevitSDKPillReminderAlarmData alarmItem = getAlarmItem(year, month, day, alarm1, alarm1b);

                        if (alarmItem != null) {
                            alarms.add(alarmItem);
                        }

                    }

                    getAlarmsScheduleArray = new byte[28];
                    getAlarmsScheduleIndex = 0;
                }
            }
        } else if (!(byte2 == (byte) 0x00ff) && !(byte3 == (byte) 0x00ff)) {

            LifevitSDKPillReminderMessageData message = new LifevitSDKPillReminderMessageData();
            message.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_ALARMSCHEDULE);
            message.setMessageText("Error getting alarm schedule");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnError(message);
            }
            currentRequest = -1;
            sendingThread.taskFinished();
            alarms = null;
        }

    }


    void processGetSchedulePerformanceHistory(byte[] bytes) {

        byte byte2 = bytes[2];
        byte byte3 = bytes[3];
        byte byte4 = bytes[4];

        if (bytes.length == 5) {


            if ((byte2 == (byte) 0x02) && (byte3 == (byte) 0x00f0) && (byte4 == (byte) 0x00f2)) {
                //FINISHED HISTORY RECORDS
                LifevitSDKPillReminderAlarmListData info = new LifevitSDKPillReminderAlarmListData();

                info.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_SCHEDULEPERFORMANCEHISTORY);
                info.setAlarmList(records);

                if (mLifevitSDKManager.getPillReminderListener() != null) {
                    mLifevitSDKManager.getPillReminderListener().pillReminderOnResult(info);
                }
                currentRequest = -1;
                sendingThread.taskFinished();
                records = null;
            }
        } else if (bytes.length == 20) {

            for (int i = 0; i <= bytes.length - 1; i++) {
                getSchedulePerformanceHistoryArray[getSchedulePerformanceHistoryIndex] = bytes[i];
                getSchedulePerformanceHistoryIndex++;
            }


            if (getSchedulePerformanceHistoryIndex == 60) {

                byte byte3a = getSchedulePerformanceHistoryArray[3];

                if (byte3a == (byte) 0x00d5) {

                    byte numberRecords = getSchedulePerformanceHistoryArray[4];

                    for (int i = 5; i < 59; i = i + 6) {

                        byte record1 = getSchedulePerformanceHistoryArray[i];
                        byte record2 = getSchedulePerformanceHistoryArray[i + 1];
                        byte record3 = getSchedulePerformanceHistoryArray[i + 2];
                        byte record4 = getSchedulePerformanceHistoryArray[i + 3];
                        byte record5 = getSchedulePerformanceHistoryArray[i + 4];
                        byte record6 = getSchedulePerformanceHistoryArray[i + 5];

                        if ((record1 != (byte) 0x00ff) && (record2 != (byte) 0x00ff) && (record3 != (byte) 0x00ff) && (record4 != (byte) 0x00ff)
                                && (record5 != (byte) 0x00ff) && (record6 != (byte) 0x00ff)) {

                            int alarmNumber = (record1 >> 4 & 0x000f);
                            int year = (((record1 & 0x000f) << 2) + ((record2 >> 6) & 0x03));
                            int month = ((record2 >> 2) & 0x000f);
                            int day = (((record2 & 0x03) << 3) + ((record3 >> 5) & 0x07));
                            int hour = (record3 & 0x001f);
                            int minute = ((record4 >> 2) & 0x003f);
                            int taken = (record4 & 0x03);
                            int hourPlanned = (record5 & 0x00ff);
                            int minutePlanned = (record6 & 0x00ff);

                            LifevitSDKPillReminderPerformanceData alarmPerformanceItem = getAlarmPerformanceItem(alarmNumber, year, month, day, hour, minute, taken, hourPlanned, minutePlanned);

                            if (alarmPerformanceItem != null) {
                                records.add(alarmPerformanceItem);
                            }
                        }
                    }

                    getSchedulePerformanceHistoryArray = new byte[60];
                    getSchedulePerformanceHistoryIndex = 0;
                }
            }
        } else if (!(byte2 == (byte) 0x00ff) && !(byte3 == (byte) 0x00ff)) {

            LifevitSDKPillReminderMessageData message = new LifevitSDKPillReminderMessageData();
            message.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_SCHEDULEPERFORMANCEHISTORY);
            message.setMessageText("Error getting schedule performance history");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnError(message);
            }
            currentRequest = -1;
            sendingThread.taskFinished();
            alarms = null;
        }
    }


    void processSetAlarmDuration(byte[] bytes) {


        byte byte2 = bytes[2];
        byte byte3 = bytes[3];
        byte byte4 = bytes[4];

        if ((byte2 == (byte) 0x02) && (byte3 == (byte) 0x00f0) && (byte4 == (byte) 0x00f2)) {

            LifevitSDKPillReminderMessageData info = new LifevitSDKPillReminderMessageData();
            info.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMDURATION);
            info.setMessageText("Alarm duration set successfully");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnResult(info);
            }

        } else {
            LifevitSDKPillReminderMessageData message = new LifevitSDKPillReminderMessageData();
            message.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMDURATION);
            message.setMessageText("Error setting alarm duration");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnError(message);
            }
        }
        sendingThread.taskFinished();
        currentRequest = -1;
    }


    void processSetAlarmConfirmationTime(byte[] bytes) {


        byte byte2 = bytes[2];
        byte byte3 = bytes[3];
        byte byte4 = bytes[4];

        if ((byte2 == (byte) 0x02) && (byte3 == (byte) 0x00f0) && (byte4 == (byte) 0x00f2)) {

            LifevitSDKPillReminderMessageData info = new LifevitSDKPillReminderMessageData();
            info.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMCONFIRMATIONTIME);
            info.setMessageText("Alarm confirmation time set successfully");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnResult(info);
            }

        } else {
            LifevitSDKPillReminderMessageData message = new LifevitSDKPillReminderMessageData();
            message.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMCONFIRMATIONTIME);
            message.setMessageText("Error setting alarm confirmation time");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnError(message);
            }
        }
        sendingThread.taskFinished();
        currentRequest = -1;
    }


    void processGetClearAlarmSchedule(byte[] bytes) {

        byte byte2 = bytes[2];
        byte byte3 = bytes[3];
        byte byte4 = bytes[4];

        if ((byte2 == (byte) 0x02) && (byte3 == (byte) 0x00f0) && (byte4 == (byte) 0x00f2)) {

            LifevitSDKPillReminderMessageData info = new LifevitSDKPillReminderMessageData();
            info.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_CLEARALARMSCHEDULE);
            info.setMessageText("Alarm schedule cleared successfully");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnResult(info);
            }

        } else {
            LifevitSDKPillReminderMessageData message = new LifevitSDKPillReminderMessageData();
            message.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_GET_CLEARALARMSCHEDULE);
            message.setMessageText("Error clearing alarm schedule");

            if (mLifevitSDKManager.getPillReminderListener() != null) {
                mLifevitSDKManager.getPillReminderListener().pillReminderOnError(message);
            }
        }
        sendingThread.taskFinished();
        currentRequest = -1;
    }


    /*********************************************************************************************/
    /**************************************  Helper methods  *************************************/
    /*********************************************************************************************/

/*
    private byte hexaToInt(int number) {
        //
        int numberOnlyTwoDigits = number % 100;
        int tens = numberOnlyTwoDigits / 16;
        int ones = numberOnlyTwoDigits % 16;
        int result = tens * 10 + ones;
        return (byte) result;
    }


    private byte intToHexa(int number) {
        //
        int numberOnlyTwoDigits = number % 100;
        int tens = numberOnlyTwoDigits / 10;
        int ones = numberOnlyTwoDigits % 10;
        //byte result = (byte) (((tens & 0b00001111) << 4) + (ones & 0b00001111));
        int result = tens * 16 + ones;

        LogUtils.log(Log.DEBUG, TAG, "Entrada: " + number + ", Salida: " + result + ", tens: " + tens + ", ones: " + ones);

        return (byte) result;
    }
*/
    public void sendGetDeviceTime() {
        LogUtils.log(Log.DEBUG, TAG, "getDeviceTime");

        byte[] bArr = new byte[5];

        bArr[0] = (byte) 0x00AA;
        bArr[1] = (byte) 0x0055;
        bArr[2] = (byte) 0x02;
        bArr[3] = (byte) 0x00D2;
        bArr[4] = (byte) 0x00D4;

        currentRequest = LifevitSDKConstants.PILLREMINDER_REQUEST_GET_DEVICETIME;

        LogUtils.log(Log.DEBUG, TAG, "Sending: " + HexUtils.getStringToPrint(bArr));

        sendMessage(bArr);
    }

    public void sendSetDeviceTime() {
        LogUtils.log(Log.DEBUG, TAG, "setDeviceTime");

        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1; // Note: zero based!
        int day = now.get(Calendar.DAY_OF_MONTH);
        int weekday = now.get(Calendar.DAY_OF_WEEK);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);

        int year2000 = year - 2000;


        byte[] bArr = new byte[12];

        bArr[0] = (byte) 0x00AA;
        bArr[1] = (byte) 0x0055;
        bArr[2] = (byte) 0x09;
        bArr[3] = (byte) 0x00C2;
        bArr[4] = (byte) (year2000 & 0x00ff);
        bArr[5] = (byte) (month & 0x00ff);
        bArr[6] = (byte) (day & 0x00ff);
        bArr[7] = (byte) (weekday & 0x00ff);
        bArr[8] = (byte) (hour & 0x00ff);
        bArr[9] = (byte) (minute & 0x00ff);
        bArr[10] = (byte) (second & 0x00ff);

        bArr[11] = getDatasXor(bArr); // xor byte

        currentRequest = LifevitSDKConstants.PILLREMINDER_REQUEST_SET_DEVICETIME;

        LogUtils.log(Log.DEBUG, TAG, "Sending: " + HexUtils.getStringToPrint(bArr));

        sendMessage(bArr);
    }


    public void sendGetDeviceTimeZone() {
        LogUtils.log(Log.DEBUG, TAG, "getDeviceTimeZone");

        byte[] bArr = new byte[5];

        bArr[0] = (byte) 0x00AA;
        bArr[1] = (byte) 0x0055;
        bArr[2] = (byte) 0x02;
        bArr[3] = (byte) 0x00CD;
        bArr[4] = (byte) 0x00CF;

        currentRequest = LifevitSDKConstants.PILLREMINDER_REQUEST_GET_DEVICETIMEZONE;

        LogUtils.log(Log.DEBUG, TAG, "Sending: " + HexUtils.getStringToPrint(bArr));

        sendMessage(bArr);
    }

    public void sendSetDeviceTimeZone(int hour, int minute) {
        LogUtils.log(Log.DEBUG, TAG, "setDeviceTimeZone");

        byte[] bArr = new byte[7];

        bArr[0] = (byte) 0x00AA;
        bArr[1] = (byte) 0x0055;
        bArr[2] = (byte) 0x04;
        bArr[3] = (byte) 0x00C4;
        bArr[4] = (byte) (hour & 0x00ff);
        bArr[5] = (byte) (minute & 0x00ff);

        bArr[6] = getDatasXor(bArr); // xor byte

        currentRequest = LifevitSDKConstants.PILLREMINDER_REQUEST_SET_DEVICETIMEZONE;

        LogUtils.log(Log.DEBUG, TAG, "Sending: " + HexUtils.getStringToPrint(bArr));

        sendMessage(bArr);
    }

    public void sendGetBatteryLevel() {
        LogUtils.log(Log.DEBUG, TAG, "getBatteryLevel");

        byte[] bArr = new byte[5];

        bArr[0] = (byte) 0x00AA;
        bArr[1] = (byte) 0x0055;
        bArr[2] = (byte) 0x02;
        bArr[3] = (byte) 0x00CE;
        bArr[4] = (byte) 0x00D0;


        currentRequest = LifevitSDKConstants.PILLREMINDER_REQUEST_GET_BATTERYLEVEL;

        LogUtils.log(Log.DEBUG, TAG, "Sending: " + HexUtils.getStringToPrint(bArr));

        sendMessage(bArr);
    }

    public void sendGetLatestSynchronizationTime() {
        LogUtils.log(Log.DEBUG, TAG, "getLatestSynchronizationTime");

        byte[] bArr = new byte[5];

        bArr[0] = (byte) 0x00AA;
        bArr[1] = (byte) 0x0055;
        bArr[2] = (byte) 0x02;
        bArr[3] = (byte) 0x00CF;
        bArr[4] = (byte) 0x00D1;


        currentRequest = LifevitSDKConstants.PILLREMINDER_REQUEST_GET_LATESTSYNCHRONIZATIONTIME;

        LogUtils.log(Log.DEBUG, TAG, "Sending: " + HexUtils.getStringToPrint(bArr));

        sendMessage(bArr);
    }

    public void sendSetSuccessfulSynchronizationStatus() {
        LogUtils.log(Log.DEBUG, TAG, "setSuccessfulSynchronizationStatus");

        byte[] bArr = new byte[5];

        bArr[0] = (byte) 0x00AA;
        bArr[1] = (byte) 0x0055;
        bArr[2] = (byte) 0x02;
        bArr[3] = (byte) 0x00D0;
        bArr[4] = (byte) 0x00D2;


        currentRequest = LifevitSDKConstants.PILLREMINDER_REQUEST_SET_SUCCESSFULSYNCHRONIZATIONSTATUS;

        LogUtils.log(Log.DEBUG, TAG, "Sending: " + HexUtils.getStringToPrint(bArr));

        sendMessage(bArr);
    }

    public void sendClearSchedulePerformanceHistory() {
        LogUtils.log(Log.DEBUG, TAG, "clearSchedulePerformanceHistory");

        byte[] bArr = new byte[5];

        bArr[0] = (byte) 0x00AA;
        bArr[1] = (byte) 0x0055;
        bArr[2] = (byte) 0x02;
        bArr[3] = (byte) 0x00D1;
        bArr[4] = (byte) 0x00D3;


        currentRequest = LifevitSDKConstants.PILLREMINDER_REQUEST_GET_CLEARSCHEDULEPERFORMANCEHISTORY;

        LogUtils.log(Log.DEBUG, TAG, "Sending: " + HexUtils.getStringToPrint(bArr));

        sendMessage(bArr);
    }

    public void sendGetAlarmSchedule() {
        LogUtils.log(Log.DEBUG, TAG, "getAlarmSchedule");

        byte[] bArr = new byte[5];

        bArr[0] = (byte) 0x00AA;
        bArr[1] = (byte) 0x0055;
        bArr[2] = (byte) 0x02;
        bArr[3] = (byte) 0x00D3;
        bArr[4] = (byte) 0x00D5;


        currentRequest = LifevitSDKConstants.PILLREMINDER_REQUEST_GET_ALARMSCHEDULE;

        LogUtils.log(Log.DEBUG, TAG, "Sending: " + HexUtils.getStringToPrint(bArr));

        sendMessage(bArr);
    }

    HashMap<Long, ArrayList<LifevitSDKPillReminderAlarmData>> prepareAlarms(ArrayList<LifevitSDKPillReminderAlarmData> array) {

        Collections.sort(array, new Comparator<LifevitSDKPillReminderAlarmData>() {
            @Override
            public int compare(LifevitSDKPillReminderAlarmData o1, LifevitSDKPillReminderAlarmData o2) {
                return o1.getDate() < o2.getDate() ? 1 : -1;
            }
        });

        HashMap<Long, ArrayList<LifevitSDKPillReminderAlarmData>> alarms = new HashMap<Long, ArrayList<LifevitSDKPillReminderAlarmData>>();


        for (LifevitSDKPillReminderAlarmData d : array) {
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

            try {
                Date todayWithZeroTime = formatter.parse(formatter.format(d.getDate()));
                Long milliseconds = todayWithZeroTime.getTime();

                ArrayList<LifevitSDKPillReminderAlarmData> alarmArrayForDay = alarms.get(milliseconds);

                if (alarmArrayForDay != null) {
                    alarmArrayForDay.add(d);
                } else {
                    ArrayList<LifevitSDKPillReminderAlarmData> a = new ArrayList<LifevitSDKPillReminderAlarmData>();
                    a.add(d);
                    alarms.put(milliseconds, a);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        return alarms;
    }


    public void sendSetAlarmsSchedule(ArrayList<LifevitSDKPillReminderAlarmData> alarms) {
        LogUtils.log(Log.DEBUG, TAG, "setAlarmsSchedule");

        HashMap<Long, ArrayList<LifevitSDKPillReminderAlarmData>> alarmDic = prepareAlarms(alarms);

        int totalDaysWithAlarms = alarmDic.size();

        if (totalDaysWithAlarms > 0) {
            int currentDay = 1;

            //for (Long key : alarmDic.) {
            for (Map.Entry<Long, ArrayList<LifevitSDKPillReminderAlarmData>> entry : alarmDic.entrySet()) {

                ArrayList<LifevitSDKPillReminderAlarmData> a = alarmDic.get(entry.getKey());

                long longKey = (long) entry.getKey();


                Calendar cal = Calendar.getInstance();
                //Date d = new Date(longKey / 1000);
                cal.setTimeInMillis(longKey);

                int yearDate = cal.get(Calendar.YEAR) - 2000;
                int monthDate = cal.get(Calendar.MONTH) + 1;
                int dayDate = cal.get(Calendar.DAY_OF_MONTH);

                int totalAlarmsThisDay = a.size();

                if (totalAlarmsThisDay > 10) {
                    LifevitSDKPillReminderMessageData message = new LifevitSDKPillReminderMessageData();
                    message.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMSSCHEDULE);
                    message.setMessageText("Too many alarms for the same day");

                    if (mLifevitSDKManager.getPillReminderListener() != null) {
                        mLifevitSDKManager.getPillReminderListener().pillReminderOnError(message);

                        sendingThread.taskFinished();
                        currentRequest = -1;
                        return;
                    }
                }

                /*
                byte b5 = (byte) ((totalDaysWithAlarms & 0x00ff) | ((currentDay >> 1) & 0x01));
                byte b6 = (byte) (((currentDay >> 1) & 0x00fe) | ((totalAlarmsThisDay >> 2) & 0x03));
                byte b7 = (byte) (totalAlarmsThisDay & 0x003f);
                */

                byte b5 = (byte) ((totalDaysWithAlarms >> 1) & 0x00ff);
                byte b6 = (byte) (((totalDaysWithAlarms << 7) & 0x80) + ((currentDay >> 2) & 0x007f));
                byte b7 = (byte) (((currentDay << 6) & 0x00c0) | (totalAlarmsThisDay & 0x003f));

                byte b8 = (byte) (((yearDate << 1) & 0x00fe) | ((monthDate >> 3) & 0x01));
                byte b9 = (byte) (((monthDate << 5) & 0x00e0) | (dayDate & 0x001f));

                byte[] bArr = new byte[20];
                for (int i = 0; i < 20; i++) {
                    bArr[i] = (byte) 0xff;
                }

                int alarmForThisDay = 0;
                for (LifevitSDKPillReminderAlarmData alarm : a) {
                    if ((alarm.getColor() < 1) || (alarm.getColor() > 5)) {
                        LifevitSDKPillReminderMessageData message = new LifevitSDKPillReminderMessageData();
                        message.setRequest(LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMSSCHEDULE);
                        message.setMessageText("Wrong color number (available only 1-5)");

                        if (mLifevitSDKManager.getPillReminderListener() != null) {
                            mLifevitSDKManager.getPillReminderListener().pillReminderOnError(message);

                            sendingThread.taskFinished();
                            currentRequest = -1;
                            return;
                        }
                    }

                    cal.setTimeInMillis(alarm.getDate());

                    int hourDate = cal.get(Calendar.HOUR_OF_DAY);
                    int minuteDate = cal.get(Calendar.MINUTE);
                    int color = alarm.getColor();

                    byte b10 = (byte) (((hourDate << 3) & 0x00f8) | ((minuteDate >> 3) & 0x07));
                    byte b11 = (byte) (((minuteDate << 5) & 0x00e0) | (color & 0x001f));


                    switch (alarmForThisDay) {
                        case 0:
                        case 5:

                            //Reiniciamos el array de bytes...
                            bArr = new byte[20];

                            bArr[0] = (byte) 0x00AA;
                            bArr[1] = (byte) 0x0055;
                            bArr[2] = (byte) 0x11;
                            bArr[3] = (byte) 0x00D4;
                            bArr[4] = b5;
                            bArr[5] = b6;
                            bArr[6] = b7;
                            bArr[7] = b8;
                            bArr[8] = b9;
                            bArr[9] = b10;
                            bArr[10] = b11;

                            for (int i = 11; i < 20; i++) {
                                bArr[i] = (byte) 0xff;
                            }
                            break;


                        case 1:
                        case 6:
                            bArr[11] = b10;
                            bArr[12] = b11;
                            break;

                        case 2:
                        case 7:
                            bArr[13] = b10;
                            bArr[14] = b11;
                            break;

                        case 3:
                        case 8:
                            bArr[15] = b10;
                            bArr[16] = b11;
                            break;

                        case 4:
                        case 9:
                            bArr[17] = b10;
                            bArr[18] = b11;
                            break;

                    }

                    if ((alarmForThisDay == 4) || (alarmForThisDay == 9) || (alarmForThisDay == a.size() - 1)) {

                        bArr[19] = getDatasXor(bArr); // xor byte
                        currentRequest = LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMSSCHEDULE;

                        LogUtils.log(Log.DEBUG, TAG, "Sending: " + HexUtils.getStringToPrint(bArr));


                        //DEBE SER: aa5511d4 00804626 c7424142 63426442 a243018e
                        //Y:        aa5511d4 00804626 c74365ff ffffffff ffffff38


                        packetsToSend.add(bArr);
                    }

                    alarmForThisDay = alarmForThisDay + 1;

                }

                currentDay = currentDay + 1;

            }
        }

        currentRequest = LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMSSCHEDULE;
        sendMessage(packetsToSend.get(0));

    }

    public void sendGetSchedulePerformanceHistory() {
        LogUtils.log(Log.DEBUG, TAG, "getSchedulePerformanceHistory");

        byte[] bArr = new byte[5];

        bArr[0] = (byte) 0x00AA;
        bArr[1] = (byte) 0x0055;
        bArr[2] = (byte) 0x02;
        bArr[3] = (byte) 0x00D5;
        bArr[4] = (byte) 0x00D7;


        currentRequest = LifevitSDKConstants.PILLREMINDER_REQUEST_GET_SCHEDULEPERFORMANCEHISTORY;

        LogUtils.log(Log.DEBUG, TAG, "Sending: " + HexUtils.getStringToPrint(bArr));

        sendMessage(bArr);
    }

    public void sendSetAlarmDuration(int duration) {
        LogUtils.log(Log.DEBUG, TAG, "setAlarmDuration");

        byte[] bArr = new byte[6];

        bArr[0] = (byte) 0x00AA;
        bArr[1] = (byte) 0x0055;
        bArr[2] = (byte) 0x03;
        bArr[3] = (byte) 0x00D6;
        bArr[4] = (byte) (duration & 0x00ff);

        bArr[5] = getDatasXor(bArr); // xor byte

        currentRequest = LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMDURATION;

        LogUtils.log(Log.DEBUG, TAG, "Sending: " + HexUtils.getStringToPrint(bArr));

        sendMessage(bArr);
    }

    public void sendSetAlarmConfirmationTime(int confirmationTime) {
        LogUtils.log(Log.DEBUG, TAG, "setAlarmConfirmationTime");

        byte[] bArr = new byte[6];

        bArr[0] = (byte) 0x00AA;
        bArr[1] = (byte) 0x0055;
        bArr[2] = (byte) 0x03;
        bArr[3] = (byte) 0x00D7;
        bArr[4] = (byte) (confirmationTime & 0x00ff);

        bArr[5] = getDatasXor(bArr); // xor byte

        currentRequest = LifevitSDKConstants.PILLREMINDER_REQUEST_SET_ALARMCONFIRMATIONTIME;

        LogUtils.log(Log.DEBUG, TAG, "Sending: " + HexUtils.getStringToPrint(bArr));

        sendMessage(bArr);
    }

    public void sendClearAlarmSchedule() {
        LogUtils.log(Log.DEBUG, TAG, "clearAlarmSchedule");

        byte[] bArr = new byte[5];

        bArr[0] = (byte) 0x00AA;
        bArr[1] = (byte) 0x0055;
        bArr[2] = (byte) 0x02;
        bArr[3] = (byte) 0x00D8;
        bArr[4] = (byte) 0x00DA;


        currentRequest = LifevitSDKConstants.PILLREMINDER_REQUEST_GET_CLEARALARMSCHEDULE;

        LogUtils.log(Log.DEBUG, TAG, "Sending: " + HexUtils.getStringToPrint(bArr));

        sendMessage(bArr);
    }


    static byte getDatasXor(byte[] bArr) {
        int b = 0;
        for (int i = 2; i < bArr.length - 1; i++) {
            b = (b + bArr[i]);
        }

        byte check = (byte) (b % 256);

        return check;
    }


}