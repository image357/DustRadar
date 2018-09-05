package edu.teco.dustradar.gps;

import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.util.Log;

import edu.teco.dustradar.bluetooth.BLEService;


public class GPSService extends Service {

    private final static String TAG = BLEService.class.getSimpleName();


    // constructors

    public GPSService() {
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
        Intent bleServiceIntent = new Intent(context, GPSService.class);
        context.startService(bleServiceIntent);
    }


    public static void stopService(Context context) {
        if(context == null) {
            throw new Resources.NotFoundException("Cannot stop service without context");
        }

        Intent bleServiceIntent = new Intent(context, GPSService.class);
        context.stopService(bleServiceIntent);
    }


    public static boolean isRunning(Context context) {
        if(context == null) {
            throw new Resources.NotFoundException("Cannot check service without context");
        }

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (GPSService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    // event handlers

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_REDELIVER_INTENT;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // no binding allowed
        throw new UnsupportedOperationException("binding not allowed");
    }
}
