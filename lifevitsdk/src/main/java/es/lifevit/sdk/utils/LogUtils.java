package es.lifevit.sdk.utils;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanSettings;
import android.util.Log;

import es.lifevit.sdk.BuildConfig;
import es.lifevit.sdk.LifevitSDKConstants;

public class LogUtils {


    public static void log(int logLevel, String tag, String message) {
        if (BuildConfig.DEBUG_MESSAGES) {

            if (logLevel < BuildConfig.DEBUG_MESSAGES_LEVEL) {
                return;
            }

            String finalMessage = "[" + tag + "] " + message;
            switch (logLevel) {
                case Log.ERROR:
                    Log.e(LifevitSDKConstants.TAG, finalMessage);
                    break;
                case Log.WARN:
                    Log.w(LifevitSDKConstants.TAG, finalMessage);
                    break;
                case Log.INFO:
                    Log.i(LifevitSDKConstants.TAG, finalMessage);
                    break;
                default:
                    Log.d(LifevitSDKConstants.TAG, finalMessage);
            }
        }
    }


    public static String getDeviceNameByType(int deviceType) {
        switch (deviceType) {
            case LifevitSDKConstants.DEVICE_TENSIOMETER:
                return "TENSIOMETER";
            case LifevitSDKConstants.DEVICE_BRACELET_AT500HR:
                return "BRACELET_AT500HR";
            case LifevitSDKConstants.DEVICE_OXIMETER:
                return "OXIMETER";
            case LifevitSDKConstants.DEVICE_TENSIOBRACELET:
                return "TENSIOBRACELET";
            case LifevitSDKConstants.DEVICE_THERMOMETER:
                return "THERMOMETER";
            case LifevitSDKConstants.DEVICE_WEIGHT_SCALE:
                return "WEIGHT_SCALE";
            case LifevitSDKConstants.DEVICE_BRACELET_AT250:
                return "BRACELET_AT250";
            case LifevitSDKConstants.DEVICE_BABY_TEMP_BT125:
                return "BABY_TEMP_BT125";
            case LifevitSDKConstants.DEVICE_BRACELET_AT250_FIRMWARE_UPDATER:
                return "BRACELET_AT250_FIRMWARE_UPDATER";
            case LifevitSDKConstants.DEVICE_PILL_REMINDER:
                return "PILLREMINDER";
            case LifevitSDKConstants.DEVICE_BRACELET_AT2019:
                return "BRACELET_AT2019";
            case LifevitSDKConstants.DEVICE_GLUCOMETER:
                return "GLUCOMETER";
            case LifevitSDKConstants.DEVICE_OTHERS:
                return "OTHERS";
        }
        return "CODE_" + deviceType + "_NOT_FOUND";
    }


    public static String getConnectionStatusName(int connStatus) {
        switch (connStatus) {
            case LifevitSDKConstants.STATUS_DISCONNECTED:
                return "DISCONNECTED";
            case LifevitSDKConstants.STATUS_SCANNING:
                return "SCANNING";
            case LifevitSDKConstants.STATUS_CONNECTING:
                return "CONNECTING";
            case LifevitSDKConstants.STATUS_CONNECTED:
                return "CONNECTED";
        }
        return "CODE_" + connStatus + "_NOT_FOUND";
    }


    public static String getBluetoothStateName(int connStatus) {
        switch (connStatus) {
            case BluetoothProfile.STATE_DISCONNECTED:
                return "DISCONNECTED";
            case BluetoothProfile.STATE_CONNECTING:
                return "CONNECTING";
            case BluetoothProfile.STATE_CONNECTED:
                return "CONNECTED";
            case BluetoothProfile.STATE_DISCONNECTING:
                return "DISCONNECTING";
        }
        return "CODE_" + connStatus + "_NOT_FOUND";
    }


    public static String getGattStatusName(int gattStatus) {

        switch (gattStatus) {
            case BluetoothGatt.GATT_SUCCESS:
                return "GATT_SUCCESS";
            case BluetoothGatt.GATT_CONNECTION_CONGESTED:
                return "GATT_CONNECTION_CONGESTED";
            case BluetoothGatt.GATT_FAILURE:
                return "GATT_FAILURE";
            case BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION:
                return "GATT_INSUFFICIENT_AUTHENTICATION";
            case BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION:
                return "GATT_INSUFFICIENT_ENCRYPTION";
            case BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH:
                return "GATT_INVALID_ATTRIBUTE_LENGTH";
            case BluetoothGatt.GATT_INVALID_OFFSET:
                return "GATT_INVALID_OFFSET";
            case BluetoothGatt.GATT_READ_NOT_PERMITTED:
                return "GATT_READ_NOT_PERMITTED";
            case BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED:
                return "GATT_REQUEST_NOT_SUPPORTED";
            case BluetoothGatt.GATT_WRITE_NOT_PERMITTED:
                return "GATT_WRITE_NOT_PERMITTED";
        }
        return "CODE_" + gattStatus + "_NOT_FOUND";
    }


    public static String getCallbackTypeName(int callbackType) {
        switch (callbackType) {
            case ScanSettings.CALLBACK_TYPE_ALL_MATCHES:
                return "ALL_MATCHES";
            case ScanSettings.CALLBACK_TYPE_FIRST_MATCH:
                return "FIRST_MATCH";
            case ScanSettings.CALLBACK_TYPE_MATCH_LOST:
                return "MATCH_LOST";
        }
        return "CODE_" + callbackType + "_NOT_FOUND";
    }


}