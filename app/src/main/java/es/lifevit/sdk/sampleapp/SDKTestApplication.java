package es.lifevit.sdk.sampleapp;

import android.app.Application;

import es.lifevit.sdk.LifevitSDKManager;


public class SDKTestApplication extends Application {

    LifevitSDKManager lifevitSDKManager;
    private static SDKTestApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        lifevitSDKManager = new LifevitSDKManager(this);

        lifevitSDKManager.setLogLevel(-1);

        // startService(new Intent(this, LifeVitTensiService.class));
    }

    public static SDKTestApplication getInstance() {
        return instance;
    }

    public LifevitSDKManager getLifevitSDKManager() {
        return lifevitSDKManager;
    }
}
