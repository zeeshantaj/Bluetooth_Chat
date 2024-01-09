package com.example.bluetooth_chat_java_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class QR_Code_Generated_Activity extends AppCompatActivity {

    private TextView ipTxt;
    private Button scanBtn;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_generated);

        ipTxt = findViewById(R.id.ipTxt);
        ImageView qrImg = findViewById(R.id.qrImage);
        ipTxt.setText("Ip Address: "+getDeviceIPAddress());
        Bitmap bitmap = generateQRCode(getDeviceIPAddress());
        qrImg.setImageBitmap(bitmap);

        scanBtn = findViewById(R.id.qrScanBtn);
        scanBtn.setOnClickListener(v -> {

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request camera permission if not granted
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.CAMERA},
                        CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                // Start QR code scanning
                startQRScanner();
            }


        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan canceled", Toast.LENGTH_SHORT).show();
            } else {
                String scannedData = result.getContents();
                // Handle the scanned data as needed (e.g., process the QR code content)
                Toast.makeText(this, "Scanned: " + scannedData, Toast.LENGTH_SHORT).show();

                WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {

                        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                            // The current device is the group owner (server)
                            // Check if the client is connected (assuming the other device is the client)
                            if (wifiP2pInfo.groupOwnerAddress != null) {
                                // Check if the client (other device) is connected by checking the group owner's IP
                                InetAddress clientIpAddress = wifiP2pInfo.groupOwnerAddress;
                                // Perform actions based on the connection status
                            }
                        } else if (wifiP2pInfo.groupFormed) {
                            // The current device is the client
                            // Perform actions based on the connection status
                        }
                    }
                };

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start QR scanner
                startQRScanner();
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity if permission is denied
            }
        }
    }

    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan a QR code");
        integrator.setOrientationLocked(false);
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }

    private Bitmap generateQRCode(String data) {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
            BitMatrix bitMatrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, 512, 512);
            return barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String getDeviceIPAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface iface : interfaces) {
                List<InetAddress> addresses = Collections.list(iface.getInetAddresses());
                for (InetAddress address : addresses) {
                    if (!address.isLoopbackAddress() && address.getHostAddress().contains(".")) {
                        // Filter out IPv6 and other non-IPv4 addresses
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}