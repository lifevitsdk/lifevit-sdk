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
import es.lifevit.sdk.utils.TemperatureUtils;


/**
 * Created by aescanuela on 26/1/16.
 */
public class LifevitSDKBleDeviceThermometerV2 extends LifevitSDKBleDevice {

    private final static String TAG = LifevitSDKBleDeviceThermometerV2.class.getSimpleName();

    public static final byte THERMOMETER_CURRENT_RESULT = (byte) 0xff;
    public static final byte THERMOMETER_LAST_RESULT = (byte) 0xaa;
    public static final byte THERMOMETER_ERROR_CODE =(byte) 0xee;
    public static final byte THERMOMETER_SUCCESS = (byte) 0xdd;

    public static final int THERMOMETER_MODE_EAR_CELSIUS = 0;
    public static final int THERMOMETER_MODE_EAR_FARENHEIT = 1;
    public static final int THERMOMETER_MODE_FOREHEAD_CELSIUS = 2;
    public static final int THERMOMETER_MODE_FOREHEAD_FARENHEIT = 3;

    /**
     * Service Descriptors
     */

    // Custom Service
    private static String THERMOMETER_SERVICE_UUID = "0000fff0-0000-1000-8000-00805f9b34fb";

    // Notify characteristic
    private static String THERMOMETER_NOTIFY_UUID = "0000fff4-0000-1000-8000-00805f9b34fb";

    // Descriptor of Notify Characteristic
    private static String THERMOMETER_CLIENT_CHARACTERISTIC_CONFIG_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    // Write request characteristic
    private static String THERMOMETER_WRITE_UUID = "0000fff3-0000-1000-8000-00805f9b34fb";


    /**
     * Other constants
     */

    private static final byte COMMAND_BYTE0 = (byte) 0x02;
    private static final byte COMMAND_BYTE1 = (byte) 0x20;
    private static final byte COMMAND_BYTE2 = (byte) 0xDD;



    public static final int THERMOMETERV2_ERROR_CODE_LOW_TEMP = 1;
    public static final int THERMOMETERV2_ERROR_CODE_HIGH_TEMP = 2;
    public static final int THERMOMETERV2_ERROR_CODE_AMBIENT_LOW = 3;
    public static final int THERMOMETERV2_ERROR_CODE_AMBIENT_HIGH = 4;
    public static final int THERMOMETERV2_ERROR_CODE_LOW_VOLTAGE = 5;
    public static final int THERMOMETERV2_ERROR_CODE_ERR = 6;


    /**
     * Creator
     */

    public LifevitSDKBleDeviceThermometerV2(BluetoothDevice dev, LifevitSDKManager manager) {
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
        return name != null && ("Belter_TP".equals(name) || "e-Thermometer".equals(name));
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
    }

