﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Services;
using System.Data.SqlClient;

[WebService(Namespace = "http://tempuri.org/")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
// To allow this Web Service to be called from script, using ASP.NET AJAX, uncomment the following line. 
// [System.Web.Script.Services.ScriptService]

public class Service1 : System.Web.Services.WebService
{
    public Service1()
    {

        //Uncomment the following line if using designed components 
        //InitializeComponent(); 
    }

    [WebMethod]
    public string HelloWorld()
    {
        return "Hello World";
    }
    [WebMethod]
    public int Multiplication(int a, int b)
    {
        return (a * b);
    }
    [WebMethod]
     public int createRoute(String User_id, String Source_id, String Dest_id, String Locations, int No_of_seats)
     {
         String connectionString = "Data Source=.\\SQLEXPRESS;AttachDbFilename=C:\\Users\\Boss\\Documents\\Visual Studio 2010\\Projects\\WebService1\\WebService1\\App_Data\\Database1.mdf;Integrated Security=True;User Instance=True;";
         SqlConnection scon = new SqlConnection(connectionString);
         scon.Open();
         String ss = "INSERT into route_info (user_id,source_id,dest_id,locations,no_of_seats) VALUES('" + User_id + "','" + Source_id + "','" + Dest_id + "','" + Locations + "','" + No_of_seats + "')";
         SqlCommand cmd1 = new SqlCommand(ss, scon);
         cmd1.ExecuteNonQuery();
         scon.Close();
         return 0;
     }
}