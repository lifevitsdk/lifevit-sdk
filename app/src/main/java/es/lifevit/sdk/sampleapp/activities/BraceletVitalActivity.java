package es.lifevit.sdk.sampleapp.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import es.lifevit.sdk.LifevitSDKBleDeviceBraceletVital;
import es.lifevit.sdk.LifevitSDKBraceletParams;
import es.lifevit.sdk.LifevitSDKConstants;
import es.lifevit.sdk.LifevitSDKManager;
import es.lifevit.sdk.LifevitSDKUserData;
import es.lifevit.sdk.bracelet.LifevitSDKVitalActivityPeriod;
import es.lifevit.sdk.bracelet.LifevitSDKVitalAlarms;
import es.lifevit.sdk.bracelet.LifevitSDKVitalPeriod;
import es.lifevit.sdk.bracelet.LifevitSDKVitalWeather;
import es.lifevit.sdk.bracelet.LifevitSDKVitalScreenNotification;
import es.lifevit.sdk.listeners.LifevitSDKBraceletVitalListener;
import es.lifevit.sdk.listeners.LifevitSDKDeviceListener;
import es.lifevit.sdk.sampleapp.R;
import es.lifevit.sdk.sampleapp.SDKTestApplication;


public class BraceletVitalActivity extends AppCompatActivity {

    private static final String TAG = BraceletVitalActivity.class.getSimpleName();

