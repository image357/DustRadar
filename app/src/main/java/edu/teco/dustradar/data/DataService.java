package edu.teco.dustradar.data;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.IBinder;
import android.util.Log;

import java.util.Arrays;
import java.util.List;


public class DataService extends Service {

    private final static String TAG = DataService.class.getSimpleName();

    // broadcasts
    private final static List<String> allBroadcasts = Arrays.asList(
    );


    // constructors

    public DataService() {
    }


    // static service handlers

    public static void startService(Context context, BroadcastReceiver receiver) {
        if(context == null) {
            throw new Resources.NotFoundException("Cannot start service without context or device");
        }

        if (isRunning(context)) {
            Log.w(TAG, "Service not started because it is already running");
            return;
        }

        // register BroadcastReceiver for context
        context.registerReceiver(receiver, getIntentFilter());

        // start service
        Intent bleServiceIntent = new Intent(context, DataService.class);
        context.startService(bleServiceIntent);
    }


    public static void stopService(Context context, BroadcastReceiver receiver) {
        if(context == null) {
            throw new Resources.NotFoundException("Cannot stop service without context");
        }

        // try tounregister BroadcastReceiver for context
        try {
            context.unregisterReceiver(receiver);
        }
        catch (IllegalArgumentException e) {
            // pass
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
        return START_REDELIVER_INTENT;
    }


    @Override public void onDestroy() {
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
}
