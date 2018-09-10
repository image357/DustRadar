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

import com.squareup.tape.QueueFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import edu.teco.dustradar.bluetooth.BLEService;


public class DataService extends Service {

    private final static String TAG = DataService.class.getSimpleName();

    // broadcasts
    public final static String BROADCAST_DATA_STORED = "BROADCAST_DATA_STORED";

    private final static List<String> allBroadcasts = Arrays.asList(
            BROADCAST_DATA_STORED
    );


    // static members

    public final static String fileName = "DataQueue";
    private static QueueFile queueFile = null;
    private static boolean shouldRecord = false;


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
        Intent serviceIntent = new Intent(context, DataService.class);
        context.startService(serviceIntent);
    }


    public static void stopService(Context context) {
        if(context == null) {
            throw new Resources.NotFoundException("Cannot stop service without context");
        }

        Intent serviceIntent = new Intent(context, DataService.class);
        context.stopService(serviceIntent);
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
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DustRadar::DataService::Wakelock");
        wakeLock.acquire();

        shouldRecord = false;

        File file = new File(getFilesDir(), fileName);
        queueFile = null;
        try {
            queueFile = new QueueFile(file);
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Cannot create or access queueFile");
        }

        registerReceiver(mBLEReceiver, BLEService.getIntentFilter());

        Log.i(TAG, "DataService started");
        return START_REDELIVER_INTENT;
    }


    @Override public void onDestroy() {
        Log.i(TAG, "DataService destroyed");
        shouldRecord = false;

        // close BroadcastReceiver
        try {
            unregisterReceiver(mBLEReceiver);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        // close QueueFile
        try {
            queueFile.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        queueFile = null;

        wakeLock.release();
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // no binding allowed
        throw new UnsupportedOperationException("binding not allowed");
    }


    // static methods

    public static void setRecord(boolean record) {
        shouldRecord = record;
    }


    public static int size() {
        if (queueFile == null) {
            return -1;
        }

        return queueFile.size();
    }


    public static DataObject peek() {
        if (queueFile == null) {
            return null;
        }

        DataObject data = null;
        try {
            byte[] bytes = queueFile.peek();
            data = DataObject.deserialize(bytes);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return data;
    }


    public static DataObject poll() {
        if (queueFile == null) {
            return null;
        }

        DataObject data = null;
        try {
            byte[] bytes = queueFile.peek();
            queueFile.remove();
            data = DataObject.deserialize(bytes);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return data;
    }


    public static boolean add(DataObject object) {
        if (queueFile == null) {
            return false;
        }

        try {
            byte[] bytes = DataObject.serialize(object);
            queueFile.add(bytes);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


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

    private final BroadcastReceiver mBLEReceiver = (new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BLEService.BROADCAST_BLE_DATA_AVAILABLE.equals(action)) {
                String msg = intent.getStringExtra(BLEService.BROADCAST_EXTRA_DATA);
                DataObject data = new DataObject(msg);
                if (data.isValid() && queueFile != null && shouldRecord) {
                    // store data
                    try {
                        byte[] bytes = DataObject.serialize(data);
                        queueFile.add(bytes);
                        broadcastUpdate(BROADCAST_DATA_STORED);
                        Log.i(TAG, "stored data: " + msg + " - total: " + String.valueOf(queueFile.size()));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "Cannot serialze or save data");
                    }
                }
                return;
            }

            if (BLEService.BROADCAST_BLE_DATADESCRIPTION_AVAILABLE.equals(action)) {
                String msg = intent.getStringExtra(BLEService.BROADCAST_EXTRA_DATA);
                Log.d(TAG, "datadescription: " + msg);
                // TODO: handle datadescription
                return;
            }

            if (BLEService.BROADCAST_BLE_METADATA_AVAILABLE.equals(action)) {
                String msg = intent.getStringExtra(BLEService.BROADCAST_EXTRA_DATA);
                Log.d(TAG, "metadata: " + msg);
                // TODO: handle metadata
                return;
            }
        }
    });
}
