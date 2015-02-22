package com.example.general.webservicecalldemo;


import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

/**
 * Created by General on 2/22/2015.
 */
public class WebSerCallAgain
{
    private static String NAMESPACE = "http://tempuri.org/";
    //Webservice URL - It is asmx file location hosted in the server in case of .Net
    //Change the IP address to your machine IP address
    private static String URL = "http://rideshare.somee.com/Service.asmx";
    //SOAP Action URI again http://tempuri.org
    private static String SOAP_ACTION = "http://tempuri.org/";
    private static String webMethName="Multiplication";

    public static String invokeHelloWorldWS(int n1,int n2)
    {
        String resTxt = null;
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);
        // Property which holds input parameters
        PropertyInfo sayHelloPI = new PropertyInfo();
        // Set Name
        sayHelloPI.setName("a");
        // Set Value
        sayHelloPI.setValue(n1);
        // Set dataType
        sayHelloPI.setType(int.class);
        // Add the property to request object
        request.addProperty(sayHelloPI);


        PropertyInfo sayHelloPI1 = new PropertyInfo();
        // Set Name
        sayHelloPI1.setName("b");
        // Set Value
        sayHelloPI1.setValue(n2);
        // Set dataType
        sayHelloPI1.setType(int.class);
        // Add the property to request object
        request.addProperty(sayHelloPI1);

        System.out.println("okay");

        // Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        //Set envelope as dotNet
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

        try {
            // Invoke web service
            androidHttpTransport.call(SOAP_ACTION+webMethName, envelope);
            // Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            // Assign it to resTxt variable static variable
            resTxt = response.toString();

        } catch (Exception e) {
            //Print error
            e.printStackTrace();
            //Assign error message to resTxt
            resTxt = "Error occured";
        }
        //Return resTxt to calling object
        return resTxt;
    }
}
