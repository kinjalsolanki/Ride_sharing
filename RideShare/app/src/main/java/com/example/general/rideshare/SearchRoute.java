package com.example.general.rideshare;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class SearchRoute extends ActionBarActivity {

    TextView tview;
    String routes;
    String area;
    List<LatLng> points;
    LatLng location;
    GoogleMap googleMap;
    double destLat, destLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_route);
        Bundle bundle = getIntent().getExtras();
        routes = bundle.getString("routes");
        destLat=bundle.getDouble("destLat");
        destLon=bundle.getDouble("destLon");
        //area=bundle.getString("area");
        //System.out.println("In searchroute:====================="+area);
        //tview=(TextView)findViewById(R.id.textView);
        //tview.setText(routes);
        createMapView();

        googleMap.setMyLocationEnabled(true);

        splitRoutes();

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


    private void splitRoutes() {

        String allRoutes[]=routes.split(" ");
        System.out.println("Routes returned:::========================="+Arrays.toString(allRoutes));
        for(int i=0;i<allRoutes.length;i++)
        {
            System.out.println("inside foe================================="+i);
            points=PolyUtil.decode(allRoutes[i].substring(1));
            System.out.println("Decoded roue=================================="+i+"============="+points);
           /* String routesArray[]=routes.split("lat/lng:");
            System.out.println("1-----------------"+Arrays.toString(routesArray));

            for(int j=1;j<routesArray.length-1;j++)
            {
                String singleLocation[]=routesArray[j].split(",");
                System.out.println("2-----------------"+Arrays.toString(singleLocation)+"----------------"+singleLocation.length);

                    if(singleLocation.length>=2) {
                        String lat = singleLocation[0].substring(2);
                        String lon="";
                        if(j!=routesArray.length-1)
                            lon = singleLocation[1].substring(0, singleLocation[1].length() - 1);
                        else
                            lon = singleLocation[1].substring(0, singleLocation[1].length() - 3);
                        System.out.println("4Latitude-------------------"+lat+"Longi------------------"+lon);

                        double l1=0,l2=0;
                        if(lat.matches("[0-9]{1,13}(\\.[0-9]*)?")) {
                            l1=Double.parseDouble(lat);
                        }

                        if(lon.matches("[0-9]{1,13}(\\.[0-9]*)?")) {
                            l2=Double.parseDouble(lon);

                        }
                        if(l1!=0 && l2!=0) {
                            location = new LatLng(l1, l2);
                            points.add(location);
                        }
                }

            }
*/
            LatLng l=new LatLng(destLat,destLon);

            if(PolyUtil.isLocationOnPath(l,points,true,100))
                getPath();
            //System.out.println("extracted point===================================="+i+"============"+points.toString());

        }

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
}
