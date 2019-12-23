package es.lifevit.sdk.sampleapp.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import es.lifevit.sdk.LifevitSDKConstants;
import es.lifevit.sdk.LifevitSDKManager;
import es.lifevit.sdk.listeners.LifevitSDKDeviceListener;
import es.lifevit.sdk.listeners.LifevitSDKWeightScaleListener;
import es.lifevit.sdk.sampleapp.R;
import es.lifevit.sdk.sampleapp.SDKTestApplication;

public class WeightScaleActivity extends AppCompatActivity {


    TextView textview_connection_result, textview_measurement_info;
    TextView textview_bmr, textview_bone, textview_fat, textview_muscle, textview_visceral, textview_water, textview_weight, textview_info;

    Button button_connect, weight_scale_button_clear_results, weight_scale_button_history;
    private CheckBox weight_scale_check_connected;

    boolean isDisconnected = true;

    private String uuid = null;
    private LifevitSDKDeviceListener cl;

//    private boolean started = false;
//    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_scale);

        initComponents();
        initListeners();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_WEIGHT_SCALE)) {
            button_connect.setText("Disconnect");
            isDisconnected = false;
            textview_connection_result.setText("Connected");
            textview_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        }
        else if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnecting(LifevitSDKConstants.DEVICE_WEIGHT_SCALE)) {
            button_connect.setText("Disconnect");
            isDisconnected = false;
            textview_connection_result.setText("Connecting");
            textview_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        }
        else {
            button_connect.setText("Connect");
            isDisconnected = true;
            textview_connection_result.setText("Disconnected");
            textview_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }

        initSdk();
//        startTimer();
    }


    @Override
    protected void onPause() {
//        stopTimer();
        super.onPause();
    }

