package com.example.bluetooth_chat_java_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetooth_chat_java_app.Controller.ChatController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mAdapter;
    private BluetoothSocket mSocket;
    private static final int REQUEST_ENABLE_CODE = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 2;
    private ArrayAdapter<String> mDiscoveredDevicesArrayAdapter;
    private ArrayAdapter<String> mArrayAdapter;
    private Button scanBtn;
    private ChatController chatController;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_OBJECT = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_OBJECT = "device_name";
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);

        chatController = new ChatController(this,new Handler());
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
        protected void onPostExecute(Boolean isConnected) {
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
