package com.example.tracker;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class DataAnalysis {

    utility ut = new utility();

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void analysis(Context myCon, String fileX, String fileName, File filesDir, double km, double speed) {


        String[] mesurations = fileX.split("_");

        int k = 3; //numero di cluster che vogliamo

        int [] countForLevel = new int[k];
        countForLevel[0] = 0;
        countForLevel[1] = 0;
        countForLevel[2] = 0;

        int generalCount = 1;

        //in mesurations, 0 e 1 saranno altitudine e longitudine iniziale, mentre lenght-1 e lenght-2 saranno altitudine e longitudine finale, e lenght-3 sarà la velocità
        for(int i = 3; i < mesurations.length-3; i++) {
            String[] values = mesurations[i].split("#"); //dividendo per # ottengo 3 valori, deltax, deltay e deltaz
            //values[0] = deltax values[1]=deltay values[2]=deltaz

            double doubleValue = Double.parseDouble(values[2]);

            if (doubleValue > 5) {
                generalCount++;
                if(doubleValue < 12)
                {
                    countForLevel[0] ++;
                }
                else if(doubleValue >= 12 && doubleValue < 25)
                {
                    countForLevel[1] ++;
                }
                else{
                    countForLevel[2] ++;
                }
            }
        }


        double[] percentages = new double[k];



        for(int i=0; i<k; i++)
        {
            percentages[i] = countForLevel[i] * 100 / generalCount;
        }


        int level = 0;

        if(percentages[0] >= 80.0)
        {
            level = 0;
        }

        if(percentages[1] >= 30.0)
        {
            level = 1;
        }

        if(percentages[2] >= 10.0)
        {
            level = 2;
        }

        ut.updateStats(myCon, km, speed, level);

        try {
            File file = new File(filesDir, fileName);
            file.createNewFile();
            FileOutputStream writer = myCon.openFileOutput(fileName, Context.MODE_PRIVATE);
            writer.write((mesurations[1] + "_"  + mesurations[2]  + "_" + mesurations[mesurations.length-2] + "_" + mesurations[mesurations.length-1] + "_" + percentages[0] + "_" + percentages[1] + "_" + percentages[2] + "_" + level + "_" + mesurations[mesurations.length-3] + "_not").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    }


