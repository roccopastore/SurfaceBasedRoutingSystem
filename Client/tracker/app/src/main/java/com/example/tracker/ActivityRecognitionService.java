package com.example.tracker;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ActivityRecognitionService extends Service {
    private static final String TAG = ActivityRecognitionService.class.getSimpleName();

    private Intent intent;
    private PendingIntent pendingIntent;
    IBinder iBinder = new ActivityRecognitionService.LocalBinder();
    private ActivityRecognitionClient activityRecognitionClient;


    public class LocalBinder extends Binder {
        public ActivityRecognitionService getServerInstance() {
            return ActivityRecognitionService.this;
        }
    }

    public ActivityRecognitionService() {

    }

    @Override
    public void onCreate() {
        activityRecognitionClient = new ActivityRecognitionClient(this);
        intent = new Intent(this, ActivityHandlerIntentService.class);
        pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_MUTABLE);
        activityUpdatesHandler();
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public void activityUpdatesHandler() {
        Task<Void> task = activityRecognitionClient.requestActivityUpdates(
                0,
                pendingIntent);

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeActivityUpdatesButtonHandler();
    }


    public void removeActivityUpdatesButtonHandler() {
        Task<Void> task = activityRecognitionClient.removeActivityUpdates(
                pendingIntent);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {

            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to remove activity updates!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


}