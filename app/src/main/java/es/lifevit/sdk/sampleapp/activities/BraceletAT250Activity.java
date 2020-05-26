package es.lifevit.sdk.sampleapp.activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import es.lifevit.sdk.LifevitSDKConstants;
import es.lifevit.sdk.LifevitSDKManager;
import es.lifevit.sdk.bracelet.LifevitSDKAT250TimeRange;
import es.lifevit.sdk.bracelet.LifevitSDKHeartbeatData;
import es.lifevit.sdk.bracelet.LifevitSDKSleepData;
import es.lifevit.sdk.bracelet.LifevitSDKStepData;
import es.lifevit.sdk.listeners.LifevitSDKBraceletAT250Listener;
import es.lifevit.sdk.listeners.LifevitSDKDeviceListener;
import es.lifevit.sdk.sampleapp.R;
import es.lifevit.sdk.sampleapp.SDKTestApplication;

public class BraceletAT250Activity extends AppCompatActivity {

    private static final String TAG = BraceletAT250Activity.class.getSimpleName();

    TextView textview_connection_result, textview_info;
    Button button_connect, button_command;
    boolean isDisconnected = true;
    private LifevitSDKDeviceListener cl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bracelet_at250);

        registerReceiver(firmwareUpdateReceiver, new IntentFilter(LifevitSDKConstants.AT250_DFU_BROADCAST_ACTION));

        initComponents();
        initListeners();
        initSdk();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_BRACELET_AT250)) {
            button_connect.setText("Disconnect");
            isDisconnected = false;
            textview_connection_result.setText("Connected");
            textview_connection_result.setTextColor(ContextCompat.getColor(BraceletAT250Activity.this, android.R.color.holo_green_dark));
        } else {
            button_connect.setText("Connect");
            isDisconnected = true;
            textview_connection_result.setText("Disconnected");
            textview_connection_result.setTextColor(ContextCompat.getColor(BraceletAT250Activity.this, android.R.color.holo_red_dark));
        }
    }

    private void initComponents() {
        textview_info = findViewById(R.id.bracelet_at250_textview_command_info);
        textview_info.setMovementMethod(new ScrollingMovementMethod());
        textview_connection_result = findViewById(R.id.bracelet_at250_connection_result);


        button_connect = findViewById(R.id.bracelet_at250_connect);
        button_command = findViewById(R.id.bracelet_at250_button_command);
    }


    private void initListeners() {

        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDisconnected) {

                    // Before connecting, set firmware update notification parameters
                    SDKTestApplication.getInstance().getLifevitSDKManager().braceletAT250SetFirmwareUpdateParameters("Firmware Update", R.drawable.ic_stat_notify_dfu,
                            "Firmware Update", "Firmware is Updating");

                    SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_BRACELET_AT250, 10000);

