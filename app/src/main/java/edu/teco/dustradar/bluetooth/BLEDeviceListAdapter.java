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


/**
 * ListAdapter for storing BLEScan results
 */
public class BLEDeviceListAdapter extends BaseAdapter {

    private ArrayList<BluetoothDevice> mBLEDevices;
    private Activity mActivity;
    private int mBLEListLayout;
    private int mBLENameResource;
    private int mBLEAddressResource;


    /**
     * @param activity Activity that wants to store BLEscan results
     * @param bleListLayout Resource id that points to the layout of a single view item
     * @param bleNameResource Resource id that points to the first TextView in a view item
     * @param bleAddressResource Resource id that points to the second TextView in a view item
     */
    public BLEDeviceListAdapter(Activity activity, int bleListLayout, int bleNameResource, int bleAddressResource) {
        super();

        mBLEDevices = new ArrayList<>();
        mActivity = activity;
        mBLEListLayout = bleListLayout;
        mBLENameResource = bleNameResource;
        mBLEAddressResource = bleAddressResource;
    }


    /**
     * @param device adds a device to the ListAdapter and sorts it
     */
    public void addDevice(BluetoothDevice device) {
        if(! mBLEDevices.contains(device)) {
            mBLEDevices.add(device);
            sort();
        }
    }


    /**
     * @param position Position index for returning a device in the ListAdapter
     * @return BLE device
     */
    public BluetoothDevice getDevice(int position) {
        if (position >= mBLEDevices.size()) {
            return null;
        }

        return mBLEDevices.get(position);
    }


    /**
     * Clear the ListAdapter
     */
    public void clear() {
        mBLEDevices.clear();
    }


    /**
     * Sorts the ListAdapter in lexicographical order. First TextView > second TextView. UNKNOWN < all
     */
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


    /**
     * @return Number of stored devices
     */
    @Override
    public int getCount() {
        return mBLEDevices.size();
    }


    /**
     * @param i Position index for returning a BLE device from the ListAdapter
     * @return BLE device
     */
    @Override
    public Object getItem(int i) {
        return mBLEDevices.get(i);
    }


    /**
     * @param i Position index of the BLE device in the ListAdapter
     * @return hashCode() of the BLE device address string
     */
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


    /**
     * Creates list item view
     * @param i Position index for the BLE device within the ListAdapter
     * @param view old view to reuse
     * @param viewGroup viewGroup - not used
     * @return View of the list item at position i
     */
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
