package edu.teco.dustradar.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import edu.teco.dustradar.R;


public class BLEDeviceListAdapter extends BaseAdapter {

    private ArrayList<BluetoothDevice> mBLEDevices;
    private Activity mActivity;
    private int mBLEListLayout;
    private int mBLENameResource;
    private int mBLEAddressResource;


    public BLEDeviceListAdapter(Activity activity, int bleListLayout, int bleNameResource, int bleAddressResource) {
        super();

        mBLEDevices = new ArrayList<>();
        mActivity = activity;
        mBLEListLayout = bleListLayout;
        mBLENameResource = bleNameResource;
        mBLEAddressResource = bleAddressResource;
    }


    public void addDevice(BluetoothDevice device) {
        if(! mBLEDevices.contains(device)) {
            mBLEDevices.add(device);
            sort();
        }
    }


    public BluetoothDevice getDevice(int position) {
        if (position >= mBLEDevices.size()) {
            return null;
        }

        return mBLEDevices.get(position);
    }


    public void clear() {
        mBLEDevices.clear();
    }


    public void sort() {
        Collections.sort(mBLEDevices, new Comparator<BluetoothDevice>() {
            @Override
            public int compare(BluetoothDevice o1, BluetoothDevice o2) {
                String name1 = o1.getName();
                String name2 = o2.getName();

                if (name1 == null) {
                    if (name2 == null)
                        return o1.getAddress().compareTo(o2.getAddress());
                    else
                        return 1;
                }

                if (name2 == null)
                    return -1;

                int compareval = name1.compareTo(name2);
                if (compareval == 0) {
                    return o1.getAddress().compareTo(o2.getAddress());
                }

                return compareval;
            }
        });
    }


    @Override
    public int getCount() {
        return mBLEDevices.size();
    }


    @Override
    public Object getItem(int i) {
        return mBLEDevices.get(i);
    }


    @Override
    public long getItemId(int i) {
        return getDevice(i).getAddress().hashCode();
    }


    private static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        String deviceNamePrefix;
        String deviceAddressPrefix;
        String deviceUnknown;
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;

        // General ListView optimization code.
        if (view == null) {
            view = mActivity.getLayoutInflater().inflate(mBLEListLayout, null);

            viewHolder = new ViewHolder();
            viewHolder.deviceName = view.findViewById(mBLENameResource);
            viewHolder.deviceAddress = view.findViewById(mBLEAddressResource);
            viewHolder.deviceNamePrefix = view.getResources().getString(R.string.blebridge_devicename);
            viewHolder.deviceAddressPrefix = view.getResources().getString(R.string.blebridge_deviceaddress);
            viewHolder.deviceUnknown = view.getResources().getString(R.string.blebridge_unknown_device);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDevice device = getDevice(i);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0) {
            viewHolder.deviceName.setText(viewHolder.deviceNamePrefix + deviceName);
        }
        else {

            viewHolder.deviceName.setText(viewHolder.deviceNamePrefix + viewHolder.deviceUnknown);
        }

        viewHolder.deviceAddress.setText(viewHolder.deviceAddressPrefix + device.getAddress());

        return view;
    }

}
