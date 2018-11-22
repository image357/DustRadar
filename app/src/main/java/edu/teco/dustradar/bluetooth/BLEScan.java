package edu.teco.dustradar.bluetooth;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;

/**
 * Wrapper class for scanning for BLE devices. Needs BLEDeviceListAdapter to function
 */
public class BLEScan {

    private static final String TAG = BLEScan.class.getSimpleName();

    // private members
    private final BluetoothAdapter mBluetoothAdapter;
    private BLEDeviceListAdapter mBLEDeviceListAdapter = null;

    private boolean scanning;


    /**
     * @param activity Activity that wants to start a scan
     */
    // constructors
    public BLEScan(Activity activity) {
        BluetoothManager bluetoothManager =
                (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();
        scanning = false;
    }


    /**
     * @param activity Activity that wants to start a scan
     * @param adapter ListAdapter to store scan results
     */
    public BLEScan(Activity activity, BLEDeviceListAdapter adapter) {
        this(activity);
        mBLEDeviceListAdapter = adapter;
    }


    // public methods

    /**
     * @return true when BLE is accessible. false otherwise
     */
    public boolean hasBluetooth() {
        if (mBluetoothAdapter == null) {
            return false;
        }

        return true;
    }


    /**
     * @param activity Activity that wants to enable BLE
     * @param requestCode Request code that will be used in onActivityResult(...)
     */
    public void enable(Activity activity, int requestCode) {
        if (isEnabled()) {
            return;
        }

        Intent enableBLE = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBLE, requestCode);
    }


    /**
     * @return true when BLE id enabled. false otherwise
     */
    public boolean isEnabled() {
        return getBluetoothAdapter().isEnabled();
    }


    /**
     * @param adapter ListAdapter that will store scan results
     */
    public void addScanAdapter(BLEDeviceListAdapter adapter) {
        mBLEDeviceListAdapter = adapter;
    }


    /**
     * Starts BLE scan
     */
    @SuppressWarnings("deprecation")
    public void startScan() {
        if (scanning) {
            return;
        }

        scanning = true;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                getBluetoothAdapter().startLeScan(mBLEScanCallback);
            }
        });
    }


    /**
     * Stops BLE scan
     */
    @SuppressWarnings("deprecation")
    public void stopScan() {
        if (!scanning) {
            return;
        }

        scanning = false;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                getBluetoothAdapter().stopLeScan(mBLEScanCallback);
            }
        });
    }


    /**
     * Clears all scan results from the ListAdapter
     */
    public void resetScanResults() {
        BLEDeviceListAdapter adapter = getBLEDeviceListAdapter();
        adapter.clear();
        adapter.notifyDataSetChanged();
    }


    /**
     * @return true when scanning. false otherwise
     */
    public boolean isScanning() {
        return scanning;
    }


    // private methods

    private BluetoothAdapter getBluetoothAdapter() throws  Resources.NotFoundException {
        if (mBluetoothAdapter == null) {
            throw new Resources.NotFoundException("BLE connection is not initialized.");
        }

        return mBluetoothAdapter;
    }


    private BLEDeviceListAdapter getBLEDeviceListAdapter() throws  Resources.NotFoundException {
        if (mBLEDeviceListAdapter == null) {
            throw new Resources.NotFoundException("BLE connection is not initialized.");
        }

        return mBLEDeviceListAdapter;
    }


    // callbacks

    private BluetoothAdapter.LeScanCallback mBLEScanCallback =  new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            BLEDeviceListAdapter adapter = getBLEDeviceListAdapter();
            adapter.addDevice(device);
            adapter.notifyDataSetChanged();
        }
    };


    // location permission for API >= 23

    /**
     * @param activity Activity that will be checked for the permission
     * @return true when the activity has the permission. false otherwise
     */
    public boolean hasLocationPermission (Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        return (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }


    /**
     * @param activity Activity that tries to request the permission
     * @param requestCode Request code that will be used in onActivityResult(...)
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void requestLocationPermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        if (hasLocationPermission(activity)) {
            return;
        }

        activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
    }

}