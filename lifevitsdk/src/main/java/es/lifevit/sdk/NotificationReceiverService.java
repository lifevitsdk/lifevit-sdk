package es.lifevit.sdk;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.List;

import es.lifevit.sdk.utils.LogUtils;

import static es.lifevit.sdk.LifevitSDKConstants.SNS_TYPE_CALL;
import static es.lifevit.sdk.LifevitSDKConstants.SNS_TYPE_EMAIL;
import static es.lifevit.sdk.LifevitSDKConstants.SNS_TYPE_FACEBOOK;
import static es.lifevit.sdk.LifevitSDKConstants.SNS_TYPE_INSTAGRAM;
import static es.lifevit.sdk.LifevitSDKConstants.SNS_TYPE_LINE;
import static es.lifevit.sdk.LifevitSDKConstants.SNS_TYPE_QQ;
import static es.lifevit.sdk.LifevitSDKConstants.SNS_TYPE_SKYPE;
import static es.lifevit.sdk.LifevitSDKConstants.SNS_TYPE_SMS;
import static es.lifevit.sdk.LifevitSDKConstants.SNS_TYPE_TWITTER;
import static es.lifevit.sdk.LifevitSDKConstants.SNS_TYPE_WECHAT;
import static es.lifevit.sdk.LifevitSDKConstants.SNS_TYPE_WHATSAPP;

public class NotificationReceiverService extends NotificationListenerService {

    public static final String BROADCAST_NOTIFICATION = "LIFEVIT_NOTIFICATION";
    public static String TAG = NotificationReceiverService.class.getSimpleName();

    public static String PACKAGE_NAME = NotificationReceiverService.class.getCanonicalName();

    public void onNotificationPosted(StatusBarNotification sbn) {
        LogUtils.log(Log.DEBUG, TAG, "onNotificationPosted");
        handleNotificationPosted(this, sbn.getPackageName(), sbn.getNotification());
    }

    public void handleNotificationPosted(Context context, String pkg, Notification notification) {

        LogUtils.log(Log.DEBUG, TAG, "onNotificationPosted pkg = " + pkg);

        List<Integer> notifications = PreferenceUtil.getBraceletNotifications(context);

        if (notifications.contains(SNS_TYPE_CALL) && "com.google.android.dialer".equals(pkg)) {
            LogUtils.log(Log.DEBUG, TAG, "handleNotificationPosted: SNS_TYPE_CALL");
            pushMsg(context, SNS_TYPE_CALL);
        }

        if (notifications.contains(SNS_TYPE_SMS) && (pkg.toLowerCase().contains("com.android.mms") || pkg.toLowerCase().contains("com.samsung.android.messaging"))) {
            LogUtils.log(Log.DEBUG, TAG, "handleNotificationPosted: SNS_TYPE_SMS");
            pushMsg(context, SNS_TYPE_SMS);
        }

        if (notifications.contains(SNS_TYPE_WECHAT) && "com.tencent.mm".equals(pkg)) {
            LogUtils.log(Log.DEBUG, TAG, "handleNotificationPosted: SNS_TYPE_WECHAT");
            pushMsg(context, SNS_TYPE_WECHAT);
        }

        if (notifications.contains(SNS_TYPE_QQ) && ("com.tencent.mobileqq".equals(pkg) || "com.tencent.mobileqqi".equals(pkg))) {
            LogUtils.log(Log.DEBUG, TAG, "handleNotificationPosted: SNS_TYPE_QQ");
            pushMsg(context, SNS_TYPE_QQ);
        }

        if (notifications.contains(SNS_TYPE_EMAIL) && ("com.android.email".equals(pkg) || pkg.toLowerCase().contains("email") || "com.google.android.gm".equals(pkg))) {
            LogUtils.log(Log.DEBUG, TAG, "handleNotificationPosted: SNS_TYPE_EMAIL");
            pushMsg(context, SNS_TYPE_EMAIL);
        }

        if (notifications.contains(SNS_TYPE_FACEBOOK) && ("com.facebook.katana".equals(pkg) || "com.facebook.orca".equals(pkg))) {
            LogUtils.log(Log.DEBUG, TAG, "handleNotificationPosted: SNS_TYPE_FACEBOOK");
            pushMsg(context, SNS_TYPE_FACEBOOK);
        }

        if (notifications.contains(SNS_TYPE_TWITTER) && "com.twitter.android".equals(pkg)) {
            LogUtils.log(Log.DEBUG, TAG, "handleNotificationPosted: SNS_TYPE_TWITTER");
            pushMsg(context, SNS_TYPE_TWITTER);
        }

        if (notifications.contains(SNS_TYPE_WHATSAPP) && "com.whatsapp".equals(pkg)) {
            LogUtils.log(Log.DEBUG, TAG, "handleNotificationPosted: SNS_TYPE_WHATSAPP");
            pushMsg(context, SNS_TYPE_WHATSAPP);
        }

        if (notifications.contains(SNS_TYPE_INSTAGRAM) && "com.instagram.android".equals(pkg)) {
            LogUtils.log(Log.DEBUG, TAG, "handleNotificationPosted: SNS_TYPE_INSTAGRAM");
            pushMsg(context, SNS_TYPE_INSTAGRAM);
        }

        if (notifications.contains(SNS_TYPE_LINE) && pkg.toLowerCase().contains("line")) {
            LogUtils.log(Log.DEBUG, TAG, "handleNotificationPosted: SNS_TYPE_LINE");
            pushMsg(context, SNS_TYPE_LINE);
        }

        if (notifications.contains(SNS_TYPE_SKYPE) && pkg.toLowerCase().contains("skype")) {
            LogUtils.log(Log.DEBUG, TAG, "handleNotificationPosted: SNS_TYPE_SKYPE");
            pushMsg(context, SNS_TYPE_SKYPE);
        }
    }


    public void onNotificationRemoved(StatusBarNotification sbn) {
    }


    private void pushMsg(Context context, int type) {
        Intent broadcast = new Intent(BROADCAST_NOTIFICATION);
        broadcast.putExtra("type", type);
        context.sendBroadcast(broadcast);
    }


}
