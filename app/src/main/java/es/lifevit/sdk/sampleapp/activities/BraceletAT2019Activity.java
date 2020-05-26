package es.lifevit.sdk.sampleapp.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import es.lifevit.sdk.LifevitSDKConstants;
import es.lifevit.sdk.LifevitSDKManager;
import es.lifevit.sdk.LifevitSDKUserData;
import es.lifevit.sdk.bracelet.LifevitSDKAlarmTime;
import es.lifevit.sdk.bracelet.LifevitSDKAppNotification;
import es.lifevit.sdk.bracelet.LifevitSDKBraceletData;
import es.lifevit.sdk.bracelet.LifevitSDKHeartbeatData;
import es.lifevit.sdk.bracelet.LifevitSDKMonitoringAlarm;
import es.lifevit.sdk.bracelet.LifevitSDKSedentaryAlarm;
import es.lifevit.sdk.bracelet.LifevitSDKSleepData;
import es.lifevit.sdk.bracelet.LifevitSDKStepData;
import es.lifevit.sdk.bracelet.LifevitSDKSummarySleepData;
import es.lifevit.sdk.bracelet.LifevitSDKSummaryStepData;
import es.lifevit.sdk.listeners.LifevitSDKBraceletAT2019Listener;
import es.lifevit.sdk.listeners.LifevitSDKDeviceListener;
import es.lifevit.sdk.sampleapp.R;
import es.lifevit.sdk.sampleapp.SDKTestApplication;


public class BraceletAT2019Activity extends AppCompatActivity {

    private static final String TAG = BraceletAT2019Activity.class.getSimpleName();

