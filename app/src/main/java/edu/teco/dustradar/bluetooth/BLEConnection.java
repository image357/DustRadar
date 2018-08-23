package edu.teco.dustradar.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.widget.Toast;

public class BLEConnection {

    // private members
    private final BluetoothAdapter mBluetoothAdapter;
    BLEDeviceListAdapter mBLEDeviceListAdapter = null;
    private boolean scanning;


    // constructors
    public BLEConnection(Context context) {
        BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();
    }


    // public methods
    public boolean hasBluetooth() {
        if(mBluetoothAdapter == null) {
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


    public void initScan(BLEDeviceListAdapter bleDeviceListAdapter) {
        mBLEDeviceListAdapter = bleDeviceListAdapter;
    }

    public void startScan() {
        scanning = true;
        getBluetoothAdapter().startLeScan(mBLEScanCallback);
    }

    public void stopScan() {
        scanning = false;
        getBluetoothAdapter().stopLeScan(mBLEScanCallback);
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
            getBLEDeviceListAdapter().addDevice(device);
            getBLEDeviceListAdapter().notifyDataSetChanged();
        }
    };

}