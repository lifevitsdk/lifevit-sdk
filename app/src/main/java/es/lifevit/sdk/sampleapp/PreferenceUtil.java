package es.lifevit.sdk.sampleapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import es.lifevit.sdk.LifevitSDKConstants;


public class PreferenceUtil {

    private static final String PREF_BRACELET_ADDRESS = "lifevit_sampleapp_ble_devices_bracelet_address";


    public static void setBraceletAddress(Context context, String addr) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = mySharedPrefs.edit();
        editor.putString(PREF_BRACELET_ADDRESS, addr);
        editor.commit();
    }

    public static String getBraceletAddress(Context context) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mySharedPrefs.getString(PREF_BRACELET_ADDRESS, "");
    }



}