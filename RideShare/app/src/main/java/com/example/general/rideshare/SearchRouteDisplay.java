package com.example.general.rideshare;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class SearchRouteDisplay extends ActionBarActivity implements View.OnClickListener {

    Button selectR,joinRoute;
    TextView tview;
    String routes;
    String area;
    List<LatLng> points;
    List<String> areas;
    LatLng location;
    GoogleMap googleMap;
    LatLng sLatLng,dLatLng;
    Button selectRoute;
    double destLat, destLon, sourceLat, sourceLon;
    String user_name="",gdistance="",vehicleType="Rikshaw",approxfare;
    int red=100,green=100,blue=100;
    TextView dist,fare;
    RadioButton r;
    int seat;
    ScrollView sv;
    LinearLayout ll,l1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_route_display);

        joinRoute=(Button) findViewById(R.id.searchRoute);
        joinRoute.setOnClickListener(this);

        ll=new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);


        dist=(TextView)findViewById(R.id.disttext);
        fare=(TextView)findViewById(R.id.fare);


        sv=(ScrollView) findViewById(R.id.scrollView3);
        Bundle bundle = getIntent().getExtras();
        routes = bundle.getString("routes");
        destLat=bundle.getDouble("destLat");
        destLon=bundle.getDouble("destLon");
        sourceLat=bundle.getDouble("sourceLat");
        sourceLon=bundle.getDouble("sourceLon");
        seat=bundle.getInt("seats");
        sLatLng=new LatLng(sourceLat,sourceLon);
        dLatLng=new LatLng(destLat,destLon);

        user_name=bundle.getString("uname");
        createMapView();
        googleMap.setMyLocationEnabled(true);
        splitRoutes();

    }





    private void createMapView(){
        try {
            if(null == googleMap){
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();
                if(null == googleMap)
                {
                    Toast.makeText(getApplicationContext(),
                            "Error creating map", Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch (NullPointerException exception)
        {
            Log.e("mapApp", exception.toString());
        }
    }

    private void getPath() {

        PolylineOptions lineOptions = null;
        lineOptions = new PolylineOptions();

        if(points!=null) {
            lineOptions.addAll(points);
            lineOptions.width(4);

            l1=new LinearLayout(this);


            red=red+10;
            green=green-100;
            blue=blue+150;

            if(red<0 || red>255)
                red=100;

            if(green<0 || green>255)
                green=100;

            if(blue<0 || blue>255)
                blue=100;

            lineOptions.color(Color.rgb(red, green, blue));
            r=new RadioButton(this);
            r.setText("Select this route");
            r.setOnClickListener(this);
            r.setTextColor(Color.rgb(red, green, blue));
            r.setTag(route_id);
            l1.addView(r);
            ll.addView(l1);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(points.get(0)));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            googleMap.addPolyline(lineOptions);
        }

    }

    String route_id="";
    private void splitRoutes() {

        int j=0;
        String allRoutes[]=routes.split(" ");
        System.out.println("Routes returned:::========================="+ Arrays.toString(allRoutes));
        for(int i=0;i<allRoutes.length;i++)
        {
            System.out.println("inside foe================================="+i);
            route_id="";
            for(j=0;j<allRoutes[i].length();j++)
            {
                if(Character.isDigit(allRoutes[i].charAt(j)))
                {
                    route_id=route_id+allRoutes[i].charAt(j);
                }
                else
                    break;
            }
            LatLng l = new LatLng(destLat, destLon);
            LatLng l1=new LatLng(sourceLat,sourceLon);

            if(route_id!="") {
                int rid = Integer.parseInt(route_id);
                System.out.println("Route id===============================" + rid);
                points = PolyUtil.decode(allRoutes[i].substring(j));
                System.out.println("Decoded roue==================================" + i + "=============" + points);


                if (PolyUtil.isLocationOnPath(l, points, true, 200) && PolyUtil.isLocationOnPath(l1, points, true, 200))
                    getPath();
            }

            else
            {
                Toast.makeText(getApplicationContext(),"No routes found" , Toast.LENGTH_LONG).show();
            }
            //System.out.println("extracted point===================================="+i+"============"+points.toString());
        }
        sv.addView(ll);

    }


    private String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                result.append(address.getSubLocality()).append("|");
                if(address.getPostalCode()!=null)
                    result.append(address.getPostalCode());
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();
    }


    @Override
    public void onClick(View v)
    {
        if(v.getTag().toString().equals("joinRoute")) {
            AsyncSearchRouteWS task1 = new AsyncSearchRouteWS();
            task1.execute();

        }
        else {
            route_id = v.getTag().toString();
            System.out.println("ruxkfnvzfd"+route_id);
        }
    }

    private void getPath1()
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
                DirectionsJSONParser1 parser = new DirectionsJSONParser1();

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
                areas = new ArrayList<String>();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    if(j==0){
                        // Get distance from the list
                        gdistance = (String) point.get("distance").split(" ")[0];
                    }

                    System.out.println("gdistance==================" + gdistance);
                    dist.setText("Distance\n"+gdistance+" km");
                }
            }

            AsyncCallFareShareWS task=new AsyncCallFareShareWS();
            task.execute();
        }
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









    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        List<LatLng> markerPoints=new ArrayList<LatLng>();
        markerPoints.add(points.get(0));
        markerPoints.add(points.get(points.size() - 1));
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Waypoints
        String waypoints = "";
       for(int i=0;i<markerPoints.size();i++){
            LatLng point  = (LatLng) markerPoints.get(i);
            if(i==0)
                waypoints = "waypoints=";
            waypoints += point.latitude + "," + point.longitude + "|";
        }

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + waypoints;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }


    private class AsyncSearchRouteWS extends AsyncTask<String, Void, Void>
    {
        @Override
        protected Void doInBackground(String... params) {
            //Invoke webservice
            int rid=Integer.parseInt(route_id);
            System.out.println("route id======================================="+rid);
            String area=getAddress(sourceLat,sourceLon);
            res = RouteSelect.selectRoute(rid,seat,area,user_name,sLatLng.toString(),dLatLng.toString());
            getPath1();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(getBaseContext(), "Ride joined!", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPreExecute() {
        }
        @Override
        protected void onProgressUpdate(Void... values) {
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_route_display, menu);
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
}
