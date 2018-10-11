package edu.teco.dustradar.blebridge;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class KeepAliveManager extends Service {

    private final static String TAG = KeepAliveManager.class.getSimpleName();

    // broadcast actions

    public final static String BROADCAST_KEEP_ALIVE_PING = "BROADCAST_KEEP_ALIVE_PING";
    public final static String BROADCAST_KEEP_ALIVE_REPLY = "BROADCAST_KEEP_ALIVE_REPLY";

    private final static List<String> allBroadcasts = Arrays.asList(
            BROADCAST_KEEP_ALIVE_PING,
            BROADCAST_KEEP_ALIVE_REPLY
    );


    // private members

    private PowerManager.WakeLock wakeLock;
    private Handler keepAliveHandler;

    private Context mContext = this;

    private final long pingDelay = 2000;


    // constructors

    public KeepAliveManager() {
    }


    // static service handlers

    public static void startService(Context context) {
        if(context == null) {
            throw new Resources.NotFoundException("Cannot start service without context");
        }

        if (isRunning(context)) {
            Log.w(TAG, "Service not started because it is already running");
            return;
        }

        // start service
        Intent serviceIntent = new Intent(context, KeepAliveManager.class);
        context.startService(serviceIntent);
    }


    public static void stopService(Context context) {
        if(context == null) {
            throw new Resources.NotFoundException("Cannot stop service without context");
        }

        Intent serviceIntent = new Intent(context, KeepAliveManager.class);
        context.stopService(serviceIntent);
    }


    public static boolean isRunning(Context context) {
        if(context == null) {
            throw new Resources.NotFoundException("Cannot check service without context");
        }

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (KeepAliveManager.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    public static IntentFilter getIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        for(String broadcast : allBroadcasts) {
            intentFilter.addAction(broadcast);
        }

        return intentFilter;
    }


    // event handlers

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager powerManager = (PowerManager) getSystemService(this.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DustRadar::KeepAliveManager::Wakelock");
        wakeLock.acquire();

        registerReceiver(mKeepAliveReceiver, getIntentFilter());

        keepAliveHandler = new Handler();
        keepAliveHandler.postDelayed(keepAliveRunnable, pingDelay);


        Log.i(TAG, "KeepAliveManager started");
        return START_REDELIVER_INTENT;
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "KeepAliveManager destroyed");

        keepAliveHandler.removeCallbacks(keepAliveRunnable);

        // close BroadcastReceivers
        try {
            unregisterReceiver(mKeepAliveReceiver);
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

    public static void registerAliveService(Class service) {
    }


    // private methods

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    private Runnable keepAliveRunnable = (new Runnable() {
        @Override
        public void run() {
            Intent ping = new Intent(BROADCAST_KEEP_ALIVE_PING);
            sendBroadcast(ping);

            keepAliveHandler.postDelayed(keepAliveRunnable, pingDelay);
        }
    });


    private void checkRunningServiceOrRestart(Class service) {
    }


    // BroadcastReceivers

    private final BroadcastReceiver mKeepAliveReceiver = (new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BROADCAST_KEEP_ALIVE_REPLY.equals(action)) {
                // TODO: handle keep alive ping replies
                return;
            }
        }
    });

}
