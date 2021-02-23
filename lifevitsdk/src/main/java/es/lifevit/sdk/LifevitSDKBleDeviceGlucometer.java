package es.lifevit.sdk;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;
import java.util.UUID;

import es.lifevit.sdk.utils.HexUtils;
import es.lifevit.sdk.utils.LogUtils;
import es.lifevit.sdk.utils.TemperatureUtils;
import es.lifevit.sdk.utils.Utils;


/**
 * Created by aescanuela on 26/1/16.
 */
public class LifevitSDKBleDeviceGlucometer extends LifevitSDKBleDevice {

    private final static String TAG = LifevitSDKBleDeviceGlucometer.class.getSimpleName();

    public static class Category {

        public static final byte INFO = (byte) 0x00;
        public static final byte START_PACKET = (byte) 0x01;
        public static final byte PROCEDURE = (byte) 0x02;
        public static final byte RESULT = (byte) 0x03;
        public static final byte END_PACKET = (byte) 0x04;
        public static final byte CONFIRM = (byte) 0x05;
        public static final byte END = (byte) 0x06;
    }

    public static class Client {

        public static final byte APPLE = (byte) 0x00;
        public static final byte BIOLAND = (byte) 0x01;
        public static final byte HAIER = (byte) 0x02;
        public static final byte UNKNOWN = (byte) 0x03;
        public static final byte XIAOMI = (byte) 0x04;
        public static final byte GALLERY = (byte) 0x05;
        public static final byte KANWEI = (byte) 0x06;
    }

    /**
     * Service Descriptors
     */

    // Custom Service
    private static String GLUCOMETER_SERVICE_UUID = "00001000-0000-1000-8000-00805f9b34fb";

    // Notify characteristic
    private static String GLUCOMETER_NOTIFY_UUID = "00001002-0000-1000-8000-00805f9b34fb";

    // Descriptor of Notify Characteristic
    private static String GLUCOMETER_CLIENT_CHARACTERISTIC_CONFIG_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    // Write request characteristic
    private static String GLUCOMETER_WRITE_UUID = "00001001-0000-1000-8000-00805f9b34fb";




    /**
     * Creator
     */

    public LifevitSDKBleDeviceGlucometer(BluetoothDevice dev, LifevitSDKManager manager) {
        this.mBluetoothDevice = dev;
        this.mLifevitSDKManager = manager;
    }

    @Override
    protected int getType() {
        return LifevitSDKConstants.DEVICE_GLUCOMETER;
    }


    protected static boolean matchDevice(BluetoothDevice device) {
        return isGlucometerDevice(device.getName());
    }

    /**
     * Receivers
     */

    public void connectGatt(Context context, boolean firstTime) {

        LogUtils.log(Log.DEBUG, TAG, "[connection] CONNECT: " + mBluetoothDevice.getAddress());

        mBluetoothGatt = mBluetoothDevice.connectGatt(context, false, this.mGattCallback);
        mContext = context;
        mFirstTime = firstTime;
    }

    public void startReceiver(String action, Intent intent) {
    }


    /**
     * Other methods
     */

    protected static boolean isGlucometerDevice(String name) {
        return name != null && ("Bioland-BGM".equals(name));
    }


    protected static UUID[] getUUIDs() {
        UUID[] uuidArray = new UUID[1];
        uuidArray[0] = UUID.fromString(GLUCOMETER_SERVICE_UUID);
        return uuidArray;
    }


    protected void sendDeviceStatus() {

        mLifevitSDKManager.deviceOnConnectionChanged(LifevitSDKConstants.DEVICE_GLUCOMETER, mDeviceStatus, true);
    }


    /**
     * Enable Device Notification
     *
     * @return
     */
    protected void enableDeviceNotification() {

        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(GLUCOMETER_SERVICE_UUID));
        if (RxService == null) {
            LogUtils.log(Log.ERROR, TAG, "Rx service not found!");
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(UUID.fromString(GLUCOMETER_NOTIFY_UUID));
        if (TxChar == null) {
            LogUtils.log(Log.ERROR, TAG, "Tx charateristic not found!");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar, true);

        for (BluetoothGattDescriptor descriptor: TxChar.getDescriptors()){
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }

        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(UUID.fromString(GLUCOMETER_CLIENT_CHARACTERISTIC_CONFIG_UUID));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

        write(descriptor);
    }

