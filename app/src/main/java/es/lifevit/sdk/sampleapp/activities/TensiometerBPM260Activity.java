package es.lifevit.sdk.sampleapp.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import es.lifevit.sdk.LifevitSDKConstants;
import es.lifevit.sdk.LifevitSDKHeartData;
import es.lifevit.sdk.listeners.LifevitSDKDeviceListener;
import es.lifevit.sdk.listeners.LifevitSDKHeartListener;
import es.lifevit.sdk.sampleapp.SDKTestApplication;
import es.lifevit.sdk.sampleapp.databinding.ActivityTensiometerBpm260Binding;

@SuppressLint("SetTextI18n")
public class TensiometerBPM260Activity extends AppCompatActivity {

    private static final String TAG = TensiometerBPM260Activity.class.getSimpleName();

    boolean isDisconnected = true;
    String lastDeviceConnectedAddress = "";

    private LifevitSDKDeviceListener cl;

    private ActivityTensiometerBpm260Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTensiometerBpm260Binding.inflate(getLayoutInflater());
        initListeners();
        setContentView(binding.getRoot());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_TENSIOMETER)) {
            binding.connect.setText("Disconnect");
            isDisconnected = false;
            binding.result.setText("Connected");
            binding.result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            binding.connect.setText("Connect");
            isDisconnected = true;
            binding.result.setText("Disconnected");
            binding.result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }
        initSdk();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SDKTestApplication.getInstance().getLifevitSDKManager().removeDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setHeartListener(null);
    }

    private void initListeners() {

        binding.connect.setOnClickListener(view -> {
            if (isDisconnected) {
                SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_TENSIOMETER, 10000);
            } else {
                SDKTestApplication.getInstance().getLifevitSDKManager().disconnectDevice(LifevitSDKConstants.DEVICE_TENSIOMETER);
            }
        });

        binding.start.setOnClickListener(view -> SDKTestApplication.getInstance().getLifevitSDKManager().startMeasurement());
    }

    private void initSdk() {

        // Create listener
        cl = new LifevitSDKDeviceListener() {

            @Override
            public void deviceOnConnectionError(int deviceType, final int errorCode) {
                runOnUiThread(() -> {
                    if (errorCode == LifevitSDKConstants.CODE_LOCATION_DISABLED) {
                        binding.result.setText("ERROR: Debe activar permisos localización");
                    } else if (errorCode == LifevitSDKConstants.CODE_BLUETOOTH_DISABLED) {
                        binding.result.setText("ERROR: El bluetooth no está activado");
                    } else if (errorCode == LifevitSDKConstants.CODE_LOCATION_TURN_OFF) {
                        binding.result.setText("ERROR: La Ubicación está apagada");
                    } else {
                        binding.result.setText("ERROR: Desconocido");
                    }
                });
            }

            @Override
            public void deviceOnConnectionChanged(int deviceType, final int status) {
                runOnUiThread(() -> {

                    Log.d(TAG, "Status changed: " + status);

                    switch (status) {
                        case LifevitSDKConstants.STATUS_DISCONNECTED:
                            binding.connect.setText("Connect");
                            isDisconnected = true;
                            binding.result.setText("Disconnected");
                            binding.result.setTextColor(ContextCompat.getColor(TensiometerBPM260Activity.this, android.R.color.holo_red_dark));

                            // Start timer again
//                                startTimer();

                            break;
                        case LifevitSDKConstants.STATUS_SCANNING:
                            binding.connect.setText("Stop scan");
                            isDisconnected = false;
                            binding.result.setText("Scanning");
                            binding.result.setTextColor(ContextCompat.getColor(TensiometerBPM260Activity.this, android.R.color.holo_blue_dark));
                            break;
                        case LifevitSDKConstants.STATUS_CONNECTING:
                            binding.connect.setText("Disconnect");
                            isDisconnected = false;
                            binding.result.setText("Connecting");
                            binding.result.setTextColor(ContextCompat.getColor(TensiometerBPM260Activity.this, android.R.color.holo_orange_dark));

                            break;
                        case LifevitSDKConstants.STATUS_CONNECTED:
                            binding.connect.setText("Disconnect");
                            isDisconnected = false;
                            binding.result.setText("Connected");
                            binding.result.setTextColor(ContextCompat.getColor(TensiometerBPM260Activity.this, android.R.color.holo_green_dark));

                            // Save connected device address
                            lastDeviceConnectedAddress = SDKTestApplication.getInstance().getLifevitSDKManager().getDeviceAddress(LifevitSDKConstants.DEVICE_TENSIOMETER);
                            break;
                    }
                });
            }
        };

        SDKTestApplication.getInstance().getLifevitSDKManager().addDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setHeartListener(new LifevitSDKHeartListener() {
            @Override
            public void heartDeviceOnProgressMeasurement(final int pulse) {
                runOnUiThread(() -> {
                    binding.pulse.setText(String.valueOf(pulse));
                    binding.info.setText("Measuring...");
                });
            }


            @Override
            public void heartDeviceOnBatteryResult(final int battery) {
                runOnUiThread(() -> binding.info.setText("Battery charge: " + battery));
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

                        binding.info.setText("Error: " + errorText);
                        binding.systolic.setText("---");
                        binding.diastolic.setText("---");
                        binding.pulse.setText("---");

                    } else {
                        binding.info.setText("Measurement result");
                        binding.systolic.setText(String.valueOf(result.getSystolic()));
                        binding.diastolic.setText(String.valueOf(result.getDiastolic()));
                        binding.pulse.setText(String.valueOf(result.getPulse()));
                    }
                });
            }
        });
    }
}