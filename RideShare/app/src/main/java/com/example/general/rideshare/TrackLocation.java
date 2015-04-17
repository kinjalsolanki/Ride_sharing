package com.example.general.rideshare;

import android.app.Activity;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class TrackLocation extends ActionBarActivity implements View.OnClickListener {

    String uname="";
    String res="";
    LinearLayout ll;
    int id=0;
    ScrollView sv;
    int routeid=0;
    List<LatLng> points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_track_location);

        Bundle bundle=getIntent().getExtras();
        uname=bundle.getString("uname");

        sv = new ScrollView(this);
        ll = new LinearLayout(this);

        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(5,20,5,20);
        sv.addView(ll);

        AsyncGetRoutes task=new AsyncGetRoutes();
        task.execute();

        AsyncGetOwnerRoutes t=new AsyncGetOwnerRoutes();
        t.execute();

    }


    private void display(String route_id, String source_id, String dest_id, String no_of_seat) {


        String initial[]=source_id.split(" ");
        String[] sloc=initial[1].split(",");
        Double slat=Double.parseDouble(sloc[0].substring(1));
        Double slon=Double.parseDouble(sloc[1].substring(0,sloc[1].length()-2));
        String source=getAddress(slat,slon);

        initial=dest_id.split(" ");
        String[] dloc=initial[1].split(",");
        Double dlat=Double.parseDouble(dloc[0].substring(1));
        Double dlon=Double.parseDouble(dloc[1].substring(0,dloc[1].length()-2));
        String destn=getAddress(dlat,dlon);

        LinearLayout l=new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(5,20,5,20);

        TextView tv = new TextView(this);
        tv.setText("Source: " + source);
        l.addView(tv);

        tv = new TextView(this);
        tv.setText("Destination: " + destn);
        l.addView(tv);

        tv = new TextView(this);
        tv.setText("Available Seats: " + no_of_seat);
        l.addView(tv);

        Button btn = new Button(this);
        btn.setText("Track Taxi");
        btn.setTag("Track");
        btn.setId(Integer.parseInt(route_id));
        btn.setOnClickListener(this);
        l.addView(btn);

        btn = new Button(this);
        btn.setText("Leave Ride");
        btn.setTag("Leave");
        btn.setId(Integer.parseInt(route_id));
        btn.setOnClickListener(this);
        l.addView(btn);

        ll.addView(l);
    }

    private void display1(String route_id, String source_id, String dest_id, String no_of_seat) {
        String initial[]=source_id.split(" ");
        String[] sloc=initial[1].split(",");
        Double slat=Double.parseDouble(sloc[0].substring(1));
        Double slon=Double.parseDouble(sloc[1].substring(0,sloc[1].length()-2));
        String source=getAddress(slat,slon);

        initial=dest_id.split(" ");
        String[] dloc=initial[1].split(",");
        Double dlat=Double.parseDouble(dloc[0].substring(1));
        Double dlon=Double.parseDouble(dloc[1].substring(0,dloc[1].length()-2));
        String destn=getAddress(dlat,dlon);

        LinearLayout l=new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(5,20,5,20);

        TextView tv = new TextView(this);
        tv.setText("Source: " + source);
        l.addView(tv);

        tv = new TextView(this);
        tv.setText("Destination: " + destn);
        l.addView(tv);

        tv = new TextView(this);
        tv.setText("Available Seats: " + no_of_seat);
        l.addView(tv);

        Button btn = new Button(this);
        btn.setText("Track User");
        btn.setTag("Track");
        btn.setId(Integer.parseInt(route_id));
        btn.setOnClickListener(this);
        l.addView(btn);


        btn = new Button(this);
        btn.setText("End Ride");
        btn.setTag("End");
        btn.setId(Integer.parseInt(route_id));
        btn.setOnClickListener(this);
        l.addView(btn);
        ll.addView(l);
    }

    @Override
    public void onClick(View v)
    {
        routeid=v.getId();

        if(v.getTag().toString().equals("Track")) {
            AsyncGetCurrentLoc task = new AsyncGetCurrentLoc();
            task.execute();
        }
        else if(v.getTag().toString().equals("End"))
        {
            AsyncEndRide task=new AsyncEndRide();
            task.execute();
        }
        else if(v.getTag().toString().equals("Leave"))
        {
            AsyncLeaveRide task=new AsyncLeaveRide();
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
                 result.append(address.getAddressLine(0)).append(",");
                result.append(address.getSubLocality());

            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();
    }

    private class AsyncGetRoutes extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            //Invoke webservice
            res=GetMyRouteCall.getRoute(uname);
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println(uname+"===my routes================================="+res);

            String[] split=res.split("\\*");

            System.out.println(Arrays.toString(split));

            for(int i=0;i<split.length;i++)
            {
                String param[]=split[i].split("\\|");
                System.out.println(Arrays.toString(param));
                display(param[0],param[1],param[2],param[3]);
            }
            setContentView(sv);
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

    private class AsyncEndRide extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            //Invoke webservice
            res=EndRideCall.endRide(routeid);
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
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

    private class AsyncLeaveRide extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            //Invoke webservice
            res=LeaveRideCall.leaveRide(routeid,uname);
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            Intent intent=getIntent();
            finish();
            startActivity(intent);
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

    private class AsyncGetOwnerRoutes extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            //Invoke webservice
            res=GetMyRouteCall.getOwnerRoute(uname);
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println(uname+"===my routes================================="+res);

            String[] split=res.split("\\*");

            System.out.println(Arrays.toString(split));

            for(int i=0;i<split.length;i++)
            {
                String param[]=split[i].split("\\|");
                System.out.println(Arrays.toString(param));
                display1(param[0],param[1],param[2],param[3]);
            }
            setContentView(sv);
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

    private class AsyncGetCurrentLoc extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            //Invoke webservice
            res=GetCurrentLocCall.getLocation(routeid);
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            String[] split=res.split(" ");
            String[] loc=split[1].split(",");
            double lat=Double.parseDouble(loc[0].substring(1));
            double lon=Double.parseDouble(loc[1].substring(0,loc[1].length()-2));
            LatLng l=new LatLng(lat,lon);
            points = PolyUtil.decode(split[2]);

            Intent i = new Intent(TrackLocation.this,ShowCurrentLoc.class);
            i.putExtra("latlng",split[1]);
            i.putExtra("rid",routeid);
            i.putExtra("path",split[2]);
            startActivity(i);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_track_location, menu);
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
