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
public class RouteCreationCall
{
    private static String NAMESPACE = "http://tempuri.org/";
    private static String URL = "http://rideshare.somee.com/Service1.asmx";
    private static String SOAP_ACTION = "http://tempuri.org/";
    private static String webMethName="createRoute";

    public static String createRoute(ArrayList<LatLng> points,LatLng source, LatLng destination,String userid,int seat)
    {
        // Create request


        SoapObject request = new SoapObject(NAMESPACE, webMethName);
        String res="jhghj";
        PropertyInfo addUserid = new PropertyInfo();
        addUserid.setName("User_id");
        addUserid.setValue(userid);
        addUserid.setType(String.class);
        request.addProperty(addUserid);


        PropertyInfo addSource = new PropertyInfo();
        addSource.setName("Source_id");
        addSource.setValue(source.toString());
        addSource.setType(String.class);
        request.addProperty(addSource);

        PropertyInfo addDest = new PropertyInfo();
        addDest.setName("Dest_id");
        addDest.setValue(destination.toString());
        addDest.setType(String.class);
        request.addProperty(addDest);

        PropertyInfo addLocation = new PropertyInfo();
        addLocation.setName("Locations");
        addLocation.setValue(points.toString());
        addLocation.setType(String.class);
        request.addProperty(addLocation);

        PropertyInfo addSeats = new PropertyInfo();
        addSeats.setName("No_of_seats");
        addSeats.setValue(seat);
        addSeats.setType(int.class);
        request.addProperty(addSeats);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        System.out.println("button clicked----ishq sava-----------------------------------------------------------------------------------------------------------");

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
