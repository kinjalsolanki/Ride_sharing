package com.example.general.rideshare;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class GoogleRoute extends ActionBarActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {

    String uname,approxfare;
    LatLng sLatLng,dLatLng;
    GoogleMap googleMap;
    GoogleApiClient mGoogleApiClient;
    String rdistance="", gdistance="",vehicleType;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final String TAG = MainActivity.class.getSimpleName();
    double latitude = 0, longitude = 0;
    ArrayList<LatLng> points = null;
    ArrayList<String> area;
    private ProgressBar spinner;
    TextView dist,fare;
    Button b;
    GoogleCloudMessaging gcm;
    String regid;
    String PROJECT_NUMBER = "762985634560";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_route);

        dist=(TextView)findViewById(R.id.disttext);
        fare=(TextView)findViewById(R.id.fare);
        b=(Button)findViewById(R.id.createRoute);
        b.setOnClickListener(this);


        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);


        Bundle bundle = getIntent().getExtras();
        sLatLng=(LatLng)bundle.get("Source");
        dLatLng=(LatLng)bundle.get("Destination");
        vehicleType=bundle.getString("vehicle");
        uname=bundle.getString("uname");

        createMapView();
        googleMap.setMyLocationEnabled(true);
        if (checkPlayServices()) {
            buildGoogleApiClient();
        }
        getRegId();
        spinner.setVisibility(View.VISIBLE);
        getPath();
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
                    dist.setText("Distance\n"+gdistance+" km");
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
            spinner.setVisibility(View.GONE);
            AsyncCallFareShareWS task=new AsyncCallFareShareWS();
            task.execute();
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


    private void getMyCurrentLocation() {

        Location mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
        }
        displayLocation();
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
        getMenuInflater().inflate(R.menu.menu_google_route, menu);
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(GoogleRoute.this, GetInformation.class);
        intent.putExtra("uname",uname);
        intent.putExtra("Source",sLatLng);
        intent.putExtra("Destination",dLatLng);
        intent.putExtra("point",points);
        intent.putExtra("area",area);
        intent.putExtra("regId",regid);
        intent.putExtra("vehicle",vehicleType);
        intent.putExtra("fare",approxfare);
        startActivity(intent);
    }



    String res="";
    private class AsyncCallFareShareWS extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            //Invoke webservice
            res = FareShareCall.fareShare(vehicleType, Double.parseDouble(gdistance));
            return res;
        }

        @Override
        protected void onPostExecute(String result) {

            fare.setText("Approx Fare: \n"+res);
            approxfare=res;
            //Set respons
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

}
