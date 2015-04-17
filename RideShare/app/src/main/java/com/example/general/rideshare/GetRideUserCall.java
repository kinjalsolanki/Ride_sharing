package com.example.general.rideshare;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

/**
 * Created by General on 4/7/2015.
 */
public class GetRideUserCall {
    private static String NAMESPACE = "http://tempuri.org/";
    private static String URL = "http://rideshare.somee.com/Service1.asmx";
    private static String SOAP_ACTION = "http://tempuri.org/";
    private static String webMethName="getRideUsers";

    public static String getUsers(int rid)
    {
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);
        String res="jhghj";

        PropertyInfo addRid = new PropertyInfo();
        addRid.setName("RouteId");
        addRid.setValue(rid);
        addRid.setType(int.class);
        request.addProperty(addRid);

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
