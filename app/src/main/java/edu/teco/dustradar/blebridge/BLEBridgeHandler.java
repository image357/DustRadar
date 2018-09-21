package edu.teco.dustradar.blebridge;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import edu.teco.dustradar.R;
import edu.teco.dustradar.bluetooth.BLEService;
import edu.teco.dustradar.data.DataService;
import edu.teco.dustradar.gps.GPSService;
import edu.teco.dustradar.http.HTTPIntent;
import edu.teco.dustradar.http.HTTPService;


public class BLEBridgeHandler extends Fragment {

    private static final String TAG = BLEBridgeHandler.class.getSimpleName();

    // private members

    private Switch recordingSwtich;
    private Switch transmittingSwitch;

    // view updates
    private TextView tvBLEConnectionStatus;
    private String bleConnectionStatus = "Connected";

    private TextView tvGPSConnectionStatus;
    private String gpsConnectionStatus = "Unavailable";

    private TextView tvStoredDatapoints;

    private TextView tvLastData;
    private String lastData = null;

    private long lastViewUpdate = 0;
    private final long minLastViewUpdate = 500;


    // constructors

    public BLEBridgeHandler() {
    }


    // event handlers

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_blebridge_handler, container, false);

        recordingSwtich = rootView.findViewById(R.id.switch_record);
        recordingSwtich.setOnCheckedChangeListener(onRecordingSwitchChange);

        transmittingSwitch = rootView.findViewById(R.id.switch_transmit);
        transmittingSwitch.setOnCheckedChangeListener(onTransmittingSwitchChange);

        tvBLEConnectionStatus = rootView.findViewById(R.id.textView_blebridge_ble_connection_status);
        tvGPSConnectionStatus = rootView.findViewById(R.id.textView_blebridge_gps_connection_status);
        tvStoredDatapoints = rootView.findViewById(R.id.textView_blebridge_datapoints);
        tvLastData = rootView.findViewById(R.id.textView_blebridge_last_data);

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();

        updateView();
        registerHandlerReceiver();
    }


    @Override
    public void onPause() {
        unregisterHandlerReceiver();

        super.onPause();
    }


    private CompoundButton.OnCheckedChangeListener onRecordingSwitchChange = (new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            DataService.setRecord(isChecked);
        }
    });


    private CompoundButton.OnCheckedChangeListener onTransmittingSwitchChange = (new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                final Intent intent = new Intent(HTTPService.BROADCAST_START_TRANSMIT);
                getActivity().sendBroadcast(intent);
            }
            else {
                final Intent intent = new Intent(HTTPService.BROADCAST_STOP_TRANSMIT);
                getActivity().sendBroadcast(intent);
            }
        }
    });


    // private methods

    private void updateView() {
        long currenttime = System.currentTimeMillis();
        if ((currenttime - lastViewUpdate) < minLastViewUpdate) {
            return;
        }
        lastViewUpdate = currenttime;
        String temp;

        temp = getResources().getString(R.string.blebridge_ble_connection_status);
        tvBLEConnectionStatus.setText(temp + "   " + bleConnectionStatus);

        temp = getResources().getString(R.string.blebridge_gps_connection_status);
        tvGPSConnectionStatus.setText(temp + "   " + gpsConnectionStatus);

        temp = getResources().getString(R.string.blebridge_stored_datapoints);
        tvStoredDatapoints.setText(temp + "   " + String.valueOf(DataService.size()));

        temp = getResources().getString(R.string.blebridge_last_data);
        tvLastData.setText(temp + "   " + lastData);
    }


    private void registerHandlerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(DataService.BROADCAST_DATA_STORED);
        filter.addAction(BLEService.BROADCAST_BLE_DATA_AVAILABLE);
        filter.addAction(BLEService.BROADCAST_GATT_CONNECTED);
        filter.addAction(BLEService.BROADCAST_GATT_DISCONNECTED);
        filter.addAction(GPSService.BROADCAST_LOCATION_AVAILABLE);
        filter.addAction(HTTPService.BROADCAST_HTTP_TIMEOUT);
        filter.addAction(HTTPIntent.BROADCAST_HTTP_POST_SUCCESS);
        getActivity().registerReceiver(mHandlerReceiver, filter);
    }


    private void unregisterHandlerReceiver() {
        try {
            getActivity().unregisterReceiver(mHandlerReceiver);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


    // BroadcastReceivers

    private final BroadcastReceiver mHandlerReceiver = (new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (DataService.BROADCAST_DATA_STORED.equals(action)) {
                updateView();
                return;
            }

            if (BLEService.BROADCAST_BLE_DATA_AVAILABLE.equals(action)) {
                lastData = intent.getStringExtra(BLEService.BROADCAST_EXTRA_DATA);
                updateView();
                return;
            }

            if (BLEService.BROADCAST_GATT_CONNECTED.equals(action)) {
                bleConnectionStatus = "Connected";
                updateView();
                return;
            }

            if (BLEService.BROADCAST_GATT_DISCONNECTED.equals(action)) {
                bleConnectionStatus = "Disconnected";
                updateView();
                return;
            }

            if (GPSService.BROADCAST_LOCATION_AVAILABLE.equals(action)) {
                gpsConnectionStatus = "Available";
                updateView();
                return;
            }

            if (HTTPIntent.BROADCAST_HTTP_POST_SUCCESS.equals(action)) {
                updateView();
                return;
            }

            if (HTTPService.BROADCAST_HTTP_TIMEOUT.equals(action)) {
                updateView();
                return;
            }
        }
    });

}
