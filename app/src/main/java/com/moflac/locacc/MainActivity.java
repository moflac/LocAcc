package com.moflac.locacc;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import android.content.ServiceConnection;
import android.widget.Toast;
import android.widget.ToggleButton;


public class MainActivity extends Activity {

    // textfields containing updated data
    TextView txtLat;
    TextView txtLong;
    TextView txtSpeed;
    TextView txtBearing;
    TextView txtAltitude;
    TextView txtAccuracy;
    TextView txtX;
    TextView txtY;
    TextView txtZ;
    // record button
    ToggleButton toggle;
    // record button status
    boolean onToggle;

    // service collecting, providing and writing data
    private SenService mService = null;
    private boolean mBound = false;

    private BroadcastReceiver broadcastReceiver;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerReceiver();

        toggle = (ToggleButton)findViewById(R.id.toggleButton);

        //startLocationUpdates();

        // check permissions

         if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            !=  PackageManager.PERMISSION_GRANTED  ){
                requestPermissions(new String[]{
                                    android.Manifest.permission.ACCESS_FINE_LOCATION },
                                    REQUEST_CODE_ASK_PERMISSIONS);
                return ;
         }
         else
                startService();


    }

    // start service for location and accelerometer
    public void startService() {
        Intent serviceIntent = new Intent(this, SenService.class);
        // bind service for button controls
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        startService(serviceIntent);
    }

    // register broadcast receiver for location and acceleration values
    private void registerReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override

            // when receiving intent get values from extras and fill textfields
            public void onReceive(Context context, Intent intent) {

                if(intent!=null) {
                    // get and set gps values
                    Bundle b = intent.getBundleExtra("Location");

                    if (b != null) {
                        // get textviews
                        Location lastLoc = (Location) b.getParcelable("Location");
                        txtLat = (TextView) findViewById(R.id.textView5);
                        txtLong = (TextView) findViewById(R.id.textView6);
                        txtBearing = (TextView) findViewById(R.id.textView7);
                        txtSpeed = (TextView) findViewById(R.id.textView11);
                        txtAltitude = (TextView) findViewById(R.id.textView10);
                        txtAccuracy = (TextView) findViewById(R.id.textView12);
                        // set textview data (with locale specific decimal separator :)
                        txtLat.setText(String.format("%f", lastLoc.getLatitude()));
                        txtLong.setText(String.format("%f", lastLoc.getLongitude()));
                        txtBearing.setText(String.format("%.2f", lastLoc.getBearing()));
                        txtSpeed.setText(String.format("%.2f", lastLoc.getSpeed() * 3.6) + " km/h");
                        txtAltitude.setText(String.format("%.2f", lastLoc.getAltitude()) + " m");
                        txtAccuracy.setText(String.format("%.2f", lastLoc.getAccuracy())+ " m");

                    }

                    // set acceleration values
                    float[] accel = intent.getFloatArrayExtra("Acceleration");
                    if (accel != null) {
                        txtX = (TextView) findViewById(R.id.textView17);
                        txtY = (TextView) findViewById(R.id.textView19);
                        txtZ = (TextView) findViewById(R.id.textView20);
                        // with three decimal accuracy
                        for(int i = 0; i<3; i++ ) {
                            if (Math.abs(accel[i]) < 0.01)
                                accel[i] = 0;
                        }
                        txtX.setText(String.format("%.2f", accel[0])+" m/s²");
                        txtY.setText(String.format("%.2f", accel[1])+" m/s²");
                        txtZ.setText(String.format("%.2f", accel[2])+" m/s²");

                    }
                }
            }
        };
        // register receivers for location and acceleration intents
        registerReceiver(broadcastReceiver, new IntentFilter("GPSUpdate"));
        registerReceiver(broadcastReceiver, new IntentFilter("AccUpdate"));
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onDestroy(){
        // if still recording, save to file
        mService.stopRecording();
        unregisterReceiver(broadcastReceiver);
        unbindService(connection);

        super.onDestroy();
        // unregister the receiver when the activity is destroyed


    }
    @Override
    protected void onResume() {
        super.onResume();
       // registerReceiver();


    }

    @Override
    protected void onPause() {
        //unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // start service with granted permission
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startService();
                } else {
                    // Permission Denied
                    Toast.makeText( this, getResources().getString(R.string.t_no_permission) , Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // IBinder and get LocalService instance
            SenService.LocalBinder binder = (SenService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    // when recording toggle is pressed
    public void onToggleClicked(View view) {
        // Is the toggle on?
        onToggle = ((ToggleButton) view).isChecked();
        // on - start recording
        if (onToggle) {
            mService.startRecording();
        // off - stop recording, save on file
        } else {
            mService.stopRecording();
        }
    }

    // change layout when orientation changed
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main);
        } else {
            setContentView(R.layout.activity_main);
        }
        // restore toggle status
        if(onToggle)
        {
            ToggleButton tgl=(ToggleButton)findViewById(R.id.toggleButton);
            tgl.setChecked(true);
        }
    }

}


