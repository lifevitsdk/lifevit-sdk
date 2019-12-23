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


/**
 * Created by aescanuela on 26/1/16.
 */
public class LifevitSDKBleDeviceTensiometerV3 extends LifevitSDKBleDevice {

    private final static String CLASS_TAG = LifevitSDKBleDeviceTensiometerV3.class.getSimpleName();

    private static final String DEVICE_NAME = "Belter_BT";

    int lastSystolic;
    int lastDiastolic;
    int lastPulse;


    /**
     * Service Descriptors
     */

    // DEVICE INFORMATION
    private static String UUID_SERVICE_DEVICE_INFO = "0000180A-0000-1000-8000-00805f9b34fb";

    // CUSTOM SERVICE (PRIMARY SERVICE)
    private static String UUID_SERVICE_RS232 = "0000fff0-0000-1000-8000-00805f9b34fb";

    // PROPERTIES: NOTIFY
    // Descriptors: Characteristic User Description, UUID: 0x2901
    // Descriptors: Client Characteristic Configuration, UUID: 0x2902
    private static String UUID_BLOOD_PRESSURE_READ = "0000fff4-0000-1000-8000-00805f9b34fb";

    // Properties: READ, WRITE, WRITE_NO_RESPONSE
    // Write Type: WRITE_RESQUEST
    private static String UUID_BLOOD_PRESSURE_SEND = "0000fff3-0000-1000-8000-00805f9b34fb";
    // Descriptors: Characteristic User Description, UUID: 0x2901
    private static String UUID_BLOOD_PRESSURE_READ_DESCRIPTOR = "00002901-0000-1000-8000-00805f9b34fb";
    private static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";


    protected LifevitSDKBleDeviceTensiometerV3(BluetoothDevice dev, LifevitSDKManager ch) {
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
        LogUtils.log(Log.DEBUG, CLASS_TAG, "connectGatt. firstTime: " + firstTime);
        mBluetoothGatt = mBluetoothDevice.connectGatt(context, false, this.mGattCallback);
        mContext = context;
        mFirstTime = firstTime;
    }

    protected void startReceiver(String action, Intent intent) {
        LogUtils.log(Log.DEBUG, CLASS_TAG, "startReceiver. action: " + action);
        if (mBluetoothGatt != null) {
            startMeasuring();
        }
    }


    /**
     * Other methods
     */

    protected static boolean isNewTensiometerDevice(String name) {
        return DEVICE_NAME.equalsIgnoreCase(name);
    }


    protected static UUID[] getUUIDs() {
//        UUID[] uuidArray = new UUID[1];
//        uuidArray[0] = UUID.fromString(UUID_SERVICE_DEVICE_INFO);
//        uuidArray[0] = UUID.fromString(UUID_SERVICE_RS232);
//        return uuidArray;
        return new UUID[0];

    }


    protected void sendDeviceStatus() {
        if (mLifevitSDKManager.getConnectionListener() != null) {
            mLifevitSDKManager.getConnectionListener().heartDeviceOnConnectionChanged(mDeviceStatus);
        }

        LogUtils.log(Log.DEBUG, CLASS_TAG, "sendDeviceStatus. mDeviceStatus: " + mDeviceStatus);

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
            LogUtils.log(Log.ERROR, CLASS_TAG, "Rx service not found!");
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(UUID.fromString(UUID_BLOOD_PRESSURE_READ));
        if (TxChar == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "Tx charateristic not found!");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar, true);

        // This is specific to Heart Rate Measurement.
        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

