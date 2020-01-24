package com.moflac.locacc;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
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
        simpleFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(DateFormat.getDateTimeInstance().format(new Date()))
                .setContentText("")
                .addAction(R.drawable.closewindow24, getString(R.string.t_quit),
                        pendingIntent)
                .setSmallIcon(R.drawable.location96)
                .setContentIntent(pendingIntent)


                .build();
        startForeground(111, notification);
        startLocationUpdates();
        // sendMessageToActivity("hello");
        //do heavy work on a background thread
        //stopSelf();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
       // PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
       // PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LocAcc:tag");
       // wl.acquire(10000);

        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    private void sendLocationToActivity(Location loc, int j) {
        Bundle b = new Bundle();
        b.putParcelable("Location", loc);

        Intent intent = new Intent("GPSUpdate");
        // You can also include some extra data.
        //intent.putExtra("Location", loc);
        intent.putExtra("Location", b);
        intent.putExtra("counter",j);
       // Bundle b = new Bundle();
       // b.putParcelable("Location", l);
       // intent.putExtra("Location", b);
        sendBroadcast(intent);
    }
    private void sendAccelerationToActivity(float[] acceleration) {

        Intent intent = new Intent("AccUpdate");
        // You can also include some extra data.
        //intent.putExtra("Location", loc);
        intent.putExtra("Acceleration", acceleration);
        // Bundle b = new Bundle();
        // b.putParcelable("Location", l);
        // intent.putExtra("Location", b);
        sendBroadcast(intent);
    }
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        //mLocationRequest.setFastestInterval(FASTEST_INTERVAL);



        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // Google API SDK v11 uses getFusedLocationProviderClient(this)
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // get timestamp and location

                        ddate=simpleFormat.format( new Date() );
                        curLocation=locationResult.getLastLocation();
                        sendLocationToActivity(curLocation, i);

                        i++;
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

        // leave out small jitters
        for(int i=0; i<3; i++)
        {
            if (linear_acceleration[i] < 0.01f) {
                linear_acceleration[i] = 0f;

            }
            else
            {
                pass = true;
                zeroes = 0;

            }
        }
       if (pass == true || zeroes < 2)
        {   Log.i("service", linear_acceleration[0]+" x "+ zeroes);
            pass = false;
            sendAccelerationToActivity(linear_acceleration);

       }
       zeroes++;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void startRecording(){
        recording = true;
        storedRows.clear();
      //  String tmpTime = timeStampFormat.format( new Date() );
      //  Log.i("xxxx",tmpTime);
    }
    public void stopRecording(){
        recording = false;
        mWriter.writeFile(storedRows, this);



    }

    public void saveRow(String date, Location loc, int i) {
        // add current data on arraylist
        if(storedRows!=null) {
            DataRow trow = new DataRow();
            trow.time = date;
            trow.accuracy = loc.getAccuracy();
            trow.altitude = loc.getAltitude();
            trow.bearing = loc.getBearing();
            trow.latitude = loc.getLatitude();
            trow.longitude = loc.getLongitude();
            trow.speed = loc.getSpeed();
            trow.x = linear_acceleration[0];
            trow.y = linear_acceleration[1];
            trow.z = Float.valueOf(i); //linear_acceleration[2];

            storedRows.add(trow);
        }
    }


    public class LocalBinder extends Binder {
        SenService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SenService.this;
        }
    }

}