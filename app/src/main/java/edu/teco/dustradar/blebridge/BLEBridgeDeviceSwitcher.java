package edu.teco.dustradar.blebridge;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import edu.teco.dustradar.R;

public class BLEBridgeDeviceSwitcher extends Fragment {

    private static final String TAG = BLEBridgeDeviceSwitcher.class.getSimpleName();

    // parameters
    private static final String ARG_DEVICEADDRESS = "param_deviceaddress";

    private ArrayList<String> deviceAddress;


    // private members
    private ViewPager mViewPager = null;


    // constructors

    public BLEBridgeDeviceSwitcher() {
    }

    public static BLEBridgeDeviceSwitcher newInstance(ArrayList<String> deviceAddress) {
        BLEBridgeDeviceSwitcher fragment = new BLEBridgeDeviceSwitcher();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_DEVICEADDRESS, deviceAddress);
        fragment.setArguments(args);
        return fragment;
    }


    // event handlers

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deviceAddress = getArguments().getStringArrayList(ARG_DEVICEADDRESS);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_blebridge_device_switcher, container, false);

        BLEBridgeDeviceSwitcher.SectionsPagerAdapter mSectionsPagerAdapter =
                new BLEBridgeDeviceSwitcher.SectionsPagerAdapter(
                        getChildFragmentManager(),
                        deviceAddress.size()
                );
        mViewPager = rootView.findViewById(R.id.device_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.getCurrentItem();

        return rootView;
    }


    // FragmentPagerAdapter

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private int numberOfSections = 1;

        SectionsPagerAdapter(FragmentManager fm, int numberOfSections) {
            super(fm);
            this.numberOfSections = numberOfSections;
        }

        @Override
        public Fragment getItem(int position) {
            return BLEBridgeHandler.newInstance(deviceAddress.get(position));
        }

        @Override
        public int getCount() {
            return numberOfSections;
        }

    }

}
