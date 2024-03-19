package es.lifevit.sdk.sampleapp.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import es.lifevit.sdk.LifevitSDKConstants;
import es.lifevit.sdk.LifevitSDKManager;
import es.lifevit.sdk.listeners.LifevitSDKDeviceListener;
import es.lifevit.sdk.listeners.LifevitSDKThermometerListener;
import es.lifevit.sdk.sampleapp.R;
import es.lifevit.sdk.sampleapp.SDKTestApplication;

public class ThermometerActivity extends AppCompatActivity {


    TextView textview_connection_result, textview_measurement_result, textview_measurement_info;
    Button button_connect, button_command;

    boolean isDisconnected = true;
    private LifevitSDKDeviceListener cl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thermometer);

        initComponents();
        initListeners();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (SDKTestApplication.getInstance().getLifevitSDKManager().isDeviceConnected(LifevitSDKConstants.DEVICE_THERMOMETER)) {
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

        button_connect.setOnClickListener(view -> {
            if (isDisconnected) {
                SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_THERMOMETER, 60000);
            } else {
                SDKTestApplication.getInstance().getLifevitSDKManager().disconnectDevice(LifevitSDKConstants.DEVICE_THERMOMETER);
            }
        });

        button_command.setOnClickListener(view -> {
            if (!isDisconnected) {
                final LifevitSDKManager manager = SDKTestApplication.getInstance().getLifevitSDKManager();

                CharSequence[] colors = new CharSequence[]{
                        "0. Set to Celsius",
                        "1. Set to farenheit",
                        "2. Get Last Measurement",
                        "3. Get version number",
                        "4. Shutdown"
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ThermometerActivity.this);
                builder.setTitle("Select command");
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                manager.sendThermometerCommand(LifevitSDKConstants.THERMOMETERV2_COMMAND_CELSIUS);
                                break;
                            case 1:
                                manager.sendThermometerCommand(LifevitSDKConstants.THERMOMETERV2_COMMAND_FARENHEIT);
                                break;
                            case 2:
                                manager.sendThermometerCommand(LifevitSDKConstants.THERMOMETERV2_COMMAND_LAST_MEASURE);
                                break;
                            case 3:
                                manager.sendThermometerCommand(LifevitSDKConstants.THERMOMETERV2_COMMAND_VERSION_NUMBER);
                                break;
                            case 4:
                                manager.sendThermometerCommand(LifevitSDKConstants.THERMOMETERV2_COMMAND_SHUTDOWN);
                                break;

                        }
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
                if (deviceType != LifevitSDKConstants.DEVICE_THERMOMETER) {
                    return;
                }
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
                if (deviceType != LifevitSDKConstants.DEVICE_THERMOMETER) {
                    return;
                }

                runOnUiThread(() -> {
                    switch (status) {
                        case LifevitSDKConstants.STATUS_DISCONNECTED:
                            button_connect.setText("Connect");
                            isDisconnected = true;
                            textview_connection_result.setText("Disconnected");
                            textview_connection_result.setTextColor(ContextCompat.getColor(ThermometerActivity.this, android.R.color.holo_red_dark));
                            button_command.setVisibility(View.GONE);
                            break;
                        case LifevitSDKConstants.STATUS_SCANNING:
                            button_connect.setText("Stop scan");
                            isDisconnected = false;
                            textview_connection_result.setText("Scanning");
                            textview_connection_result.setTextColor(ContextCompat.getColor(ThermometerActivity.this, android.R.color.holo_blue_dark));
                            break;
                        case LifevitSDKConstants.STATUS_CONNECTING:
                            button_connect.setText("Disconnect");
                            isDisconnected = false;
                            textview_connection_result.setText("Connecting");
                            textview_connection_result.setTextColor(ContextCompat.getColor(ThermometerActivity.this, android.R.color.holo_orange_dark));
                            break;
                        case LifevitSDKConstants.STATUS_CONNECTED:
                            button_connect.setText("Disconnect");
                            isDisconnected = false;
                            textview_connection_result.setText("Connected");
                            textview_connection_result.setTextColor(ContextCompat.getColor(ThermometerActivity.this, android.R.color.holo_green_dark));
                            button_command.setVisibility(View.VISIBLE);
                            break;
                    }
                });
            }
        };

        LifevitSDKThermometerListener thermometerListener = new LifevitSDKThermometerListener() {
            @Override
            public void onThermometerDeviceResult(final int thermometerMode, final int temperatureUnit, final double temperatureValue) {
                runOnUiThread(() -> {
                    textview_measurement_result.setText(String.format("%.2f", temperatureValue)
                            + (temperatureUnit == LifevitSDKConstants.TEMPERATURE_UNIT_CELSIUS ? " ºC" : " ºF"));

                    if(thermometerMode == LifevitSDKConstants.THERMOMETER_MODE_BODY){
                        textview_measurement_info.setText("Body measurement");
                    }else if(thermometerMode == LifevitSDKConstants.THERMOMETER_MODE_EAR){
                        textview_measurement_info.setText("Ear measurement");
                    }else if(thermometerMode == LifevitSDKConstants.THERMOMETER_MODE_FOREHEAD){
                        textview_measurement_info.setText("Forehead measurement");
                    }else{
                        textview_measurement_info.setText("Ambient/Object measurement");
                    }
                });
            }

            @Override
            public void onThermometerCommandSuccess(final int command, final int data) {
                runOnUiThread(() -> {
                    switch (command){
                        case LifevitSDKConstants.THERMOMETER_SUCCESS_UNIT:
                            textview_measurement_result.setText("Command success: Changed unit" );
                            break;

                       /* case LifevitSDKConstants.THERMOMETER_SUCCESS_HISTORY:
                            textview_measurement_result.setText("Command success: History" );
                            break;*/
                        case LifevitSDKConstants.THERMOMETER_SUCCESS_SHUTDOWN:
                            textview_measurement_result.setText("Command success: Shutdown" );
                            break;
                        case LifevitSDKConstants.THERMOMETER_SUCCESS_VERSION:
                            textview_measurement_result.setText("Command success: Version number:" + data );
                            break;

                    }
                });
            }

            @Override
            public void onThermometerDeviceError(final int errorCode) {
                runOnUiThread(() -> {
                    switch (errorCode){
                        case LifevitSDKConstants.THERMOMETER_ERROR_BODY_TEMPERATURE_HIGH:
                            textview_measurement_result.setText("ERROR: Body temperature too high" );
                            break;
                        case LifevitSDKConstants.THERMOMETER_ERROR_BODY_TEMPERATURE_LOW:
                            textview_measurement_result.setText("ERROR: Body temperature too low" );
                            break;
                        case LifevitSDKConstants.THERMOMETER_ERROR_AMBIENT_TEMPERATURE_HIGH:
                            textview_measurement_result.setText("ERROR: Ambient/Object temperature too high" );
                            break;
                        case LifevitSDKConstants.THERMOMETER_ERROR_AMBIENT_TEMPERATURE_LOW:
                            textview_measurement_result.setText("ERROR: Ambient/Object temperature too low" );
                            break;
                        case LifevitSDKConstants.THERMOMETER_ERROR_HARDWARE:
                            textview_measurement_result.setText("ERROR: Hardware error" );
                            break;
                        case LifevitSDKConstants.THERMOMETER_ERROR_LOW_VOLTAGE:
                            textview_measurement_result.setText("ERROR: Low voltage error" );
                            break;
                        default:
                            textview_measurement_result.setText("ERROR: Unknown error" );
                            break;
                    }
                });
            }
        };

        // Create connection helper
        SDKTestApplication.getInstance().getLifevitSDKManager().addDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setThermometerListener(thermometerListener);
    }


    @Override
    protected void onStop() {
        super.onStop();
        SDKTestApplication.getInstance().getLifevitSDKManager().removeDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setThermometerListener(null);
    }


}
