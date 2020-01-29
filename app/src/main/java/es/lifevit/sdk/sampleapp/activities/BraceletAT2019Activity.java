package es.lifevit.sdk.sampleapp.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import es.lifevit.sdk.LifevitSDKBleDeviceBraceletAT2019;
import es.lifevit.sdk.LifevitSDKConstants;
import es.lifevit.sdk.LifevitSDKManager;
import es.lifevit.sdk.LifevitSDKUserData;
import es.lifevit.sdk.bracelet.LifevitSDKAlarmTime;
import es.lifevit.sdk.bracelet.LifevitSDKAppNotification;
import es.lifevit.sdk.bracelet.LifevitSDKBraceletData;
import es.lifevit.sdk.bracelet.LifevitSDKHeartbeatData;
import es.lifevit.sdk.bracelet.LifevitSDKMonitoringAlarm;
import es.lifevit.sdk.bracelet.LifevitSDKSleepData;
import es.lifevit.sdk.bracelet.LifevitSDKStepData;
import es.lifevit.sdk.bracelet.LifevitSDKSummarySleepData;
import es.lifevit.sdk.bracelet.LifevitSDKSummaryStepData;
import es.lifevit.sdk.listeners.LifevitSDKBraceletAT2019Listener;
import es.lifevit.sdk.listeners.LifevitSDKDeviceListener;
import es.lifevit.sdk.sampleapp.R;
import es.lifevit.sdk.sampleapp.SDKTestApplication;

import static es.lifevit.sdk.LifevitSDKConstants.GENDER_MALE;
import static es.lifevit.sdk.bracelet.LifevitSDKSedentaryAlarm.SedentaryIntervals.PERIOD_30_MIN;

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
                        "2. Set device time",
                        "3. Get device time",
                        "4. Get current day data",
                        "5. Get steps data",
                        "6. Get sleep data",
                        "7. Get heart rate data",
                        "8. Get step historical data",
                        "9. Get sleep historical data",
                        "10. Get heart rate historical data",
                        "11. Set alarm at 10:05 every Tuesday",
                        "12. Set steps goal at 700 and sleep time at 8:05",
                        "13. Set user information",
                        "14. Set sedentary alarm f_10:00 t_21:30 e_wednesday for 30min",
                        "15. Set antitheft",
                        "16. Set left hand",
                        "17. Set iOS Phone",
                        "18. Set heart rate interval to 70/120/160 and 220",
                        "19. Set heart rate monitoring f17:30 t20:30",
                        "20. Set find phone",
                        "21. Set notification reminder",
                        "22. Set sleep monitoring reminder"
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(BraceletAT2019Activity.this);
                builder.setTitle("Select command");
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_GET_BASIC_INFO:
                                manager.bracelet2019GetBasicInfo();
                                break;
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_GET_FEATURE_LIST:
                                manager.bracelet2019GetFeatureList();
                                break;
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SET_TIME: {
                                Calendar cal = Calendar.getInstance();
                                Long time = cal.getTimeInMillis() / 1000;

                                manager.bracelet2019SetDeviceTime(time);
                                break;
                            }
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_GET_DEVICE_TIME:
                                manager.bracelet2019GetDeviceTime();
                                break;
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SYNC_DATA:
                                manager.bracelet2019SynchronizeData();
                                break;
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SYNC_SPORTS_DATA:
                                manager.bracelet2019SynchronizeSportsData();
                                break;
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SYNCHRONIZE_SLEEP_DATA:
                                manager.bracelet2019SynchronizeSleepData();
                                break;
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SYNCHRONIZE_HEART_RATE_DATA:
                                manager.bracelet2019SynchronizeHeartRateData();
                                break;
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SYNCHRONIZE_HISTORIC_SPORT_DATA:
                                manager.bracelet2019SynchronizeHistoricSportData();
                                break;
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SYNCHRONIZE_HISTORIC_SLEEP_DATA:
                                manager.bracelet2019SynchronizeHistoricSleepData();
                                break;
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SYNCHRONIZE_HISTORIC_HEART_RATE_DATA:
                                manager.bracelet2019SynchronizeHistoricHeartRateData();
                                break;
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_CONFIGURE_ALARM: {
                                LifevitSDKAlarmTime alarm = new LifevitSDKAlarmTime();

                                alarm.setHour(10);
                                alarm.setMinute(5);

                                manager.bracelet2019ConfigureAlarm(alarm);
                                break;
                            }
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SET_GOALS: {
                                LifevitSDKAlarmTime alarm = new LifevitSDKAlarmTime();
                                alarm.setHour(10);
                                alarm.setMinute(5);

                                int steps = 700;

                                manager.bracelet2019SetGoals(steps, alarm);
                                break;
                            }
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SET_USER_INFORMATION: {
                                Calendar cal = Calendar.getInstance();
                                cal.set(Calendar.YEAR, 1980);
                                cal.set(Calendar.MONTH, 12 - 1);
                                cal.set(Calendar.YEAR, 24);

                                long birthdate = cal.getTimeInMillis() / 1000;

                                LifevitSDKUserData user = new LifevitSDKUserData(birthdate, 85, 174, GENDER_MALE);

                                manager.bracelet2019SetUserInformation(user);
                                break;
                            }
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_CONFIGURE_BRACELET_SEDENTARY_ALARM: {
                                LifevitSDKMonitoringAlarm alarm = new LifevitSDKMonitoringAlarm(10, 0, 21, 30, PERIOD_30_MIN, false, false, true, false, false, false, false);
                                manager.bracelet2019ConfigureBraceletSedentaryAlarm(alarm);
                                break;
                            }
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_ANTITHEFT:
                                manager.bracelet2019ConfigureAntitheft(true);
                                break;

                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_RISE_HAND:

                                manager.bracelet2019ConfigureRiseHand(true);
                                break;

                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_BATTERY:
                                manager.bracelet2019GetBattery();
                                break;

                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_ANDROID_PHONE:
                                manager.bracelet2019ConfigureAndroidPhone();
                                break;

                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_FIND_PHONE:
                                manager.bracelet2019ConfigureFindPhone(true);
                                break;

                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_HEART_RATE_INTERVAL_SETTING:

                                manager.bracelet2019ConfigureHeartRateIntervalSetting(70, 120, 160, 220);
                                break;

                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_HEART_RATE_MONITORING: {
                                LifevitSDKMonitoringAlarm monitoring = new LifevitSDKMonitoringAlarm(17, 30, 20, 30);


                                manager.bracelet2019ConfigureHeartRateMonitoring(true, true, monitoring);
                                break;
                            }
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_ACNS: {
                                LifevitSDKAppNotification notification = new LifevitSDKAppNotification();
                                notification.setAlarm(true);

                                manager.bracelet2019ConfigureACNS(notification);
                                break;
                            }
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SLEEP_MONITORING: {
                                LifevitSDKMonitoringAlarm monitoring = new LifevitSDKMonitoringAlarm(23, 00, 7, 30);

                                manager.bracelet2019ConfigureSleepMonitoring(true, monitoring);
                                break;
                            }

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
