package com.example.tracker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity{

    Boolean isRecording = false;
    FileOutputStream writer;
    Context context = this;
    private String TAG = MainActivity.class.getSimpleName();
    boolean stoppedBackground = false;
    boolean rec = true;

    BroadcastReceiver broadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String s1 = intent.getStringExtra("WHATTODO");
            if(s1!=null) {

                if (s1.equals("stillfigure") || s1.equals("walkingfigure") || s1.equals("bikefigure")) {
                    setFigure(s1);
                    if(s1.equals("bikefigure"))
                    {
                        isRecording = true;
                    }
                    else{
                        isRecording = false;
                    }
                }
            }
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();


        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.darkblue));
        }

        Button btnRRecords = findViewById(R.id.ridesRecord);
        btnRRecords.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                ComponentName componentName =
                        new ComponentName(getApplicationContext(), RidesRecord.class);
                i.setComponent(componentName);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(i);
            }
        });

        Button btnStats = findViewById(R.id.stats);
        btnStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                ComponentName componentName =
                        new ComponentName(getApplicationContext(), Stats.class);
                i.setComponent(componentName);
                startActivity(i);
            }
        });

        ImageView settingsBtn = findViewById(R.id.settingsBtn);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                ComponentName componentName =
                        new ComponentName(getApplicationContext(), PreferencesActivity.class);
                i.setComponent(componentName);
                startActivity(i);
            }

        });


        int permissionCheckLoc = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheckLoc != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    100);
        }

        int permissionCheckAR = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION);
        if(permissionCheckAR != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    100);
        }

        try {
            File checkFStats = new File(getFilesDir(), "stats");
            Log.e("Check", checkFStats.exists() + "");
            if (!checkFStats.exists()) {
                checkFStats.createNewFile();
                writer = openFileOutput("stats", Context.MODE_PRIVATE);
                String DEVICE_ID = UUID.randomUUID().toString();
                writer.write(("0_0_0_0_0_0_" + DEVICE_ID).getBytes());
            }

            File checkRawFolder = new File(getFilesDir(), "/raw/");
            if(!checkRawFolder.exists())
            {
                new File(getFilesDir(), "/raw/").mkdirs();
                //Files.setPosixFilePermissions(Paths.get(getFilesDir() + "/raw/"), PosixFilePermissions.fromString("rwxrwxrwx"));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if(isRecording)
        {
            setFigure("bikefigure");
        }
        else{
            setFigure("bikefigure");
        }

        Intent i = new Intent(this,MyService.class);
        startService(i);


    }


    protected void setFigure(String figure)
    {
        ImageView img = findViewById(R.id.activityIcon);
        img.setImageDrawable(getResources().getDrawable(getResources().getIdentifier("@drawable/" + figure, null, getPackageName())));
    }


    @Override
    protected void onResume() {

        if(isRecording)
        {
            setFigure("bikefigure");
        }
        else{
            setFigure("stillfigure");
        }

        if(stoppedBackground) {
            startService(new Intent(this, MyService.class));
            stoppedBackground = false;
        }
        registerReceiver(broadcastReceiver2, new IntentFilter(MyService.BROADCAST_ACTION));
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(broadcastReceiver2);
        boolean checkBackground = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("background", false);
        Log.e("Check", checkBackground + "");
        if(!checkBackground) {
            stoppedBackground = true;
            stopService(new Intent(this, MyService.class));
        }
        super.onStop();
    }

}