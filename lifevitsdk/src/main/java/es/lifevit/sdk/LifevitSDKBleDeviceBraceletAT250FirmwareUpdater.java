package es.lifevit.sdk;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.UUID;

import es.lifevit.sdk.dfu.DfuService;
import es.lifevit.sdk.utils.HexUtils;
import es.lifevit.sdk.utils.LogUtils;
import no.nordicsemi.android.dfu.DfuBaseService;


/**
 * Created by aescanuela on 26/1/16.
 */
public class LifevitSDKBleDeviceBraceletAT250FirmwareUpdater extends LifevitSDKBleDevice {

    private final static String CLASS_TAG = LifevitSDKBleDeviceBraceletAT250FirmwareUpdater.class.getSimpleName();

    private static final String DEVICE_NAME = "DfuTarg";
    private static final String DEVICE_NAME_PREFIX = "dfu";


    /**
     * Service descriptor
     */

    // Custom service (primary service)
    private static final String UUID_SERVICE = "00001530-1212-EFDE-1523-785FEABCD123";

    // Properties: NOTIFY
    // Descriptors:
    // 1. Client Characteristic Configuration, UUID: 0x2902 (defines how the characteristic may be configured by a specific client)
    private static final String UUID_CHARACTERISTIC_NOTIFY = "00001531-1212-EFDE-1523-785FEABCD123";

    // Properties: READ, WRITE, WRITE_NO_RESPONS (client can read, write, and write with response, on this characteristic)
    // Write Type: WRITE REQUEST (will give you a response back telling you the write was successful)
    private static final String UUID_CHARACTERISTIC_SEND = "00001532-1212-EFDE-1523-785FEABCD123";


    /**
     * Creator
     */

    protected LifevitSDKBleDeviceBraceletAT250FirmwareUpdater(BluetoothDevice dev, LifevitSDKManager manager) {

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[connection] LifevitSDKBleDeviceBraceletAT250FirmwareUpdater");

        this.mBluetoothDevice = dev;
        this.mLifevitSDKManager = manager;
    }

    @Override
    protected int getType() {
        return LifevitSDKConstants.DEVICE_BRACELET_AT250_FIRMWARE_UPDATER;
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
        LogUtils.log(Log.DEBUG, CLASS_TAG, "[startReceiver]" + " action: " + (action != null ? action : "null"));
    }


    protected void doFirmwareUpdate() {
        LogUtils.log(Log.DEBUG, CLASS_TAG, "doFirmwareUpdate");
        sendHex(this.getDevice().getName(), this.getDevice().getAddress());
    }


    private void sendHex(String name, String address) {

        LogUtils.log(Log.DEBUG, CLASS_TAG, "sendHex"
                + ", name: " + (name != null ? name : "null")
                + ", address: " + (address != null ? address : "null")
        );

        Intent intent = new Intent(mContext, DfuService.class);

        intent.putExtra(DfuBaseService.EXTRA_DEVICE_NAME, name);
        intent.putExtra(DfuBaseService.EXTRA_DEVICE_ADDRESS, address);
        intent.putExtra(DfuBaseService.EXTRA_FILE_MIME_TYPE, DfuBaseService.MIME_TYPE_OCTET_STREAM);

//        copyFilefromAssets();
//        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + FilePath;

        Uri uri = null;
        intent.putExtra(DfuBaseService.EXTRA_FILE_PATH, "assets://" + DfuBaseService.FIRMWARE_FILE_NAME);
        intent.putExtra(DfuBaseService.EXTRA_FILE_TYPE, DfuBaseService.TYPE_APPLICATION);
        intent.putExtra(DfuBaseService.EXTRA_FILE_URI, uri);
        mContext.startService(intent);
    }


//    private void copyFilefromAssets() {
//        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + FilePath;
//        File file = new File(filePath);
//        try {
//
//            if (file.exists()) {
//                return;
//            }
//
//            InputStream inputStream = mContext.getAssets().open(FilePath);
//            OutputStream outputStream = new FileOutputStream(filePath);
//            byte[] buffer = new byte[1024];
//            while (inputStream.read(buffer) > 0) {
//                outputStream.write(buffer);
//            }
//            outputStream.flush();
//            inputStream.close();
//            outputStream.close();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }


    /**
     * Other methods
     */

    protected static boolean isBraceletAT250FirmwareUpdaterDevice(String name) {
        return name != null && name.toLowerCase().contains(DEVICE_NAME_PREFIX);
    }


    protected static boolean matchDevice(BluetoothDevice device) {
        return isBraceletAT250FirmwareUpdaterDevice(device.getName());
    }


    protected static UUID[] getUUIDs() {
        UUID[] uuidArray = new UUID[1];
        uuidArray[0] = UUID.fromString(UUID_SERVICE);
        return uuidArray;
    }


    protected void sendDeviceStatus() {
        LogUtils.log(Log.DEBUG, CLASS_TAG, "sendDeviceStatus: " + mDeviceStatus);

        // Send device status to stop scan
        mLifevitSDKManager.deviceOnConnectionChanged(LifevitSDKConstants.DEVICE_BRACELET_AT250_FIRMWARE_UPDATER, mDeviceStatus, true);
    }


    /**
     * Enable Device Notification
     *
     * @return
     */
    protected void enableDeviceNotification() {

        LogUtils.log(Log.DEBUG, CLASS_TAG, "enableDeviceNotification");

        // Send device status "STATUS_CONNECTED" to stop scan
        mDeviceStatus = LifevitSDKConstants.STATUS_CONNECTED;
        LogUtils.log(Log.DEBUG, CLASS_TAG, "onDescriptorWrite - sendDeviceStatus");
        sendDeviceStatus();


        // Start Firmware Update
        doFirmwareUpdate();


//        if (this.mBluetoothGatt == null) {
//            return;
//        }
//
//        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(UUID_SERVICE));
//        if (RxService == null) {
//            Log.e(LifevitSDKConstants.TAG, "[" + CLASS_TAG + "] Rx service not found!");
//            return;
//        }
//
//        BluetoothGattCharacteristic txCharacteristic = RxService.getCharacteristic(UUID.fromString(UUID_CHARACTERISTIC_NOTIFY));
//
//        if (txCharacteristic == null) {
//            Log.e(LifevitSDKConstants.TAG, "[" + CLASS_TAG + "] Tx charateristic not found!");
//            return;
//        }
//
//        mBluetoothGatt.setCharacteristicNotification(txCharacteristic, true);
//
//        for (BluetoothGattDescriptor descriptor : txCharacteristic.getDescriptors()) {
//
//            // It's a notify characteristic
//            if (descriptor != null) {
//                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                write(descriptor);
//            }
//        }
    }


    protected void sendMessage(byte[] data) {

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[sendMessage]");


//        if (mBluetoothGatt == null) {
//            Log.e(LifevitSDKConstants.TAG, "[" + CLASS_TAG + "] sendMessage: mBluetoothGatt is null");
//            return;
//        }
//        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(UUID_SERVICE));
//        if (RxService == null) {
//            Log.e(LifevitSDKConstants.TAG, "[" + CLASS_TAG + "] Rx service not found!");
//            return;
//        }
//        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(UUID.fromString(UUID_CHARACTERISTIC_SEND));
//        if (RxChar == null) {
//            Log.e(LifevitSDKConstants.TAG, "[" + CLASS_TAG + "] Rx charateristic not found!");
//            return;
//        }
//
//        dividePacketsAndSendMessage(RxChar, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
    }


    /*********************************************************************************************/
    /***************************  Methods to receive data from device  ***************************/
    /*********************************************************************************************/


    protected void characteristicReadProcessData(BluetoothGattCharacteristic characteristic) {

        byte[] rx = characteristic.getValue();
        LogUtils.log(Log.DEBUG, CLASS_TAG, "[RECEIVED]: " + HexUtils.getStringToPrint(rx));
    }


}