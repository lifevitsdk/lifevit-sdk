package es.lifevit.sdk.sampleapp.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import es.lifevit.sdk.LifevitSDKConstants;
import es.lifevit.sdk.LifevitSDKHeartData;
import es.lifevit.sdk.LifevitSDKManager;
import es.lifevit.sdk.bracelet.LifevitSDKTensioBraceletMeasurementInterval;
import es.lifevit.sdk.listeners.LifevitSDKDeviceListener;
import es.lifevit.sdk.listeners.LifevitSDKTensiobraceletListener;
import es.lifevit.sdk.sampleapp.R;
import es.lifevit.sdk.sampleapp.SDKTestApplication;

public class TensiobraceletActivity extends AppCompatActivity {


    TextView textview_connection_result, textview_info;
    Button button_connect, button_command;
    boolean isDisconnected = true;
    private LifevitSDKDeviceListener cl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tensiobracelet);

        initComponents();
        initListeners();
//        initSdk();

//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_TENSIOBRACELET)) {
            button_connect.setText("Disconnect");
            isDisconnected = false;
            textview_connection_result.setText("Connected");
            textview_connection_result.setTextColor(ContextCompat.getColor(TensiobraceletActivity.this, android.R.color.holo_green_dark));
        } else {
            button_connect.setText("Connect");
            isDisconnected = true;
            textview_connection_result.setText("Disconnected");
            textview_connection_result.setTextColor(ContextCompat.getColor(TensiobraceletActivity.this, android.R.color.holo_red_dark));
        }

        // Create connection helper
        initSdk();

    }

    private void initComponents() {
        textview_info = findViewById(R.id.tensiobracelet_textview_command_info);
        textview_info.setMovementMethod(new ScrollingMovementMethod());
        textview_connection_result = findViewById(R.id.tensiobracelet_connection_result);

        button_connect = findViewById(R.id.tensiobracelet_connect);
        button_command = findViewById(R.id.tensiobracelet_button_command);
    }


    private void initListeners() {

        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDisconnected) {
                    SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_TENSIOBRACELET, 10000);
                } else {
                    SDKTestApplication.getInstance().getLifevitSDKManager().disconnectDevice(LifevitSDKConstants.DEVICE_TENSIOBRACELET);
                }
            }
        });

        button_command.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final LifevitSDKManager manager = SDKTestApplication.getInstance().getLifevitSDKManager();

                CharSequence[] colors = new CharSequence[]{
                        "0. Set current date",
                        "1. Set date today 10:58",
                        "2. Start measurement",
                        "3. Get Blood Pressure History Data",
                        "4. Return",
                        "5. Program Automatic Measurements (from 8:00 to 21:30, every 30 minutes)",
                        "6. Program 2 Automatic Measurements (from 9:30 to 11:00 every hour, from 13:00 to 15:00 every 30 minutes)",
                        "7. Program 3 Automatic Measurements (from 10:00 to 11:00 every 30 minutes, from 12:00 to 13:00 every 30 minutes, from 15:00 to 17:00 every 30 minutes)",
                        "8. Deactivate Automatic Measurements"
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(TensiobraceletActivity.this);
                builder.setTitle("Select command");
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                manager.tensiobraceletSetDate(Calendar.getInstance().getTimeInMillis());
                                break;
                            case 1:
                                Calendar cal = Calendar.getInstance();
                                cal.set(Calendar.HOUR_OF_DAY, 10);
                                cal.set(Calendar.MINUTE, 58);
                                manager.tensiobraceletSetDate(cal.getTimeInMillis());
                                break;
                            case 2:
                                manager.tensiobraceletStartMeasurement();
                                break;
                            case 3:
                                manager.tensiobraceletGetBloodPressureHistoryData();
                                break;
                            case 4:
                                manager.tensiobraceletReturnMainScreen();
                                break;
                            case 5:
                                manager.tensiobraceletProgramAutomaticMeasurements(new LifevitSDKTensioBraceletMeasurementInterval(8,
                                        LifevitSDKTensioBraceletMeasurementInterval.StartingEndingMinutes.O_CLOCK, 21,
                                        LifevitSDKTensioBraceletMeasurementInterval.StartingEndingMinutes.HALF_PAST,
                                        1, 1, LifevitSDKTensioBraceletMeasurementInterval.MinutesIntervals.INTERVAL_30_MIN));
                                break;
                            case 6:
                                manager.tensiobraceletProgramAutomaticMeasurements(new LifevitSDKTensioBraceletMeasurementInterval(9,
                                        LifevitSDKTensioBraceletMeasurementInterval.StartingEndingMinutes.HALF_PAST, 11,
                                        LifevitSDKTensioBraceletMeasurementInterval.StartingEndingMinutes.O_CLOCK,
                                        1, 2, LifevitSDKTensioBraceletMeasurementInterval.MinutesIntervals.INTERVAL_60_MIN));

                                manager.tensiobraceletProgramAutomaticMeasurements(new LifevitSDKTensioBraceletMeasurementInterval(13,
                                        LifevitSDKTensioBraceletMeasurementInterval.StartingEndingMinutes.O_CLOCK, 15,
                                        LifevitSDKTensioBraceletMeasurementInterval.StartingEndingMinutes.O_CLOCK,
                                        2, 2, LifevitSDKTensioBraceletMeasurementInterval.MinutesIntervals.INTERVAL_30_MIN));
                                break;
                            case 7:
                                manager.tensiobraceletProgramAutomaticMeasurements(new LifevitSDKTensioBraceletMeasurementInterval(10,
                                        LifevitSDKTensioBraceletMeasurementInterval.StartingEndingMinutes.O_CLOCK, 11,
                                        LifevitSDKTensioBraceletMeasurementInterval.StartingEndingMinutes.O_CLOCK,
                                        1, 3, LifevitSDKTensioBraceletMeasurementInterval.MinutesIntervals.INTERVAL_30_MIN));

                                manager.tensiobraceletProgramAutomaticMeasurements(new LifevitSDKTensioBraceletMeasurementInterval(12,
                                        LifevitSDKTensioBraceletMeasurementInterval.StartingEndingMinutes.O_CLOCK, 13,
                                        LifevitSDKTensioBraceletMeasurementInterval.StartingEndingMinutes.O_CLOCK,
                                        2, 3, LifevitSDKTensioBraceletMeasurementInterval.MinutesIntervals.INTERVAL_30_MIN));

                                manager.tensiobraceletProgramAutomaticMeasurements(new LifevitSDKTensioBraceletMeasurementInterval(15,
                                        LifevitSDKTensioBraceletMeasurementInterval.StartingEndingMinutes.O_CLOCK, 17,
                                        LifevitSDKTensioBraceletMeasurementInterval.StartingEndingMinutes.O_CLOCK,
                                        3, 3, LifevitSDKTensioBraceletMeasurementInterval.MinutesIntervals.INTERVAL_30_MIN));
                                break;
                            case 8:
                                manager.tensiobraceletDeactivateAutomaticMeasurements();
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
                if (deviceType != LifevitSDKConstants.DEVICE_TENSIOBRACELET) {
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
                if (deviceType != LifevitSDKConstants.DEVICE_TENSIOBRACELET) {
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
                                textview_connection_result.setTextColor(ContextCompat.getColor(TensiobraceletActivity.this, android.R.color.holo_red_dark));
                                break;
                            case LifevitSDKConstants.STATUS_SCANNING:
                                button_connect.setText("Stop scan");
                                isDisconnected = false;
                                textview_connection_result.setText("Scanning");
                                textview_connection_result.setTextColor(ContextCompat.getColor(TensiobraceletActivity.this, android.R.color.holo_blue_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTING:
                                button_connect.setText("Disconnect");
                                isDisconnected = false;
                                textview_connection_result.setText("Connecting");
                                textview_connection_result.setTextColor(ContextCompat.getColor(TensiobraceletActivity.this, android.R.color.holo_orange_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTED:
                                button_connect.setText("Disconnect");
                                isDisconnected = false;
                                textview_connection_result.setText("Connected");
                                textview_connection_result.setTextColor(ContextCompat.getColor(TensiobraceletActivity.this, android.R.color.holo_green_dark));
                                break;
                        }
                    }
                });
            }
        };

        LifevitSDKTensiobraceletListener bListener = new LifevitSDKTensiobraceletListener() {

            @Override
            public void tensiobraceletOnMeasurement(final int pulse) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String text = textview_info.getText().toString();
                        text += "\n";
                        text += "Measuring... Value: " + pulse;
                        textview_info.setText(text);
                    }
                });
            }

            @Override
            public void tensiobraceletResult(final LifevitSDKHeartData result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String text = textview_info.getText().toString();
                        text += "\n";
                        text += "Measurement result: "
                                + "\nSystolic: " + result.getSystolic()
                                + "\nDiastolic: " + result.getDiastolic()
                                + "\nPulse: " + result.getPulse();
                        textview_info.setText(text);
                    }
                });
            }

            @Override
            public void tensiobraceletHistoricResults(final List<LifevitSDKHeartData> results) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String text = textview_info.getText().toString();
                        text += "\n";
                        text += "Historic results:\n";

                        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm");

                        for (LifevitSDKHeartData heartData : results) {
                            text += "\n";
                            text += "- Date: " + format.format(heartData.getDate())
                                    + ", systolic: " + heartData.getSystolic()
                                    + ", diastolic: " + heartData.getDiastolic()
                                    + ", pulse: " + heartData.getPulse();
                        }
                        textview_info.setText(text);
                    }
                });
            }

            @Override
            public void tensiobraceletError(final int errorCode) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // Error
                        String errorText = "ERROR: General";
                        switch (errorCode) {
                            case LifevitSDKConstants.TENSIOBRACELET_ERROR_HAND_HIGH:
                                errorText = "Hand high";
                                break;
                            case LifevitSDKConstants.TENSIOBRACELET_ERROR_HAND_LOW:
                                errorText = "Hand low";
                                break;
                            case LifevitSDKConstants.TENSIOBRACELET_ERROR_GENERAL:
                                errorText = "General error";
                                break;
                            case LifevitSDKConstants.TENSIOBRACELET_ERROR_LOW_POWER:
                                errorText = "Low power";
                                break;
                            case LifevitSDKConstants.TENSIOBRACELET_ERROR_INCORRECT_POSITION:
                                errorText = "Incorrect position";
                                break;
                            case LifevitSDKConstants.TENSIOBRACELET_ERROR_BODY_MOVED:
                                errorText = "Body moved";
                                break;
                            case LifevitSDKConstants.TENSIOBRACELET_ERROR_TIGHT_WEARING:
                                errorText = "Tight wearing";
                                break;
                            case LifevitSDKConstants.TENSIOBRACELET_ERROR_LOOSE_WEARING:
                                errorText = "Loose wearing";
                                break;
                            case LifevitSDKConstants.TENSIOBRACELET_ERROR_AIR_LEAKAGE:
                                errorText = "Air leakage";
                                break;
                            default:
                                errorText = "Unknown error";
                        }

                        textview_info.setText("Error: " + errorText);
                    }
                });
            }

            @Override
            public void tensiobraceletCommandReceived() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String text = textview_info.getText().toString();
                        text += "\n";
                        text += "Command received.";
                        textview_info.setText(text);
                    }
                });
            }

        };

        // Create connection helper
        SDKTestApplication.getInstance().getLifevitSDKManager().addDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setTensiobraceletListener(bListener);
    }


    @Override
    protected void onStop() {
        super.onStop();
        SDKTestApplication.getInstance().getLifevitSDKManager().removeDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setTensiobraceletListener(null);
    }

}
