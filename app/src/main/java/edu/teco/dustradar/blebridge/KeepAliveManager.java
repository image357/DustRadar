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

/**
 * Background services that constantly pings the BLEBridge services in order to prevent Android forced service stops
 */
public class KeepAliveManager extends Service {

    private final static String TAG = KeepAliveManager.class.getSimpleName();

    // broadcast actions
    public final static String BROADCAST_KEEP_ALIVE_PING = "BROADCAST_KEEP_ALIVE_PING";
    public final static String BROADCAST_KEEP_ALIVE_REPLY = "BROADCAST_KEEP_ALIVE_REPLY";


    // private members
    private PowerManager.WakeLock wakeLock;
    private Handler keepAliveHandler;

    private Context mContext = this;

    private final long pingDelay = 2000;


    // constructors

    /**
     * Empty constructor. Do not use it!
     */
    public KeepAliveManager() {
    }


    // static service handlers

    /**
     * @param context Context that will start the service
     */
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

    /**
     * @param context Context that will stop the service
     */
    public static void stopService(Context context) {
        if(context == null) {
            throw new Resources.NotFoundException("Cannot stop service without context");
        }

        Intent serviceIntent = new Intent(context, KeepAliveManager.class);
        context.stopService(serviceIntent);
    }

    /**
     * @param context Context that can call getSystemService(...)
     * @return true when running. false otherwise
     */
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


    // event handlers

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager powerManager = (PowerManager) getSystemService(this.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DustRadar::KeepAliveManager::Wakelock");
        wakeLock.acquire();

        registerReceiver();
        keepAliveHandler = new Handler();
        keepAliveHandler.postDelayed(keepAliveRunnable, pingDelay);


        Log.i(TAG, "KeepAliveManager started");
        return START_REDELIVER_INTENT;
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "KeepAliveManager destroyed");

        keepAliveHandler.removeCallbacks(keepAliveRunnable);
        unregisterReceiver();

        wakeLock.release();
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // no binding allowed
        throw new UnsupportedOperationException("binding not allowed");
    }


    // private methods

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    private Runnable keepAliveRunnable = (new Runnable() {
        @Override
        public void run() {
            broadcastUpdate(KeepAliveManager.BROADCAST_KEEP_ALIVE_PING);
            keepAliveHandler.postDelayed(keepAliveRunnable, pingDelay);
        }
    });


    // BroadcastReceiver

    private final BroadcastReceiver mReceiver = (new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (KeepAliveManager.BROADCAST_KEEP_ALIVE_REPLY.equals(action)) {
                // TODO: handle keep alive ping replies
                return;
            }
        }
    });

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(KeepAliveManager.BROADCAST_KEEP_ALIVE_REPLY);

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
