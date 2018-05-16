package edu.teco.dustradar.connection;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.res.Resources;

public class BLEConnection {

    // private members
    private final BluetoothAdapter mBluetoothAdapter;


    // constructors
    public BLEConnection(Context context) {
        BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();
    }


    // public methods
    public BluetoothAdapter getBluetoothAdapter() throws  Resources.NotFoundException {
        if (mBluetoothAdapter == null) {
            throw new Resources.NotFoundException("BLE connection is not initialized.");
        }

        return mBluetoothAdapter;
    }

    public boolean isEnabled(Activity activity) {
        return getBluetoothAdapter().isEnabled();
    }

    public void scanForDevices() {
        // TODO: implement scan
    }


    // private methods
}