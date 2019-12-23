package es.lifevit.sdk;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import es.lifevit.sdk.bracelet.LifevitSDKTensioBraceletMeasurementInterval;
import es.lifevit.sdk.utils.HexUtils;
import es.lifevit.sdk.utils.LogUtils;
import es.lifevit.sdk.utils.Utils;


/**
 * Created by aescanuela on 26/1/16.
 */
public class LifevitSDKBleDeviceTensiobracelet extends LifevitSDKBleDevice {

    private final static String TAG = LifevitSDKBleDeviceTensiobracelet.class.getSimpleName();

    private static final String DEVICE_NAME = "BPW1";


    /**
     * Attributes
     */

    // Sending queue, to send instructions to device
    static TensiobraceletSendQueue sendingThread;


    int historyDataNumberPackages = 0;
    byte[] historyDataCurrentBytes;
    List<LifevitSDKHeartData> historyAllResults = new ArrayList<>();


    /**
     * Service descriptor 4
     */

    // Custom service (primary service)
    private static final String UUID_SERVICE = "69400001-B5A3-F393-E0A9-E50E24DCCA99";

    // Properties: WRITE, WRITE_NO_RESPONS (client can write, and write with response, on this characteristic)
    // Write Type: WRITE REQUEST (will give you a response back telling you the write was successful)
    // Descriptors:
    // 1. Characteristic user description, UUID: 0x2901 (read-only, provides a textual user description for a characteristic value)
    protected static final String UUID_WRITE_CHARACTERISTIC = "69400002-B5A3-F393-E0A9-E50E24DCCA99";

    // Properties: NOTIFY (allows the server to use the Handle Value Notification ATT operation on this characteristic )
    // Descriptors:
    // 1. Client Characteristic Configuration, UUID: 0x2902 (defines how the characteristic may be configured by a specific client)
    protected static final String UUID_NOTIFY_CHARACTERISTIC_READ = "69400003-B5A3-F393-E0A9-E50E24DCCA99";

    protected static final String UUID_READ_DESCRIPTOR = "00002902-B5A3-F393-E0A9-E50E24DCCA99";

    protected static final String UUID_SERVICE_TO_SCAN = "69400001-B5A3-F393-E0A9-E50E24DCCA99";


    /**
     * Actions to send to BLE device
     */

    protected static final int ACTION_SET_DATE = 0;
    protected static final int ACTION_START_MEASUREMENT = 1;
    protected static final int ACTION_GET_BLOOD_PRESSURE_HISTORY_DATA = 2;
    protected static final int ACTION_RETURN = 3;
    protected static final int ACTION_PROGRAM_AUTOMATIC_MEASUREMENTS = 4;
    protected static final int ACTION_DEACTIVATE_AUTOMATIC_MEASUREMENTS = 5;
    private boolean firstPacketReceived = false;


    /**
     * Creator
     */

    protected LifevitSDKBleDeviceTensiobracelet(BluetoothDevice dev, LifevitSDKManager manager) {

        this.mBluetoothDevice = dev;
        this.mLifevitSDKManager = manager;
    }


    @Override
    protected int getType() {
        return LifevitSDKConstants.DEVICE_TENSIOBRACELET;
    }

    /**
     * Receivers
     */

