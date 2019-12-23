package es.lifevit.sdk;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.UUID;

import es.lifevit.sdk.utils.HexUtils;
import es.lifevit.sdk.utils.LogUtils;


/**
 * Created by aescanuela on 26/1/16.
 */
public class LifevitSDKBleDeviceThermometer extends LifevitSDKBleDevice {

    private final static String TAG = LifevitSDKBleDeviceThermometer.class.getSimpleName();


    /**
     * Service Descriptors
     */

    // Custom Service
    private static String THERMOMETER_SERVICE_UUID = "0000fff0-0000-1000-8000-00805f9b34fb";

    // Notify characteristic
    private static String THERMOMETER_NOTIFY_UUID = "0000fff1-0000-1000-8000-00805f9b34fb";

    // Descriptor of Notify Characteristic
    private static String THERMOMETER_CLIENT_CHARACTERISTIC_CONFIG_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    // Write request characteristic
    private static String THERMOMETER_WRITE_UUID = "0000fff2-0000-1000-8000-00805f9b34fb";


    /**
     * Other constants
     */

    private static final byte COMMAND_HEADER_BYTE_1 = (byte) 0xFE;
    private static final byte COMMAND_HEADER_BYTE_2 = (byte) 0xFD;
    private static final byte COMMAND_TAIL_BYTE_1 = (byte) 0X0D;
    private static final byte COMMAND_TAIL_BYTE_2 = (byte) 0X0A;
    private static final byte CONNECTING = (byte) 0xAA;


    /**
     * Creator
     */

    public LifevitSDKBleDeviceThermometer(BluetoothDevice dev, LifevitSDKManager manager) {
        this.mBluetoothDevice = dev;
        this.mLifevitSDKManager = manager;
    }

