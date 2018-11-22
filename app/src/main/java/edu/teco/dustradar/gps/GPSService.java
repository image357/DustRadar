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
import android.os.PowerManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import edu.teco.dustradar.blebridge.KeepAliveManager;


/**
 * Background service that constantly acquires new GPS and network location fixes
 */
public class GPSService extends Service implements LocationListener {

    private final static String TAG = GPSService.class.getSimpleName();

    // broadcast actions
    public final static String BROADCAST_GPSSERVICE_LOCATION_PROVIDER_DISABLED = "BROADCAST_GPSSERVICE_LOCATION_PROVIDER_DISABLED";
    public final static String BROADCAST_GPSSERVICE_LOCATION_AVAILABLE = "BROADCAST_GPSSERVICE_LOCATION_AVAILABLE";


    // private members
    private PowerManager.WakeLock wakeLock;
    private LocationManager mManger;


    // static members
    private static Location mLocation = null;


    // constructors

    /**
     * Empty constructor. Do not use it!
     */
    public GPSService() {
    }


    // static service handlers

    /**
     * @param context Context that will start the service
     */
    public static void startService(Context context) {
        if(context == null) {
            throw new Resources.NotFoundException("Cannot start service without context or device");
        }

        if (isRunning(context)) {
            Log.w(TAG, "Service not started because it is already running");
            return;
        }

        // start service
        Intent serviceIntent = new Intent(context, GPSService.class);
        context.startService(serviceIntent);
    }

    /**
     * @param context Context that will stop the service
     */
    public static void stopService(Context context) {
        if(context == null) {
            throw new Resources.NotFoundException("Cannot stop service without context");
        }

        Intent serviceIntent = new Intent(context, GPSService.class);
        context.stopService(serviceIntent);
    }

    /**
     * @param context Context that can call getSystemService(...)
     * @return true when running. false otherwise
     */
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
        PowerManager powerManager = (PowerManager) getSystemService(this.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DustRadar::GPSService::Wakelock");
        wakeLock.acquire();

        mLocation = null;
        mManger = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        try {
            // TODO: accuracy based on settings
            mManger.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            mManger.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
        catch (SecurityException e) {
            Log.e(TAG, "Cannot access location providers");
            e.printStackTrace();
        }

        registerReceiver();

        Log.i(TAG, "GPSService started");
        return START_REDELIVER_INTENT;
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "GPSService destroyed");

        mManger.removeUpdates(this);
        mLocation = null;
        unregisterReceiver();

        wakeLock.release();
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // no binding allowed
        throw new UnsupportedOperationException("binding not allowed");
    }


    // LocationListener implementation

    /**
     * @param location new location fix
     */
    @Override
    public void onLocationChanged(Location location) {
        // TODO: make sophisticated location update
        mLocation = location;
        broadcastUpdate(BROADCAST_GPSSERVICE_LOCATION_AVAILABLE);
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
        broadcastUpdate(BROADCAST_GPSSERVICE_LOCATION_PROVIDER_DISABLED);
    }


    // static methods

    /**
     * @return Last known Location. Can be null if no location fix was acquired before.
     */
    public static Location getLocation() {
        return mLocation;
    }

    /**
     * @param activity Activity that will be checked for the permission
     * @return true when the activity has the permission. false otherwise
     */
    public static boolean hasLocationPermission (Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        return (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }


    /**
     * @param activity Activity that tries to request the permission
     * @param requestCode Request code that will be used in onActivityResult(...)
     */
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


    /**
     * @param activity Activity that will be checked for the permission
     * @return true when the activity has the permission. false otherwise
     */
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


    /**
     * @param activity Activity that tries to request the permission
     * @param requestCode Request code that will be used in onActivityResult(...)
     */
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


    // private methods

    private void broadcastUpdate(final String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    // BroadcastReceivers

    private final BroadcastReceiver mReceiver = (new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (KeepAliveManager.BROADCAST_KEEP_ALIVE_PING.equals(action)) {
                Intent reply = new Intent(KeepAliveManager.BROADCAST_KEEP_ALIVE_REPLY);
                sendBroadcast(reply);
                return;
            }
        }
    });

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(KeepAliveManager.BROADCAST_KEEP_ALIVE_PING);

        registerReceiver(mReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        try {
            unregisterReceiver(mReceiver);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

}
