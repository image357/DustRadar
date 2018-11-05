package edu.teco.dustradar.blebridge;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import edu.teco.dustradar.R;
import edu.teco.dustradar.bluetooth.BLEScan;
import edu.teco.dustradar.bluetooth.BLEService;
import edu.teco.dustradar.data.DataService;
import edu.teco.dustradar.gps.GPSService;
import edu.teco.dustradar.http.HTTPService;

public class BLEBridge extends AppCompatActivity {

    private static final String TAG = BLEBridge.class.getSimpleName();

    // private members
    private BLEScan bleScan;
    private ArrayList<BluetoothDevice> devices;

    private Long lastTimestamp;
    private boolean inSettings;

    private boolean shouldTimeout;
    private final int timeoutTime = 30000;

    private long lastWarnConnection = 0;
    private final long minLastWarnConnection = 4000;

    private boolean isInHandler = false;


    // request codes
    private final int BLE_ENABLE_REQUEST_CODE = 1;
    private final int FINE_LOCATION_PERMISSION_REQUEST_CODE = 2;
    private final int GPS_LOCATION_PERMISSION_REQUEST_CODE = 3;


    // event handlers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blebridge);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            return;
        }

        bleScan = new BLEScan(this);
        if (! bleScan.hasBluetooth()) {
            return;
        }

        PreferenceManager.setDefaultValues(this, R.xml.fragment_blebridge_settings, false);
        inSettings = false;

        isInHandler = false;
        BLEBridgeScan firstFragment = new BLEBridgeScan();
        firstFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, firstFragment).commit();
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "BLEBridge: onResume()");

        lastTimestamp = System.currentTimeMillis();
        makePermissionChecks();
        registerReceiver();
    }


    @Override
    public void onPause() {
        Log.i(TAG, "BLEBridge: onPause()");

        unregisterReceiver();

        super.onPause();
    }


    @Override
    public void onBackPressed() {
        Log.i(TAG, "BLEBridge: onBackPressed()");

        if (inSettings) {
            inSettings = false;
            super.onBackPressed();

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String key = getResources().getString(R.string.blebridge_pref_default_key);
            boolean usedefault = sharedPref.getBoolean(key, false);
            if (usedefault) {
                Log.i(TAG, "using default values");
                sharedPref.edit().clear().commit();
                PreferenceManager.setDefaultValues(this, R.xml.fragment_blebridge_settings, true);
            }
            return;
        }

        Long currentTimestamp = System.currentTimeMillis();
        int minBackDifference = 300;
        if ((currentTimestamp - lastTimestamp) > minBackDifference) {
            Snackbar.make(findViewById(R.id.blebridge_content), "Tap twice and fast to exit",
                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
            lastTimestamp = currentTimestamp;
            return;
        }

        stopServices();
        super.onBackPressed();
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "BLEBridge: onDestroy()");
        stopServices();
        super.onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BLE_ENABLE_REQUEST_CODE:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this, "You have to enable BLE to use this mode.",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

            case GPS_LOCATION_PERMISSION_REQUEST_CODE:
                if (!GPSService.hasHighAccuracyPermission(this)) {
                    Toast.makeText(this, "You have to enable high accuracy location services to use this mode.",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "You have to allow location access to use this mode.",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_blebridge, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            if (!inSettings) {
                inSettings = true;

                BLEBridgeSettings settingsFragment = new BLEBridgeSettings();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, settingsFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

            return true;
        }

        if (id == R.id.action_transmit) {
            if (!DataService.isRunning(this)) {
                DataService.startService(this);
            }

            if (!HTTPService.isRunning(this)) {
                HTTPService.startService(this);
            }

            if (!KeepAliveManager.isRunning(this)) {
                KeepAliveManager.startService(this);
            }

            final Context context = this;

            Handler handler = new Handler();
            handler.postDelayed((new Runnable() {
                @Override
                public void run() {
                    final Intent intent = new Intent(HTTPService.BROADCAST_HTTPSERVICE_START_TRANSMIT);
                    BLEBridgeHandler.isTransmitting = true;
                    context.sendBroadcast(intent);
                }
            }), 1000);

            if (!isInHandler) {
                isInHandler = true;
                ArrayList<String> deviceAddress = new ArrayList<>();
                deviceAddress.add("None");
                BLEBridgeDeviceSwitcher switcherFragment = BLEBridgeDeviceSwitcher.newInstance(deviceAddress);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, switcherFragment);
                transaction.commit();
            }

            return true;
        }

        if (id == R.id.action_clear) {
            final Activity activity = this;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Clear Data");
            builder.setMessage("Do you really want to clear all stored datapoints?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!DataService.isRunning(activity)) {
                        DataService.startService(activity);
                    }

                    Handler handler = new Handler();
                    handler.postDelayed((new Runnable() {
                        @Override
                        public void run() {
                            DataService.clear();
                        }
                    }), 1000);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // pass
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // public methods

    public void InitiateBLEConnection(ArrayList<BluetoothDevice> devices) {
        isInHandler = true;
        this.devices = devices;
        startServices(devices);

        shouldTimeout = true;


        final Handler handler = new Handler();
        final Activity activity = this;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (shouldTimeout) {
                    Toast.makeText(activity, "Connection timed out.", Toast.LENGTH_LONG).show();
                    activity.finish();
                    return;
                }
            }
        }, timeoutTime);
    }


    // private methods

    private void warnConnection() {
        long currenttime = System.currentTimeMillis();
        if ((currenttime - lastWarnConnection) < minLastWarnConnection) {
            return;
        }
        lastWarnConnection = currenttime;

        Toast.makeText(this, "Warning: Cannot transmit data!", Toast.LENGTH_SHORT).show();
    }


    private void makePermissionChecks() {
        if (! bleScan.hasBluetooth()) {
            Toast.makeText(this, "BLE is not supported on your device.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        bleScan.enable(this, BLE_ENABLE_REQUEST_CODE);
        bleScan.requestLocationPermission(this, FINE_LOCATION_PERMISSION_REQUEST_CODE);
        GPSService.requestHighAccuracyPermission(this, GPS_LOCATION_PERMISSION_REQUEST_CODE);
    }


    private void startServices(ArrayList<BluetoothDevice> devices) {
        if (GPSService.isRunning(this)) {
            Log.d(TAG, "Service is already running");
            GPSService.stopService(this);
        }

        if (BLEService.isRunning(this)) {
            Log.d(TAG, "Service is already running");
            BLEService.stopService(this);
        }

        if (DataService.isRunning(this)) {
            Log.d(TAG, "Service is already running");
            DataService.stopService(this);
        }

        if (HTTPService.isRunning(this)) {
            Log.d(TAG, "Service is already running");
            HTTPService.stopService(this);
        }

        if (KeepAliveManager.isRunning(this)) {
            Log.d(TAG, "Service is already running");
            KeepAliveManager.stopService(this);
        }

        GPSService.startService(this);
        BLEService.startService(this, devices);
        DataService.startService(this);
        HTTPService.startService(this);

        KeepAliveManager.startService(this);
    }


    private void stopServices() {
        KeepAliveManager.stopService(this);

        DataService.stopService(this);
        BLEService.stopService(this);
        GPSService.stopService(this);
        HTTPService.stopService(this);
        BLEBridgeHandler.isTransmitting = false;
    }


    // BroadcastReceiver

    private final BroadcastReceiver mReceiver = (new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BLEService.BROADCAST_BLESERVICE_FIRST_CONNECT.equals(action)) {
                shouldTimeout = false;

                // start BLEBridgeHandler Fragment
                ArrayList<String> deviceAddress = new ArrayList<>();
                for (BluetoothDevice device : devices) {
                    deviceAddress.add(device.getAddress());
                }

                BLEBridgeDeviceSwitcher switcherFragment = BLEBridgeDeviceSwitcher.newInstance(deviceAddress);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, switcherFragment);
                transaction.commit();

                Toast.makeText(context, "Connected", Toast.LENGTH_LONG).show();
                return;
            }

            if (BLEService.BROADCAST_BLESERVICE_GATT_MISSING_SERVICE.equals(action)) {
                Log.w(TAG, "user selected unsupported device");
                Toast.makeText(context, "You have to select a DustTracker device.",
                        Toast.LENGTH_LONG).show();
                stopServices();
                finish();
                return;
            }

            if (HTTPService.BROADCAST_HTTPSERVICE_TIMEOUT.equals(action)) {
                warnConnection();
                return;
            }

            if (KeepAliveManager.BROADCAST_KEEP_ALIVE_PING.equals(action)) {
                Intent reply = new Intent(KeepAliveManager.BROADCAST_KEEP_ALIVE_REPLY);
                sendBroadcast(reply);
                return;
            }

            if (BLEService.BROADCAST_BLESERVICE_ERROR.equals(action)) {
                Toast.makeText(context, "BLEService error",
                        Toast.LENGTH_LONG).show();
                stopServices();
                finish();
                return;
            }

            if (DataService.BROADCAST_DATASERVICE_ERROR.equals(action)) {
                Toast.makeText(context, "DataService error",
                        Toast.LENGTH_LONG).show();
                stopServices();
                finish();
                return;
            }
        }
    });

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BLEService.BROADCAST_BLESERVICE_ERROR);
        intentFilter.addAction(DataService.BROADCAST_DATASERVICE_ERROR);

        intentFilter.addAction(BLEService.BROADCAST_BLESERVICE_FIRST_CONNECT);
        intentFilter.addAction(BLEService.BROADCAST_BLESERVICE_GATT_MISSING_SERVICE);

        intentFilter.addAction(HTTPService.BROADCAST_HTTPSERVICE_TIMEOUT);

        intentFilter.addAction(KeepAliveManager.BROADCAST_KEEP_ALIVE_PING);

        registerReceiver(mReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        try {
            unregisterReceiver(mReceiver);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

}
