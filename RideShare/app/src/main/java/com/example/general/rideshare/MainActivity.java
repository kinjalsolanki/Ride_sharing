package com.example.general.rideshare;

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

import java.io.IOException;
import java.util.List;

public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    GoogleMap googleMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    TextView mLatitudeText, mLongitudeText;
    Button source,dest;
    MarkerOptions markerOptions;
    LatLng latLng;

    double latitude=0,longitude=0;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLatitudeText =(TextView)findViewById(R.id.textView);
        mLongitudeText=(TextView)findViewById(R.id.textView1);
        source=(Button)findViewById(R.id.button);
        dest=(Button)findViewById(R.id.button1);
        source.setOnClickListener(this);
        dest.setOnClickListener(this);
        createMapView();
        googleMap.setMyLocationEnabled(true);
        if (checkPlayServices()) {
            buildGoogleApiClient();
        }


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

            mLongitudeText
                    .setText("(Couldn't get the location. Make sure location is enabled on the device)");
        }
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        switch(v.getId()) {
            case R.id.button:
                latLng = new LatLng(latitude, longitude);
                googleMap.addMarker(new MarkerOptions().position(latLng).title("You are here"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                break;
            case R.id.button1:
                String location = mLongitudeText.getText().toString();
                if(location!=null && !location.equals("")){
                    new GeocoderTask().execute(location);
                }
                break;
        }

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
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {

            if(addresses==null || addresses.size()==0){
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }

            // Clears all the existing markers on the map
            googleMap.clear();

            // Adding Markers on Google Map for each matching address
            for(int i=0;i<addresses.size();i++){

                Address address = (Address) addresses.get(i);

                // Creating an instance of GeoPoint, to display in Google Map
                latLng = new LatLng(address.getLatitude(), address.getLongitude());

                String addressText = String.format("%s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getCountryName());

                googleMap.addMarker(new MarkerOptions().position(latLng).title(addressText));

                // Locate the first location
                if(i==0) {
                    //googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                }
            }
        }
    }
}
