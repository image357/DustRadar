package edu.teco.dustradar.data;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

import edu.teco.dustradar.bluetooth.BLEService;


public class DataService extends Service {

    private final static String TAG = DataService.class.getSimpleName();

    // broadcasts
    private final static List<String> allBroadcasts = Arrays.asList(
    );


    // private members
    private PowerManager.WakeLock wakeLock;


    // constructors

    public DataService() {
    }


    // static service handlers

    public static void startService(Context context) {
        if(context == null) {
            throw new Resources.NotFoundException("Cannot start service without context or device");
        }

        if (isRunning(context)) {
            Log.w(TAG, "Service not started because it is already running");
            return;
        }

        // start service
        Intent bleServiceIntent = new Intent(context, DataService.class);
        context.startService(bleServiceIntent);
    }


    public static void stopService(Context context) {
        if(context == null) {
            throw new Resources.NotFoundException("Cannot stop service without context");
        }

        Intent bleServiceIntent = new Intent(context, DataService.class);
        context.stopService(bleServiceIntent);
    }


    public static boolean isRunning(Context context) {
        if(context == null) {
            throw new Resources.NotFoundException("Cannot check service without context");
        }

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (DataService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    // event handlers

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager powerManager = (PowerManager) getSystemService(this.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DustRadar::DataService::Wakelock");
        wakeLock.acquire();

        registerReceiver(mBLEReceiver, BLEService.getIntentFilter());

        return START_REDELIVER_INTENT;
    }


    @Override public void onDestroy() {
        Log.d(TAG, "DataService destroyed");
        try {
            unregisterReceiver(mBLEReceiver);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        wakeLock.release();
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // no binding allowed
        throw new UnsupportedOperationException("binding not allowed");
    }


    // static methods

    public static IntentFilter getIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        for(String broadcast : allBroadcasts) {
            intentFilter.addAction(broadcast);
        }

        return intentFilter;
    }


    // BroadcastReceivers

    private final BroadcastReceiver mBLEReceiver = (new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BLEService.BROADCAST_BLE_DATA_AVAILABLE.equals(action)) {
                String data = intent.getStringExtra(BLEService.BROADCAST_EXTRA_DATA);
                Log.d(TAG, "data: " + data);
                // TODO: handle data
                return;
            }

            if (BLEService.BROADCAST_BLE_DATADESCRIPTION_AVAILABLE.equals(action)) {
                String data = intent.getStringExtra(BLEService.BROADCAST_EXTRA_DATA);
                Log.d(TAG, "datadescription: " + data);
                // TODO: handle datadescription
                return;
            }

            if (BLEService.BROADCAST_BLE_METADATA_AVAILABLE.equals(action)) {
                String data = intent.getStringExtra(BLEService.BROADCAST_EXTRA_DATA);
                Log.d(TAG, "metadata: " + data);
                // TODO: handle metadata
                return;
            }
        }
    });
}
