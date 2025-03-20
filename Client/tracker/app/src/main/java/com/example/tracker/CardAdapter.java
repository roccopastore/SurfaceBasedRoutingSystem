package com.example.tracker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder>{

    public static Context c;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleView;
        public ImageView pathView;
        public CardView CV;
        public ImageButton buttonView;
        public TextView dV1;
        public TextView dV2;

        public ViewHolder(View v) {
            super(v);
            titleView = v.findViewById(R.id.fileName);
            pathView = v.findViewById(R.id.pathView);
            CV = v.findViewById(R.id.cardPath);
            buttonView = v.findViewById(R.id.imageButton);
            dV1 = v.findViewById(R.id.detailsView);
            dV2 = v.findViewById(R.id.detailsView2);
        }
    }

    private ArrayList<Card> mDataset;

    public CardAdapter(ArrayList<Card> myDataset, Context context) {
        mDataset = myDataset;
        this.c = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        String pathLevel = mDataset.get(position).getPath();


        holder.pathView.setImageDrawable(c.getDrawable(c.getResources().getIdentifier(pathLevel, "drawable", c.getPackageName())));
        holder.titleView.setText(mDataset.get(position).getTitle());
        holder.dV1.setText(mDataset.get(position).getDv1());
        holder.dV2.setText(mDataset.get(position).getDv2());
        holder.buttonView.setOnClickListener(v -> {

            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            Bundle bundle = new Bundle();
            String sensor = mDataset.get(position).getSensor();
            String fileName = mDataset.get(position).getTitle();
            bundle.putString("data", fileName + "_" + sensor);
            rideInfo rInfo = new rideInfo();
            rInfo.setArguments(bundle);
            rInfo.show(activity.getSupportFragmentManager(), "dialog");
        });
    }
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