    @Override
    protected int getType() {
        return LifevitSDKConstants.DEVICE_THERMOMETER;
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

    protected static boolean isThermometerDevice(String name) {
        return name != null && (("Bluetooth BP".equals(name))
                || ("Bluetooth MT".equals(name))
                || ("Urion MT".equals(name)));
    }


    protected static UUID[] getUUIDs() {
        UUID[] uuidArray = new UUID[1];
        uuidArray[0] = UUID.fromString(THERMOMETER_SERVICE_UUID);
        return uuidArray;
    }


    protected void sendDeviceStatus() {

        mLifevitSDKManager.deviceOnConnectionChanged(LifevitSDKConstants.DEVICE_THERMOMETER, mDeviceStatus, true);
    }


    /**
     * Enable Device Notification
     *
     * @return
     */
    protected void enableDeviceNotification() {

        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(THERMOMETER_SERVICE_UUID));
        if (RxService == null) {
            LogUtils.log(Log.ERROR, TAG, "Rx service not found!");
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(UUID.fromString(THERMOMETER_NOTIFY_UUID));
        if (TxChar == null) {
            LogUtils.log(Log.ERROR, TAG, "Tx charateristic not found!");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar, true);

        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(UUID.fromString(THERMOMETER_CLIENT_CHARACTERISTIC_CONFIG_UUID));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

        write(descriptor);

        getTemperature();
    }

    protected void sendMessage(byte[] data) {

        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(THERMOMETER_SERVICE_UUID));
        if (RxService == null) {
            LogUtils.log(Log.ERROR, TAG, "Rx service not found!");
            return;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(UUID.fromString(THERMOMETER_WRITE_UUID));
        if (RxChar == null) {
            LogUtils.log(Log.ERROR, TAG, "Rx charateristic not found!");
            return;
        }

        dividePacketsAndSendMessage(RxChar, data, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }


    protected void characteristicReadProcessData(BluetoothGattCharacteristic characteristic) {

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if (UUID.fromString(THERMOMETER_NOTIFY_UUID).equals(characteristic.getUuid())) {
            final byte[] bytes = characteristic.getValue();
            if (bytes != null && bytes.length != 1 && bytes.length >= 8) {
                doWithData(bytes);
            }
        }
    }


    protected void getTemperature() {
        byte[] values = {COMMAND_HEADER_BYTE_1, COMMAND_HEADER_BYTE_2,
                CONNECTING, (byte) 0xA0,
                COMMAND_TAIL_BYTE_1, COMMAND_TAIL_BYTE_2};

        sendMessage(values);
    }


    private static boolean isCorrectData(byte[] a) {
        return a.length == 8 &&
                a[0] == COMMAND_HEADER_BYTE_1 &&
                (a[1] == COMMAND_HEADER_BYTE_2 || a[1] == (byte) 251 || a[1] == (byte) 26 || a[1] == (byte) 21) &&
                a[6] == COMMAND_TAIL_BYTE_1 &&
                a[7] == COMMAND_TAIL_BYTE_2;
    }


    protected void doWithData(byte[] buffer) {

        LogUtils.log(Log.DEBUG, TAG, "[doWithData]: " + HexUtils.getStringToPrint(buffer));

        if (!isCorrectData(buffer)) {
            LogUtils.log(Log.ERROR, TAG, "NOT CORRECT DATA!!");
            if (mLifevitSDKManager.getThermometerListener() != null) {
                mLifevitSDKManager.getThermometerListener().onThermometerDeviceError(LifevitSDKConstants.THERMOMETER_ERROR_HARDWARE);
            }
            return;
        }

        byte command = buffer[3];

        if (command == CONNECTING) {

            // ------ CONNECTED ------

            LogUtils.log(Log.DEBUG, TAG, "Forehead thermometer data transmission is activated");

        } else if (command == LifevitSDKConstants.THERMOMETER_MODE_ENVIRONMENT || command == LifevitSDKConstants.THERMOMETER_MODE_BODY) {

            // ------ MEASUREMENT ------

            int temperatureUnit = LifevitSDKConstants.TEMPERATURE_UNIT_CELSIUS;
            double temperatureValue;

            if (buffer[2] == (byte) 0x15) {
                temperatureUnit = LifevitSDKConstants.TEMPERATURE_UNIT_FAHRENHEIT;
            }

            int highBytes = (buffer[4] & 0xff);
            int lowBytes = (buffer[5] & 0xff);

            temperatureValue = ((double) ((highBytes * 256) + lowBytes)) / 10;

            if (mLifevitSDKManager.getThermometerListener() != null) {
                mLifevitSDKManager.getThermometerListener().onThermometerDeviceResult(command, temperatureUnit, temperatureValue);
            }

        } else {

            // ------ ERRORS ------

            int errorCode = 0;

            if (command == (byte) 0x81 && buffer[5] == (byte) 1) {
                errorCode = LifevitSDKConstants.THERMOMETER_ERROR_BODY_TEMPERATURE_HIGH;
            } else if (command == (byte) 0x82 && buffer[5] == (byte) 2) {
                errorCode = LifevitSDKConstants.THERMOMETER_ERROR_BODY_TEMPERATURE_LOW;
            } else if (command == (byte) 0x83 && buffer[5] == (byte) 3) {
                errorCode = LifevitSDKConstants.THERMOMETER_ERROR_AMBIENT_TEMPERATURE_HIGH;
            } else if (command == (byte) 0x84 && buffer[5] == (byte) 4) {
                errorCode = LifevitSDKConstants.THERMOMETER_ERROR_AMBIENT_TEMPERATURE_LOW;
            } else if (command == (byte) 0x85 && buffer[5] == (byte) 5) {
                errorCode = LifevitSDKConstants.THERMOMETER_ERROR_HARDWARE;
            } else if (command == (byte) 0x86 && buffer[5] == (byte) 6) {
                errorCode = LifevitSDKConstants.THERMOMETER_ERROR_HARDWARE;
            } else if (command == (byte) 0x87 && buffer[5] == (byte) 7) {
                errorCode = LifevitSDKConstants.THERMOMETER_ERROR_AMBIENT_TEMPERATURE_HIGH;
            } else if (command == (byte) 0x88 && buffer[5] == (byte) 8) {
                errorCode = LifevitSDKConstants.THERMOMETER_ERROR_AMBIENT_TEMPERATURE_LOW;
            }

            if (errorCode != 0) {
                if (mLifevitSDKManager.getThermometerListener() != null) {
                    mLifevitSDKManager.getThermometerListener().onThermometerDeviceError(errorCode);
                }
            }
        }
    }

}