    TextView textview_connection_result, textview_info;
    Button button_connect, button_command;
    boolean isDisconnected = true;
    private LifevitSDKDeviceListener cl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bracelet_at2019);

        initComponents();
        initListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_BRACELET_AT2019)) {
            button_connect.setText("Disconnect");
            isDisconnected = false;
            textview_connection_result.setText("Connected");
            textview_connection_result.setTextColor(ContextCompat.getColor(BraceletAT2019Activity.this, android.R.color.holo_green_dark));
        } else {
            button_connect.setText("Connect");
            isDisconnected = true;
            textview_connection_result.setText("Disconnected");
            textview_connection_result.setTextColor(ContextCompat.getColor(BraceletAT2019Activity.this, android.R.color.holo_red_dark));
        }
        initSdk();
    }


    @Override
    protected void onPause() {
        super.onPause();
        SDKTestApplication.getInstance().getLifevitSDKManager().removeDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setBraceletAT2019Listener(null);
    }


    private void initComponents() {
        textview_info = findViewById(R.id.bracelet_at2019_textview_command_info);
        textview_info.setMovementMethod(new ScrollingMovementMethod());
        textview_connection_result = findViewById(R.id.bracelet_at2019_connection_result);

        button_connect = findViewById(R.id.bracelet_at2019_connect);
        button_command = findViewById(R.id.bracelet_at2019_button_command);
    }


    private void initListeners() {

        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDisconnected) {
                    SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_BRACELET_AT2019, 10000);
                } else {
                    SDKTestApplication.getInstance().getLifevitSDKManager().disconnectDevice(LifevitSDKConstants.DEVICE_BRACELET_AT2019);
                }
            }
        });

        button_command.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final LifevitSDKManager manager = SDKTestApplication.getInstance().getLifevitSDKManager();

                CharSequence[] colors = new CharSequence[]{
                        "0. Get basic info",
                        "1. Get feature list",
                        "2. Set device time (current time)",
                        "3. Set device time (21/10/2015 4:29)",
                        "4. Get device time",
                        "5. Get current day data summary",
                        "6. [Day data] Steps",
                        "7. [Day data] Sleep",
                        "8. [Day data] Heart Rate",
                        "9. [Historical] Step",
                        "10. [Historical] Sleep",
                        "11. [Historical] Heart Rate",
                        "12. [Alarm] Set alarm at 10:30 and repeat every day",
                        "13. [Alarm] Set alarm at 11:40 only for today",
                        "14. [Alarm] Remove alarms",
                        "15. [Goals] Set steps goal to 500 and sleep goal to 6h 30min",
                        "16. [Goals] Set steps goal to 7000 and sleep goal to 8h",
                        "17. [User information] Male, 70kg, 1.74m, born 24/12/1980",
                        "18. [User information] Female, 57kg, 1.62m, born 13/06/1990",
                        "19. [Sedentary reminder] Enable every 30 min from 8:00 to 18:30 all week days (Mon - Fri)",
                        "20. [Sedentary reminder] Disable",
                        "21. [Antitheft] Enable",
                        "22. [Antitheft] Disable",
                        "23. [Set hand] Left",
                        "24. [Set hand] Right",
                        "25. Configure using an Android Phone",
                        "26. [Heart rate intervals] 55 (burn fat), 65 (aerobic), 80 (limit) and 100 (user max)",
                        "27. [Heart rate intervals] 70 (burn fat), 120 (aerobic), 160 (limit) and 220 (user max)",
                        "28. Set heart rate monitoring from 17:30 to 20:30",
                        "29. Set find phone",
                        "30. [Set notifications] Enable all",
                        "31. [Set notifications] Disable all",
                        "32. Set sleep monitoring reminder",
                        "33. Get current battery",
                        "34. Start complete synchronization",
                        "35. Send message received"
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(BraceletAT2019Activity.this);
                builder.setTitle("Select command");
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                manager.bracelet2019GetBasicInfo();
                                break;
                            case 1:
                                manager.bracelet2019GetFeatureList();
                                break;
                            case 2: {
                                Calendar cal = Calendar.getInstance();
                                Long time = cal.getTimeInMillis();
                                manager.bracelet2019SetDeviceTime(time);
                                break;
                            }
                            case 3: {
                                Calendar cal = Calendar.getInstance();
                                cal.set(2015, Calendar.OCTOBER, 21, 4, 29);
                                manager.bracelet2019SetDeviceTime(cal.getTimeInMillis());
                                break;
                            }
                            case 4:
                                manager.bracelet2019GetDeviceTime();
                                break;
                            case 5:
                                manager.bracelet2019SynchronizeData();
                                break;
                            case 6:
                                manager.bracelet2019SynchronizeSportsData();
                                break;
                            case 7:
                                manager.bracelet2019SynchronizeSleepData();
                                break;
                            case 8:
                                manager.bracelet2019SynchronizeHeartRateData();
                                break;
                            case 9:
                                manager.bracelet2019SynchronizeHistoricSportData();
                                break;
                            case 10:
                                manager.bracelet2019SynchronizeHistoricSleepData();
                                break;
                            case 11:
                                manager.bracelet2019SynchronizeHistoricHeartRateData();
                                break;
                            case 12: {
                                LifevitSDKAlarmTime alarm = new LifevitSDKAlarmTime();
                                alarm.setHour(10);
                                alarm.setMinute(30);
                                alarm.setAllDays();
                                manager.bracelet2019ConfigureAlarm(alarm);
                                break;
                            }
                            case 13: {
                                LifevitSDKAlarmTime alarm = new LifevitSDKAlarmTime();
                                alarm.setHour(11);
                                alarm.setMinute(40);
                                manager.bracelet2019ConfigureAlarm(alarm);
                                break;
                            }
                            case 14: {
                                manager.bracelet2019RemoveAlarm(true);
                                break;
                            }
                            case 15: {
                                manager.bracelet2019SetGoals(500, 6, 30);
                                break;
                            }
                            case 16: {
                                manager.bracelet2019SetGoals(7000, 8, 0);
                                break;
                            }
                            case 17: {

                                // Male, 85kg, 1.74m, born 24/12/1980

                                Calendar cal = Calendar.getInstance();
                                cal.set(Calendar.YEAR, 1980);
                                cal.set(Calendar.MONTH, 11);
                                cal.set(Calendar.DAY_OF_MONTH, 24);

                                long birthdate = cal.getTimeInMillis();

                                LifevitSDKUserData user = new LifevitSDKUserData(birthdate, 70, 174, LifevitSDKConstants.GENDER_MALE);

                                manager.bracelet2019SetUserInformation(user);
                                break;
                            }
                            case 18: {

                                // Female, 57kg, 1.62m, born 24/12/1980

                                Calendar cal = Calendar.getInstance();
                                cal.set(Calendar.YEAR, 1990);
                                cal.set(Calendar.MONTH, 5);
                                cal.set(Calendar.DAY_OF_MONTH, 13);

                                long birthdate = cal.getTimeInMillis();

                                LifevitSDKUserData user = new LifevitSDKUserData(birthdate, 57, 162, LifevitSDKConstants.GENDER_FEMALE);

                                manager.bracelet2019SetUserInformation(user);
                                break;
                            }
                            case 19: {
                                LifevitSDKMonitoringAlarm alarm = new LifevitSDKMonitoringAlarm(8, 30, 18, 30, LifevitSDKSedentaryAlarm.SedentaryIntervals.PERIOD_30_MIN, true, true, true, true, true, false, false);
                                manager.bracelet2019ConfigureBraceletSedentaryAlarm(alarm);
                                break;
                            }
                            case 20: {
                                manager.bracelet2019DisableBraceletSedentaryAlarm();
                                break;
                            }
                            case 21:
                                manager.bracelet2019ConfigureAntitheft(true);
                                break;
                            case 22:
                                manager.bracelet2019ConfigureAntitheft(false);
                                break;
                            case 23:
                                manager.bracelet2019ConfigureRiseHand(true);
                                break;
                            case 24:
                                manager.bracelet2019ConfigureRiseHand(false);
                                break;
                            case 25:
                                manager.bracelet2019ConfigureAndroidPhone();
                                break;
                            case 26:
                                manager.bracelet2019ConfigureHeartRateIntervalSetting(55, 65, 80, 100);
                                break;
                            case 27:
                                manager.bracelet2019ConfigureHeartRateIntervalSetting(70, 120, 160, 220);
                                break;
                            case 28: {
                                LifevitSDKMonitoringAlarm monitoring = new LifevitSDKMonitoringAlarm(17, 30, 20, 30);
                                manager.bracelet2019ConfigureHeartRateMonitoring(true, true, monitoring);
                                break;
                            }
                            case 29:
                                manager.bracelet2019ConfigureFindPhone(true);
                                break;
                            case 30: {
                                LifevitSDKAppNotification notification = new LifevitSDKAppNotification();
                                notification.setAllNotifications(true);
                                manager.bracelet2019ConfigureACNS(notification);
                                break;
                            }
                            case 31: {
                                LifevitSDKAppNotification notification = new LifevitSDKAppNotification();
                                notification.setAllNotifications(false);
                                manager.bracelet2019ConfigureACNS(notification);
                                break;
                            }
                            case 32: {
                                LifevitSDKMonitoringAlarm monitoring = new LifevitSDKMonitoringAlarm(23, 00, 7, 30);
                                manager.bracelet2019ConfigureSleepMonitoring(true, monitoring);
                                break;
                            }
                            case 33:
                                manager.bracelet2019GetBattery();
                                break;
                            case 34:
                                manager.bracelet2019StartSynchronization();
                                break;
                            case 35:
                                manager.bracelet2019SendMessageReceived();
                                break;
                        }
                    }
                });
                builder.show();
            }
        });
    }

    private void initSdk() {

        // Create listener
        cl = new LifevitSDKDeviceListener() {

            @Override
            public void deviceOnConnectionError(int deviceType, final int errorCode) {
                if (deviceType != LifevitSDKConstants.DEVICE_BRACELET_AT2019) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (errorCode == LifevitSDKConstants.CODE_LOCATION_DISABLED) {
                            textview_connection_result.setText("ERROR: Debe activar permisos localización");
                        } else if (errorCode == LifevitSDKConstants.CODE_BLUETOOTH_DISABLED) {
                            textview_connection_result.setText("ERROR: El bluetooth no está activado");
                        } else if (errorCode == LifevitSDKConstants.CODE_LOCATION_TURN_OFF) {
                            textview_connection_result.setText("ERROR: La Ubicación está apagada");
                        } else {
                            textview_connection_result.setText("ERROR: Desconocido");
                        }
                    }
                });
            }

            @Override
            public void deviceOnConnectionChanged(int deviceType, final int status) {
                if (deviceType != LifevitSDKConstants.DEVICE_BRACELET_AT2019) {
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (status) {
                            case LifevitSDKConstants.STATUS_DISCONNECTED:
                                button_connect.setText("Connect");
                                isDisconnected = true;
                                textview_connection_result.setText("Disconnected");
                                textview_connection_result.setTextColor(ContextCompat.getColor(BraceletAT2019Activity.this, android.R.color.holo_red_dark));
                                break;
                            case LifevitSDKConstants.STATUS_SCANNING:
                                button_connect.setText("Stop scan");
                                isDisconnected = false;
                                textview_connection_result.setText("Scanning");
                                textview_connection_result.setTextColor(ContextCompat.getColor(BraceletAT2019Activity.this, android.R.color.holo_blue_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTING:
                                button_connect.setText("Disconnect");
                                isDisconnected = false;
                                textview_connection_result.setText("Connecting");
                                textview_connection_result.setTextColor(ContextCompat.getColor(BraceletAT2019Activity.this, android.R.color.holo_orange_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTED:
                                button_connect.setText("Disconnect");
                                isDisconnected = false;
                                textview_connection_result.setText("Connected");
                                textview_connection_result.setTextColor(ContextCompat.getColor(BraceletAT2019Activity.this, android.R.color.holo_green_dark));
                                break;
                        }
                    }
                });
            }
        };

        LifevitSDKBraceletAT2019Listener bListener = new LifevitSDKBraceletAT2019Listener() {


            @Override
            public void braceletCurrentStepsReceived(final LifevitSDKStepData steps) {
                synchronized (textview_info) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String text = textview_info.getText().toString();
                            text += "\n";
                            text += "Current steps: " + steps.getSteps()
                                    + ", calories: " + String.format("%.2f", steps.getCalories())
                                    + ", distance: " + String.format("%.2f", steps.getDistance());
                            textview_info.setText(text);

                            Log.d(TAG, "[braceletCurrentStepsReceived] " + text);
                        }
                    });
                }
            }

            @Override
            public void braceletStepsReceived(final List<LifevitSDKStepData> stepData) {
                synchronized (textview_info) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            String text = textview_info.getText().toString();
                            text += "\n";
                            text += "Sync Step Packets received: " + stepData.size();

                            // Print logs
                            DateFormat timeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            for (LifevitSDKStepData packet : stepData) {

                                String msg = "[Steps] Date: " + timeFormatter.format(packet.getDate()) + ", Steps:" + packet.getSteps()
                                        + ", Calories:" + packet.getCalories() + ", Distance:" + packet.getDistance();

                                text += "\n";
                                text += "---->" + msg;

                                Log.d(TAG, msg);
                            }

                            textview_info.setText(text);
                            Log.d(TAG, "[braceletStepsReceived] " + text);
                        }
                    });
                }
            }

            @Override
            public void braceletSummaryStepsReceived(final LifevitSDKSummaryStepData steps) {
                synchronized (textview_info) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String text = textview_info.getText().toString();
                            text += "\n";
                            text += "Current steps: " + steps.getSteps()
                                    + ", calories: " + String.format("%.2f", steps.getCalories())
                                    + ", distance: " + String.format("%.2f", steps.getDistance())
                                    + ", active time: " + steps.getActiveTime()
                                    + ", heart rate: " + steps.getHeartRate();
                            textview_info.setText(text);
                            Log.d(TAG, "[braceletSummaryStepsReceived] " + text);
                        }
                    });
                }
            }

            @Override
            public void braceletSummarySleepReceived(final LifevitSDKSummarySleepData sleep) {
                synchronized (textview_info) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String text = textview_info.getText().toString();
                            text += "\n";
                            text += "Current awakes: " + sleep.getAwakes()
                                    + ", total deep sleep minutes: " + sleep.getTotalDeepMinutes()
                                    + ", total light sleep minutes: " + sleep.getTotalLightMinutes();
                            textview_info.setText(text);
                            Log.d(TAG, "[braceletSummarySleepReceived] " + text);
                        }
                    });
                }
            }

            @Override
            public void braceletInformation(final Object message) {
                synchronized (textview_info) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (message != null) {

                                //if (message.getClass().isInstance("java.lang.String")) {
                                if (message instanceof String) {
                                    String text = textview_info.getText().toString();
                                    text += "\n";
                                    text += message;
                                    textview_info.setText(text);
                                    //} else if (message.getClass().isInstance("java.lang.Long")) {
                                } else if (message instanceof Long) {
                                    String text = textview_info.getText().toString();
                                    text += "\n";

                                    Date d = new Date((Long) message);

                                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    String strDate = dateFormat.format(d);

                                    text += dateFormat.format(d);

                                    textview_info.setText(text);
                                    Log.d(TAG, "[braceletInformation] " + text);
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void braceletError(final int errorCode) {
                synchronized (textview_info) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (errorCode == LifevitSDKConstants.CODE_WRONG_PARAMETERS) {
                                String text = textview_info.getText().toString();
                                text += "\n";
                                text += "Wrong parameters.";
                                textview_info.setText(text);
                                Log.d(TAG, "[braceletError] " + text);
                            }
                        }
                    });
                }
            }

            @Override
            public void braceletCurrentBattery(final int battery) {
                synchronized (textview_info) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String text = textview_info.getText().toString();
                            text += "\n";
                            text += "Current battery level: " + battery;
                            textview_info.setText(text);
                            Log.d(TAG, "[braceletCurrentBattery] " + text);
                        }
                    });
                }
            }

            @Override
            public void braceletDataReceived(final LifevitSDKBraceletData braceletData) {
                synchronized (textview_info) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "[braceletDataReceived]");

                            String text = textview_info.getText().toString();

                            ArrayList<LifevitSDKStepData> stepsData = braceletData.getStepsData();
                            if (!stepsData.isEmpty()) {

                                text += "\n";
                                text += "----- Received Activity (steps) packets: " + stepsData.size();
                                int i = 0;

                                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                while (i < stepsData.size()) {
                                    text += "\n";
                                    text += "[Steps] Index " + i
                                            + ", date: " + df.format(new Date(stepsData.get(i).getDate()))
                                            + ", Steps:" + stepsData.get(i).getSteps()
                                            + ", Calories:" + stepsData.get(i).getCalories()
                                            + ", Distance:" + stepsData.get(i).getDistance();
                                    i++;
                                }
                            }

                            ArrayList<LifevitSDKSleepData> sleepData = braceletData.getSleepData();
                            if (!sleepData.isEmpty()) {

                                text += "\n";
                                text += "----- Received Sleep packets: " + sleepData.size();
                                int i = 0;

                                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                while (i < sleepData.size()) {
                                    text += "\n";
                                    text += "[Sleep] Index " + i
                                            + ", date: " + df.format(new Date(sleepData.get(i).getDate()))
                                            + ", Deepness:" + sleepData.get(i).getSleepDeepness()
                                            + ", Duration:" + sleepData.get(i).getSleepDuration();
                                    i++;
                                }
                            }

                            ArrayList<LifevitSDKHeartbeatData> heartRateData = braceletData.getHeartData();
                            if (!heartRateData.isEmpty()) {

                                text += "\n";
                                text += "----- Received Heartbeat packets: " + heartRateData.size();
                                int i = 0;

                                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                while (i < heartRateData.size()) {
                                    text += "\n";
                                    text += "[Heartbeat] Index " + i
                                            + ", date: " + df.format(new Date(heartRateData.get(i).getDate()))
                                            + ", Heart Rate: " + heartRateData.get(i).getHeartrate();
                                    i++;
                                }
                            }

                            textview_info.setText(text);
                        }
                    });
                }
            }
        };

        // Create connection helper
        SDKTestApplication.getInstance().getLifevitSDKManager().addDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setBraceletAT2019Listener(bListener);
    }


}
