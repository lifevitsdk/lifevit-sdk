package es.lifevit.sdk.sampleapp.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.widget.Button;

import es.lifevit.sdk.sampleapp.R;
import es.lifevit.sdk.sampleapp.activities.base.BaseAppCompatActivity;
import es.lifevit.sdk.sampleapp.databinding.ActivityMainBinding;

public class MainActivity extends BaseAppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        initListeners();
        initPermissions();
        setContentView(binding.getRoot());
    }

    private void initListeners() {
        setActions(
                new Pair<>(binding.pillreminder, PillReminderActivity.class),
                new Pair<>(binding.tensiometer, TensiometerActivity.class),
                new Pair<>(binding.braceletAt500Hr, BraceletAT500HrActivity.class),
                new Pair<>(binding.braceletAt250, BraceletAT250Activity.class),
                new Pair<>(binding.oximeter, OximeterActivity.class),
                new Pair<>(binding.tensiobracelet, TensiobraceletActivity.class),
                new Pair<>(binding.thermometer, ThermometerActivity.class),
                new Pair<>(binding.weightScale, WeightScaleActivity.class),
                new Pair<>(binding.multipleConnection, MultipleConnectionActivity.class),
                new Pair<>(binding.multipleConnection2, MultipleConnectionActivity2.class),
                new Pair<>(binding.babyTempBt125, BabyTempBT125Activity.class),
                new Pair<>(binding.scanAllDevices, ScanAllDevicesActivity.class),
                new Pair<>(binding.braceletVital, BraceletVitalActivity.class),
                new Pair<>(binding.glucometer, GlucometerActivity.class),
                new Pair<>(binding.tensiometerBpm260, TensiometerBPM260Activity.class),
                new Pair<>(binding.tensiometerBpm300, TensiometerBPM300Activity.class),
                new Pair<>(binding.thermometerKelvinPlus, ThermometerKelvinPlusActivity.class)
        );
    }

    /** @noinspection rawtypes*/
    private void setActions(Pair<Button, Class>... buttonactions) {
        for(Pair<Button, Class> buttonaction : buttonactions) {
            buttonaction.first.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), buttonaction.second)));
        }
    }

    private void initPermissions() {
        requestPermissions(
                findViewById(R.id.main_activity_main_view),
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                100,
                R.string.message_need_location_permission,
                false,
                accepted -> { }
        );
    }
}
