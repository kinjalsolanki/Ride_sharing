using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Services;
using System.Data.SqlClient;
using System.Data;
using System.Configuration;
using System.Net;
using System.IO;
using System.Text;

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
    public String createRoute(String User_id, String Source_id, String Dest_id, String Locations, int No_of_seats, String Areas, String Reg_id)
    {
        //String connectionString = "Data Source=.\\SQLEXPRESS;AttachDbFilename=C:\\Users\\Boss\\Documents\\Visual Studio 2010\\Projects\\WebService1\\WebService1\\App_Data\\Database1.mdf;Integrated Security=True;User Instance=True;";
        //SqlConnection scon = new SqlConnection(connectionString);
        SqlConnection scon = new SqlConnection(ConfigurationManager.ConnectionStrings["RouteDatabase"].ConnectionString);
        scon.Open();
        String ss = "INSERT into route_info (user_id,source_id,dest_id,locations,no_of_seats,areas,reg_id,other_users,current_location) VALUES('" + User_id + "','" + Source_id + "','" + Dest_id + "','" + Locations + "','" + No_of_seats + "','" + Areas + "','" + Reg_id + "','-','"+Source_id+"')";
        SqlCommand cmd1 = new SqlCommand(ss, scon);
        cmd1.ExecuteNonQuery();
        System.Diagnostics.Debug.WriteLine("-----------------Areas--------------" + Areas);
        scon.Close();
        return "Route Created";
    }

    public void SendNotification(string deviceId, string message)
    {
        string GoogleAppID = "AIzaSyDDEO1ceYkLYA0O309cPOZ27ceBky9KU-Q";
        var SENDER_ID = "762985634560";
        var value = message;
        WebRequest tRequest;
        tRequest = WebRequest.Create("https://android.googleapis.com/gcm/send");
        tRequest.Method = "post";
        tRequest.ContentType = " application/x-www-form-urlencoded;charset=UTF-8";
        tRequest.Headers.Add(string.Format("Authorization: key={0}", GoogleAppID));

        tRequest.Headers.Add(string.Format("Sender: id={0}", SENDER_ID));

        string postData = "collapse_key=score_update&time_to_live=1000&delay_while_idle=0&data.message=" + value + "&data.time=" + System.DateTime.Now.ToString() + "&registration_id=" + deviceId + "";
        Console.WriteLine(postData);
        Byte[] byteArray = Encoding.UTF8.GetBytes(postData);
        tRequest.ContentLength = byteArray.Length;

        Stream dataStream = tRequest.GetRequestStream();
        dataStream.Write(byteArray, 0, byteArray.Length);
        dataStream.Close();

        WebResponse tResponse = tRequest.GetResponse();

        dataStream = tResponse.GetResponseStream();

        StreamReader tReader = new StreamReader(dataStream);

        String sResponseFromServer = tReader.ReadToEnd();
        System.Diagnostics.Debug.WriteLine(sResponseFromServer);


        tReader.Close();
        dataStream.Close();
        tResponse.Close();

    }

    [WebMethod]
    public string returnOwnerRoutes(String Uname)
    {
        SqlConnection scon = new SqlConnection(ConfigurationManager.ConnectionStrings["RouteDatabase"].ConnectionString);
        scon.Open();
        String list = "select * from route_info where user_id like '%" + Uname + "%'";
        String loc = "";
        SqlCommand command1 = new SqlCommand(list, scon);
        SqlDataReader DR1 = command1.ExecuteReader();
        while (DR1.Read())
        {
            loc = loc + DR1["route_id"].ToString() + "|" + DR1["source_id"].ToString() + "|" + DR1["dest_id"].ToString() + "|" + DR1["no_of_seats"].ToString() + "*";
        }
        scon.Close();
        DR1.Close();
        return loc;
    }


    [WebMethod]
    public void endRide(int Rid)
    {
        SqlConnection scon = new SqlConnection(ConfigurationManager.ConnectionStrings["RouteDatabase"].ConnectionString);
        scon.Open();
        String command = "delete from route_info where route_id=" + Rid;
        SqlCommand command1 = new SqlCommand(command, scon);
        command1.ExecuteNonQuery();
        scon.Close();
    }

    [WebMethod]
    public void leaveRide(int Rid,String Uname)
    {
        SqlConnection scon = new SqlConnection(ConfigurationManager.ConnectionStrings["RouteDatabase"].ConnectionString);
        scon.Open();
        String user="";
        String command = "select other_users from route_info where route_id="+Rid;
        SqlCommand command1 = new SqlCommand(command, scon);
        SqlDataReader DR1 = command1.ExecuteReader();
        while (DR1.Read())
        {
            user = DR1["other_users"].ToString();  
        }
        DR1.Close();

        String[] u = user.Split(',');
        user = "";
        int count = 1;
        for (int i = 0; i < u.Length; i++)
        {
            if (!u[i].Equals(Uname))
            {
                user = user + u[i] + ",";
                count++;
            }
        }

        if (user.Equals(""))
            user = "--";

        command = "update route_info set other_users='" + user.Substring(0,user.Length-1) + "', no_of_seats="+(3-count)+" where route_id=" + Rid;
        command1 = new SqlCommand(command, scon);
        command1.ExecuteNonQuery();
        
        scon.Close();
    }


    [WebMethod]
    public String RecommendedRoute()
    {
        SqlConnection scon = new SqlConnection(ConfigurationManager.ConnectionStrings["RouteDatabase"].ConnectionString);
        scon.Open();
        String list = "SELECT m_area, count(*) as m_count FROM mine_info GROUP BY m_area ORDER BY m_count DESC";
        SqlCommand command = new SqlCommand(list, scon);
        SqlDataReader DR = command.ExecuteReader();
        int dno = 2;
        String ms = "";
       /* String[] area_array = new String[3];
        while (DR.Read() && dno >= 0)
        {
            area_array[dno] = DR["m_area"].ToString();
            dno--;
        }
        DR.Close();
        dno = 2;
        while (dno >= 0 && area_array[dno] != null)
        {
            String list1 = "select distinct m_area, m_pin from mine_info where m_area='" + area_array[dno] + "'";
            SqlCommand command1 = new SqlCommand(list1, scon);
            SqlDataReader DR1 = command1.ExecuteReader();
            while (DR1.Read())
            {
                ms = ms + DR1["m_area"] + "|" + DR1["m_pin"] + ",";

            }
            dno--;
            DR1.Close();
        }*/
        while (DR.Read() && dno >= 0)
        {
            ms = ms + DR["m_area"] + ",";
            dno--;
        }

        DR.Close();
        scon.Close();
        return ms;
    }
    


    [WebMethod]
    public String selectRoute(int RouteId, int Seats, String Uname, String Area)
    {
        SqlConnection scon = new SqlConnection(ConfigurationManager.ConnectionStrings["RouteDatabase"].ConnectionString);
        scon.Open();
        String[] la = Area.Split('|');
        int pin = Int32.Parse(la[1]);
        String list1 = "insert into mine_info (m_area,m_pin) values ('"+la[0]+"','"+pin+"')";
        SqlCommand command2 = new SqlCommand(list1, scon);
        SqlDataReader DR1 = command2.ExecuteReader();
        DR1.Close();

        String list = "select no_of_seats,reg_id,other_users from route_info where route_id=" + RouteId;
        int seats = 3;
        String reg_id = "";
        String users = "";
        SqlCommand command1 = new SqlCommand(list, scon);
        SqlDataReader DR = command1.ExecuteReader();
        while (DR.Read())
        {
            seats = Int32.Parse(DR["no_of_seats"].ToString());
            reg_id = DR["reg_id"].ToString();
            users = DR["other_users"].ToString();
        }
        DR.Close();

        if (users == "-")
        {
            list = "update route_info set other_users='" + Uname + "' where route_id=" + RouteId;
        }
        else
        {
            list = "update route_info set other_users='" + users+","+Uname + "' where route_id=" + RouteId;
        }

        command1 = new SqlCommand(list, scon);
        command1.ExecuteNonQuery();

        seats = seats - Seats;

        SendNotification(reg_id, "test notification..........yooo");

        list = "update route_info set no_of_seats=" + seats + "where route_id=" + RouteId;
        command1 = new SqlCommand(list, scon);
        command1.ExecuteNonQuery();

        scon.Close();
        return "Route Registered!";
    }

    [WebMethod]
    public string returnCurrentLocation(int Rid)
    {
        SqlConnection scon = new SqlConnection(ConfigurationManager.ConnectionStrings["RouteDatabase"].ConnectionString);
        scon.Open();
        String list = "select * from route_info where route_id="+Rid;
        String loc = "";
        SqlCommand command1 = new SqlCommand(list, scon);
        SqlDataReader DR1 = command1.ExecuteReader();
        while (DR1.Read())
        {
            loc = DR1["current_location"] + " "+DR1["locations"];
        }
        scon.Close();
        DR1.Close();
        return loc;
    }


    [WebMethod]
    public string returnMyRoutes(String Uname)
    {
        SqlConnection scon = new SqlConnection(ConfigurationManager.ConnectionStrings["RouteDatabase"].ConnectionString);
        scon.Open();
        String list = "select * from route_info where other_users like '%" + Uname + "%'";
        String loc = "";
        SqlCommand command1 = new SqlCommand(list, scon);
        SqlDataReader DR1 = command1.ExecuteReader();
        while (DR1.Read())
        {
            loc = loc + DR1["route_id"].ToString() +"|"+ DR1["source_id"].ToString() + "|"+DR1["dest_id"].ToString()+"|"+DR1["no_of_seats"].ToString()+"*";
        }
        scon.Close();
        DR1.Close();
        return loc;
    }


    [WebMethod]
    public void updateCurrentLoc(String Loc, String Uname)
    {
        SqlConnection scon = new SqlConnection(ConfigurationManager.ConnectionStrings["RouteDatabase"].ConnectionString);
        scon.Open();
        String list = "update route_info set current_location='"+Loc+"' where user_id='"+Uname+"'";
        SqlCommand command1 = new SqlCommand(list, scon);
        command1.ExecuteNonQuery();
        scon.Close();

    }

    [WebMethod]
    public string returnRouteSD(String Areas, int Seats)
    {
        SqlConnection scon = new SqlConnection(ConfigurationManager.ConnectionStrings["RouteDatabase"].ConnectionString);
        scon.Open();
        String list = "select * from route_info where areas like '%" + Areas + "%' and no_of_seats >=" + Seats;
        String loc = "";
        SqlCommand command1 = new SqlCommand(list, scon);
        SqlDataReader DR1 = command1.ExecuteReader();
        while (DR1.Read())
        {
            loc = loc + DR1["route_id"].ToString() + DR1["locations"].ToString() + " ";
        }
        DR1.Close();
        scon.Close();
        System.Diagnostics.Debug.WriteLine("-----------------Areas--------------" + loc);

        //loc = loc.Substring(0,loc.Length-1);
        return loc;
    }
}