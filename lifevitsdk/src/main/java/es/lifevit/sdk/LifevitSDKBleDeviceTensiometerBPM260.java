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
public class LifevitSDKBleDeviceTensiometerBPM260 extends LifevitSDKBleDevice {

    private final static String CLASS_TAG = LifevitSDKBleDeviceTensiometerBPM260.class.getSimpleName();

    private static final String DEVICE_STARTS = "e-Distri";
    private static final String DEVICE_STARTS_NEW = "TMB-2296-B";

    /*
    00001801-0000-1000-8000-00805f9b34fb
    00001800-0000-1000-8000-00805f9b34fb
    d973f2e0-b19e-11e2-9e96-0800200c9a66
     */

    // CUSTOM SERVICE (PRIMARY SERVICE)
    private static String UUID_SERVICE_RS232 = "0000fff0-0000-1000-8000-00805f9b34fb";

    // PROPERTIES: NOTIFY
    // Descriptors: Client Characteristic Configuration, UUID: 0x2902
    private static String UUID_BLOOD_PRESSURE_READ = "0000fff1-0000-1000-8000-00805f9b34fb";

    // Properties: WRITE, WRITE_NO_RESPONSE
    // Write Type: WRITE_RESQUEST
    private static String UUID_BLOOD_PRESSURE_SEND = "0000fff2-0000-1000-8000-00805f9b34fb";

    private static String UUID_BLOOD_PRESSURE_READ_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    protected LifevitSDKBleDeviceTensiometerBPM260(BluetoothDevice dev, LifevitSDKManager ch) {
        this.mBluetoothDevice = dev;
        this.mLifevitSDKManager = ch;
    }

    @Override
    protected int getType() {
        return LifevitSDKConstants.DEVICE_TENSIOMETER;
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
        if (mBluetoothGatt != null) {
            startMeasuring();
        }
    }


    /**
     * Other methods
     */

    protected static boolean isTensiometerBPM260Device(String name) {
        return name != null && (name.startsWith(DEVICE_STARTS) || name.startsWith(DEVICE_STARTS_NEW));
    }


    protected static UUID[] getUUIDs() {
        UUID[] uuidArray = new UUID[1];
        uuidArray[0] = UUID.fromString(UUID_SERVICE_RS232);
        return uuidArray;
    }


    protected void sendDeviceStatus() {
        mLifevitSDKManager.deviceOnConnectionChanged(LifevitSDKConstants.DEVICE_TENSIOMETER, mDeviceStatus, true);
    }


    /**
     * Enable Device Notification
     *
     * @return
     */
    protected void enableDeviceNotification() {

        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(UUID_SERVICE_RS232));
        if (RxService == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "Rx service not found! enableDeviceNotification()");
            disconnectGatt();
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(UUID.fromString(UUID_BLOOD_PRESSURE_READ));
        if (TxChar == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "Tx characteristic not found!");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar, true);

        // This is specific to Heart Rate Measurement.
        BluetoothGattDescriptor descriptor = TxChar
                .getDescriptor(UUID.fromString(UUID_BLOOD_PRESSURE_READ_DESCRIPTOR));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

        write(descriptor);
    }


    private void startMeasuring() {
        //Start pressure...
        if (mDeviceStatus == LifevitSDKConstants.STATUS_CONNECTED) {
            byte[] data = {(-3), (-3), -6, 5, 13, 10};
            sendMessage(data);
        } else {
            LogUtils.log(Log.ERROR, CLASS_TAG, "Device not ready");
        }
    }


    protected void sendMessage(byte[] data) {

        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(UUID_SERVICE_RS232));

        LogUtils.log(Log.DEBUG, CLASS_TAG, "mBluetoothGatt: " + mBluetoothGatt);

        if (RxService == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "Rx service not found! sendMessage()");
            return;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(UUID.fromString(UUID_BLOOD_PRESSURE_SEND));
        if (RxChar == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "Rx charateristic not found!");
            return;
        }

        dividePacketsAndSendMessage(RxChar, data, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }


    protected void characteristicReadProcessData(BluetoothGattCharacteristic characteristic) {

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if (UUID.fromString(UUID_BLOOD_PRESSURE_READ).equals(characteristic.getUuid())) {
            final byte[] bytes = characteristic.getValue();
            if (bytes != null && bytes.length >= 5) {
                if (bytes.length == 5 && bytes[2] == 0x06) {
                } else if (bytes[2] == -5) {
                    //durante la medición

                    byte pulH = bytes[3];
                    byte pulL = bytes[4];

                    int pulse = ((pulH << 16) & 0xff00) + (pulL & 0x00ff);

                    if (mLifevitSDKManager.getHeartListener() != null) {
                        mLifevitSDKManager.getHeartListener().heartDeviceOnProgressMeasurement(pulse);
                    }

                } else if (bytes[2] == -4) {
                    //resulado
                    int sys = bytes[3] & 0x00ff;
                    int dia = bytes[4] & 0x00ff;
                    int pul = bytes[5] & 0x00ff;

                    LifevitSDKHeartData result = new LifevitSDKHeartData();
                    result.setSystolic(sys);
                    result.setDiastolic(dia);
                    result.setPulse(pul);
                    result.setDate(Calendar.getInstance().getTimeInMillis());
                    result.setErrorCode(LifevitSDKConstants.CODE_OK);

                    if (mLifevitSDKManager.getHeartListener() != null) {
                        mLifevitSDKManager.getHeartListener().heartDeviceOnResult(result);
                    }

                } else if (bytes[2] == -3) {
                    //error

                    LifevitSDKHeartData result = new LifevitSDKHeartData();
                    result.setSystolic(0);
                    result.setDiastolic(0);
                    result.setPulse(0);
                    result.setDate(Calendar.getInstance().getTimeInMillis());
                    result.setErrorCode(LifevitSDKConstants.CODE_UNKNOWN);

                    if (bytes[3] == 0x0e) {
                        result.setErrorCode(LifevitSDKConstants.CODE_UNKNOWN);
                    }
                    if (bytes[3] == 0x01) {
                        result.setErrorCode(LifevitSDKConstants.CODE_LOW_SIGNAL);
                    }
                    if (bytes[3] == 0x02) {
                        result.setErrorCode(LifevitSDKConstants.CODE_NOISE);
                    }
                    if (bytes[3] == 0x03) {
                        result.setErrorCode(LifevitSDKConstants.CODE_INFLATION_TIME);
                    }
                    if (bytes[3] == 0x05) {
                        result.setErrorCode(LifevitSDKConstants.CODE_ABNORMAL_RESULT);
                    }
                    if (bytes[3] == 0x0c) {
                        result.setErrorCode(LifevitSDKConstants.CODE_RETRY);
                    }
                    if (bytes[3] == 0x0b) {
                        result.setErrorCode(LifevitSDKConstants.CODE_LOW_BATTERY);
                    }

                    if (mLifevitSDKManager.getHeartListener() != null) {
                        mLifevitSDKManager.getHeartListener().heartDeviceOnResult(result);
                    }
                }
            }
        }
    }
}