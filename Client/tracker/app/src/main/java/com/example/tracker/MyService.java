package com.example.tracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.renderscript.RenderScript;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

public class MyService extends Service implements SensorEventListener {

    SensorManager sensorManager;
    Sensor accelerometer;
    Sensor magneticField;
    Sensor gravity;
    Sensor gyroscope;
    Sensor linaccelerometer;
    boolean recording = false;
    String fileName;
    double THRESOLD = 2.0;
    double accx, accy, accz, prevx, prevy, prevz = 0.0;
    double startLat, startLon;
    boolean firstTime = true;
    BroadcastReceiver broadcastReceiver;
    String activityString = "";
    int timer;
    int timerPos;
    DataAnalysis dA = new DataAnalysis();
    utility ut = new utility();
    float[] acc = new float[16];
    float[] gyr = new float[3];
    float[] grav = new float[16];
    float[] magn = new float[16];
    float[] rM = new float[16];
    float[] iM = new float[16];
    float[] earthAcc = new float[4];
    float[] lacc = new float[3];
    Date startTime, stopTime;
    public static final String BROADCAST_ACTION = "com.websmithing.broadcasttest.displayevent";
    Intent intent1 = new Intent(BROADCAST_ACTION);
    String toWrite = "";
    String toUpload = "";
    double avgSpeed = 0;
    double previousLat, previousLon;
    Date previousTime;
    double totDistance;
    double lat, lon;
    int partsCount = 0;
    LocationRequest mLocationRequest = null;
    LocationCallback mLocationCallback;
    FusedLocationProviderClient mFusedLocationClient;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {

        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        if (recording) {
            intent1 = new Intent(BROADCAST_ACTION);
            intent1.putExtra("WHATTODO", "stillfigure");
            sendBroadcast(intent1);
            recording = false;
            stopRecording();
        }
        stopTracking();
        sensorManager.unregisterListener(this);
        stopSelf();
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timer = 1;
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        linaccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


        broadcastReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("activity_intent")) {
                    int activitytype = intent.getIntExtra("type", -1);
                    int activityconfidence = intent.getIntExtra("confidence", 0);
                    try {
                        handleUserActivity(activitytype, activityconfidence);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        };


        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("activity_intent"));


        sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, linaccelerometer, SensorManager.SENSOR_DELAY_NORMAL);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location loc : locationResult.getLocations()) {
                    lat = loc.getLatitude();
                    lon = loc.getLongitude();
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null) {
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                }
                startTracking();
            }
        });


        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void handleUserActivity(int activity, int activityconfidence) throws ParseException {

        switch (activity) {
            case DetectedActivity.STILL: {
                activityString = "STILL";
                break;
            }
            case DetectedActivity.WALKING: {
                activityString = "WALKING";
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                activityString = "BICYCLE";
                break;
            }

            case DetectedActivity.IN_VEHICLE: {
                activityString = "VEHICLE";
                break;
            }
        }


        if (activityString != null && activityconfidence > 75) {
            if (activityString.equals("BICYCLE")) {
                intent1 = new Intent(BROADCAST_ACTION);
                intent1.putExtra("WHATTODO", "bikefigure");
                sendBroadcast(intent1);
                if (!recording) {
                    recording = true;
                    startRecording();
                }
            } else {
                intent1 = new Intent(BROADCAST_ACTION);
                intent1.putExtra("WHATTODO", "stillfigure");
                sendBroadcast(intent1);
                if (recording) {
                    recording = false;
                    stopRecording();
                }
            }
        }
    }


    private void startRecording() {

        startTime = new Date();
        String pattern = "dd-M-yyyy hh:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        fileName = simpleDateFormat.format(startTime) + "_" + Build.MODEL;
        Log.e("model", Build.MODEL);
        firstTime = true;
        timer = 1; //timer per decidere quando raccogliere i dati dai sensori
        timerPos = 6001; //timer che indica quando registrare i dati. Impostato a 6000 registra dati ogni 50 secondi
        avgSpeed = 0;
        totDistance = 0;
        partsCount = 0;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);


        recording = true;


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void stopRecording() {

        stopTime = new Date();
        double timeInSecs = (stopTime.getTime() - previousTime.getTime()) / 1000;


        mFusedLocationClient.removeLocationUpdates(mLocationCallback);

        Location previous = new Location("");
        previous.setLatitude(previousLat);
        previous.setLongitude(previousLon);

        Location now = new Location("");
        now.setLatitude(lat);
        now.setLongitude(lon);

        double distanceInMeters = previous.distanceTo(now);


        double speed = distanceInMeters / timeInSecs;



        String cityName = "undefined";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(43.444, 29.444, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(addresses!=null && addresses.size() > 0) {
            if (addresses.get(0).getLocality() != null) {
                cityName = addresses.get(0).getLocality();
            }
        }

        String tempFileName = fileName + "_" + cityName + "_" + partsCount;

        String speedInString = "" + speed;
        if (speedInString.length() > 5) {
            speedInString = speedInString.substring(0, 5);
        }

        toWrite += "_" + speedInString + "_" + lat + "_" + lon;


        dA.analysis(this, toWrite, tempFileName, getFilesDir(), distanceInMeters, speed);
        //ut.uploadRide(this, tempFileName, previousLat, previousLon, lat, lon, toUpload);
        ut.simpleRideUpload(this, getFilesDir(), tempFileName, toUpload);
    }

    private void startTracking() {
        Intent intent = new Intent(this, ActivityRecognitionService.class);
        startService(intent);
    }

    private void stopTracking() {
        Intent intent = new Intent(this, ActivityRecognitionService.class);
        stopService(intent);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        boolean changes = false;
        if (!recording) {
            return;
        } else {


            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                if(acc[0] != sensorEvent.values[0])
                {
                    acc[0] = sensorEvent.values[0];
                    changes = true;
                }
                if(acc[1] != sensorEvent.values[1])
                {
                    acc[1] = sensorEvent.values[1];
                    changes = true;
                }
                if(acc[2] != sensorEvent.values[2])
                {
                    acc[2] = sensorEvent.values[2];
                    changes = true;
                }



            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GRAVITY) {
                if (sensorEvent.values[0] != 0.0 && sensorEvent.values[1] != 0.0 && sensorEvent.values[2] != 0.0) {
                    grav[0] = sensorEvent.values[0];
                    grav[1] = sensorEvent.values[1];
                    grav[2] = sensorEvent.values[2];

                }
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                if(magn[0] != sensorEvent.values[0]) {
                    magn[0] = sensorEvent.values[0];
                    changes = true;
                }
                if(magn[1] != sensorEvent.values[1])
                {
                    magn[1] = sensorEvent.values[1];
                    changes = true;
                }
                if(magn[2] != sensorEvent.values[2])
                {
                    magn[2] = sensorEvent.values[2];
                    changes = true;
                }
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    if(gyr[0] != sensorEvent.values[0])
                    {
                        gyr[0] = sensorEvent.values[0];
                        changes = true;
                    }
                    if(gyr[1] != sensorEvent.values[1])
                    {
                        gyr[1] = sensorEvent.values[1];
                        changes = true;
                    }
                    if(gyr[2] != sensorEvent.values[2])
                    {
                        gyr[2] = sensorEvent.values[2];
                        changes = true;
                    }
                }
            if(sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
                if(lacc[0] != sensorEvent.values[0])
                {
                    lacc[0] = sensorEvent.values[0];
                    changes = true;
                }
                if(lacc[1] != sensorEvent.values[1])
                {
                    lacc[1] = sensorEvent.values[1];
                    changes = true;
                }
                if(lacc[2] != sensorEvent.values[2])
                {
                    lacc[2] = sensorEvent.values[2];
                    changes = true;
                }
            }
            }

            if (grav[0] != 0.0 && magn[0] != 0.0) {
                SensorManager.getRotationMatrix(rM, iM, grav, magn);

                float[] inv = new float[16];


                android.opengl.Matrix.invertM(inv, 0, rM, 0);
                acc[3] = 1F;
                android.opengl.Matrix.multiplyMV(earthAcc, 0, inv, 0, acc, 0);

                accx = earthAcc[0];
                accy = earthAcc[1];
                accz = earthAcc[2];
            }


            double deltaZ = 0;
            timerPos--;
            timer--;

            if (firstTime) {


                firstTime = false;
                previousTime = startTime;

                startLat = lat;
                startLon = lon;


                previousLat = startLat;
                previousLon = startLon;

                timer = 30;
                toWrite = "";
                toUpload = "";
                toWrite += "_" + previousLat + "_" + previousLon + "_" + "0#0#0";
            } else {
                if(timerPos == 0)
                {
                    Log.e("Previous", previousLat + "");
                    Location previous = new Location("");
                    previous.setLatitude(previousLat);
                    previous.setLongitude(previousLon);

                    Location locnow = new Location("");
                    if(lat != 0.0 && lon != 0.0) {
                        locnow.setLatitude(lat);
                        locnow.setLongitude(lon);
                    }
                    else{
                        locnow.setLongitude(previousLon);
                        locnow.setLatitude(previousLat);
                    }

                    double distanceInMeters = previous.distanceTo(locnow);
                    Date tempTime = new Date();
                    double timeInSecs = (tempTime.getTime() - previousTime.getTime()) / 1000;
                    double speed = distanceInMeters / timeInSecs;

                    String speedInString = "" + speed;
                    if (speedInString.length() > 5) {
                        speedInString = speedInString.substring(0, 5);
                    }

                    toWrite += "_" + speedInString + "_" + lat + "_" + lon;

                    String tempFileName = fileName + "_" + partsCount;

                    dA.analysis(this, toWrite, tempFileName, getFilesDir(), distanceInMeters, speed);
                    //ut.uploadRide(this, tempFileName, previousLat, previousLon, lat, lon, toUpload);
                    ut.simpleRideUpload(this, getFilesDir(), tempFileName, toUpload);

                    previousTime = tempTime;
                    if(lat != 0.0 && lon != 0.0)
                    {
                        previousLat = lat;
                        previousLon = lon;
                    }

                    timerPos = 6000;

                    partsCount++;

                    toWrite = "_" + previousLat + "_" + previousLon + "_" + "0#0#0";
                    toUpload = "";

                }
                if(timer == 0) {
                    double deltaX = Math.abs(prevx - accx);
                    double deltaY = Math.abs(prevy - accy);
                    deltaZ = Math.abs(prevz - accz);
                    if (deltaX < THRESOLD)
                        deltaX = 0;
                    if (deltaY < THRESOLD) deltaY = 0;
                    if (deltaZ < THRESOLD) deltaZ = 0;

                    toWrite += "_" + deltaX + "#" + deltaY + "#" + deltaZ;
                    timer = 30;
                }
            }

            Date now = new Date();
            String pattern = "dd-M-yyyy hh:mm:ss.SSS";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            if(changes) {
                toUpload += simpleDateFormat.format(now) + "_" + acc[0] + "_" + acc[1] + "_" + acc[2] + "_" + lacc[0] + "_" + lacc[1] + "_" + lacc[2] + "_" + gyr[0] + "_" + gyr[1] + "_" + gyr[2] + "_" + magn[0] + "_" + magn[1] + "_" + magn[2] + "_" + lat + "_" + lon + "\n";
            }



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }




}