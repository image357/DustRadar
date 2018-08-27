package edu.teco.dustradar.BLEBridge;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import edu.teco.dustradar.R;
import edu.teco.dustradar.bluetooth.BLEConnection;

public class BLEBridge extends AppCompatActivity {

    private static final String TAG = BLEBridge.class.getName();

    private BLEConnection bleConnection;

    private Long lastTimestamp;


    // request codes
    private final int BLE_ENABLE_REQUEST_CODE = 1;
    private final int FINE_LOCATION_PERMISSION_REQUEST_CODE = 2;


    // event handlers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blebridge);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            return;
        }

        bleConnection = new BLEConnection(this);
        if (! bleConnection.hasBluetooth()) {
            return;
        }

        BLEBridgeConnect firstFragment = new BLEBridgeConnect();
        firstFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, firstFragment).commit();
    }


    @Override
    public void onResume() {
        super.onResume();

        lastTimestamp = System.currentTimeMillis();
        makePermissionChecks();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BLE_ENABLE_REQUEST_CODE:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this, "You have to enable BLE to use this mode.",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "You have to allow location access to use this mode.",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_blebridge, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        Long currentTimestamp = System.currentTimeMillis();

        int minBackDifference = 300;
        if ((currentTimestamp - lastTimestamp) < minBackDifference) {
            super.onBackPressed();
            return;
        }

        Snackbar.make(findViewById(R.id.blebridge_content), "Tap twice and fast to exit",
                Snackbar.LENGTH_LONG).setAction("Action", null).show();

        lastTimestamp = currentTimestamp;
    }


    // private methods

    private void makePermissionChecks() {
        if (! bleConnection.hasBluetooth()) {
            Toast.makeText(this, "BLE is not supported on your device.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        bleConnection.enable(this, BLE_ENABLE_REQUEST_CODE);
        bleConnection.requestLocationPermission(this, FINE_LOCATION_PERMISSION_REQUEST_CODE);
    }

}
