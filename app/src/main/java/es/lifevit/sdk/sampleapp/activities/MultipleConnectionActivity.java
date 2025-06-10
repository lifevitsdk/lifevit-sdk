package es.lifevit.sdk.sampleapp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import es.lifevit.sdk.LifevitSDKConstants;
import es.lifevit.sdk.listeners.LifevitSDKDeviceListener;
import es.lifevit.sdk.sampleapp.R;
import es.lifevit.sdk.sampleapp.SDKTestApplication;

public class MultipleConnectionActivity extends AppCompatActivity {


    TextView textview_bracelet_connection_result, textview_thermometer_connection_result, textview_connection_result_address,
            textview_oximeter_connection_result, textview_tensiometer_connection_result;
    Button button_connect, button_check_address, button_connect_by_addr, button_connect_thermometer, button_connect_oximeter, button_connect_tensiometer;


    boolean isDisconnectedBracelet = true, isDisconnectedThermometer = true, isDisconnectedOximeter = true, isDisconnectedTensiometer = true;
    private LifevitSDKDeviceListener cl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_connection);

        initComponents();
        initListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_BRACELET_AT500HR)) {
            button_connect.setText("Disconnect");
            isDisconnectedBracelet = false;
            textview_bracelet_connection_result.setText("Connected");
            textview_bracelet_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            button_connect.setText("Connect");
            isDisconnectedBracelet = true;
            textview_bracelet_connection_result.setText("Disconnected");
            textview_bracelet_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }

        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_THERMOMETER)) {
            button_connect_thermometer.setText("Disconnect");
            isDisconnectedThermometer = false;
            textview_thermometer_connection_result.setText("Connected");
            textview_thermometer_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            button_connect_thermometer.setText("Connect");
            isDisconnectedThermometer = true;
            textview_thermometer_connection_result.setText("Disconnected");
            textview_thermometer_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }

        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_OXIMETER)) {
            button_connect_oximeter.setText("Disconnect");
            isDisconnectedOximeter = false;
            textview_oximeter_connection_result.setText("Connected");
            textview_oximeter_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            button_connect_oximeter.setText("Connect");
            isDisconnectedOximeter = true;
            textview_oximeter_connection_result.setText("Disconnected");
            textview_oximeter_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }

        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_TENSIOMETER)) {
            button_connect_tensiometer.setText("Disconnect");
            isDisconnectedTensiometer = false;
            textview_tensiometer_connection_result.setText("Connected");
            textview_tensiometer_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            button_connect_tensiometer.setText("Connect");
            isDisconnectedTensiometer = true;
            textview_tensiometer_connection_result.setText("Disconnected");
            textview_tensiometer_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }

        initSdk();
    }

    private void initComponents() {
        textview_bracelet_connection_result = findViewById(R.id.multiple_conn_textview_connection_result);
        textview_thermometer_connection_result = findViewById(R.id.multiple_conn_textview_thermometer_connected_result);
        textview_oximeter_connection_result = findViewById(R.id.multiple_conn_textview_oximeter_connected_result);
        textview_tensiometer_connection_result = findViewById(R.id.multiple_conn_textview_tensiometer_connected_result);

        textview_connection_result_address = (EditText) findViewById(R.id.multiple_conn_textview_connection_result_address);

        button_connect = findViewById(R.id.multiple_conn_button_connect);
        button_check_address = findViewById(R.id.multiple_conn_button_check_address);
        button_connect_by_addr = findViewById(R.id.multiple_conn_button_connect_by_addr);
        button_connect_thermometer = findViewById(R.id.multiple_conn_button_connect_thermometer);
        button_connect_oximeter = findViewById(R.id.multiple_conn_button_connect_oximeter);
        button_connect_tensiometer = findViewById(R.id.multiple_conn_button_connect_tensiometer);
    }


    private void initListeners() {

        button_connect.setOnClickListener(_ -> {
            if (isDisconnectedBracelet) {
                SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_BRACELET_AT500HR, 10000);
            } else {
                SDKTestApplication.getInstance().getLifevitSDKManager().disconnectDevice(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
            }
        });

        button_check_address.setOnClickListener(_ -> {
            String addr = SDKTestApplication.getInstance().getLifevitSDKManager().getDeviceAddress(LifevitSDKConstants.DEVICE_BRACELET_AT500HR);
            textview_connection_result_address.setText(addr);
        });

        button_connect_by_addr.setOnClickListener(view -> SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_BRACELET_AT500HR, 10000, textview_connection_result_address.getText().toString()));

        button_connect_thermometer.setOnClickListener(_ -> {
            if (isDisconnectedThermometer) {
                SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_THERMOMETER, 10000);
            } else {
                SDKTestApplication.getInstance().getLifevitSDKManager().disconnectDevice(LifevitSDKConstants.DEVICE_THERMOMETER);
            }
        });

        button_connect_oximeter.setOnClickListener(_ -> {
            if (isDisconnectedOximeter) {
                SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_OXIMETER, 10000);
            } else {
                SDKTestApplication.getInstance().getLifevitSDKManager().disconnectDevice(LifevitSDKConstants.DEVICE_OXIMETER);
            }
        });

        button_connect_tensiometer.setOnClickListener(_ -> {
            if (isDisconnectedTensiometer) {
                SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_TENSIOMETER, 10000);
            } else {
                SDKTestApplication.getInstance().getLifevitSDKManager().disconnectDevice(LifevitSDKConstants.DEVICE_TENSIOMETER);
            }
        });
    }


    private void initSdk() {

        // Create connection listener
        cl = new LifevitSDKDeviceListener() {

            @Override
            public void deviceOnConnectionError(final int deviceType, final int errorCode) {
//                if (deviceType != LifevitSDKConstants.DEVICE_BRACELET_AT500HR && deviceType != LifevitSDKConstants.DEVICE_THERMOMETER) {
//                    return;
//                }
                runOnUiThread(() -> {

                    TextView textView = null;
                    if (deviceType == LifevitSDKConstants.DEVICE_BRACELET_AT500HR) {
                        textView = textview_bracelet_connection_result;
                    } else if (deviceType == LifevitSDKConstants.DEVICE_THERMOMETER) {
                        textView = textview_thermometer_connection_result;
                    } else if (deviceType == LifevitSDKConstants.DEVICE_OXIMETER) {
                        textView = textview_oximeter_connection_result;
                    } else if (deviceType == LifevitSDKConstants.DEVICE_TENSIOMETER) {
                        textView = textview_tensiometer_connection_result;
                    }

                    if (textView != null) {
                        if (errorCode == LifevitSDKConstants.CODE_LOCATION_DISABLED) {
                            textView.setText("ERROR: Debe activar permisos localización");
                        } else if (errorCode == LifevitSDKConstants.CODE_BLUETOOTH_DISABLED) {
                            textView.setText("ERROR: El bluetooth no está activado");
                        } else if (errorCode == LifevitSDKConstants.CODE_LOCATION_TURN_OFF) {
                            textView.setText("ERROR: La Ubicación está apagada");
                        } else {
                            textView.setText("ERROR: Desconocido");
                        }
                    }
                });
            }

            @Override
            public void deviceOnConnectionChanged(final int deviceType, final int status) {
//                if (deviceType != LifevitSDKConstants.DEVICE_BRACELET_AT500HR && deviceType != LifevitSDKConstants.DEVICE_THERMOMETER) {
//                    return;
//                }

                runOnUiThread(() -> {

                    if (deviceType == LifevitSDKConstants.DEVICE_BRACELET_AT500HR) {

                        switch (status) {
                            case LifevitSDKConstants.STATUS_DISCONNECTED:
                                button_connect.setText("Connect");
                                isDisconnectedBracelet = true;
                                textview_bracelet_connection_result.setText("Disconnected");
                                textview_bracelet_connection_result.setTextColor(ContextCompat.getColor(MultipleConnectionActivity.this, android.R.color.holo_red_dark));
                                break;
                            case LifevitSDKConstants.STATUS_SCANNING:
                                button_connect.setText("Stop scan");
                                isDisconnectedBracelet = false;
                                textview_bracelet_connection_result.setText("Scanning");
                                textview_bracelet_connection_result.setTextColor(ContextCompat.getColor(MultipleConnectionActivity.this, android.R.color.holo_blue_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTING:
                                button_connect.setText("Disconnect");
                                isDisconnectedBracelet = false;
                                textview_bracelet_connection_result.setText("Connecting");
                                textview_bracelet_connection_result.setTextColor(ContextCompat.getColor(MultipleConnectionActivity.this, android.R.color.holo_orange_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTED:
                                button_connect.setText("Disconnect");
                                isDisconnectedBracelet = false;
                                textview_bracelet_connection_result.setText("Connected");
                                textview_bracelet_connection_result.setTextColor(ContextCompat.getColor(MultipleConnectionActivity.this, android.R.color.holo_green_dark));
                                break;
                        }

                    } else if (deviceType == LifevitSDKConstants.DEVICE_THERMOMETER) {

                        switch (status) {
                            case LifevitSDKConstants.STATUS_DISCONNECTED:
                                button_connect_thermometer.setText("Connect");
                                isDisconnectedThermometer = true;
                                textview_thermometer_connection_result.setText("Disconnected");
                                textview_thermometer_connection_result.setTextColor(ContextCompat.getColor(MultipleConnectionActivity.this, android.R.color.holo_red_dark));
                                break;
                            case LifevitSDKConstants.STATUS_SCANNING:
                                button_connect_thermometer.setText("Stop scan");
                                isDisconnectedThermometer = false;
                                textview_thermometer_connection_result.setText("Scanning");
                                textview_thermometer_connection_result.setTextColor(ContextCompat.getColor(MultipleConnectionActivity.this, android.R.color.holo_blue_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTING:
                                button_connect_thermometer.setText("Disconnect");
                                isDisconnectedThermometer = false;
                                textview_thermometer_connection_result.setText("Connecting");
                                textview_thermometer_connection_result.setTextColor(ContextCompat.getColor(MultipleConnectionActivity.this, android.R.color.holo_orange_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTED:
                                button_connect_thermometer.setText("Disconnect");
                                isDisconnectedThermometer = false;
                                textview_thermometer_connection_result.setText("Connected");
                                textview_thermometer_connection_result.setTextColor(ContextCompat.getColor(MultipleConnectionActivity.this, android.R.color.holo_green_dark));
                                break;
                        }
                    } else if (deviceType == LifevitSDKConstants.DEVICE_OXIMETER) {

                        switch (status) {
                            case LifevitSDKConstants.STATUS_DISCONNECTED:
                                button_connect_oximeter.setText("Connect");
                                isDisconnectedOximeter = true;
                                textview_oximeter_connection_result.setText("Disconnected");
                                textview_oximeter_connection_result.setTextColor(ContextCompat.getColor(MultipleConnectionActivity.this, android.R.color.holo_red_dark));
                                break;
                            case LifevitSDKConstants.STATUS_SCANNING:
                                button_connect_oximeter.setText("Stop scan");
                                isDisconnectedOximeter = false;
                                textview_oximeter_connection_result.setText("Scanning");
                                textview_oximeter_connection_result.setTextColor(ContextCompat.getColor(MultipleConnectionActivity.this, android.R.color.holo_blue_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTING:
                                button_connect_oximeter.setText("Disconnect");
                                isDisconnectedOximeter = false;
                                textview_oximeter_connection_result.setText("Connecting");
                                textview_oximeter_connection_result.setTextColor(ContextCompat.getColor(MultipleConnectionActivity.this, android.R.color.holo_orange_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTED:
                                button_connect_oximeter.setText("Disconnect");
                                isDisconnectedOximeter = false;
                                textview_oximeter_connection_result.setText("Connected");
                                textview_oximeter_connection_result.setTextColor(ContextCompat.getColor(MultipleConnectionActivity.this, android.R.color.holo_green_dark));
                                break;
                        }
                    } else if (deviceType == LifevitSDKConstants.DEVICE_TENSIOMETER) {

                        switch (status) {
                            case LifevitSDKConstants.STATUS_DISCONNECTED:
                                button_connect_tensiometer.setText("Connect");
                                isDisconnectedTensiometer = true;
                                textview_tensiometer_connection_result.setText("Disconnected");
                                textview_tensiometer_connection_result.setTextColor(ContextCompat.getColor(MultipleConnectionActivity.this, android.R.color.holo_red_dark));
                                break;
                            case LifevitSDKConstants.STATUS_SCANNING:
                                button_connect_tensiometer.setText("Stop scan");
                                isDisconnectedTensiometer = false;
                                textview_tensiometer_connection_result.setText("Scanning");
                                textview_tensiometer_connection_result.setTextColor(ContextCompat.getColor(MultipleConnectionActivity.this, android.R.color.holo_blue_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTING:
                                button_connect_tensiometer.setText("Disconnect");
                                isDisconnectedTensiometer = false;
                                textview_tensiometer_connection_result.setText("Connecting");
                                textview_tensiometer_connection_result.setTextColor(ContextCompat.getColor(MultipleConnectionActivity.this, android.R.color.holo_orange_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTED:
                                button_connect_tensiometer.setText("Disconnect");
                                isDisconnectedTensiometer = false;
                                textview_tensiometer_connection_result.setText("Connected");
                                textview_tensiometer_connection_result.setTextColor(ContextCompat.getColor(MultipleConnectionActivity.this, android.R.color.holo_green_dark));
                                break;
                        }
                    }

                });
            }
        };

        // Create connection helper
        SDKTestApplication.getInstance().getLifevitSDKManager().addDeviceListener(cl);
    }


    @Override
    protected void onStop() {
        super.onStop();
        SDKTestApplication.getInstance().getLifevitSDKManager().removeDeviceListener(null);
    }


}
