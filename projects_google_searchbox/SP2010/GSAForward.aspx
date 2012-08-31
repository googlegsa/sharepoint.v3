<%@ Page Language="C#" %>
<%@ Register Tagprefix="SharePoint" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Import Namespace="System.IO" %>
<%@ Import Namespace="System.Net" %>
<%@ Import Namespace="System.Web.Configuration" %>
<%@ Import Namespace="System.Security.Cryptography.X509Certificates" %>
<%@ Import Namespace="System.IO.Compression" %>
<%@ Import Namespace="Microsoft.SharePoint.ApplicationPages" %>
<%@ Import Namespace="Microsoft.SharePoint" %>
<script runat="server">
        public const string secureCookieToBeDiscarded = "secure";
    
        /*Enumeration which defines Search Box Log levels*/
        public enum LOG_LEVEL
        {
            INFO,
            ERROR
        }
        // <TODO: To move this code to a common module where it can be shared between GSASearchResult.aspx and GSAForward.aspx>
    
        /*Helper module to process GSA requests*/
        class GSAHelper
        {
            public LOG_LEVEL currentLogLevel = LOG_LEVEL.ERROR;

            /**
             * Block Logging. The flag is used to avoid the cyclic conditions. 
             **/
            public bool BLOCK_LOGGING = false;

            /*
             * The default location points to the 14 hive location where SharePoint usually logs all its messages
             * User can always override this location and point to a different location.
             */
            public const String DEFAULT_LOG_LOCATION = @"C:\program files\Common Files\Microsoft Shared\web server extensions\14\LOGS\";
            public string LogLocation = DEFAULT_LOG_LOCATION;
            public const String PRODUCTNAME = "GSBS";     

            public GSAHelper()
            {
                //set the log location
                LogLocation = getLogLocationFromConfig();

                //set the current log level
                currentLogLevel = getLogLevel();
            }

            /// <summary>
            /// Get the Log Level from the 
            /// </summary>
            /// <returns>Log Level</returns>
            private LOG_LEVEL getLogLevel()
            {
                string LogLevel = WebConfigurationManager.AppSettings["verbose"];
                if ((LogLevel == null) || (LogLevel.Trim().Equals("")))
                {
                    return LOG_LEVEL.ERROR;//by default log level is error
                }
                else
                {
                    if (LogLevel.ToLower().Equals("true"))
                    {
                        return LOG_LEVEL.INFO;
                    }
                    else
                    {
                        return LOG_LEVEL.ERROR;
                    }
                }
            }

            /// <summary>
            /// Get the Log Location from the Web.config
            /// </summary>
            /// <returns></returns>
            private string getLogLocationFromConfig()
            {
                string ConfigLogLocation = WebConfigurationManager.AppSettings["logLocation"];
                if ((ConfigLogLocation == null) || (ConfigLogLocation.Trim().Equals("")))
                {
                    ConfigLogLocation = DEFAULT_LOG_LOCATION;
                }

                if (!ConfigLogLocation.EndsWith("\\"))
                {
                    ConfigLogLocation += "\\";
                }

                return ConfigLogLocation;
            }

            /// <summary>
            /// Function to check the existance to cookie and discard if the setting is enabled in web.config file. (Currently function is defined for secure cookie. 
            /// If problem for other cookies, change parameter 'cookieNameToBeChecked' accordingly, while calling the function.
            /// </summary>
            /// <param name="webConfigSetting">Value from the web.config custom key value pair for cookie to be discarded</param>
            /// <param name="name">Variable holding the name of cookie</param>
            /// <param name="cookieNameToBeChecked">Name of cookie to be discarded. Can be any name, usually string variable</param>
            /// <param name="value">value">Value of the cookie to be discarded</param>
            /// <returns>Boolean check whether to discard the cookie, as per web.config setting</returns>
            public bool CheckCookieToBeDroppedAndLogMessage(string webConfigSetting, string name, string cookieNameToBeChecked, string value)
            {
                bool secureCookieDecision = true;
                log("The " + cookieNameToBeChecked + " cookie exists with value as " + value + ".", LOG_LEVEL.INFO);
                if (cookieNameToBeChecked.Equals(name) && webConfigSetting == "true")
                {
                    secureCookieDecision = true;
                    log("Currently the " + cookieNameToBeChecked + "cookie is being discarded.  To avoid discarding of the" + cookieNameToBeChecked + "cookie, set the value for 'omitSecureCookie' key existing in the web.config file of the web application to 'false', as this value is configurable through the web.config file.", LOG_LEVEL.INFO);
                }
                else
                {
                    secureCookieDecision = false;
                }
                return secureCookieDecision;

            }
            
            /// <summary>
            /// Add the cookie from the cookie collection to the container. Your container may have some existing cookies
            /// </summary>
            /// <param name="CookieCollection">Cookie to be copied into the cookie container</param>
            /// <returns> Cookie container after cookies are added</returns>
            public CookieContainer SetCookies(CookieContainer cc ,HttpCookieCollection CookieCollection, String domain)
            {
                if (null != CookieCollection)
                {
                    if (null == cc)
                    {
                        cc = new CookieContainer();
                    }

                    Cookie c = new Cookie();//add cookies available in current request to the GSA request
                    for (int i = 0; i < CookieCollection.Count - 1; i++)
                    {
                        string tempCookieName = CookieCollection[i].Name;
                        c.Name = tempCookieName;
                        Encoding utf8 = Encoding.GetEncoding("utf-8");
                        String value = CookieCollection[i].Value;
                        c.Value = HttpUtility.UrlEncode(value, utf8);//Encoding the cookie value
                        c.Domain = domain;
                        c.Expires = CookieCollection[i].Expires;

                        ///* 
                        // * The 'secure' cookie issue - Setting for secure cookie, which will decide whether the secure cookie should be passed on for processing or not.
                        // * Value 'false' indicates that cookie will be not be dropped, and value 'true' indicates that the cookie will be dropped.
                        // */

                        if (tempCookieName.ToLower() == secureCookieToBeDiscarded)
                        {
                            bool secureCookieDiscardDecision = CheckCookieToBeDroppedAndLogMessage(WebConfigurationManager.AppSettings["omitSecureCookie"], tempCookieName.ToLower(), secureCookieToBeDiscarded, value);
                            if (secureCookieDiscardDecision == false)
                            {
                                cc.Add(c);
                            }
                        }
                        else
                        {

                            // Add the other cookies to the cookie container
                            cc.Add(c);
                        }

                        /*Cookie Information*/
                        log("Cookie Name= " + tempCookieName + "| Value= " + value + "| Domain= " + domain + "| Expires= " + c.Expires, LOG_LEVEL.INFO);

                    }
                }
                else
                {
                    log("No cookies found in cookie collection", LOG_LEVEL.INFO);
                    return null;
                }
                return cc;
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
                //Stream objStream = null;
                StreamReader objSR = null;
                String returnstring = "";

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
                        log("Error occured while converting the zipped contents, Error Message: " + zipe.Message, LOG_LEVEL.ERROR);
                    }
                }
                else
                {
                    objSR = new StreamReader(objStream, Encoding.UTF8);//we have set in the URL to get the result in the UTF-8 encoding format
                    returnstring = (objSR.ReadToEnd());// read the content from the stream
                }

                return returnstring;
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
                HttpWebResponse objResp = null;

                log("Search Request to GSA:" + GSASearchUrl, LOG_LEVEL.INFO);

                objReq = (HttpWebRequest)HttpWebRequest.Create(GSASearchUrl);
                objReq.KeepAlive = true;
                objReq.AllowAutoRedirect = isAutoRedirect;
                objReq.MaximumAutomaticRedirections = 100;
                objReq.Credentials = System.Net.CredentialCache.DefaultCredentials;//set credentials

                /*handling for the certificates*/
                ServicePointManager.ServerCertificateValidationCallback += new System.Net.Security.RemoteCertificateValidationCallback(customXertificateValidation);


                ////////////////////////COPYING THE CURRENT REQUEST PARAMETRS, HEADERS AND COOKIES ///////////////////////                        
                /*Copying all the current request headers to the new request to GSA.Some headers might not be copied .. skip those headers and copy the rest*/

                String[] requestHeaderKeys = null;
                if (ResponseForCopyingHeaders != null)
                {
                    requestHeaderKeys = ResponseForCopyingHeaders.Headers.AllKeys;//add headers in GSA response to current response
                }
                else
                {
                    requestHeaderKeys = HttpContext.Current.Request.Headers.AllKeys;//add headers available in current request to the GSA request
                }

                for (int i = 0; i < requestHeaderKeys.Length - 1; i++)
                {
                    try
                    {
                        /*Logging the header key and value*/
                        log("Request Header Key=" + requestHeaderKeys[i] + "| Value= " + HttpContext.Current.Request.Headers[requestHeaderKeys[i]], LOG_LEVEL.INFO);

                        /*Set-Cookie is not handled by auto redirect*/
                        if (isAutoRedirect == true)
                        {
                            if ((requestHeaderKeys[i] == "Set-Cookie") || (requestHeaderKeys[i] == "Location"))
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
                cc = SetCookies(cc, HttpContext.Current.Request.Cookies, objReq.RequestUri.Host);
                objReq.CookieContainer = cc;//Set GSA request cookiecontainer
                requestHeaderKeys = null;
                /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                objReq.Method = "GET";//Make a Get Request (idempotent)
                objResp = (HttpWebResponse)objReq.GetResponse();//fire getresponse
                return objResp;
            } 
           
            
            /// <summary>
            /// For logging the search box messages.
            /// </summary>
            /// <param name="msg">The message to be logged</param>
            /// <param name="logLevel">Log level</param>
            public void log(String msg, LOG_LEVEL logLevel)
            {
                /**
                 * If logging is already blocked, do not do further processing 
                 **/
                if ((BLOCK_LOGGING == false) && (logLevel >= currentLogLevel))
                {
                    try
                    {
                        String time = DateTime.Today.ToString("yyyy_MM_dd");
                        string WebAppName = "";

                        /**
                         * If possible get the web app name to be appended in log file name. If exception skip it.
                         * Note: If we breakup create a unction to get the web app name it fails with 'Unknown error' in SharePoint
                         **/
                        try
                        {

                            WebAppName = SPContext.Current.Site.WebApplication.Name;
                            if ((WebAppName == null) || (WebAppName.Trim().Equals("")))
                            {
                                /**
                                 * This is generally the acse with the SharePoint central web application.
                                 * e.g. DefaultServerComment = "SharePoint Central Administration v3"
                                 **/
                                WebAppName = SPContext.Current.Site.WebApplication.DefaultServerComment;
                            }
                        }
                        catch (Exception) { }


                        int portNumber = -1;

                        /**
                         * If possible get the port number to be appended in log file name. If exception skip it
                         **/
                        try
                        {
                            portNumber = SPContext.Current.Site.WebApplication.AlternateUrls[0].Uri.Port;
                        }
                        catch (Exception) { }

                        String CustomName = PRODUCTNAME + "_" + WebAppName + "_" + portNumber + "_" + time + ".log";
                        String loc = LogLocation + CustomName;


                        /*
                         * We need to make even a normal user with 'reader' access to be able to log messages
                         * This requires to elevate the user temporarily for write operation.
                         */
                        SPSecurity.RunWithElevatedPrivileges(delegate()
                        {
                            FileStream f = new FileStream(loc, FileMode.Append, FileAccess.Write);

                            /**
                             * If we use FileLock [i.e.  f.Lock(0, f.Length)] then it may cause issue
                             * Logging failed due to: The process cannot access the file 'C:\Program Files\Common Files\Microsoft Shared\Web Server Extensions\12\LOGS\GSBS_SharePoint - 9000_9000_2009_12_03.log' because it is being used by another process.
                             * Thread was being aborted.
                             **/

                            StreamWriter logger = new StreamWriter(f);

                            logger.WriteLine("[ {0} ]  [{1}] :- [GSAForward.aspx] {2}", DateTime.Now.ToString(), logLevel, msg);
                            logger.Flush();
                            logger.Close();
                        });
                    }
                    catch (Exception logException) {
                        if (BLOCK_LOGGING == false)
                        {
                            BLOCK_LOGGING = true;
                            HttpContext.Current.Response.Write("<b><u>Logging failed due to:</u></b> " + logException.Message + "<br/>");
                            HttpContext.Current.Response.End();
                        }
                    }

                }
            }
            
        }//end: class

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
       
                
</script>

<%
  // Compose the embedded mode specific query string arguments. We don't need
  // to check the application settings related to "enable/disable embedded mode"
  // as any request to this file would mean we are running in embedded mode
  // only. This will not do any harm.
  string embeddedModeQueryArg = "";
  embeddedModeQueryArg = "&emsingleres=" +
      HttpUtility.UrlEncode("/_layouts/GSAForward.aspx?forward=") +
      "&emmain=" +
      HttpUtility.UrlEncode("/_layouts/GSASearchresults.aspx");
  // Read the theme setting as this should be notified forward requests so that
  // GSA respects the theme setting.
  string useContainerTheme =
      WebConfigurationManager.AppSettings["UseContainerTheme"];
  if (useContainerTheme == null ||
      useContainerTheme.Trim().Equals("true")) {
    embeddedModeQueryArg = embeddedModeQueryArg +
        "&emdstyle=true";
  }
  GSAHelper gsaHelper = new GSAHelper();
    
  string GSALocation = WebConfigurationManager.AppSettings["GSALocation"];
  if((GSALocation == null) || (GSALocation.Trim().Equals(""))) {
    HttpContext.Current.Response.Write(
        "Google Search Appliance location is not specified");
    HttpContext.Current.Response.End();
    return;
  }
  // Read to which URI endpoint the request should be made on the GSA.
  string strForward = HttpContext.Current.Request.QueryString["forward"];
  // Compose the absolute URL where request to be sent.
  string gsaUrl = GSALocation + "/" + strForward;

  // If the forward request is a search request we should add the search and
  // static root path prefix as well.
  Boolean bGSARequest = false;
  if (strForward.StartsWith("/search")) {
    gsaUrl = gsaUrl + embeddedModeQueryArg;
    bGSARequest = true;
  }

  if (bGSARequest) // If Forward request is Search Request then pass on existing session queries and handle redirects.
  {
      HttpWebRequest objReq = null;
      HttpWebResponse objResp = null;
      Stream objStream = null;
      StreamReader objSR = null;
      CookieContainer cc = new CookieContainer();
      int i;
      String GSASearchUrl = gsaUrl;
      ////////////////////////////// PROCESSING THE RESULTS FROM THE GSA/////////////////
      objResp = (HttpWebResponse)gsaHelper.GetResponse(false, GSASearchUrl, null, null);//fire getresponse
      string contentEncoding = objResp.Headers["Content-Encoding"];
      string returnstring = "";//initialize the return string
      objStream = objResp.GetResponseStream();//if the request is successful, get the results in returnstring
      returnstring = gsaHelper.GetContentFromStream(contentEncoding, objStream);
      gsaHelper.log("Return Status from GSA: " + objResp.StatusCode, LOG_LEVEL.INFO);
      int FirstResponseCode = (int)objResp.StatusCode;//check the response code from the reply from 
      
      //*********************************************************************
      //Manually handling the Redirect from SAML bridge. Need to extract the Location and the GSA session Cookie
      string newURL = objResp.Headers["Location"];
      string GSASessionCookie = objResp.Headers["Set-Cookie"];
      //*********************************************************************

      CookieContainer newcc = new CookieContainer();//Added for SAML
      //if(GSASessionCookie!=null){
      /*handling for redirect*/
      if (FirstResponseCode == 302)
      {
          gsaHelper.log("The Response is being redirected to location " + newURL, LOG_LEVEL.INFO);
          Cookie responseCookies = new Cookie(); ;//add cookies in GSA response to current response
          int j;
          for (j = 0; j < objResp.Cookies.Count - 1; j++)
          {
              responseCookies.Name = objResp.Cookies[j].Name;
              Encoding utf8 = Encoding.GetEncoding("utf-8");
              string value = objResp.Cookies[j].Value;
              responseCookies.Value = HttpUtility.UrlEncode(value, utf8);
              responseCookies.Domain = objReq.RequestUri.Host;
              responseCookies.Expires = objResp.Cookies[j].Expires;

              /*Cookie Information*/
              gsaHelper.log("Cookie Name= " + responseCookies.Name + "| Value= " + value + "| Domain= " + responseCookies.Domain
                  + "| Expires= " + responseCookies.Expires.ToString(), LOG_LEVEL.INFO);

              ///* 
              // * The 'secure' cookie issue - Setting for secure cookie, which will decide whether the secure cookie should be passed on for processing or not.
              // * Value 'false' indicates that cookie will be not be dropped, and value 'true' indicates that the cookie will be dropped.
              // */

              if (responseCookies.Name.ToLower() == secureCookieToBeDiscarded)
              {
                  bool secureCookieDiscardDecision = gsaHelper.CheckCookieToBeDroppedAndLogMessage(WebConfigurationManager.AppSettings["omitSecureCookie"], responseCookies.Name.ToLower(), secureCookieToBeDiscarded, value);
                  if (secureCookieDiscardDecision == false)
                  {
                      newcc.Add(responseCookies);
                  }
              }
              else
              {
                  // Add the other cookies to the cookie container
                  newcc.Add(responseCookies);
              }
          }


          /*
             We need to check if there is a cookie or not. This check is for the 
             initial request to GSA in case of SAML is configured with GSA. 
           */
          gsaHelper.log("Adding cookies: " + GSASessionCookie, LOG_LEVEL.INFO);

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
                          gsaHelper.log("The cookie contains only key '" + name + "'without any value", LOG_LEVEL.INFO);
                          value = "";
                      }
                      /////////////////////////
                      responseCookies.Name = name;
                      Encoding utf8 = Encoding.GetEncoding("utf-8");
                      responseCookies.Value = HttpUtility.UrlEncode(value, utf8);
                      Uri GoogleUri = new Uri(GSASearchUrl);
                      responseCookies.Domain = GoogleUri.Host;
                      responseCookies.Expires = DateTime.Now.AddDays(1);//add 1 day from now 

                      ///* 
                      // * The 'secure' cookie issue - Setting for secure cookie, which will decide whether the secure cookie should be passed on for processing or not.
                      // * Value 'false' indicates that cookie will be not be dropped, and value 'true' indicates that the cookie will be dropped.
                      // */

                      if (responseCookies.Name.ToLower() == secureCookieToBeDiscarded)
                      {
                          bool secureCookieDiscardDecision = gsaHelper.CheckCookieToBeDroppedAndLogMessage(WebConfigurationManager.AppSettings["omitSecureCookie"], responseCookies.Name.ToLower(), secureCookieToBeDiscarded, value);
                          if (secureCookieDiscardDecision == false)
                          {
                              newcc.Add(responseCookies);
                          }
                      }
                      else
                      {

                          // Add the other cookies to the cookie container
                          newcc.Add(responseCookies);
                      }

                      /*Cookie Information*/
                      gsaHelper.log("Cookie Name= " + responseCookies.Name
                          + "| Value= " + value
                          + "| Domain= " + GoogleUri.Host
                          + "| Expires= " + responseCookies.Expires, LOG_LEVEL.INFO);
                  }
              }//end: if ((key_val != null) && (key_val[0] != null))
          }

          HttpWebResponse objNewResp = (HttpWebResponse)gsaHelper.GetResponse(true, GSASearchUrl, newcc, objResp);//fire getresponse
          contentEncoding = objResp.Headers["Content-Encoding"];
          returnstring = "";//initialize the return string
          Stream objNewStream = objNewResp.GetResponseStream();//if the request is successful, get the results in returnstring
          returnstring = gsaHelper.GetContentFromStream(contentEncoding, objNewStream);
      }
      else
      {
          HttpCookie responseCookies;//add cookies in GSA response to current response

          //set the cookies in the current response
          for (int j = 0; j < objResp.Cookies.Count - 1; j++)
          {
              responseCookies = new HttpCookie(objResp.Cookies[j].Name);
              responseCookies.Value = objResp.Cookies[j].Value;
              responseCookies.Domain = objReq.RequestUri.Host;
              responseCookies.Expires = objResp.Cookies[j].Expires;

              ///* 
              // * The 'secure' cookie issue - Setting for secure cookie, which will decide whether the secure cookie should be passed on for processing or not.
              // * Value 'false' indicates that cookie will be not be dropped, and value 'true' indicates that the cookie will be dropped.
              // */

              if (objResp.Cookies[j].Name.ToLower() == secureCookieToBeDiscarded)
              {
                  bool secureCookieDiscardDecision = gsaHelper.CheckCookieToBeDroppedAndLogMessage(WebConfigurationManager.AppSettings["omitSecureCookie"], objResp.Cookies[j].Name.ToLower(), secureCookieToBeDiscarded, objResp.Cookies[j].Value);
                  if (secureCookieDiscardDecision == false)
                  {
                      HttpContext.Current.Response.Cookies.Add(responseCookies);
                  }
              }
              else
              {


                  // Add the other cookies to the cookie containe
                  HttpContext.Current.Response.Cookies.Add(responseCookies);
              }

              /*Cookie Information*/
              gsaHelper.log("Cookie Name= " + objResp.Cookies[j].Name
                  + "| Value= " + objResp.Cookies[j].Value
                  + "| Domain= " + objReq.RequestUri.Host
                  + "| Expires= " + responseCookies.Expires, LOG_LEVEL.INFO);
              
              responseCookies = null;
          }
      }//end if condition for SAML
      // ********************************************
      HttpContext.Current.Response.Write(returnstring);
      
  }
  else // This is Original GSAForward.aspx which will be used to serve non search requests
  {
      // To prevent 417 expectation failed.
      System.Net.ServicePointManager.Expect100Continue = false;
      // Create an HTTP request to the composed GSA URL.
      HttpWebRequest fwdHttpReq = (HttpWebRequest)WebRequest.Create(gsaUrl);

      CookieContainer cc = null;
      cc = gsaHelper.SetCookies(cc, HttpContext.Current.Request.Cookies, fwdHttpReq.RequestUri.Host);
      fwdHttpReq.CookieContainer = cc;
     
      if (HttpContext.Current.Request.HttpMethod == "POST")
      {
          // If this is a POST request then copy the body and required request
          // headers.
          fwdHttpReq.Method = HttpContext.Current.Request.HttpMethod;
          fwdHttpReq.ContentType = HttpContext.Current.Request.ContentType;
          fwdHttpReq.ContentLength = HttpContext.Current.Request.ContentLength;
          Stream reqStream = fwdHttpReq.GetRequestStream();
          StreamReader sr = new StreamReader(HttpContext.Current.Request.InputStream);
          byte[] bytes = sr.CurrentEncoding.GetBytes(sr.ReadToEnd());
          reqStream.Write(bytes, 0, bytes.Length);
          reqStream.Close();
          sr.Close();
      }

      // Read the response from the forwarded HTTP request and populate the current
      // open response with the same.
      HttpWebResponse response = (HttpWebResponse)fwdHttpReq.GetResponse();

      // Copy all HTTP headers.
      for (int i = 0; i < response.Headers.Count; i++)
      {
          HttpContext.Current.Response.AddHeader(
              response.Headers.Keys[i], response.Headers[i]);
      }
      // Copy all Cookies.
      foreach (Cookie c in response.Cookies)
      {
          HttpContext.Current.Response.SetCookie(new HttpCookie(c.Name, c.Value));
      }
      // Copy the response body.
      Stream objStream = response.GetResponseStream();
      byte[] buffer = new byte[4096];
      int len = 0;
      while ((len = objStream.Read(buffer, 0, 4096)) > 0)
      {
          if (len < 4096)
          {
              byte[] buffer2 = new byte[len];
              for (int j = 0; j < len; j++)
              {
                  buffer2[j] = buffer[j];
              }
              HttpContext.Current.Response.BinaryWrite(buffer2);
          }
          else
          {
              HttpContext.Current.Response.BinaryWrite(buffer);
          }
      }
      // Arun : This is to doc preview to work.
      HttpContext.Current.Response.Headers.Remove("Transfer-Encoding");
      Response.Cache.SetCacheability(HttpCacheability.NoCache);
      HttpContext.Current.Response.StatusCode = (int)response.StatusCode;
      HttpContext.Current.Response.End(); 
  }
%>
