package es.lifevit.sdk.sampleapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Timer;
import java.util.TimerTask;

import es.lifevit.sdk.LifevitSDKConstants;
import es.lifevit.sdk.LifevitSDKHeartData;
import es.lifevit.sdk.listeners.LifevitSDKDeviceListener;
import es.lifevit.sdk.listeners.LifevitSDKHeartListener;
import es.lifevit.sdk.sampleapp.R;
import es.lifevit.sdk.sampleapp.SDKTestApplication;

public class TensiometerActivity extends AppCompatActivity {

    private static final String TAG = TensiometerActivity.class.getSimpleName();

    TextView textview_connection_result, textview_measurement_result_sys, textview_measurement_result_dia, textview_measurement_result_puls, textview_measurement_info,
            textview_connect_by_addr_result;
    Button button_connect, button_measurement, button_connect_by_addr;
    LinearLayout connect_by_addr_container;

    boolean isDisconnected = true;
    String lastDeviceConnectedAddress = "";

    // TimerTask
    Timer timer;
    TimerTask timerTask;
    static final long TIMER_TASK_PERIOD = 10000;
    private LifevitSDKDeviceListener cl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tensiometer);

        initComponents();
        initListeners();

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_TENSIOMETER)) {
            button_connect.setText("Disconnect");
            isDisconnected = false;
            textview_connection_result.setText("Connected");
            textview_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            button_connect.setText("Connect");
            isDisconnected = true;
            textview_connection_result.setText("Disconnected");
            textview_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }

        initSdk();

        // Start timer task
