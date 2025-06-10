package es.lifevit.sdk.sampleapp.activities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Locale;

import es.lifevit.sdk.LifevitSDKConstants;
import es.lifevit.sdk.LifevitSDKManager;
import es.lifevit.sdk.listeners.LifevitSDKDeviceListener;
import es.lifevit.sdk.listeners.LifevitSDKGlucometerListener;
import es.lifevit.sdk.sampleapp.SDKTestApplication;
import es.lifevit.sdk.sampleapp.databinding.ActivityGlucometerBinding;

@SuppressLint("SetTextI18n")
public class GlucometerActivity extends AppCompatActivity {

    boolean isDisconnected = true;
    private LifevitSDKDeviceListener cl;
    private ActivityGlucometerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGlucometerBinding.inflate(getLayoutInflater());

        initListeners();

        setContentView(binding.getRoot());
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean glucometerConnected = SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_GLUCOMETER);
        binding.connect.setText(glucometerConnected ? "Disconnect" : "Connect");
        isDisconnected = !glucometerConnected;
        binding.connectionResult.setText(glucometerConnected ? "Connected" : "Disconnected");
        binding.connectionResult.setTextColor(ContextCompat.getColor(this, glucometerConnected ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
        binding.command.setVisibility(glucometerConnected ? VISIBLE : GONE);

        initSdk();
    }

    private void initListeners() {
        binding.connect.setOnClickListener(_ -> {
            if (isDisconnected) {
                SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_GLUCOMETER, 600000);
            } else {
                SDKTestApplication.getInstance().getLifevitSDKManager().disconnectDevice(LifevitSDKConstants.DEVICE_GLUCOMETER);
            }
        });

        binding.command.setOnClickListener(_ -> {
            if (!isDisconnected) {
                final LifevitSDKManager manager = SDKTestApplication.getInstance().getLifevitSDKManager();

                CharSequence[] colors = new CharSequence[]{
                        "0. Send command INFO to Glucomenter",
                        "1. Send command START_PACKET to Glucomenter",
                        "2. Send command PROCEDURE to Glucomenter",
                        "3. Send command RESULT to Glucomenter",
                        "4. Send command END_PACKET to Glucomenter",
                        "5. Send command CONFIRM to Glucomenter",
                        "6. Send command END to Glucomenter"
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(GlucometerActivity.this);
                builder.setTitle("Select command");
                builder.setItems(colors, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            manager.sendGlucometerCommand(LifevitSDKConstants.GlucometerCommand.INFO);
                            break;
                        case 1:
                            manager.sendGlucometerCommand(LifevitSDKConstants.GlucometerCommand.START_PACKET);
                            break;
                        case 2:
                            manager.sendGlucometerCommand(LifevitSDKConstants.GlucometerCommand.PROCEDURE);
                            break;
                        case 3:
                            manager.sendGlucometerCommand(LifevitSDKConstants.GlucometerCommand.RESULT);
                            break;
                        case 4:
                            manager.sendGlucometerCommand(LifevitSDKConstants.GlucometerCommand.END_PACKET);
                            break;
                        case 5:
                            manager.sendGlucometerCommand(LifevitSDKConstants.GlucometerCommand.CONFIRM);
                            break;
                        case 6:
                            manager.sendGlucometerCommand(LifevitSDKConstants.GlucometerCommand.END);
                            break;


                    }
                });
                builder.show();
            }

        });
    }


    private void initSdk() {

        // Create connection listener
        cl = new LifevitSDKDeviceListener() {

            @Override
            public void deviceOnConnectionError(int deviceType, final int errorCode) {
                if (deviceType != LifevitSDKConstants.DEVICE_GLUCOMETER) {
                    return;
                }
                runOnUiThread(() -> {
                    if (errorCode == LifevitSDKConstants.CODE_LOCATION_DISABLED) {
                        binding.connectionResult.setText("ERROR: Debe activar permisos localización");
                    } else if (errorCode == LifevitSDKConstants.CODE_BLUETOOTH_DISABLED) {
                        binding.connectionResult.setText("ERROR: El bluetooth no está activado");
                    } else if (errorCode == LifevitSDKConstants.CODE_LOCATION_TURN_OFF) {
                        binding.connectionResult.setText("ERROR: La Ubicación está apagada");
                    } else {
                        binding.connectionResult.setText("ERROR: Desconocido");
                    }
                });
            }

            @Override
            public void deviceOnConnectionChanged(int deviceType, final int status) {
                if (deviceType != LifevitSDKConstants.DEVICE_GLUCOMETER) {
                    return;
                }

                runOnUiThread(() -> {
                    switch (status) {
                        case LifevitSDKConstants.STATUS_DISCONNECTED:
                            binding.connect.setText("Connect");
                            isDisconnected = true;
                            binding.connectionResult.setText("Disconnected");
                            binding.connectionResult.setTextColor(ContextCompat.getColor(GlucometerActivity.this, android.R.color.holo_red_dark));
                            binding.command.setVisibility(GONE);
                            break;
                        case LifevitSDKConstants.STATUS_SCANNING:
                            binding.connect.setText("Stop scan");
                            isDisconnected = false;
                            binding.connectionResult.setText("Scanning");
                            binding.connectionResult.setTextColor(ContextCompat.getColor(GlucometerActivity.this, android.R.color.holo_blue_dark));
                            break;
                        case LifevitSDKConstants.STATUS_CONNECTING:
                            binding.connect.setText("Disconnect");
                            isDisconnected = false;
                            binding.connectionResult.setText("Connecting");
                            binding.connectionResult.setTextColor(ContextCompat.getColor(GlucometerActivity.this, android.R.color.holo_orange_dark));
                            break;
                        case LifevitSDKConstants.STATUS_CONNECTED:
                            binding.connect.setText("Disconnect");
                            isDisconnected = false;
                            binding.connectionResult.setText("Connected");
                            binding.connectionResult.setTextColor(ContextCompat.getColor(GlucometerActivity.this, android.R.color.holo_green_dark));
                            binding.command.setVisibility(VISIBLE);
                            break;
                    }
                });
            }
        };

        LifevitSDKGlucometerListener glucometerListener = new LifevitSDKGlucometerListener() {
            @Override
            public void onGlucometerDeviceResult(final long date, final double value) {
                runOnUiThread(() -> {
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    binding.measurementResult.setText(binding.measurementResult.getText() + "\n" + "[" + dateFormatter.format(date) + "] " + String.format(Locale.getDefault(), "%.2f", value));
                });
            }

            @Override
            public void onGlucometerDeviceError(final int errorCode) {
                runOnUiThread(() -> {
                    switch (errorCode) {
                        case LifevitSDKConstants.THERMOMETER_ERROR_BODY_TEMPERATURE_HIGH:
                            binding.measurementResult.setText("ERROR: Body temperature too high");
                            break;
                        case LifevitSDKConstants.THERMOMETER_ERROR_BODY_TEMPERATURE_LOW:
                            binding.measurementResult.setText("ERROR: Body temperature too low");
                            break;
                        case LifevitSDKConstants.THERMOMETER_ERROR_AMBIENT_TEMPERATURE_HIGH:
                            binding.measurementResult.setText("ERROR: Ambient/Object temperature too high");
                            break;
                        case LifevitSDKConstants.THERMOMETER_ERROR_AMBIENT_TEMPERATURE_LOW:
                            binding.measurementResult.setText("ERROR: Ambient/Object temperature too low");
                            break;
                        case LifevitSDKConstants.THERMOMETER_ERROR_HARDWARE:
                            binding.measurementResult.setText("ERROR: Hardware error");
                            break;
                        case LifevitSDKConstants.THERMOMETER_ERROR_LOW_VOLTAGE:
                            binding.measurementResult.setText("ERROR: Low voltage error");
                            break;
                        default:
                            binding.measurementResult.setText("ERROR: Unknown error");
                            break;
                    }
                });
            }
        };

        // Create connection helper
        SDKTestApplication.getInstance().getLifevitSDKManager().addDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setGlucometerListener(glucometerListener);
    }


    @Override
    protected void onStop() {
        super.onStop();
        SDKTestApplication.getInstance().getLifevitSDKManager().removeDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setGlucometerListener(null);
    }
}