//    int times;
//
//
//    private Runnable runnable = new Runnable() {
//        @Override
//        public void run() {
//            LifevitSDKManager lifevitSDKManager;
//            if (times % 2 == 0) {
//                lifevitSDKManager = SDKTestApplication.getInstance().getLifevitSDKManager();
//            } else {
//                lifevitSDKManager = SDKTestApplication.getInstance().getLifevitSDKManager2();
//            }
//
//            lifevitSDKManager.connectDevice(LifevitSDKConstants.DEVICE_WEIGHT_SCALE,
//                    15000, "C8:B2:1E:16:D9:A1");
//
//            startTimer();
//            times++;
//        }
//    };
//
//    public void stopTimer() {
//        started = false;
//        handler.removeCallbacks(runnable);
//    }
//
//    public void startTimer() {
//        started = true;
//        handler.postDelayed(runnable, 5000);
//    }


    private void initComponents() {
        textview_connection_result = findViewById(R.id.weight_scale_textview_connection_result);
        textview_measurement_info = findViewById(R.id.weight_scale_textview_measurement_info);

        textview_bmr = findViewById(R.id.weight_scale_textview_measurement_bmr);
        textview_bone = findViewById(R.id.weight_scale_textview_measurement_bone);
        textview_fat = findViewById(R.id.weight_scale_textview_measurement_fat);
        textview_muscle = findViewById(R.id.weight_scale_textview_measurement_muscle);
        textview_visceral = findViewById(R.id.weight_scale_textview_measurement_visceral);
        textview_water = findViewById(R.id.weight_scale_textview_measurement_water);
        textview_weight = findViewById(R.id.weight_scale_textview_measurement_weight);
        textview_info = findViewById(R.id.weight_scale_textview_measurement_info);


        button_connect = findViewById(R.id.weight_scale_button_connect);
        weight_scale_button_clear_results = findViewById(R.id.weight_scale_button_clear_results);
        weight_scale_button_history = findViewById(R.id.weight_scale_button_history);
        weight_scale_check_connected = findViewById(R.id.weight_scale_check_connected);
    }


    private void initListeners() {

        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDisconnected) {
                    // Set up device
                    SDKTestApplication.getInstance().getLifevitSDKManager().setUpWeightScale(LifevitSDKConstants.WEIGHT_SCALE_GENDER_MALE, 30, 170, LifevitSDKConstants.WEIGHT_UNIT_KG);
                    // Connect
                    if(uuid!=null && weight_scale_check_connected.isChecked()){
                        SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_WEIGHT_SCALE, 100000, uuid);
                    }
                    else {
                        SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_WEIGHT_SCALE, 100000);
                    }
                } else {
                    SDKTestApplication.getInstance().getLifevitSDKManager().disconnectDevice(LifevitSDKConstants.DEVICE_WEIGHT_SCALE);
                }
            }
        });

        weight_scale_button_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SDKTestApplication.getInstance().getLifevitSDKManager().getWeightHistoryData();
            }
        });

        weight_scale_button_clear_results.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textview_bmr.setText("---");
                textview_fat.setText("---");
                textview_muscle.setText("---");
                textview_visceral.setText("---");
                textview_water.setText("---");
                textview_weight.setText("---");
                textview_bone.setText("---");
                textview_info.setText("---");
            }
        });
    }


    private void initSdk() {

        // Create connection listener
        cl = new LifevitSDKDeviceListener() {

            @Override
            public void deviceOnConnectionError(int deviceType, final int errorCode) {
                if (deviceType != LifevitSDKConstants.DEVICE_WEIGHT_SCALE) {
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

                Log.d("WeightScaleActivity", "deviceOnConnectionChanged: Bascula MAC: '" + "' STATUS: '" + status + "'");

                if (deviceType != LifevitSDKConstants.DEVICE_WEIGHT_SCALE) {
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
                                textview_connection_result.setTextColor(ContextCompat.getColor(WeightScaleActivity.this, android.R.color.holo_red_dark));

                                // Set up device
                               SDKTestApplication.getInstance().getLifevitSDKManager().setUpWeightScale(LifevitSDKConstants.WEIGHT_SCALE_GENDER_MALE, 30, 170, LifevitSDKConstants.WEIGHT_UNIT_KG);
                                // Connect
                                if(uuid!=null && weight_scale_check_connected.isChecked()){
                                    SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_WEIGHT_SCALE, 100000, uuid);
                                }
                                else {
                                    SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_WEIGHT_SCALE, 100000);
                                }
                                break;
                            case LifevitSDKConstants.STATUS_SCANNING:
                                button_connect.setText("Stop scan");
                                isDisconnected = false;
                                textview_connection_result.setText("Scanning");
                                textview_connection_result.setTextColor(ContextCompat.getColor(WeightScaleActivity.this, android.R.color.holo_blue_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTING:
                                button_connect.setText("Disconnect");
                                isDisconnected = false;
                                textview_connection_result.setText("Connecting");
                                textview_connection_result.setTextColor(ContextCompat.getColor(WeightScaleActivity.this, android.R.color.holo_orange_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTED:

                                //uuid = SDKTestApplication.getInstance().getLifevitSDKManager().getDeviceAddress(LifevitSDKConstants.DEVICE_WEIGHT_SCALE);

                                button_connect.setText("Disconnect");
                                isDisconnected = false;
                                textview_connection_result.setText("Connected");
                                textview_connection_result.setTextColor(ContextCompat.getColor(WeightScaleActivity.this, android.R.color.holo_green_dark));
                                break;
                        }
                    }
                });
            }
        };

        LifevitSDKWeightScaleListener weightScaleListener = new LifevitSDKWeightScaleListener() {
            @Override
            public void onScaleMeasurementOnlyWeight(final double weight, final int unit) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textview_bmr.setText("");
                        textview_bone.setText("");
                        textview_fat.setText("");
                        textview_muscle.setText("");
                        textview_visceral.setText("");
                        textview_water.setText("");

                        textview_weight.setText(String.format("%.1f %s (measuring)", weight, unit == LifevitSDKConstants.WEIGHT_UNIT_KG ? "Kg" : "Lb"));
                    }
                });
            }

            @Override
            public void onScaleMeasurementAllValues(final double weight, final int unit, final double fatPercentage, final double waterPercentage, final double musclePercent,
                                                    final double bmrKcal, final double visceralPercentage, final double boneRaw) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String unitStr = (unit == LifevitSDKConstants.WEIGHT_UNIT_KG ? "Kg" : "Lb");

                        textview_bmr.setText(String.format("%.1f", bmrKcal) + " Kcal");
                        textview_bone.setText(String.format("%.1f", boneRaw) + " " + unitStr);
                        textview_fat.setText(String.format("%.1f", fatPercentage) + " %");
                        textview_muscle.setText(String.format("%.1f", musclePercent) + " %");
                        textview_visceral.setText(String.format("%.1f", visceralPercentage) + " %");
                        textview_water.setText(String.format("%.1f", waterPercentage) + " %");

                        textview_weight.setText(String.format("%.1f %s", weight, unitStr));
                    }
                });
            }
        };

        // Create connection helper
        SDKTestApplication.getInstance().getLifevitSDKManager().addDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setWeightScaleListener(weightScaleListener);
    }


    @Override
    protected void onStop() {
        super.onStop();
        SDKTestApplication.getInstance().getLifevitSDKManager().removeDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setWeightScaleListener(null);
    }


}
