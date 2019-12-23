package es.lifevit.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;


public class PreferenceUtil {

    // Datos de usuario
    private static final String PREF_USER_HEIGHT = "lifevit_user_height";
    private static final String PREF_USER_WEIGHT = "lifevit_user_weight";
    private static final String PREF_NEW_BRACELET_LAST_REAL_TIME_DATA = "lifevit_new_bracelet_last_real_time_data";
    private static final String PREF_BRACELET_NOTIFICATIONS = "lifevit_notifications";

    private static final String PREF_WEIGHT_SCALE_USER_GENDER = "lifevit_sdk_weight_scale_user_gender";
    private static final String PREF_WEIGHT_SCALE_USER_AGE = "lifevit_sdk_weight_scale_user_age";
    private static final String PREF_WEIGHT_SCALE_USER_HEIGHT = "lifevit_sdk_weight_scale_user_height";
    private static final String PREF_WEIGHT_SCALE_UNIT = "lifevit_sdk_weight_scale_unit";

    private static final String PREF_BRACELET_AT250_USER_HEIGHT = "lifevit_sdk_bracelet_AT250_user_height";
    private static final String PREF_BRACELET_AT250_USER_WEIGHT = "lifevit_sdk_bracelet_AT250_user_weight";
    private static final String PREF_BRACELET_AT250_USER_GENDER = "lifevit_sdk_bracelet_AT250_user_gender";
    private static final String PREF_BRACELET_AT250_USER_AGE = "lifevit_sdk_bracelet_AT250_user_age";
    private static final String PREF_BRACELET_AT250_LAST_UPDATE_DATE = "lifevit_sdk_bracelet_AT250_LAST_UPDATE_DATE";
//    private static final String PREF_BRACELET_AT250_HISTORY_NUMBER_DAYS = "lifevit_sdk_BRACELET_AT250_HISTORY_NUMBER_DAYS";

    private static final String PREF_NOTIFICATION_CHANNEL_NAME = "lifevit_sdk_notification_channel_name";
    private static final String PREF_NOTIFICATION_ICON = "lifevit_sdk_notification_icon";
    private static final String PREF_NOTIFICATION_TITLE = "lifevit_sdk_notification_title";
    private static final String PREF_NOTIFICATION_MESSAGE = "lifevit_sdk_notification_message";


    // --- Purifit bracelet ---

    public static void setUserHeight(Context context, int height) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = mySharedPrefs.edit();
        editor.putInt(PREF_USER_HEIGHT, height);
        editor.commit();
    }

    public static Integer getUserHeight(Context context) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mySharedPrefs.getInt(PREF_USER_HEIGHT, 170);
    }

    public static void setUserWeight(Context context, int weight) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = mySharedPrefs.edit();
        editor.putInt(PREF_USER_WEIGHT, weight);
        editor.commit();
    }

    public static Integer getUserWeight(Context context) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mySharedPrefs.getInt(PREF_USER_WEIGHT, 60);
    }

    public static void setNewBraceletLastRealTimeData(Context context, String data) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = mySharedPrefs.edit();
        editor.putString(PREF_NEW_BRACELET_LAST_REAL_TIME_DATA, data);
        editor.commit();
    }

    public static String getNewBraceletLastRealTimeData(Context context) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mySharedPrefs.getString(PREF_NEW_BRACELET_LAST_REAL_TIME_DATA, "");
    }


    public static void setBraceletNotifications(Context context, List<Integer> notifications) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = mySharedPrefs.edit();
        String listString = "";
        for (Integer notif : notifications) {
            listString += listString.length() == 0 ? notif.toString() : "," + notif.toString();
        }
        editor.putString(PREF_BRACELET_NOTIFICATIONS, listString);
        editor.commit();
    }


    public static List<Integer> getBraceletNotifications(Context context) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String list = mySharedPrefs.getString(PREF_BRACELET_NOTIFICATIONS, "");
        String[] parts = list.split(",");
        ArrayList<Integer> notifications = new ArrayList<>();
        for (String notif : parts) {
            try {
                notifications.add(Integer.parseInt(notif));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return notifications;
    }

    // --- Weight scale ---

    public static void setWeightScaleUserGender(Context context, int gender) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = mySharedPrefs.edit();
        editor.putInt(PREF_WEIGHT_SCALE_USER_GENDER, gender);
        editor.commit();
    }

    public static Integer getWeightScaleUserGender(Context context) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mySharedPrefs.getInt(PREF_WEIGHT_SCALE_USER_GENDER, LifevitSDKConstants.WEIGHT_SCALE_GENDER_FEMALE);
    }

    public static void setWeightScaleUserAge(Context context, int age) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = mySharedPrefs.edit();
        editor.putInt(PREF_WEIGHT_SCALE_USER_AGE, age);
        editor.commit();
    }

    public static Integer getWeightScaleUserAge(Context context) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mySharedPrefs.getInt(PREF_WEIGHT_SCALE_USER_AGE, 170);
    }

    public static void setWeightScaleUserHeight(Context context, int height) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = mySharedPrefs.edit();
        editor.putInt(PREF_WEIGHT_SCALE_USER_HEIGHT, height);
        editor.commit();
    }

    public static Integer getWeightScaleUserHeight(Context context) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mySharedPrefs.getInt(PREF_WEIGHT_SCALE_USER_HEIGHT, 170);
    }

    public static void setWeightScaleUnit(Context context, int unit) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = mySharedPrefs.edit();
        editor.putInt(PREF_WEIGHT_SCALE_UNIT, unit);
        editor.commit();
    }

    public static Integer getWeightScaleUnit(Context context) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mySharedPrefs.getInt(PREF_WEIGHT_SCALE_UNIT, LifevitSDKConstants.WEIGHT_UNIT_KG);
    }

    // --- Bracelet AT250 ---

    public static void setBraceletAT250UserHeight(Context context, int height) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = mySharedPrefs.edit();
        editor.putInt(PREF_BRACELET_AT250_USER_HEIGHT, height);
        editor.commit();
    }

    public static Integer getBraceletAT250UserHeight(Context context) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mySharedPrefs.getInt(PREF_BRACELET_AT250_USER_HEIGHT, 170);
    }

    public static void setBraceletAT250UserWeight(Context context, int weight) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = mySharedPrefs.edit();
        editor.putInt(PREF_BRACELET_AT250_USER_WEIGHT, weight);
        editor.commit();
    }

    public static Integer getBraceletAT250UserWeight(Context context) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mySharedPrefs.getInt(PREF_BRACELET_AT250_USER_WEIGHT, 60);
    }

    public static void setBraceletAT250UserGender(Context context, int gender) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = mySharedPrefs.edit();
        editor.putInt(PREF_BRACELET_AT250_USER_GENDER, gender);
        editor.commit();
    }

    public static Integer getBraceletAT250UserGender(Context context) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mySharedPrefs.getInt(PREF_BRACELET_AT250_USER_GENDER, LifevitSDKConstants.WEIGHT_SCALE_GENDER_FEMALE);
    }

    public static void setBraceletAT250UserAge(Context context, int age) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = mySharedPrefs.edit();
        editor.putInt(PREF_BRACELET_AT250_USER_AGE, age);
        editor.commit();
    }

    public static Integer getBraceletAT250UserAge(Context context) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mySharedPrefs.getInt(PREF_BRACELET_AT250_USER_AGE, 170);
    }


