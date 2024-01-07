package com.example.bluetooth_chat_java_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mAdapter;
    private static final int REQUEST_ENABLE_CODE = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 2;
    private ArrayAdapter<String> mDiscoveredDevicesArrayAdapter;
    private ArrayAdapter<String> mArrayAdapter;
    private Button scanBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanBtn = findViewById(R.id.scanBtn);

        mAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!mAdapter.isEnabled()) {
                // Bluetooth is not enabled, request to turn it on
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_CODE);
            } else {
                // Bluetooth is already enabled, fetch paired devices
                showPairedDevices();
            }
        }
    }
    private void showPairedDevices() {
        Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();
        ArrayList<String> devicesList = new ArrayList<>();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();
                devicesList.add(deviceName + "\n" + deviceAddress);
            }
        } else {
            devicesList.add("No paired devices found");
        }

        mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devicesList);
        ListView listView = findViewById(R.id.listView);



        listView.setAdapter(mArrayAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_CODE) {
            if (resultCode == RESULT_OK) {
                // User enabled Bluetooth
                Toast.makeText(this, "Bluetooth is Enabled", Toast.LENGTH_SHORT).show();
                showPairedDevices();
            } else if (resultCode == RESULT_CANCELED) {
                // User didn't enable Bluetooth
                Toast.makeText(this, "Bluetooth is not Enabled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}