    TextView textview_connection_result, textview_info;
    Button button_connect, button_command;
    boolean isDisconnected = true;
    private LifevitSDKDeviceListener cl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bracelet_vital);

        initComponents();
        initListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_BRACELET_VITAL)) {
            button_connect.setText("Disconnect");
            isDisconnected = false;
            textview_connection_result.setText("Connected");
            textview_connection_result.setTextColor(ContextCompat.getColor(BraceletVitalActivity.this, android.R.color.holo_green_dark));
        } else {
            button_connect.setText("Connect");
            isDisconnected = true;
            textview_connection_result.setText("Disconnected");
            textview_connection_result.setTextColor(ContextCompat.getColor(BraceletVitalActivity.this, android.R.color.holo_red_dark));
        }
        initSdk();
    }


    @Override
    protected void onPause() {
        super.onPause();
        SDKTestApplication.getInstance().getLifevitSDKManager().removeDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setBraceletVitalListener(null);
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
                    SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_BRACELET_VITAL, 60000);
                } else {
                    SDKTestApplication.getInstance().getLifevitSDKManager().disconnectDevice(LifevitSDKConstants.DEVICE_BRACELET_VITAL);
                }
            }
        });

        button_command.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final LifevitSDKManager manager = SDKTestApplication.getInstance().getLifevitSDKManager();

                CharSequence[] colors = new CharSequence[]{
                        "0. Unlock QR Code",
                        "1. Show QR Code",
                        "2. Set device time (current time)",
                        "3. Set device time (21/10/2015 4:29)",
                        "4. Get device time",
                        "5. Set user information",
                        "6. Get User information",
                        "7. Set Device Parameters",
                        "8. Get Device Parameters",
                        "9. Get MAC Address",
                        "10. Set Step Goal",
                        "11. Get Step Goal",
                        "12. Get Device Battery",
                        "13. Get Blood Oxygen Data",
                        "14. Get Automatic Blood Oxygen Data",
                        "15. Set Automatic Blood Oxygen Data Period",
                        "16. Get Automatic Blood Oxygen Data Period",
                        "17. Get Temperature Data",
                        "18. Get Automatic Temperature Data",
                        "19. Get Heart Rate Data",
                        "20. Get Single Heart Rate Data",
                        "21. Set Automatic Heart Rate Detection Data",
                        "22. Start ECG",
                        "23. ECG Measurement Status",
                        "24. Get ECG Waveform",
                        "25. Get HRV Data",
                        "26. Set Realtime Step counting ON",
                        "27. Set Realtime Step counting OFF",
                        "28. Get Total Step Data",
                        "29. Get Detailed Step Data",
                        "30. Get Detailed Sleep Data",
                        "31. Set Activity Period",
                        "32. Get Activity Period",
                        "33. Set Sport Mode",
                        "34. Start HRV",
                        "35. Start HeartRate",
                        "36. Start Blood Oxygen Test",
                        "37. Get sports Data",
                        "38. Set Alarms",
                        "39. Set screen notification",
                        "40. Set weather",
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(BraceletVitalActivity.this);
                builder.setTitle("Select command");
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                manager.showVitalQR(false);
                                break;
                            case 1:
                                manager.showVitalQR(true);
                                break;
                            case 2: {
                                Calendar cal = Calendar.getInstance();
                                manager.setBraceletDate(cal.getTime());
                                break;
                            }
                            case 3: {
                                Calendar cal = Calendar.getInstance();
                                cal.set(2015, Calendar.OCTOBER, 21, 4, 29);
                                manager.setBraceletDate(cal.getTime());
                                break;
                            }
                            case 4:
                                manager.getBraceletDate();
                                break;
                            case 5: {
                                LifevitSDKUserData data = new LifevitSDKUserData(37, 92, 190, LifevitSDKConstants.GENDER_MALE);
                                manager.setBraceletUserInformation(data);
                            }
                                break;
                            case 6:
                                manager.getBraceletUserInformation();
                                break;
                            case 7: {
                                LifevitSDKBraceletParams data = new LifevitSDKBraceletParams();

                                manager.setBraceletParameters(data);
                            }
                                break;
                            case 8:
                                manager.getBraceletParameters();
                                break;
                            case 9:
                                manager.getBraceletMAC();
                                break;
                            case 10:
                                manager.setBraceletTargetSteps(7000);
                                break;
                            case 11:
                                manager.getBraceletTargetSteps();
                                break;
                            case 12: {
                                manager.getBraceletBattery();
                                break;
                            }
                            case 13: {
                                manager.getVitalData(LifevitSDKBleDeviceBraceletVital.Data.OXYMETER, false);
                                break;
                            }
                            case 14: {
                                manager.getVitalData(LifevitSDKBleDeviceBraceletVital.Data.OXYMETER, true);
                                break;
                            }
                            case 15: {
                                //Set oximeter period

                                //Set periodic health data
                                LifevitSDKVitalPeriod period = new LifevitSDKVitalPeriod();
                                period.setType(LifevitSDKBleDeviceBraceletVital.Data.OXYMETER);
                                period.setThursday(true);
                                period.setStartHour(10);
                                period.setEndHour(14);
                                period.setWeekDays(true);
                                manager.setVitalPeriodicConfiguration(period);
                                break;
                            }
                            case 16: {
                                //Get oximeter period
                                manager.getVitalPeriodicConfiguration(LifevitSDKBleDeviceBraceletVital.Data.OXYMETER);
                                break;
                            }
                            case 17: {


                                manager.getVitalData(LifevitSDKBleDeviceBraceletVital.Data.TEMPERATURE, false);
                                break;
                            }
                            case 18: {


                                manager.getVitalData(LifevitSDKBleDeviceBraceletVital.Data.TEMPERATURE, true);
                                break;
                            }
                            case 19: {

                                manager.getVitalData(LifevitSDKBleDeviceBraceletVital.Data.HR, false);
                                break;
                            }
                            case 20: {
                                manager.getVitalData(LifevitSDKBleDeviceBraceletVital.Data.HR, true);
                                break;
                            }
                            case 21:
                                //Set periodic health data
                                LifevitSDKVitalPeriod period = new LifevitSDKVitalPeriod();
                                period.setType(LifevitSDKBleDeviceBraceletVital.Data.HR);
                                period.setThursday(true);
                                period.setStartHour(10);
                                period.setEndHour(14);
                                period.setWeekDays(true);
                                manager.setVitalPeriodicConfiguration(period);
                                break;
                            case 22:
                                manager.startVitalECG();
                                break;
                            case 23:
                                manager.getVitalECGStatus();
                                break;
                            case 24:
                                manager.getVitalECGWaveform();
                                break;
                            case 25:
                                //GET HRV DATA
                                manager.getVitalData(LifevitSDKBleDeviceBraceletVital.Data.VITALS);
                                break;
                            case 26:
                                //Set realtime counting on
                                manager.setVitalRealTime(true, true);
                                break;
                            case 27:
                                //Set realtime counting off
                                manager.setVitalRealTime(false, false);
                                break;
                            case 28: {
                                //Get Total Step data

                                manager.getBraceletCurrentSteps();
                                break;
                            }
                            case 29:
                                //Get detailed step data
                                manager.getVitalData(LifevitSDKBleDeviceBraceletVital.Data.STEPS);
                                break;
                            case 30: {
                                //Get detailed sleep data
                                manager.getVitalData(LifevitSDKBleDeviceBraceletVital.Data.SLEEP);
                                break;
                            }
                            case 31: {
                                //Set Activity Period
                                LifevitSDKVitalActivityPeriod aPeriod = new LifevitSDKVitalActivityPeriod();
                                aPeriod.setStartHour(10);
                                aPeriod.setEndHour(14);
                                aPeriod.setWeekDays(true);
                                manager.setVitalActivityPeriod(aPeriod);
                                break;
                            }
                            case 32: {
                                //Get Activity Period
                                manager.getVitalActivityPeriod();
                                break;
                            }
                            case 33:
                                //Set sport mode
                                //manager.getVitalData(LifevitSDKBleDeviceBraceletVital.Data.SPORTS);
                                manager.setVitalSportMode(0, 0, 0 ,60);
                                break;
                            case 34:
                                manager.startVitalHealthMeasurement(LifevitSDKBleDeviceBraceletVital.Data.VITALS);
                                break;
                            case 35:
                                manager.startVitalHealthMeasurement(LifevitSDKBleDeviceBraceletVital.Data.HR);
                                break;
                            case 36:
                                manager.startVitalHealthMeasurement(LifevitSDKBleDeviceBraceletVital.Data.OXYMETER);
                                break;
                            case 37:
                                manager.getVitalData(LifevitSDKBleDeviceBraceletVital.Data.SPORTS);
                                break;
                            case 38:
                                //SET ALARMS
                                LifevitSDKVitalAlarms alarms = new LifevitSDKVitalAlarms();
                                alarms.getAlarm1().setEnabled(true);
                                alarms.getAlarm1().setOnlyWeekDays();

                                alarms.getAlarm1().setHour( Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                                alarms.getAlarm1().setMinute( Calendar.getInstance().get(Calendar.MINUTE) + 1);

                                manager.setVitalAlarms(alarms);
                                break;
                            case 39:
                                //SET NOTIF
                                LifevitSDKVitalScreenNotification notif = new LifevitSDKVitalScreenNotification();
                                notif.setType(LifevitSDKVitalScreenNotification.Type.CALL);
                                notif.setContact("LIFEVIT");
                                notif.setText("Check our new SDK for the VITAL Bracelet. Great medical features!");
                                manager.setVitalNotification(notif);
                                break;
                            case 40:
                                //SET WEATHER
                                LifevitSDKVitalWeather weather = new LifevitSDKVitalWeather();
                                manager.setVitalWeather(weather);
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
                if (deviceType != LifevitSDKConstants.DEVICE_BRACELET_VITAL) {
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
                if (deviceType != LifevitSDKConstants.DEVICE_BRACELET_VITAL) {
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
                                textview_connection_result.setTextColor(ContextCompat.getColor(BraceletVitalActivity.this, android.R.color.holo_red_dark));
                                break;
                            case LifevitSDKConstants.STATUS_SCANNING:
                                button_connect.setText("Stop scan");
                                isDisconnected = false;
                                textview_connection_result.setText("Scanning");
                                textview_connection_result.setTextColor(ContextCompat.getColor(BraceletVitalActivity.this, android.R.color.holo_blue_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTING:
                                button_connect.setText("Disconnect");
                                isDisconnected = false;
                                textview_connection_result.setText("Connecting");
                                textview_connection_result.setTextColor(ContextCompat.getColor(BraceletVitalActivity.this, android.R.color.holo_orange_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTED:
                                button_connect.setText("Disconnect");
                                isDisconnected = false;
                                textview_connection_result.setText("Connected");
                                textview_connection_result.setTextColor(ContextCompat.getColor(BraceletVitalActivity.this, android.R.color.holo_green_dark));
                                break;
                        }
                    }
                });
            }
        };

        LifevitSDKBraceletVitalListener bListener = new LifevitSDKBraceletVitalListener() {




            @Override
            public void braceletInformation(final Object message) {
                synchronized (textview_info) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (message != null) {

                                //if (message.getClass().isInstance("java.lang.String")) {
                                if (message instanceof Long) {
                                    String text = textview_info.getText().toString();
                                    text = "";
                                    text += "\n";

                                    Date d = new Date((Long) message);

                                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    String strDate = dateFormat.format(d);

                                    text += dateFormat.format(d);

                                    textview_info.setText(text);
                                    Log.d(TAG, "[braceletInformation] " + text);
                                }
                                else{
                                    String text = textview_info.getText().toString();
                                    text = "";
                                    text += "\n";
                                    text += message.toString();
                                    textview_info.setText(text);
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
            public void braceletOperation(final int operation) {
                synchronized (textview_info) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String text = textview_info.getText().toString();
                            text += "\n";
                            text += "Operation received: " + operation;
                            textview_info.setText(text);
                            Log.d(TAG, "[braceletOperation] " + text);
                        }
                    });
                }
            }

            @Override
            public void braceletSOS() {
                synchronized (textview_info) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String text = textview_info.getText().toString();
                            text += "\n";
                            text += "SOS received!";
                            textview_info.setText(text);
                            Log.d(TAG, "[braceletSOS] " + text);
                        }
                    });
                }
            }
        };

        // Create connection helper
        SDKTestApplication.getInstance().getLifevitSDKManager().addDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setBraceletVitalListener(bListener);
    }


}
