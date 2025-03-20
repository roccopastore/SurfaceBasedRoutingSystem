package com.example.tracker;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Stats extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Stats");
        actionBar.setBackgroundDrawable(new ColorDrawable(this.getResources().getColor(R.color.lightblue)));

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.darkblue));
        }

        utility ut = new utility();
        Context myCon = getApplicationContext();
        ut.readFromFirebase(myCon, new myCallback() {
            @Override
            public void onCallback(List avgSpeed, List totKms) {
                try {
                    File file = new File(getFilesDir(), "stats");
                    FileInputStream inputStream = new FileInputStream(file);
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    String temp = br.readLine();
                    if(temp!=null) {
                        String[] mesurations = temp.split("_");
                        TextView km = findViewById(R.id.totKms);

                        String kmS = "" + ((Double.parseDouble(mesurations[2])) / 1000);
                        if (kmS.length() > 5) {
                            kmS = kmS.substring(0, 5);
                        } else {
                            kmS = kmS;
                        }
                        km.setText("" + kmS);

                        TextView as = findViewById(R.id.avgSpeed);
                        String asT = "";
                        if (mesurations[1].length() > 5) {
                            asT = mesurations[1].substring(0, 5);
                        } else {
                            asT = mesurations[1];
                        }

                        int betterCountAVG = 0;
                        int betterCountKTMS = 0;
                        double asTD = Double.parseDouble(mesurations[1]);

                        for (int i = 0; i < avgSpeed.size(); i++) {
                            if (asTD > Double.parseDouble("" + avgSpeed.get(i))) {
                                betterCountAVG++;
                            }

                            if (Double.parseDouble(kmS) > Double.parseDouble("" + totKms.get(i))) {
                                betterCountKTMS++;
                            }
                        }


                        double tempSpeed = Double.parseDouble(asT) * 3.6;
                        String tempSpeedString = tempSpeed + "";
                        if (tempSpeedString.length() > 3) {
                            tempSpeedString = tempSpeedString.substring(0, 3);
                        }
                        as.setText(tempSpeedString + " km/h");

                        ProgressBar pb1 = findViewById(R.id.goodBar);
                        ProgressBar pb2 = findViewById(R.id.discreteBar);
                        ProgressBar pb3 = findViewById(R.id.badBar);

                        int iPb1 = Integer.parseInt(mesurations[3]);
                        int iPb2 = Integer.parseInt(mesurations[4]);
                        int iPb3 = Integer.parseInt(mesurations[5]);

                        double perc1 = ((double) iPb1 / (double) (iPb1 + iPb2 + iPb3)) * 100;
                        double perc2 = ((double) iPb2 / (double) (iPb1 + iPb2 + iPb3)) * 100;
                        double perc3 = ((double) iPb3 / (double) (iPb1 + iPb2 + iPb3)) * 100;

                        pb1.setProgress((int) perc1);
                        pb2.setProgress((int) perc2);
                        pb3.setProgress((int) perc3);

                        TextView btAVG = findViewById(R.id.betterText2);
                        TextView btKTMS = findViewById(R.id.betterText1);

                        double betterCalcAVG = ((double) betterCountAVG / (double) avgSpeed.size()) * 100;
                        double betterCalcKTMS = ((double) betterCountKTMS / (double) totKms.size()) * 100;

                        String inStringAVG = "" + betterCalcAVG;
                        String inStringKTMS = "" + betterCalcKTMS;

                        if (inStringKTMS.length() > 5) {
                            inStringKTMS = inStringKTMS.substring(0, 5);
                        }
                        if (inStringAVG.length() > 5) {
                            inStringAVG = inStringAVG.substring(0, 5);
                        }

                        btAVG.setText("> than " + inStringAVG + "% of users");
                        btKTMS.setText("> than " + inStringKTMS + "% of users");
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }
}