    protected void connectGatt(Context context, boolean firstTime) {

        LogUtils.log(Log.DEBUG, TAG, "[connection] CONNECT: " + mBluetoothDevice.getAddress());

        mBluetoothGatt = mBluetoothDevice.connectGatt(context, false, mGattCallback);
        mContext = context;
        mFirstTime = firstTime;

        sendingThread = new TensiobraceletSendQueue(this);
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

    protected void setDate(long date) {
        sendingThread.addToQueue(ACTION_SET_DATE, date);
    }

    protected void startMeasurement() {
        sendingThread.addToQueue(ACTION_START_MEASUREMENT);
    }

    protected void getBloodPressureHistoryData() {
        sendingThread.addToQueue(ACTION_GET_BLOOD_PRESSURE_HISTORY_DATA);
    }

    protected void returnScreen() {
        sendingThread.addToQueue(ACTION_RETURN);
    }

    protected void setProgramAutomaticMeasurements(LifevitSDKTensioBraceletMeasurementInterval config) {
        sendingThread.addToQueue(ACTION_PROGRAM_AUTOMATIC_MEASUREMENTS, config);
    }

    protected void deactivateAutomaticMeasurements() {
        sendingThread.addToQueue(ACTION_DEACTIVATE_AUTOMATIC_MEASUREMENTS);
    }


    /**
     * Other methods
     */

    protected static boolean isTensioBraceletDevice(String name) {
        return DEVICE_NAME.equalsIgnoreCase(name);
    }

    protected static boolean matchDevice(BluetoothDevice device) {
        return DEVICE_NAME.equalsIgnoreCase(device.getName());
    }


    protected static UUID[] getUUIDs() {
        UUID[] uuidArray = new UUID[0];
        // uuidArray[0] = UUID.fromString(UUID_SERVICE_TO_SCAN);
        return uuidArray;
    }


    protected void sendDeviceStatus() {
        mLifevitSDKManager.deviceOnConnectionChanged(LifevitSDKConstants.DEVICE_TENSIOBRACELET, mDeviceStatus, true);

//        if (mDeviceStatus == LifevitSDKConstants.STATUS_CONNECTED) {
//            sendGetInfo();
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
            LogUtils.log(Log.ERROR, TAG, "No BluetoothGatt!");
            return;
        }

        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(UUID_SERVICE));
        if (RxService == null) {
            LogUtils.log(Log.ERROR, TAG, "Rx service not found!");
            return;
        }

        BluetoothGattCharacteristic txCharacteristic = RxService.getCharacteristic(UUID.fromString(UUID_NOTIFY_CHARACTERISTIC_READ));

