package com.example.general.rideshare;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

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
public class FareShareCall
{
    private static String NAMESPACE = "http://tempuri.org/";
    private static String URL = "http://rideshare.somee.com/Service1.asmx";
    private static String SOAP_ACTION = "http://tempuri.org/";
    private static String webMethName="fareShare";

    public static String fareShare(String type, Double distance)
    {
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);
        String res="jhghj";
        PropertyInfo addType = new PropertyInfo();
        addType.setName("Type");
        addType.setValue(type);
        addType.setType(String.class);
        request.addProperty(addType);

        PropertyInfo addDist = new PropertyInfo();
        addDist.setName("Dist");
        addDist.setValue(distance.toString());
        addDist.setType(Double.class);
        request.addProperty(addDist);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object
        //System.out.println("button clicked--ishqsava-----------------------------------------------------------------------------------------------------------");


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