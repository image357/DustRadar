package edu.teco.dustradar.BLEBridge;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import edu.teco.dustradar.R;
import edu.teco.dustradar.bluetooth.BLEConnection;
import edu.teco.dustradar.bluetooth.BLEDeviceListAdapter;


public class BLEBridgeConnect extends Fragment {

    private static final String TAG = BLEBridgeConnect.class.getName();

    BLEConnection bleConnection;
    BLEDeviceListAdapter bleDeviceListAdapter;

    ListView listBLE;
    Button connectButton;
    Switch scanningSwitch;


    // constructors

    public BLEBridgeConnect() {
    }


    // event handlers

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_blebridge_connect, container, false);

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

        bleConnection = new BLEConnection(getActivity(), bleDeviceListAdapter);

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();

        connectButton.setText(R.string.blebridge_button_connect);

        bleConnection.startScan();
        scanningSwitch.setChecked(true);
    }


    @Override
    public void onPause () {
        bleConnection.stopScan();
        bleConnection.resetScanResults();
        scanningSwitch.setChecked(false);
        listBLE.clearChoices();
        listBLE.deferNotifyDataSetChanged();

        super.onPause();
    }


    private View.OnClickListener onConnectButtonClick = (new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int postion = listBLE.getCheckedItemPosition();


            if (postion < 0 || postion >= bleDeviceListAdapter.getCount()) {
                Snackbar.make(v, "First select a DustTracker device.", Snackbar.LENGTH_SHORT).show();
                return;
            }

            BluetoothDevice device = bleDeviceListAdapter.getDevice(postion);
            if (device == null) {
                Snackbar.make(v, "Cannot connect to the device.", Snackbar.LENGTH_SHORT).show();
                return;
            }

            connectButton.setText(R.string.blebridge_button_connecting);
        }
    });


    private CompoundButton.OnCheckedChangeListener onScanningSwitchChange = (new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                bleConnection.startScan();
            }
            else {
                bleConnection.stopScan();
            }
        }
    });

}