//                    SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_BRACELET_AT250, 10000);
//                    SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_BRACELET_AT250, 10000, "AA:AA:AA:AA:AA:AA");
//                    SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_BRACELET_AT250, 10000);
//                    SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_TENSIOMETER, 10000);
//                    SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_BRACELET_AT250, 10000);
//                    SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_BRACELET_AT250, 10000);
                } else {
                    SDKTestApplication.getInstance().getLifevitSDKManager().disconnectDevice(LifevitSDKConstants.DEVICE_BRACELET_AT250);
                }
            }
        });

        button_command.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final LifevitSDKManager manager = SDKTestApplication.getInstance().getLifevitSDKManager();


                CharSequence[] colors = new CharSequence[]{
                        "1. Set device date and time (21/10/2015 4:29)",
                        "2. Set device date and time (current time)",
                        "3. Set user info (65kg, 175cm, male, 30 years)",
                        "4. Set target steps (7000 steps)",
                        "5. Get yesterday's complete data",
                        "6. Start real time activity data",
                        "7. Sync HR History Values",
                        "8. Enable HR Monitoring",
                        "9. Disable HR Monitoring",
                        "10. Enable HR Real time",
                        "11. Disable HR Real time",
                        "12. Set Week days HR Monitoring",
                        "13. Update Firmware",
                        "14. Get Firmware version number"
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(BraceletAT250Activity.this);
                builder.setTitle("Select command");
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Calendar cal = Calendar.getInstance();
                                cal.set(2015, Calendar.OCTOBER, 21, 4, 29);
                                manager.braceletAT250SetDeviceDate(cal.getTime());
                                break;
                            case 1:
                                manager.braceletAT250SetDeviceDate(new Date());
                                break;
                            case 2:
                                manager.braceletAT250SetPersonalInfo(65, 175, LifevitSDKConstants.WEIGHT_SCALE_GENDER_MALE, 30);
                                break;
                            case 3:
                                manager.braceletAT250SetTargetSteps(7000);
                                break;
                            case 4:
                                manager.braceletAT250GetHistoryData(1);
                                break;
                            case 5:
                                manager.braceletAT250GetTodayData();
                                break;
                            case 6:
                                manager.braceletAT250GetHRData();
                                break;
                            case 7:
                                manager.braceletAT250SetMonitoringHR(true);
                                break;
                            case 8:
                                manager.braceletAT250SetMonitoringHR(false);
                                break;
                            case 9:
                                manager.braceletAT250SetRealtimeHR(true);
                                break;
                            case 10:
                                manager.braceletAT250SetRealtimeHR(false);
                                break;
                            case 11:
                                LifevitSDKAT250TimeRange timeRange = new LifevitSDKAT250TimeRange();
                                timeRange.setOnlyWeekDays();
                                manager.braceletAT250SetMonitoringHRAuto(true, timeRange);
                                break;
                            case 12:
                                manager.braceletAT250UpdateFirmware();
                                break;
                            case 13:
                                manager.braceletAT250GetFirmwareVersionNumber();
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
                if (deviceType != LifevitSDKConstants.DEVICE_BRACELET_AT250) {
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
                if (deviceType != LifevitSDKConstants.DEVICE_BRACELET_AT250) {
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
                                textview_connection_result.setTextColor(ContextCompat.getColor(BraceletAT250Activity.this, android.R.color.holo_red_dark));
                                break;
                            case LifevitSDKConstants.STATUS_SCANNING:
                                button_connect.setText("Stop scan");
                                isDisconnected = false;
                                textview_connection_result.setText("Scanning");
                                textview_connection_result.setTextColor(ContextCompat.getColor(BraceletAT250Activity.this, android.R.color.holo_blue_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTING:
                                button_connect.setText("Disconnect");
                                isDisconnected = false;
                                textview_connection_result.setText("Connecting");
                                textview_connection_result.setTextColor(ContextCompat.getColor(BraceletAT250Activity.this, android.R.color.holo_orange_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTED:
                                button_connect.setText("Disconnect");
                                isDisconnected = false;
                                textview_connection_result.setText("Connected");
                                textview_connection_result.setTextColor(ContextCompat.getColor(BraceletAT250Activity.this, android.R.color.holo_green_dark));
                                break;
                        }
                    }
                });
            }
        };

        LifevitSDKBraceletAT250Listener bListener = new LifevitSDKBraceletAT250Listener() {

            @Override
            public void braceletSyncReceived(final List<LifevitSDKStepData> stepsData, final List<LifevitSDKSleepData> sleepData) {
                synchronized (textview_info) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String text = textview_info.getText().toString();
                            text += "\n";
                            text += "Sync Step Packets received: " + stepsData.size();
                            text += "\n";
                            text += "Sync Sleep Packets received: " + sleepData.size();
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
            public void braceletHeartRateReceived(final int value) {
                synchronized (textview_info) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String text = textview_info.getText().toString();
                            text += "\n";
                            text += "HR received: " + value;
                            textview_info.setText(text);
                        }
                    });
                }
            }

            @Override
            public void braceletHeartRateSyncReceived(final List<LifevitSDKHeartbeatData> data) {
                synchronized (textview_info) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String text = textview_info.getText().toString();
                            text += "\n";
                            text += "Received heartdata packets: " + data.size();
                            int i = 0;

                            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            while (i < data.size() && i < 100) {
                                text += "\n";
                                text += "Index " + i + " heartdata : " + data.get(i).getHeartrate() + " date: "
                                        + df.format(new Date(data.get(i).getDate()));
                                i++;
                            }
                            textview_info.setText(text);
                        }
                    });
                }
            }

            @Override
            public void firmwareVersion(final String firmwareVersion) {
                synchronized (textview_info) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String text = textview_info.getText().toString();
                            text += "\n";
                            text += ("Firmware version: " + firmwareVersion);
                            textview_info.setText(text);
                        }
                    });
                }
            }

            @Override
            public void isGoingToUpdateFirmware(final boolean isGoingToUpdate) {
                synchronized (textview_info) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String text = textview_info.getText().toString();
                            text += "\n";
                            if (isGoingToUpdate) {
                                text += ("Is going to update firmware. Restarting device...");
                            } else {
                                text += ("Is not going to update firmware (device has last version)");
                            }
                            textview_info.setText(text);
                        }
                    });
                }
            }

            @Override
            public void operationFinished(final boolean finishedOk) {
                synchronized (textview_info) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String text = textview_info.getText().toString();
                            text += "\n";
                            text += ("Operation finished " + (finishedOk ? "OK" : "ERROR"));
                            textview_info.setText(text);
                        }
                    });
                }
            }

        };

        // Create connection helper
        SDKTestApplication.getInstance().getLifevitSDKManager().addDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setBraceletAT250Listener(bListener);
    }


    @Override
    protected void onStop() {
        super.onStop();
        SDKTestApplication.getInstance().getLifevitSDKManager().removeDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setBraceletAT250Listener(null);
    }


    @Override
    protected void onDestroy() {

        try {
            unregisterReceiver(firmwareUpdateReceiver);
        } catch (Exception e) {
            Log.e(TAG, "firmwareUpdateReceiver not registered");
        }

        super.onDestroy();
    }


    private Notification createProgressNotification(int state, final int progress) {

        final String deviceName = "AT-250HR";

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, LifevitSDKConstants.AT250_NOTIFICATION_CHANNEL_ID_DFU)
                .setSmallIcon(R.drawable.ic_stat_notify_dfu)
                .setOnlyAlertOnce(true);//.setLargeIcon(largeIcon);

        switch (state) {
            case LifevitSDKConstants.AT250_DFU_STATE_CONNECTING:
                builder.setOngoing(true).setContentTitle(getString(R.string.dfu_status_connecting)).setContentText(getString(R.string.dfu_status_connecting_msg, deviceName)).setProgress(100, 0, true);
                break;
            case LifevitSDKConstants.AT250_DFU_STATE_STARTING:
                builder.setOngoing(true).setContentTitle(getString(R.string.dfu_status_starting)).setContentText(getString(R.string.dfu_status_starting_msg)).setProgress(100, 0, true);
                break;
            case LifevitSDKConstants.AT250_DFU_STATE_ENABLING_DFU_MODE:
                builder.setOngoing(true).setContentTitle(getString(R.string.dfu_status_switching_to_dfu)).setContentText(getString(R.string.dfu_status_switching_to_dfu_msg))
                        .setProgress(100, 0, true);
                break;
            case LifevitSDKConstants.AT250_DFU_STATE_VALIDATING:
                builder.setOngoing(true).setContentTitle(getString(R.string.dfu_status_validating)).setContentText(getString(R.string.dfu_status_validating_msg)).setProgress(100, 0, true);
                break;
            case LifevitSDKConstants.AT250_DFU_STATE_DISCONNECTING:
                builder.setOngoing(true).setContentTitle(getString(R.string.dfu_status_disconnecting)).setContentText(getString(R.string.dfu_status_disconnecting_msg, deviceName))
                        .setProgress(100, 0, true);
                break;
            case LifevitSDKConstants.AT250_DFU_STATE_COMPLETED:
                builder.setOngoing(false).setContentTitle(getString(R.string.dfu_status_completed)).setSmallIcon(android.R.drawable.stat_sys_upload_done)
                        .setContentText(getString(R.string.dfu_status_completed_msg)).setAutoCancel(true);//.setColor(0xFF00B81A);
                break;
            case LifevitSDKConstants.AT250_DFU_STATE_ABORTED:
                builder.setOngoing(false).setContentTitle(getString(R.string.dfu_status_aborted)).setSmallIcon(android.R.drawable.stat_sys_upload_done)
                        .setContentText(getString(R.string.dfu_status_aborted_msg)).setAutoCancel(true);
                break;
            case LifevitSDKConstants.AT250_DFU_STATE_ERROR:
                builder.setOngoing(false).setContentTitle(getString(R.string.dfu_status_error)).setSmallIcon(android.R.drawable.stat_sys_upload_done)
                        .setContentText(getString(R.string.dfu_status_error_msg)).setAutoCancel(true);//.setColor(Color.RED);
            case LifevitSDKConstants.AT250_DFU_STATE_PROGRESS:
                builder.setOngoing(true).setContentTitle("Uploading firmware...").setContentText(progress + "%").setProgress(100, progress, false);
                break;
        }

        return builder.build();
    }


    private BroadcastReceiver firmwareUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {

            final int state = intent.getIntExtra(LifevitSDKConstants.AT250_DFU_BROADCAST_EXTRA_STATE, -1);
            final int progress = intent.getIntExtra(LifevitSDKConstants.AT250_DFU_BROADCAST_EXTRA_PROGRESS, -1);

            // 1. Update text view
            synchronized (textview_info) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String message = "";
                        switch (state) {
                            case LifevitSDKConstants.AT250_DFU_STATE_CONNECTING:
                                message = "- Firmware update: Connecting";
                                break;
                            case LifevitSDKConstants.AT250_DFU_STATE_STARTING:
                                message = "- Firmware update: Starting";
                                break;
                            case LifevitSDKConstants.AT250_DFU_STATE_ENABLING_DFU_MODE:
                                message = "- Firmware update: DFU mode";
                                break;
                            case LifevitSDKConstants.AT250_DFU_STATE_VALIDATING:
                                message = "- Firmware update: Validating";
                                break;
                            case LifevitSDKConstants.AT250_DFU_STATE_DISCONNECTING:
                                message = "- Firmware update: Disconnecting";
                                break;
                            case LifevitSDKConstants.AT250_DFU_STATE_COMPLETED:
                                message = "- Firmware update: Completed";
                                break;
                            case LifevitSDKConstants.AT250_DFU_STATE_ABORTED:
                                message = "- Firmware update: Aborted";
                                break;
                            case LifevitSDKConstants.AT250_DFU_STATE_ERROR:
                                message = "- Firmware update: ERROR";
                                break;
                            case LifevitSDKConstants.AT250_DFU_STATE_PROGRESS:
                                message = "- Firmware update: Progress: " + progress;
                                break;
                        }

                        String text = textview_info.getText().toString();
                        text += "\n";
                        text += message;
                        textview_info.setText(text);
                    }
                });
            }

            // 2. Notification
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel mChannel = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && mNotificationManager != null) {
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                mChannel = new NotificationChannel(LifevitSDKConstants.AT250_NOTIFICATION_CHANNEL_ID_DFU, "Firmware Update", importance);
                mChannel.setShowBadge(false);
                mChannel.setSound(null, null);
                mNotificationManager.createNotificationChannel(mChannel);
            }

            mNotificationManager.notify(LifevitSDKConstants.AT250_FOREGROUND_NOTIFICATION_ID, createProgressNotification(state, progress));
        }
    };


}
