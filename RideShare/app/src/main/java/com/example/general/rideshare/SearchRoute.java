package com.example.general.rideshare;

import android.app.Fragment;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class SearchRoute extends ActionBarActivity implements View.OnClickListener {

    TextView tview;
    String routes;
    String area;
    List<LatLng> points;
    LatLng location;
    GoogleMap googleMap;
    Button selectRoute;
    double destLat, destLon, sourceLat, sourceLon;
    String user_name="";
    int red=100,green=100,blue=100;
    TextView t;
    RadioButton r;
    int seat;
    ScrollView sv;
    LinearLayout ll;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_route);
        Bundle bundle = getIntent().getExtras();
        routes = bundle.getString("routes");
        destLat=bundle.getDouble("destLat");
        destLon=bundle.getDouble("destLon");
        sourceLat=bundle.getDouble("sourceLat");
        sourceLon=bundle.getDouble("sourceLon");
        seat=bundle.getInt("seats");
        ll=new LinearLayout(this);
        user_name=bundle.getString("uname");
        sv=(ScrollView)findViewById(R.id.scrollView2);
        selectRoute=(Button) findViewById(R.id.button5);
        t=(TextView) findViewById(R.id.textView);
        selectRoute.setOnClickListener(this);
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

                red=red+10;
                green=green-100;
                blue=blue+150;

                if(red<0 || red>255)
                    red=100;

                if(green<0 || green>255)
                    green=100;

                if(blue<0 || blue>255)
                    blue=100;
                lineOptions.color(Color.rgb(red,green,blue));

                r=new RadioButton(this);
                r.setText("Select this route"+red);
                r.setTextColor(Color.rgb(red, green, blue));
                r.setTag(route_id);
                ll.addView(r);

                googleMap.moveCamera(CameraUpdateFactory.newLatLng(points.get(0)));
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                googleMap.addPolyline(lineOptions);

            }

        }

    String route_id="";
    private void splitRoutes() {

        int j=0;
        String allRoutes[]=routes.split(" ");
        System.out.println("Routes returned:::========================="+Arrays.toString(allRoutes));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_route, menu);
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
        AsyncSearchRouteWS task1 = new AsyncSearchRouteWS();
        //Call execute
        task1.execute();

    }

    String res="";
    private class AsyncSearchRouteWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            //Invoke webservice
            int rid=Integer.parseInt(route_id);
            System.out.println("route id======================================="+rid);
            String area=getAddress(sourceLat,sourceLon);
            res = RouteSelect.selectRoute(rid,seat,area,user_name);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            t.setText(res);
            //startActivity(intent);
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
