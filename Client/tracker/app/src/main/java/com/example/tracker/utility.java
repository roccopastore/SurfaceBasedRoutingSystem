package com.example.tracker;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class utility {
    public static void reverse(String[] nums)
    {
        for (int low = 0, high = nums.length - 1; low < high; low++, high--) {
            String temp = nums[low];
            nums[low] = nums[high];
            nums[high] = temp;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void simpleRideUpload(Context myCon, File filesDir, String fileName, String toWrite) {
        try {
            File file = new File(filesDir, "/raw/" + fileName);
            file.createNewFile();
            FileOutputStream writer = new FileOutputStream(file);
            writer.write(("Timestamp_AccelerometroX_AccelerometroY_AccelerometroZ_AccLineareX_AccLineareY_AccLineareZ_GiroscopioX_GiroscopioY_GiroscopioZ_MagnetometroX_MagnetometroY_MagnetometroZ_Latitudine_Longitudine\n").getBytes());
            writer.write((toWrite).getBytes());
            writer.close();
            uploadRawFile(file);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error", e.getMessage());
        }
    }

    public static void updateStats(Context context, double dM, double speed, int level)
    {
        try {
            FileOutputStream writer;
            File statsFile;
            statsFile = new File(context.getFilesDir(), "stats");
            String temp = "";
            String[] mesurations = new String[100];
            String totDistance = "0";
            String avgSpeed = "0";
            int countG = 0;
            int countD = 0;
            int countB = 0;
            String numOfElements = "0";

            FileInputStream inputStream = new FileInputStream(statsFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            temp = br.readLine();
            Log.e("TEMP", temp+"");
            mesurations = temp.split("_");


            numOfElements = mesurations[0];
            totDistance = mesurations[2];
            avgSpeed = mesurations[1];
            countG = Integer.parseInt(mesurations[3]);
            countD = Integer.parseInt(mesurations[4]);
            countB = Integer.parseInt(mesurations[5]);
            String DEVICE_ID = mesurations[6];

            if(level == 0)
            {
                countG += 1;
            }
            else if(level == 1)
            {
                countD += 1;
            }
            else{
                countB += 1;
            }

            int nNum = Integer.parseInt(numOfElements) + 1;
            double tD = Double.parseDouble(totDistance) + dM;
            double nAvg = Double.parseDouble(avgSpeed) + ((speed - Double.parseDouble(avgSpeed))/nNum);

            String tempAvg = ""+nAvg;

            if(tempAvg.length() > 5)
            {
                tempAvg = tempAvg.substring(0,5);
            }


            uploadOnFirebase(context, nAvg, tD, DEVICE_ID);

            writer = context.openFileOutput("stats", Context.MODE_PRIVATE);
            writer.write((nNum +"_"+ Double.parseDouble(tempAvg) + "_" + tD + "_"+ countG + "_"+ countD + "_" + countB + "_" + DEVICE_ID).getBytes());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void uploadOnFirebase(Context context, double avgSpeed, double totKms, String DEVICE_ID)
    {
        FirebaseApp.initializeApp(context);
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://tracker-f56ac-default-rtdb.firebaseio.com/");
        DatabaseReference users = database.getReference("users");
        Log.e("device id", DEVICE_ID);
        users.child(DEVICE_ID).child("avgSpeed").setValue((long)avgSpeed);
        users.child(DEVICE_ID).child("totKms").setValue((long)totKms);
    }

    public static void uploadRating(Context context, String[] mesurations, float v)
    {
        FirebaseApp.initializeApp(context);
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://tracker-f56ac-default-rtdb.firebaseio.com/");
        DatabaseReference users = database.getReference("ratings");
        users.child(mesurations[0]).child("start").setValue(mesurations[2] + ", " + mesurations[3]);
        users.child(mesurations[0]).child("stop").setValue(mesurations[4] + ", " + mesurations[5]);
        users.child(mesurations[0]).child("rating").setValue(v);
    }

    public static void uploadRide(Context context, String ride, double startLat, double startLong, double stopLat, double stopLong, String toUp)
    {
        FirebaseApp.initializeApp(context);
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://tracker-f56ac-default-rtdb.firebaseio.com/");
        DatabaseReference users = database.getReference("rides");
        users.child(ride).child("start").setValue(startLat +","+startLong);
        users.child(ride).child("stop").setValue(stopLat+","+stopLong);
        users.child(ride).child("data").setValue(toUp);
    }


    public static void readFromFirebase(Context context, myCallback myCallback)
    {

        List<Long> totKmsV = new ArrayList<>();
        List<Long> avgSpeedV = new ArrayList<>();

        FirebaseApp.initializeApp(context);
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://tracker-f56ac-default-rtdb.firebaseio.com/");
        DatabaseReference users = database.getReference("users");


        users.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    HashMap<String, Object> dataMap = (HashMap<String, Object>) task.getResult().getValue();
                    if (dataMap != null) {
                        for (String key : dataMap.keySet()) {
                            Object data = dataMap.get(key);
                            HashMap<Long, Long> userData = (HashMap<Long, Long>) data;
                            avgSpeedV.add(userData.get("avgSpeed"));
                            totKmsV.add(userData.get("totKms"));
                        }
                        myCallback.onCallback(avgSpeedV, totKmsV);
                    }
                }
                }
        });
    }

    public static void uploadRawFile(File filetoup)
    {
        Uri file = Uri.fromFile(filetoup);
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://tracker-f56ac.appspot.com/");
        StorageReference storageRef = storage.getReference();
        StorageReference fileRef = storageRef.child("/Rides/" + filetoup.getName());
        fileRef.putFile(file);
    }

    public static ArrayList<File> filterRide(String[] files, String filterDay, File fileDir)
    {
        ArrayList<File> listOfFiles = new ArrayList<File>();
        for(int i=0; i<files.length; i++)
        {
            if(files[i].contains(filterDay))
            {
                File tempFile = new File(fileDir, files[i]);
                listOfFiles.add(tempFile);
            }
        }
        return listOfFiles;
    }

    public static File searchRide(String[] files, String filename, File fileDir)
    {
        for(int i=0; i<files.length; i++)
        {
            if(files[i].equalsIgnoreCase(filename))
            {
                return new File(fileDir, files[i]);
            }
        }

        return null;
    }

}