//    protected static void setNewBraceletLastUpdateDate(Context context, long date) {
//        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
//        Editor editor = mySharedPrefs.edit();
//        editor.putLong(PREF_BRACELET_AT250_LAST_UPDATE_DATE, date);
//        editor.commit();
//    }
//
//    protected static long getNewBraceletLastUpdateDate(Context context) {
//        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
//        return mySharedPrefs.getLong(PREF_BRACELET_AT250_LAST_UPDATE_DATE, 0L);
//    }


//    protected static void setBraceletBT250HistoryNumberDays(Context context, int numberDays) {
//        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
//        Editor editor = mySharedPrefs.edit();
//        editor.putInt(PREF_BRACELET_AT250_HISTORY_NUMBER_DAYS, numberDays);
//        editor.commit();
//    }
//
//    protected static int getBraceletBT250HistoryNumberDays(Context context) {
//        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
//        return mySharedPrefs.getInt(PREF_BRACELET_AT250_HISTORY_NUMBER_DAYS, 0);
//    }


    public static void setChannelName(Context context, String channelName) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = mySharedPrefs.edit();
        editor.putString(PREF_NOTIFICATION_CHANNEL_NAME, channelName);
        editor.commit();
    }

    public static String getChannelName(Context context) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mySharedPrefs.getString(PREF_NOTIFICATION_CHANNEL_NAME, "");
    }

    public static void setNotificationIcon(Context context, int notificationIcon) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = mySharedPrefs.edit();
        editor.putInt(PREF_NOTIFICATION_ICON, notificationIcon);
        editor.commit();
    }

    public static int getNotificationIcon(Context context) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mySharedPrefs.getInt(PREF_NOTIFICATION_ICON, 0);
    }

    public static void setPrefNotificationTitle(Context context, String notificationTitle) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = mySharedPrefs.edit();
        editor.putString(PREF_NOTIFICATION_TITLE, notificationTitle);
        editor.commit();
    }

    public static String getPrefNotificationTitle(Context context) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mySharedPrefs.getString(PREF_NOTIFICATION_TITLE, "");
    }

    public static void setPrefNotificationMessage(Context context, String notificationMessage) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = mySharedPrefs.edit();
        editor.putString(PREF_NOTIFICATION_MESSAGE, notificationMessage);
        editor.commit();
    }

    public static String getPrefNotificationMessage(Context context) {
        SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mySharedPrefs.getString(PREF_NOTIFICATION_MESSAGE, "");
    }

}