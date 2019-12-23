package es.lifevit.sdk.sampleapp.activities;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import es.lifevit.sdk.LifevitSDKConstants;
import es.lifevit.sdk.listeners.LifevitSDKDeviceListener;
import es.lifevit.sdk.sampleapp.R;
import es.lifevit.sdk.sampleapp.SDKTestApplication;

public class MultipleConnectionActivity2 extends AppCompatActivity {


    TextView multiple_connection_tensiometer_connection_result, multiple_connection_at500hr_connection_result,
            multiple_connection_at250_connection_result, multiple_connection_oximeter_connection_result,
            multiple_connection_tensiobracelet_connection_result, multiple_connection_thermometer_connection_result,
            multiple_connection_weight_scale_connection_result, multiple_connection_baby_temp_connection_result;

    Button button_connect;


    boolean isDisconnectedBracelet = true, isDisconnectedThermometer = true, isDisconnectedOximeter = true, isDisconnectedTensiometer = true,
            isDisconnectedBraceletAt250 = true, isDisconnectedTensiobracelet = true, isDisconnectedWeightScale = true, isDisconnectedBabyTemp = true;
    private LifevitSDKDeviceListener cl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_connection_2);

        initComponents();
        initListeners();
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_BRACELET_AT500HR)) {
//            button_connect.setText("Disconnect");
            isDisconnectedBracelet = false;
            multiple_connection_at500hr_connection_result.setText("Connected");
            multiple_connection_at500hr_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
//            button_connect.setText("Connect");
            isDisconnectedBracelet = true;
            multiple_connection_at500hr_connection_result.setText("Disconnected");
            multiple_connection_at500hr_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }

        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_THERMOMETER)) {
            isDisconnectedThermometer = false;
            multiple_connection_thermometer_connection_result.setText("Connected");
            multiple_connection_thermometer_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            isDisconnectedThermometer = true;
            multiple_connection_thermometer_connection_result.setText("Disconnected");
            multiple_connection_thermometer_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }

        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_OXIMETER)) {
            isDisconnectedOximeter = false;
            multiple_connection_oximeter_connection_result.setText("Connected");
            multiple_connection_oximeter_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            isDisconnectedOximeter = true;
            multiple_connection_oximeter_connection_result.setText("Disconnected");
            multiple_connection_oximeter_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }

        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_TENSIOMETER)) {
            isDisconnectedTensiometer = false;
            multiple_connection_tensiometer_connection_result.setText("Connected");
            multiple_connection_tensiometer_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            isDisconnectedTensiometer = true;
            multiple_connection_tensiometer_connection_result.setText("Disconnected");
            multiple_connection_tensiometer_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }


        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_BRACELET_AT250)) {
            isDisconnectedBraceletAt250 = false;
            multiple_connection_at250_connection_result.setText("Connected");
            multiple_connection_at250_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            isDisconnectedBraceletAt250 = true;
            multiple_connection_at250_connection_result.setText("Disconnected");
            multiple_connection_at250_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }

        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_TENSIOBRACELET)) {
            isDisconnectedTensiobracelet = false;
            multiple_connection_tensiobracelet_connection_result.setText("Connected");
            multiple_connection_tensiobracelet_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            isDisconnectedTensiobracelet = true;
            multiple_connection_tensiobracelet_connection_result.setText("Disconnected");
            multiple_connection_tensiobracelet_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }

        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_WEIGHT_SCALE)) {
            isDisconnectedWeightScale = false;
            multiple_connection_weight_scale_connection_result.setText("Connected");
            multiple_connection_weight_scale_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            isDisconnectedWeightScale = true;
            multiple_connection_weight_scale_connection_result.setText("Disconnected");
            multiple_connection_weight_scale_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }

        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_BABY_TEMP_BT125)) {
            isDisconnectedBabyTemp = false;
            multiple_connection_baby_temp_connection_result.setText("Connected");
            multiple_connection_baby_temp_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            isDisconnectedBabyTemp = true;
            multiple_connection_baby_temp_connection_result.setText("Disconnected");
            multiple_connection_baby_temp_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }

        initSdk();
    }

    private void initComponents() {

        multiple_connection_tensiometer_connection_result = findViewById(R.id.multiple_connection_tensiometer_connection_result);
        multiple_connection_at500hr_connection_result = findViewById(R.id.multiple_connection_at500hr_connection_result);
        multiple_connection_at250_connection_result = findViewById(R.id.multiple_connection_at250_connection_result);
        multiple_connection_oximeter_connection_result = findViewById(R.id.multiple_connection_oximeter_connection_result);
        multiple_connection_tensiobracelet_connection_result = findViewById(R.id.multiple_connection_tensiobracelet_connection_result);
        multiple_connection_thermometer_connection_result = findViewById(R.id.multiple_connection_thermometer_connection_result);
        multiple_connection_weight_scale_connection_result = findViewById(R.id.multiple_connection_weight_scale_connection_result);
        multiple_connection_baby_temp_connection_result = findViewById(R.id.multiple_connection_baby_temp_connection_result);

        button_connect = findViewById(R.id.multiple_connection_button_connect);
    }


    private void initListeners() {

        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int scanPeriod = 50000;

                SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_TENSIOMETER, scanPeriod);
                SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_BRACELET_AT250, scanPeriod);
                SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_BRACELET_AT500HR, scanPeriod);
                SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_OXIMETER, scanPeriod);
                SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_TENSIOBRACELET, scanPeriod);
                SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_THERMOMETER, scanPeriod);
                SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_WEIGHT_SCALE, scanPeriod);
                SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_BABY_TEMP_BT125, scanPeriod);
            }
        });
    }


    private void initSdk() {

        // Create connection listener
        cl = new LifevitSDKDeviceListener() {

            @Override
            public void deviceOnConnectionError(final int deviceType, final int errorCode) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        TextView textView = null;
                        if (deviceType == LifevitSDKConstants.DEVICE_BRACELET_AT500HR) {
                            textView = multiple_connection_at500hr_connection_result;
                        } else if (deviceType == LifevitSDKConstants.DEVICE_THERMOMETER) {
                            textView = multiple_connection_thermometer_connection_result;
                        } else if (deviceType == LifevitSDKConstants.DEVICE_OXIMETER) {
                            textView = multiple_connection_oximeter_connection_result;
                        } else if (deviceType == LifevitSDKConstants.DEVICE_TENSIOMETER) {
                            textView = multiple_connection_tensiometer_connection_result;
                        } else if (deviceType == LifevitSDKConstants.DEVICE_BRACELET_AT250) {
                            textView = multiple_connection_at250_connection_result;
                        } else if (deviceType == LifevitSDKConstants.DEVICE_TENSIOBRACELET) {
                            textView = multiple_connection_tensiobracelet_connection_result;
                        } else if (deviceType == LifevitSDKConstants.DEVICE_WEIGHT_SCALE) {
                            textView = multiple_connection_weight_scale_connection_result;
                        } else if (deviceType == LifevitSDKConstants.DEVICE_BABY_TEMP_BT125) {
                            textView = multiple_connection_baby_temp_connection_result;
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
                    }
                });
            }

            @Override
            public void deviceOnConnectionChanged(final int deviceType, final int status) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        TextView textView = null;
                        String text = "";
                        int color = -1;
                        boolean connStatus = false;

                        switch (deviceType) {
                            case LifevitSDKConstants.DEVICE_BRACELET_AT500HR:
                                textView = multiple_connection_at500hr_connection_result;
                                break;
                            case LifevitSDKConstants.DEVICE_THERMOMETER:
                                textView = multiple_connection_thermometer_connection_result;
                                break;
                            case LifevitSDKConstants.DEVICE_OXIMETER:
                                textView = multiple_connection_oximeter_connection_result;
                                break;
                            case LifevitSDKConstants.DEVICE_TENSIOMETER:
                                textView = multiple_connection_tensiometer_connection_result;
                                break;
                            case LifevitSDKConstants.DEVICE_BRACELET_AT250:
                                textView = multiple_connection_at250_connection_result;
                                break;
                            case LifevitSDKConstants.DEVICE_TENSIOBRACELET:
                                textView = multiple_connection_tensiobracelet_connection_result;
                                break;
                            case LifevitSDKConstants.DEVICE_WEIGHT_SCALE:
                                textView = multiple_connection_weight_scale_connection_result;
                                break;
                            case LifevitSDKConstants.DEVICE_BABY_TEMP_BT125:
                                textView = multiple_connection_baby_temp_connection_result;
                                break;
                        }

                        switch (status) {
                            case LifevitSDKConstants.STATUS_DISCONNECTED:
//                                    button_connect.setText("Connect");
                                connStatus = true;
                                text = "Disconnected";
                                textView.setTextColor(ContextCompat.getColor(MultipleConnectionActivity2.this, android.R.color.holo_red_dark));
                                break;
                            case LifevitSDKConstants.STATUS_SCANNING:
//                                    button_connect.setText("Stop scan");
                                connStatus = false;
                                text = "Scanning";
                                color = ContextCompat.getColor(MultipleConnectionActivity2.this, android.R.color.holo_blue_dark);
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTING:
//                                    button_connect.setText("Disconnect");
                                connStatus = false;
                                text = "Connecting";
                                color = ContextCompat.getColor(MultipleConnectionActivity2.this, android.R.color.holo_orange_dark);
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTED:
//                                    button_connect.setText("Disconnect");
                                connStatus = false;
                                text = "Connected";
                                color = ContextCompat.getColor(MultipleConnectionActivity2.this, android.R.color.holo_green_dark);
                                break;
                        }

                        if (textView != null && !text.isEmpty()) {
                            textView.setText(text);
                        }

                        if (textView != null && color != -1) {
                            textView.setTextColor(color);
                        }

                        switch (deviceType) {
                            case LifevitSDKConstants.DEVICE_BRACELET_AT500HR:
                                isDisconnectedBracelet = connStatus;
                                break;
                            case LifevitSDKConstants.DEVICE_THERMOMETER:
                                isDisconnectedThermometer = connStatus;
                                break;
                            case LifevitSDKConstants.DEVICE_OXIMETER:
                                isDisconnectedOximeter = connStatus;
                                break;
                            case LifevitSDKConstants.DEVICE_TENSIOMETER:
                                isDisconnectedTensiometer = connStatus;
                                break;
                            case LifevitSDKConstants.DEVICE_BRACELET_AT250:
                                isDisconnectedBraceletAt250 = connStatus;
                                break;
                            case LifevitSDKConstants.DEVICE_TENSIOBRACELET:
                                isDisconnectedTensiobracelet = connStatus;
                                break;
                            case LifevitSDKConstants.DEVICE_WEIGHT_SCALE:
                                isDisconnectedWeightScale = connStatus;
                                break;
                            case LifevitSDKConstants.DEVICE_BABY_TEMP_BT125:
                                isDisconnectedBabyTemp = connStatus;
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
        SDKTestApplication.getInstance().getLifevitSDKManager().removeDeviceListener(cl);
    }


}
