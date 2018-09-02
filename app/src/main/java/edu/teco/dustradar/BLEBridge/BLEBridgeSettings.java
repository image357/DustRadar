package edu.teco.dustradar.BLEBridge;


import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import edu.teco.dustradar.R;

/**
 * A simple {@link PreferenceFragmentCompat} subclass.
 */
public class BLEBridgeSettings extends PreferenceFragmentCompat {


    public BLEBridgeSettings() {
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.fragment_blebridge_settings);
    }

}
