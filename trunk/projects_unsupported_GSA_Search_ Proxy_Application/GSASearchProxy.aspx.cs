using System;
using System.Configuration;
using System.Data;
using System.Web;
using System.Web.Security;
using System.Web.UI;
using System.Web.UI.HtmlControls;
using System.Web.UI.WebControls;
using System.Web.UI.WebControls.WebParts;
using System.Net;
using System.IO;
using System.Web.Configuration;
using System.Security.Cryptography.X509Certificates;
using System.Xml;
using System.Xml.Xsl;
using System.Text;
using System.Xml.XPath;
using System.Security.Principal;
using System.Diagnostics;
using System.IO.Compression;
using System.Collections;


/*
    Unsupported GSA Proxy Application
    Author: Amit Agrawal
 */
public partial class GSASearchProxy : System.Web.UI.Page
{
    public string GSALocation = "";
    public string accessLevel = "a";//do a Public and Secured search 
    public string siteCollection = "";
    public string frontEnd = "";
    public string cookiedomain = "";
    public LOG_LEVEL LogLevel = LOG_LEVEL.ERROR;
    public string LogFilePath = @"c:\";

        
    /*Log levels*/
    public enum LOG_LEVEL
    {
        INFO,
        ERROR
    }


    /// <summary>
    /// Logs the proxy application messages.
    /// </summary>
    /// <param name="msg">The message to be logged</param>
    /// <param name="logLevel">Log level</param>
    public void log(String msg, LOG_LEVEL CurrentLevel)
    {
        try
        {
            /*Check the log level for screening purpose*/
            if (CurrentLevel >= LogLevel)
            {
                /*Construct logs on daily basis*/
                String time = DateTime.Today.ToString("yyyy_MM_dd");

                /*Append the log message to the file*/
                String fileName=LogFilePath+"ProxyLog_"+time+".log";
                FileStream f = new FileStream(fileName, FileMode.Append, FileAccess.Write);

                /**
                 * If we use FileLock [i.e.  f.Lock(0, f.Length)] then it may cause issue
                 * Logging failed due to: The process cannot access the file 'C:\ProxyLog_2009_12_03.log' because it is being used by another process.
                 * Thread was being aborted.
                 **/

                StreamWriter logger = new StreamWriter(f);

                logger.WriteLine("[ {0} ]  [{1}] :- {2}", DateTime.Now.ToString(), CurrentLevel, msg);
                logger.Flush();
                logger.Close();
            }
        }
        catch (Exception e)
        {
            /*handle the log file related exception*/
            HttpContext.Current.Response.Write("Got this Exception while writing to log file: " + e.Message + "<br/>");
            HttpContext.Current.Response.Write("Exception Trace: " + e.StackTrace+ "<br/>");
            HttpContext.Current.Response.End();//End the response
        }
            
    }

    /// <summary>
    /// For X.509 certificate handling. 
    /// </summary>
    /// <param name="sender"></param>
    /// <param name="cert"></param>
    /// <param name="chain"></param>
    /// <param name="error"></param>
    /// <returns></returns>
    private static bool customXertificateValidation(object sender, X509Certificate cert, X509Chain chain, System.Net.Security.SslPolicyErrors error)
    {
        return true;
    }
       

