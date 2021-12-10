package es.lifevit.sdk.sampleapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
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
import es.lifevit.sdk.bracelet.LifevitSDKAT500SedentaryReminderTimeRange;
import es.lifevit.sdk.bracelet.LifevitSDKAt500HrAlarmTime;
import es.lifevit.sdk.bracelet.LifevitSDKHeartbeatData;
import es.lifevit.sdk.bracelet.LifevitSDKSleepData;
import es.lifevit.sdk.bracelet.LifevitSDKStepData;
import es.lifevit.sdk.listeners.LifevitSDKBraceletListener;
import es.lifevit.sdk.listeners.LifevitSDKDeviceListener;
import es.lifevit.sdk.sampleapp.R;
import es.lifevit.sdk.sampleapp.SDKTestApplication;

public class BraceletAT500HrActivity extends AppCompatActivity {


    private static String TAG = BraceletAT500HrActivity.class.getSimpleName();


    TextView textview_connection_result, textview_info;
    Button button_connect, button_command;
    boolean isDisconnected = true;
    private LifevitSDKDeviceListener cl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bracelet);

        initComponents();
        initListeners();
        initSdk();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_BRACELET_AT500HR)) {
            button_connect.setText("Disconnect");
            isDisconnected = false;
            textview_connection_result.setText("Connected");
            textview_connection_result.setTextColor(ContextCompat.getColor(BraceletAT500HrActivity.this, android.R.color.holo_green_dark));
        } else {
            button_connect.setText("Connect");
            isDisconnected = true;
            textview_connection_result.setText("Disconnected");
            textview_connection_result.setTextColor(ContextCompat.getColor(BraceletAT500HrActivity.this, android.R.color.holo_red_dark));
        }

        initSdk();
    }

    private void initComponents() {
        textview_info = findViewById(R.id.textview_command_info);
        textview_info.setMovementMethod(new ScrollingMovementMethod());
        textview_connection_result = findViewById(R.id.bracelet_connection_result);
        button_connect = findViewById(R.id.bracelet_connect);
        button_command = findViewById(R.id.button_command);
    }


    private void initListeners() {

        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDisconnected) {
                    SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_BRACELET_AT500HR, 120000);
                } else {
                    SDKTestApplication.getInstance().getLifevitSDKManager().disconnectDevice(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
                }
            }
        });

        button_command.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final LifevitSDKManager manager = SDKTestApplication.getInstance().getLifevitSDKManager();

                if (SDKTestApplication.getInstance().getLifevitSDKManager().isUserRunning()) {
                    CharSequence[] colors = new CharSequence[]{"1. Stop Activity",
                            "2. Get version",
                            "3. Get battery"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(BraceletAT500HrActivity.this);
                    builder.setTitle("Select command");
                    builder.setItems(colors, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    manager.setBraceletActivity(false);
                                    break;
                                case 1:
                                    manager.getBraceletVersion();
                                    break;
                                case 2:
                                    manager.getBraceletBattery();
                                    break;
                            }
                        }
                    });
                    builder.show();
                } else {
                    CharSequence[] colors = new CharSequence[]{
                            "1. Set User Height (190cm)",
                            "2. Set User Weight (90kg)",
                            "3. Set current date",
                            "4. Set date: 21/10/2015 4:29",
                            "5. Configure ALL notifications",
                            "6. Configure NO notifications",
                            "7. Configure NO Hand",
                            "8. Configure LEFT Hand",
                            "9. Configure RIGHT Hand",
                            "10. Activate Antitheft",
                            "11. Deactivate Antitheft ",
                            "12. Activate Monitor HR",
                            "13. Deactivate Monitor HR",
                            "14. Activate Find Phone",
                            "15. Deactivate Find Phone",
                            "16. Get current steps",
                            "17. Get Heartbeat",
                            "18. Sync History",
                            "19. Start Activity",
                            "20. Bind bracelet",
                            "21. Activate bracelet",
                            "22. Get version",
                            "23. Get battery",
                            "24. Set distance unit: Km",
                            "25. Set distance unit: miles",
                            "26. Set sedentary reminder enabled (8:30 to 22:00, every 30 minutes)",
                            "27. Set sedentary reminder disabled",
                            "28. Set primary alarm to 7:30 monday-friday",
                            "29. Set secondary alarm today in 5 minutes",
                            "30. Deactivate primary alarm",
                            "31. Deactivate secondary alarm",
                            "32. Disable camera",
                            "33. Enable camera"
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(BraceletAT500HrActivity.this);
                    builder.setTitle("Select command");
                    builder.setItems(colors, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    manager.setBraceletUserHeight(190);
                                    break;
                                case 1:
                                    manager.setBraceletUserWeight(90);
                                    break;
                                case 2:
                                    manager.setBraceletDate(new Date());
                                    break;
                                case 3:
                                    Calendar cal = Calendar.getInstance();
                                    cal.set(2015, Calendar.OCTOBER, 21, 4, 29);
                                    manager.setBraceletDate(cal.getTime());
                                    break;
                                case 4:
                                    ArrayList<Integer> notifications = new ArrayList<Integer>();
                                    notifications.add(LifevitSDKConstants.SNS_TYPE_CALL);
                                    notifications.add(LifevitSDKConstants.SNS_TYPE_EMAIL);
                                    notifications.add(LifevitSDKConstants.SNS_TYPE_FACEBOOK);
                                    notifications.add(LifevitSDKConstants.SNS_TYPE_INSTAGRAM);
                                    notifications.add(LifevitSDKConstants.SNS_TYPE_LINE);
                                    notifications.add(LifevitSDKConstants.SNS_TYPE_QQ);
                                    notifications.add(LifevitSDKConstants.SNS_TYPE_SKYPE);
                                    notifications.add(LifevitSDKConstants.SNS_TYPE_SMS);
                                    notifications.add(LifevitSDKConstants.SNS_TYPE_TWITTER);
                                    notifications.add(LifevitSDKConstants.SNS_TYPE_WHATSAPP);
                                    notifications.add(LifevitSDKConstants.SNS_TYPE_WECHAT);
                                    manager.enableBraceletNotifications(notifications);
                                    break;
                                case 5:
                                    manager.enableBraceletNotifications(new ArrayList<Integer>());
                                    break;
                                case 6:
                                    manager.setBraceletArm(LifevitSDKConstants.BRACELET_HAND_NONE);
                                    break;
                                case 7:
                                    manager.setBraceletArm(LifevitSDKConstants.BRACELET_HAND_LEFT);
                                    break;
                                case 8:
                                    manager.setBraceletArm(LifevitSDKConstants.BRACELET_HAND_RIGHT);
                                    break;
                                case 9:
                                    manager.enableBraceletAntilost(true);
                                    break;
                                case 10:
                                    manager.enableBraceletAntilost(false);
                                    break;
                                case 11:
                                    manager.enableBraceletMonitorHR(true);
                                    break;
                                case 12:
                                    manager.enableBraceletMonitorHR(false);
                                    break;
                                case 13:
                                    manager.enableBraceletFindPhone(true);
                                    break;
                                case 14:
                                    manager.enableBraceletFindPhone(false);
                                    break;
                                case 15:
                                    manager.getBraceletCurrentSteps();
                                    break;
                                case 16:
                                    manager.getBraceletHeartBeat();
                                    break;
                                case 17:
                                    manager.getBraceletHistorySync();
                                    break;
                                case 18:
                                    manager.setBraceletActivity(true);
                                    break;
                                case 19:
                                    manager.bindBracelet();
                                    break;
                                case 20:
                                    manager.activateBracelet();
                                    break;
                                case 21:
                                    manager.getBraceletVersion();
                                    break;
                                case 22:
                                    manager.getBraceletBattery();
                                    break;
                                case 23:
                                    manager.setBraceletDistanceUnit(LifevitSDKConstants.BRACELET_DISTANCE_KM);
                                    break;
                                case 24:
                                    manager.setBraceletDistanceUnit(LifevitSDKConstants.BRACELET_DISTANCE_MILES);
                                    break;
                                case 25:
                                    manager.setBraceletSedentaryReminderEnabled(new LifevitSDKAT500SedentaryReminderTimeRange(8, 30,
                                            22, 0, LifevitSDKAT500SedentaryReminderTimeRange.SedentaryIntervals.PERIOD_30_MIN));
                                    break;
                                case 26:
                                    manager.setBraceletSedentaryReminderDisabled();
                                    break;
                                case 27:
                                    manager.setBraceletAlarm(new LifevitSDKAt500HrAlarmTime(false, 7, 30,
                                            true, true, true, true, true, false, false));
                                    break;
                                case 28:
                                    Calendar alarmCal = Calendar.getInstance();
                                    alarmCal.add(Calendar.MINUTE, 5);
                                    int alarmCalDayOfWeek = alarmCal.get(Calendar.DAY_OF_WEEK);
                                    manager.setBraceletAlarm(new LifevitSDKAt500HrAlarmTime(true, alarmCal.get(Calendar.HOUR_OF_DAY), alarmCal.get(Calendar.MINUTE),
                                            alarmCalDayOfWeek == Calendar.MONDAY, alarmCalDayOfWeek == Calendar.TUESDAY, alarmCalDayOfWeek == Calendar.WEDNESDAY,
                                            alarmCalDayOfWeek == Calendar.THURSDAY, alarmCalDayOfWeek == Calendar.FRIDAY, alarmCalDayOfWeek == Calendar.SATURDAY,
                                            alarmCalDayOfWeek == Calendar.SUNDAY));
//                                    manager.setBraceletAlarm(new LifevitSDKAt500HrAlarmTime(false, alarmCal.get(Calendar.HOUR_OF_DAY), alarmCal.get(Calendar.MINUTE),
//                                            true, true, true,
//                                            true, true, true,
//                                            true));
                                    break;
                                case 29:
                                    manager.disableBraceletAlarm(false);
                                    break;
                                case 30:
                                    manager.disableBraceletAlarm(true);
                                    break;
                                case 31:
                                    manager.enableCamera(false);
                                    break;
                                case 32:
                                    manager.enableCamera(true);
                                    break;
                            }
                        }
                    });
                    builder.show();
                }
            }
        });
    }

    private void initSdk() {

        // Create listener
        cl = new LifevitSDKDeviceListener() {

            @Override
            public void deviceOnConnectionError(int deviceType, final int errorCode) {
                if (deviceType != LifevitSDKConstants.DEVICE_BRACELET) {
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
                if (deviceType != LifevitSDKConstants.DEVICE_BRACELET) {
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
                                textview_connection_result.setTextColor(ContextCompat.getColor(BraceletAT500HrActivity.this, android.R.color.holo_red_dark));
                                break;
                            case LifevitSDKConstants.STATUS_SCANNING:
                                button_connect.setText("Stop scan");
                                isDisconnected = false;
                                textview_connection_result.setText("Scanning");
                                textview_connection_result.setTextColor(ContextCompat.getColor(BraceletAT500HrActivity.this, android.R.color.holo_blue_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTING:
                                button_connect.setText("Disconnect");
                                isDisconnected = false;
                                textview_connection_result.setText("Connecting");
                                textview_connection_result.setTextColor(ContextCompat.getColor(BraceletAT500HrActivity.this, android.R.color.holo_orange_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTED:
                                button_connect.setText("Disconnect");
                                isDisconnected = false;
                                textview_connection_result.setText("Connected");
                                textview_connection_result.setTextColor(ContextCompat.getColor(BraceletAT500HrActivity.this, android.R.color.holo_green_dark));
                                break;
                        }
                    }
                });
            }
        };

        LifevitSDKBraceletListener bListener = new LifevitSDKBraceletListener() {

            @Override
            public void braceletActivityStarted() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String text = textview_info.getText().toString();
                        text += "\n";
                        text += "STARTED activity";
                        textview_info.setText(text);
                    }
                });
            }

            @Override
            public void braceletActivityFinished() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String text = textview_info.getText().toString();
                        text += "\n";
                        text += "FINISHED activity";
                        textview_info.setText(text);
                    }
                });
            }

            @Override
            public void braceletCurrentBattery(final int battery) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String text = textview_info.getText().toString();
                        text += "\n";
                        text += "BATERIA: " + battery + "%";
                        textview_info.setText(text);
                    }
                });
            }

            @Override
            public void braceletBeepReceived() {
                synchronized (textview_info) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            String text = textview_info.getText().toString();
                            text += "\n";
                            text += "BEEP RECEIVED (make whatever in your app)";
                            textview_info.setText(text);
                        }
                    });
                }
            }

            @Override
            public void braceletParameterSet(final int parameter) {
                synchronized (textview_info) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String text = textview_info.getText().toString();
                            text += "\n";
                            switch (parameter) {
                                case LifevitSDKConstants.BRACELET_PARAM_ANTILOST:
                                    text += "Antilost set";
                                    break;
                                case LifevitSDKConstants.BRACELET_PARAM_ACNS:
                                    text += "ACNS set";
                                    break;
                                case LifevitSDKConstants.BRACELET_PARAM_DATE:
                                    text += "Date set";
                                    break;
                                case LifevitSDKConstants.BRACELET_PARAM_FIND_DEVICE:
                                    text += "FindDevice set";
                                    break;
                                case LifevitSDKConstants.BRACELET_PARAM_FIND_PHONE:
                                    text += "FindPhone set";
                                    break;
                                case LifevitSDKConstants.BRACELET_PARAM_HANDS:
                                    text += "Hands set";
                                    break;
                                case LifevitSDKConstants.BRACELET_PARAM_HEIGHT:
                                    text += "User height set";
                                    break;
                                case LifevitSDKConstants.BRACELET_PARAM_HRMONITOR:
                                    text += "HRMonitor set";
                                    break;
                                case LifevitSDKConstants.BRACELET_PARAM_CAMERA:
                                    text += "HRMonitor set";
                                    break;
                                case LifevitSDKConstants.BRACELET_PARAM_TARGET:
                                    text += "Target set";
                                    break;
                                case LifevitSDKConstants.BRACELET_PARAM_WEIGHT:
                                    text += "User weight set";
                                    break;
                                case LifevitSDKConstants.BRACELET_PARAM_DISTANCE_UNIT:
                                    text += "Bracelet distance unit set";
                                    break;
                                case LifevitSDKConstants.BRACELET_PARAM_SIT:
                                    text += "Bracelet sit reminders set";
                                    break;
                                case LifevitSDKConstants.BRACELET_PARAM_ALARM_1:
                                    text += "Bracelet alarm 1 set";
                                    break;
                                case LifevitSDKConstants.BRACELET_PARAM_ALARM_2:
                                    text += "Bracelet alarm 2 set";
                                    break;
                            }
                            textview_info.setText(text);
                        }
                    });
                }
            }

            @Override
            public void braceletSyncStepsReceived(final List<LifevitSDKStepData> data) {
                synchronized (textview_info) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            String text = textview_info.getText().toString();
                            text += "\n";
                            text += "Sync Step Packets received: " + data.size();
                            textview_info.setText(text);

                            // Print logs
                            DateFormat timeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            for (LifevitSDKStepData packet : data) {
                                Log.d(TAG, "[Steps] Date: " + timeFormatter.format(packet.getDate()) + ", Steps:" + packet.getSteps()
                                        + ", Calories:" + packet.getCalories() + ", Distance:" + packet.getDistance());
                            }
                        }
                    });
                }
            }

            @Override
            public void braceletSyncSleepReceived(final List<LifevitSDKSleepData> data) {
                synchronized (textview_info) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String text = textview_info.getText().toString();
                            text += "\n";
                            text += "Sync Sleep Packets received: " + data.size();
                            textview_info.setText(text);

                            // Print logs
                            DateFormat timeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            for (LifevitSDKSleepData packet : data) {
                                Log.d(TAG, "[Sleep] Date: " + timeFormatter.format(packet.getDate()) + ", Duration:" + packet.getSleepDuration()
                                        + ", Deepness:" + packet.getSleepDeepness());
                            }
                        }
                    });
                }
            }

            @Override
            public void braceletSyncHeartReceived(final List<LifevitSDKHeartbeatData> data) {
                synchronized (textview_info) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String text = textview_info.getText().toString();
                            text += "\n";
                            text += "Sync HeartBeat Packets received: " + data.size();
                            textview_info.setText(text);

                            // Print logs
                            DateFormat timeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            for (LifevitSDKHeartbeatData packet : data) {
                                Log.d(TAG, "[Heart Rate] Date: " + timeFormatter.format(packet.getDate()) + ", HeartRate:" + packet.getHeartRate());
                            }
                        }
                    });
                }
            }

            @Override
            public void braceletActivityStepsReceived(final int steps) {
                synchronized (textview_info) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String text = textview_info.getText().toString();
                            text += "\n";
                            text += "Current activity steps: " + steps;
                            textview_info.setText(text);
                        }
                    });
                }
            }

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
                        }
                    });
                }
            }

            @Override
            public void braceletHeartDataReceived(final int heartrate) {

                synchronized (textview_info) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String text = textview_info.getText().toString();
                            text += "\n";
                            text += "Current heartrate: " + heartrate;
                            textview_info.setText(text);
                        }
                    });
                }
            }

            @Override
            public void braceletInfoReceived(final String info) {
                synchronized (textview_info) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String text = textview_info.getText().toString();
                            text += "\n";
                            text += info;
                            textview_info.setText(text);
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
                            if (errorCode == LifevitSDKConstants.CODE_NOTIFICATION_ACCESS) {
                                String text = textview_info.getText().toString();
                                text += "\n";
                                text += "You have to enable this App to access notifications.";
                                textview_info.setText(text);

                                // --- Show alert dialog to go to Notifications Settings ---
                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(BraceletAT500HrActivity.this);
                                builder.setTitle("");
                                builder.setMessage("You have to enable this App to access notifications. Do you want to give permissions to this App?");

                                builder.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        // Go to settings
                                        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                                    }
                                });

                                builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                });

                                final android.app.AlertDialog dialog = builder.create();
                                dialog.show();

                            }
                        }
                    });
                }
            }
        };

        // Create connection helper
        SDKTestApplication.getInstance().getLifevitSDKManager().addDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setBraceletListener(bListener);
    }


    @Override
    protected void onStop() {
        super.onStop();
        SDKTestApplication.getInstance().getLifevitSDKManager().removeDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setBraceletListener(null);
    }

}
