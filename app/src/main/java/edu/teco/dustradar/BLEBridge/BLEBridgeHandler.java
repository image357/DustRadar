package edu.teco.dustradar.blebridge;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.teco.dustradar.R;
import edu.teco.dustradar.bluetooth.BLEService;


public class BLEBridgeHandler extends Fragment {

    private static final String TAG = BLEBridgeHandler.class.getSimpleName();


    // constructors

    public BLEBridgeHandler() {
    }


    // event handlers

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_blebridge_handler, container, false);

        BLEService.readMetadata();

        return rootView;
    }

}
