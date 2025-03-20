package com.example.ennesimamap;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.geojson.Point;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class navigationService extends IntentService {

    String geometry = "";
    String pathTime = "";
    int multipleTimePath = 0;
    double multipleDistancePath = 0.0;

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.example.ennesimamap.action.FOO";
    private static final String ACTION_BAZ = "com.example.ennesimamap.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.example.ennesimamap.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.example.ennesimamap.extra.PARAM2";

    public navigationService() {
        super("navigationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // this method is called in background thread
    }

    public int onStartCommand (Intent intent, int flags, int startId) {
        String start = intent.getStringExtra("start");
        String stop = intent.getStringExtra("stop");
        String alg = intent.getStringExtra("algorithm");


        req2server(start,stop, alg);
        return START_STICKY;
    }

    public void req2server(String start, String stop, String alg)
    {
        boolean[] wait4response = {false};
        String[] responseString = {""};
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://192.168.1.4:4000/route?start="+start+"&stop="+stop+"&algorithm="+alg)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("error", e.getMessage());
                wait4response[0] = true;
            }

            @Override
            public void onResponse(okhttp3.Call call, final okhttp3.Response response) {
                if (response.isSuccessful()) {
                    try {
                        responseString[0] = response.body().string();
                        onResponseAction(responseString[0]);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }

    public void onResponseAction(String toSearch){
        List<Point> allCoords = new ArrayList<>();

        String[] singlePoints = toSearch.split(";");
        for(int i=0; i<singlePoints.length; i++)
        {
            allCoords.add(Point.fromLngLat(Double.parseDouble(singlePoints[i].split(",")[0]), Double.parseDouble(singlePoints[i].split(",")[1])));
        }

        if(allCoords.size() > 25)
        {
            handleMultipleCall(allCoords.size(), toSearch);
        } else {


            RouteOptions ro = RouteOptions.builder()
                    .profile("cycling")
                    .coordinates(toSearch.substring(0, toSearch.length() - 2))
                    .steps(true)
                    .build();


            MapboxDirections cl = MapboxDirections.builder()
                    .routeOptions(ro)
                    .accessToken(getString(R.string.mapbox_access_token))
                    .build();


            cl.enqueueCall(new Callback<DirectionsResponse>() {
                @Override
                public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

                    geometry = response.body().routes().get(0).geometry();
                    pathTime = response.body().routes().get(0).duration() + "";
                    showOnMap();
                }

                @Override
                public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                    Log.e("Error", t.getMessage());
                }
            });
        }
    }

    public void handleMultipleCall(int dim, String coords){

        ArrayList<String> routes = new ArrayList<>();
        String newcoords = "";
        String[] singleCoords = coords.split(";");
        double dividerDouble = dim/24.00;
        int iterationsExpected = 0;
        if(dividerDouble > (int)dividerDouble)
        {
            iterationsExpected = (int) dividerDouble + 1;
        }
        else{
            iterationsExpected = (int) dividerDouble;
        }
        int index = 0;
        int noeditedDim = dim;
        multipleTimePath = 0;
        multipleDistancePath = 0;


        while(index < noeditedDim){
            int limit = 0;
            newcoords = "";
            if(dim <= 24)
            {
                limit = dim;
            }
            else{
                limit = (int) (noeditedDim/dividerDouble);
            }
            if(index > 0)
            {
                newcoords+=singleCoords[index-1] + ";";
            }
            for(int i=0; i<limit; i++)
            {
                newcoords+=singleCoords[index] + ";";
                index++;
            }

            dim -= limit;

            RouteOptions ro = RouteOptions.builder()
                    .profile("cycling")
                    .coordinates(newcoords.substring(0, newcoords.length() - 2))
                    .steps(true)
                    .build();


            MapboxDirections cl = MapboxDirections.builder()
                    .routeOptions(ro)
                    .accessToken(getString(R.string.mapbox_access_token))
                    .build();


            int finalindex = index;

            int finalIterationsExpected = iterationsExpected;
            cl.enqueueCall(new Callback<DirectionsResponse>() {
                @Override
                public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {


                    routes.add(response.body().routes().get(0).geometry());
                    multipleTimePath += response.body().routes().get(0).duration();
                    multipleDistancePath += response.body().routes().get(0).distance();
                    if(routes.size() == finalIterationsExpected)
                    {
                        Log.e("Distanza : ", multipleDistancePath + "");
                        showMultipleRouteOnMap(routes);
                    }
                }

                @Override
                public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                    Log.e("errore", t.getMessage());
                }

            });

        }


    }


    public void showOnMap(){
        Intent intent = new Intent("geometryUpdate");
        intent.putExtra("Geometry", geometry);
        intent.putExtra("Time", pathTime);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void showMultipleRouteOnMap(ArrayList<String> routes){
        Intent intent = new Intent("geometryUpdate");
        intent.putExtra("Time", multipleTimePath + "");
        intent.putStringArrayListExtra("Geometries", routes);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}