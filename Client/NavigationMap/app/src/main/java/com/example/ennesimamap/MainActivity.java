package com.example.ennesimamap;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;


import com.android.volley.toolbox.JsonRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.DirectionsCriteria;

import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.LegStep;
import com.mapbox.api.directions.v5.models.RouteLeg;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.api.matching.v5.MapboxMapMatching;
import com.mapbox.api.matching.v5.models.MapMatchingResponse;
import com.mapbox.api.optimization.v1.MapboxOptimization;
import com.mapbox.api.optimization.v1.models.OptimizationResponse;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Circle;
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions;
import com.mapbox.mapboxsdk.plugins.annotation.Line;
import com.mapbox.mapboxsdk.plugins.annotation.LineManager;
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager;
import com.mapbox.navigation.base.route.NavigationRoute;
import com.mapbox.navigation.base.route.NavigationRouterCallback;
import com.mapbox.navigation.base.route.RouterFailure;
import com.mapbox.navigation.base.route.RouterOrigin;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    MapboxMap mpM = null;
    private MapView mapView;
    double originPointLon = 0;
    double originPointLat = 0;
    double destinationPointLon = 0;
    double destinationPointLat = 0;
    private MapboxDirections client;
    private DirectionsRoute route;
    private static final String ROUTE_LAYER_ID = "route-layer-id";
    private static final String ROUTE_SOURCE_ID = "route-source-id";
    private static final String ICON_LAYER_ID = "icon-layer-id";
    private static final String ICON_SOURCE_ID = "icon-source-id";
    private static final String RED_PIN_ICON_ID = "red-pin-icon-id";
    List<Circle> points = new ArrayList<>();
    List<LatLng> pointsOfRoute = new ArrayList<>();
    List<Line> lines = new ArrayList<>();

    Geocoder g;
    //utility ut = new utility();
    List<String> badpoints = new ArrayList<>();
    List<String> goodpoints = new ArrayList<>();
    List<String> verybadpoints = new ArrayList<>();
    String geometry = "";
    Double multipleTimePath = 0.0;
    String pathTime = "";
    BarFragment bf = new BarFragment();
    int preferenceValue;
    List<String> listofcoords = new ArrayList<>();

    public class Node {

        private String id;
        private String coords;

        private List<Node> shortestPath = new LinkedList<>();

        private Integer distance = Integer.MAX_VALUE;

        Map<Node, Integer> adjacentNodes = new HashMap<>();

        public void addDestination(Node destination, int distance) {
            adjacentNodes.put(destination, distance);
        }

        public Node(String id, String coords) {
            this.id = id;
            this.coords = coords;
        }


        public String getCoords() {
            return this.coords;
        }

        public Map<Node, Integer> getAdjacentNodes() {
            return this.adjacentNodes;
        }

        public void setDistance(int val) {
            this.distance = val;
        }

        public int getDistance() {
            return this.distance;
        }

        public void setShortestPath(List<Node> sp) {
            this.shortestPath = sp;
        }

        public List<Node> getShortestPath() {
            return this.shortestPath;
        }

        public String getId(){
            return this.id;
        }

    }

    public class Graph {

        private Set<Node> nodes = new HashSet<>();

        public void addNode(Node nodeA) {
            nodes.add(nodeA);
        }

        public Set<Node> getNodes() {
            return this.nodes;
        }

    }

    List<Node> listOfNodes;
    Graph graph;

    EditText start;
    EditText stop;

    Chip time;
    CircleManager circleManager;
    LineManager lineManager;


    Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.darkblue));
        }

        preferenceValue = 50;

        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("geometryUpdate"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("geometryMultipleUpdate"));

        g = new Geocoder(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        FloatingActionButton deleteButton = findViewById(R.id.deletebtn);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                resetAll();
            }
        });



        FloatingActionButton selectionButton = findViewById(R.id.selectionbtn);
        selectionButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                /*Intent i = new Intent();
                ComponentName componentName =
                        new ComponentName(getApplicationContext(), PreferencesActivity.class);
                i.setComponent(componentName);
                startActivity(i);*/
                try {

                    File file = new File(getFilesDir(), "/points.csv");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileWriter fw = new FileWriter(file);
                    for (int i = 0; i < listofcoords.size(); i++) {
                        fw.append(listofcoords.get(i) + "\n");
                    }
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final SeekBar seek = new SeekBar(context);
                seek.setMax(100);
                seek.setKeyProgressIncrement(1);
                seek.setProgress(preferenceValue);
                seek.getProgressDrawable().setColorFilter(Color.argb(100,30,144,255), PorterDuff.Mode.SRC_IN);
                seek.getThumb().setColorFilter(Color.argb(100,30,144,255), PorterDuff.Mode.SRC_IN);
                new AlertDialog.Builder(context)
                        .setTitle("Select your rout calculation preference")
                        .setView(seek)
                        .show();
                seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        preferenceValue = i;
                        Log.e("preferencevalue ", preferenceValue + "");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
            }
        });



        Button settingsBtn = findViewById(R.id.settingsbtn);
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

        Button calculateBtn = findViewById(R.id.routebtn);
        calculateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start = findViewById(R.id.startText);
                stop = findViewById(R.id.stopText);
                String startString = start.getText().toString() + " bologna";
                String stopString = stop.getText().toString() + " bologna";
                if(!startString.equals("") && !stopString.equals(""))
                {
                    try {
                        List<Address> addressesO = g.getFromLocationName(startString, 2);
                        List<Address> addresses1 = g.getFromLocationName(stopString, 2);
                        originPointLat = addressesO.get(0).getLatitude();
                        originPointLon = addressesO.get(0).getLongitude();

                        destinationPointLat = addresses1.get(0).getLatitude();
                        destinationPointLon = addresses1.get(0).getLongitude();
                        if(pointsOfRoute.size() == 0)
                        {
                            pointsOfRoute.add(new LatLng(originPointLat, originPointLon));
                            pointsOfRoute.add(new LatLng(destinationPointLat, destinationPointLon));
                        }
                        if(lineManager != null)
                        {
                            for(int i=0; i<lines.size(); i++)
                            {
                                lineManager.delete(lines.get(i));
                            }
                            lines = new ArrayList<>();
                        }
                        startRoute(mpM);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Log.e("Error", "Something is missing");
                }
            }
        });



        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mpM = mapboxMap;
                mapboxMap.setStyle(Style.DARK, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        CameraPosition position = new CameraPosition.Builder()
                                .target(new LatLng(44.494366, 11.344623))
                                .zoom(13)
                                .tilt(20)
                                .build();

                        circleManager = new CircleManager(mapView, mpM, style);
                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 10);
                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                        mpM.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
                            @Override
                            public boolean onMapClick(@NonNull LatLng point) {

                                if(points.size() == 2)
                                {
                                    resetAll();
                                }
                                CircleOptions circleOptions = new CircleOptions();
                                circleOptions.withLatLng(point);
                                circleOptions.withCircleColor("#0582ca");
                                Circle c = circleManager.create(circleOptions);
                                listofcoords.add(point.getLatitude() + "," + point.getLongitude());
                                try {
                                    start = findViewById(R.id.startText);
                                    stop = findViewById(R.id.stopText);
                                    List<Address> loc = g.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
                                    if(start.getText().toString().equals(""))
                                    {
                                        start.setText(loc.get(0).getAddressLine(0));
                                    }
                                    else{
                                        stop.setText(loc.get(0).getAddressLine(0));
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                points.add(c);
                                pointsOfRoute.add(point);
                                return false;
                            }
                        });

                        initSource(style);
                        initLayers(style);

                    }
                });

            }
        });

    }

    public void resetAll(){

        FloatingActionButton closebutton = findViewById(R.id.deletebtn);
        closebutton.setVisibility(View.INVISIBLE);
        start.setText("");
        stop.setText("");
        time.setVisibility(View.INVISIBLE);
        if(circleManager != null) {
            circleManager.delete(points);
            points = new ArrayList<>();
        }
        if(lineManager != null)
        {

            for(int i=0; i<lines.size(); i++)
            {
                lineManager.delete(lines.get(i));
            }
            lines = new ArrayList<>();
        }
        pointsOfRoute = new ArrayList<>();
    }

    /*public void prepareDataFromFile(){
        listOfNodes = new ArrayList<>();
        try {
            File datafile = new File(getFilesDir() + "/nodesMemory");
            FileReader fr = new FileReader(datafile);
            BufferedReader br = new BufferedReader(fr);

            String temp = br.readLine();
            while (temp != null) {
                String[] parts = temp.split(",");
                Node node = new Node(parts[0], parts[1] + "," + parts[2]);
                listOfNodes.add(node);
                temp = br.readLine();
            }

            fr = new FileReader(datafile);
            br = new BufferedReader(fr);

            temp = br.readLine();
            while(temp != null){
                String[] parts = temp.split(",");
                if(parts.length > 3)
                {
                    Node nodoSource = listOfNodes.stream().filter(s -> {return s.getId().equals(parts[0]);}).findAny().orElse(null);
                    listOfNodes.remove(nodoSource);
                    for(int i=3; i<parts.length; i+=2)
                    {
                        String nododestid = parts[i];
                        Node nodoDest = listOfNodes.stream().filter(s -> {return s.getId().equals(nododestid);}).findAny().orElse(null);
                        nodoSource.addDestination(nodoDest, Integer.parseInt(parts[i+1]));
                    }
                    listOfNodes.add(nodoSource);
                }
                temp = br.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        graph = new Graph();

        for(int i=0; i<listOfNodes.size();i++)
        {
            graph.addNode(listOfNodes.get(i));
        }



    }*/


    private void initSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(ROUTE_SOURCE_ID));

        GeoJsonSource iconGeoJsonSource = new GeoJsonSource(ICON_SOURCE_ID, FeatureCollection.fromFeatures(new Feature[] {
                Feature.fromGeometry(Point.fromLngLat(originPointLon, originPointLat)),
                Feature.fromGeometry(Point.fromLngLat(destinationPointLon, destinationPointLat))}));
        loadedMapStyle.addSource(iconGeoJsonSource);
    }

    private void initLayers(@NonNull Style loadedMapStyle) {
        LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID);

// Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(5f),
                lineColor(Color.parseColor("#e6a91d"))
        );
        loadedMapStyle.addLayer(routeLayer);

// Add the red marker icon image to the map
        loadedMapStyle.addImage(RED_PIN_ICON_ID, BitmapUtils.getBitmapFromDrawable(
                getResources().getDrawable(R.drawable.ic_launcher_background)));

// Add the red marker icon SymbolLayer to the map
        loadedMapStyle.addLayer(new SymbolLayer(ICON_LAYER_ID, ICON_SOURCE_ID).withProperties(
                iconImage(RED_PIN_ICON_ID),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconOffset(new Float[] {0f, -9f})));
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String geometryBR = intent.getStringExtra("Geometry");
            ArrayList<String> geometriesBR = intent.getStringArrayListExtra("Geometries");
            String timeBR = intent.getStringExtra("Time");


            if(geometryBR == null && geometriesBR != null)
            {
                multipleTimePath = Double.parseDouble(timeBR);
                showMultipleRouteOnMap(geometriesBR);
            }

            else if(geometriesBR == null && geometryBR != null)
            {
                geometry = geometryBR;
                pathTime = timeBR;
                showOnMap();
            }

            else{
                Log.e("Error", "Something wrong");
            }
        }
    };


    public void startRoute(MapboxMap mapboxMap) throws IOException {

        start = findViewById(R.id.startText);
        stop = findViewById(R.id.stopText);



        final String[] responseString = {""};
        final boolean[] wait4response = {false};

        Intent serviceIntent = new Intent(this, navigationService.class);
        serviceIntent.putExtra("start", pointsOfRoute.get(0).getLatitude() + "," + pointsOfRoute.get(0).getLongitude());
        serviceIntent.putExtra("stop", pointsOfRoute.get(1).getLatitude() + "," + pointsOfRoute.get(1).getLongitude());


        serviceIntent.putExtra("algorithm", (double)(preferenceValue/100.00) + "");
        this.startService(serviceIntent);

    }

    public void handleMultipleCall(int dim, String coords){

        String newcoords = "";
        String[] singleCoords = coords.split(";");
        Log.e("dim", dim + "");
        double dividerDouble = dim/24.00;
        int iterationsExpected = 0;
        Log.e("divider", dividerDouble + "");
        if(dividerDouble > (int)dividerDouble)
        {
            Log.e("Maggiore", "si");
            iterationsExpected = (int) dividerDouble + 1;
        }
        else{
            iterationsExpected = (int) dividerDouble;
        }
        int index = 0;
        int noeditedDim = dim;
        List<String> routes = new ArrayList<>();
        multipleTimePath = 0.0;


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
            Log.e("limit", limit + "");
            if(index > 0)
            {
                newcoords+=singleCoords[index-1] + ";";
            }
            for(int i=0; i<limit; i++)
            {
                newcoords+=singleCoords[index] + ";";
                index++;
            }
            Log.e("index", index +"");

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

                    Log.e("risposta", response.message());

                    routes.add(response.body().routes().get(0).geometry());
                    multipleTimePath += response.body().routes().get(0).duration();
                    Log.e("finalindex",finalindex+"");
                    if(routes.size() == finalIterationsExpected)
                    {
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


    /*public Node getclosestPoint(String coords)
    {
        Double lat1 = Double.parseDouble(coords.split(",")[0]);
        Double lon1 = Double.parseDouble(coords.split(",")[1]);

        Double min = Double.MAX_VALUE;
        Node returnNode = null;

        for(int i=0; i<listOfNodes.size();i++)
        {
            if(listOfNodes.get(i).getAdjacentNodes().size() > 0) {
                Double lat2 = Double.parseDouble(listOfNodes.get(i).getCoords().split(",")[0]);
                Double lon2 = Double.parseDouble(listOfNodes.get(i).getCoords().split(",")[1]);
                Double distance = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)) * 6371;
                if (distance < min) {
                    min = distance;
                    returnNode = listOfNodes.get(i);
                }
            }
        }

        return returnNode;


    }*/

    public List<Node> getclosestPointwPath(String coords, List<Node> calculatedNodes)
    {

        Double lat1 = Double.parseDouble(coords.split(",")[0]);
        Double lon1 = Double.parseDouble(coords.split(",")[1]);

        Double min = Double.MAX_VALUE;
        List<Node> returnPath = null;

        for(int i=0; i<calculatedNodes.size(); i++)
        {

            if(calculatedNodes.get(i).getShortestPath().size() > 0)
            {
                Log.e("path size", calculatedNodes.get(i).getShortestPath().size() + "");
                Double lat2 = Double.parseDouble(calculatedNodes.get(i).getCoords().split(",")[0]);
                Double lon2 = Double.parseDouble(calculatedNodes.get(i).getCoords().split(",")[1]);
                Double distance = Math.acos(Math.sin(lat1)*Math.sin(lat2)+Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1))*6371;
                if(distance < min)
                {
                    min = distance;
                    returnPath = calculatedNodes.get(i).getShortestPath();
                }
            }

        }
        return returnPath;
    }

    public void showOnMap(){

        FloatingActionButton closebutton = findViewById(R.id.deletebtn);
        closebutton.setVisibility(View.VISIBLE);
        Log.e("geometry", geometry);
            if (mpM != null) {

                //Toast.makeText(context, "Tempo di percorrenza : " + ((float)((int)((route.duration()/60)*100)) / 100.0f)  + " minuti", Toast.LENGTH_SHORT).show();
                mpM.getStyle(new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        lineManager = new LineManager(mapView, mpM, style);
                        LineOptions lineOptions = new LineOptions()
                                .withGeometry(LineString.fromPolyline(geometry,6))
                                .withLineColor("#0582ca")
                                .withLineWidth(4.3F);

                        Line l = lineManager.create(lineOptions);
                        lines.add(l);

                        time = findViewById(R.id.chip);
                        time.setVisibility(View.VISIBLE);
                        time.setText("Time : " + (float)((int)((Double.parseDouble(pathTime)/60)*100)) / 100.0f + " minutes");
                    }

                });}
    }


    @Override
    public void onResume() {

        super.onResume();


    }

    public void showMultipleRouteOnMap(List<String> routes){

        FloatingActionButton closebutton = findViewById(R.id.deletebtn);
        closebutton.setVisibility(View.VISIBLE);

        Log.e("routes", routes.toString());
        if (mpM != null) {

            mpM.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {


                        for(int i=0; i<routes.size(); i++)
                        {
                            if(lineManager == null) {
                                lineManager = new LineManager(mapView, mpM, style);
                            }
                            LineOptions lineOptions = new LineOptions()
                                    .withGeometry(LineString.fromPolyline(routes.get(i),6))
                                    .withLineColor("#0582ca")
                                    .withLineWidth(4.3F);

                            Line l = lineManager.create(lineOptions);
                            lines.add(l);

                            time = findViewById(R.id.chip);
                            time.setVisibility(View.VISIBLE);
                            time.setText("Time : " + (float)((int)(multipleTimePath/60)*100) / 100.0f + " minutes");

                        }
                }



            });}
    }
}
