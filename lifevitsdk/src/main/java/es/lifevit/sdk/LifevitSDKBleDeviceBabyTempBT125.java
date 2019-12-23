package es.lifevit.sdk;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.UUID;

import es.lifevit.sdk.utils.LogUtils;
import es.lifevit.sdk.utils.TemperatureUtils;


/**
 * Created by aescanuela on 26/1/16.
 */
public class LifevitSDKBleDeviceBabyTempBT125 extends LifevitSDKBleDevice {

    private final static String CLASS_TAG = LifevitSDKBleDeviceBabyTempBT125.class.getSimpleName();

    private static final String DEVICE_NAME_TESTS = "iCcur";
    private static final String DEVICE_NAME = "thermF";


    /**
     * Service descriptor
     */

    // Custom service (primary service)
    private static String UUID_SERVICE_RS232 = "cccc1e08-93d4-4c06-bcba-e719578f1408";


    public static final String CURRENT_TEMPERATURE_CONFIG_UDID = "ccccaa01-93d4-4c06-bcba-e719578f1408";

    public static final String CURRENT_TEMPERATURE_DATA_UDID = "ccccaa02-93d4-4c06-bcba-e719578f1408";
    public static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";


    /**
     * Creator
     */

    protected LifevitSDKBleDeviceBabyTempBT125(BluetoothDevice dev, LifevitSDKManager manager) {

        this.mBluetoothDevice = dev;
        this.mLifevitSDKManager = manager;
    }



    @Override
    protected int getType() {
        return LifevitSDKConstants.DEVICE_BABY_TEMP_BT125;
    }
    /**
     * Receivers
     */

    protected void connectGatt(Context context, boolean firstTime) {

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[connection] CONNECT: " + mBluetoothDevice.getAddress());

        mBluetoothGatt = mBluetoothDevice.connectGatt(context, false, mGattCallback);
        mContext = context;
        mFirstTime = firstTime;

    }
    @Override
    protected void startReceiver(String action, Intent intent) {
    }


    /**
     * Other methods
     */

    public static boolean isBabyTempDevice(String name) {
        return name != null && (name.contains(DEVICE_NAME) || name.contains(DEVICE_NAME_TESTS));
    }


    protected static UUID[] getUUIDs() {
        return new UUID[0];
    }


    protected void sendDeviceStatus() {
        mLifevitSDKManager.deviceOnConnectionChanged(LifevitSDKConstants.DEVICE_BABY_TEMP_BT125, mDeviceStatus, true);
        if (mDeviceStatus == LifevitSDKConstants.STATUS_CONNECTED) {
            sendGetTemper();
        }
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

        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(UUID_SERVICE_RS232));
        if (RxService == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "] Rx service not found!");
            return;
        }

        BluetoothGattCharacteristic txCharacteristic = RxService.getCharacteristic(UUID.fromString(CURRENT_TEMPERATURE_DATA_UDID));

        if (txCharacteristic == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "] Tx charateristic not found!");
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
            LogUtils.log(Log.ERROR, CLASS_TAG, "] sendMessage: mBluetoothGatt is null");
            return;
        }
        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(UUID_SERVICE_RS232));
        if (RxService == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "] Rx service not found!");
            return;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(UUID.fromString(CURRENT_TEMPERATURE_CONFIG_UDID));
        if (RxChar == null) {
            LogUtils.log(Log.ERROR, CLASS_TAG, "] Rx charateristic not found!");
            return;
        }

        dividePacketsAndSendMessage(RxChar, data, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }


    /*********************************************************************************************/
    /***************************  Methods to receive data from device  ***************************/
    /*********************************************************************************************/


    protected void characteristicReadProcessData(BluetoothGattCharacteristic characteristic) {

        if (UUID.fromString(CURRENT_TEMPERATURE_DATA_UDID).equals(characteristic.getUuid())) {
            final byte[] bytes = characteristic.getValue();
            if (bytes != null && bytes.length >= 5) {

                String rawTemperatureString = new String(bytes);
                int rawBodyTemp = Integer.valueOf(rawTemperatureString.substring(0, 4));
                int rawEnvironmentTemp = Integer.valueOf(rawTemperatureString.substring(4, 8));

                Float resultTemperatureCelsius = TemperatureUtils.getTemperatureInDegreesFromRaw(rawBodyTemp);
                Float resultEnvTemperatureCelsius = TemperatureUtils.getEnvironmentTemperInDegreesFromRaw(rawEnvironmentTemp);

                if (mLifevitSDKManager.getBabyTempBT125Listener()!=null) {
                    mLifevitSDKManager.getBabyTempBT125Listener().onBabyTempDataReady(resultTemperatureCelsius, resultEnvTemperatureCelsius);
                }
            }
        }
    }


    /*********************************************************************************************/
    /**************************************  Helper methods  *************************************/
    /*********************************************************************************************/


    public void sendGetTemper() {
        byte[] values = {1};
        sendMessage(values);
    }


}