package com.moflac.locacc;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ServiceConnection;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

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
    TextView txtCounter;
    ToggleButton toggle;
    int i=0;

       // background service
    private SenService mService = null;
    private boolean mBound = false;
    DecimalFormat df = new DecimalFormat("#.##");
    private BroadcastReceiver broadcastReceiver;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        registerReceiver();

        toggle = (ToggleButton)findViewById(R.id.toggleButton);

        //startLocationUpdates();

        // check permissions
        if ( Build.VERSION.SDK_INT >= 23){
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
        //startService();
    }

    // start service for location and accelerometer in foreground
    public void startService() {
        Intent serviceIntent = new Intent(this, SenService.class);
        // bind service for button controls
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        ContextCompat.startForegroundService(this, serviceIntent);
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
                    int j = intent.getIntExtra("counter",0);
                    if (b != null) {


                        Location lastLoc = (Location) b.getParcelable("Location");
                        txtLat = (TextView) findViewById(R.id.textView5);
                        txtLong = (TextView) findViewById(R.id.textView6);
                        txtBearing = (TextView) findViewById(R.id.textView7);
                        txtSpeed = (TextView) findViewById(R.id.textView11);
                        txtAltitude = (TextView) findViewById(R.id.textView10);
                        txtAccuracy = (TextView) findViewById(R.id.textView12);
                        txtCounter = (TextView) findViewById(R.id.textView15);
                        txtLat.setText(Double.toString(lastLoc.getLatitude()));
                        txtLong.setText(Double.toString(lastLoc.getLongitude()));
                        txtBearing.setText(Float.toString(lastLoc.getBearing()));
                        txtSpeed.setText(df.format(lastLoc.getSpeed() * 3.6) + " km/h");
                        txtAltitude.setText(df.format(lastLoc.getAltitude()));
                        txtAccuracy.setText(Float.toString(lastLoc.getAccuracy()));
                        txtCounter.setText(Integer.toString(j));


                    }

                    // set acceleration values
                    float[] accel = intent.getFloatArrayExtra("Acceleration");
                    if (accel != null) {
                        txtX = (TextView) findViewById(R.id.textView17);
                        txtY = (TextView) findViewById(R.id.textView19);
                        txtZ = (TextView) findViewById(R.id.textView20);
                        txtX.setText(df.format(accel[0])+" m/s²");
                        txtY.setText(df.format(accel[1])+" m/s²");
                        txtZ.setText(df.format(accel[2])+" m/s²");
                        //txtZ.setText(mService.getDate());
                    }
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("GPSUpdate"));
        registerReceiver(broadcastReceiver, new IntentFilter("AccUpdate"));
    }
    @Override
    protected void onStop() {


        super.onStop();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        // unregister the receiver when the activity is destroyed

            unregisterReceiver(broadcastReceiver);

    }
    @Override
    protected void onResume() {
        super.onResume();
        //registerReceiver(broadcastReceiver, new IntentFilter("GPSAccUpdate"));
        //registerReceiver(broadcastReceiver, new IntentFilter("AccUpdate"));

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

    public void onToggleClicked(View view) {
        // Is the toggle on?
        boolean on = ((ToggleButton) view).isChecked();

        if (on) {
            mService.startRecording();

        } else {
            mService.stopRecording();
        }
    }
}

