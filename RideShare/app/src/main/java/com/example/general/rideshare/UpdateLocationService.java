package com.example.general.rideshare;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

//import com.google.android.gms.location.LocationListener;

public class UpdateLocationService extends Service {
    public UpdateLocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    double latitude,longitude;
    String uname;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        //Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        Bundle bundle=intent.getExtras();
        uname=bundle.getString("uname");

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.makeUseOfNewLocation(location);
                latitude=location.getLatitude();
                //String givenlatitude=String.valueOf(latitude);
                longitude=location.getLongitude();
                //Toast.makeText(getApplicationContext(),String.valueOf(latitude)+ " " +String.valueOf(longitude) , Toast.LENGTH_SHORT).show();
                AsyncUpdateLoc task1 = new AsyncUpdateLoc();
                //Call execute
                task1.execute();

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        // Register the listener with the Location Manager to receive location updates
        int MINIMUM_TIME_BETWEEN_UPDATES=100;

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MINIMUM_TIME_BETWEEN_UPDATES, 1.0f, locationListener);

        return START_STICKY;
    }


    private class AsyncUpdateLoc extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            //Invoke webservice
            UpdateLocCall.updateLoc(latitude,longitude,uname);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }
}