    /// <summary>
    /// This method make the HttpWebRequest to the supplied URL
    /// </summary>
    /// <param name="isAutoRedirect">Indicates whether handle request manually or automatically</param>
    /// <param name="searchURL">The URL which should be hit</param>
    /// <returns>Response of the request</returns>
    public HttpWebResponse GetResponse(Boolean isAutoRedirect, String GSASearchUrl, CookieContainer cc, HttpWebResponse ResponseForCopyingHeaders)
    {
        HttpWebRequest objReq = null;
        HttpWebResponse response = null;

        objReq = (HttpWebRequest)HttpWebRequest.Create(GSASearchUrl);
        objReq.KeepAlive = true;
        objReq.AllowAutoRedirect = isAutoRedirect;
        objReq.MaximumAutomaticRedirections = 100;
        objReq.Credentials = System.Net.CredentialCache.DefaultCredentials;//set credentials
        log("Creds: "+System.Net.CredentialCache.DefaultCredentials.ToString(),LOG_LEVEL.INFO);        

        /*handling for the certificates*/
        ServicePointManager.ServerCertificateValidationCallback += new System.Net.Security.RemoteCertificateValidationCallback(customXertificateValidation);

        /*
         Copying all the current request headers to the new request to GSA.
         Some headers might not be copied. Skip those headers and copy the rest
        */
        String[] requestHeaderKeys = null;
        if (ResponseForCopyingHeaders != null)
        {
            requestHeaderKeys = ResponseForCopyingHeaders.Headers.AllKeys;//add headers in GSA response to current response
        }
        else
        {
            requestHeaderKeys = HttpContext.Current.Request.Headers.AllKeys;//add headers available in current request to the GSA request
        }

        log("********************Header Information***************",LOG_LEVEL.INFO);
        for (int i = 0; i < requestHeaderKeys.Length - 1; i++)
        {
            try
            {
                /*Logging the header key and value*/
                log("Key: " + requestHeaderKeys[i] + " | Value= " + ResponseForCopyingHeaders.Headers[requestHeaderKeys[i]],LOG_LEVEL.INFO);

                /*Set-Cookie is not handled by auto redirect*/
                if (isAutoRedirect == true)
                {
                    if ((requestHeaderKeys[i] == "Set-Cookie") || (requestHeaderKeys[i] == "Location") || (requestHeaderKeys[i] == "WWW-Authenticate"))
                    {
                        continue;//Skip certain headers when using autoredirect
                    }
                }

                if (ResponseForCopyingHeaders != null)
                {
                    objReq.Headers.Add(requestHeaderKeys[i], ResponseForCopyingHeaders.Headers[requestHeaderKeys[i]]);
                }
                else
                {
                    objReq.Headers.Add(requestHeaderKeys[i], HttpContext.Current.Request.Headers[requestHeaderKeys[i]]);
                }
            }
            catch (Exception HeaderEx)
            {
                //just skipping the header information if any exception occures while adding to the GSA request
            }
        }
        log("********************End: Header Information***************", LOG_LEVEL.INFO);

        cc = SetCookies(cc, HttpContext.Current.Request.Cookies, cookiedomain);

        /*Add a dummy cookie*/
        Cookie dummyCookie = new Cookie();
        dummyCookie.Path = "/";
        dummyCookie.Name = "dummy";
        dummyCookie.Value = "dummy";
        dummyCookie.Domain = cookiedomain;
        cc.Add(dummyCookie);

        objReq.CookieContainer = cc;//Set GSA request cookiecontainer
        requestHeaderKeys = null;
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        objReq.Method = "GET";//Make a Get Request (idempotent)
        try
        {
            response = (HttpWebResponse)objReq.GetResponse();//fire getresponse
        }
        catch(Exception e)
        {
            log(e.Message,LOG_LEVEL.ERROR  );
            log(e.StackTrace, LOG_LEVEL.ERROR);

            HttpContext.Current.Response.Write(e.Message + "<br/>");
            HttpContext.Current.Response.Write(e.StackTrace + "<br/>");
            HttpContext.Current.Response.End();
        }
        return response;
    }

    /// <summary>
    /// Add the cookie from the cookie collection to the container. Your container may have some existing cookies
    /// </summary>
    /// <param name="CookieCollection">Cookie to be copied into the cookie container</param>
    /// <returns> Cookie container after cookies are added</returns>
    public CookieContainer SetCookies(CookieContainer cc, HttpCookieCollection CookieCollection, String domain)
    {
        if (null != CookieCollection)
        {
            if (null == cc)
            {
                cc = new CookieContainer();
            }

            log("********************Cookie Information***************", LOG_LEVEL.INFO);
            Cookie c = new Cookie();//add cookies available in current request to the GSA request
            for (int i = 0; i < CookieCollection.Count ; i++)
            {
                string tempCookieName = CookieCollection[i].Name;
                c.Name = tempCookieName;
                Encoding utf8 = Encoding.GetEncoding("utf-8");
                String value = CookieCollection[i].Value;
                //c.Value = HttpUtility.UrlEncode(value, utf8);//Encoding the cookie value
                c.Value = value;// not Encoding the cookie value
                c.Domain = domain;
				c.Path= "/";
                c.Expires = CookieCollection[i].Expires;
                cc.Add(c);

                /*Cookie Information*/
                log("Cookie Name= " + tempCookieName + "| Value= " + value + "| Domain= " + domain + "| Expires= " + c.Expires, LOG_LEVEL.INFO);

            }
            log("********************End: Cookie Information***************", LOG_LEVEL.INFO);
        }
        else
        {
            //log("No cookies found in cookie collection", LOG_LEVEL.INFO);
            return null;
        }
        return cc;
    }

