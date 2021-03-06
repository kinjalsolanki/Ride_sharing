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
public class LeaveRideCall
{
    private static String NAMESPACE = "http://tempuri.org/";
    private static String URL = "http://rideshare.somee.com/Service1.asmx";
    private static String SOAP_ACTION = "http://tempuri.org/";
    private static String webMethName="leaveRide";

    public static String leaveRide(int Rid,String uname)
    {

        SoapObject request = new SoapObject(NAMESPACE, webMethName);
        String res="";

        PropertyInfo addUser = new PropertyInfo();
        addUser.setName("Uname");
        addUser.setValue(uname);
        addUser.setType(String.class);
        request.addProperty(addUser);

        PropertyInfo addId = new PropertyInfo();
        addId.setName("Rid");
        addId.setValue(Rid);
        addId.setType(int.class);
        request.addProperty(addId);

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

