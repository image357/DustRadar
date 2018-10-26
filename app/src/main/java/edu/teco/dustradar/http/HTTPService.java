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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

import edu.teco.dustradar.R;
import edu.teco.dustradar.blebridge.KeepAliveManager;
import edu.teco.dustradar.data.DataObject;
import edu.teco.dustradar.data.DataService;

public class HTTPService extends Service {

    private final static String TAG = HTTPService.class.getSimpleName();

    // broadcasts
    public final static String BROADCAST_HTTPSERVICE_START_TRANSMIT = "BROADCAST_HTTPSERVICE_START_TRANSMIT";
    public final static String BROADCAST_HTTPSERVICE_STOP_TRANSMIT = "BROADCAST_HTTPSERVICE_STOP_TRANSMIT";
    public final static String BROADCAST_HTTPSERVICE_NOTHING_TO_TRANSMIT = "BROADCAST_HTTPSERVICE_NOTHING_TO_TRANSMIT";
    public final static String BROADCAST_HTTPSERVICE_TIMEOUT = "BROADCAST_HTTPSERVICE_TIMEOUT";

    // actions
    public final static String ACTION_HTTPSERVICE_POST_THING = "ACTION_HTTPSERVICE_POST_THING";
    public final static String ACTION_HTTPSERVICE_POST_DATASTREAM = "ACTION_HTTPSERVICE_POST_DATASTREAM";
    public final static String ACTION_HTTPSERVICE_POST_SENSOR = "ACTION_HTTPSERVICE_POST_SENSOR";
    public final static String ACTION_HTTPSERVICE_POST_OBSERVEDPROPERTY = "ACTION_HTTPSERVICE_POST_OBSERVEDPROPERTY";


    // private members
    private PowerManager.WakeLock wakeLock;
    private boolean shouldTransmit;
    private Handler handler;

    private HashMap<String, DataObject> transmitMap;
    private final int maxTransmitMapSize = 100;

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

        registerReceiver();