    /// <summary>
    /// Reads the web application settings for GSA.
    /// </summary>
    private void InitGSA()
    {
        
        /*Google Search Appliance URL*/
        GSALocation = WebConfigurationManager.AppSettings["GSALocation"];
        if ((GSALocation == null) || (GSALocation.Trim().Equals("")))
        {
            log("Google Search Appliance location is not specified", LOG_LEVEL.ERROR);//log error
            HttpContext.Current.Response.Write("Google Search Appliance location is not specified");
            HttpContext.Current.Response.End();
        }

        /*Site Collection URL*/
        siteCollection = WebConfigurationManager.AppSettings["siteCollection"];
        if ((siteCollection == null) || (siteCollection.Trim().Equals("")))
        {
            log("Site collection value for Google Search Appliance is not specified", LOG_LEVEL.ERROR);//log error
            HttpContext.Current.Response.Write("Site collection value for Google Search Appliance is not specified");
            HttpContext.Current.Response.End();
        }

        /*
            Need for cookie domain
            =======================
            While retrieving the cookie information from the set-cookie header of the incoming request 
            you may not get the domain for the cookie and while creating cookie you require the domain
          
            Cookie domain parameter gives you the flexibility to change\configure
            the domain of the newly added cookies
         */
        cookiedomain = WebConfigurationManager.AppSettings["cookieDomain"];
        if ((siteCollection == null) || (siteCollection.Trim().Equals("")))
        {
            /*
                Case: when user does not specify the value of the cookie domain explicitly
                Solution: take the domain same as that of the GSA
             */
            Uri u = new Uri(GSALocation);
            String machineWithDomain = u.Host;
            
            /*Limiting a cookie for a domain scope*/
            cookiedomain = machineWithDomain.Substring(machineWithDomain.IndexOf('.'));
            log("Cookie domain not specified.. Taking " + cookiedomain+" as domain", LOG_LEVEL.ERROR);//log error
        }

        /*Get the Log file path. This defaults to c:*/
        LogFilePath = WebConfigurationManager.AppSettings["logLocation"];
        if ((LogFilePath == null) || (LogFilePath.Trim().Equals("")))
        {
            LogFilePath = @"c:\";
        }

        if (!LogFilePath.EndsWith("\\"))
        {
            LogFilePath += "\\";
        }
        
        String tempLevel = WebConfigurationManager.AppSettings["verbose"];
        if ((tempLevel != null) && (!tempLevel.Trim().Equals("")))
        {
            /*When user has specified the log level explicitly*/
            if (tempLevel.Trim().ToLower().Equals("true"))
            {
                LogLevel = LOG_LEVEL.INFO;
            }
        }

        /*Google Search Appliance Front End*/
        frontEnd = WebConfigurationManager.AppSettings["frontEnd"];
        if ((frontEnd == null) || (frontEnd.Trim().Equals("")))
        {
            log("Front end value for Google Search Appliance is not specified", LOG_LEVEL.ERROR);//log error
            HttpContext.Current.Response.Write("Front end value for Google Search Appliance is not specified");
            HttpContext.Current.Response.End();
        }
    }

