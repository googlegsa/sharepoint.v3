using System;
using System.Data;
using System.Configuration;
using System.Web;
using System.Web.Security;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Web.UI.WebControls.WebParts;
using System.Web.UI.HtmlControls;

using System.IO;
using System.Web.Configuration;
using System.Security.Cryptography.X509Certificates;
using System.Net;
using System.Security.Principal;
using System.Collections.Specialized;

public partial class _Default : System.Web.UI.Page
{
    //Print the search Information
    protected void Page_Load(object sender, EventArgs e)
    {
        ////////////////////////////////////////////
        //lblCurrentUser.Text = WindowsIdentity.GetCurrent().Name;
        //lblLoginUser.Text = Request.ServerVariables["LOGON_USER"];


        //Response.Write("<span style=\"text-decoration: underline\"><strong>Current Logged User:</strong></span>");
        //Response.Write(Request.ServerVariables["LOGON_USER"]);
        //Response.Write("<br/>");
        //Response.Write("<hr/>");
        //Response.Write("<br/>");

        Response.Write("<strong><span style=\"text-decoration: underline\">Current Process User:</span></strong>");
        Response.Write("  "+WindowsIdentity.GetCurrent().Name);
        Response.Write("<br/>");
        //Response.Write("<hr/>");
        Response.Write("<br/>");


        //PrintConfigurationParameters();
        PrintHeaders();
        PrintCookies();

        ////////////////////////////////////////////
        Response.Write("<strong><span style=\"text-decoration: underline\">Request Query String Parameters:</span></strong>");
        Response.Write("<br/>");
        Response.Write("<br/>");
        NameValueCollection nvc = HttpContext.Current.Request.QueryString;//print the request query string
        if ((null != nvc) && (nvc.Count > 0))
        {
            string tblRequest = "<table border='1'>";
            tblRequest += "<tr>";
            tblRequest += "<th>Request Key</th>";
            tblRequest += "<th>Request Value</th>";
            tblRequest += "</tr>";

            for (int xx = 0; xx < nvc.Count; ++xx)
            {
                tblRequest += "<tr>";
                tblRequest += "<td>" + nvc.GetKey(xx) + "</td>";
                tblRequest += "<td>" + nvc[xx] + "</td>";
                tblRequest += "</tr>";
            }

            tblRequest += "</table>";
            Response.Write(tblRequest);
            Response.Write("<br/>");
            Response.Write("<br/>");
        }
    }
    private void PrintConfigurationParameters()
    {
        //lblAppconfig.Text = "<h4><u>Application Configuration Parameters:</u></h4>";
        Response.Write("<strong><span style=\"text-decoration: underline\">Application Configuration Parameters:</span></strong>");
        Response.Write("<br/>");
        Response.Write("<br/>");

        if (null != WebConfigurationManager.AppSettings)
        {
            string tblRequest = "<table border='1'>";
            tblRequest += "<tr>";
            tblRequest += "<th>Configuration Key</th>";
            tblRequest += "<th>Configuration Value</th>";
            tblRequest += "</tr>";

            string[] keys = WebConfigurationManager.AppSettings.AllKeys;
            foreach (string key in keys)
            {
                tblRequest += "<tr>";
                tblRequest += "<td>" + key + "</td>";
                tblRequest += "<td>" + WebConfigurationManager.AppSettings[key] + "</td>";
                tblRequest += "</tr>";
            }

            tblRequest += "</table>";
            Response.Write(tblRequest);
            Response.Write("<br/>");
            Response.Write("<br/>");
        }
    }

    private void PrintHeaders()
    {
        //alignment for the table
        Response.Write("<strong><span style=\"text-decoration: underline\">Request Headers:</span></strong>");
        Response.Write("<br/>");
        Response.Write("<br/>");
        string[] requestHeaderKeys = HttpContext.Current.Request.Headers.AllKeys;
        //lblRequestHeader.Text = "";
        if (null != HttpContext.Current.Request.Headers)
        {
            string tblRequest = "<table border='1'>";
            tblRequest += "<tr>";
            tblRequest += "<th>Name</th>";
            tblRequest += "<th>Value</th>";
            tblRequest += "</tr>";

            for (int i = 0; i < HttpContext.Current.Request.Headers.Count; i++)
            {
                try
                {
                    tblRequest += "<tr>";
                    tblRequest += "<td>" + requestHeaderKeys[i] + "</td>";
                    tblRequest += "<td>" + HttpContext.Current.Request.Headers[requestHeaderKeys[i]] + "</td>";
                    tblRequest += "</tr>";
                }
                catch (Exception)
                { //just skipping the header information if any exception occures while adding to the GSA request
                }


            }
            tblRequest += "</table>";
            Response.Write(tblRequest);
            Response.Write("<br/>");
            Response.Write("<br/>");

        }
    }


    private void PrintCookies()
    {
        Response.Write("<strong><span style=\"text-decoration: underline\">Request Cookies:</span></strong>");
        if (null != HttpContext.Current.Request.Cookies)
        {
            Response.Write("<h4><u>Total Cookies:</u> " + HttpContext.Current.Request.Cookies.Count + "</h4>");
            //Response.Write("<br/>");
            Response.Write("<br/>");
            int countcookies = HttpContext.Current.Request.Cookies.Count;
            if (countcookies > 0)
            {
                string tblRequest = "<table border='1'>";
                tblRequest += "<tr>";
                tblRequest += "<th>Cookie Name</th>";
                tblRequest += "<th>Cookie Value</th>";
                tblRequest += "<th>Expires On</th>";
                tblRequest += "</tr>";
                for (int cc = 0; cc < countcookies; ++cc)
                {
                    HttpCookie c = HttpContext.Current.Request.Cookies[cc];
                    if (null != c)
                    {
                        tblRequest += "<tr>";
                        tblRequest += "<td>" + c.Name + "</td>";
                        tblRequest += "<td>" + c.Value + "</td>";
                        tblRequest += "<td>" + c.Expires.ToString() + "</td>";
                        tblRequest += "</tr>";
                    }
                }

                tblRequest += "</table>";
                Response.Write(tblRequest);
                Response.Write("<br/>");
                Response.Write("<br/>");
            }
        }
    }
}
