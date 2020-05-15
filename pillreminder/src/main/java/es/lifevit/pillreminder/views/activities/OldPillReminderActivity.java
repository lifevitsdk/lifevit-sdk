package es.lifevit.pillreminder.views.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import es.lifevit.pillreminder.PRApplication;
import es.lifevit.pillreminder.R;
import es.lifevit.sdk.LifevitSDKConstants;
import es.lifevit.sdk.LifevitSDKManager;
import es.lifevit.sdk.listeners.LifevitSDKDeviceListener;
import es.lifevit.sdk.listeners.LifevitSDKPillReminderListener;
import es.lifevit.sdk.pillreminder.LifevitSDKPillReminderAlarmData;
import es.lifevit.sdk.pillreminder.LifevitSDKPillReminderAlarmListData;
import es.lifevit.sdk.pillreminder.LifevitSDKPillReminderData;
import es.lifevit.sdk.pillreminder.LifevitSDKPillReminderMessageData;
import es.lifevit.sdk.pillreminder.LifevitSDKPillReminderPerformanceData;

public class OldPillReminderActivity extends BaseAppCompatActivity {


    TextView textview_connection_result, /*textview_measurement_result, textview_measurement_info,*/ pillreminder_textview;
    Button button_connect, button_command;

    boolean isDisconnected = true;
    private LifevitSDKDeviceListener cl;