        if (txCharacteristic == null) {
            LogUtils.log(Log.ERROR, TAG, "Tx charateristic not found!");
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
            LogUtils.log(Log.ERROR, TAG, "sendMessage: mBluetoothGatt is null");
            return;
        }
        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(UUID_SERVICE));
        if (RxService == null) {
            LogUtils.log(Log.ERROR, TAG, "Rx service not found!");
            return;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(UUID.fromString(UUID_WRITE_CHARACTERISTIC));
        if (RxChar == null) {
            LogUtils.log(Log.ERROR, TAG, "Rx charateristic not found!");
            return;
        }

        dividePacketsAndSendMessage(RxChar, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
    }


    /*********************************************************************************************/
    /***************************  Methods to receive data from device  ***************************/
    /*********************************************************************************************/


    protected void characteristicReadProcessData(BluetoothGattCharacteristic characteristic) {

        LogUtils.log(Log.DEBUG, TAG, "characteristicReadProcessData - RECEIVED from characteristic: " + characteristic.getUuid().toString());

        if (characteristic.getUuid().equals(UUID.fromString(UUID_NOTIFY_CHARACTERISTIC_READ))) {

            byte[] received = characteristic.getValue();

            LogUtils.log(Log.DEBUG, TAG, "characteristicReadProcessData - RECEIVED (byte format): " + HexUtils.getStringToPrint(received));

            //Miramos a ver si tiene 1 o 2 paquetes...
            byte[] rx;
            if (received[0] == 0x5A && received[1] == 0x00 && received[2] == 0x0A) {

                // One history measurements received (packet 1 of 2) --> Wait for second package
                historyDataCurrentBytes = Arrays.copyOf(received, received.length);
                firstPacketReceived = true;
                return;
            } else if (firstPacketReceived) {
                rx = new byte[historyDataCurrentBytes.length + received.length];
                for (int i = 0; i < rx.length; ++i) {
                    rx[i] = i < historyDataCurrentBytes.length ? historyDataCurrentBytes[i] : received[i - historyDataCurrentBytes.length];
                }
                firstPacketReceived = false;
            } else {
                rx = received;
                firstPacketReceived = false;
            }

            if (rx[3] == 0x40 && rx[5] == 0x42) {

                // Received when we start any command

                LogUtils.log(Log.DEBUG, TAG, "Command received.");

                if(mLifevitSDKManager.getTensiobraceletListener()!=null) {
                    mLifevitSDKManager.getTensiobraceletListener().tensiobraceletCommandReceived();
                }
                if (sendingThread.getLastCommandSent() == ACTION_PROGRAM_AUTOMATIC_MEASUREMENTS
                        || sendingThread.getLastCommandSent() == ACTION_DEACTIVATE_AUTOMATIC_MEASUREMENTS
                        || sendingThread.getLastCommandSent() == ACTION_SET_DATE
                        || sendingThread.getLastCommandSent() == ACTION_RETURN) {

                    // Operacion acabada
                    sendingThread.taskFinished();
                }

            } else if (sendingThread.getLastCommandSent() == ACTION_START_MEASUREMENT) {

                // User started a measurement

                if (rx.length == 10 && rx[2] == 0x06) {

                    // Last packet of measurement received -> Finished OK

                    finalResultReceived(rx);

                    // Operacion acabada
                    sendingThread.taskFinished();

                } else if (rx[0] == 0x5A && rx[1] == 0x00 && rx[2] == 0x0A) {

                    int year = rx[3];
                    //Java comienza en 0 el mes (Enero) por eso se resta 1
                    int month = rx[4] - 1;
                    int day = rx[5];
                    int hour = rx[6];
                    int minute = rx[7];
                    int systolic = Utils.bytesToInt(new byte[]{0, 0, rx[8], rx[9]});
                    int diastolic = rx[10];
                    int pulse = rx[11];
                    int fgCheckCode = rx[12];

                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, 2000 + year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, day);
                    cal.set(Calendar.HOUR_OF_DAY, hour);
                    cal.set(Calendar.MINUTE, minute);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);

                    LifevitSDKHeartData data = new LifevitSDKHeartData();
                    data.setDate(cal.getTimeInMillis());
                    data.setDiastolic(diastolic);
                    data.setSystolic(systolic);
                    data.setPulse(pulse);
                    data.setIHB(fgCheckCode > 0);
                    if (mLifevitSDKManager.getTensiobraceletListener() != null)
                        mLifevitSDKManager.getTensiobraceletListener().tensiobraceletResult(data);
                    sendingThread.taskFinished();

                } else if (rx[3] == 0x50 || rx[3] == 0x51 || rx[3] == 0x52) {

                    // Last packet of measurement received -> ERROR
                    if (mLifevitSDKManager.getTensiobraceletListener() != null) {

                        if (rx[3] == 0x50 && rx[4] == 1) {
                            mLifevitSDKManager.getTensiobraceletListener().tensiobraceletError(LifevitSDKConstants.TENSIOBRACELET_ERROR_HAND_HIGH);
                        } else if (rx[3] == 0x50 && rx[4] == 3) {
                            mLifevitSDKManager.getTensiobraceletListener().tensiobraceletError(LifevitSDKConstants.TENSIOBRACELET_ERROR_HAND_LOW);
                        } else if (rx[3] == 0x50 && rx[4] == 4) {
                            mLifevitSDKManager.getTensiobraceletListener().tensiobraceletError(LifevitSDKConstants.TENSIOBRACELET_ERROR_GENERAL);
                        } else if (rx[3] == 0x50 && rx[4] == 5) {
                            mLifevitSDKManager.getTensiobraceletListener().tensiobraceletError(LifevitSDKConstants.TENSIOBRACELET_ERROR_LOW_POWER);
                        } else if (rx[3] == 0x52 && rx[4] == 1) {
                            mLifevitSDKManager.getTensiobraceletListener().tensiobraceletError(LifevitSDKConstants.TENSIOBRACELET_ERROR_INCORRECT_POSITION);
                        } else if (rx[3] == 0x52 && rx[4] == 2) {
                            mLifevitSDKManager.getTensiobraceletListener().tensiobraceletError(LifevitSDKConstants.TENSIOBRACELET_ERROR_BODY_MOVED);
                        } else if (rx[3] == 0x52 && rx[4] == 3) {
                            mLifevitSDKManager.getTensiobraceletListener().tensiobraceletError(LifevitSDKConstants.TENSIOBRACELET_ERROR_TIGHT_WEARING);
                        } else if (rx[3] == 0x52 && rx[4] == 4) {
                            mLifevitSDKManager.getTensiobraceletListener().tensiobraceletError(LifevitSDKConstants.TENSIOBRACELET_ERROR_LOOSE_WEARING);
                        } else if (rx[3] == 0x52 && rx[4] == 5) {
                            mLifevitSDKManager.getTensiobraceletListener().tensiobraceletError(LifevitSDKConstants.TENSIOBRACELET_ERROR_AIR_LEAKAGE);
                        } else {
                            mLifevitSDKManager.getTensiobraceletListener().tensiobraceletError(LifevitSDKConstants.TENSIOBRACELET_ERROR_GENERAL);
                        }
                    }
                    // Operacion acabada
                    sendingThread.taskFinished();

                } else {

                    // Intermediate packet of measurement received -> Pulse

                    int pulse = Utils.bytesToInt(new byte[]{0, 0, rx[3], rx[4]});

                    LogUtils.log(Log.DEBUG, TAG, "Blood pressure measurement: " + pulse);

                    if (mLifevitSDKManager.getTensiobraceletListener() != null)
                        mLifevitSDKManager.getTensiobraceletListener().tensiobraceletOnMeasurement(pulse);
                }


            } else if (rx[3] == 0x60) {

                // First packet of History Data --> Contains how many measurements will arrive

                historyDataNumberPackages = (int) rx[4];

            } else if (historyDataNumberPackages > 0) {

                if (rx[0] == 0x5A && rx[1] == 0x00 && rx[2] == 0x0A) {

                    // One history measurements received (packet 1 of 2) --> Wait for second package

                    historyDataCurrentBytes = Arrays.copyOf(rx, rx.length);

                } else {

                    // Combine both arrays
                    byte[] combined = new byte[historyDataCurrentBytes.length + rx.length];
                    for (int i = 0; i < combined.length; ++i) {
                        combined[i] = i < historyDataCurrentBytes.length ? historyDataCurrentBytes[i] : rx[i - historyDataCurrentBytes.length];
                    }

                    int year = combined[3];
                    int month = combined[4];
                    int day = combined[5];
                    int hour = combined[6];
                    int minute = combined[7];
                    int systolic = Utils.bytesToInt(new byte[]{0, 0, combined[8], combined[9]});
                    int diastolic = combined[10];
                    int pulse = combined[11];
                    int fgCheckCode = combined[12];

                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, day);
                    cal.set(Calendar.HOUR, hour);
                    cal.set(Calendar.MINUTE, minute);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);

                    LifevitSDKHeartData data = new LifevitSDKHeartData();
                    data.setDate(cal.getTimeInMillis());
                    data.setDiastolic(diastolic);
                    data.setSystolic(systolic);
                    data.setPulse(pulse);
                    data.setIHB(fgCheckCode > 0);

                    historyAllResults.add(data);

                    historyDataNumberPackages--;

                    if (historyDataNumberPackages == 0) {

                        if (mLifevitSDKManager.getTensiobraceletListener() != null)
                            mLifevitSDKManager.getTensiobraceletListener().tensiobraceletHistoricResults(historyAllResults);

                        historyAllResults = new ArrayList<>();

                        // Operacion acabada
                        sendingThread.taskFinished();
                    }
                }
            } else if (rx.length == 10 && rx[2] == 0x06) {
                finalResultReceived(rx);
            } else if (rx[0] == 0x5A && rx[1] == 0x00 && rx[2] == 0x0A) {
                int year = rx[3];
                //Java comienza en 0 el mes (Enero) por eso se resta 1
                int month = rx[4] - 1;
                int day = rx[5];
                int hour = rx[6];
                int minute = rx[7];
                int systolic = Utils.bytesToInt(new byte[]{0, 0, rx[8], rx[9]});
                int diastolic = rx[10];
                int pulse = rx[11];
                int fgCheckCode = rx[12];

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, 2000 + year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, day);
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, minute);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                LifevitSDKHeartData data = new LifevitSDKHeartData();
                data.setDate(cal.getTimeInMillis());
                data.setDiastolic(diastolic);
                data.setSystolic(systolic);
                data.setPulse(pulse);
                data.setIHB(fgCheckCode > 0);
                if (mLifevitSDKManager.getTensiobraceletListener() != null)
                    mLifevitSDKManager.getTensiobraceletListener().tensiobraceletResult(data);
            } else if (rx[0] == 0x5A && rx[1] == 0x00 && rx[2] == 0x02) {

                // Intermediate packet of measurement received -> Pulse

                int pulse = Utils.bytesToInt(new byte[]{0, 0, rx[3], rx[4]});
                Log.d(TAG, "Blood pressure measurement: " + pulse);

                if (mLifevitSDKManager.getTensiobraceletListener() != null)
                    mLifevitSDKManager.getTensiobraceletListener().tensiobraceletOnMeasurement(pulse);

            }
        }

        sendingThread.setLastCommandSent(-1);
    }


    private void finalResultReceived(byte[] rx) {

        // 5A:00:06:FF:  00:7A  :4A:  3E:  00:  F7:

        int sys = Utils.bytesToInt(new byte[]{0, 0, rx[4], rx[5]});
        int dia = (int) rx[6]; //  Utils.bytesToInt(new byte[]{0, 0, 0, rx[6]});
        int pul = Utils.bytesToInt(new byte[]{0, 0, 0, rx[7]});
        int fg = Utils.bytesToInt(new byte[]{0, 0, 0, rx[8]});

        LifevitSDKHeartData heartData = new LifevitSDKHeartData();
        heartData.setSystolic(sys);
        heartData.setDiastolic(dia);
        heartData.setPulse(pul);
        heartData.setDate(Calendar.getInstance().getTimeInMillis());
        heartData.setIHB(fg > 0);

        if (mLifevitSDKManager.getTensiobraceletListener() != null)
            mLifevitSDKManager.getTensiobraceletListener().tensiobraceletResult(heartData);

    }


