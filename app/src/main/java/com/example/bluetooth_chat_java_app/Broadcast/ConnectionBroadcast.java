package com.example.bluetooth_chat_java_app.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

public class ConnectionBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to Wi-Fi Direct connection changes
            handleConnectionChanges(intent);
        }
        Log.e("OnReceive","OnReceive");
    }
    private void handleConnectionChanges(Intent intent) {
        NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.e("OnReceive Connected","OnReceive Connected");
            // Device B is connected to Device A
            // Implement logic to receive messages or signals from Device A
            // For example: use sockets to receive messages from Device A
            // Display a notification or perform actions based on the received message
        }
    }
}
