package com.moflac.locacc;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.NotificationCompat.Builder;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class SenService extends Service implements SensorEventListener {
    public static final String CHANNEL_ID = "LocAcc";
    private LocationRequest mLocationRequest;
    private SensorManager sensorManager;
    private Sensor sensor;
    private final IBinder binder = new LocalBinder();

    private long UPDATE_INTERVAL =  5 * 1000;  /* 10 secs */
    //private long FASTEST_INTERVAL = 500; /* 0.5 sec */
    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];
    private int zeroes = 0;
    private String ddate;
    SimpleDateFormat simpleFormat;
    SimpleDateFormat timeStampFormat;
    // data recording status
    private boolean recording = false;
    // current location
    private Location curLocation;

    int i=0;

    // data structures used to write to file
    DataRow storedData;
    private ArrayList<DataRow> storedRows = new ArrayList<>();
    DataWriter mWriter = new DataWriter();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // set timestamp format for file rows
        simpleFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
        // initiat location updates
        startLocationUpdates();
        // initiate accelerometer updates
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
       stopForeground(true);
       super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void sendLocationToActivity(Location loc, int j) {
        Bundle b = new Bundle();
        b.putParcelable("Location", loc);
        // send location to activity in a bundle
        Intent intent = new Intent("GPSUpdate");
        intent.putExtra("Location", b);

        sendBroadcast(intent);
    }
    private void sendAccelerationToActivity(float[] acceleration) {
        // send acceleration array to main activity
        Intent intent = new Intent("AccUpdate");
        intent.putExtra("Acceleration", acceleration);
        sendBroadcast(intent);
    }
    protected void startLocationUpdates() {
        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // API SDK v11 uses getFusedLocationProviderClient(this)
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // get timestamp and location

                        ddate=simpleFormat.format( new Date() );
                        curLocation=locationResult.getLastLocation();
                        sendLocationToActivity(curLocation, i);

                        // store data to arraylist if button is pressed
                        if(recording == true)
                        {
                            saveRow(ddate, curLocation, i);
                        }
                    }
                },
                Looper.myLooper());
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // called on changing accelerometer values
        final float alpha = 0.8f;
        boolean pass = false;

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];

        sendAccelerationToActivity(linear_acceleration);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // record button pressed, start service in foreground with notification
    public void startRecording(){
        recording = true;
        storedRows.clear();
        startForeground(111, getNotification());

    }
    // recording stopped, move service to background and write values to a file
    public void stopRecording(){

        stopForeground(true);
        // IF recording, save file
        if(recording) {
            mWriter.writeFile(storedRows, this);
            recording = false;
        }



    }

    public void saveRow(String date, Location loc, int i) {
        // add current data on arraylist
        if(storedRows!=null) {
            DataRow trow = new DataRow();
            trow.setTime(date);
            trow.setAccuracy(loc.getAccuracy());
            trow.setAltitude(loc.getAltitude());
            trow.setBearing(loc.getBearing());
            trow.setLatitude(loc.getLatitude());
            trow.setLongitude(loc.getLongitude());
            trow.setSpeed(loc.getSpeed()*3.6f);
            trow.setX(linear_acceleration[0]);
            trow.setY(linear_acceleration[1]);
            trow.setZ(linear_acceleration[2]);

            storedRows.add(trow);
        }
    }


    public class LocalBinder extends Binder {
        SenService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SenService.this;
        }
    }
    private Notification getNotification() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        String ndate=sdf.format( new Date() );
        Intent notificationIntent = new Intent(this,  MainActivity.class);

        // The PendingIntent to launch activity.
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
               notificationIntent,  0);
        // build notification
        Builder builder;
        builder = new Builder(this, CHANNEL_ID)
                // started at time...
                .setContentTitle(getString(R.string.n_text)+": "+ ndate)
                // notification icon
                .setSmallIcon(R.drawable.location96)
                .setContentIntent(pendingIntent);

        return builder.build();
    }

}