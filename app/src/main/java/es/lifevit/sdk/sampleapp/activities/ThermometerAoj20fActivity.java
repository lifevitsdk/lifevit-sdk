package es.lifevit.sdk.sampleapp.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import es.lifevit.sdk.LifevitSDKConstants;
import es.lifevit.sdk.LifevitSDKManager;
import es.lifevit.sdk.sampleapp.SDKTestApplication;
import es.lifevit.sdk.sampleapp.databinding.ActivityThermometerAoj20fBinding;

@SuppressLint("SetTextI18n")
public class ThermometerAoj20fActivity extends AppCompatActivity {

    private static final String TAG = ThermometerAoj20fActivity.class.getSimpleName();

    boolean isDisconnected = true;
    String lastDeviceConnectedAddress = "";

    private ActivityThermometerAoj20fBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityThermometerAoj20fBinding.inflate(getLayoutInflater());
        initListeners();
        setContentView(binding.getRoot());
    }

    private void initListeners() {
        binding.connect.setOnClickListener(view -> {
            if (isDisconnected) {
                SDKTestApplication.getInstance().getLifevitSDKManager().connectToAoj20f(
                        new LifevitSDKManager.Aoj20fConnectionListener() {
                            @Override
                            public void statusChanged(int status) {
                                onConnectionChanged(status);
                            }

                            @Override
                            public void onMeasurementTaken(@NotNull Double temperature, @NotNull LifevitSDKManager.ThermometerModes mode) {
                                runOnUiThread(() -> {
                                    binding.info.setText(mode.name());
                                    binding.temperature.setText(Double.toString(temperature));
                                });
                            }
                        }
                );
            } else {
                SDKTestApplication.getInstance().getLifevitSDKManager().disconnectAoj20f();
                onConnectionChanged(LifevitSDKConstants.STATUS_DISCONNECTED);
            }
        });
    }

    private void onConnectionChanged(final int status) {
        runOnUiThread(() -> {

            Log.d(TAG, "Status changed: " + status);

            switch (status) {
                case LifevitSDKConstants.STATUS_DISCONNECTED:
                    binding.connect.setText("Connect");
                    isDisconnected = true;
                    binding.result.setText("Disconnected");
                    binding.result.setTextColor(ContextCompat.getColor(ThermometerAoj20fActivity.this, android.R.color.holo_red_dark));
                    break;
                case LifevitSDKConstants.STATUS_SCANNING:
                    binding.connect.setText("Stop scan");
                    isDisconnected = false;
                    binding.result.setText("Scanning");
                    binding.result.setTextColor(ContextCompat.getColor(ThermometerAoj20fActivity.this, android.R.color.holo_blue_dark));
                    break;
                case LifevitSDKConstants.STATUS_CONNECTING:
                    binding.connect.setText("Disconnect");
                    isDisconnected = false;
                    binding.result.setText("Connecting");
                    binding.result.setTextColor(ContextCompat.getColor(ThermometerAoj20fActivity.this, android.R.color.holo_orange_dark));

                    break;
                case LifevitSDKConstants.STATUS_CONNECTED:
                    binding.connect.setText("Disconnect");
                    isDisconnected = false;
                    binding.result.setText("Connected");
                    binding.result.setTextColor(ContextCompat.getColor(ThermometerAoj20fActivity.this, android.R.color.holo_green_dark));

                    // Save connected device address
                    lastDeviceConnectedAddress = SDKTestApplication.getInstance().getLifevitSDKManager().getDeviceAddress(LifevitSDKConstants.DEVICE_TENSIOMETER);
                    break;
            }
        });
    }

    @Override
    protected void onPause() {
        SDKTestApplication.getInstance().getLifevitSDKManager().disconnectAoj20f();
        onConnectionChanged(LifevitSDKConstants.STATUS_DISCONNECTED);
        super.onPause();
    }
}