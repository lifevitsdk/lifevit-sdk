package es.lifevit.sdk.sampleapp.activities;

import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;

import es.lifevit.sdk.LifevitSDKConstants;
import es.lifevit.sdk.listeners.LifevitSDKBabyTempBT125Listener;
import es.lifevit.sdk.listeners.LifevitSDKDeviceListener;
import es.lifevit.sdk.sampleapp.R;
import es.lifevit.sdk.sampleapp.SDKTestApplication;

public class BabyTempBT125Activity extends AppCompatActivity {

    private static final String TAG = BabyTempBT125Activity.class.getSimpleName();

    TextView textview_connection_result, textview_measurement_result_body, textview_measurement_result_env;
    LinearLayout measurement_container;
    Button button_connect;


    boolean isDisconnected = true;
    private LifevitSDKDeviceListener cl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baby_temp_bt125);

        initComponents();
        initListeners();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_BABY_TEMP_BT125)) {
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


    @Override
    protected void onPause() {
        super.onPause();

        SDKTestApplication.getInstance().getLifevitSDKManager().removeDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setBabyTempBT125Listener(null);
    }


    private void initComponents() {

        textview_connection_result = findViewById(R.id.babytemp_textview_connection_result);
        textview_measurement_result_env = findViewById(R.id.babytemp_textview_measurement_result_env);
        textview_measurement_result_body = findViewById(R.id.babytemp_textview_measurement_result_body);
        measurement_container = findViewById(R.id.babytemp_measurement_container);
        button_connect = findViewById(R.id.babytemp_button_connect);
    }


    private void initListeners() {

        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDisconnected) {
                    SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_BABY_TEMP_BT125, 10000);
                } else {
                    SDKTestApplication.getInstance().getLifevitSDKManager().disconnectDevice(LifevitSDKConstants.DEVICE_BABY_TEMP_BT125);
                }
            }
        });
    }


    private void initSdk() {

        // Create listener
        cl = new LifevitSDKDeviceListener() {

            @Override
            public void deviceOnConnectionError(int deviceType, final int errorCode) {
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (status) {
                            case LifevitSDKConstants.STATUS_DISCONNECTED:
                                button_connect.setText("Connect");
                                isDisconnected = true;
                                textview_connection_result.setText("Disconnected");
                                textview_connection_result.setTextColor(ContextCompat.getColor(BabyTempBT125Activity.this, android.R.color.holo_red_dark));
                                break;
                            case LifevitSDKConstants.STATUS_SCANNING:
                                button_connect.setText("Stop scan");
                                isDisconnected = false;
                                textview_connection_result.setText("Scanning");
                                textview_connection_result.setTextColor(ContextCompat.getColor(BabyTempBT125Activity.this, android.R.color.holo_blue_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTING:
                                button_connect.setText("Disconnect");
                                isDisconnected = false;
                                textview_connection_result.setText("Connecting");
                                textview_connection_result.setTextColor(ContextCompat.getColor(BabyTempBT125Activity.this, android.R.color.holo_orange_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTED:
                                button_connect.setText("Disconnect");
                                isDisconnected = false;
                                textview_connection_result.setText("Connected");
                                textview_connection_result.setTextColor(ContextCompat.getColor(BabyTempBT125Activity.this, android.R.color.holo_green_dark));
                                break;
                        }
                    }
                });
            }
        };

        LifevitSDKBabyTempBT125Listener btListener = new LifevitSDKBabyTempBT125Listener() {
            @Override
            public void onBabyTempDataReady(final double bodyTemperature, final double environmentTemperature) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DecimalFormat df = new DecimalFormat("#.00");
                        textview_measurement_result_body.setText(df.format(environmentTemperature));
                        textview_measurement_result_env.setText(df.format(bodyTemperature));
                    }
                });
            }
        };

        SDKTestApplication.getInstance().getLifevitSDKManager().addDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setBabyTempBT125Listener(btListener);
    }


}
