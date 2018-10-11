package edu.teco.dustradar.http;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import edu.teco.dustradar.R;
import edu.teco.dustradar.blebridge.KeepAliveManager;
import edu.teco.dustradar.data.DataObject;
import edu.teco.dustradar.data.DataService;

public class HTTPService extends Service {

    private final static String TAG = HTTPService.class.getSimpleName();

    // broadcasts
    public final static String BROADCAST_START_TRANSMIT = "BROADCAST_START_TRANSMIT";
    public final static String BROADCAST_STOP_TRANSMIT = "BROADCAST_STOP_TRANSMIT";
    public final static String BROADCAST_HTTP_TIMEOUT = "BROADCAST_HTTP_TIMEOUT";

    // actions
    public final static String ACTION_POST_THING = "ACTION_POST_THING";
    public final static String ACTION_POST_DATASTREAM = "ACTION_POST_DATASTREAM";
    public final static String ACTION_POST_SENSOR = "ACTION_POST_SENSOR";
    public final static String ACTION_POST_OBSERVEDPROPERTY = "ACTION_POST_OBSERVEDPROPERTY";
    public final static String ACTION_POST_OBSERVATION = "ACTION_POST_OBSERVATION";
    public final static String ACTION_POST_FEATUREOFINTEREST = "ACTION_POST_FEATUREOFINTEREST";
    public final static String ACTION_POST_EVENT = "ACTION_POST_EVENT";

    private final static List<String> allBroadcasts = Arrays.asList(
            BROADCAST_START_TRANSMIT,
            BROADCAST_STOP_TRANSMIT,
            BROADCAST_HTTP_TIMEOUT
    );


    // private members

    private PowerManager.WakeLock wakeLock;
    private boolean shouldTransmit;
    private Handler handler;

    private HashMap<String, DataObject> transmitMap;


    // constructors

    public HTTPService() {
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
        transmitMap = new HashMap<>();

        registerTransmitReceiver();
        registerHTTPIntentReceiver();
        registerKeepAliveReceiver();

        Log.i(TAG, "HTTPService started");
        return START_REDELIVER_INTENT;
    }


    @Override public void onDestroy() {
        Log.i(TAG, "HTTPService destroyed");
        shouldTransmit = false;
        handler.removeCallbacks(transmitRunnable);

        // refill unsubmitted data
        Log.d(TAG, "transmitMap size: " + String.valueOf(transmitMap.size()));
        if (! transmitMap.isEmpty()) {
            Iterator it = transmitMap.entrySet().iterator();
            while (it.hasNext()) {
                HashMap.Entry<String, DataObject> pair = (HashMap.Entry) it.next();
                DataService.add(pair.getValue());
                it.remove();
            }
        }
        transmitMap.clear();
        transmitMap = null;

        unregisterTransmitReceiver();
        unregisterHTTPIntentReceiver();
        unregisterKeepAliveReceiver();

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
        }
    });

    private void registerTransmitReceiver(){
        registerReceiver(mTransmitReceiver, getIntentFilter());
    }

    private void unregisterTransmitReceiver() {
        try {
            unregisterReceiver(mTransmitReceiver);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


    private final BroadcastReceiver mHTTPIntentReceiver = (new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (HTTPIntent.BROADCAST_HTTP_POST_FAILURE.equals(action)) {
                String uuid = intent.getStringExtra(HTTPIntent.EXTRA_BROADCAST);
                if (uuid == null) {
                    return;
                }

                if (!transmitMap.containsKey(uuid)) {
                    return;
                }

                DataObject data = transmitMap.remove(uuid);
                DataService.add(data);

                final Intent bIntent = new Intent(BROADCAST_HTTP_TIMEOUT);
                sendBroadcast(bIntent);
                return;
            }

            if (HTTPIntent.BROADCAST_HTTP_POST_SUCCESS.equals(action)) {
                String uuid = intent.getStringExtra(HTTPIntent.EXTRA_BROADCAST);
                if (uuid == null) {
                    return;
                }

                if (!transmitMap.containsKey(uuid)) {
                    return;
                }

                transmitMap.remove(uuid);
            }
        }
    });

    private void registerHTTPIntentReceiver() {
        registerReceiver(mHTTPIntentReceiver, HTTPIntent.getIntentFilter());
    }

    private void unregisterHTTPIntentReceiver() {
        try {
            unregisterReceiver(mHTTPIntentReceiver);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


    private final BroadcastReceiver mKeepAliveReceiver = (new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (KeepAliveManager.BROADCAST_KEEP_ALIVE_PING.equals(action)) {
                Intent reply = new Intent(KeepAliveManager.BROADCAST_KEEP_ALIVE_REPLY);
                sendBroadcast(reply);
                return;
            }
        }
    });

    private void registerKeepAliveReceiver() {
        registerReceiver(mKeepAliveReceiver, KeepAliveManager.getIntentFilter());
    }

    private void unregisterKeepAliveReceiver() {
        try {
            unregisterReceiver(mKeepAliveReceiver);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


    // transmission

    private Runnable transmitRunnable = (new Runnable() {
        @Override
        public void run() {
            if (shouldTransmit) {
                DataObject data = null;
                String uuid = null;
                if (DataService.size() != 0) {
                    data = DataService.poll();
                    uuid = UUID.randomUUID().toString();
                }

                if (data != null && data.isValid()) {
                    Log.i(TAG, "Transmitting data to SensorThings server");

                    transmitMap.put(uuid, data);

                    Context context = getApplicationContext();
                    STGenerator stgen = new STGenerator(context, data);

                    String key = getResources().getString(R.string.blebridge_pref_frosturl_key);
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String sturl = sharedPref.getString(key, null);

                    HTTPIntent.Post(context, ACTION_POST_THING,
                            sturl + "/v1.0/Things", stgen.getThing());
                    HTTPIntent.Post(context, ACTION_POST_SENSOR,
                            sturl + "/v1.0/Sensors", stgen.getSensor_SDS011());
                    HTTPIntent.Post(context, ACTION_POST_OBSERVEDPROPERTY,
                            sturl + "/v1.0/ObservedProperties", stgen.getObservedProperty_PM10());
                    HTTPIntent.Post(context, ACTION_POST_OBSERVEDPROPERTY,
                            sturl + "/v1.0/ObservedProperties", stgen.getObservedProperty_PM25());

                    HTTPIntent.Post(context, ACTION_POST_DATASTREAM,
                            sturl + "/v1.0/Datastreams", stgen.getDatastream_PM10());
                    HTTPIntent.Post(context, ACTION_POST_DATASTREAM,
                            sturl + "/v1.0/Datastreams", stgen.getDatastream_PM25());

                    HTTPIntent.Post(context, uuid,
                            sturl + "/v1.0/Observations", stgen.getEvent_PM10());
                    HTTPIntent.Post(context, uuid,
                            sturl + "/v1.0/Observations", stgen.getEvent_PM25());
                }

                long postDelay = 100;
                if (DataService.size() == 0) {
                    postDelay += 5000;
                }
                handler.postDelayed(transmitRunnable, postDelay);
            }
        }
    });

}
