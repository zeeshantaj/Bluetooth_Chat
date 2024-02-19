package com.example.bluetooth_chat_java_app;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.bluetooth_chat_java_app.Controller.ChatController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mAdapter;
    private BluetoothSocket mSocket;
    private static final int REQUEST_ENABLE_CODE = 1;
    private ArrayAdapter<String> mArrayAdapter;
    public static final String DEVICE_OBJECT = "device_name";
    private ProgressBar progressBar;
    private Button btScanBtn;
    private List<BluetoothDevice> deviceList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);
        btScanBtn = findViewById(R.id.btScanBTn);

        deviceList = new ArrayList<>();

        mAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            checkAndRequestPermissions();
            if (!mAdapter.isEnabled()) {
                // Bluetooth is not enabled, request to turn it on
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_CODE);
            } else {
                // Bluetooth is already enabled, fetch paired devices
                showPairedDevices();
            }
        }

        // Register broadcast receiver and start discovery
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
        mAdapter.startDiscovery();


//        ActivityResultLauncher<String[]> locationPermissionRequest =
//                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
//                        result -> {
//                            Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
//                            Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
//                            if (fineLocationGranted || coarseLocationGranted) {
//                                // Location access granted.
//                                // Proceed with Bluetooth scanning or other location-dependent tasks.
//                                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
//                            } else {
//                                // Location access not granted.
//                                Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
//                                // Check if permission is denied permanently
//                                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
//                                        !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
//                                    // Permission denied permanently, prompt user to enable in settings
//                                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                                    builder.setTitle("Permission Required");
//                                    builder.setMessage("Location permission is required for Bluetooth scanning. Please enable it in the app settings.");
//                                    builder.setPositiveButton("Go to Settings", (dialog, which) -> {
//                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
//                                        intent.setData(uri);
//                                        startActivity(intent);
//                                    });
//                                    builder.setNegativeButton("Cancel", null);
//                                    builder.show();
//                                }
//                            }
//                        }
//                );
        btScanBtn.setOnClickListener(v -> {
            // Check and request permissions before starting a new scan
            discoverDevice();
            Toast.makeText(this, "clk", Toast.LENGTH_SHORT).show();
        });
//

    }

    private void discoverDevice() {
        mAdapter.startDiscovery();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                Log.e("MyApp","device available"+deviceName);
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver,intentFilter);
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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String info = mArrayAdapter.getItem(position);
                if (info != null){
                    String address = info.substring(info.length() - 17);
                    Toast.makeText(MainActivity.this, "Item CLicked "+address, Toast.LENGTH_SHORT).show();
                   // connectToDevice(address);
                    ConnectTask connectTask = new ConnectTask();
                    connectTask.execute(address);
                }

            }
        });
    }



    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.BLUETOOTH);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.BLUETOOTH_ADMIN);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), REQUEST_ENABLE_CODE);
        }
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
    private void updateUI() {
        ArrayList<String> deviceNameList = new ArrayList<>();
        for (BluetoothDevice device : deviceList) {
            deviceNameList.add(device.getName() + "\n" + device.getAddress());
        }
        mArrayAdapter.clear();
        mArrayAdapter.addAll(deviceNameList);
        mArrayAdapter.notifyDataSetChanged();
    }
    private void connectToDevice(String deviceAddress) {




        mAdapter.cancelDiscovery();
        BluetoothDevice device = mAdapter.getRemoteDevice(deviceAddress);

        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard UUID for SPP (Serial Port Profile)
            mSocket = device.createRfcommSocketToServiceRecord(uuid);
            mSocket.connect();

            if (mSocket.isConnected()) {
                // Connection successful
                Toast.makeText(this, "Connected to device: " + device.getName(), Toast.LENGTH_SHORT).show();
                // You're now connected and can perform further actions if needed
            }
        } catch (IOException e) {
            // Connection failed
            Toast.makeText(this, "Failed to connect to device: " + device.getName(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private class ConnectTask extends AsyncTask<String,Void,Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            String deviceAddress = strings[0];
            BluetoothDevice device = mAdapter.getRemoteDevice(deviceAddress);

            try {
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                mSocket = device.createRfcommSocketToServiceRecord(uuid);
                mSocket.connect();
                return mSocket.isConnected();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        @Override
        protected void onPostExecute(Boolean isConnected)   {
            super.onPostExecute(isConnected);
            progressBar.setVisibility(View.GONE);

            if (isConnected) {
                Toast.makeText(MainActivity.this, "Connected to device", Toast.LENGTH_SHORT).show();
                // You're now connected and can perform further actions if needed

            } else {
                Toast.makeText(MainActivity.this, "Failed to connect to device", Toast.LENGTH_SHORT).show();
            }
        }

    }


}
