package edu.teco.dustradar.bluetooth;

import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class BLEService extends Service {

    private final static String TAG = BLEService.class.getSimpleName();

    // static members
    public final static String INTENT_EXTRA_BLE_DEVICE_ADDRESS = "bleDeviceAddress";

    // UUIDs
    public final static UUID DATA_SERVICE_UUID =
            UUID.fromString("6e57fcf9-8064-4995-a3a8-e5ca44552192");
    public final static UUID NOTIFY_CHARACTERISTIC_UUID =
            UUID.fromString("7a812f99-06fa-4d89-819d-98e9aafbd4ef");
    public final static UUID NOTIFY_DESCRIPTOR_UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static UUID DATA_CHARACTERISTIC_UUID =
            UUID.fromString("3525d870-6549-490a-bcc8-576cd6afde8a");
    public final static UUID METADATA_CHARACTERISTIC_UUID =
            UUID.fromString("0933fb51-155f-4f17-98c6-3a9663f51a3c");


    // broadcast actions
    public final static String BROADCAST_GATT_CONNECTED = "BROADCAST_GATT_CONNECTED";
    public final static String BROADCAST_GATT_DISCONNECTED = "BROADCAST_GATT_DISCONNECTED";
    public final static String BROADCAST_GATT_SERVICES_DISCOVERED = "BROADCAST_GATT_SERVICES_DISCOVERED";
    public final static String BROADCAST_BLE_DATA_AVAILABLE = "BROADCAST_BLE_DATA_AVAILABLE";
    public final static String BROADCAST_BLE_METADATA_AVAILABLE = "BROADCAST_BLE_METADATA_AVAILABLE";


    private boolean shouldReconnect = false;

    private BluetoothGattService DataService;
    private BluetoothGattCharacteristic NotifyCharactersistic;
    private BluetoothGattCharacteristic DataCharactersistic;


    // static members
    private static BluetoothGatt mBluetoothGatt = null;
    private static BluetoothGattCharacteristic MetadataCharactersistic = null;
    private static Queue<BluetoothGattCharacteristic> CharFIFO = new LinkedList<>();


    // constructors

    public BLEService() {
    }


    // static service handlers

    public static void startService(Context context, BroadcastReceiver receiver, BluetoothDevice device) {
        if(context == null || device == null) {
            throw new Resources.NotFoundException("Cannot start service without context or device");
        }

        if (isRunning(context)) {
            Log.w(TAG, "Service not started because it is already running");
            return;
        }

        // register BroadcastReceiver for context
        context.registerReceiver(receiver, getGattUpdateIntentFilter());

        // start service
        Intent bleServiceIntent = new Intent(context, BLEService.class);
        bleServiceIntent.putExtra(BLEService.INTENT_EXTRA_BLE_DEVICE_ADDRESS, device.getAddress());
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

        // stop service
        Intent bleServiceIntent = new Intent(context, BLEService.class);
        context.stopService(bleServiceIntent);
    }


    public static boolean isRunning(Context context) {
        if(context == null) {
            throw new Resources.NotFoundException("Cannot check service without context");
        }

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (BLEService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    // event handlers

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            throw new Resources.NotFoundException("Service started without intent");
        }

        // get BluetoothAdapter
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            throw new Resources.NotFoundException("BluetoothAdapter is not available in BLEService");
        }

        // get BLE device
        String deviceAddress = intent.getStringExtra(INTENT_EXTRA_BLE_DEVICE_ADDRESS);
        BluetoothDevice mDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        if (mDevice == null) {
            throw new Resources.NotFoundException("BLEDevice is not available in BLEService");
        }

        // connect to device
        shouldReconnect = true;
        mBluetoothGatt = mDevice.connectGatt(this, true, mGattCallback);


        return START_REDELIVER_INTENT;
    }


    @Override
    public void onDestroy() {
        shouldReconnect = false;

        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }

        mBluetoothGatt = null;
        MetadataCharactersistic = null;

        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // no binding allowed
        throw new UnsupportedOperationException("binding not allowed");
    }


    // static public methods

    public static IntentFilter getGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_GATT_CONNECTED);
        intentFilter.addAction(BROADCAST_GATT_DISCONNECTED);
        intentFilter.addAction(BROADCAST_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BROADCAST_BLE_DATA_AVAILABLE);
        intentFilter.addAction(BROADCAST_BLE_METADATA_AVAILABLE);

        return intentFilter;
    }


    public static void readMetadata() {
        if (mBluetoothGatt == null || MetadataCharactersistic == null) {
            Log.e(TAG, "Cannot read metadata characterstic");
            return;
        }

        CharFIFO.add(MetadataCharactersistic);
    }


    // private methods

    private void readCharacteristic(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (gatt == null) {
            throw new Resources.NotFoundException("Cannot read BLE without gatt");
        }

        if (characteristic != null) {
            CharFIFO.add(characteristic);
        }

        BluetoothGattCharacteristic newcharactersistic = CharFIFO.peek();
        if (newcharactersistic == null) {
            return;
        }

        boolean allowed = gatt.readCharacteristic(newcharactersistic);
        if (allowed) {
            CharFIFO.poll();
        }
        else {
            Log.w(TAG, "No permission to read characteristic");
        }
    }


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    // callbacks

    private final BluetoothGattCallback mGattCallback = (new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i(TAG, "Connected to GATT server");
                    intentAction = BROADCAST_GATT_CONNECTED;
                    broadcastUpdate(intentAction);
                    gatt.discoverServices();
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i(TAG, "Disconnected from GATT server");
                    intentAction = BROADCAST_GATT_DISCONNECTED;
                    broadcastUpdate(intentAction);
                    if (shouldReconnect) {
                        Log.i(TAG, "Trying to reconnect to GATT server");
                        gatt.connect();
                    }
                    break;

                default:
                    Log.d(TAG, "Unhandled message in onConnectionStateChange()");
                    super.onConnectionStateChange(gatt, status, newState);
                    break;
            }
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt pgatt, int status) {
            final BluetoothGatt gatt = pgatt;

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "GATT Services discovered");
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        // DataService
                        DataService = gatt.getService(DATA_SERVICE_UUID);

                        NotifyCharactersistic =
                                DataService.getCharacteristic(NOTIFY_CHARACTERISTIC_UUID);
                        gatt.setCharacteristicNotification(NotifyCharactersistic, true);

                        BluetoothGattDescriptor descriptor =
                                NotifyCharactersistic.getDescriptor(NOTIFY_DESCRIPTOR_UUID);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);

                        DataCharactersistic =
                                DataService.getCharacteristic(DATA_CHARACTERISTIC_UUID);

                        MetadataCharactersistic =
                                DataService.getCharacteristic(METADATA_CHARACTERISTIC_UUID);

                        broadcastUpdate(BROADCAST_GATT_SERVICES_DISCOVERED);
                    }
                });
            }
            else {
                Log.d(TAG, "Unhandled status in onServicesDiscovered(): " + status);
            }
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic.equals(NotifyCharactersistic)) {
                if (CharFIFO.size() == 0) {
                    readCharacteristic(gatt, DataCharactersistic);
                }
                else {
                    Log.i(TAG, "CharFIFO size: " + String.valueOf(CharFIFO.size()));
                    readCharacteristic(gatt, null);
                }

                return;
            }

            Log.w(TAG, "unhandled notification in onCharacteristicChanged()");
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (characteristic.equals(DataCharactersistic)) {
                    String data = characteristic.getStringValue(0);
                    Log.d(TAG, "data: " + data);
                    broadcastUpdate(BROADCAST_BLE_DATA_AVAILABLE);
                    // TODO: handle data
                    return;
                }

                if (characteristic.equals(MetadataCharactersistic)) {
                    String data = characteristic.getStringValue(0);
                    Log.d(TAG, "metadata: " + data);
                    broadcastUpdate(BROADCAST_BLE_METADATA_AVAILABLE);
                    // TODO: handle metadata
                    return;
                }

                Log.w(TAG, "unhandled notification in onCharacteristicRead()");
            }
            else {
                Log.d(TAG, "Unhandled status in onCharacteristicRead(): " + status);
            }

            if (CharFIFO.size() != 0) {
                // read next characteristic in queue
                Log.i(TAG, "CharFIFO size: " + String.valueOf(CharFIFO.size()));
                readCharacteristic(gatt, null);
            }
        }
    });


}
