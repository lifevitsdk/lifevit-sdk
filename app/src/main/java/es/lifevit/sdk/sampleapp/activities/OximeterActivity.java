package es.lifevit.sdk.sampleapp.activities;

import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import es.lifevit.sdk.LifevitSDKConstants;
import es.lifevit.sdk.LifevitSDKOximeterData;
import es.lifevit.sdk.listeners.LifevitSDKDeviceListener;
import es.lifevit.sdk.listeners.LifevitSDKOximeterListener;
import es.lifevit.sdk.sampleapp.R;
import es.lifevit.sdk.sampleapp.SDKTestApplication;

public class OximeterActivity extends AppCompatActivity {


    TextView textview_connection_result, textview_measurement_result_spo2, textview_measurement_result_pi, textview_measurement_result_rpm,
            textview_measurement_result_lpm, textview_measurement_result_pleth, textview_measurement_info;
    Button button_connect;

    boolean isDisconnected = true;
    private LifevitSDKDeviceListener cl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oximeter);

        initComponents();
        initListeners();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_OXIMETER)) {
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
    }

    /**
     * Components initialization
     * @author Lifevit
     */
    public void initComponents() {
        textview_connection_result = findViewById(R.id.oximeter_textview_connection_result);
        textview_measurement_result_spo2 = findViewById(R.id.oximeter_textview_measurement_result_spo2);
        textview_measurement_result_pi = findViewById(R.id.oximeter_textview_measurement_result_pi);
        textview_measurement_result_rpm = findViewById(R.id.oximeter_textview_measurement_result_rpm);
        textview_measurement_result_lpm = findViewById(R.id.oximeter_textview_measurement_result_lpm);
        textview_measurement_result_pleth = findViewById(R.id.oximeter_textview_measurement_result_pleth);
        textview_measurement_info = findViewById(R.id.oximeter_textview_measurement_info);

        button_connect = findViewById(R.id.oximeter_button_connect);
    }


    private void initListeners() {

        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDisconnected) {
                    SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_OXIMETER, 120000);
                } else {
                    SDKTestApplication.getInstance().getLifevitSDKManager().disconnectDevice(LifevitSDKConstants.DEVICE_OXIMETER);
                }
            }
        });
    }


    private void initSdk() {

        // Create connection listener
        cl = new LifevitSDKDeviceListener() {

            @Override
            public void deviceOnConnectionError(int deviceType, final int errorCode) {
                if (deviceType != LifevitSDKConstants.DEVICE_OXIMETER) {
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
                if (deviceType != LifevitSDKConstants.DEVICE_OXIMETER) {
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
                                textview_connection_result.setTextColor(ContextCompat.getColor(OximeterActivity.this, android.R.color.holo_red_dark));
                                break;
                            case LifevitSDKConstants.STATUS_SCANNING:
                                button_connect.setText("Stop scan");
                                isDisconnected = false;
                                textview_connection_result.setText("Scanning");
                                textview_connection_result.setTextColor(ContextCompat.getColor(OximeterActivity.this, android.R.color.holo_blue_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTING:
                                button_connect.setText("Disconnect");
                                isDisconnected = false;
                                textview_connection_result.setText("Connecting");
                                textview_connection_result.setTextColor(ContextCompat.getColor(OximeterActivity.this, android.R.color.holo_orange_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTED:
                                button_connect.setText("Disconnect");
                                isDisconnected = false;
                                textview_connection_result.setText("Connected");
                                textview_connection_result.setTextColor(ContextCompat.getColor(OximeterActivity.this, android.R.color.holo_green_dark));
                                break;
                        }
                    }
                });
            }
        };

        LifevitSDKOximeterListener oximeterListener = new LifevitSDKOximeterListener() {
            @Override
            public void oximeterDeviceOnProgressMeasurement(final int pleth) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textview_measurement_result_pleth.setText(String.valueOf(pleth));
                    }
                });
            }

            @Override
            public void oximeterDeviceOnResult(final LifevitSDKOximeterData data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textview_measurement_result_spo2.setText(String.valueOf(data.getSpO2()));
                        textview_measurement_result_pi.setText(String.valueOf(data.getPi()));
                        textview_measurement_result_rpm.setText(String.valueOf(data.getRpm()));
                        textview_measurement_result_lpm.setText(String.valueOf(data.getLpm()));
                    }
                });
            }
        };

        // Create connection helper
        SDKTestApplication.getInstance().getLifevitSDKManager().addDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setOximeterListener(oximeterListener);
    }


    @Override
    protected void onStop() {
        super.onStop();
        SDKTestApplication.getInstance().getLifevitSDKManager().removeDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setOximeterListener(null);
    }


}
