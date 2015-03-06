package com.example.general.rideshare;

import com.google.android.gms.maps.model.LatLng;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Set;

import static java.lang.Math.round;

/**
 * Created by General on 2/22/2015.
 */
public class RouteSearchCall
{
    private static String NAMESPACE = "http://tempuri.org/";
    private static String URL = "http://rideshare.somee.com/Service1.asmx";
    private static String SOAP_ACTION = "http://tempuri.org/";
    private static String webMethName="returnRouteSD";

    public static String returnRouteSD(String a)
    {
        // Create request

      /*  double slat=source.latitude;
        double slon=source.longitude;

        double dlat=destination.latitude;
        double dlon=destination.longitude;

        DecimalFormat formatter = new DecimalFormat("0.000");
        formatter.setRoundingMode(RoundingMode.DOWN);
        slat=Double.parseDouble(formatter.format(slat));
        slon=Double.parseDouble(formatter.format(slon));
        dlat=Double.parseDouble(formatter.format(dlat));
        dlon=Double.parseDouble(formatter.format(dlon));
        //System.out.println("-----------------------------------------"+slat);

        source=new LatLng(slat,slon);
        destination=new LatLng(dlat,dlon);

        System.out.println("-----------------------------------------"+source.toString()+"---------------------------"+destination.toString());

        SoapObject request = new SoapObject(NAMESPACE, webMethName);
        String res="";
        PropertyInfo addSource = new PropertyInfo();
        addSource.setName("Source");
        addSource.setValue(source.toString());
        addSource.setType(String.class);
        request.addProperty(addSource);

        PropertyInfo addDest = new PropertyInfo();
        addDest.setName("Dest");
        addDest.setValue(destination.toString());
        addDest.setType(String.class);
        request.addProperty(addDest);*/

        SoapObject request = new SoapObject(NAMESPACE, webMethName);
        String res="";

        PropertyInfo addArea = new PropertyInfo();
        addArea.setName("Area");
        addArea.setValue(a);
        addArea.setType(String.class);
        request.addProperty(addArea);

       // System.out.println(source.toString()+"------------------------------"+destination.toString());
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

        try {
            androidHttpTransport.call(SOAP_ACTION+webMethName, envelope);
            Object response = envelope.getResponse();
            //SoapPrimitive r=(SoapPrimitive) response.get;
            //res=response.getProperty(0).toString();
            res=response.toString();
           // System.out.println("result from api call========================="+res);

        } catch (Exception e) {
            res="Not done";
            e.printStackTrace();
        }
        return res;
    }

}