//    public static String HEART_RATE_MEASUREMENT = "0000C004-0000-1000-8000-00805f9b34fb";
//    public static final UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(HEART_RATE_MEASUREMENT);
//    public static final String EXTRA_DATA = "net.totome.bluetooth.le.EXTRA_DATA";
//
//
//    private void broadcastUpdate(String action, BluetoothGattCharacteristic characteristic) {
//        Intent intent = new Intent(action);
//        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
//            int format;
//            if ((characteristic.getProperties() & 1) != 0) {
//                format = 18;
//                Log.d(TAG, "Heart rate format UINT16.");
//            } else {
//                format = 17;
//                Log.d(TAG, "Heart rate format UINT8.");
//            }
//            int heartRate = characteristic.getIntValue(format, 1).intValue();
//            System.out.println("Received heart rate: %d" + heartRate);
//            Log.d(TAG, String.format("Received heart rate: %d", new Object[]{Integer.valueOf(heartRate)}));
//            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
//        } else {
//            byte[] data = characteristic.getValue();
//            if (data != null && data.length > 0) {
//                StringBuilder stringBuilder = new StringBuilder(data.length);
//                int length = data.length;
//                for (int i = 0; i < length; i++) {
//                    stringBuilder.append(String.format("%02X", new Object[]{Byte.valueOf(data[i])}));
//                }
//                intent.putExtra(EXTRA_DATA, stringBuilder.toString());
//            }
//        }
////        sendBroadcast(intent);
//    }


    /*********************************************************************************************/
    /**************************  Methods to send instruction to device  **************************/
    /*********************************************************************************************/


    protected byte calculateXOR(byte[] data, int dataLength) {
        byte xor = 0x0;
        for (int i = 0; i < dataLength; i++) {
            xor = (byte) (xor ^ data[i]);
        }
        return xor;
    }


    protected void sendGetInfo() {

        byte[] data = new byte[6];
        data[0] = (byte) 0x5A;
        data[1] = (byte) 0x00;
        data[2] = (byte) 0x02;
        data[3] = (byte) 0x04;
        data[4] = (byte) 0x00;
        data[5] = (byte) 0x06;

        LogUtils.log(Log.DEBUG, TAG, "SEND: " + HexUtils.getStringToPrint(data));

        sendMessage(data);
    }


    protected void sendSetDate(Long date) {

        byte[] data = new byte[11];
        data[0] = (byte) 0x5A;
        data[1] = (byte) 0x00;
        data[2] = (byte) 0x07;
        data[3] = (byte) 0x20;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);

        data[4] = (byte) (cal.get(Calendar.YEAR) - 2000);
        data[5] = (byte) (cal.get(Calendar.MONTH) + 1);
        data[6] = (byte) cal.get(Calendar.DAY_OF_MONTH);
        data[7] = (byte) cal.get(Calendar.HOUR_OF_DAY);
        data[8] = (byte) cal.get(Calendar.MINUTE);
        data[9] = (byte) cal.get(Calendar.SECOND);

        data[10] = calculateXOR(data, 10);

        LogUtils.log(Log.DEBUG, TAG, "SEND: " + HexUtils.getStringToPrint(data));

        sendMessage(data);
    }


    protected void sendGetTime() {

        byte[] data = new byte[6];
        data[0] = (byte) 0x5A;
        data[1] = (byte) 0x00;
        data[2] = (byte) 0x02;
        data[3] = (byte) 0x03;
        data[4] = (byte) 0x00;
        data[5] = (byte) 0x01;

        LogUtils.log(Log.DEBUG, TAG, "SEND: " + HexUtils.getStringToPrint(data));

        sendMessage(data);
    }


    protected void sendStartMeasurement() {

        byte[] data = new byte[6];
        data[0] = (byte) 0x5A;
        data[1] = (byte) 0x00;
        data[2] = (byte) 0x02;
        data[3] = (byte) 0x00;
        data[4] = (byte) 0x00;
        data[5] = (byte) 0x02;

        LogUtils.log(Log.DEBUG, TAG, "SEND: " + HexUtils.getStringToPrint(data));

        sendMessage(data);
    }

    protected void sendGetBloodPressureHistoryData() {

        byte[] data = new byte[6];
        data[0] = (byte) 0x5A;
        data[1] = (byte) 0x00;
        data[2] = (byte) 0x02;
        data[3] = (byte) 0x01;
        data[4] = (byte) 0x00;
        data[5] = (byte) 0x03;

        LogUtils.log(Log.DEBUG, TAG, "SEND: " + HexUtils.getStringToPrint(data));

        sendMessage(data);
    }


    protected void sendReturn() {

        byte[] data = new byte[6];
        data[0] = (byte) 0x5A;
        data[1] = (byte) 0x00;
        data[2] = (byte) 0x02;
        data[3] = (byte) 0x06;
        data[4] = (byte) 0x00;
        data[5] = (byte) 0x04;

        LogUtils.log(Log.DEBUG, TAG, "SEND: " + HexUtils.getStringToPrint(data));

        sendMessage(data);
    }


    protected void sendProgramAutomaticMeasurements(LifevitSDKTensioBraceletMeasurementInterval config) {

        byte[] data = new byte[11];
        data[0] = (byte) 0x5A;
        data[1] = (byte) 0x00;
        data[2] = (byte) 0x07;
        data[3] = (byte) 0x21;
        data[4] = (byte) config.getStartHour();
        data[5] = (byte) (config.getStartMinute() == LifevitSDKTensioBraceletMeasurementInterval.StartingEndingMinutes.O_CLOCK ? 0x00 : 0x1E);
        data[6] = (byte) config.getFinishHour();
        data[7] = (byte) (config.getFinishMinute() == LifevitSDKTensioBraceletMeasurementInterval.StartingEndingMinutes.O_CLOCK ? 0x00 : 0x1E);

        byte currentInterval = (byte) ((byte) config.getCurrentIntervalNumber() << 4 & 0b11110000);
        byte totalIntervals = (byte) ((byte) config.getTotalIntervals() & 0b00001111);
        data[8] = (byte) (currentInterval | totalIntervals);

        data[9] = (byte) config.getMeasurementIntervalValue();

        data[10] = calculateXOR(data, 10);

        LogUtils.log(Log.DEBUG, TAG, "SEND: " + HexUtils.getStringToPrint(data));

        sendMessage(data);
    }


    protected void sendDeactivateAutomaticMeasurements() {

        byte[] data = new byte[11];
        data[0] = (byte) 0x5A;
        data[1] = (byte) 0x00;
        data[2] = (byte) 0x07;
        data[3] = (byte) 0x21;

        data[4] = (byte) 0x00;
        data[5] = (byte) 0x00;
        data[6] = (byte) 0x00;
        data[7] = (byte) 0x00;
        data[8] = (byte) 0x11;
        data[9] = (byte) 0x00;

        data[10] = calculateXOR(data, 10);

        LogUtils.log(Log.DEBUG, TAG, "SEND: " + HexUtils.getStringToPrint(data));

        sendMessage(data);
    }


}