    int alarmsRequestCounter;
    int historyRecordsRequestCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pillreminder);

        initComponents();
        initListeners();

        alarmsRequestCounter = 0;
        historyRecordsRequestCounter = 0;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (PRApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_PILL_REMINDER)) {
            button_connect.setText("Disconnect");
            isDisconnected = false;
            textview_connection_result.setText("Connected");
            textview_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            button_command.setVisibility(View.VISIBLE);
        } else {
            button_connect.setText("Connect");
            isDisconnected = true;
            textview_connection_result.setText("Disconnected");
            textview_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            button_command.setVisibility(View.GONE);
        }

        initSdk();
    }


    private void initComponents() {
        textview_connection_result = findViewById(R.id.pillreminder_textview_connection_result);
        //textview_measurement_result = findViewById(R.id.pillreminder_textview_measurement_result);
        //textview_measurement_info = findViewById(R.id.pillreminder_textview_measurement_info);
        pillreminder_textview = findViewById(R.id.pillreminder_textView);

        button_connect = findViewById(R.id.pillreminder_button_connect);
        button_command = findViewById(R.id.pillreminder_button_command);
    }


    private void initListeners() {

        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDisconnected) {
                    PRApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_PILL_REMINDER, 60000);
                } else {
                    PRApplication.getInstance().getLifevitSDKManager().disconnectDevice(LifevitSDKConstants.DEVICE_PILL_REMINDER);
                }
            }
        });

        button_command.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isDisconnected) {
                    final LifevitSDKManager manager = PRApplication.getInstance().getLifevitSDKManager();

                    CharSequence[] colors = new CharSequence[]{
                            "0. Read device datetime",
                            "1. Get timezone",
                            "2. Get battery level",
                            "3. Get Latest Synchronization Time",
                            "4. Set Successful Synchronization Status",
                            "5. Clear Schedule Performance History",
                            "6. Get Alarm Schedule",
                            "7. Set Alarms Schedule 2 minutes from now",
                            "8. Get Schedule Performance History",
                            "9. Set Alarm Duration to 1 minute",
                            "10. Set Alarm Confirmation Time to 5 minutes",
                            "11. Clear Alarm Schedule"
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(OldPillReminderActivity.this);
                    builder.setTitle("Select command");
                     builder.setItems(colors, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    manager.prGetDeviceTime();
                                    break;
                                case 1:
                                    manager.prGetDeviceTimeZone();
                                    break;
                                case 2:
                                    manager.prGetBatteryLevel();
                                    break;
                                case 3:
                                    manager.prGetLatestSynchronizationTime();
                                    break;
                                case 4:
                                    manager.prSetSuccessfulSynchronizationStatus();
                                    break;
                                case 5:
                                    manager.prClearSchedulePerformanceHistory();
                                    break;
                                case 6:
                                    manager.prGetAlarmSchedule();
                                    break;
                                case 7:
                                    ArrayList<LifevitSDKPillReminderAlarmData> alarms = new ArrayList();

                                    //FIXME: FECHA FIJADA PARA COMPARAR CON LOS VALORES DE iOS
                                    long now = System.currentTimeMillis();
                                    //long now = 1559888240000L;

                                    LifevitSDKPillReminderAlarmData a1 = new LifevitSDKPillReminderAlarmData();
                                    a1.setDate(now + 60*1000);
                                    a1.setColor(LifevitSDKConstants.PILLREMINDER_COLOR_RED);
                                    alarms.add(a1);

                                    LifevitSDKPillReminderAlarmData a2 = new LifevitSDKPillReminderAlarmData();
                                    a2.setDate(now + 2*60*1000);
                                    a2.setColor(LifevitSDKConstants.PILLREMINDER_COLOR_BLUE);
                                    alarms.add(a2);

                                    LifevitSDKPillReminderAlarmData a3 = new LifevitSDKPillReminderAlarmData();
                                    a3.setDate(now + 4*60*1000);
                                    a3.setColor(LifevitSDKConstants.PILLREMINDER_COLOR_GREEN);
                                    alarms.add(a3);

                                    LifevitSDKPillReminderAlarmData a4 = new LifevitSDKPillReminderAlarmData();
                                    a4.setDate(now + 10*60*1000);
                                    a4.setColor(LifevitSDKConstants.PILLREMINDER_COLOR_PURPLE);
                                    alarms.add(a4);

                                    LifevitSDKPillReminderAlarmData a5 = new LifevitSDKPillReminderAlarmData();
                                    a5.setDate(now + 2*60*1000);
                                    a5.setColor(LifevitSDKConstants.PILLREMINDER_COLOR_YELLOW);
                                    alarms.add(a5);

                                    LifevitSDKPillReminderAlarmData a6 = new LifevitSDKPillReminderAlarmData();
                                    a6.setDate(now + 7*60*1000);
                                    a6.setColor(LifevitSDKConstants.PILLREMINDER_COLOR_RED);
                                    alarms.add(a6);



                                    manager.prSetAlarmSchedule(alarms);
                                    break;
                                case 8:
                                    manager.prGetSchedulePerformanceHistory();
                                    break;
                                case 9:
                                    manager.prSetAlarmDuration(1);
                                    break;
                                case 10:
                                    manager.prSetAlarmConfirmationTime(5);
                                    break;
                                case 11:
                                    manager.prClearAlarmSchedule();
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

        // Create connection listener
        cl = new LifevitSDKDeviceListener() {

            @Override
            public void deviceOnConnectionError(int deviceType, final int errorCode) {
                if (deviceType != LifevitSDKConstants.DEVICE_PILL_REMINDER) {
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
                if (deviceType != LifevitSDKConstants.DEVICE_PILL_REMINDER) {
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
                                textview_connection_result.setTextColor(ContextCompat.getColor(OldPillReminderActivity.this, android.R.color.holo_red_dark));
                                button_command.setVisibility(View.GONE);
                                break;
                            case LifevitSDKConstants.STATUS_SCANNING:
                                button_connect.setText("Stop scan");
                                isDisconnected = false;
                                textview_connection_result.setText("Scanning");
                                textview_connection_result.setTextColor(ContextCompat.getColor(OldPillReminderActivity.this, android.R.color.holo_blue_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTING:
                                button_connect.setText("Disconnect");
                                isDisconnected = false;
                                textview_connection_result.setText("Connecting");
                                textview_connection_result.setTextColor(ContextCompat.getColor(OldPillReminderActivity.this, android.R.color.holo_orange_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTED:
                                button_connect.setText("Disconnect");
                                isDisconnected = false;
                                textview_connection_result.setText("Connected");
                                textview_connection_result.setTextColor(ContextCompat.getColor(OldPillReminderActivity.this, android.R.color.holo_green_dark));
                                button_command.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                });
            }
        };

        LifevitSDKPillReminderListener pillReminderListener = new LifevitSDKPillReminderListener() {
            @Override
            public void pillReminderOnResult(final Object info) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String text = pillreminder_textview.getText().toString();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                        if (info instanceof LifevitSDKPillReminderMessageData) {

                            LifevitSDKPillReminderMessageData message = (LifevitSDKPillReminderMessageData) info;

                            switch (message.getRequest()) {
                                case LifevitSDKConstants.PILLREMINDER_REQUEST_GET_BATTERYLEVEL:
                                    text += "\n";
                                    text += "Battery level: " + message.getMessageText()+ "\n";
                                    break;
                                default:
                                    text += "\n";
                                    text += message.getMessageText()+ "\n";
                                    break;
                            }

                            pillreminder_textview.setText(text);
                        } else if (info instanceof LifevitSDKPillReminderAlarmListData) {
                            LifevitSDKPillReminderAlarmListData data = (LifevitSDKPillReminderAlarmListData) info;

                            switch (data.getRequest()) {
                                case LifevitSDKConstants.PILLREMINDER_REQUEST_GET_ALARMSCHEDULE:

                                    if (alarmsRequestCounter == 0) {
                                        text += "\n";
                                        text += "Alarm schedule request successful:\n";
                                    }

                                    if (data.getAlarmList() != null) {

                                        ArrayList<LifevitSDKPillReminderAlarmData> alarmList= (ArrayList<LifevitSDKPillReminderAlarmData>) data.getAlarmList();

                                        for (LifevitSDKPillReminderAlarmData alarm : alarmList) {

                                            Date d = new Date(alarm.getDate());
                                            text += dateFormat.format(d) + "\n";
                                        }
                                    } else {
                                        alarmsRequestCounter = 0;
                                    }

                                    break;

                                case LifevitSDKConstants.PILLREMINDER_REQUEST_GET_SCHEDULEPERFORMANCEHISTORY:

                                    if (historyRecordsRequestCounter == 0) {
                                        text += "\n";
                                        text += "Schedule performance history request successful:\n";
                                    }

                                    if (data.getAlarmList() != null) {

                                        ArrayList<LifevitSDKPillReminderPerformanceData> records= (ArrayList<LifevitSDKPillReminderPerformanceData>) data.getAlarmList();

                                        for (LifevitSDKPillReminderPerformanceData record : records) {
                                            Date d = new Date(record.getDate());

                                            Date dTaken = new Date(record.getDate());
                                            text += dateFormat.format(d) +" - status: "+ record.getStatusTaken()+" ("+dateFormat.format(dTaken) + ")\n";
                                        }
                                    } else {
                                        historyRecordsRequestCounter = 0;
                                    }

                                    break;
                                default:
                                    break;
                            }

                            pillreminder_textview.setText(text);

                        } else if (info instanceof LifevitSDKPillReminderPerformanceData) {
                            LifevitSDKPillReminderPerformanceData record = (LifevitSDKPillReminderPerformanceData) info;
                            text += "\n";
                            text += "Real-time performance received:\n";

                            Date d = new Date(record.getDate());

                            text += dateFormat.format(d) + " - status: " + record.getStatusTaken() + "\n";
                            pillreminder_textview.setText(text);

                        } else if (info instanceof LifevitSDKPillReminderData) {
                            LifevitSDKPillReminderData data = (LifevitSDKPillReminderData) info;
                            Date d = new Date(data.getDate());

                            switch (data.getRequest()) {
                                case LifevitSDKConstants.PILLREMINDER_REQUEST_GET_DEVICETIME:
                                    text += "\n";

                                    text += "Device date: " + dateFormat.format(d) + "\n";
                                    break;

                                case LifevitSDKConstants.PILLREMINDER_REQUEST_GET_LATESTSYNCHRONIZATIONTIME:
                                    text += "\n";

                                    Long dl = data.getDate();

                                    if (dl != null){
                                        text += "Latest synchronization time: " + dateFormat.format(d) + "\n";
                                    } else {
                                        text += "Device not synchronized\n";
                                    }

                                    break;
                            }
                            pillreminder_textview.setText(text);
                        }

                    }
                });
            }

            @Override
            public void pillReminderOnError(final LifevitSDKPillReminderMessageData info) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String text = pillreminder_textview.getText().toString();
                        text += "\n";
                        text += info.getMessageText() + "\n";

                        pillreminder_textview.setText(text);

                    }
                });
            }

        };

        // Create connection helper
        PRApplication.getInstance().getLifevitSDKManager().addDeviceListener(cl);
        PRApplication.getInstance().getLifevitSDKManager().setPillReminderListener(pillReminderListener);
    }


    @Override
    protected void onStop() {
        super.onStop();
        PRApplication.getInstance().getLifevitSDKManager().removeDeviceListener(cl);
        PRApplication.getInstance().getLifevitSDKManager().setPillReminderListener(null);
    }


}