    protected void sendMessage(byte[] data) {

        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(GLUCOMETER_SERVICE_UUID));
        if (RxService == null) {
            LogUtils.log(Log.ERROR, TAG, "Rx service not found!");
            return;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(UUID.fromString(GLUCOMETER_WRITE_UUID));
        if (RxChar == null) {
            LogUtils.log(Log.ERROR, TAG, "Tx charateristic not found!");
            return;
        }

        dividePacketsAndSendMessage(RxChar, data, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }


    protected void characteristicReadProcessData(BluetoothGattCharacteristic characteristic) {

        LogUtils.log(Log.DEBUG, TAG, "characteristicReadProcessData: "  +characteristic.getUuid());
        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if (UUID.fromString(GLUCOMETER_NOTIFY_UUID).equals(characteristic.getUuid())) {
            final byte[] bytes = characteristic.getValue();
            //if (bytes != null && bytes.length != 1 && bytes.length >= 8) {
            doWithData(bytes);
            //}
        }
    }


    protected void getResults() {
        byte byte0 = 0x5a;
        byte byte1 = 0x0b;
        byte byte2 = Category.CONFIRM;

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR) - 2000;
        byte byte3 = (byte) year;
        int month = cal.get(Calendar.MONTH) + 1;
        byte byte4 = (byte) month;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        byte byte5 = (byte) day;
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        byte byte6 = (byte) hour;
        int minute = cal.get(Calendar.MINUTE);
        byte byte7 = (byte) minute;
        byte[] crc = calculateCRC(new byte[]{byte0, byte1, byte2, byte3, byte4, byte5, byte6, byte7});
        byte[] values = {byte0, byte1, byte2,
                byte3, byte4,
                byte5, byte6,byte7, crc[3], crc[2], crc[1]};

        sendMessage(values);
    }

    private byte[] calculateCRC(byte[] bytes) {
        int result = 0;
        for (int i = 0; i < bytes.length; i++) {
            LogUtils.log(Log.DEBUG, TAG, "Result CRC --> " + result + " NEW BYTE " + (int) bytes[i]);
            result += (int) bytes[i];
        }
        LogUtils.log(Log.DEBUG, TAG, "Result CRC --> " + result);
        byte[] values = Utils.intToByteArray(result);
        return values;
    }


    private static boolean isCorrectData(byte[] a) {
       /* if (a.length == 8 &&
                a[0] == COMMAND_BYTE0 &&
                a[1] == COMMAND_BYTE1 && a[2] == COMMAND_BYTE2) {
            return true;
        }*/
        return true;
    }


    protected void doWithData(byte[] buffer) {
        try {
            LogUtils.log(Log.DEBUG, TAG, "[doWithData]: " + HexUtils.getStringToPrint(buffer));

            if (!isCorrectData(buffer)) {
                LogUtils.log(Log.ERROR, TAG, "NOT CORRECT DATA!!");
                if (mLifevitSDKManager.getGlucometerListener() != null) {
                    mLifevitSDKManager.getGlucometerListener().onGlucometerDeviceError(LifevitSDKConstants.THERMOMETER_ERROR_HARDWARE);
                }
                return;
            }

            byte command = buffer[2];

            if (command == Category.START_PACKET) {
                LogUtils.log(Log.DEBUG, TAG, "Start packet" );

            }
            else if (command == Category.PROCEDURE) {
                LogUtils.log(Log.DEBUG, TAG, "Procedure packet" );

            }
            else if (command == Category.RESULT) {
                LogUtils.log(Log.DEBUG, TAG, "Result packet" );

            } else if (command == Category.END_PACKET) {
                LogUtils.log(Log.DEBUG, TAG, "End packet" );

            }else if (command == Category.INFO) {
                LogUtils.log(Log.DEBUG, TAG, "Info packet" );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}