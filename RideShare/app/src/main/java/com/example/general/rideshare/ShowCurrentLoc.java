package com.example.general.rideshare;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;


public class ShowCurrentLoc extends ActionBarActivity {

    LatLng location;
    List<LatLng> points;
    GoogleMap googleMap;
    GoogleApiClient mGoogleApiClient;
    int rid;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_current_loc);
        Bundle bundle = getIntent().getExtras();
        String l=bundle.getString("latlng");
        String p=bundle.getString("path");
        rid=bundle.getInt("rid");
        String[] loc=l.split(",");
        double lat=Double.parseDouble(loc[0].substring(1));
        double lon=Double.parseDouble(loc[1].substring(0,loc[1].length()-2));

        location=new LatLng(lat,lon);
        points = PolyUtil.decode(p);


        createMapView();
        googleMap.setMyLocationEnabled(true);
        if (checkPlayServices()) {
            buildGoogleApiClient();
        }

        AsyncCallFareShareWS task=new AsyncCallFareShareWS();
        task.execute();

    }


    String res="";
    List<LatLng> users;
    LatLng loc1;
    private class AsyncCallFareShareWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            //Invoke webservice
            res = GetRideUserCall.getUsers(rid);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
           users=new ArrayList<LatLng>();
           String sp[]=res.split("\\|");
            for(int i=0;i<sp.length-1;i++)
            {
                String[] loc=sp[i].split(" ")[1].split(",");
                double lat=Double.parseDouble(loc[0].substring(1));
                double lon=Double.parseDouble(loc[1].substring(0,loc[1].length()-2));
                loc1=new LatLng(lat,lon);


                googleMap.addMarker(new MarkerOptions().position(location).title("Ride sharer here!").icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi)));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }

            googleMap.addMarker(new MarkerOptions().position(location).title("Taxi is here!").icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi)));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            getPath();
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

        PolylineOptions lineOptions = null;
        lineOptions = new PolylineOptions();

        if(points!=null) {
            lineOptions.addAll(points);
            lineOptions.width(4);
            lineOptions.color(Color.RED);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(points.get(0)));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            googleMap.addPolyline(lineOptions);

        }

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
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

    private void createMapView(){
        try {
            if(null == googleMap){
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();
                if(null == googleMap) {
                    Toast.makeText(getApplicationContext(),
                            "Error creating map", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException exception){
            Log.e("mapApp", exception.toString());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_current_loc, menu);
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
