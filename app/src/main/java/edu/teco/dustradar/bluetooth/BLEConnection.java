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
import android.os.Build;
import android.support.v4.content.ContextCompat;

public class BLEConnection {

    // private members
    private final BluetoothAdapter mBluetoothAdapter;
    private BLEDeviceListAdapter mBLEDeviceListAdapter = null;

    private boolean scanning;


    // constructors
    public BLEConnection(Activity activity) {
        BluetoothManager bluetoothManager =
                (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();
        scanning = false;
    }


    public BLEConnection(Activity activity, BLEDeviceListAdapter adapter) {
        this(activity);
        mBLEDeviceListAdapter = adapter;
    }


    // public methods
    public boolean hasBluetooth() {
        if (mBluetoothAdapter == null) {
            return false;
        }

        return true;
    }


    public void enable(Activity activity, int requestCode) {
        if (isEnabled()) {
            return;
        }

        Intent enableBLE = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBLE, requestCode);
    }


    public boolean isEnabled() {
        return getBluetoothAdapter().isEnabled();
    }


    public void addScanAdapter(BLEDeviceListAdapter adapter) {
        mBLEDeviceListAdapter = adapter;
    }


    @SuppressWarnings("deprecation")
    public void startScan() {
        scanning = true;
        getBluetoothAdapter().startLeScan(mBLEScanCallback);
    }


    @SuppressWarnings("deprecation")
    public void stopScan() {
        scanning = false;
        getBluetoothAdapter().stopLeScan(mBLEScanCallback);
    }


    public void resetScanResults() {
        BLEDeviceListAdapter adapter = getBLEDeviceListAdapter();
        adapter.clear();
        adapter.notifyDataSetChanged();
    }


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

    public boolean hasLocationPermission (Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        return (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }


    @TargetApi(Build.VERSION_CODES.M)
    public void requestLocationPermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        if (hasLocationPermission(activity)) {
            return;
        }

        activity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode);
    }

}