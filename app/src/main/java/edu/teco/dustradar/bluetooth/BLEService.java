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
import android.os.PowerManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import edu.teco.dustradar.blebridge.KeepAliveManager;

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
    public final static String BROADCAST_BLESERVICE_GATT_CONNECTED = "BROADCAST_BLESERVICE_GATT_CONNECTED";
    public final static String BROADCAST_BLESERVICE_GATT_DISCONNECTED = "BROADCAST_BLESERVICE_GATT_DISCONNECTED";
    public final static String BROADCAST_BLESERVICE_GATT_SERVICES_DISCOVERED = "BROADCAST_BLESERVICE_GATT_SERVICES_DISCOVERED";
    public final static String BROADCAST_BLESERVICE_GATT_MISSING_SERVICE = "BROADCAST_BLESERVICE_GATT_MISSING_SERVICE";

    public final static String BROADCAST_BLESERVICE_FIRST_CONNECT = "BROADCAST_BLESERVICE_FIRST_CONNECT";
    public final static String BROADCAST_BLESERVICE_ERROR = "BROADCAST_BLESERVICE_ERROR";

    public final static String BROADCAST_BLESERVICE_DATA_AVAILABLE = "BROADCAST_BLESERVICE_DATA_AVAILABLE";
    public final static String BROADCAST_BLESERVICE_READ_DATADESCRIPTION = "BROADCAST_BLESERVICE_READ_DATADESCRIPTION";
    public final static String BROADCAST_BLESERVICE_DATADESCRIPTION_AVAILABLE = "BROADCAST_BLESERVICE_DATADESCRIPTION_AVAILABLE";
    public final static String BROADCAST_BLESERVICE_READ_METADATA = "BROADCAST_BLESERVICE_READ_METADATA";
    public final static String BROADCAST_BLESERVICE_METADATA_AVAILABLE = "BROADCAST_BLESERVICE_METADATA_AVAILABLE";

    public final static String EXTRA_BLESERVICE_DATA = "EXTRA_BLESERVICE_DATA";
    public final static String EXTRA_BLESERVICE_ADDRESS = "EXTRA_BLESERVICE_ADDRESS";


    // private members
    ArrayList<String> deviceAddress = new ArrayList<>();
    private boolean shouldReconnect = false;
    private int firstConnect = -1;

    private HashMap<String, BluetoothGatt> mBluetoothGatt = new HashMap<>();

    private HashMap<String, BluetoothGattService> DataService = new HashMap<>();
    private HashMap<String, BluetoothGattCharacteristic> NotifyChar = new HashMap<>();
    private HashMap<String, BluetoothGattCharacteristic> DataChar = new HashMap<>();
    private HashMap<String, BluetoothGattCharacteristic> DataDescriptionChar = new HashMap<>();

    private HashMap<String, BluetoothGattService> MetadataService = new HashMap<>();
    private HashMap<String, BluetoothGattCharacteristic> MetadataChar = new HashMap<>();

    private Queue<Pair<BluetoothGatt, BluetoothGattCharacteristic>> CharFIFO = new LinkedList<>();
    private final int maxCharFIFOsize = 100;

    private PowerManager.WakeLock wakeLock;


    // constructors

    public BLEService() {
    }


    // static service handlers

    public static void startService(Context context, ArrayList<BluetoothDevice> devices) {
        if(context == null || devices == null) {
            throw new Resources.NotFoundException("Cannot start service without context or devices");
        }

        if (isRunning(context)) {
            Log.w(TAG, "Service not started because it is already running");
            return;
        }

        // start service
        Intent serviceIntent = new Intent(context, BLEService.class);

        ArrayList<String> address = new ArrayList<>();
        for (BluetoothDevice device : devices) {
            address.add(device.getAddress());
        }
        serviceIntent.putStringArrayListExtra(BLEService.INTENT_EXTRA_BLE_DEVICE_ADDRESS, address);
        context.startService(serviceIntent);
    }

    public static void stopService(Context context) {
        if(context == null) {
            throw new Resources.NotFoundException("Cannot stop service without context");
        }

        // stop service
        Intent serviceIntent = new Intent(context, BLEService.class);
        context.stopService(serviceIntent);
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

        // get wakelock
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DustRadar::BLEService::Wakelock");
        wakeLock.acquire();


        // get BluetoothAdapter
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            broadcastUpdate(BLEService.BROADCAST_BLESERVICE_ERROR);
            throw new Resources.NotFoundException("BluetoothAdapter is not available in BLEService");
        }

        // get BLE devices and connect
        deviceAddress = intent.getStringArrayListExtra(BLEService.INTENT_EXTRA_BLE_DEVICE_ADDRESS);
        firstConnect = deviceAddress.size();
        shouldReconnect = true;

        for (String address : deviceAddress) {
            BluetoothDevice mDevice = mBluetoothAdapter.getRemoteDevice(address);
            if (mDevice == null) {
                broadcastUpdate(BLEService.BROADCAST_BLESERVICE_ERROR);
                throw new Resources.NotFoundException("BLEDevice is not available");
            }

            mBluetoothGatt.put(mDevice.getAddress(), mDevice.connectGatt(this, true, mGattCallback));
        }

        registerReceiver();

        Log.i(TAG, "BLEService started");
        return START_REDELIVER_INTENT;
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "BLEService destroyed");

        shouldReconnect = false;
        unregisterReceiver();
        for (BluetoothGatt gatt : mBluetoothGatt.values()) {
            if (gatt != null) {
                gatt.close();
            }
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


    // private methods

    private void readCharacteristic(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (gatt == null) {
            Log.w(TAG, "Cannot read BLE without gatt");
            return;
        }

        if (characteristic != null) {
            CharFIFO.add(new Pair<>(gatt, characteristic));
        }

        Pair<BluetoothGatt, BluetoothGattCharacteristic> gattchar = CharFIFO.poll();
        if(gattchar == null) {
            return;
        }
        if (gattchar.first == null || gattchar.second == null) {
            return;
        }

        boolean allowed = gattchar.first.readCharacteristic(gattchar.second);
        if (!allowed) {
            Log.w(TAG, "No permission to read characteristic");
            CharFIFO.add(gattchar);
            Log.i(TAG, "CharFIFO size: " + String.valueOf(CharFIFO.size()));
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

        Log.w(TAG, "device with address " + gatt.getDevice().getAddress() + " does not have required services");
        return false;
    }


    private void broadcastUpdate(final String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final String address) {
        Intent intent = new Intent(action);
        intent.putExtra(EXTRA_BLESERVICE_ADDRESS, address);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final String address, final String data) {
        Intent intent = new Intent(action);
        intent.putExtra(EXTRA_BLESERVICE_DATA, data);
        intent.putExtra(EXTRA_BLESERVICE_ADDRESS, address);
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
                    intentAction = BROADCAST_BLESERVICE_GATT_CONNECTED;
                    broadcastUpdate(intentAction, gatt.getDevice().getAddress());
                    gatt.discoverServices();
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i(TAG, "Disconnected from GATT server");
                    intentAction = BROADCAST_BLESERVICE_GATT_DISCONNECTED;
                    broadcastUpdate(intentAction, gatt.getDevice().getAddress());
                    if (shouldReconnect) {
                        Log.i(TAG, "Trying to reconnect to GATT server");
                        gatt.connect();
                    }
                    break;

                default:
                    Log.w(TAG, "Unhandled message in onConnectionStateChange()");
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
                            broadcastUpdate(BROADCAST_BLESERVICE_GATT_MISSING_SERVICE, gatt.getDevice().getAddress());
                            return;
                        }

                        // services
                        BluetoothGattService dataservice = gatt.getService(DATA_SERVICE_UUID);
                        DataService.put(gatt.getDevice().getAddress(), dataservice);
                        BluetoothGattService metadataservice = gatt.getService(METADATA_SERVICE_UUID);
                        MetadataService.put(gatt.getDevice().getAddress(), metadataservice);

                        // notify characterstic
                        BluetoothGattCharacteristic notifychar = dataservice.getCharacteristic(NOTIFY_CHARACTERISTIC_UUID);
                        NotifyChar.put(gatt.getDevice().getAddress(), notifychar);
                        gatt.setCharacteristicNotification(notifychar, true);
                        BluetoothGattDescriptor descriptor = notifychar.getDescriptor(NOTIFY_DESCRIPTOR_UUID);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);

                        // other characterstics
                        DataChar.put(gatt.getDevice().getAddress(), dataservice.getCharacteristic(DATA_CHARACTERISTIC_UUID));
                        DataDescriptionChar.put(gatt.getDevice().getAddress(), dataservice.getCharacteristic(DATADESCRIPTION_CHARACTERISTIC_UUID));
                        MetadataChar.put(gatt.getDevice().getAddress(), metadataservice.getCharacteristic(METADATA_CHARACTERISTIC_UUID));

                        broadcastUpdate(BROADCAST_BLESERVICE_GATT_SERVICES_DISCOVERED, gatt.getDevice().getAddress());
                        Log.i(TAG, "device " + gatt.getDevice().getAddress() + " connected");

                        if (firstConnect != 0) {
                            firstConnect -= 1;
                        }

                        if (firstConnect == 0) {
                            broadcastUpdate(BROADCAST_BLESERVICE_FIRST_CONNECT);
                        }
                    }
                });
            }
            else {
                Log.w(TAG, "Unhandled status in onServicesDiscovered(): " + status);
            }
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (NotifyChar.containsValue(characteristic)) {
                if (CharFIFO.size() > maxCharFIFOsize) {
                    Log.w(TAG, "CharFIFO has been cleared.");
                    CharFIFO.clear();
                }
                readCharacteristic(gatt, DataChar.get(gatt.getDevice().getAddress()));
                return;
            }

            Log.w(TAG, "Unhandled notification with uuid: " + characteristic.getUuid().toString());
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (DataChar.containsValue(characteristic)) {
                    String data = characteristic.getStringValue(0);
                    broadcastUpdate(BROADCAST_BLESERVICE_DATA_AVAILABLE, gatt.getDevice().getAddress(), data);
                    return;
                }

                if (DataDescriptionChar.containsValue(characteristic)) {
                    String data = characteristic.getStringValue(0);
                    broadcastUpdate(BROADCAST_BLESERVICE_DATADESCRIPTION_AVAILABLE, gatt.getDevice().getAddress(), data);
                    return;
                }

                if (MetadataChar.containsValue(characteristic)) {
                    String data = characteristic.getStringValue(0);
                    broadcastUpdate(BROADCAST_BLESERVICE_METADATA_AVAILABLE, gatt.getDevice().getAddress(), data);
                    return;
                }

                Log.w(TAG, "Unhandled read with uuid: " + characteristic.getUuid().toString());
            }
            else {
                Log.w(TAG, "Unhandled status in onCharacteristicRead(): " + status);
            }

            if (CharFIFO.size() != 0) {
                // read next characteristic in queue
                Log.i(TAG, "CharFIFO size: " + String.valueOf(CharFIFO.size()));
                readCharacteristic(gatt, null);
            }
        }
    });


    // BroadcastReceiver

    private final BroadcastReceiver mReceiver = (new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BLEService.BROADCAST_BLESERVICE_READ_DATADESCRIPTION.equals(action)) {
                if (mBluetoothGatt == null || DataDescriptionChar == null) {
                    Log.e(TAG, "Cannot read datadescription characterstic");
                    return;
                }

                String address = intent.getStringExtra(BLEService.EXTRA_BLESERVICE_ADDRESS);
                if (address == null) {
                    // read all
                    for (String key : DataDescriptionChar.keySet()) {
                        BluetoothGattCharacteristic readchar = DataDescriptionChar.get(key);
                        BluetoothGatt gatt = mBluetoothGatt.get(key);
                        CharFIFO.add(new Pair<>(gatt, readchar));
                    }
                }
                else {
                    BluetoothGattCharacteristic readchar = DataDescriptionChar.get(address);
                    BluetoothGatt gatt = mBluetoothGatt.get(address);

                    if (gatt == null || readchar == null) {
                        Log.w(TAG, "No connection to device with address: " + address);
                        return;
                    }

                    CharFIFO.add(new Pair<>(gatt, readchar));
                }
                return;
            }

            if (BLEService.BROADCAST_BLESERVICE_READ_METADATA.equals(action)) {
                if (mBluetoothGatt == null || MetadataChar == null) {
                    Log.e(TAG, "Cannot read metadata characterstic");
                    return;
                }

                String address = intent.getStringExtra(BLEService.EXTRA_BLESERVICE_ADDRESS);
                if (address == null) {
                    // read all
                    for (String key : MetadataChar.keySet()) {
                        BluetoothGattCharacteristic readchar = MetadataChar.get(key);
                        BluetoothGatt gatt = mBluetoothGatt.get(key);
                        CharFIFO.add(new Pair<>(gatt, readchar));
                    }
                }
                else {
                    BluetoothGattCharacteristic readchar = MetadataChar.get(address);
                    BluetoothGatt gatt = mBluetoothGatt.get(address);

                    if (gatt == null || readchar == null) {
                        Log.w(TAG, "No connection to device with address: " + address);
                        return;
                    }

                    CharFIFO.add(new Pair<>(gatt, readchar));
                }
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

        intentFilter.addAction(BLEService.BROADCAST_BLESERVICE_READ_DATADESCRIPTION);
        intentFilter.addAction(BLEService.BROADCAST_BLESERVICE_READ_METADATA);

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
