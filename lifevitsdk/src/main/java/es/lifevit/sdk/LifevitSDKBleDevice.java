package es.lifevit.sdk;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;

import es.lifevit.sdk.utils.HexUtils;
import es.lifevit.sdk.utils.LogUtils;


/**
 * Created by aescanuela on 26/1/16.
 */
public abstract class LifevitSDKBleDevice {

    private final static String CLASS_TAG = LifevitSDKBleDevice.class.getSimpleName();

    // Device Information Service
    protected static String SERIAL_UUID = "00002a25-0000-1000-8000-00805f9b34fb";
    private String serialNumber = "";

    /**
     * Attributes
     */

    protected BluetoothDevice mBluetoothDevice;
    protected LifevitSDKManager mLifevitSDKManager;
    protected int mDeviceStatus = LifevitSDKConstants.STATUS_DISCONNECTED;
    protected BluetoothGatt mBluetoothGatt;
    protected Context mContext;
    protected boolean mFirstTime;

    /**
     * Device states constants
     */

    /**
     * Actions constants
     */

    protected BluetoothDevice getDevice() {
        return mBluetoothDevice;
    }

    private boolean sIsWriting = false;
    private LinkedBlockingDeque<Object> sWriteQueue = new LinkedBlockingDeque<>();

    private static Object lock = new Object();

    /**
     * Abstract methods to be defined by Subclasses
     */

    protected abstract void connectGatt(Context context, boolean firstTime);

