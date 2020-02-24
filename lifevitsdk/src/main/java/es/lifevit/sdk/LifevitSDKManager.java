package es.lifevit.sdk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import es.lifevit.sdk.bracelet.LifevitSDKAT250TimeRange;
import es.lifevit.sdk.bracelet.LifevitSDKAT500SedentaryReminderTimeRange;
import es.lifevit.sdk.bracelet.LifevitSDKAlarmTime;
import es.lifevit.sdk.bracelet.LifevitSDKAppNotification;
import es.lifevit.sdk.bracelet.LifevitSDKAt500HrAlarmTime;
import es.lifevit.sdk.bracelet.LifevitSDKMonitoringAlarm;
import es.lifevit.sdk.bracelet.LifevitSDKSedentaryAlarm;
import es.lifevit.sdk.bracelet.LifevitSDKTensioBraceletMeasurementInterval;
import es.lifevit.sdk.listeners.LifevitSDKAllDevicesListener;
import es.lifevit.sdk.listeners.LifevitSDKBabyTempBT125Listener;
import es.lifevit.sdk.listeners.LifevitSDKBraceletAT2019Listener;
import es.lifevit.sdk.listeners.LifevitSDKBraceletAT250Listener;
import es.lifevit.sdk.listeners.LifevitSDKBraceletListener;
import es.lifevit.sdk.listeners.LifevitSDKDeviceListener;
import es.lifevit.sdk.listeners.LifevitSDKHeartListener;
import es.lifevit.sdk.listeners.LifevitSDKOximeterListener;
import es.lifevit.sdk.listeners.LifevitSDKPillReminderListener;
import es.lifevit.sdk.listeners.LifevitSDKTensiobraceletListener;
import es.lifevit.sdk.listeners.LifevitSDKThermometerListener;
import es.lifevit.sdk.listeners.LifevitSDKWeightScaleListener;
import es.lifevit.sdk.pillreminder.LifevitSDKPillReminderAlarmData;
import es.lifevit.sdk.utils.BLEAdvertisedData;
import es.lifevit.sdk.utils.BLEUtil;
import es.lifevit.sdk.utils.LogUtils;


/**
 * Created by aescanuela on 26/1/16.
 */
public class LifevitSDKManager {

    private boolean mScanning = false;

    private class LifevitScanResult {
        private BluetoothDevice device;
        private int rssi;
        private String name;
        private String advertisedName;

        public LifevitScanResult(String name, String advertisedName, int rssi, BluetoothDevice device) {
            this.rssi = rssi;
            this.device = device;
            this.name = name;
            this.advertisedName = advertisedName;
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getRssi() {
            return rssi;
        }

        public void setRssi(int rssi) {
            this.rssi = rssi;
        }

        public BluetoothDevice getDevice() {
            return device;
        }

        public void setDevice(BluetoothDevice device) {
            this.device = device;
        }

        public String getAdvertisedName() {
            return advertisedName;
        }

        public void setAdvertisedName(String advertisedName) {
            this.advertisedName = advertisedName;
        }
    }


    private boolean scan_batch_results = false;

    private final static String CLASS_TAG = LifevitSDKManager.class.getSimpleName();
    private static final long DETECTION_TIME_MILLIS = 4000;

    private ScanCallback mNewScanCallback;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLEScanner;

    private Context mContext;

    private static Object lock = new Object();

    private ArrayList<LifevitSDKDeviceListener> deviceListeners = new ArrayList<>();
    private LifevitSDKAllDevicesListener allDevicesListener;

    private LifevitSDKHeartListener heartListener;
    private LifevitSDKBraceletListener braceletListener;
    private LifevitSDKBraceletAT250Listener braceletAT250Listener;
    private LifevitSDKOximeterListener oximeterListener;
    private LifevitSDKTensiobraceletListener tensiobraceletListener;
    private LifevitSDKThermometerListener thermometerListener;
    private LifevitSDKWeightScaleListener weightScaleListener;
    private LifevitSDKBabyTempBT125Listener babyTempBT125Listener;
    private LifevitSDKPillReminderListener pillReminderListener;
    private LifevitSDKBraceletAT2019Listener braceletAT2019Listener;


    // Dispositivos conectados
    private ConcurrentHashMap<Integer, LifevitSDKBleDevice> hshDeviceByType = new ConcurrentHashMap<>();

    // Dispositivos a escanear (4 hashes)
    private ConcurrentHashMap<Integer, String> hshDeviceAddressByType = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Date> hshConnectionTimeout = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, HashMap<String, LifevitScanResult>> hshDetectedDevices = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, String> hshConnectingDevices = new ConcurrentHashMap<>();

    // Todos los dispositivos encontrados
    private ConcurrentHashMap<Integer, List<LifevitSDKDeviceScanData>> hshAllDevicesFound = new ConcurrentHashMap<>();

    private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", -1);
            if (type != -1) {
                sendBraceletNotification(type);
            }
        }
    };

    private boolean mNotificationListenerStarted = false;

    private HandlerThread mHandlerThread = null;
    private HandlerThread mHandlerScansThread = null;
    private HandlerThread mHandlerConnectThread = null;
    private Timer deviceDetectionTimer;


    public LifevitSDKManager(Context context) {

        LogUtils.log(Log.DEBUG, CLASS_TAG, "Create manager with new creator.");

        context.registerReceiver(notificationReceiver, new IntentFilter(NotificationReceiverService.BROADCAST_NOTIFICATION));

        mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();


        mHandlerScansThread = new HandlerThread("HandlerScansThread");
        mHandlerScansThread.start();
        mHandlerConnectThread = new HandlerThread("mHandlerConnectThread");
        mHandlerConnectThread.start();

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            scan_batch_results = false;
        } else {
            //scan_batch_results = mBluetoothAdapter.isOffloadedScanBatchingSupported();
        }
        // Start BLE Scanner
        initializeBleScanner();

        initCallbacks();

        mContext = context;

        // No conectar automaticamente pulsera
        // checkBracelet();
    }


    // -------------------------- Listeners ----------------------- //


    public ArrayList<LifevitSDKDeviceListener> getDeviceListeners() {
        return deviceListeners;
    }

    public void addDeviceListener(LifevitSDKDeviceListener deviceListener) {
        this.deviceListeners.add(deviceListener);
    }

    public void removeDeviceListener(LifevitSDKDeviceListener deviceListener) {
        this.deviceListeners.remove(deviceListener);
    }

    public LifevitSDKAllDevicesListener getAllDevicesListener() {
        return allDevicesListener;
    }

    public void setAllDevicesListener(LifevitSDKAllDevicesListener allDevicesListener) {
        this.allDevicesListener = allDevicesListener;
    }

    public LifevitSDKHeartListener getHeartListener() {
        return heartListener;
    }

    public void setHeartListener(LifevitSDKHeartListener heartListener) {
        this.heartListener = heartListener;
    }

    public LifevitSDKBraceletListener getBraceletListener() {
        return braceletListener;
    }

    public void setBraceletListener(LifevitSDKBraceletListener braceletListener) {
        this.braceletListener = braceletListener;
    }

    public LifevitSDKBraceletAT250Listener getBraceletAT250Listener() {
        return braceletAT250Listener;
    }

    public void setBraceletAT250Listener(LifevitSDKBraceletAT250Listener braceletListener) {
        this.braceletAT250Listener = braceletListener;
    }

    public LifevitSDKOximeterListener getOximeterListener() {
        return oximeterListener;
    }

    public void setOximeterListener(LifevitSDKOximeterListener oximeterListener) {
        this.oximeterListener = oximeterListener;
    }

    public LifevitSDKTensiobraceletListener getTensiobraceletListener() {
        return tensiobraceletListener;
    }

    public void setTensiobraceletListener(LifevitSDKTensiobraceletListener tensiobraceletListener) {
        this.tensiobraceletListener = tensiobraceletListener;
    }

    public LifevitSDKThermometerListener getThermometerListener() {
        return thermometerListener;
    }

    public void setThermometerListener(LifevitSDKThermometerListener thermometerListener) {
        this.thermometerListener = thermometerListener;
    }

    public LifevitSDKWeightScaleListener getWeightScaleListener() {
        return weightScaleListener;
    }

    public void setWeightScaleListener(LifevitSDKWeightScaleListener weightScaleListener) {
        this.weightScaleListener = weightScaleListener;
    }

    public LifevitSDKBabyTempBT125Listener getBabyTempBT125Listener() {
        return babyTempBT125Listener;
    }

    public void setBabyTempBT125Listener(LifevitSDKBabyTempBT125Listener babyTempBT125Listener) {
        this.babyTempBT125Listener = babyTempBT125Listener;
    }

    public LifevitSDKPillReminderListener getPillReminderListener() {
        return pillReminderListener;
    }

    public void setPillReminderListener(LifevitSDKPillReminderListener pillReminderListener) {
        this.pillReminderListener = pillReminderListener;
    }

    public LifevitSDKBraceletAT2019Listener getBraceletAT2019Listener() {
        return braceletAT2019Listener;
    }

    public void setBraceletAT2019Listener(LifevitSDKBraceletAT2019Listener braceletAT2019Listener) {
        this.braceletAT2019Listener = braceletAT2019Listener;
    }

    protected HandlerThread getmHandlerThread() {
        return mHandlerThread;
    }