//        startTimer();
    }


    @Override
    protected void onPause() {
        super.onPause();

        SDKTestApplication.getInstance().getLifevitSDKManager().removeDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setHeartListener(null);

//        stoptimertask();
    }


    private void initComponents() {
        textview_connection_result = findViewById(R.id.textview_connection_result);
        textview_measurement_result_sys = findViewById(R.id.textview_measurement_result_sys);
        textview_measurement_result_dia = findViewById(R.id.textview_measurement_result_dia);
        textview_measurement_result_puls = findViewById(R.id.textview_measurement_result_puls);
        textview_measurement_info = findViewById(R.id.textview_measurement_info);
        textview_connect_by_addr_result = findViewById(R.id.textview_connect_by_addr_result);

        button_connect = findViewById(R.id.button_connect);
        button_measurement = findViewById(R.id.button_measurement);
        button_connect_by_addr = findViewById(R.id.button_connect_by_addr);

        connect_by_addr_container = findViewById(R.id.connect_by_addr_container);
    }


    private void initListeners() {

        button_connect.setOnClickListener(_ -> {
            if (isDisconnected) {
                SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_TENSIOMETER, 10000);
            } else {
                SDKTestApplication.getInstance().getLifevitSDKManager().disconnectDevice(LifevitSDKConstants.DEVICE_TENSIOMETER);
            }
        });

        button_measurement.setOnClickListener(view -> SDKTestApplication.getInstance().getLifevitSDKManager().startMeasurement());

        button_connect_by_addr.setOnClickListener(_ -> {
            if (isDisconnected) {
                textview_connect_by_addr_result.setText("Connecting device... Check connection status in upper TextView");

                // Stop auto-connection timer
//                    stoptimertask();
                // TODO: Should also stop Scan?

                SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_TENSIOMETER, 10000,
                        lastDeviceConnectedAddress);
            } else {
                textview_connect_by_addr_result.setText("Device is already connected");
            }
        });
    }


    private void initSdk() {

        // Create listener
        cl = new LifevitSDKDeviceListener() {

            @Override
            public void deviceOnConnectionError(int deviceType, final int errorCode) {
                runOnUiThread(() -> {
                    if (errorCode == LifevitSDKConstants.CODE_LOCATION_DISABLED) {
                        textview_connection_result.setText("ERROR: Debe activar permisos localización");
                    } else if (errorCode == LifevitSDKConstants.CODE_BLUETOOTH_DISABLED) {
                        textview_connection_result.setText("ERROR: El bluetooth no está activado");
                    } else if (errorCode == LifevitSDKConstants.CODE_LOCATION_TURN_OFF) {
                        textview_connection_result.setText("ERROR: La Ubicación está apagada");
                    } else {
                        textview_connection_result.setText("ERROR: Desconocido");
                    }
                });
            }

            @Override
            public void deviceOnConnectionChanged(int deviceType, final int status) {
                runOnUiThread(() -> {

                    Log.d(TAG, "Status changed: " + status);

                    switch (status) {
                        case LifevitSDKConstants.STATUS_DISCONNECTED:
                            button_connect.setText("Connect");
                            isDisconnected = true;
                            textview_connection_result.setText("Disconnected");
                            textview_connection_result.setTextColor(ContextCompat.getColor(TensiometerActivity.this, android.R.color.holo_red_dark));

                            // Start timer again
//                                startTimer();

                            break;
                        case LifevitSDKConstants.STATUS_SCANNING:
                            button_connect.setText("Stop scan");
                            isDisconnected = false;
                            textview_connection_result.setText("Scanning");
                            textview_connection_result.setTextColor(ContextCompat.getColor(TensiometerActivity.this, android.R.color.holo_blue_dark));
                            break;
                        case LifevitSDKConstants.STATUS_CONNECTING:
                            button_connect.setText("Disconnect");
                            isDisconnected = false;
                            textview_connection_result.setText("Connecting");
                            textview_connection_result.setTextColor(ContextCompat.getColor(TensiometerActivity.this, android.R.color.holo_orange_dark));

                            // Stop timer
//                                stoptimertask();

                            break;
                        case LifevitSDKConstants.STATUS_CONNECTED:
                            button_connect.setText("Disconnect");
                            isDisconnected = false;
                            textview_connection_result.setText("Connected");
                            textview_connection_result.setTextColor(ContextCompat.getColor(TensiometerActivity.this, android.R.color.holo_green_dark));

                            // Save connected device address
                            lastDeviceConnectedAddress = SDKTestApplication.getInstance().getLifevitSDKManager().getDeviceAddress(LifevitSDKConstants.DEVICE_TENSIOMETER);

                            connect_by_addr_container.setVisibility(View.VISIBLE);

                            break;
                    }
                });
            }
        };


        LifevitSDKHeartListener hListener = new LifevitSDKHeartListener() {

            @Override
            public void heartDeviceOnProgressMeasurement(final int pulse) {
                runOnUiThread(() -> {
                    textview_measurement_result_puls.setText(String.valueOf(pulse));
                    textview_measurement_info.setText("Measuring...");
                });
            }


            @Override
            public void heartDeviceOnBatteryResult(final int battery) {
                runOnUiThread(() -> textview_measurement_info.setText("Battery charge: " + String.valueOf(battery)));
            }

            @Override
            public void heartDeviceOnResult(final LifevitSDKHeartData result) {
                runOnUiThread(() -> {


                    if (result.getErrorCode() != LifevitSDKConstants.CODE_OK) {
                        // Error
                        String errorText = "";
                        switch (result.getErrorCode()) {
                            case LifevitSDKConstants.CODE_UNKNOWN:
                                errorText = "CODE_UNKNOWN";
                                break;
                            case LifevitSDKConstants.CODE_LOW_SIGNAL:
                                errorText = "CODE_LOW_SIGNAL";
                                break;
                            case LifevitSDKConstants.CODE_NOISE:
                                errorText = "CODE_NOISE";
                                break;
                            case LifevitSDKConstants.CODE_INFLATION_TIME:
                                errorText = "CODE_INFLATION_TIME";
                                break;
                            case LifevitSDKConstants.CODE_ABNORMAL_RESULT:
                                errorText = "CODE_ABNORMAL_RESULT";
                                break;
                            case LifevitSDKConstants.CODE_RETRY:
                                errorText = "CODE_RETRY";
                                break;
                            case LifevitSDKConstants.CODE_LOW_BATTERY:
                                errorText = "CODE_LOW_BATTERY";
                                break;
                            case LifevitSDKConstants.CODE_NO_RESULTS:
                                errorText = "CODE_NO_RESULTS";
                                break;
                            case LifevitSDKConstants.CODE_TOO_MUCH_INTERFERENCE:
                                errorText = "CODE_TOO_MUCH_INTERFERENCE";
                                break;
                        }

                        textview_measurement_info.setText("Error: " + errorText);
                        textview_measurement_result_sys.setText("---");
                        textview_measurement_result_dia.setText("---");
                        textview_measurement_result_puls.setText("---");

                    } else {
                        textview_measurement_info.setText("Measurement result");
                        textview_measurement_result_sys.setText(String.valueOf(result.getSystolic()));
                        textview_measurement_result_dia.setText(String.valueOf(result.getDiastolic()));
                        textview_measurement_result_puls.setText(String.valueOf(result.getPulse()));
                    }
                });
            }
        };


        // Create connection helper
//        SDKTestApplication.getInstance().getLifevitSDKManager().setConnectionListener(cl);

        SDKTestApplication.getInstance().getLifevitSDKManager().addDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setHeartListener(hListener);
    }


    public void startTimer() {
        //set a new Timer
        if (timer != null) {
            stoptimertask();
        }

        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        timer.schedule(timerTask, TIMER_TASK_PERIOD, TIMER_TASK_PERIOD); //
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
//        handler.removeCallbacks();
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                boolean connected = SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_TENSIOMETER);
                Log.e(TAG, "----- TimerTask executed. Is connected: " + connected);
                if (!connected) {
                    Log.e(TAG, "----- Start scan");
                    SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_TENSIOMETER, 4500);
                }

            }
        };
    }


}
