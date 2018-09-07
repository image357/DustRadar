package edu.teco.dustradar.bluetooth;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
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
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class BLEService extends Service {

    private final static String TAG = BLEService.class.getSimpleName();

    // intent extras key
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
    public final static UUID DATADESCRIPTION_CHARACTERISTIC_UUID =
            UUID.fromString("aa1c2b71-e510-41bb-96b2-7129cf59f260");

    public final static UUID METADATA_SERVICE_UUID =
            UUID.fromString("aec37142-b2a4-4f05-a8b3-bf4602267641");
    public final static UUID METADATA_CHARACTERISTIC_UUID =
            UUID.fromString("0933fb51-155f-4f17-98c6-3a9663f51a3c");

    private final static List<UUID> requiredServiceUUIDs = Arrays.asList(
            DATA_SERVICE_UUID,
            METADATA_SERVICE_UUID
    );


    // broadcasts
    public final static String BROADCAST_GATT_CONNECTED = "BROADCAST_GATT_CONNECTED";
    public final static String BROADCAST_GATT_DISCONNECTED = "BROADCAST_GATT_DISCONNECTED";
    public final static String BROADCAST_GATT_SERVICES_DISCOVERED = "BROADCAST_GATT_SERVICES_DISCOVERED";
    public final static String BROADCAST_BLE_DATA_AVAILABLE = "BROADCAST_BLE_DATA_AVAILABLE";
    public final static String BROADCAST_BLE_DATADESCRIPTION_AVAILABLE = "BROADCAST_BLE_DATADESCRIPTION_AVAILABLE";
    public final static String BROADCAST_BLE_METADATA_AVAILABLE = "BROADCAST_BLE_METADATA_AVAILABLE";
    public final static String BROADCAST_EXTRA_DATA = "BROADCAST_EXTRA_DATA";
    public final static String BROADCAST_MISSING_SERVICE = "BROADCAST_MISSING_SERVICE";

    private final static List<String> allBroadcasts = Arrays.asList(
            BROADCAST_GATT_CONNECTED,
            BROADCAST_GATT_DISCONNECTED,
            BROADCAST_GATT_SERVICES_DISCOVERED,
            BROADCAST_BLE_DATA_AVAILABLE,
            BROADCAST_BLE_DATADESCRIPTION_AVAILABLE,
            BROADCAST_BLE_METADATA_AVAILABLE,
            BROADCAST_EXTRA_DATA,
            BROADCAST_MISSING_SERVICE
    );


    // private members
    private boolean shouldReconnect = false;

    private BluetoothGattService DataService;
    private BluetoothGattCharacteristic NotifyChar;
    private BluetoothGattCharacteristic DataChar;


    // static members
    private static BluetoothGatt mBluetoothGatt = null;
    private static BluetoothGattService MetadataService = null;
    private static BluetoothGattCharacteristic MetadataChar = null;
    private static BluetoothGattCharacteristic DataDescriptionChar;

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
        context.registerReceiver(receiver, getIntentFilter());

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
        MetadataService = null;
        MetadataChar = null;
        DataDescriptionChar = null;

        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // no binding allowed
        throw new UnsupportedOperationException("binding not allowed");
    }


    // static methods

    public static boolean hasLocationPermission (Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        return (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }


    @TargetApi(Build.VERSION_CODES.M)
    public static void requestLocationPermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        if (hasLocationPermission(activity)) {
            return;
        }

        activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
    }


    public static IntentFilter getIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        for(String broadcast : allBroadcasts) {
            intentFilter.addAction(broadcast);
        }

        return intentFilter;
    }


    public static void readMetadata() {
        if (mBluetoothGatt == null || MetadataChar == null) {
            Log.e(TAG, "Cannot read metadata characterstic");
            return;
        }

        CharFIFO.add(MetadataChar);
    }


    public static void readDataDescription() {
        if (mBluetoothGatt == null || DataDescriptionChar == null) {
            Log.e(TAG, "Cannot read datadescription characterstic");
            return;
        }

        CharFIFO.add(DataDescriptionChar);
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


    private boolean hasRequiredServices(BluetoothGatt gatt) {
        List<BluetoothGattService> services = gatt.getServices();

        int foundServices = 0;

        for (BluetoothGattService service : services) {
            for (UUID uuid : requiredServiceUUIDs) {
                if (uuid.equals(service.getUuid())) {
                    foundServices++;
                }
            }
        }

        if (foundServices == requiredServiceUUIDs.size()) {
            return true;
        }

        return false;
    }


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    private void broadcastUpdate(final String action, final String data) {
        final Intent intent = new Intent(action);
        intent.putExtra(BROADCAST_EXTRA_DATA, data);
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
                        if (!hasRequiredServices(gatt)) {
                            broadcastUpdate(BROADCAST_MISSING_SERVICE);
                            return;
                        }

                        // services
                        DataService = gatt.getService(DATA_SERVICE_UUID);
                        MetadataService = gatt.getService(METADATA_SERVICE_UUID);

                        // notify characterstic
                        NotifyChar = DataService.getCharacteristic(NOTIFY_CHARACTERISTIC_UUID);
                        gatt.setCharacteristicNotification(NotifyChar, true);
                        BluetoothGattDescriptor descriptor = NotifyChar.getDescriptor(NOTIFY_DESCRIPTOR_UUID);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);

                        // other characterstics
                        DataChar = DataService.getCharacteristic(DATA_CHARACTERISTIC_UUID);
                        DataDescriptionChar = DataService.getCharacteristic(DATADESCRIPTION_CHARACTERISTIC_UUID);
                        MetadataChar = MetadataService.getCharacteristic(METADATA_CHARACTERISTIC_UUID);

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
            if (characteristic.equals(NotifyChar)) {
                if (CharFIFO.size() == 0) {
                    readCharacteristic(gatt, DataChar);
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
                if (characteristic.equals(DataChar)) {
                    String data = characteristic.getStringValue(0);
                    broadcastUpdate(BROADCAST_BLE_DATA_AVAILABLE, data);
                    return;
                }

                if (characteristic.equals(DataDescriptionChar)) {
                    String data = characteristic.getStringValue(0);
                    broadcastUpdate(BROADCAST_BLE_DATADESCRIPTION_AVAILABLE, data);
                    return;
                }

                if (characteristic.equals(MetadataChar)) {
                    String data = characteristic.getStringValue(0);
                    broadcastUpdate(BROADCAST_BLE_METADATA_AVAILABLE, data);
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
