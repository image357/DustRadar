package edu.teco.dustradar.blebridge;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import java.util.ArrayList;

import edu.teco.dustradar.R;
import edu.teco.dustradar.bluetooth.BLEDeviceListAdapter;
import edu.teco.dustradar.bluetooth.BLEScan;


public class BLEBridgeScan extends Fragment {

    private static final String TAG = BLEBridgeScan.class.getSimpleName();

    // private members

    private BLEScan bleScan;
    private BLEDeviceListAdapter bleDeviceListAdapter;

    private ListView listBLE;
    private Button connectButton;
    private Switch scanningSwitch;

    private boolean isConnecting;


    // constructors

    public BLEBridgeScan() {
    }


    // event handlers

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_blebridge_scan, container, false);

        connectButton = rootView.findViewById(R.id.button_connect);
        connectButton.setOnClickListener(onConnectButtonClick);
        scanningSwitch = rootView.findViewById(R.id.switch_scan);
        scanningSwitch.setOnCheckedChangeListener(onScanningSwitchChange);

        listBLE = rootView.findViewById(R.id.listview_bledevices);
        bleDeviceListAdapter = new BLEDeviceListAdapter(
                getActivity(),
                R.layout.list_item_bledevices,
                R.id.list_item_bledevices_devicename,
                R.id.list_item_bledevices_deviceaddress
        );
        listBLE.setAdapter(bleDeviceListAdapter);
        listBLE.setOnItemClickListener(onListClick);

        bleScan = new BLEScan(getActivity(), bleDeviceListAdapter);

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();

        isConnecting = false;
        connectButton.setText(R.string.blebridge_button_connect);

        bleScan.startScan();
        scanningSwitch.setChecked(true);
    }


    @Override
    public void onPause () {
        bleScan.stopScan();
        bleScan.resetScanResults();
        scanningSwitch.setChecked(false);
        listBLE.clearChoices();
        listBLE.deferNotifyDataSetChanged();

        super.onPause();
    }


    private View.OnClickListener onConnectButtonClick = (new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SparseBooleanArray isChecked = listBLE.getCheckedItemPositions();
            ArrayList<Integer> positions = new ArrayList<>();
            Log.d(TAG, "isChecked size: " + String.valueOf(isChecked.size()));
            Log.d(TAG, "list size: " + String.valueOf(listBLE.getCount()));
            for (int i = 0; i < isChecked.size(); i++) {
                if (isChecked.valueAt(i)) {
                    int index = isChecked.keyAt(i);
                    positions.add(index);
                    Log.d(TAG, "index added: " + String.valueOf(index));
                }
            }

            if (positions.size() == 0) {
                Snackbar.make(v, "Select a DustTracker device first.", Snackbar.LENGTH_SHORT).show();
                return;
            }

            for (int position : positions) {
                if (position < 0 || position >= bleDeviceListAdapter.getCount()) {
                    Snackbar.make(v, "Select a DustTracker device first.", Snackbar.LENGTH_SHORT).show();
                    return;
                }
            }

            ArrayList<BluetoothDevice> devices = new ArrayList<>();
            for (int position : positions) {
                BluetoothDevice device = bleDeviceListAdapter.getDevice(position);
                if (device == null) {
                    Snackbar.make(v, "Cannot connect to the device.", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                devices.add(device);
            }

            if (!isConnecting) {
                isConnecting = true;
                connectButton.setText(R.string.blebridge_button_connecting);
                ((BLEBridge) getActivity()).InitiateBLEConnection(devices);
            }
        }
    });


    private CompoundButton.OnCheckedChangeListener onScanningSwitchChange = (new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                bleScan.startScan();
            }
            else {
                bleScan.stopScan();
            }
        }
    });


    private AdapterView.OnItemClickListener onListClick = (new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            scanningSwitch.setChecked(false);
            bleScan.stopScan();
        }
    });

}
