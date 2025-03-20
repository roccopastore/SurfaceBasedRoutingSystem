package com.example.tracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tracker.databinding.FragmentRideInfoListDialogItemBinding;
import com.example.tracker.databinding.FragmentRideInfoListDialogBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class rideInfo extends BottomSheetDialogFragment {

    // TODO: Customize parameter argument names
    private static final String ARG_ITEM_COUNT = "item_count";
    private FragmentRideInfoListDialogBinding binding;

    // TODO: Customize parameters
    public static rideInfo newInstance(int itemCount) {
        final rideInfo fragment = new rideInfo();
        final Bundle args = new Bundle();
        args.putInt(ARG_ITEM_COUNT, itemCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentRideInfoListDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        String sensorData =  getArguments().getString("data");
        Log.e("Riga", sensorData);
        String [] mesurations = sensorData.split("_");
        utility ut = new utility();
        //mesurations avrà dimensione numero_misurazioni + 2, perché i 2 finali sono i dati analizzati

        String fileName = mesurations[0]+"_"+mesurations[1] + "_" + mesurations[2] + "_" + mesurations[3];
        File filesDir = view.getContext().getFilesDir();

        for(int x = 0; x < mesurations.length; x++)
        {
            Log.e("mesurations[" + x + "]", mesurations[x] + "");
        }

        String latStart, longStart;
        String latStop, longStop;

        if(mesurations[4].length() > 7)
        {
            latStart = mesurations[4].substring(0,7);
        }
        else {
            latStart = mesurations[4];
        }
        if(mesurations[5].length() > 7)
        {
            longStart = mesurations[5].substring(0,7);
        }
        else {
            longStart = mesurations[5];
        }

        if(mesurations[6].length() > 7)
        {
            latStop = mesurations[6].substring(0,7);
        }
        else {
            latStop = mesurations[6];
        }

        if(mesurations[7].length() > 7)
        {
            longStop = mesurations[7].substring(0,7);
        }
        else {
            longStop = mesurations[7];
        }

        String level1 = mesurations[mesurations.length-5];
        String level2 = mesurations[mesurations.length-4];
        String speed = mesurations[mesurations.length-2];


        TextView start = view.findViewById(R.id.startText);
        TextView stop = view.findViewById(R.id.stopText);
        TextView spd = view.findViewById(R.id.speedText);
        TextView asp1 = view.findViewById(R.id.asp1Text);
        TextView asp2 = view.findViewById(R.id.asp2Text);

        RatingBar rb = getView().findViewById(R.id.ratingBar);

        start.setText(latStart + ", " + longStart);
        stop.setText(latStop + ", " + longStop);

        double tempSpeed = Double.parseDouble(speed) * 3.6;
        String tempSpeedString = tempSpeed+"";
        if(tempSpeedString.length() > 5)
        {
            tempSpeedString = tempSpeedString.substring(0,5);
        }

        spd.setText(tempSpeedString + " km/h");
        asp1.setText(level1 + "%");
        asp2.setText(level2 + "%");


        if(!mesurations[mesurations.length-1].equals("not"))
        {
            rb.setIsIndicator(true);
            rb.setRating(Float.parseFloat(mesurations[mesurations.length-1]));
        }

        rb.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                Toast.makeText(view.getContext(), "Saving evaluation", Toast.LENGTH_LONG).show();
                if(!ratingBar.isIndicator()) {
                    ratingBar.setIsIndicator(true);
                    String toWrite = "";
                    for(int i=2; i<mesurations.length-1; i++)
                    {
                        toWrite += mesurations[i] + "_";
                    }
                    toWrite += v;
                    ratingBar.setRating(v);
                    try {
                        Log.e("filename", fileName);
                        FileOutputStream writer = view.getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
                        writer.write((toWrite).getBytes());
                        ut.uploadRating(view.getContext(), mesurations, v);
                        TimeUnit.SECONDS.sleep(3);
                        Intent intent = new Intent(view.getContext(), RidesRecord.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }



    private class ViewHolder extends RecyclerView.ViewHolder {

        final TextView text;

        ViewHolder(FragmentRideInfoListDialogItemBinding binding) {
            super(binding.getRoot());
            text = binding.text;
        }

    }

    private class infoAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final int mItemCount;

        infoAdapter(int itemCount) {
            mItemCount = itemCount;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            return new ViewHolder(FragmentRideInfoListDialogItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.text.setText(String.valueOf(position));
        }

        @Override
        public int getItemCount() {
            return mItemCount;
        }

    }
}