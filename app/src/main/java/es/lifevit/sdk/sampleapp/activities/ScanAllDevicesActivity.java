package es.lifevit.sdk.sampleapp.activities;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Map;

import es.lifevit.sdk.LifevitSDKConstants;
import es.lifevit.sdk.LifevitSDKDeviceScanData;
import es.lifevit.sdk.listeners.LifevitSDKAllDevicesListener;
import es.lifevit.sdk.listeners.LifevitSDKDeviceListener;
import es.lifevit.sdk.sampleapp.R;
import es.lifevit.sdk.sampleapp.SDKTestApplication;
import es.lifevit.sdk.utils.LogUtils;

public class ScanAllDevicesActivity extends AppCompatActivity {

    private static final String TAG = ScanAllDevicesActivity.class.getSimpleName();

    TextView textview_info, scan_all_connection_result;
    Button button_connect;
    private LifevitSDKDeviceListener cl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_all_devices);

        initComponents();
        initListeners();
        initSdk();
    }


    private void initComponents() {
        textview_info = findViewById(R.id.scan_all_textview_command_info);
        textview_info.setMovementMethod(new ScrollingMovementMethod());

        button_connect = findViewById(R.id.scan_all_connect);
        scan_all_connection_result = findViewById(R.id.scan_all_connection_result);
    }


    private void initListeners() {

        button_connect.setOnClickListener(view -> SDKTestApplication.getInstance().getLifevitSDKManager().connectDevice(LifevitSDKConstants.DEVICE_OTHERS, 10000));
    }


    private void initSdk() {

        // Create listener
        cl = new LifevitSDKDeviceListener() {

            @Override
            public void deviceOnConnectionError(int deviceType, final int errorCode) {

                runOnUiThread(() -> {
                    if (errorCode == LifevitSDKConstants.CODE_LOCATION_DISABLED) {
                        scan_all_connection_result.setText("ERROR: Debe activar permisos localización");
                    } else if (errorCode == LifevitSDKConstants.CODE_BLUETOOTH_DISABLED) {
                        scan_all_connection_result.setText("ERROR: El bluetooth no está activado");
                    } else if (errorCode == LifevitSDKConstants.CODE_LOCATION_TURN_OFF) {
                        scan_all_connection_result.setText("ERROR: La Ubicación está apagada");
                    } else {
                        scan_all_connection_result.setText("ERROR: Desconocido");
                    }
                });

            }

            @Override
            public void deviceOnConnectionChanged(int deviceType, final int status) {

                runOnUiThread(() -> {
                    switch (status) {
                        case LifevitSDKConstants.STATUS_DISCONNECTED:
                            scan_all_connection_result.setText("Disconnected");
                            break;
                        case LifevitSDKConstants.STATUS_SCANNING:
                            scan_all_connection_result.setText("Scanning");
                            break;
                        case LifevitSDKConstants.STATUS_CONNECTING:
                            scan_all_connection_result.setText("Connecting");
                            break;
                        case LifevitSDKConstants.STATUS_CONNECTED:
                            scan_all_connection_result.setText("Connected");
                            break;
                    }
                });
            }
        };

        LifevitSDKAllDevicesListener bListener = allResults -> runOnUiThread(() -> {

            StringBuilder stringBuilder = new StringBuilder();
            for (Map.Entry<Integer, List<LifevitSDKDeviceScanData>> deviceEntry : allResults.entrySet()) {
                stringBuilder.append("Device " + LogUtils.getDeviceNameByType(deviceEntry.getKey()) + ":\n");
                for (LifevitSDKDeviceScanData deviceScanData : deviceEntry.getValue()) {
                    stringBuilder.append("    " + deviceScanData.getAddress() + ", distance: " +
                            String.format("%.1f", deviceScanData.getDistanceMetersCalculated()) + " m\n");
                }
            }

            textview_info.setText(stringBuilder.toString());
        });

        // Create connection helper
        SDKTestApplication.getInstance().getLifevitSDKManager().addDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setAllDevicesListener(bListener);
    }


    @Override
    protected void onStop() {
        super.onStop();
        SDKTestApplication.getInstance().getLifevitSDKManager().removeDeviceListener(cl);
        SDKTestApplication.getInstance().getLifevitSDKManager().setAllDevicesListener(null);
    }


}
