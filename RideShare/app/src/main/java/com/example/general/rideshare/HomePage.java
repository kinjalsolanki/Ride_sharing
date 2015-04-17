package com.example.general.rideshare;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class HomePage extends ActionBarActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleMap googleMap;
    GoogleApiClient mGoogleApiClient;


    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;

    String vehicleType="Rickshaw";
    //PlacesTask placesTask;
    //PlaceParserTask placeParserTask;
    ArrayList<LatLng> points = null;
    ArrayList<String> area;
    public String user_name = "user_name";
    String rdistance="", gdistance="";
    double googledistace=0;
    public LatLng sLatLng,dLatLng;
    Location mLastLocation;
    PlaceParserTask placeParserTask;
    PlacesTask placesTask;
    double latitude = 0, longitude = 0;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final String TAG = MainActivity.class.getSimpleName();

    GoogleCloudMessaging gcm;
    String regid;
    String PROJECT_NUMBER = "762985634560";

    ImageButton sourceLocationBtn,destLocationBtn,getSrc,getDest,refreshBtn,vehicleBtn;
    AutoCompleteTextView sourcetextview,desttextview;
    Button searchRoute,selectRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        sourceLocationBtn=(ImageButton)findViewById(R.id.sourcebtn);
        sourcetextview=(AutoCompleteTextView)findViewById(R.id.sourcetext);
        sourceLocationBtn.setOnClickListener(this);
        sourcetextview.setThreshold(1);

        destLocationBtn=(ImageButton)findViewById(R.id.destbtn);
        desttextview=(AutoCompleteTextView)findViewById(R.id.desttext);
        destLocationBtn.setOnClickListener(this);
        desttextview.setThreshold(1);

        getSrc=(ImageButton)findViewById(R.id.getsrc);
        getSrc.setOnClickListener(this);

        vehicleBtn=(ImageButton)findViewById(R.id.vehicle);
        vehicleBtn.setOnClickListener(this);


        searchRoute=(Button)findViewById(R.id.searchRoute);
        searchRoute.setOnClickListener(this);

        selectRoute=(Button)findViewById(R.id.chooseRoute);
        selectRoute.setOnClickListener(this);

        getDest=(ImageButton)findViewById(R.id.getdest);
        getDest.setOnClickListener(this);

        refreshBtn=(ImageButton) findViewById(R.id.refreshbtn);
        refreshBtn.setOnClickListener(this);

        Bundle b = getIntent().getExtras();
        user_name = b.getString("email");

        startService(new Intent(getBaseContext(), UpdateLocationService.class).putExtra("uname", user_name));



        sourcetextview.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesTask = new PlacesTask();
                placesTask.execute(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });


        desttextview.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesTask = new PlacesTask();
                placesTask.execute(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });



        createMapView();
        googleMap.setMyLocationEnabled(true);
        if (checkPlayServices()) {
            buildGoogleApiClient();
        }
        getRegId();
        getMyCurrentLocation();



        //slider

        mDrawerList = (ListView)findViewById(R.id.navList);mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


    }



    private class PlacesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... place) {
            // For storing data from web service
            String data = "";

            // Obtain browser key from https://code.google.com/apis/console
            String key = "key=AIzaSyAvc17jSFhlXTUUe2vFchfoJc5F6bIb7yI";

            String input = "";

            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }

            // place type to be searched
            String types = "types=geocode";

            // Sensor enabled
            String sensor = "sensor=false";

            // Building the parameters to the web service
            String parameters = input + "&" + types + "&" + sensor + "&" + key;

            // Output format
            String output = "json";

            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/" + output + "?" + parameters;

            try {
                // Fetching the data from we service
                data = downloadUrl(url);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Creating ParserTask
            placeParserTask = new PlaceParserTask();

            // Starting Parsing the JSON string returned by Web Service
            placeParserTask.execute(result);
        }
    }


    private class PlaceParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;

            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try {
                jObject = new JSONObject(jsonData[0]);

                // Getting the parsed data as a List construct
                places = placeJsonParser.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {

            String[] from = new String[]{"description"};
            int[] to = new int[]{android.R.id.text1};

            // Creating a SimpleAdapter for the AutoCompleteTextView
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);

            // Setting the adapter
            sourcetextview.setAdapter(adapter);
            desttextview.setAdapter(adapter);

        }
    }


    private void addDrawerItems() {
        String[] osArray = { "Track Ride", "Help", "Exit" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(position==0) {
                    Intent intent = new Intent(HomePage.this, TrackLocation.class);
                    intent.putExtra("uname", user_name);
                    startActivity(intent);
                }

                if(position==2)
                {
                    System.exit(0);
                }
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Options");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void getMyCurrentLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
        }
        displayLocation();
    }

    private void displayLocation() {
        sLatLng = new LatLng(latitude, longitude);
        String a = getFullAddress(sLatLng.latitude, sLatLng.longitude);
        googleMap.addMarker(new MarkerOptions().position(sLatLng).title("From: "+a));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sLatLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    private String getFullAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                result.append(address.getAddressLine(0));
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();
    }

    public void getRegId() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(PROJECT_NUMBER);
                    msg = "Device registered, registration ID=" + regid;
                    Log.i("GCM", msg);

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //etRegId.setText(msg + "\n");
                System.out.println("Reg id=======================================" + msg);
            }
        }.execute(null, null, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    public void onConnected(Bundle bundle) {
        getMyCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void createMapView() {
        try {
            if (null == googleMap) {
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();
                if (null == googleMap) {
                    Toast.makeText(getApplicationContext(),
                            "Error creating map", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException exception) {
            Log.e("mapApp", exception.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            System.out.println(item.getTitle());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v)
    {
        if(v.getTag().toString().equals("sourcebtn"))
        {
            desttextview.setVisibility(View.INVISIBLE);
            getDest.setVisibility(View.INVISIBLE);
            sourcetextview.setVisibility(View.VISIBLE);
            getSrc.setVisibility(View.VISIBLE);
        }
        else if(v.getTag().toString().equals("destbtn"))
        {
            sourcetextview.setVisibility(View.INVISIBLE);
            getSrc.setVisibility(View.INVISIBLE);
            desttextview.setVisibility(View.VISIBLE);
            getDest.setVisibility(View.VISIBLE);
        }
        else if(v.getTag().toString().equals("refreshbtn"))
        {
            sourcetextview.setVisibility(View.INVISIBLE);
            getSrc.setVisibility(View.INVISIBLE);
            desttextview.setVisibility(View.INVISIBLE);
            getDest.setVisibility(View.INVISIBLE);
            googleMap.clear();
            getMyCurrentLocation();
        }
        else if(v.getTag().toString().equals("getsrc"))
        {
            String location = sourcetextview.getText().toString();
            if (location != null && !location.equals("")) {
                new GeocoderTaskSource().execute(location);
            }
        }
        else if(v.getTag().toString().equals("getdest"))
        {
            String location = desttextview.getText().toString();
            if (location != null && !location.equals("")) {
                new GeocoderTaskDest().execute(location);
            }
        }
        else if(v.getTag().toString().equals("selectRoute")) {
            if (sLatLng != null && dLatLng != null) {
                final CharSequence[] items = {"Google Path", "Recommended Path", "Via Waypoints"};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select one Option");
                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {

                        Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();

                        //If the Cheese item is chosen close the dialog box

                        if (items[item] == "Google Path") {
                            Intent intent = new Intent(HomePage.this, GoogleRoute.class);
                            intent.putExtra("uname",user_name);
                            intent.putExtra("Source",sLatLng);
                            intent.putExtra("Destination",dLatLng);
                            intent.putExtra("vehicle",vehicleType);
                            startActivity(intent);
                        } else if (items[item] == "Recommended Path")
                        {
                            Intent intent = new Intent(HomePage.this, RecommendedRoute.class);
                            intent.putExtra("uname",user_name);
                            intent.putExtra("Source",sLatLng);
                            intent.putExtra("Destination",dLatLng);
                            intent.putExtra("vehicle",vehicleType);
                            startActivity(intent);
                        } else if (items[item] == "Via Waypoints") {
                            showInputDialog();
                        }

                        dialog.dismiss();

                    }
                });

                AlertDialog alert = builder.create();

                //display dialog box

                alert.show();

            }
            else
            {
                Toast.makeText(getBaseContext(), "Please Enter Source and Destination", Toast.LENGTH_SHORT).show();
            }
        }
        if(v.getTag().toString().equals("vehicle"))
        {
            final CharSequence[] items = {"Rickshaw", "Taxi"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select one Option");
            builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int item) {

                    Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();

                    //If the Cheese item is chosen close the dialog box

                    if (items[item] == "Rickshaw") {
                        vehicleType="Rickshaw";
                    } else if (items[item] == "Taxi") {
                        vehicleType="Taxi";}

                    dialog.dismiss();

                }
            });

            AlertDialog alert = builder.create();

            //display dialog box

            alert.show();

        }

        if(v.getTag().toString().equals("searchRoute"))
        {
            if (sLatLng != null && dLatLng != null) {
                AsyncSearchRouteWS task1 = new AsyncSearchRouteWS();
                task1.execute();
            }
            else
            {
                Toast.makeText(getBaseContext(), "Please Enter Source and Destination", Toast.LENGTH_SHORT).show();
            }
        }

    }


    final Context context = this;
    String res="";
    private class AsyncSearchRouteWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            //Invoke webservice

            String a = getAddress(dLatLng.latitude, dLatLng.longitude);
            res = RouteSearchCall.returnRouteSD(a, 1);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //Set response
            //mLatitudeText.setText(res);
            Intent intent = new Intent(context, SearchRouteDisplay.class);
            intent.putExtra("routes", res);
            intent.putExtra("destLat", dLatLng.latitude);
            intent.putExtra("destLon", dLatLng.longitude);
            intent.putExtra("sourceLat", sLatLng.latitude);
            intent.putExtra("sourceLon", sLatLng.longitude);
            intent.putExtra("vehicle",vehicleType);
            intent.putExtra("uname", user_name);
            intent.putExtra("seats", 1);
            startActivity(intent);
            //Make ProgressBar invisible
            //pg.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onPreExecute() {
            //Make ProgressBar invisible
            //pg.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

    }




    protected void showInputDialog() {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(HomePage.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(HomePage.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(HomePage.this, WaypointPath.class);
                        intent.putExtra("uname",user_name);
                        intent.putExtra("Source",sLatLng);
                        intent.putExtra("Destination",dLatLng);
                        intent.putExtra("vehicle",vehicleType);
                        intent.putExtra("area", editText.getText().toString());
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void getPath()
    {
        if(sLatLng!=null && dLatLng!=null)
        {
            String url = getDirectionsUrl(sLatLng, dLatLng);
            DownloadTask downloadTask = new DownloadTask();
            // Start downloading json data from Google Directions API
            downloadTask.execute(url);
        }
        else
        {
            Toast.makeText(getBaseContext(), "Please Enter Source and Destination......", Toast.LENGTH_SHORT).show();
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Waypoints
        String waypoints = "";
     /*   for(int i=2;i<markerPoints.size();i++){
            LatLng point  = (LatLng) markerPoints.get(i);
            if(i==2)
                waypoints = "waypoints=";
            waypoints += point.latitude + "," + point.longitude + "|";
        }*/

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + waypoints;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service

            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }


        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exceptn downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                area = new ArrayList<String>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if(j==0){
                        // Get distance from the list
                        gdistance = (String) point.get("distance").split(" ")[0];
                        continue;
                    }

                    System.out.println("gdistance==================" + gdistance);
                    double lat = 0, lng = 0;
                    if (point.get("lat") != null && point.get("lng") != null) {
                        System.out.println("Latitude:" + point.get("lat"));
                        System.out.println("Longitude:"+ point.get("lng"));
                        lat = Double.parseDouble(point.get("lat"));
                        lng = Double.parseDouble(point.get("lng"));
                    }


                        LatLng position = null;
                        if (lat != 0 && lng != 0)
                            position = new LatLng(lat, lng);
                        if (j % 10 == 0) {
                            String a = getAddress(lat, lng);
                            System.out.println("Individual response:-------------------------------for--- " + j + "-------------" + a);
                            if (!area.contains(a) && a.charAt(a.length() - 1) != '|')
                                area.add(a);
                        }

                        if (position != null)
                            points.add(position);
                    }
                }

                // Adding all the points in the route to LineOptions

                System.out.println("--------------------------------------------------------points-----------------------------------------------------------");
                System.out.println(points);
                lineOptions.addAll(points);
                lineOptions.width(4);
                lineOptions.color(Color.BLUE);

            //    googleMap.clear();
                googleMap.addPolyline(lineOptions);
        }
    }

    private String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                result.append(address.getSubLocality()).append("|");
                if (address.getPostalCode() != null)
                    result.append(address.getPostalCode());
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();
    }

    private class GeocoderTaskDest extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;

            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
                System.out.println("get path response...................." + addresses);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {

            if (addresses == null || addresses.size() == 0) {
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }

            // Clears all the existing markers on the map

            // Adding Markers on Google Map for each matching address
            for (int i = 0; i < addresses.size(); i++) {

                Address address = (Address) addresses.get(i);

                // Creating an instance of GeoPoint, to display in Google Map
                dLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                //System.out.println("Destination location:-------------------------------------------------------------" + DlatLng.toString());
                String addressText = String.format("%s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getCountryName());

                googleMap.addMarker(new MarkerOptions().position(dLatLng).title("To: "+address.getAddressLine(0)));

                // Locate the first location
                if (i == 0) {
                    //googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(dLatLng));
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                }
            }
        }
    }

    private class GeocoderTaskSource extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;

            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
                System.out.println("get path response...................." + addresses);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {

            if (addresses == null || addresses.size() == 0) {
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }

            // Clears all the existing markers on the map
            googleMap.clear();

            // Adding Markers on Google Map for each matching address
            for (int i = 0; i < addresses.size(); i++) {

                Address address = (Address) addresses.get(i);

                // Creating an instance of GeoPoint, to display in Google Map
                sLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                //System.out.println("Destination location:-------------------------------------------------------------" + DlatLng.toString());
                String addressText = String.format("%s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getCountryName());

                googleMap.addMarker(new MarkerOptions().position(sLatLng).title("From: "+address.getAddressLine(0)));

                // Locate the first location
                if (i == 0) {
                    //googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(sLatLng));
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                }
            }
        }
    }
}
