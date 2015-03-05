using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Services;
using System.Data.SqlClient;
using System.Data;
using System.Configuration;

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
     public String createRoute(String User_id, String Source_id, String Dest_id, String Locations, int No_of_seats)
     {
         //String connectionString = "Data Source=.\\SQLEXPRESS;AttachDbFilename=C:\\Users\\Boss\\Documents\\Visual Studio 2010\\Projects\\WebService1\\WebService1\\App_Data\\Database1.mdf;Integrated Security=True;User Instance=True;";
         //SqlConnection scon = new SqlConnection(connectionString);
         SqlConnection scon = new SqlConnection(ConfigurationManager.ConnectionStrings["RouteDatabase"].ConnectionString);
         scon.Open();
         String ss = "INSERT into route_info (user_id,source_id,dest_id,locations,no_of_seats) VALUES('" + User_id + "','" + Source_id + "','" + Dest_id + "','" + Locations + "','" + No_of_seats + "')";
         SqlCommand cmd1 = new SqlCommand(ss, scon);
         cmd1.ExecuteNonQuery();
         scon.Close();
         return "Route Created";
     }
    [WebMethod]
    public string returnRouteSD(String Source, String Dest)
    {
        String[] Sour = Source.Split(',');
        String[] Desti = Dest.Split(',');

       // String connectionString = "Data Source=.\\SQLEXPRESS;AttachDbFilename=C:\\Users\\Boss\\Documents\\Visual Studio 2010\\Projects\\WebService1\\WebService1\\App_Data\\Database1.mdf;Integrated Security=True;User Instance=True;";
        //SqlConnection scon = new SqlConnection(connectionString);
        SqlConnection scon = new SqlConnection(ConfigurationManager.ConnectionStrings["RouteDatabase"].ConnectionString);
        scon.Open();
        String list = "select * from route_info where source_id like'" + Sour[0] + "%," + Sour[1].Substring(0, Sour[1].Length - 1) + "%' and dest_id like'" + Desti[0] + "%," + Desti[1].Substring(0, Desti[1].Length - 1) + "%'";
        SqlCommand command = new SqlCommand(list, scon);
        SqlDataReader DR = command.ExecuteReader();
        String loc = "";
        while (DR.Read())
        {
            loc = DR["no_of_seats"].ToString()+DR["locations"].ToString()+"|";
        }
        DR.Close();
        String list1 = "select * from route_info where locations like '%" + Sour[0] + "%," + Sour[1].Substring(0, Sour[1].Length - 1) + "%' and dest_id like'" + Desti[0] + "%," + Desti[1].Substring(0, Desti[1].Length - 1) + "%'";
        SqlCommand command1 = new SqlCommand(list1, scon);
        SqlDataReader DR1 = command1.ExecuteReader();
        while (DR1.Read())
        {
            loc = loc + DR1["no_of_seats"].ToString() + DR1["locations"].ToString()+"|";
        }
        DR1.Close();
        String list2 = "select * from route_info where locations like '%" + Sour[0] + "%," + Sour[1].Substring(0, Sour[1].Length - 1) + "%' and locations like'%" + Desti[0] + "%," + Desti[1].Substring(0, Desti[1].Length - 1) + "%'";
        SqlCommand command2 = new SqlCommand(list2, scon);
        SqlDataReader DR2 = command2.ExecuteReader();
        while (DR2.Read())
        {
            loc = loc + DR2["no_of_seats"].ToString() + DR2["locations"].ToString()+"|";
        }
        DR2.Close();
        String list3 = "select * from route_info where source_id like '%" + Sour[0] + "%," + Sour[1].Substring(0, Sour[1].Length - 1) + "%' and locations like'%" + Desti[0] + "%," + Desti[1].Substring(0, Desti[1].Length - 1) + "%'";
        SqlCommand command3 = new SqlCommand(list3, scon);
        SqlDataReader DR3 = command3.ExecuteReader();
        while (DR3.Read())
        {
            loc = loc + DR3["no_of_seats"].ToString() + DR3["locations"].ToString() + "|";
        }
        DR3.Close();
        scon.Close();
        loc = loc.Substring(0,loc.Length-1);
        return loc;
    }
}