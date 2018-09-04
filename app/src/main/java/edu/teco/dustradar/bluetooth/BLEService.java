package edu.teco.dustradar.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;

public class BLEService extends Service {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;


    // constructors

    public BLEService() {
    }


    // event handlers

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // get BluetoothAdapter
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            throw new Resources.NotFoundException("BluetoothAdapter is not available in BLEService");
        }

        // get BLEDevice
        String deviceAddress = intent.getStringExtra("bleDeviceAddress");
        mDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        if (mDevice == null) {
            throw new Resources.NotFoundException("BLEDevice is not available in BLEService");
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    // private methods

    void connect() {

    }
}
