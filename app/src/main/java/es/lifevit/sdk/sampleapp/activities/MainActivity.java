package es.lifevit.sdk.sampleapp.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import es.lifevit.sdk.sampleapp.R;
import es.lifevit.sdk.sampleapp.activities.base.BaseAppCompatActivity;

public class MainActivity extends BaseAppCompatActivity {

    Button button_tensiometer, button_bracelet_at500hr, button_bracelet_at250, button_oximeter, button_tensiobracelet,
            button_thermometer, button_weightScale, button_multiple_connection, button_multiple_connection2,
            button_baby_temp_bt125, button_scan_all_devices, button_pillreminder, button_bracelet_vital, button_glucometer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponents();
        initListeners();
        initPermissions();
    }


    private void initComponents() {

        button_tensiometer = findViewById(R.id.button_tensiometer);
        button_bracelet_at500hr = findViewById(R.id.button_bracelet_at500hr);
        button_bracelet_at250 = findViewById(R.id.button_bracelet_at250);
        button_oximeter = findViewById(R.id.button_oximeter);
        button_tensiobracelet = findViewById(R.id.button_tensiobracelet);
        button_thermometer = findViewById(R.id.button_thermometer);
        button_weightScale = findViewById(R.id.button_weightScale);
        button_multiple_connection = findViewById(R.id.button_multiple_connection);
        button_multiple_connection2 = findViewById(R.id.button_multiple_connection2);
        button_baby_temp_bt125 = findViewById(R.id.button_baby_temp_bt125);
        button_scan_all_devices = findViewById(R.id.button_scan_all_devices);
        button_pillreminder = findViewById(R.id.button_pillreminder);
        button_bracelet_vital = findViewById(R.id.button_bracelet_vital);
        button_glucometer = findViewById(R.id.button_glucometer);
    }


    private void initListeners() {

        button_pillreminder.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), PillReminderActivity.class)));
        button_tensiometer.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), TensiometerActivity.class)));
        button_bracelet_at500hr.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), BraceletAT500HrActivity.class)));
        button_bracelet_at250.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), BraceletAT250Activity.class)));
        button_oximeter.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), OximeterActivity.class)));
        button_tensiobracelet.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), TensiobraceletActivity.class)));
        button_thermometer.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), ThermometerActivity.class)));
        button_weightScale.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), WeightScaleActivity.class)));
        button_multiple_connection.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), MultipleConnectionActivity.class)));
        button_multiple_connection2.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), MultipleConnectionActivity2.class)));
        button_baby_temp_bt125.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), BabyTempBT125Activity.class)));
        button_scan_all_devices.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), ScanAllDevicesActivity.class)));
        button_bracelet_vital.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), BraceletVitalActivity.class)));
        button_glucometer.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), GlucometerActivity.class)));

    }


    private void initPermissions() {
        requestPermissions(
                findViewById(R.id.main_activity_main_view),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                100,
                R.string.message_need_location_permission,
                false,
                accepted -> { }
        );
    }

}