    /// <summary>
    /// Action to be performed when the search button is clicked
    /// </summary>
    /// <param name="sender"></param>
    /// <param name="e"></param>
    //protected void Button1_Click(object sender, EventArgs e)
    protected void Page_Load(object sender, EventArgs e)
    {
        /*initialization of the parameters*/
        InitGSA();

        HttpWebResponse response = null;
        String searchurl = "";
        int redirectCount = -1;
        HttpWebRequest objReq = null;

        do
        {
            ++redirectCount;
            log("Redirect # " + redirectCount, LOG_LEVEL.INFO);
            searchurl = GSALocation + "/search?" + HttpContext.Current.Request.QueryString;
            HttpContext.Current.Response.Clear();
            log("Search Request to URL: " + searchurl, LOG_LEVEL.INFO);

            if (redirectCount > 0)
            {
                #region redirect
                CookieContainer newcc = new CookieContainer();//Added for SAML

                //copy the cookies from the incomming response
                Cookie responseCookies = new Cookie(); ;//add cookies in GSA response to current response
                int j;
                for (j = 0; j < response.Cookies.Count - 1; j++)
                {
                    responseCookies.Name = response.Cookies[j].Name;
                    Encoding utf8 = Encoding.GetEncoding("utf-8");
                    string value = response.Cookies[j].Value;
                    responseCookies.Value = HttpUtility.UrlEncode(value, utf8);
                    responseCookies.Domain = objReq.RequestUri.Host;
                    responseCookies.Expires = response.Cookies[j].Expires;

                    /*Cookie Information*/
                    log("Cookie Name= " + responseCookies.Name + "| Value= " + value + "| Domain= " + responseCookies.Domain+ "| Expires= " + responseCookies.Expires.ToString(), LOG_LEVEL.INFO);

                    newcc.Add(responseCookies);
                }

                /*
                  We need to check if there is a cookie or not. This check is for the 
                  initial request to GSA in case of SAML is configured with GSA. 
                 */
                string newURL = response.Headers["Location"];
                log("The redirect URL is: " + newURL, LOG_LEVEL.INFO);
                string GSASessionCookie = response.Headers["Set-Cookie"];
                log("GSASessionCookie: " + GSASessionCookie, LOG_LEVEL.INFO);

                /*Break multiple cookie based on semi-colon as separator*/
                Char[] seps = { ';' };
                if (GSASessionCookie != null)
                {

                    String[] key_val = GSASessionCookie.Split(seps);

                    /*check if there is atleast one cookie in the set-cookie header*/
                    if ((key_val != null) && (key_val[0] != null))
                    {
                        foreach (String one_cookie in key_val)
                        {
                            /*
                              Get the key and value for each cookie. 
                              Encode the value of the cookie while adding the cookie for new request
                            */
                            Char[] Seps_Each_Cookie = { '=' };

                            /*
                              Problem
                              ========
                              You can have cookie values containing '=' which is also the separator 
                              for the key and value of the cookie. 
                                          
                              Solution
                              ========
                              Parse the cookies and get 1st part as keyName and remaing part as value. 
                              Get only 2 tokens as value could also contain '='. E.g. String one_cookie = "aa=bb=cc=dd";
                            */

                            string name;
                            string value;

                            /*
                                Problem:
                                =========
                                Cookie may or may not have a value.
                                E.g. GSA_SESSION_ID=7d8b50eb55a1c077159657da24e5b71d; secure
                                'secure' does not have any value.
                                            
                                Solution:
                                ========
                                Check if the cookie contains '='/cookie key-value separator. 
                                If so get the value. 
                                If not value should be empty;
                            */

                            if (one_cookie.Contains("="))
                            {
                                String[] Cookie_Key_Val = one_cookie.Trim().Split(Seps_Each_Cookie, 2);
                                name = Cookie_Key_Val[0];
                                value = Cookie_Key_Val[1];
                            }
                            else
                            {
                                name = one_cookie.Trim();
                                value = "";
                            }
                            /////////////////////////
                            responseCookies.Name = name;
                            Encoding utf8 = Encoding.GetEncoding("utf-8");
                            responseCookies.Value = HttpUtility.UrlEncode(value, utf8);
                            responseCookies.Domain = cookiedomain;
                            responseCookies.Expires = DateTime.Now.AddDays(1);//add 1 day from now 
                            newcc.Add(responseCookies);
                        }
                    }//end: if ((key_val != null) && (key_val[0] != null))
                }
                response = (HttpWebResponse)GetResponse(true, searchurl, newcc, response);//fire getresponse
                
            #endregion redirect           
            }
            else 
            {
                response = (HttpWebResponse)GetResponse(false, searchurl, null, null);//fire getresponse
            }
            log("Got Response code: " + response.StatusCode, LOG_LEVEL.INFO);
        } while (response.StatusCode == HttpStatusCode.Redirect);

        log("Content length is " + response.ContentLength, LOG_LEVEL.INFO);//log error
        log("Content type is " + response.ContentType, LOG_LEVEL.INFO);//log error

        // Get the stream associated with the response.
        Stream receiveStream = response.GetResponseStream();
        string contentEncoding = response.Headers["Content-Encoding"];
        string returnstring = GetContentFromStream(contentEncoding, receiveStream);

        HttpContext.Current.Response.Write(returnstring);
        response.Close();
    }

    /// <summary>
    /// Takes out the content from the Stream. Also, handles the ZIP output from GSA. 
    /// Typically when kerberos is enabled on GSA, we get the encoding as "GZIP". 
    /// This creates problem while displaying the results in IE 6 and we need to handle it differently
    /// </summary>
    /// <param name="ContentEncoding">content encoding of the HTTP response</param>
    /// <param name="objStream">Stream from which the string is read</param>
    /// <returns></returns>
    public String GetContentFromStream(String contentEncoding, Stream objStream)
    {
        StreamReader objSR = null;
        String returnstring = "";

        /*Handle the zipped content response stream*/
        if ((contentEncoding != null) && (contentEncoding.Contains("gzip")))
        {
            try
            {
                GZipStream unzipped = new GZipStream(objStream, CompressionMode.Decompress);
                objSR = new StreamReader(unzipped);//we have set in the URL to get the result in the UTF-8 encoding format
                returnstring = (objSR.ReadToEnd());// read the content from the stream
            }
            catch (Exception zipe)
            {
                returnstring = "Error occured while converting the zipped contents, Error Message: " + zipe.Message;
            }
        }
        else
        {
            objSR = new StreamReader(objStream, Encoding.UTF8);//we have set in the URL to get the result in the UTF-8 encoding format
            returnstring = (objSR.ReadToEnd());// read the content from the stream
        }

        return returnstring;
    }
}