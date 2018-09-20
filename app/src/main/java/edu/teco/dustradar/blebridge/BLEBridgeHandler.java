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
import edu.teco.dustradar.http.HTTPService;


public class BLEBridgeHandler extends Fragment {

    private static final String TAG = BLEBridgeHandler.class.getSimpleName();

    // private members

    Switch recordingSwtich;
    Switch transmittingSwitch;
    TextView dataPoints;


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

        dataPoints = rootView.findViewById(R.id.textView_blebridge_datapoints);

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
            HTTPService.setTransmit(isChecked);
        }
    });


    // private methods

    private void updateView() {
        String temp = getResources().getString(R.string.stored_datapoints);
        dataPoints.setText(temp + "   " + String.valueOf(DataService.size()));
    }

    private void registerHandlerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(DataService.BROADCAST_DATA_STORED);
        filter.addAction(BLEService.BROADCAST_BLE_DATA_AVAILABLE);
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
        private final long minUpdateTime = 500;
        private long lastDatapointsUpdate = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(DataService.BROADCAST_DATA_STORED.equals(action)) {
                long currenttime = System.currentTimeMillis();
                if ((currenttime - lastDatapointsUpdate) > minUpdateTime) {
                    updateView();
                    lastDatapointsUpdate = currenttime;
                }
                return;
            }
        }
    });

}
