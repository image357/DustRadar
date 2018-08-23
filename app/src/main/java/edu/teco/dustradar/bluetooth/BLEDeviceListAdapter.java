package edu.teco.dustradar.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import edu.teco.dustradar.R;

public class BLEDeviceListAdapter extends BaseAdapter {
    private static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }


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
        }
    }


    public BluetoothDevice getDevice(int position) {
        return mBLEDevices.get(position);
    }


    public void clear() {
        mBLEDevices.clear();
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
        return i;
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
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDevice device = getDevice(i);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0) {
            viewHolder.deviceName.setText(deviceName);
        }
        else {
            viewHolder.deviceName.setText(R.string.unknown_device);
        }

        viewHolder.deviceAddress.setText(device.getAddress());

        return view;
    }

}