        Log.i(TAG, "HTTPService started");
        return START_REDELIVER_INTENT;
    }


    @Override public void onDestroy() {
        Log.i(TAG, "HTTPService destroyed");
        shouldTransmit = false;
        handler.removeCallbacks(transmitRunnable);

        // refill submitted data
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
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    // BroadcastReceivers

    private final BroadcastReceiver mReceiver = (new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (HTTPService.BROADCAST_HTTPSERVICE_START_TRANSMIT.equals(action)) {
                if (shouldTransmit == false) {
                    shouldTransmit = true;
                    handler.postDelayed(transmitRunnable, 100);
                    Log.i(TAG, "Transmission started");
                }
                return;
            }

            if (HTTPService.BROADCAST_HTTPSERVICE_STOP_TRANSMIT.equals(action)) {
                shouldTransmit = false;
                Log.i(TAG, "Transmission stopped");
                return;
            }

            if (HTTPIntent.BROADCAST_HTTPINTENT_POST_FAILURE.equals(action)) {
                String uuid = intent.getStringExtra(HTTPIntent.EXTRA_HTTPINTENT_BROADCAST);
                if (uuid == null) {
                    return;
                }

                if (!transmitMap.containsKey(uuid)) {
                    return;
                }

                DataObject data = transmitMap.remove(uuid);
                DataService.add(data);

                final Intent bIntent = new Intent(BROADCAST_HTTPSERVICE_TIMEOUT);
                sendBroadcast(bIntent);
                return;
            }

            if (HTTPIntent.BROADCAST_HTTPINTENT_POST_SUCCESS.equals(action)) {
                String uuid = intent.getStringExtra(HTTPIntent.EXTRA_HTTPINTENT_BROADCAST);
                if (uuid == null) {
                    return;
                }

                if (!transmitMap.containsKey(uuid)) {
                    return;
                }

                transmitMap.remove(uuid);
            }

            if (KeepAliveManager.BROADCAST_KEEP_ALIVE_PING.equals(action)) {
                broadcastUpdate(KeepAliveManager.BROADCAST_KEEP_ALIVE_REPLY);
                return;
            }
        }
    });

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(HTTPService.BROADCAST_HTTPSERVICE_START_TRANSMIT);
        intentFilter.addAction(HTTPService.BROADCAST_HTTPSERVICE_STOP_TRANSMIT);

        intentFilter.addAction(HTTPIntent.BROADCAST_HTTPINTENT_POST_FAILURE);
        intentFilter.addAction(HTTPIntent.BROADCAST_HTTPINTENT_POST_SUCCESS);

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


    // transmission

    private Runnable transmitRunnable = (new Runnable() {
        private HashSet<String> CreatedThingIds = new HashSet<>();
        private HashSet<String> CreatedSensorIds = new HashSet<>();
        private HashSet<String> CreatedObservedPropertyIds = new HashSet<>();
        private HashSet<String> CreatedDatastreamIds = new HashSet<>();

        @Override
        public void run() {
            if (!shouldTransmit) {
                restartRunnable(1000);
                return;
            }

            if (transmitMap.size() > maxTransmitMapSize) {
                restartRunnable(1000);
                return;
            }

            if (DataService.size() == 0) {
                broadcastUpdate(HTTPService.BROADCAST_HTTPSERVICE_NOTHING_TO_TRANSMIT);
                restartRunnable(5000);
                return;
            }

            DataObject data = DataService.poll();
            String uuid = UUID.randomUUID().toString();
            if (data == null || !data.isValid()) {
                restartRunnable(1000);
                return;
            }

            transmitMap.put(uuid, data);
            Log.d(TAG, String.valueOf(transmitMap.size()));

            Context context = getApplicationContext();
            STGenerator stgen = new STGenerator(context, data);

            String key = getResources().getString(R.string.blebridge_pref_frosturl_key);
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String sturl = sharedPref.getString(key, null);

            if (!CreatedThingIds.contains(stgen.getThing_id())) {
                HTTPIntent.Post(context, ACTION_HTTPSERVICE_POST_THING,
                        sturl + "/v1.0/Things", stgen.getThing());
                CreatedThingIds.add(stgen.getThing_id());
            }

            if (!CreatedSensorIds.contains(stgen.getSensor_SDS011_id())) {
                HTTPIntent.Post(context, ACTION_HTTPSERVICE_POST_SENSOR,
                        sturl + "/v1.0/Sensors", stgen.getSensor_SDS011());
                CreatedSensorIds.add(stgen.getSensor_SDS011_id());
            }

            if (!CreatedObservedPropertyIds.contains(stgen.getObservedProperty_PM10_id())) {
                HTTPIntent.Post(context, ACTION_HTTPSERVICE_POST_OBSERVEDPROPERTY,
                        sturl + "/v1.0/ObservedProperties", stgen.getObservedProperty_PM10());
                CreatedObservedPropertyIds.add(stgen.getObservedProperty_PM10_id());
            }

            if (!CreatedObservedPropertyIds.contains(stgen.getObservedProperty_PM25_id())) {
                HTTPIntent.Post(context, ACTION_HTTPSERVICE_POST_OBSERVEDPROPERTY,
                        sturl + "/v1.0/ObservedProperties", stgen.getObservedProperty_PM25());
                CreatedObservedPropertyIds.add(stgen.getObservedProperty_PM25_id());
            }

            if (!CreatedDatastreamIds.contains(stgen.getDatastream_PM10_id())) {
                HTTPIntent.Post(context, ACTION_HTTPSERVICE_POST_DATASTREAM,
                        sturl + "/v1.0/Datastreams", stgen.getDatastream_PM10());
                CreatedDatastreamIds.add(stgen.getDatastream_PM10_id());
            }

            if (!CreatedDatastreamIds.contains(stgen.getDatastream_PM25_id())) {
                HTTPIntent.Post(context, ACTION_HTTPSERVICE_POST_DATASTREAM,
                        sturl + "/v1.0/Datastreams", stgen.getDatastream_PM25());
                CreatedDatastreamIds.add(stgen.getDatastream_PM25_id());
            }

            HTTPIntent.Post(context, uuid,
                    sturl + "/v1.0/Observations", stgen.getEvent_PM10());
            HTTPIntent.Post(context, uuid,
                    sturl + "/v1.0/Observations", stgen.getEvent_PM25());

            restartRunnable(20);
        }


        // private methods

        private void restartRunnable(long delay) {
            handler.postDelayed(transmitRunnable, delay);
        }
    });

}