// -------------------------- Public Methods ----------------------- //


    public void connectDevice(int deviceType, long scanPeriod) {
//        LogUtils.log(Log.DEBUG, CLASS_TAG, "connectDevice. deviceType = " + LogUtils.getDeviceNameByType(deviceType) + ", scanPeriod: " + scanPeriod);
        connectDevice(deviceType, scanPeriod, null);
    }


    public void connectDevice(final int deviceType, final long scanPeriod, final String address) {

        new Handler(mHandlerConnectThread.getLooper()).post(new Runnable() {
            @Override
            public void run() {

                synchronized (lock) {

                    LogUtils.log(Log.INFO, CLASS_TAG, "[connectDevice] deviceType = " + LogUtils.getDeviceNameByType(deviceType)
                            + ", scanPeriod = " + scanPeriod + ", address: " + address);

                    boolean canStartScan = checkIfCanStartScan(deviceType);
                    if (!canStartScan) {
                        return;
                    }

                    if (isDeviceConnected(deviceType)) {
                        // Device is already connected
                        LogUtils.log(Log.ERROR, CLASS_TAG, "[connectDevice] Device is already connected.");
                        return;
                    }

                    if (hshDetectedDevices.containsKey(deviceType)) {
                        // Device is already connected
                        LogUtils.log(Log.ERROR, CLASS_TAG, "[devicesToScan] Device is already added to scan. Current list: " + printDevicesToScan());

                        deviceOnConnectionChanged(deviceType, LifevitSDKConstants.STATUS_SCANNING, false);
                        return;
                    } else {
                        hshDetectedDevices.put(deviceType, new HashMap<String, LifevitScanResult>());
                        LogUtils.log(Log.INFO, CLASS_TAG, "[devicesToScan] ADD device: " + LogUtils.getDeviceNameByType(deviceType) + ", current list: " + printDevicesToScan());
                    }


                    if (address != null) {
                        hshDeviceAddressByType.put(deviceType, address);
                        LogUtils.log(Log.DEBUG, CLASS_TAG, "mAddressToMatch: " + address);
                    } else {
                        hshDeviceAddressByType.remove(deviceType);
                    }

                    //Ponemos el timeout...
                    hshConnectionTimeout.put(deviceType, new Date(System.currentTimeMillis() + scanPeriod));

                    if (mScanning == false) {
                        // Quiere decir que hemos añadido el primer dispositivo
                        // Reiniciamos el escaneo
                        stopLeScan();
                        mScanning = true;
                        startLeScan(null, false, true);

                        if (!scan_batch_results) {
                            // Añadimos el detectionTimer
                            TimerTask detectionTask = new TimerTask() {
                                @Override
                                public void run() {
                                    checkDetectedDevices();

                                }
                            };
                            deviceDetectionTimer = new Timer();
                            deviceDetectionTimer.schedule(detectionTask, DETECTION_TIME_MILLIS, DETECTION_TIME_MILLIS);
                        }
                    }

                    deviceOnConnectionChanged(deviceType, LifevitSDKConstants.STATUS_SCANNING, false);
                }
            }
        });

        // Run thread
        //thread.start();
    }

    private void checkDetectedDevices() {
        try {

            LogUtils.log(Log.INFO, CLASS_TAG, "[Checking Found devices ScanningQueue]: " + printDevicesToScan());
            LogUtils.log(Log.INFO, CLASS_TAG, "[Checking Found devices ConnectingDevices]: " + printScanningQueue());
            ArrayList<Integer> types = new ArrayList<>();
            types.addAll(hshDetectedDevices.keySet());
            for (Integer type : types) {
                Collection<LifevitScanResult> detectedDevices = hshDetectedDevices.get(type).values();
                String uuid = hshDeviceAddressByType.get(type);

                LifevitScanResult bestResult = null;
                for (LifevitScanResult result : detectedDevices) {
                    if (uuid != null && uuid.length() > 0) {
                        //Conectamos solo a UUID
                        if (result.getDevice().getAddress().equalsIgnoreCase(uuid)) {
                            bestResult = result;
                            break;
                        }
                    } else {
                        if (bestResult == null || bestResult.getRssi() < result.getRssi()) {
                            bestResult = result;
                        }
                    }

                }

                if (bestResult != null) {
                    // DE momento no pasamos el tipo

                    synchronized (lock) {
                        LogUtils.log(Log.DEBUG, CLASS_TAG, "[CHECK ScanningQueue]" + printScanningQueue());
                        if (hshConnectingDevices.get(type) == null) {
                            boolean connecting = connect(bestResult.device);
                            if (connecting) {
                                hshConnectingDevices.put(type, bestResult.device.getAddress());

                                LogUtils.log(Log.DEBUG, CLASS_TAG, "[ADD ScanningQueue]" + printScanningQueue());
                            }
                        }
                    }

                } else {
                    Date timeout = hshConnectionTimeout.get(type);
                    if (timeout != null && timeout.before(new Date())) {
                        // synchronized (lock) {
                        LogUtils.log(Log.INFO, LifevitSDKConstants.TAG, "Disconnecting device by timeout : " + LogUtils.getDeviceNameByType(type));
                        disconnectDevice(type);
                        hshConnectionTimeout.remove(type);
                        hshDetectedDevices.remove(type);
                        hshDeviceAddressByType.remove(type);

                        if (hshDetectedDevices.size() == 0) {
                            stopLeScan();
                            // }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (allDevicesListener != null && !hshAllDevicesFound.isEmpty()) {
            allDevicesListener.allDevicesDetected(hshAllDevicesFound);
        }
    }


    private boolean checkIfCanStartScan(int deviceType) {

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Return NO permissions
            if (deviceListeners.size() > 0) {
                ArrayList<LifevitSDKDeviceListener> listeners = new ArrayList<>(deviceListeners);
                for (LifevitSDKDeviceListener deviceListener : listeners) {
                    deviceListener.deviceOnConnectionError(deviceType, LifevitSDKConstants.CODE_LOCATION_DISABLED);
                }
            }

            // Do not scan...
            LogUtils.log(Log.ERROR, CLASS_TAG, "(deviceOnConnectionError), deviceToConnect: " + LogUtils.getDeviceNameByType(deviceType));

            return false;
        }

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {

            if (deviceListeners.size() > 0) {
                ArrayList<LifevitSDKDeviceListener> listeners = new ArrayList<>(deviceListeners);
                for (LifevitSDKDeviceListener deviceListener : listeners) {
                    deviceListener.deviceOnConnectionError(deviceType, LifevitSDKConstants.CODE_BLUETOOTH_DISABLED);
                }
            }

            LogUtils.log(Log.ERROR, CLASS_TAG, "(deviceOnConnectionError), deviceToConnect: " + LogUtils.getDeviceNameByType(deviceType));

            return false;
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && LifevitSDKConstants.CHECK_GPS) {

            LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            boolean gps_enabled = false;
            boolean network_enabled = false;

            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (!gps_enabled && !network_enabled) {
                // notify user
                if (deviceListeners.size() > 0) {
                    ArrayList<LifevitSDKDeviceListener> listeners = new ArrayList<>(deviceListeners);
                    for (LifevitSDKDeviceListener deviceListener : listeners) {
                        deviceListener.deviceOnConnectionError(deviceType, LifevitSDKConstants.CODE_LOCATION_TURN_OFF);
                    }
                }

                LogUtils.log(Log.ERROR, CLASS_TAG, "(deviceOnConnectionError), deviceToConnect: " + LogUtils.getDeviceNameByType(deviceType));
                return false;
            }
        }

        return true;
    }


    public void disconnectDevice(int deviceType) {

        LogUtils.log(Log.INFO, CLASS_TAG, "[disconnectDevice] deviceType = " + LogUtils.getDeviceNameByType(deviceType));

        LifevitSDKBleDevice device = getDeviceByType(deviceType);
        if (device != null) {
            // If device connected, disconnect
            device.disconnectGatt();
            deviceOnConnectionChanged(deviceType, LifevitSDKConstants.STATUS_DISCONNECTED, false);
            // If it is a bracelet, forget address
//            if (deviceType == LifevitSDKConstants.DEVICE_BRACELET_AT500HR || deviceType == LifevitSDKConstants.DEVICE_BRACELET_AT250) {
//                PreferenceUtil.setBraceletAddress(mContext, "");
//            }

            hshDeviceByType.remove(deviceType);

        } else {
            // If not connected, nor scanning, tell user that device was already disconnected
            deviceOnConnectionChanged(deviceType, LifevitSDKConstants.STATUS_DISCONNECTED, false);
        }
    }


    private LifevitSDKBleDevice getDeviceByType(int deviceType) {
        return hshDeviceByType.get(deviceType);
    }


    private UUID[] getDeviceUUIDs(int deviceType) {
        switch (deviceType) {
            case LifevitSDKConstants.DEVICE_TENSIOMETER:
                // FIXME: What about LifevitSDKBleDeviceTensiometer ?
                return LifevitSDKBleDeviceTensiometerV2.getUUIDs();
            case LifevitSDKConstants.DEVICE_BRACELET_AT500HR:
                return LifevitSDKBleDeviceBraceletAT500HR.getUUIDs();
            case LifevitSDKConstants.DEVICE_BRACELET_AT250:
                return LifevitSDKBleDeviceBraceletAT250.getUUIDs();
            case LifevitSDKConstants.DEVICE_OXIMETER:
                return LifevitSDKBleDeviceOximeter.getUUIDs();
            case LifevitSDKConstants.DEVICE_TENSIOBRACELET:
                return LifevitSDKBleDeviceTensiobracelet.getUUIDs();
            case LifevitSDKConstants.DEVICE_THERMOMETER:
                return LifevitSDKBleDeviceThermometer.getUUIDs();
            case LifevitSDKConstants.DEVICE_WEIGHT_SCALE:
                return LifevitSDKBleDeviceWeightScale.getUUIDs();
            case LifevitSDKConstants.DEVICE_BABY_TEMP_BT125:
                return LifevitSDKBleDeviceBabyTempBT125.getUUIDs();
            case LifevitSDKConstants.DEVICE_BRACELET_AT250_FIRMWARE_UPDATER:
                return LifevitSDKBleDeviceBraceletAT250FirmwareUpdater.getUUIDs();
            case LifevitSDKConstants.DEVICE_BRACELET_AT2019:
                return LifevitSDKBleDeviceBraceletAT2019.getUUIDs();
        }
        return null;
    }


    public boolean isDeviceConnected(int deviceType) {
        LifevitSDKBleDevice device = getDeviceByType(deviceType);
        boolean result = device != null && device.getDeviceStatus() == LifevitSDKConstants.STATUS_CONNECTED;
        LogUtils.log(Log.DEBUG, CLASS_TAG, "[isDeviceConnected] deviceType = " + LogUtils.getDeviceNameByType(deviceType) + ", device object: " + device + " ==> is connected? " + result);
        return result;
    }

    public boolean isDeviceConnecting(int deviceType) {
        LifevitSDKBleDevice device = getDeviceByType(deviceType);
        boolean result = device != null && device.getDeviceStatus() == LifevitSDKConstants.STATUS_CONNECTING;
        LogUtils.log(Log.DEBUG, CLASS_TAG, "[isDeviceConnecting] deviceType = " + LogUtils.getDeviceNameByType(deviceType) + ", device object: " + device + " ==> is connected? " + result);
        return result;
    }


    public String getDeviceAddress(int deviceType) {
        String result = "";
        LifevitSDKBleDevice device = getDeviceByType(deviceType);
        if (device != null) {
            result = device.mBluetoothDevice.getAddress();
        }
        LogUtils.log(Log.DEBUG, CLASS_TAG, "getDeviceAddress. deviceType = " + LogUtils.getDeviceNameByType(deviceType) + ", result: " + result);
        return result;
    }


    // -------------------------- Initialization Methods ----------------------- //


    private void initializeBleScanner() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mBluetoothAdapter != null) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            }
        }
    }


    @SuppressLint("NewApi")
    private void initCallbacks() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mNewScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(final int callbackType, final ScanResult result) {
                    new Handler(mHandlerScansThread.getLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (hshDetectedDevices.size() == 0) {
                                stopLeScan();
                                cleanConnectingData();
                                return;
                            }
                            BluetoothDevice btDevice = result.getDevice();

                            if (btDevice == null) {
                                LogUtils.log(Log.ERROR, CLASS_TAG, "btDevice is NULL!!");
                                return;
                            }
                            LogUtils.log(Log.DEBUG, CLASS_TAG, "[New scan] onScanResult: " + LogUtils.getCallbackTypeName(callbackType) + ", RSSI: " + result.getRssi() + " , NAME: " + btDevice.getName());


                            String deviceName = result.getDevice().getName();
                            String bAdData = "";
                            final BLEAdvertisedData badata = BLEUtil.parseAdertisedData(result.getScanRecord().getBytes());
                            if (badata != null) {
                                bAdData = badata.getName();
                            }
                            if (deviceName == null) {
                                deviceName = badata.getName();
                            }

                            if (deviceName != null) {

                                addToDetectedDevice(new LifevitScanResult(deviceName, bAdData, result.getRssi(), result.getDevice()));
                            }
                        }
                    });

                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    LogUtils.log(Log.DEBUG, CLASS_TAG, "onBatchScanResults - Results: " + results.size());
                    for (ScanResult scanResult : results) {
                        LogUtils.log(Log.DEBUG, CLASS_TAG, "     * Address:" + scanResult.getDevice().getAddress() + ", name: " + scanResult.getDevice().getName());
                        onScanResult(0, scanResult);
                    }

                    checkDetectedDevices();
                }

                @Override
                public void onScanFailed(int errorCode) {
                    LogUtils.log(Log.ERROR, CLASS_TAG, "onScanFailed - Error Code: " + errorCode);
                }
            };
        }
    }

    private void addToDetectedDevice(final LifevitScanResult result) {

        String dName = result.getName();

        if (dName == null) {
            LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> Scanning result --> Name NOT RETURNED, ADDR: " + result.getDevice().getAddress());
            return;
        }

        LogUtils.log(Log.DEBUG, CLASS_TAG, ">>> Scanning result --> Name: " + dName + ", ADDR: " + result.getDevice().getAddress());

        int deviceType = LifevitSDKConstants.DEVICE_OTHERS;
        if (dName != null) {
            if (LifevitSDKBleDeviceTensiometer.isTensiometerDevice(dName)
                    || LifevitSDKBleDeviceTensiometerV2.isNewTensiometerDevice(dName) || LifevitSDKBleDeviceTensiometerV3.isNewTensiometerDevice(dName)) {
                deviceType = LifevitSDKConstants.DEVICE_TENSIOMETER;
            } else if (LifevitSDKBleDeviceBraceletAT500HR.isAt500HrBraceletDevice(dName)) {
                deviceType = LifevitSDKConstants.DEVICE_BRACELET_AT500HR;
            } else if (LifevitSDKBleDeviceBraceletAT250.isBraceletAT250Device(dName)) {
                deviceType = LifevitSDKConstants.DEVICE_BRACELET_AT250;
            } else if (LifevitSDKBleDeviceOximeter.isOximeterDevice(dName)) {
                deviceType = LifevitSDKConstants.DEVICE_OXIMETER;
            } else if (LifevitSDKBleDeviceTensiobracelet.isTensioBraceletDevice(dName)) {
                deviceType = LifevitSDKConstants.DEVICE_TENSIOBRACELET;
            } else if (LifevitSDKBleDeviceThermometer.isThermometerDevice(dName)) {
                deviceType = LifevitSDKConstants.DEVICE_THERMOMETER;
            } else if (LifevitSDKBleDeviceThermometerV2.isThermometerDevice(dName)) {
                deviceType = LifevitSDKConstants.DEVICE_THERMOMETER;
            } else if (LifevitSDKBleDeviceWeightScale.isWeightScaleDevice(dName)) {
                deviceType = LifevitSDKConstants.DEVICE_WEIGHT_SCALE;
            } else if (LifevitSDKBleDeviceBabyTempBT125.isBabyTempDevice(dName)) {
                deviceType = LifevitSDKConstants.DEVICE_BABY_TEMP_BT125;
            } else if (LifevitSDKBleDeviceBraceletAT250FirmwareUpdater.isBraceletAT250FirmwareUpdaterDevice(dName)) {
                deviceType = LifevitSDKConstants.DEVICE_BRACELET_AT250_FIRMWARE_UPDATER;
            } else if (LifevitSDKBleDevicePillReminder.matchDeviceName(dName) || LifevitSDKBleDevicePillReminder.matchDeviceName(result.getAdvertisedName())) {
                deviceType = LifevitSDKConstants.DEVICE_PILL_REMINDER;
            } else if (LifevitSDKBleDeviceBraceletAT2019.isAt2019BraceletDevice(dName)) {
                deviceType = LifevitSDKConstants.DEVICE_BRACELET_AT2019;
            }

            if (deviceType != LifevitSDKConstants.DEVICE_OTHERS) {
                synchronized (lock) {
                    HashMap<String, LifevitScanResult> detectedDevices = hshDetectedDevices.get(deviceType);
                    if (detectedDevices != null) {
                        //Miramos si se conecta por uuuid... y si es ese uuid...
                        String uuid = hshDeviceAddressByType.get(deviceType);
                        if (uuid != null && uuid.equalsIgnoreCase(result.getDevice().getAddress())) {
                            //connect(result.getDevice());
                            if (hshConnectingDevices.get(deviceType) == null) {
                                boolean connecting = connect(result.getDevice());
                                if (connecting) {
                                    hshConnectingDevices.put(deviceType, result.getDevice().getAddress());
                                }
                            }
                        }
                        //Por si acaso lo añadimos igualmente
                        detectedDevices.put(result.getDevice().getAddress(), result);
                    }
                }
            }
        }

        if (!hshAllDevicesFound.containsKey(deviceType)) {
            hshAllDevicesFound.put(deviceType, new ArrayList<LifevitSDKDeviceScanData>());
        }
        List<LifevitSDKDeviceScanData> detectedDevices = hshAllDevicesFound.get(deviceType);
        boolean alreadyAdded = false;
        for (LifevitSDKDeviceScanData detectedDevice : detectedDevices) {
            if (detectedDevice.getAddress().equals(result.getDevice().getAddress())) {
                alreadyAdded = true;
                break;
            }
        }
        if (!alreadyAdded) {
            LifevitSDKDeviceScanData deviceScanData = new LifevitSDKDeviceScanData();
            deviceScanData.setAddress(result.getDevice().getAddress());
            deviceScanData.setRssi(result.getRssi());
            detectedDevices.add(deviceScanData);
        }
    }


    private BluetoothAdapter.LeScanCallback mOldScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {

            if (hshDetectedDevices.size() == 0) {
                stopLeScan();
                cleanConnectingData();
                return;
            }
            LogUtils.log(Log.DEBUG, CLASS_TAG, "[Old scan] RSSI: " + rssi + ", deviceName: " + device.getName());

            // We are going to wait some time to find the nearest device
            final BLEAdvertisedData badata = BLEUtil.parseAdertisedData(scanRecord);
            String deviceName = device.getName();
            String bAdName = badata.getName();
            if (deviceName == null) {
                deviceName = bAdName;
            }
            LifevitScanResult scanResult = new LifevitScanResult(deviceName, bAdName, rssi, device);

            LogUtils.log(Log.DEBUG, CLASS_TAG, "[RSSI] Current result RSSI: " + scanResult.getRssi() + " from " + scanResult.getDevice().getAddress());

            addToDetectedDevice(scanResult);

        }
    };


    // -------------------------- BLE Scan Methods ----------------------- //


    private void stopLeScan() {

        try {
            synchronized (lock) {
                mScanning = false;
                LogUtils.log(Log.INFO, CLASS_TAG, "[stopLeScan]");

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    if (mBluetoothAdapter != null) {
                        mBluetoothAdapter.stopLeScan(mOldScanCallback);
                    }
                } else {
                    if (mLEScanner != null) {
                        mLEScanner.stopScan(mNewScanCallback);
                    }
                }

                hshAllDevicesFound.clear();

                if (deviceDetectionTimer != null) {
                    deviceDetectionTimer.cancel();
                    deviceDetectionTimer = null;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanConnectingData() {

        hshDetectedDevices.clear();
        hshConnectionTimeout.clear();
        hshDeviceAddressByType.clear();
        hshConnectingDevices.clear();

        hshAllDevicesFound.clear();

        LogUtils.log(Log.DEBUG, CLASS_TAG, "[CLEAR ScanningQueue]" + printScanningQueue());

    }


    private void startLeScan(final UUID[] serviceUuids, final boolean filterByUUIDs, boolean userStarted) {

        LogUtils.log(Log.DEBUG, CLASS_TAG, "------------------------------------------------------------------------ startLeScan");
        LogUtils.log(Log.DEBUG, CLASS_TAG, "startLeScan. serviceUuids = " + serviceUuids + ", filterByUUIDs: " + filterByUUIDs + ", userStarted: " + userStarted);
        LogUtils.log(Log.INFO, CLASS_TAG, "[connection] startLeScan");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            LogUtils.log(Log.DEBUG, CLASS_TAG, "old scan");

            mBluetoothAdapter.startLeScan(mOldScanCallback);

        } else {

            ScanSettings.Builder builder = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                builder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
                builder.setMatchMode(ScanSettings.MATCH_MODE_STICKY);
            }

            if (scan_batch_results) {
                builder.setReportDelay(DETECTION_TIME_MILLIS);
            }
            ScanSettings settings = builder.build();

            List<ScanFilter> filters = new ArrayList<>();
            if (serviceUuids != null && serviceUuids.length > 0 && filterByUUIDs) {
                // Note scan filter does not support matching an UUID array so we put one
                // UUID to hardware and match the whole array in callback.
                ScanFilter filter = new ScanFilter.Builder().setServiceUuid(
                        new ParcelUuid(serviceUuids[0])).build();
                filters.add(filter);
            }

            if (mLEScanner != null && mNewScanCallback != null) {

                //if (filters.size() > 0) {
                mLEScanner.startScan(filters, settings, mNewScanCallback);
                /*} else {
                    mLEScanner.startScan(mNewScanCallback);
                }*/
            } else {

                LogUtils.log(Log.ERROR, CLASS_TAG, "Error in startLeScan: mLEScanner is null.");

                initializeBleScanner();
            }
        }
    }


    // -------------------------- Device Connection Methods ----------------------- //


    private boolean connect(BluetoothDevice device) {

        LogUtils.log(Log.INFO, CLASS_TAG, "[connect] device = " + device);

        if (device != null) {

            LifevitSDKBleDevice bleDevice = null;

            if (LifevitSDKBleDeviceTensiometer.isTensiometerDevice(device.getName())) {

                bleDevice = new LifevitSDKBleDeviceTensiometer(device, this);

            } else if (LifevitSDKBleDeviceTensiometerV2.isNewTensiometerDevice(device.getName()) /*&& !mIsConnecting*/) {

                bleDevice = new LifevitSDKBleDeviceTensiometerV2(device, this);

            } else if (LifevitSDKBleDeviceTensiometerV3.isNewTensiometerDevice(device.getName()) /*&& !mIsConnecting*/) {

                bleDevice = new LifevitSDKBleDeviceTensiometerV3(device, this);

            } else if (LifevitSDKBleDeviceBraceletAT500HR.matchDevice(device)) {

                bleDevice = new LifevitSDKBleDeviceBraceletAT500HR(device, this);

            } else if (LifevitSDKBleDeviceBraceletAT250.matchDevice(device)) {

                bleDevice = new LifevitSDKBleDeviceBraceletAT250(device, this);

            } else if (LifevitSDKBleDeviceOximeter.isOximeterDevice(device.getName())) {

                bleDevice = new LifevitSDKBleDeviceOximeter(device, this);

            } else if (LifevitSDKBleDeviceTensiobracelet.matchDevice(device)) {

                bleDevice = new LifevitSDKBleDeviceTensiobracelet(device, this);

            } else if (LifevitSDKBleDeviceThermometer.isThermometerDevice(device.getName())) {

                bleDevice = new LifevitSDKBleDeviceThermometer(device, this);

            } else if (LifevitSDKBleDeviceThermometerV2.isThermometerDevice(device.getName())) {

                bleDevice = new LifevitSDKBleDeviceThermometerV2(device, this);

            } else if (LifevitSDKBleDeviceWeightScale.isWeightScaleDevice(device.getName())) {

                bleDevice = new LifevitSDKBleDeviceWeightScale(device, this);

            } else if (LifevitSDKBleDeviceBabyTempBT125.isBabyTempDevice(device.getName())) {

                bleDevice = new LifevitSDKBleDeviceBabyTempBT125(device, this);

            } else if (LifevitSDKBleDeviceBraceletAT250FirmwareUpdater.matchDevice(device)) {

                bleDevice = new LifevitSDKBleDeviceBraceletAT250FirmwareUpdater(device, this);

            } else if (LifevitSDKBleDevicePillReminder.matchDevice(device)) {

                bleDevice = new LifevitSDKBleDevicePillReminder(device, this);

            } else if (LifevitSDKBleDeviceBraceletAT2019.matchDevice(device)) {

                bleDevice = new LifevitSDKBleDeviceBraceletAT2019(device, this);
            }

            if (bleDevice != null) {
                // Connect to device
                LogUtils.log(Log.DEBUG, CLASS_TAG, "----- mIsConnecting = true");
                bleDevice.connectGatt(mContext, true);
                return true;
            }
        }
        return false;
    }


    void setConnectedDevice(LifevitSDKBleDevice device) {

        LogUtils.log(Log.DEBUG, CLASS_TAG, "setConnectedDevice. device = " + device);
        hshDeviceByType.put(device.getType(), device);
    }


    protected void setDeviceDisconnected(LifevitSDKBleDevice device) {

        LogUtils.log(Log.DEBUG, CLASS_TAG, "setDeviceDisconnected. device = " + device);

        hshDeviceByType.remove(device.getType());

        deviceOnConnectionChanged(device.getType(), LifevitSDKConstants.STATUS_DISCONNECTED, false);
    }


    public void deviceOnConnectionChanged(int deviceType, int deviceStatus, boolean calledAutomatically) {

        LogUtils.log(Log.INFO, CLASS_TAG, "deviceOnConnectionChanged. deviceType: " + LogUtils.getDeviceNameByType(deviceType)
                + ", status: " + LogUtils.getConnectionStatusName(deviceStatus)
                + ", deviceToConnect: " + LogUtils.getDeviceNameByType(deviceType)
                + ", calledAutomatically: " + calledAutomatically);

        if (getDeviceListeners().size() > 0L && deviceType != LifevitSDKConstants.DEVICE_BRACELET_AT250_FIRMWARE_UPDATER) {
            ArrayList<LifevitSDKDeviceListener> listeners = new ArrayList<>(deviceListeners);
            for (LifevitSDKDeviceListener listener : listeners) {
                listener.deviceOnConnectionChanged(deviceType, deviceStatus);
            }
        }

        if (deviceStatus == LifevitSDKConstants.STATUS_CONNECTED) {

            LogUtils.log(Log.DEBUG, CLASS_TAG, "(device is connected!), deviceToConnect: " + LogUtils.getDeviceNameByType(deviceType));

            //Lo quitamos de la cola de conexiones
            hshDetectedDevices.remove(deviceType);
            hshDeviceAddressByType.remove(deviceType);
            hshConnectionTimeout.remove(deviceType);
            hshConnectingDevices.remove(deviceType);

            //LogUtils.log(Log.DEBUG, CLASS_TAG, "[REMOVE ScanningQueue]" + printScanningQueue());

            if (hshDetectedDevices.size() == 0) {
                stopLeScan();
            }

        } else if (isConnectingDevice(deviceType) && deviceStatus == LifevitSDKConstants.STATUS_DISCONNECTED) {

            LogUtils.log(Log.ERROR, CLASS_TAG, "(DISCONNECTED DURING CONNECTION!), deviceToConnect: " + LogUtils.getDeviceNameByType(deviceType));

            //Indicamos que ya no se está conectando...
            hshConnectingDevices.remove(deviceType);
            //LogUtils.log(Log.DEBUG, CLASS_TAG, "[REMOVE2 ScanningQueue]" + printScanningQueue());
        } else if (deviceStatus == LifevitSDKConstants.STATUS_DISCONNECTED) {

            LogUtils.log(Log.ERROR, CLASS_TAG, "(DISCONNECTED), deviceToConnect: " + LogUtils.getDeviceNameByType(deviceType));

            //Indicamos que ya no se está conectando...
            hshConnectingDevices.remove(deviceType);
            //LogUtils.log(Log.DEBUG, CLASS_TAG, "[REMOVE3 ScanningQueue]" + printScanningQueue());
        }
    }


    private boolean isConnectingDevice(int deviceType) {

        LifevitSDKBleDevice device = hshDeviceByType.get(deviceType);
        if (device != null) {
            return device.mDeviceStatus == LifevitSDKConstants.STATUS_CONNECTING;
        }
        return false;
    }


    private String printDevicesToScan() {
        String result = "[";
        try {
            ArrayList<Integer> types = new ArrayList<>(hshDetectedDevices.keySet());
            for (Integer type : types) {
                if (!result.equalsIgnoreCase("[")) {
                    result += ",";
                }
                result += LogUtils.getDeviceNameByType(type);
            }
        } catch (Exception e) {
            return "Error printing devices";
        }
        result += "]";
        return result;
    }


    private String printScanningQueue() {
        String message = "";
        int i = 0;
        try {
            ArrayList<Map.Entry<Integer, String>> connectingDevices = new ArrayList<>(hshConnectingDevices.entrySet());
            for (Map.Entry<Integer, String> mapEntry : connectingDevices) {
                message += "Device " + mapEntry.getKey() + " with address " + mapEntry.getValue();
                if (i != hshConnectingDevices.entrySet().size() - 1) {
                    message += ",";
                }
                i++;
            }
            if (i == 0) {
                message += "EMPTY";
            }
        } catch (Exception e) {

        }
        return message;
    }


    // region --- Tensiometer Methods ---


    public void startMeasurement() {
        LifevitSDKBleDevice heartDevice = getDeviceByType(LifevitSDKConstants.DEVICE_TENSIOMETER);
        if (heartDevice == null) {
            // TODO; return not connected device
        } else {
            heartDevice.startReceiver(null, null);
        }
    }


    // endregion


    // region --- Thermometer Methods ---


    public void sendThermometerCommand(int command) {
        LifevitSDKBleDevice device = getDeviceByType(LifevitSDKConstants.DEVICE_THERMOMETER);
        if (device != null && device instanceof LifevitSDKBleDeviceThermometerV2) {
            ((LifevitSDKBleDeviceThermometerV2) device).sendCommand(command);
        }
    }


    // endregion

    // region --- Bracelet Methods ---


    public boolean isUserRunning() {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            return bracelet500HR.isRunning();
        }
        return false;
    }


    public void setBraceletActivity(boolean enabled) {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            if (!enabled) {
                bracelet500HR.endExercise();
            } else {
                bracelet500HR.startExercise();
            }
        }
    }

    public void getBraceletVersion() {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.getVersion();
        }
    }

    public void getBraceletBattery() {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.getBattery();
        }
    }

    public void setBraceletUserHeight(int height) {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.updateUserHeight(height);
        }
    }

    public void setBraceletUserWeight(int weight) {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.updateUserWeigth(weight);
        }
    }

    public void setBraceletArm(int mode) {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.updateHandParameter(mode);
        }
    }

    public void setBraceletDistanceUnit(int distanceUnit) {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.updateDistanceUnit(distanceUnit);
        }
    }

    public void setBraceletDate(Date date) {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.updateDate(date);
        }
    }

    public void setBraceletSedentaryReminderEnabled(LifevitSDKAT500SedentaryReminderTimeRange config) {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.setBraceletSedentaryReminderEnabled(config);
        }
    }

    public void setBraceletSedentaryReminderDisabled() {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.setBraceletSedentaryReminderDisabled();
        }
    }

    public void setBraceletAlarm(LifevitSDKAt500HrAlarmTime period) {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.setBraceletAlarm(period);
        }
    }

    public void disableBraceletAlarm(boolean isSecondaryAlarm) {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.disableBraceletAlarm(isSecondaryAlarm);
        }
    }

    public void enableBraceletNotifications(ArrayList<Integer> notifications) {

        // Check if access to notifications is enabled for this App
        String str = Settings.Secure.getString(mContext.getContentResolver(), "enabled_notification_listeners");
        boolean isNotificationListenerEnabled = (str != null && str.contains(NotificationReceiverService.PACKAGE_NAME));

        if (isNotificationListenerEnabled) {

            // Start notification listener
            if (!mNotificationListenerStarted) {
                ComponentName component = new ComponentName(mContext, NotificationReceiverService.class);
                PackageManager pm = mContext.getPackageManager();
                pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                mNotificationListenerStarted = true;
            }

            // Finally, set notifications
            PreferenceUtil.setBraceletNotifications(mContext, notifications);

            braceletListener.braceletInfoReceived("Notifications enabled: " + notifications.size());

        } else {
            if (braceletListener != null) {
                braceletListener.braceletError(LifevitSDKConstants.CODE_NOTIFICATION_ACCESS);
            }
        }
    }

    public void enableBraceletAntilost(boolean enabled) {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.updateParameter(LifevitSDKConstants.BRACELET_PARAM_ANTILOST, enabled);
        }
    }

    public void enableBraceletMonitorHR(boolean enabled) {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.updateParameter(LifevitSDKConstants.BRACELET_PARAM_HRMONITOR, enabled);
        }
    }

    public void enableCamera(boolean enabled) {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.updateParameter(LifevitSDKConstants.BRACELET_PARAM_CAMERA, enabled);
        }
    }

    public void enableBraceletFindPhone(boolean enabled) {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.updateParameter(LifevitSDKConstants.BRACELET_PARAM_FIND_PHONE, enabled);
        }
    }

    public void sendFindDevice() {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.findDevice();
        }
    }

    public void activateBracelet() {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.activateDevice();
        }
    }

    public void bindBracelet() {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.bindDevice();
        }
    }

    public void getBraceletHistorySync() {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.getSyncHistoryData();
        }
    }

    public void getBraceletHeartBeat() {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.getCurrentHeartBeat();
        }
    }

    public void getBraceletCurrentSteps() {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.getCurrentDaySteps();
        }
    }

    public void sendBraceletNotification(int type) {
        LifevitSDKBleDeviceBraceletAT500HR bracelet500HR = (LifevitSDKBleDeviceBraceletAT500HR) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
        if (bracelet500HR != null) {
            bracelet500HR.sendNotificationToDevice(type);
        }
    }


    // endregion

    // region --- Tensiobracelet Methods ---


    public void tensiobraceletSetDate(long dateTime) {
        LifevitSDKBleDeviceTensiobracelet tensiobracelet = (LifevitSDKBleDeviceTensiobracelet) getDeviceByType(LifevitSDKConstants.DEVICE_TENSIOBRACELET);
        if (tensiobracelet != null) {
            tensiobracelet.setDate(dateTime);
        }
    }

    public void tensiobraceletStartMeasurement() {
        LifevitSDKBleDeviceTensiobracelet tensiobracelet = (LifevitSDKBleDeviceTensiobracelet) getDeviceByType(LifevitSDKConstants.DEVICE_TENSIOBRACELET);
        if (tensiobracelet != null) {
            tensiobracelet.startMeasurement();
        }
    }

    public void tensiobraceletGetBloodPressureHistoryData() {
        LifevitSDKBleDeviceTensiobracelet tensiobracelet = (LifevitSDKBleDeviceTensiobracelet) getDeviceByType(LifevitSDKConstants.DEVICE_TENSIOBRACELET);
        if (tensiobracelet != null) {
            tensiobracelet.getBloodPressureHistoryData();
        }
    }

    public void tensiobraceletReturnMainScreen() {
        LifevitSDKBleDeviceTensiobracelet tensiobracelet = (LifevitSDKBleDeviceTensiobracelet) getDeviceByType(LifevitSDKConstants.DEVICE_TENSIOBRACELET);
        if (tensiobracelet != null) {
            tensiobracelet.returnScreen();
        }
    }

    public void tensiobraceletProgramAutomaticMeasurements(LifevitSDKTensioBraceletMeasurementInterval config) {
        LifevitSDKBleDeviceTensiobracelet tensiobracelet = (LifevitSDKBleDeviceTensiobracelet) getDeviceByType(LifevitSDKConstants.DEVICE_TENSIOBRACELET);
        if (tensiobracelet != null) {
            tensiobracelet.setProgramAutomaticMeasurements(config);
        }
    }

    public void tensiobraceletDeactivateAutomaticMeasurements() {
        LifevitSDKBleDeviceTensiobracelet tensiobracelet = (LifevitSDKBleDeviceTensiobracelet) getDeviceByType(LifevitSDKConstants.DEVICE_TENSIOBRACELET);
        if (tensiobracelet != null) {
            tensiobracelet.deactivateAutomaticMeasurements();
        }
    }


    // endregion


    // region --- Bracelet AT2019 ---

    public void bracelet2019GetBasicInfo() {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.getBasicInfo();
        }
    }

    public void bracelet2019GetFeatureList() {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.getFeatureList();
        }
    }

    public void bracelet2019SetDeviceTime(Long time) {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.setTime(time);
        }
    }


    public void bracelet2019GetDeviceTime() {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.getDeviceTime();
        }
    }

    public void bracelet2019SynchronizeData() {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.synchronizeData();
        }
    }

    /*
    public void bracelet2019EndSynchronizeData() {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.endSynchronizeData();
        }
    }
    */
    public void bracelet2019SynchronizeSportsData() {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.synchronizeSportsData();
        }
    }

    public void bracelet2019SynchronizeSleepData() {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.synchronizeSleepData();
        }
    }

    public void bracelet2019SynchronizeHeartRateData() {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.synchronizeHeartRateData();
        }
    }

    public void bracelet2019SynchronizeHistoricSportData() {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.synchronizeSportsData();
        }
    }

    public void bracelet2019SynchronizeHistoricSleepData() {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.synchronizeHistoricSleepData();
        }
    }

    public void bracelet2019SynchronizeHistoricHeartRateData() {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.synchronizeHistoricHeartRateData();
        }
    }

    public void bracelet2019GetBattery() {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.getBattery();
        }
    }

    public void bracelet2019StartSynchronization() {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.startSynchronization();
        }
    }

    public void bracelet2019ConfigureAndroidPhone() {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.configureAndroidPhone();
        }
    }


    public void bracelet2019ConfigureAlarm(LifevitSDKAlarmTime alarm) {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.configureAlarm(alarm);
        }
    }

    public void bracelet2019RemoveAlarm(boolean deletePrimaryAlarm) {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.removeAlarm(deletePrimaryAlarm);
        }
    }

    public void bracelet2019SetGoals(int steps, int sleepHour, int sleepMinute) {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.setGoals(steps, sleepHour, sleepMinute);
        }
    }

    public void bracelet2019SetUserInformation(LifevitSDKUserData user) {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.setUserInformation(user);
        }
    }


    public void bracelet2019ConfigureBraceletSedentaryAlarm(LifevitSDKMonitoringAlarm alarm) {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.configureBraceletSedentaryAlarm(alarm);
        }
    }

    public void bracelet2019DisableBraceletSedentaryAlarm() {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.disableBraceletSedentaryAlarm();
        }
    }

    public void bracelet2019ConfigureAntitheft(Boolean active) {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.configureAntitheft(active);
        }
    }

    public void bracelet2019ConfigureRiseHand(Boolean leftHand) {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.configureRiseHand(leftHand);
        }
    }

    public void bracelet2019ConfigureHeartRateIntervalSetting(int burnFatThreshold, int aerobicExercise, int limitExercise, int userMaxHR) {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.configureHeartRateIntervalSetting(burnFatThreshold, aerobicExercise, limitExercise, userMaxHR);
        }
    }

    public void bracelet2019ConfigureHeartRateMonitoring(Boolean enabled, Boolean timeIntervalEnabled, LifevitSDKSedentaryAlarm monitoringTime) {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.configureHeartRateMonitoring(enabled, timeIntervalEnabled, monitoringTime);
        }
    }

    public void bracelet2019ConfigureFindPhone(Boolean enabled) {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.configureFindPhone(enabled);
        }
    }

    public void bracelet2019ConfigureACNS(LifevitSDKAppNotification appNotification) {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.configureACNS(appNotification);
        }
    }

    public void bracelet2019ConfigureSleepMonitoring(Boolean enabled, LifevitSDKMonitoringAlarm monitoringTime) {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.configureSleepMonitoring(enabled, monitoringTime);
        }
    }

    public void bracelet2019SendMessageReceived() {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.messageReceived();
        }
    }

    /*
    public void bracelet2019CancelOp() {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.cancelOp();
        }
    }

    public void bracelet2019Bind() {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.sendBind();
        }
    }

    public void bracelet2019Unbind() {
        LifevitSDKBleDeviceBraceletAT2019 bracelet = (LifevitSDKBleDeviceBraceletAT2019) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
        if (bracelet != null) {
            bracelet.sendUnbind();
        }
    }
    */

    // endregion


    // region --- Weight Scale Methods ---


    public void setUpWeightScale(int gender, int ageYears, int heightCm) {

        setUpWeightScale(gender, ageYears, heightCm, LifevitSDKConstants.WEIGHT_UNIT_KG);
    }

    public void setUpWeightScale(int gender, int ageYears, int heightCm, int weightUnit) {

        LogUtils.log(Log.INFO, CLASS_TAG, "setUpWeightScale. Configure weight scale.");

        PreferenceUtil.setWeightScaleUserGender(mContext, gender);
        PreferenceUtil.setWeightScaleUserAge(mContext, ageYears);
        PreferenceUtil.setWeightScaleUserHeight(mContext, heightCm);
        PreferenceUtil.setWeightScaleUnit(mContext, weightUnit);
    }

    public void getWeightHistoryData() {

        LifevitSDKBleDeviceWeightScale device = (LifevitSDKBleDeviceWeightScale) getDeviceByType(LifevitSDKConstants.DEVICE_WEIGHT_SCALE);
        if (device != null) {
            device.getHistoryData();
        }
    }


    // endregion

    // region --- Bracelet V2 methods ---


    public void braceletAT250SetDeviceDate(Date date) {
        LifevitSDKBleDeviceBraceletAT250 braceletAt250 = (LifevitSDKBleDeviceBraceletAT250) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT250);
        if (braceletAt250 != null) {
            braceletAt250.setDeviceDate(date);
        }
    }

    public void braceletAT250SetPersonalInfo(int userHeight, int userWeight, int userGender, int userAge) {
        LifevitSDKBleDeviceBraceletAT250 braceletAt250 = (LifevitSDKBleDeviceBraceletAT250) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT250);
        if (braceletAt250 != null) {
            braceletAt250.setPersonalInfo(userHeight, userWeight, userGender, userAge);
        }
    }

    public void braceletAT250SetTargetSteps(int targetSteps) {
        LifevitSDKBleDeviceBraceletAT250 braceletAt250 = (LifevitSDKBleDeviceBraceletAT250) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT250);
        if (braceletAt250 != null) {
            braceletAt250.setTargetSteps(targetSteps);
        }
    }

    public void braceletAT250GetHistoryData(int numberDaysAgo) {
        LifevitSDKBleDeviceBraceletAT250 braceletAt250 = (LifevitSDKBleDeviceBraceletAT250) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT250);
        if (braceletAt250 != null) {
//            PreferenceUtil.setBraceletBT250HistoryNumberDays(mContext, numberDaysAgo);
            braceletAt250.getHistoryData(numberDaysAgo);
        }
    }

    public void braceletAT250GetTodayData() {
        LifevitSDKBleDeviceBraceletAT250 braceletAt250 = (LifevitSDKBleDeviceBraceletAT250) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT250);
        if (braceletAt250 != null) {
            braceletAt250.getTodayData();
        }
    }

    public void braceletAT250SetFirmwareUpdateParameters(String channelName, int notificationIcon, String notificationTitle, String notificationMessage) {
        PreferenceUtil.setChannelName(mContext, channelName);
        PreferenceUtil.setNotificationIcon(mContext, notificationIcon);
        PreferenceUtil.setPrefNotificationTitle(mContext, notificationTitle);
        PreferenceUtil.setPrefNotificationMessage(mContext, notificationMessage);
    }

    public void braceletAT250UpdateFirmware() {
        LifevitSDKBleDeviceBraceletAT250 braceletAt250 = (LifevitSDKBleDeviceBraceletAT250) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT250);
        if (braceletAt250 != null) {
            braceletAt250.updateFirmware();
        }
    }

    public void braceletAT250GetFirmwareVersionNumber() {
        LifevitSDKBleDeviceBraceletAT250 braceletAt250 = (LifevitSDKBleDeviceBraceletAT250) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT250);
        if (braceletAt250 != null) {
            braceletAt250.getFirmwareVersionNumber(false);
        }
    }


    // endregion

    // region --- Bracelet AT250HR methods ---


    public void braceletAT250GetHRData() {
        LifevitSDKBleDeviceBraceletAT250 braceletAt250 = (LifevitSDKBleDeviceBraceletAT250) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT250);
        if (braceletAt250 != null && braceletAt250.hasHR()) {
            braceletAt250.getHeartRateValue(0);
        } else if (braceletAT250Listener != null) {
            braceletAT250Listener.operationFinished(false);

        }
    }

    public void braceletAT250SetMonitoringHR(boolean enabled) {
        LifevitSDKBleDeviceBraceletAT250 braceletAt250 = (LifevitSDKBleDeviceBraceletAT250) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT250);
        if (braceletAt250 != null && braceletAt250.hasHR()) {
            braceletAt250.setMonitoringHREnabled(enabled);
        } else if (braceletAT250Listener != null) {
            braceletAT250Listener.operationFinished(false);

        }
    }

    public void braceletAT250SetRealtimeHR(boolean enabled) {
        LifevitSDKBleDeviceBraceletAT250 braceletAt250 = (LifevitSDKBleDeviceBraceletAT250) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT250);
        if (braceletAt250 != null && braceletAt250.hasHR()) {
            if (enabled) {
                braceletAt250.setMonitoringHREnabled(enabled);
            }
            braceletAt250.setRealtimeHREnabled(enabled);
        } else if (braceletAT250Listener != null) {
            braceletAT250Listener.operationFinished(false);

        }
    }

    public void braceletAT250SetMonitoringHRAuto(boolean enabled, LifevitSDKAT250TimeRange timeRange) {
        LifevitSDKBleDeviceBraceletAT250 braceletAt250 = (LifevitSDKBleDeviceBraceletAT250) getDeviceByType(LifevitSDKConstants.DEVICE_BRACELET_AT250);
        if (braceletAt250 != null && braceletAt250.hasHR()) {
            braceletAt250.setMonitoringHRAuto(enabled, timeRange);
        } else if (braceletAT250Listener != null) {
            braceletAT250Listener.operationFinished(false);
        }
    }

    // endregion


    // region --- Pill Reminder Methods ---


    public void prGetDeviceTime() {
        LifevitSDKBleDevice device = getDeviceByType(LifevitSDKConstants.DEVICE_PILL_REMINDER);
        if (device != null && device instanceof LifevitSDKBleDevicePillReminder) {
            ((LifevitSDKBleDevicePillReminder) device).getDeviceDate();
        }
    }

    public void prGetDeviceTimeZone() {
        LifevitSDKBleDevice device = getDeviceByType(LifevitSDKConstants.DEVICE_PILL_REMINDER);
        if (device != null && device instanceof LifevitSDKBleDevicePillReminder) {
            ((LifevitSDKBleDevicePillReminder) device).getDeviceTimeZone();
        }
    }

    public void prGetBatteryLevel() {
        LifevitSDKBleDevice device = getDeviceByType(LifevitSDKConstants.DEVICE_PILL_REMINDER);
        if (device != null && device instanceof LifevitSDKBleDevicePillReminder) {
            ((LifevitSDKBleDevicePillReminder) device).getBatteryLevel();
        }
    }

    public void prGetLatestSynchronizationTime() {
        LifevitSDKBleDevice device = getDeviceByType(LifevitSDKConstants.DEVICE_PILL_REMINDER);
        if (device != null && device instanceof LifevitSDKBleDevicePillReminder) {
            ((LifevitSDKBleDevicePillReminder) device).getLatestSynchronizationTime();
        }
    }

    public void prSetSuccessfulSynchronizationStatus() {
        LifevitSDKBleDevice device = getDeviceByType(LifevitSDKConstants.DEVICE_PILL_REMINDER);
        if (device != null && device instanceof LifevitSDKBleDevicePillReminder) {
            ((LifevitSDKBleDevicePillReminder) device).setSuccessfulSynchronizationStatus();
        }
    }

    public void prClearSchedulePerformanceHistory() {
        LifevitSDKBleDevice device = getDeviceByType(LifevitSDKConstants.DEVICE_PILL_REMINDER);
        if (device != null && device instanceof LifevitSDKBleDevicePillReminder) {
            ((LifevitSDKBleDevicePillReminder) device).clearSchedulePerformanceHistory();
        }
    }

    public void prGetAlarmSchedule() {
        LifevitSDKBleDevice device = getDeviceByType(LifevitSDKConstants.DEVICE_PILL_REMINDER);
        if (device != null && device instanceof LifevitSDKBleDevicePillReminder) {
            ((LifevitSDKBleDevicePillReminder) device).getAlarmSchedule();
        }
    }

    public void prSetAlarmSchedule(ArrayList<LifevitSDKPillReminderAlarmData> alarms) {
        LifevitSDKBleDevice device = getDeviceByType(LifevitSDKConstants.DEVICE_PILL_REMINDER);
        if (device != null && device instanceof LifevitSDKBleDevicePillReminder) {
            ((LifevitSDKBleDevicePillReminder) device).setAlarmsSchedule(alarms);
        }
    }

    public void prGetSchedulePerformanceHistory() {
        LifevitSDKBleDevice device = getDeviceByType(LifevitSDKConstants.DEVICE_PILL_REMINDER);
        if (device != null && device instanceof LifevitSDKBleDevicePillReminder) {
            ((LifevitSDKBleDevicePillReminder) device).getSchedulePerformanceHistory();
        }
    }

    public void prSetAlarmDuration(int duration) {
        LifevitSDKBleDevice device = getDeviceByType(LifevitSDKConstants.DEVICE_PILL_REMINDER);
        if (device != null && device instanceof LifevitSDKBleDevicePillReminder) {
            ((LifevitSDKBleDevicePillReminder) device).setAlarmDuration(duration);
        }
    }

    public void prSetAlarmConfirmationTime(int confirmationTime) {
        LifevitSDKBleDevice device = getDeviceByType(LifevitSDKConstants.DEVICE_PILL_REMINDER);
        if (device != null && device instanceof LifevitSDKBleDevicePillReminder) {
            ((LifevitSDKBleDevicePillReminder) device).setAlarmConfirmationTime(confirmationTime);
        }
    }


    public void prClearAlarmSchedule() {
        LifevitSDKBleDevice device = getDeviceByType(LifevitSDKConstants.DEVICE_PILL_REMINDER);
        if (device != null && device instanceof LifevitSDKBleDevicePillReminder) {
            ((LifevitSDKBleDevicePillReminder) device).clearAlarmSchedule();
        }
    }


    // endregion

}