    protected synchronized void disconnectGatt() {

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[connection] DISCONNECT: " + mBluetoothDevice.getAddress());

        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            if (mBluetoothGatt != null) {
                mBluetoothGatt.close();
            }
            mBluetoothGatt = null;
        }
    }

    protected abstract int getType();

    protected abstract void startReceiver(String action, Intent intent);

    protected void sendDeviceStatus(){

        mLifevitSDKManager.deviceOnConnectionChanged(getType(), mDeviceStatus, true);
    }

    protected abstract void sendMessage(byte[] data);

    protected abstract void enableDeviceNotification();

    protected abstract void characteristicReadProcessData(BluetoothGattCharacteristic characteristic);


    /**
     * Non abstract methods
     */

    protected void closeGatt() {
        LogUtils.log(Log.DEBUG, CLASS_TAG, "closeGatt. mBluetoothGatt: " + mBluetoothGatt);
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }
        mBluetoothGatt = null;
    }


    protected int getDeviceStatus() {
        return this.mDeviceStatus;
    }


    protected void dividePacketsAndSendMessage(BluetoothGattCharacteristic RxChar, byte[] data, int writeType) {

        for (int i = 0; i < data.length; i += 20) {

            byte[] subArray = Arrays.copyOfRange(data, i, Math.min(i + 20, data.length));

            LogUtils.log(Log.DEBUG, CLASS_TAG, "SENDING Message: " + HexUtils.getStringToPrint(subArray));

            RxChar.setValue(subArray);
            RxChar.setWriteType(writeType);
            write(RxChar);
        }
    }


    /**
     * Generic callback
     */

    // Various callback methods defined by the BLE API.
    protected BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    synchronized (lock) {
                        LogUtils.log(Log.DEBUG, CLASS_TAG, "onConnectionStateChange. New state: " + LogUtils.getBluetoothStateName(newState) + ", status: " + LogUtils.getGattStatusName(status) + ".Instance " + LifevitSDKBleDevice.this.toString());

                        if (newState == BluetoothProfile.STATE_CONNECTED) {

                            if (mBluetoothGatt == null) {
                                mBluetoothGatt = gatt;
                            }
                            mDeviceStatus = LifevitSDKConstants.STATUS_CONNECTING;

                            LogUtils.log(Log.DEBUG, CLASS_TAG, "Connected to GATT server. Attempting to start service discovery:" + mBluetoothGatt.discoverServices());

                            mLifevitSDKManager.setConnectedDevice(LifevitSDKBleDevice.this);
                            clearQueue();

                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            mDeviceStatus = LifevitSDKConstants.STATUS_DISCONNECTED;

                            LogUtils.log(Log.INFO, CLASS_TAG, "Disconnected from GATT server. Status = " + status);

                            if (mBluetoothGatt != null) {
                                mBluetoothGatt.disconnect();
                            }
                            closeGatt();

                            mLifevitSDKManager.setDeviceDisconnected(LifevitSDKBleDevice.this);
                        }

                        LogUtils.log(Log.DEBUG, CLASS_TAG, "onConnectionStateChange -> sendDeviceStatus");

                        // Wether it is connect or disconnect, send the new status
                        sendDeviceStatus();
                    }
                }


                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    LogUtils.log(Log.DEBUG, CLASS_TAG, "onServicesDiscovered. Status: " + LogUtils.getGattStatusName(status));
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        enableDeviceNotification();

                        for ( BluetoothGattService deviceService : gatt.getServices()) {
                                BluetoothGattCharacteristic characteristic = deviceService.getCharacteristic(UUID.fromString(SERIAL_UUID));
                                if (characteristic != null) {
                                    gatt.readCharacteristic(characteristic);
                                }

                        }
                    }
                }


                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicRead(gatt, characteristic, status);
                    LogUtils.log(Log.DEBUG, CLASS_TAG, "onCharacteristicRead. Status: " + LogUtils.getGattStatusName(status));

                    if(characteristic.getUuid().toString().equalsIgnoreCase(SERIAL_UUID)){
                        serialNumber = characteristic.getStringValue(0);
                        Log.d(LifevitSDKConstants.TAG, "Serial number: " + serialNumber);
                    }
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        characteristicReadProcessData(characteristic);
                    }
                }


                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);
                    LogUtils.log(Log.DEBUG, CLASS_TAG, "onCharacteristicChanged");
                    characteristicReadProcessData(characteristic);
                }


                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    LogUtils.log(Log.DEBUG, CLASS_TAG, "onCharacteristicWrite. Status: " + LogUtils.getGattStatusName(status));
                    sIsWriting = false;
                    nextWrite();
                }


                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorWrite(gatt, descriptor, status);
                    LogUtils.log(Log.DEBUG, CLASS_TAG, "onDescriptorWrite: " + status + ", descriptor =" + descriptor.getUuid().toString());

                    //Ya estamos listos para empezar a medir la presión, mostrar los pasos....
                    mDeviceStatus = LifevitSDKConstants.STATUS_CONNECTED;

                    LogUtils.log(Log.DEBUG, CLASS_TAG, "onDescriptorWrite - sendDeviceStatus");

                    onDescriptorWritten(descriptor);
                    sendDeviceStatus();
                    sIsWriting = false;
                    nextWrite();

                }

                @Override
                public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorRead(gatt, descriptor, status);
                    LogUtils.log(Log.DEBUG, CLASS_TAG, "onDescriptorRead. Status: " + LogUtils.getGattStatusName(status));
                }
            };


    protected synchronized void write(Object o) {
        if ((sWriteQueue.isEmpty()) && (!sIsWriting)) {
            doWrite(o);
        } else {
            sWriteQueue.add(o);
        }
    }

    protected void clearQueue() {
        sWriteQueue.clear();
    }

    private synchronized void nextWrite() {
        if ((!sWriteQueue.isEmpty()) && (!sIsWriting)) {
            doWrite(sWriteQueue.poll());
        }
    }

    private synchronized void doWrite(final Object o) {
        if ((o instanceof BluetoothGattCharacteristic)) {
            sIsWriting = true;
            if (this.mBluetoothGatt != null) {
                this.mBluetoothGatt.writeCharacteristic((BluetoothGattCharacteristic) o);
                LogUtils.log(Log.DEBUG, CLASS_TAG, "writeCharacteristic");
            }
        } else if ((o instanceof BluetoothGattDescriptor)) {

            sIsWriting = true;

            new Handler(mLifevitSDKManager.getmHandlerThread().getLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mBluetoothGatt != null) {
                        mBluetoothGatt.writeDescriptor((BluetoothGattDescriptor) o);
                    }
                }
            }, 500);

        } else {
            nextWrite();
        }
    }

    public void onDescriptorWritten(BluetoothGattDescriptor descriptor){

    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
}