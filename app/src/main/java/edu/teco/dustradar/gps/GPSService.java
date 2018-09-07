package edu.teco.dustradar.gps;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.Arrays;
import java.util.List;


public class GPSService extends Service implements LocationListener {

    private final static String TAG = GPSService.class.getSimpleName();

    // broadcast actions
    public final static String BROADCAST_LOCATION_PROVIDER_DISABLED = "BROADCAST_LOCATION_PROVIDER_DISABLED";

    private final static List<String> allBroadcasts = Arrays.asList(
            BROADCAST_LOCATION_PROVIDER_DISABLED
    );


    // private members

    private LocationManager mManger;


    // static members

    private static Location mLocation = null;


    // constructors

    public GPSService() {
    }


    // static service handlers

    public static void startService(Context context, BroadcastReceiver receiver) {
        if(context == null) {
            throw new Resources.NotFoundException("Cannot start service without context or device");
        }

        if (isRunning(context)) {
            Log.w(TAG, "Service not started because it is already running");
            return;
        }

        // register BroadcastReceiver for context
        context.registerReceiver(receiver, getIntentFilter());

        // start service
        Intent bleServiceIntent = new Intent(context, GPSService.class);
        context.startService(bleServiceIntent);
    }


    public static void stopService(Context context, BroadcastReceiver receiver) {
        if(context == null) {
            throw new Resources.NotFoundException("Cannot stop service without context");
        }

        // try tounregister BroadcastReceiver for context
        try {
            context.unregisterReceiver(receiver);
        }
        catch (IllegalArgumentException e) {
            // pass
        }

        Intent bleServiceIntent = new Intent(context, GPSService.class);
        context.stopService(bleServiceIntent);
    }


    public static boolean isRunning(Context context) {
        if(context == null) {
            throw new Resources.NotFoundException("Cannot check service without context");
        }

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (GPSService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    // event handlers

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLocation = null;
        mManger = (LocationManager) getSystemService(this.LOCATION_SERVICE);

        try {
            // TODO: accuracy based on setings
            mManger.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            mManger.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }

        return START_REDELIVER_INTENT;
    }


    @Override
    public void onDestroy() {
        mManger.removeUpdates(this);
        mLocation = null;

        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // no binding allowed
        throw new UnsupportedOperationException("binding not allowed");
    }


    // LocationListener implementation

    @Override
    public void onLocationChanged(Location location) {
        // TODO: make sophisticated location update
        mLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        broadcastUpdate(BROADCAST_LOCATION_PROVIDER_DISABLED);
    }


    // static methods

    public static Location getLocation() {
        return mLocation;
    }

    public static boolean hasLocationPermission (Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        return (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }


    @TargetApi(Build.VERSION_CODES.M)
    public static void requestLocationPermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        if (hasLocationPermission(activity)) {
            return;
        }

        activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
    }


    public static boolean hasHighAccuracyPermission(Activity activity) {
        LocationManager manager = (LocationManager) activity.getSystemService(activity.LOCATION_SERVICE);

        boolean hasGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean hasNetwork = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (hasGPS && hasNetwork) {
            return true;
        }
        else {
            return false;
        }
    }


    public static void requestHighAccuracyPermission(final Activity activity, final int requestCode) {
        if (hasHighAccuracyPermission(activity)) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("High Accuracy Disabled");
        builder.setMessage("High accuracy is disabled. In order to use this application properly you need to enable GPS and network based location services.");
        builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent settingsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivityForResult(settingsIntent, requestCode);
            }
        });
        builder.setNegativeButton("No, Just Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public static IntentFilter getIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        for(String broadcast : allBroadcasts) {
            intentFilter.addAction(broadcast);
        }

        return intentFilter;
    }


    // private methods

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

}
