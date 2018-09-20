package edu.teco.dustradar.http;

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

import edu.teco.dustradar.data.DataService;

public class HTTPService extends Service {

    private final static String TAG = HTTPService.class.getSimpleName();

    // broadcasts
    public final static String BROADCAST_START_TRANSMIT = "BROADCAST_START_TRANSMIT";
    public final static String BROADCAST_STOP_TRANSMIT = "BROADCAST_STOP_TRANSMIT";
    public final static String BROADCAST_HTTP_TIMEOUT = "BROADCAST_HTTP_TIMEOUT";

    private final static List<String> allBroadcasts = Arrays.asList(
            BROADCAST_START_TRANSMIT,
            BROADCAST_STOP_TRANSMIT,
            BROADCAST_HTTP_TIMEOUT
    );


    // private members

    private PowerManager.WakeLock wakeLock;
    private boolean shouldTransmit;
    private Handler handler;


    // constructors

    public HTTPService() {
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
        Intent serviceIntent = new Intent(context, HTTPService.class);
        context.startService(serviceIntent);
    }


    public static void stopService(Context context) {
        if(context == null) {
            throw new Resources.NotFoundException("Cannot stop service without context");
        }

        Intent serviceIntent = new Intent(context, HTTPService.class);
        context.stopService(serviceIntent);
    }


    public static boolean isRunning(Context context) {
        if(context == null) {
            throw new Resources.NotFoundException("Cannot check service without context");
        }

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (HTTPService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    // event handlers

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DustRadar::DataService::Wakelock");
        wakeLock.acquire();

        shouldTransmit = false;
        handler = new Handler();

        registerReceiver(mTransmitReceiver, getIntentFilter());

        Log.i(TAG, "HTTPService started");
        return START_REDELIVER_INTENT;
    }


    @Override public void onDestroy() {
        Log.i(TAG, "HTTPService destroyed");
        shouldTransmit = false;
        handler.removeCallbacks(transmitRunnable);

        // close BroadcastReceiver
        try {
            unregisterReceiver(mTransmitReceiver);
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


    // private methods

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    // BroadcastReceivers

    private final BroadcastReceiver mTransmitReceiver = (new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BROADCAST_START_TRANSMIT.equals(action)) {
                if (shouldTransmit == false) {
                    Log.i(TAG, "Transmission started");
                    shouldTransmit = true;
                    handler.postDelayed(transmitRunnable, 100);
                }
                return;
            }

            if (BROADCAST_STOP_TRANSMIT.equals(action)) {
                Log.i(TAG, "Transmission stopped");
                shouldTransmit = false;
                return;
            }

            if (BROADCAST_HTTP_TIMEOUT.equals(action)) {
                Log.w(TAG, "Cannot establish connectivity");
                // TODO: handle no connectivity notification
                return;
            }
        }
    });


    // transmission

    private Runnable transmitRunnable = (new Runnable() {
        @Override
        public void run() {
            if (shouldTransmit) {
                // TODO: make actual data transmission
                Log.i(TAG, "Transmitting data to SensorThings server");
                HTTPIntent.Post(getApplicationContext(), null, "http://win10-koepke.teco.edu:8080/FROST-Server/v1.0/Things", "{\"name\": \"hi\", \"description\": \"test\"}");

                long postDelay = 100;
                if (DataService.size() == 0) {
                    postDelay += 5000;
                }
                handler.postDelayed(transmitRunnable, postDelay);
            }
        }
    });

}
