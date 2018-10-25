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

import edu.teco.dustradar.blebridge.KeepAliveManager;
import edu.teco.dustradar.bluetooth.BLEService;


public class DataService extends Service {

    private final static String TAG = DataService.class.getSimpleName();

    // broadcasts
    public final static String BROADCAST_DATA_START_RECORDING = "BROADCAST_DATA_START_RECORDING";
    public final static String BROADCAST_DATA_STOP_RECORDING = "BROADCAST_DATA_STOP_RECORDING";
    public final static String BROADCAST_DATA_STORED = "BROADCAST_DATA_STORED";
    public final static String BROADCAST_DATASERVICE_ERROR = "BROADCAST_DATASERVICE_ERROR";


    // static members
    private static QueueFile queueFile = null;

    // private members
    private PowerManager.WakeLock wakeLock;
    private boolean shouldRecord;

    private final String fileName = "DataQueue";


    // constructors

    public DataService() {
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
        openQueueFile();
        registerReceiver();

        Log.i(TAG, "DataService started");
        return START_REDELIVER_INTENT;
    }


    @Override public void onDestroy() {
        Log.i(TAG, "DataService destroyed");

        shouldRecord = false;
        unregisterReceiver();
        closeQueueFile();

        wakeLock.release();
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // no binding allowed
        throw new UnsupportedOperationException("binding not allowed");
    }


    // static methods

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


    public static void clear() {
        if (queueFile == null) {
            return;
        }

        try {
            queueFile.clear();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    // private methods

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    private void openQueueFile() {
        closeQueueFile();

        File file = new File(getFilesDir(), fileName);
        try {
            queueFile = new QueueFile(file);
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Cannot create or access queueFile");
            broadcastUpdate(DataService.BROADCAST_DATASERVICE_ERROR);
        }
    }

    private void closeQueueFile() {
        try {
            queueFile.close();
        }
        catch (Exception e) {
            // pass
        }
        queueFile = null;
    }


    // BroadcastReceiver

    private final BroadcastReceiver mReceiver = (new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (DataService.BROADCAST_DATA_START_RECORDING.equals(action)) {
                shouldRecord = true;
                return;
            }

            if (DataService.BROADCAST_DATA_STOP_RECORDING.equals(action)) {
                shouldRecord = false;
                return;
            }

            if (BLEService.BROADCAST_BLE_DATA_AVAILABLE.equals(action)) {
                if (shouldRecord == false) {
                    return;
                }

                if (queueFile == null) {
                    Log.e(TAG, "Cannot record data because queueFile is null reference");
                    broadcastUpdate(DataService.BROADCAST_DATASERVICE_ERROR);
                    return;
                }

                String msg = intent.getStringExtra(BLEService.BROADCAST_EXTRA_DATA);
                DataObject data = new DataObject(msg);
                // TODO: important: add ble address to dataobject for datastream id generation
                if (data.isValid()) {
                    // store data
                    try {
                        byte[] bytes = DataObject.serialize(data);
                        queueFile.add(bytes);
                        broadcastUpdate(BROADCAST_DATA_STORED);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "Cannot serialze or save data");
                        broadcastUpdate(DataService.BROADCAST_DATASERVICE_ERROR);
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

            if (KeepAliveManager.BROADCAST_KEEP_ALIVE_PING.equals(action)) {
                broadcastUpdate(KeepAliveManager.BROADCAST_KEEP_ALIVE_REPLY);
                return;
            }
        }
    });

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(DataService.BROADCAST_DATA_START_RECORDING);
        intentFilter.addAction(DataService.BROADCAST_DATA_STOP_RECORDING);

        intentFilter.addAction(BLEService.BROADCAST_BLE_DATA_AVAILABLE);
        intentFilter.addAction(BLEService.BROADCAST_BLE_DATADESCRIPTION_AVAILABLE);
        intentFilter.addAction(BLEService.BROADCAST_BLE_METADATA_AVAILABLE);

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
