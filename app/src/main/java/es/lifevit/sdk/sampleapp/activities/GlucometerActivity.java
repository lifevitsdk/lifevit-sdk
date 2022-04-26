package es.lifevit.sdk.sampleapp.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;

import es.lifevit.sdk.LifevitSDKConstants;
import es.lifevit.sdk.LifevitSDKManager;
import es.lifevit.sdk.listeners.LifevitSDKDeviceListener;
import es.lifevit.sdk.listeners.LifevitSDKGlucometerListener;
import es.lifevit.sdk.listeners.LifevitSDKThermometerListener;
import es.lifevit.sdk.sampleapp.R;
import es.lifevit.sdk.sampleapp.SDKTestApplication;

public class GlucometerActivity extends AppCompatActivity {


    TextView textview_connection_result, textview_measurement_result, textview_measurement_info;
    Button button_connect, button_command;

    boolean isDisconnected = true;
    private LifevitSDKDeviceListener cl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glucometer);

        initComponents();
        initListeners();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_GLUCOMETER)) {
            button_connect.setText("Disconnect");
            isDisconnected = false;
            textview_connection_result.setText("Connected");
            textview_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            button_command.setVisibility(View.VISIBLE);
        } else {
            button_connect.setText("Connect");
            isDisconnected = true;
            textview_connection_result.setText("Disconnected");
            textview_connection_result.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            button_command.setVisibility(View.GONE);
        }

        initSdk();
    }


    private void initComponents() {
        textview_connection_result = findViewById(R.id.thermometer_textview_connection_result);
        textview_measurement_result = findViewById(R.id.thermometer_textview_measurement_result);
        textview_measurement_info = findViewById(R.id.thermometer_textview_measurement_info);

        button_connect = findViewById(R.id.thermometer_button_connect);
        button_command = findViewById(R.id.thermometer_button_command);
    }


    private void initListeners() {

        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDisconnected) {
                    SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_GLUCOMETER, 600000);
                } else {
                    SDKTestApplication.getInstance().getLifevitSDKManager().disconnectDevice(LifevitSDKConstants.DEVICE_GLUCOMETER);
                }
            }
        });

        button_command.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    builder.setItems(colors, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
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
                        }
                    });
                    builder.show();
                }

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
                if (deviceType != LifevitSDKConstants.DEVICE_GLUCOMETER) {
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
                                textview_connection_result.setTextColor(ContextCompat.getColor(GlucometerActivity.this, android.R.color.holo_red_dark));
                                button_command.setVisibility(View.GONE);
                                break;
                            case LifevitSDKConstants.STATUS_SCANNING:
                                button_connect.setText("Stop scan");
                                isDisconnected = false;
                                textview_connection_result.setText("Scanning");
                                textview_connection_result.setTextColor(ContextCompat.getColor(GlucometerActivity.this, android.R.color.holo_blue_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTING:
                                button_connect.setText("Disconnect");
                                isDisconnected = false;
                                textview_connection_result.setText("Connecting");
                                textview_connection_result.setTextColor(ContextCompat.getColor(GlucometerActivity.this, android.R.color.holo_orange_dark));
                                break;
                            case LifevitSDKConstants.STATUS_CONNECTED:
                                button_connect.setText("Disconnect");
                                isDisconnected = false;
                                textview_connection_result.setText("Connected");
                                textview_connection_result.setTextColor(ContextCompat.getColor(GlucometerActivity.this, android.R.color.holo_green_dark));
                                button_command.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                });
            }
        };

        LifevitSDKGlucometerListener glucometerListener = new LifevitSDKGlucometerListener() {
            @Override
            public void onGlucometerDeviceResult(final long date, final double value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        textview_measurement_result.setText(textview_measurement_result.getText() + "\n" +
                                "[" + dateFormatter.format(date) + "] " + String.format("%.2f", value));
                    }
                });
            }

            @Override
            public void onGlucometerDeviceError(final int errorCode) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (errorCode) {
                            case LifevitSDKConstants.THERMOMETER_ERROR_BODY_TEMPERATURE_HIGH:
                                textview_measurement_result.setText("ERROR: Body temperature too high");
                                break;
                            case LifevitSDKConstants.THERMOMETER_ERROR_BODY_TEMPERATURE_LOW:
                                textview_measurement_result.setText("ERROR: Body temperature too low");
                                break;
                            case LifevitSDKConstants.THERMOMETER_ERROR_AMBIENT_TEMPERATURE_HIGH:
                                textview_measurement_result.setText("ERROR: Ambient/Object temperature too high");
                                break;
                            case LifevitSDKConstants.THERMOMETER_ERROR_AMBIENT_TEMPERATURE_LOW:
                                textview_measurement_result.setText("ERROR: Ambient/Object temperature too low");
                                break;
                            case LifevitSDKConstants.THERMOMETER_ERROR_HARDWARE:
                                textview_measurement_result.setText("ERROR: Hardware error");
                                break;
                            case LifevitSDKConstants.THERMOMETER_ERROR_LOW_VOLTAGE:
                                textview_measurement_result.setText("ERROR: Low voltage error");
                                break;
                            default:
                                textview_measurement_result.setText("ERROR: Unknown error");
                                break;
                        }
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
