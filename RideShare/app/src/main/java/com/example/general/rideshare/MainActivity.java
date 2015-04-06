package com.example.general.rideshare;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
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

public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    GoogleMap googleMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    TextView mLatitudeText;
    Button source, dest, path, createRt, searchRt, RecoRt;
    LatLng SlatLng, DlatLng, MlatLng;
    AutoCompleteTextView atvPlaces;
    PlacesTask placesTask;
    PlaceParserTask placeParserTask;
    ArrayList<LatLng> points = null;
    ArrayList<String> area;
    ArrayList<LatLng> rPoints=null;
    String user_name = "user_name";
    String rdistance="", gdistance="";
    double googledistace=0;
    boolean recomm=false;
    boolean flag=false;

    double latitude = 0, longitude = 0;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final String TAG = MainActivity.class.getSimpleName();

    GoogleCloudMessaging gcm;
    String regid;
    String PROJECT_NUMBER = "762985634560";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLatitudeText = (TextView) findViewById(R.id.textView);

        source = (Button) findViewById(R.id.button);
        dest = (Button) findViewById(R.id.button1);
        path = (Button) findViewById(R.id.button2);
        createRt = (Button) findViewById(R.id.button3);
        searchRt = (Button) findViewById(R.id.button4);
        RecoRt = (Button) findViewById(R.id.button5);

        atvPlaces = (AutoCompleteTextView) findViewById(R.id.textView1);
        atvPlaces.setThreshold(1);
        source.setOnClickListener(this);
        dest.setOnClickListener(this);
        path.setOnClickListener(this);
        createRt.setOnClickListener(this);
        searchRt.setOnClickListener(this);
        RecoRt.setOnClickListener(this);


        Bundle b = getIntent().getExtras();
        user_name = b.getString("email");
        startService(new Intent(getBaseContext(), UpdateLocationService.class).putExtra("uname", user_name));

        atvPlaces.addTextChangedListener(new TextWatcher() {

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

    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();

            mLatitudeText.setText(latitude + ", " + longitude);

        } else {

            mLatitudeText
                    .setText("(Couldn't get the location. Make sure location is enabled on the device)");
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_action, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals("Track")) {
            Intent i = new Intent(MainActivity.this, TrackLocation.class);
            i.putExtra("uname", user_name);
            startActivity(i);
        }
        if (item.getTitle().equals("Exit")) {
            System.exit(0);
        }
        return true;
    }

    @Override
    public void onConnected(Bundle bundle) {
        displayLocation();
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

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button:
                SlatLng = new LatLng(latitude, longitude);
                googleMap.addMarker(new MarkerOptions().position(SlatLng).title("You are here"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(SlatLng));
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                break;
            case R.id.button1:
                String location = atvPlaces.getText().toString();
                if (location != null && !location.equals("")) {
                    new GeocoderTask().execute(location);
                }
                break;
            case R.id.button2:
                getPath();
                break;
            case R.id.button3:
                AsyncCallRouteCreateWS task = new AsyncCallRouteCreateWS();
                //Call execute
                task.execute();
                break;
            case R.id.button4:
                AsyncSearchRouteWS task1 = new AsyncSearchRouteWS();
                //Call execute
                task1.execute();
                break;
            case R.id.button5:
                recomm=true;
                AsyncRecommendRouteWS task2 = new AsyncRecommendRouteWS();
                task2.execute();

                break;
        }

    }


    String res = "";

    private class AsyncCallRouteCreateWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            //Invoke webservice
            res = RouteCreationCall.createRoute(points, SlatLng, DlatLng, user_name, 2, area, regid);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //Set response
            mLatitudeText.setText(res);

            startService(new Intent(getBaseContext(), UpdateLocationService.class).putExtra("uname", user_name));
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

    final Context context = this;

    private class AsyncSearchRouteWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            //Invoke webservice

            String a = getAddress(DlatLng.latitude, DlatLng.longitude);
            res = RouteSearchCall.returnRouteSD(a, 1);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //Set response
            //mLatitudeText.setText(res);
            Intent intent = new Intent(context, SearchRoute.class);
            intent.putExtra("routes", res);
            intent.putExtra("destLat", DlatLng.latitude);
            intent.putExtra("destLon", DlatLng.longitude);
            intent.putExtra("sourceLat", SlatLng.latitude);
            intent.putExtra("sourceLon", SlatLng.longitude);
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


    private class AsyncRecommendRouteWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            //Invoke webservice

            res = RouteRecommendation.RecommendedRoute();
            System.out.println("Recommeneded areaaaa============================================="+res);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            String[] rr = res.split(",");
            int i = 0;
            while (i <= 2 && rr[i] != null && !flag) {
                String location = rr[i];
                if (location != null && !location.equals("")) {
                    System.out.println("locatttioHADFkJBVCLJBVLJ"+location);
                    new GeocoderTask1().execute(location);
                    if(flag)
                        break;
                }
                i++;
            }
            if(!flag)
            {
                getPath();
            }
            else
            {
                flag=false;
            }

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


    private void getPath() {
        String url = getDirectionsUrl(SlatLng, DlatLng);
        DownloadTask downloadTask = new DownloadTask();
        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    private void getPath1() {
        String url = getDirectionsUrl1(SlatLng, MlatLng,DlatLng);
        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }


    private String getDirectionsUrl1(LatLng origin, LatLng midpt, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Waypoints
        String waypoints = "waypoints=";
     /*   for(int i=2;i<markerPoints.size();i++){
            LatLng point  = (LatLng) markerPoints.get(i);
            if(i==2)
                waypoints = "waypoints=";
            waypoints += point.latitude + "," + point.longitude + "|";
        }*/

        System.out.println("Midpoint........................................................"+midpt.toString());
        waypoints += midpt.latitude + "," + midpt.longitude+"|";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + waypoints;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
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
            atvPlaces.setAdapter(adapter);
        }
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
                rPoints=new ArrayList<LatLng>();
                area = new ArrayList<String>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if(j==0){
                        // Get distance from the list
                        if(!recomm) {
                            gdistance = (String) point.get("distance").split(" ")[0];
                        }
                        else{
                            rdistance= (String) point.get("distance").split(" ")[0];
                        }

                        continue;
                    }


                    if(rdistance!="" && gdistance!="")
                    {
                        if (Double.parseDouble(rdistance) <= (Double.parseDouble(gdistance) + 10)) {
                            flag = true;
                        }
                    }


                    if((flag && recomm) || (!flag && !recomm)) {
                        System.out.println("rdistance===============" + rdistance);
                        System.out.println("gdistance==================" + gdistance);
                        double lat = 0, lng = 0;
                        if (point.get("lat") != null && point.get("lng") != null) {
                            //  System.out.println("Latitude:" + point.get("lat"));
                            //  System.out.println("Longitude:"+ point.get("lng"));
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
                        if (!recomm) {
                            if (position != null)
                                points.add(position);
                        } else {
                            if (position != null)
                                rPoints.add(position);
                        }
                    }
                }

                // Adding all the points in the route to LineOptions

                System.out.println("--------------------------------------------------------points-----------------------------------------------------------");
                System.out.println(rPoints);
                if(!recomm)
                    lineOptions.addAll(points);
                else
                {
                    if(flag)
                    {
                        lineOptions.addAll(rPoints);

                    }
                }
                lineOptions.width(4);
                lineOptions.color(Color.BLUE);
            }

            // Drawing polyline in the Google Map for the i-th route
            googleMap.clear();
            googleMap.addPolyline(lineOptions);

            /*try {
                System.out.println("Distance............................................"+DirectionsJSONParser.jDistance.get("distance").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
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

    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

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
                DlatLng = new LatLng(address.getLatitude(), address.getLongitude());
                System.out.println("Destination location:-------------------------------------------------------------" + DlatLng.toString());
                String addressText = String.format("%s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getCountryName());

                googleMap.addMarker(new MarkerOptions().position(DlatLng).title(addressText));

                // Locate the first location
                if (i == 0) {
                    //googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(DlatLng));
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                }
            }
        }
    }

    private class GeocoderTask1 extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;

            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
                System.out.println("Addresses.........................."+addresses);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("ikshvkjahbvskjbkflvjdsvbkjafdvblwkjevb;kwefjvbkqejfb;kefvbkejfv"+addresses.toString());
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
                MlatLng = new LatLng(address.getLatitude(), address.getLongitude());
                System.out.println("Midpoint location:-------------------------------------------------------------" + MlatLng.toString());
                String addressText = String.format("%s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getCountryName());

                googleMap.addMarker(new MarkerOptions().position(MlatLng).title(addressText));

                // Locate the first location
                if (i == 0) {
                    //googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(MlatLng));
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                }
            }

            getPath1();
        }
    }
}