    protected void sendMessage(byte[] data) {

        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(THERMOMETER_SERVICE_UUID));
        if (RxService == null) {
            LogUtils.log(Log.ERROR, TAG, "Rx service not found!");
            return;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(UUID.fromString(THERMOMETER_WRITE_UUID));
        if (RxChar == null) {
            LogUtils.log(Log.ERROR, TAG, "Tx charateristic not found!");
            return;
        }

        dividePacketsAndSendMessage(RxChar, data, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }


    protected void characteristicReadProcessData(BluetoothGattCharacteristic characteristic) {

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if (UUID.fromString(THERMOMETER_NOTIFY_UUID).equals(characteristic.getUuid())) {
            final byte[] bytes = characteristic.getValue();
            //if (bytes != null && bytes.length != 1 && bytes.length >= 8) {
            doWithData(bytes);
            //}
        }
    }


    protected void getTemperature() {
        sendCommand(LifevitSDKConstants.THERMOMETERV2_COMMAND_LAST_MEASURE);
    }


    protected void sendCommand(int command) {
        byte byte0 = COMMAND_BYTE0;
        byte byte1 = COMMAND_BYTE1;
        byte byte2 = COMMAND_BYTE2;
        byte byte3 = (byte) 0x02;
        byte byte4 = (byte) 0xFD;
        byte byte5 = (byte) command;
        byte[] values = {byte0, byte1, byte2,
                byte3, byte4,
                byte5, calculateCRC(new byte[]{byte1, byte2, byte3, byte4, byte5})};

        sendMessage(values);
    }

    private byte calculateCRC(byte[] bytes) {
        int result = 0;
        for (int i = 0; i < bytes.length; i++) {
            System.out.println("XOR Byte0: " + Integer.toBinaryString((result & 0x00ff)));
            System.out.println("XOR Byte1: " + Integer.toBinaryString((bytes[i] & 0x00ff)));
            result = ((bytes[i] & 0x00ff) ^ (result & 0x00ff));
            System.out.println("XOR Result: " + Integer.toBinaryString((result & 0x00ff)));
        }

        return (byte) (result & 0x00ff);
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
            if (buffer.length == 10) {
                byte[] oldBuffer = buffer;
                buffer = new byte[]{(byte) 0x02, (byte) 0x20, (byte) 0xDD, (byte) 0x0A, oldBuffer[0], oldBuffer[1], oldBuffer[2],
                        oldBuffer[3], oldBuffer[4], oldBuffer[5], oldBuffer[6], oldBuffer[7], oldBuffer[8], oldBuffer[9]};
            }
            LogUtils.log(Log.DEBUG, TAG, "[doWithData]: " + HexUtils.getStringToPrint(buffer));

            if (!isCorrectData(buffer)) {
                LogUtils.log(Log.ERROR, TAG, "NOT CORRECT DATA!!");
                if (mLifevitSDKManager.getThermometerListener() != null) {
                    mLifevitSDKManager.getThermometerListener().onThermometerDeviceError(LifevitSDKConstants.THERMOMETER_ERROR_HARDWARE);
                }
                return;
            }

            byte command = buffer[4];

            if (command == THERMOMETER_CURRENT_RESULT) {

                // ------ MEASUREMENT ------

                int temperatureUnit = LifevitSDKConstants.TEMPERATURE_UNIT_CELSIUS;
                double temperatureValue;

                if ((buffer[5] == (byte) THERMOMETER_MODE_EAR_FARENHEIT) || (buffer[5] == (byte) THERMOMETER_MODE_FOREHEAD_FARENHEIT)) {
                    temperatureUnit = LifevitSDKConstants.TEMPERATURE_UNIT_FAHRENHEIT;
                }

                int mode = LifevitSDKConstants.THERMOMETER_MODE_FOREHEAD;
                if ((buffer[5] == (byte) THERMOMETER_MODE_EAR_FARENHEIT) || (buffer[5] == (byte) THERMOMETER_MODE_EAR_CELSIUS)) {
                    mode = LifevitSDKConstants.THERMOMETER_MODE_EAR;
                }

                int highBytes = (buffer[6] & 0xff);
                int lowBytes = (buffer[7] & 0xff);

                temperatureValue = ((double) ((highBytes * 256) + lowBytes)) / 100;

                if (temperatureUnit == LifevitSDKConstants.TEMPERATURE_UNIT_FAHRENHEIT) {
                    //Convertimos a farenheit (siempre viene en celsius...)
                    temperatureValue = TemperatureUtils.celsiusToFahrenheit(temperatureValue);
                }

                if (mLifevitSDKManager.getThermometerListener() != null) {
                    mLifevitSDKManager.getThermometerListener().onThermometerDeviceResult(mode, temperatureUnit, temperatureValue);
                    //Pare
                    sendCommand(LifevitSDKConstants.THERMOMETERV2_COMMAND_STOP);
                }

            }
            else if (command == THERMOMETER_LAST_RESULT) {

                // ------ MEASUREMENT ------

                int temperatureUnit = LifevitSDKConstants.TEMPERATURE_UNIT_CELSIUS;
                double temperatureValue;

                if ((buffer[5] == (byte) THERMOMETER_MODE_EAR_FARENHEIT) || (buffer[5] == (byte) THERMOMETER_MODE_FOREHEAD_FARENHEIT)) {
                    temperatureUnit = LifevitSDKConstants.TEMPERATURE_UNIT_FAHRENHEIT;
                }

                int mode = LifevitSDKConstants.THERMOMETER_MODE_FOREHEAD;
                if ((buffer[5] == (byte) THERMOMETER_MODE_EAR_FARENHEIT) || (buffer[5] == (byte) THERMOMETER_MODE_EAR_CELSIUS)) {
                    mode = LifevitSDKConstants.THERMOMETER_MODE_EAR;
                }

                int highBytes = (buffer[6] & 0xff);
                int lowBytes = (buffer[7] & 0xff);

                temperatureValue = ((double) ((highBytes * 256) + lowBytes)) / 100;

                if (temperatureUnit == LifevitSDKConstants.TEMPERATURE_UNIT_FAHRENHEIT) {
                    //Convertimos a farenheit (siempre viene en celsius...)
                    temperatureValue = TemperatureUtils.celsiusToFahrenheit(temperatureValue);
                }

                if (mLifevitSDKManager.getThermometerListener() != null) {
                    mLifevitSDKManager.getThermometerListener().onThermometerDeviceResult(mode, temperatureUnit, temperatureValue);
                    //Pare
                    sendCommand(LifevitSDKConstants.THERMOMETERV2_COMMAND_STOP);
                }

            }
            else if (command == THERMOMETER_ERROR_CODE) {

                // ------ ERRORS ------

                int errorCode = 0;

                if (buffer[5] == (byte) THERMOMETERV2_ERROR_CODE_HIGH_TEMP) {
                    errorCode = LifevitSDKConstants.THERMOMETER_ERROR_BODY_TEMPERATURE_HIGH;
                } else if (buffer[5] == (byte) THERMOMETERV2_ERROR_CODE_LOW_TEMP) {
                    errorCode = LifevitSDKConstants.THERMOMETER_ERROR_BODY_TEMPERATURE_LOW;
                } else if (buffer[5] == (byte) THERMOMETERV2_ERROR_CODE_AMBIENT_HIGH) {
                    errorCode = LifevitSDKConstants.THERMOMETER_ERROR_AMBIENT_TEMPERATURE_HIGH;
                } else if (buffer[5] == (byte) THERMOMETERV2_ERROR_CODE_AMBIENT_LOW) {
                    errorCode = LifevitSDKConstants.THERMOMETER_ERROR_AMBIENT_TEMPERATURE_LOW;
                } else if (buffer[5] == (byte) THERMOMETERV2_ERROR_CODE_ERR) {
                    errorCode = LifevitSDKConstants.THERMOMETER_ERROR_HARDWARE;
                } else if (buffer[5] == (byte) THERMOMETERV2_ERROR_CODE_LOW_VOLTAGE) {
                    errorCode = LifevitSDKConstants.THERMOMETER_ERROR_LOW_VOLTAGE;
                }

                if (errorCode != 0) {
                    if (mLifevitSDKManager.getThermometerListener() != null) {
                        mLifevitSDKManager.getThermometerListener().onThermometerDeviceError(errorCode);
                    }
                }
            } else if (command == THERMOMETER_SUCCESS) {

                if (mLifevitSDKManager.getThermometerListener() != null) {
                    mLifevitSDKManager.getThermometerListener().onThermometerCommandSuccess((buffer[5] & 0x00ff), (buffer[6] & 0x00ff));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}