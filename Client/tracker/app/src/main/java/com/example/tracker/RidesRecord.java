package com.example.tracker;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

public class RidesRecord extends AppCompatActivity {

    ArrayList<Card> listOfCards;

    RecyclerView recyclerView;
    utility ut = new utility();
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rides_record);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Rides archive");
        actionBar.setBackgroundDrawable(new ColorDrawable(this.getResources().getColor(R.color.lightblue)));

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.darkblue));
        }

        recyclerView = findViewById(R.id.list);

        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        listOfCards = new ArrayList<Card>();

        String[] files = fileList();
        ut.reverse(files);
        for (int i=0; i<files.length; i++) {
            File file = new File(getFilesDir(), files[i]);
            if(file.isDirectory())
            {
                try {
                    File[] rawfiles = file.listFiles();
                    for (int rf = 0; rf < rawfiles.length; rf++) {
                        FileInputStream inputStream = new FileInputStream(rawfiles[rf]);
                        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                        String temp = br.readLine();
                        int n = 1;
                        while (temp != null) {
                            Log.e("Line " + n, temp);
                            temp = br.readLine();
                            n++;
                        }

                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            if (!file.getName().equals("stats") && !file.isDirectory()) {
                DecimalFormat df = new DecimalFormat("###.##");
                String line = "";
                String speed = "";
                String aspLevel = "";
                try {
                    FileInputStream inputStream = new FileInputStream(file);
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    line = br.readLine();
                    String[] elements = line.split("_");
                    speed = elements[elements.length - 2];
                    aspLevel = elements[elements.length - 3];

                } catch (IOException e) {
                    e.printStackTrace();
                }

                String toWrite1 = " ";
                String toWrite2 = "";
                String uri = "";

                if(Double.isNaN(Double.parseDouble(speed)))
                {
                    speed = "0.0";
                }
                double tempSpeed = Double.parseDouble(speed) * 3.6;
                String tempSpeedString = tempSpeed+"";
                if(tempSpeedString.length() > 5)
                {
                    tempSpeedString = tempSpeedString.substring(0,5);
                }

                if (tempSpeed >= 7.0) {
                    toWrite1 = "Road that can be traveled smoothly, with an average speed of " + tempSpeedString + " km/h";
                } else {
                    toWrite1 = "Road that can be traveled below average speed : " + tempSpeedString + " km/h";
                }

                if (Integer.parseInt(aspLevel) == 0) {
                    toWrite2 = "Optimal";
                    uri = "circlegreen";
                } else if (Integer.parseInt(aspLevel) == 1) {
                    toWrite2 = "Decent";
                    uri = "circleyellow";
                } else {
                    toWrite2 = "Very bad";
                    uri = "circlered";
                }


                listOfCards.add(new Card(file.getName(), uri, toWrite1, "Road conditions : " + toWrite2, line));
            }
            }
            mAdapter = new CardAdapter(listOfCards, getApplicationContext());
            recyclerView.setAdapter(mAdapter);
    }

}