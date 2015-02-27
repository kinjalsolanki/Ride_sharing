package com.example.general.rideshare;

import com.google.android.gms.maps.model.LatLng;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by General on 2/22/2015.
 */
public class RouteSearchCall
{
    private static String NAMESPACE = "http://tempuri.org/";
    private static String URL = "http://rideshare.somee.com/Service1.asmx";
    private static String SOAP_ACTION = "http://tempuri.org/";
    private static String webMethName="returnRouteSD";

    public static String returnRouteSD(LatLng source, LatLng destination)
    {
        // Create request


        SoapObject request = new SoapObject(NAMESPACE, webMethName);
        String res="jhghj";
        PropertyInfo addSource = new PropertyInfo();
        addSource.setName("Source");
        addSource.setValue(source.toString());
        addSource.setType(String.class);
        request.addProperty(addSource);

        PropertyInfo addDest = new PropertyInfo();
        addDest.setName("Dest");
        addDest.setValue(destination.toString());
        addDest.setType(String.class);
        request.addProperty(addDest);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

        try {
            androidHttpTransport.call(SOAP_ACTION+webMethName, envelope);
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            res=response.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

}

