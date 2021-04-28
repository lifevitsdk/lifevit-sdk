package es.lifevit.sdk.sampleapp;

import android.app.Application;
import android.util.Log;

import es.lifevit.sdk.LifevitSDKManager;


public class SDKTestApplication extends Application {

    LifevitSDKManager lifevitSDKManager;
    private static SDKTestApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        lifevitSDKManager = new LifevitSDKManager(this);

        lifevitSDKManager.setLogLevel(Log.DEBUG);

        // startService(new Intent(this, LifeVitTensiService.class));
    }

    public static SDKTestApplication getInstance() {
        return instance;
    }

    public LifevitSDKManager getLifevitSDKManager() {
        return lifevitSDKManager;
    }
}
