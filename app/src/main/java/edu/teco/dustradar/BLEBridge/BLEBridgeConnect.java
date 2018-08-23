package edu.teco.dustradar.BLEBridge;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.teco.dustradar.R;
import edu.teco.dustradar.bluetooth.BLEConnection;
import edu.teco.dustradar.bluetooth.BLEDeviceListAdapter;


public class BLEBridgeConnect extends Fragment {

    BLEConnection bleConnection;
    BLEDeviceListAdapter bleDeviceListAdapter;

    public BLEBridgeConnect() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_blebridge_connect, container, false);

        ListView listBLE = rootView.findViewById(R.id.listview_bledevices);
        bleDeviceListAdapter = new BLEDeviceListAdapter(
                getActivity(),
                R.layout.list_item_bledevices,
                R.id.list_item_bledevices_devicename,
                R.id.list_item_bledevices_deviceaddress
        );
        listBLE.setAdapter(bleDeviceListAdapter);

        // bleConnection = new BLEConnection(getActivity());
        // bleConnection.initScan(bleDeviceListAdapter);

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();

        // bleConnection.startScan();
    }

}
