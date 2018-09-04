package edu.teco.dustradar.bluetooth;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.util.Log;

import java.util.UUID;

public class BLEService extends Service {

    private final static String TAG = BLEService.class.getSimpleName();

    // static members

    public final static String INTENT_EXTRA_BLE_DEVICE_ADDRESS = "bleDeviceAddress";

    // UUIDs
    public final static UUID DUSTTRACKER_DATA_SERVICE_UUID =
            UUID.fromString("6e57fcf9-8064-4995-a3a8-e5ca44552192");
    public final static UUID DUSTTRACKER_DATA_CHARACTERISTIC_UUID =
            UUID.fromString("7a812f99-06fa-4d89-819d-98e9aafbd4ef");
    public final static UUID DUSTTRACKER_DATA_DESCRIPTOR_UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // broadcast actions
    public final static String BROADCAST_GATT_CONNECTED = "BROADCAST_GATT_CONNECTED";
    public final static String BROADCAST_GATT_DISCONNECTED = "BROADCAST_GATT_DISCONNECTED";
    public final static String BROADCAST_GATT_SERVICES_DISCOVERED = "BROADCAST_GATT_SERVICES_DISCOVERED";
    public final static String BROADCAST_DATA_AVAILABLE = "BROADCAST_DATA_AVAILABLE";
    public final static String BROADCAST_CREATE_DATASTREAM = "BROADCAST_CREATE_DATASTREAM";


    // members

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;
    private BluetoothGatt mBluetoothGatt;

    private boolean shouldReconnect = false;


    // constructors

    public BLEService() {
    }


    // event handlers

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            throw new Resources.NotFoundException("Service started without intent");
        }

        // get BluetoothAdapter
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            throw new Resources.NotFoundException("BluetoothAdapter is not available in BLEService");
        }

        // get BLE device
        String deviceAddress = intent.getStringExtra(INTENT_EXTRA_BLE_DEVICE_ADDRESS);
        mDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
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
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }

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


    // static methods
    public static void startService(Activity activity, BluetoothDevice device) {
        if(activity == null || device == null) {
            throw new Resources.NotFoundException("Cannot start service without activity or device");
        }

        if (isRunning(activity)) {
            Log.w(TAG, "Service not started because it is already running");
            return;
        }

        Intent bleServiceIntent = new Intent(activity, BLEService.class);
        bleServiceIntent.putExtra(BLEService.INTENT_EXTRA_BLE_DEVICE_ADDRESS, device.getAddress());
        activity.startService(bleServiceIntent);
    }


    public static void stopService(Activity activity) {
        Intent bleServiceIntent = new Intent(activity, BLEService.class);
        activity.stopService(bleServiceIntent);
    }


    public static boolean isRunning(Activity activity) {
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (BLEService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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
                    mBluetoothGatt.discoverServices();
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i(TAG, "Disconnected from GATT server");
                    intentAction = BROADCAST_GATT_DISCONNECTED;
                    broadcastUpdate(intentAction);
                    if (shouldReconnect) {
                        mBluetoothGatt.connect();
                    }
                    break;

                default:
                    Log.d(TAG, "Unhandled message in onConnectionStateChange()");
                    super.onConnectionStateChange(gatt, status, newState);
                    break;
            }
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "GATT Services discovered");
                BluetoothGattService service = mBluetoothGatt.getService(DUSTTRACKER_DATA_SERVICE_UUID);
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(DUSTTRACKER_DATA_CHARACTERISTIC_UUID);
                mBluetoothGatt.setCharacteristicNotification(characteristic, true);

                // TODO: get descriptor

                broadcastUpdate(BROADCAST_GATT_SERVICES_DISCOVERED);
            }
            else {
                Log.d(TAG, "Unhandled status in onServicesDiscovered received: " + status);
            }
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // TODO: handle read characteristic
            String data = characteristic.getStringValue(0);
            Log.d(TAG, "new data: " + data);
            broadcastUpdate(BROADCAST_DATA_AVAILABLE);
        }
    });


}
