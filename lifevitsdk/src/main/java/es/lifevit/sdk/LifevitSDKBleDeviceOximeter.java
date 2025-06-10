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

import es.lifevit.sdk.utils.LogUtils;


/**
 * Created by aescanuela on 26/1/16.
 */
public class LifevitSDKBleDeviceOximeter extends LifevitSDKBleDevice {

    private final static String TAG = LifevitSDKBleDeviceOximeter.class.getSimpleName();

    private static final String DEVICE_NAME = "BLT_M70C";

    int bytecounter = 0;


    /**
     * Service Descriptors
     */


    // UUIDs
    private static String OXIMETER_UUID_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static String OXIMETER_UUID_READ_CHARACTERISTIC = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static String OXIMETER_UUID_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";


    protected LifevitSDKBleDeviceOximeter(BluetoothDevice dev, LifevitSDKManager ch) {
        this.mBluetoothDevice = dev;
        this.mLifevitSDKManager = ch;
    }


    @Override
    protected int getType() {
        return LifevitSDKConstants.DEVICE_OXIMETER;
    }

    /**
     * Receivers
     */

    protected void connectGatt(Context context, boolean firstTime) {
        mBluetoothGatt = mBluetoothDevice.connectGatt(context, false, this.mGattCallback);
        mContext = context;
        mFirstTime = firstTime;
    }


    protected void startReceiver(String action, Intent intent) {
    }


    /**
     * Other methods
     */

    protected static boolean isOximeterDevice(String name) {
        return DEVICE_NAME.equalsIgnoreCase(name);
    }


    public static UUID[] getUUIDs() {
        UUID[] uuidArray = new UUID[1];
        uuidArray[0] = UUID.fromString(OXIMETER_UUID_SERVICE);
        return uuidArray;
    }


    protected void sendDeviceStatus() {
        mLifevitSDKManager.deviceOnConnectionChanged(LifevitSDKConstants.DEVICE_OXIMETER, mDeviceStatus, true);
    }


    /**
     * Enable Device Notification
     *
     * @return
     */
    protected void enableDeviceNotification() {

        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(OXIMETER_UUID_SERVICE));
        if (RxService == null) {
            LogUtils.log(Log.ERROR, TAG, "Rx service not found!");
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(UUID.fromString(OXIMETER_UUID_READ_CHARACTERISTIC));
        if (TxChar == null) {
            LogUtils.log(Log.ERROR, TAG, "Tx charateristic not found!");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar, true);

        // This is specific to Heart Rate Measurement.
        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(UUID.fromString(OXIMETER_UUID_DESCRIPTOR));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

        write(descriptor);
    }


    protected void sendMessage(byte[] data) {

        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(OXIMETER_UUID_SERVICE));

        LogUtils.log(Log.DEBUG, TAG, "mBluetoothGatt: " + mBluetoothGatt);

        if (RxService == null) {
            LogUtils.log(Log.ERROR, TAG, "Rx service not found!");
            return;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(UUID.fromString(OXIMETER_UUID_READ_CHARACTERISTIC));
        if (RxChar == null) {
            LogUtils.log(Log.ERROR, TAG, "Rx charateristic not found!");
            return;
        }

        dividePacketsAndSendMessage(RxChar, data, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }


    protected void characteristicReadProcessData(BluetoothGattCharacteristic characteristic) {

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if (UUID.fromString(OXIMETER_UUID_READ_CHARACTERISTIC).equals(characteristic.getUuid())) {
            final byte[] bytes = characteristic.getValue();
            parseOximeterData(bytes);
        }
    }


    public void parseOximeterData(byte[] data) {
        byte b = data[0];
        if (b == (byte) 0xAA) {//FIRST ROW OF DATA
            //Log.e("SOSTECA-BLE/OXIMETER","NEW DATA BATCH");
            bytecounter = 0;
            for (int i = 2; i < data.length; i++) {
                bytecounter++;
            }
        } else {
            LifevitSDKOximeterData result = new LifevitSDKOximeterData();
            result.setDate(Calendar.getInstance().getTimeInMillis());

            int resultPleth = -1;

            for (int i = 0; i < data.length; i++) {
                if (bytecounter == 33) {
                    int pi = data[i] & 0xFF;
                    if (pi == 255) {
                        result.setPi(-1);
                    } else {
                        result.setPi(pi);
                    }
                } else if (bytecounter == 34) {
                    int dat = data[i] & 0x0fffffff;
                    int spo2 = dat & 0xFF;
                    if (spo2 == 127 || spo2 == 255) {
                        result.setSpO2(-1);
                    } else {
                        result.setSpO2(spo2);
                    }
                } else if (bytecounter == 35) {
                    int pulse = data[i] % 0xFF;
                    if (pulse == 127 || pulse == -1) {
                        result.setLpm(-1);
                    } else {
                        result.setLpm(pulse);
                    }
                } else if (bytecounter == 36) {
                    int rpm = data[i] % 0xFF;
                    result.setRpm(rpm);
                } else if (bytecounter >= 45) {
                    if (i != 19) {
                        int dat = data[i] & 0x0fffffff;
                        int waveVal = dat & 0xFF;
                        if (waveVal < 100) {
                            resultPleth = waveVal;
                        }
                    }
                }
                bytecounter++;
            }

            if (mLifevitSDKManager.getOximeterListener() != null) {
                if ((result.getPi() == null || result.getPi() == -1 || result.getPi() == 0)
                        && (result.getSpO2() == null || result.getSpO2() == -1 || result.getSpO2() == 0)
                        && (result.getRpm() == null || result.getRpm() == -1 || result.getRpm() == 0)
                        && (result.getLpm() == null || result.getLpm() == -1 || result.getLpm() == 0)) {

                    // Only pleth
                    mLifevitSDKManager.getOximeterListener().oximeterDeviceOnProgressMeasurement(resultPleth);
                } else {

                    // Other values, not pleth
                    mLifevitSDKManager.getOximeterListener().oximeterDeviceOnResult(result);
                }
            }
        }
    }


}