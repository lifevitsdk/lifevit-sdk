package es.lifevit.sdk.sampleapp.services;

import android.app.Service;

import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import es.lifevit.sdk.LifevitSDKConstants;
import es.lifevit.sdk.LifevitSDKHeartData;
import es.lifevit.sdk.LifevitSDKManager;
import es.lifevit.sdk.listeners.LifevitSDKDeviceListener;
import es.lifevit.sdk.listeners.LifevitSDKHeartListener;
import es.lifevit.sdk.sampleapp.PreferenceUtil;
import es.lifevit.sdk.sampleapp.SDKTestApplication;


public class LifeVitTensiService extends Service {
    private static final String TAG = "LifeVitTensiService";

    private static final long TIME_TO_DEVICE_CONNECT = 4500;
    private static final int CHECK_TENSIOMETER_CONNECTION_INTERVAL = 5 * 1000;

    private Context context;
    private LifevitSDKManager lifevitSDKManager;
    private Timer timer ;
    private String macAddress;
    private LifevitSDKDeviceListener cl;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        macAddress = PreferenceUtil.getBraceletAddress(this);
        context = this.getApplicationContext();
        lifevitSDKManager = SDKTestApplication.getInstance().getLifevitSDKManager();
        cl = createLifevitSDKDeviceListener();
        lifevitSDKManager.addDeviceListener(cl);
        lifevitSDKManager.setHeartListener(createLifevitSDKHeartListener());
        timer = new Timer();
        timer.schedule(new connectDevice(), 0, CHECK_TENSIOMETER_CONNECTION_INTERVAL);
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    class connectDevice extends TimerTask {
        public void run(){
            Log.d(TAG, "connectDevice. Comprobando bluetooth... ");
            final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            final NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (!mBluetoothAdapter.isEnabled()) {
                mNotificationManager.cancel(2);
                mBluetoothAdapter.enable();
            }
            searchDeviceForConnect();
        }
    }

    private void searchDeviceForConnect(){
        if (isTensiMacAddress()) {
            Log.d(TAG, "searchDeviceForConnect. Buscando dispositivo: " + macAddress);
            lifevitSDKManager.connectDevice(LifevitSDKConstants.DEVICE_TENSIOMETER, TIME_TO_DEVICE_CONNECT, macAddress);
        }
        else {
            Log.d(TAG, "searchDeviceForConnect. Buscando dispositivos...");
            lifevitSDKManager.connectDevice(LifevitSDKConstants.DEVICE_TENSIOMETER, TIME_TO_DEVICE_CONNECT);
        }
    }

    private boolean isTensiMacAddress(){
        return macAddress != null && !macAddress.isEmpty();
    }

    private LifevitSDKDeviceListener createLifevitSDKDeviceListener() {
        final LifevitSDKDeviceListener lifevitSDKDeviceListener = new LifevitSDKDeviceListener() {
            @Override
            public void deviceOnConnectionError(int device, int status) {
                Log.d(TAG, "deviceOnConnectionError: " + status);
            }

            @Override
            public void deviceOnConnectionChanged(int device, int status) {
                final String macAddressConnected = lifevitSDKManager.getDeviceAddress(LifevitSDKConstants.DEVICE_TENSIOMETER);
                PreferenceUtil.setBraceletAddress(LifeVitTensiService.this, macAddressConnected);
                Log.d(TAG, "deviceOnConnectionChanged: MAC:  '" + macAddress + "' STATUS: '" + status +"'.");
                if(status == LifevitSDKConstants.STATUS_CONNECTED){
                    if (!isTensiMacAddress()) {
                        macAddress = macAddressConnected;
                    }
                }
            }
        };
        return lifevitSDKDeviceListener;
    }

    private LifevitSDKHeartListener createLifevitSDKHeartListener(){
        final LifevitSDKHeartListener lifevitSDKHeartListener = new LifevitSDKHeartListener() {
            @Override
            public void heartDeviceOnProgressMeasurement(int pulse) {
                Log.d(TAG, "heartDeviceOnProgressMeasurement: " + pulse);
            }

            @Override
            public void heartDeviceOnBatteryResult(int battery) {
                Log.d(TAG, "heartDeviceOnProgressMeasurement: " + battery);
            }

            @Override
            public void heartDeviceOnResult(LifevitSDKHeartData lifevitSDKHeartData) {
                Log.d(TAG, "heartDeviceOnResult: " + lifevitSDKHeartData);
            }
        };
        return lifevitSDKHeartListener;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        lifevitSDKManager.removeDeviceListener(cl);

        super.onDestroy();
        if(timer!= null) {
            timer.cancel();
            timer.purge();
            timer= null;
        }
    }
}
