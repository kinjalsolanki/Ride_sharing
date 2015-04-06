package com.example.general.rideshare;

import android.content.Intent;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class GetInformation extends ActionBarActivity implements View.OnClickListener {

    String uname,regid,vehicleType,fare;
    LatLng sLatLng,dLatLng;
    ArrayList<LatLng> points=null;
    ArrayList<String> area;
    TextView t;
    Button createride;
    EditText seats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_information);

        Bundle bundle=getIntent().getExtras();
        uname=bundle.getString("uname");
        regid=bundle.getString("regid");
        points=(ArrayList<LatLng>)bundle.get("point");
        area=(ArrayList<String>)bundle.get("area");
        sLatLng=(LatLng)bundle.get("Source");
        dLatLng=(LatLng)bundle.get("Destination");
        vehicleType=bundle.getString("vehicle");
        fare=bundle.getString("fare");
        System.out.println("vehicle=====" + vehicleType);

        createride=(Button)findViewById(R.id.createRoute);
        createride.setOnClickListener(this);
        t=(TextView)findViewById(R.id.from);
        t.setText(getAddress(sLatLng.latitude,sLatLng.longitude));

        t=(TextView)findViewById(R.id.to);
        t.setText(getAddress(dLatLng.latitude,dLatLng.longitude));

        t=(TextView)findViewById(R.id.vehicle);
        t.setText(vehicleType);

        t=(TextView)findViewById(R.id.fare);
        t.setText(fare);

        seats=(EditText)findViewById(R.id.editText);

    }


    private String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                result.append(address.getAddressLine(0)).append(",");
                result.append(address.getSubLocality());

            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_get_information, menu);
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

    String res = "";

    @Override
    public void onClick(View v) {
        AsyncCallRouteCreateWS task=new AsyncCallRouteCreateWS();
        task.execute();
    }

    private class AsyncCallRouteCreateWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            res = RouteCreationCall.createRoute(points, sLatLng, dLatLng, uname, Integer.parseInt(seats.getText().toString()), area, regid);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Toast.makeText(getBaseContext(), "Route Created Successfully", Toast.LENGTH_SHORT).show();
            startService(new Intent(getBaseContext(), UpdateLocationService.class).putExtra("uname", uname));
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