        write(descriptor);
    }


    private void startMeasuring() {

        LogUtils.log(Log.DEBUG, CLASS_TAG, "startMeasuring.");

        //Start pressure...
        if (mDeviceStatus == LifevitSDKConstants.STATUS_CONNECTED) {

            byte[] data = new byte[9];
            data[0] = (byte) 0x02;
            data[1] = (byte) 0x40;
            data[2] = (byte) 0xDD;
            data[3] = (byte) 0x04;
            data[4] = (byte) 0xFF;
            data[5] = (byte) 0xFD;
            data[6] = (byte) 0x02;
            data[7] = (byte) 0x01;
            data[8] = (byte) 0x98;
            /*
            byte xor = 0x0;
            for (int i = 0; i < 8; i++) {
                xor = (byte) (xor ^ data[i]);
            }
            data[8] = xor;
            */

            sendMessage(data);
        } else {
            LogUtils.log(Log.ERROR, CLASS_TAG, "Device not ready");
        }
    }


    protected void sendMessage(byte[] data) {

        LogUtils.log(Log.DEBUG, CLASS_TAG, "sendMessage. data = " + HexUtils.getStringToPrint(data));

        BluetoothGattService TxService = mBluetoothGatt.getService(UUID.fromString(UUID_SERVICE_RS232));
        if (TxService == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "Tx service not found!");
            return;
        }
        BluetoothGattCharacteristic TxChar = TxService.getCharacteristic(UUID.fromString(UUID_BLOOD_PRESSURE_SEND));
        if (TxChar == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "Tx charateristic not found!");
            return;
        }

        dividePacketsAndSendMessage(TxChar, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
    }


    protected void characteristicReadProcessData(BluetoothGattCharacteristic characteristic) {

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.

        LogUtils.log(Log.DEBUG, CLASS_TAG, "characteristicReadProcessData. bytes " );

        if (UUID.fromString(UUID_BLOOD_PRESSURE_READ).equals(characteristic.getUuid())) {

            final byte[] bytes = characteristic.getValue();

            LogUtils.log(Log.DEBUG, CLASS_TAG, "characteristicReadProcessData. bytes = " + HexUtils.getStringToPrint(bytes));

            if (bytes.length == 9) {
                LogUtils.log(Log.DEBUG, CLASS_TAG, "characteristicReadProcessData. bytes = " + HexUtils.getStringToPrint(bytes));
            }

            if (bytes.length == 7 && bytes[0] == (byte) 0x02) {

                // During medition
                int temporaryDiastolic = bytes[5] & 0x00FF;

                if (mLifevitSDKManager.getConnectionListener() != null) {
                    mLifevitSDKManager.getConnectionListener().heartDeviceOnProgressMeasurement(temporaryDiastolic);
                }
                if (mLifevitSDKManager.getHeartListener() != null) {
                    mLifevitSDKManager.getHeartListener().heartDeviceOnProgressMeasurement(temporaryDiastolic);
                }

            } else if (bytes.length == 9 && bytes[0] == (byte) 0x02 && bytes[1] == (byte) 0x40 && bytes[2] == (byte) 0xD4) {

                int battery = bytes[7] & 0x00ff;

                if (mLifevitSDKManager.getConnectionListener() != null) {
                    mLifevitSDKManager.getConnectionListener().heartDeviceOnBatteryResult(battery);
                }
                if (mLifevitSDKManager.getHeartListener() != null) {
                    mLifevitSDKManager.getHeartListener().heartDeviceOnBatteryResult(battery);
                }
            } else if (bytes.length == 9 && bytes[0] == (byte) 0x02 && bytes[1] == (byte) 0x40 && bytes[4] == (byte) 0xFF) {

                // Error
                LifevitSDKHeartData result = new LifevitSDKHeartData();
                result.setSystolic(0);
                result.setDiastolic(0);
                result.setPulse(0);
                result.setDate(Calendar.getInstance().getTimeInMillis());
                result.setErrorCode(LifevitSDKConstants.CODE_UNKNOWN);

                byte errH = bytes[6];
                byte errL = bytes[7];

                int err = ((errH << 16) & 0xff00) + (errL & 0x00ff);



                if (err == (byte) 0x01) {
                    result.setErrorCode(LifevitSDKConstants.CODE_NO_RESULTS);
                } else if (err == (byte) 0x02) {
                    result.setErrorCode(LifevitSDKConstants.CODE_TOO_MUCH_INTERFERENCE);
                } else if (err == (byte) 0x03) {
                    result.setErrorCode(LifevitSDKConstants.CODE_INFLATION_TIME);
                } else if (err == (byte) 0x05) {
                    result.setErrorCode(LifevitSDKConstants.CODE_ABNORMAL_RESULT);
                } else if (err == (byte) 0x07) {
                    result.setErrorCode(LifevitSDKConstants.CODE_UNKNOWN);
                } else if (err == (byte) 0x0b) {
                    result.setErrorCode(LifevitSDKConstants.CODE_LOW_BATTERY);
                }

                if (mLifevitSDKManager.getConnectionListener() != null) {
                    mLifevitSDKManager.getConnectionListener().heartDeviceOnResult(result);
                }
                if (mLifevitSDKManager.getHeartListener() != null) {
                    mLifevitSDKManager.getHeartListener().heartDeviceOnResult(result);
                }

            } else if (bytes.length == 17) {


                byte sysH = bytes[5];
                byte sysL = bytes[6];

                int systolic = ((sysH << 16) & 0xff00) + (sysL & 0x00ff);


                byte diaH = bytes[7];
                byte diaL = bytes[8];

                int diastolic = ((diaH << 16) & 0xff00) + (diaL & 0x00ff);


                byte pulH = bytes[11];
                byte pulL = bytes[12];

                int pulse = ((pulH << 16) & 0xff00) + (pulL & 0x00ff);


                LifevitSDKHeartData result = new LifevitSDKHeartData();
                result.setSystolic(systolic);
                result.setDiastolic(diastolic);
                result.setPulse(pulse);
                result.setDate(Calendar.getInstance().getTimeInMillis());
                result.setErrorCode(LifevitSDKConstants.CODE_OK);

                if (systolic != lastSystolic || diastolic != lastDiastolic || pulse != lastPulse) {

                    lastSystolic = systolic;
                    lastDiastolic = diastolic;
                    lastPulse = pulse;

                    if (mLifevitSDKManager.getConnectionListener() != null) {
                        mLifevitSDKManager.getConnectionListener().heartDeviceOnResult(result);
                    }
                    if (mLifevitSDKManager.getHeartListener() != null) {
                        mLifevitSDKManager.getHeartListener().heartDeviceOnResult(result);
                    }
                }
            }
        }
    }

}