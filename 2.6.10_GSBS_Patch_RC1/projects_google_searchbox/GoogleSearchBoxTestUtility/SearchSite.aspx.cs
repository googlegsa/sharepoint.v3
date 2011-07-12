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
        Response.Write("<strong>Search User Identity:</strong>");
        Response.Write("  " + WindowsIdentity.GetCurrent().Name);
        Response.Write("<br/>");
        Response.Write("<br/>");

        PrintHeaders();
        PrintDetectedAuthNScheme();
        PrintCookies();

        ////////////////////////////////////////////
        Response.Write("<strong>Request <a href='http://code.google.com/apis/searchappliance/documentation/62/xml_reference.html#request_parameters'>Query String</a> Parameters:</strong>");
        Response.Write("<br/>");
        Response.Write("<br/>");
        NameValueCollection nvc = HttpContext.Current.Request.QueryString;//print the request query string
        if ((null != nvc) && (nvc.Count > 0))
        {
            //string tblRequest = "<table border='1'>";
            string tblRequest = "<TABLE WIDTH=\"1100\" BORDER='1' CELLSPACING='2' CELLPADDING='2' style='width:100%'";
            tblRequest += "<tr>";
            tblRequest += "<th><b>Request Key</b></th>";
            tblRequest += "<th><b>Request Value</b></th>";
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

        /**
         * Logic for detection : Need to detect whether the user has opened the test utility 
         * from browser or from the search box.
         * 
         * If opened from the Browser suggest user how to associate it with search box
         **/
        string SearchQuery = Request.Params["q"];
        if (null == SearchQuery)
        {
            Response.Write("To use 'Test Utility' from Search Box, Enter the 'Test Utility' URL under 'Appliance URL' of SharePoint web application");
        }
       

    }

    /// <summary>
    /// Prints the detected authN scheme and the suggested setting in GSA
    /// </summary>
    private void PrintDetectedAuthNScheme()
    {
            /**
             * Check for the Kerberos. 
             * Logic: if we get "Negotiate" as Authorization Header with "TIRM" as initials then it is NTLM internally
             **/

            string AuthorizationHeader = HttpContext.Current.Request.Headers["Authorization"];

            /**
             * Sample value: 
             * =============
             * Negotiate YIIFcwYGKwYBBQUCoIIFZzCCBWOgJDAiBgkqhkiC9xIBAgIGCSqGSIb3EgECAgYKK (correct)
             * Negotiate TlRMFcwYGKwYBBQUCoIIFZzCCBWOgJDAiBgkqhkiC9xIBAgIGCSqGSIb3EgECAgYKK
             **/
            if (null != AuthorizationHeader)
            {
                

                String[] values = AuthorizationHeader.Split(' ');
                if ((null != values) && values.Length >= 2)
                {
                    /**
                     * Logic: 
                     * =========
                     * If you get NTLM, suggest user then use the SharePoint link for 'meta-url' feed.
                     * If you get Kerberos\negotiate then use the SharePoint link for 'content' feed.
                     **/
                    const String SHAREPOINT_CONTENT_URL = "<a href='http://code.google.com/apis/searchappliance/documentation/connectors/200/connector_admin/sharepoint_content_connector.html'>'Google SharePoint connector'</a>";
                    const String SHAREPOINT_META_URL = "<a href='http://code.google.com/apis/searchappliance/documentation/connectors/200/connector_admin/sharepoint_meta_connector.html'>'Google SharePoint connector'</a>";
                    String SHAREPOINT_URL = SHAREPOINT_CONTENT_URL;

                    const String AUTH_HANDLING_BY_CONNECTOR = "'Authorization Handling-->By connector' (i.e. content-feed mode)";
                    const String AUTH_HANDLING_BY_APPLIANCE = "'Authorization Handling-->By appliance or SAML authorization provider' (i.e. meta-url mode)";
                    String AUTH_HANDLING = AUTH_HANDLING_BY_CONNECTOR;

                    if (null != values[1]) 
                    {
                        if(values[1].StartsWith("TlRM"))
                        {
                            //Kerberos-- Mix N Match
                            Response.Write("<strong>Detected Authentication scheme:</strong> NTLM");

                            SHAREPOINT_URL=SHAREPOINT_CONTENT_URL;
                            AUTH_HANDLING = AUTH_HANDLING_BY_CONNECTOR;
                        }
                        else
                        {
                            //Kerberos is set correctly in user's environment
                            SHAREPOINT_URL=SHAREPOINT_META_URL;
                            AUTH_HANDLING = AUTH_HANDLING_BY_APPLIANCE;
                        }

                        String message = "Please configure <a href='http://code.google.com/apis/searchappliance/documentation/50/admin/wia.html#deployment'>"
                                + "'Google SAML Bridge'</a> along with " + SHAREPOINT_URL 
                                + "with " + AUTH_HANDLING + " to be able to search the site contents.";

                        //Suggestion
                        Response.Write("<h6>Suggested Configuration:</h6>");

                        Response.Write(message);
                        Response.Write("<br/>");
                        Response.Write("<br/>");
                        Response.Write("<br/>");

                    }
                    else 
                    {
                        //handles for NTLM and Kerberos--no Mix N Match
                        Response.Write("<strong>Detected Authentication scheme:</strong> " + values[0]);
                        Response.Write("<br/>");
                        Response.Write("<br/>");
                    }

                }
                else if((null!=values) && (values[0]!=null))
                { 
                    //basic authN mechanism
                    Response.Write("<strong>Detected Authentication scheme:</strong> " + values[0]);
                    Response.Write("<br/>");
                    Response.Write("<br/>");

                }
            }
        
    }

    /// <summary>
    /// Prints the Configuration parameters of the incomming request
    /// </summary>
    private void PrintConfigurationParameters()
    {
        Response.Write("<strong>Application Configuration Parameters:</strong>");
        Response.Write("<br/>");
        Response.Write("<br/>");

        if (null != WebConfigurationManager.AppSettings)
        {
            //string tblRequest = "<table border='1'>";
            string tblRequest = "<TABLE WIDTH=\"1100\" BORDER='1' CELLSPACING='2'";
            tblRequest += "<tr>";
            tblRequest += "<th><b>Configuration Key</b></th>";
            tblRequest += "<th><b>Configuration Value</b></th>";
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
        Response.Write("<strong>Request Headers:</strong>");
        Response.Write("<br/>");
        Response.Write("<br/>");
        
        string[] requestHeaderKeys = HttpContext.Current.Request.Headers.AllKeys;

        if (null != HttpContext.Current.Request.Headers)
        {
            //string tblRequest = "<table border='1'>";
            string tblRequest = "<TABLE WIDTH=\"1100\" BORDER='1' CELLSPACING='2' CELLPADDING='2' style='width:100%'";
            tblRequest += "<tr>";
            tblRequest += "<th><b>Name</b></th>";
            tblRequest += "<th><b>Value</b></th>";
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
        Response.Write("<strong>Request Cookies:</strong>");
        if (null != HttpContext.Current.Request.Cookies)
        {
            Response.Write("<h6>Total Cookies: " + HttpContext.Current.Request.Cookies.Count + "</h6>");
            Response.Write("<br/>");
            int countcookies = HttpContext.Current.Request.Cookies.Count;
            if (countcookies > 0)
            {
                //string tblRequest = "<table border='1'>";
                string tblRequest = "<TABLE WIDTH=\"1100\" BORDER='1' CELLSPACING='2' CELLPADDING='2' style='width:100%'";
                tblRequest += "<tr>";
                tblRequest += "<th><b>Cookie Name</b></th>";
                tblRequest += "<th><b>Cookie Value</b></th>";
                tblRequest += "<th><b>Expires On</b></th>";
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
