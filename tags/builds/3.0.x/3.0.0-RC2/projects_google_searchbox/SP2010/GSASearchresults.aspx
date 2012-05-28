<%@ Assembly Name="Microsoft.SharePoint.ApplicationPages, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register TagPrefix="wssawc" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register TagPrefix="SharePoint" Namespace="Microsoft.SharePoint.WebControls"
    Assembly="Microsoft.SharePoint, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register TagPrefix="SearchWC" Namespace="Microsoft.SharePoint.Search.Internal.WebControls"
    Assembly="Microsoft.SharePoint.Search, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register TagPrefix="SharePoint" Namespace="Microsoft.SharePoint.WebControls"
    Assembly="Microsoft.SharePoint, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register TagPrefix="Utilities" Namespace="Microsoft.SharePoint.Utilities" Assembly="Microsoft.SharePoint, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>

<%@ Assembly Name="Microsoft.Office.Server.Search, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c"%> 

<%-- <% Enabled the Session state in the page by setting the attribute 'EnableSessionState' to true  %>--%>
<%@ Page Language="C#" DynamicMasterPageFile="~masterurl/default.master" Inherits="Microsoft.Office.Server.Search.Internal.UI.OssSearchResults" EnableSessionState="True"   %> 
<%@ Import Namespace="Microsoft.Office.Server.Search.Internal.UI" %> 
<%@ Register Tagprefix="SharePoint" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> 
<%@ Register Tagprefix="Utilities" Namespace="Microsoft.SharePoint.Utilities" Assembly="Microsoft.SharePoint, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> 
<%@ Import Namespace="Microsoft.SharePoint" %> 
<%@ Assembly Name="Microsoft.Web.CommandUI, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> 

<%@ Register Tagprefix="wssawc" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> 
<%@ Register Tagprefix="SharePoint" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>

<%@ Register Tagprefix="SearchWC" Namespace="Microsoft.Office.Server.Search.WebControls" Assembly="Microsoft.Office.Server.Search, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register Tagprefix="SPSWC" Namespace="Microsoft.SharePoint.Portal.WebControls" Assembly="Microsoft.SharePoint.Portal, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register Tagprefix="MSSWC" Namespace="Microsoft.SharePoint.Portal.WebControls" Assembly="Microsoft.Office.Server.Search, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>

<%@ Import Namespace="Microsoft.SharePoint.ApplicationPages" %>
<%@ Import Namespace="Microsoft.SharePoint" %>
<%@ Import Namespace="System.Net" %>
<%@ Import Namespace="System.IO" %>
<%@ Import Namespace="System.Web.Configuration" %>
<%@ Import Namespace="System.Security.Cryptography.X509Certificates" %>
<%@ Import Namespace="System.Xml" %>
<%@ Import Namespace="System.Xml.Xsl" %>
<%@ Import Namespace="System.Text" %>
<%@ Import Namespace="System.Xml.XPath" %>
<%@ Import Namespace="System.Security.Principal" %>
<%@ Import Namespace="System.Web.Security" %>
<%@ Import Namespace="System.Diagnostics" %>
<%@ Import Namespace="System.IO.Compression" %>

<asp:content id="Content1" contentplaceholderid="PlaceHolderPageTitle" runat="server">
    <SharePoint:EncodedLiteral ID="EncodedLiteral1" runat="server" Text="<%$Resources:wss,searchresults_pagetitle%>"
        EncodeMethod='HtmlEncode' />
</asp:content>

<asp:content id="Content2" contentplaceholderid="PlaceHolderAdditionalPageHead" runat="server">

    <style type="text/css">
.ms-titlearea
{
	padding-top: 0px !important;
}
.ms-areaseparatorright {
	PADDING-RIGHT: 6px;
}
td.ms-areaseparatorleft{
	border-right:0px;
}
div.ms-areaseparatorright{
	border-left:0px !important;
}
</style>
    <script runat="server">
		/*Author: Amit Agrawal*/
        
        public const int num = 10;//page size
        public string myquery = "";
        public const String PREV = "Previous";
        public const String NEXT = "Next";
        public const String PAGENAME = "GSASearchresults.aspx";
        //public const String PAGENAME = "GSASearchresults.aspx";
        public string tempvar = "";
        public const string PREVSTMT = "";//initially prev should be hidden
        public const string NEXTSTMT = "";
        public bool isNext = true;
        public int start = 0;/* E.g. start = 1 and num =5 (return 11-15 results)*/
        int endB = 0;

		public const string currentSite = "Current Site";
        public const string currentSiteAndAllSubsites = "Current Site and all subsites";
        public const string currentList = "Current List";
        public const string currentFolder = "Current Folder";
        public const string currentFolderAndAllSubfolders = "Current Folder and all subfolders";

        public const string secureCookieToBeDiscarded = "secure";
        
        /*Enumeration which defines Search Box Log levels*/
        public enum LOG_LEVEL
        {
            INFO,
            ERROR
        }
        
        /*Google Search Box for SharePoint*/
        class GoogleSearchBox
        {
            public string GSALocation;
            public string accessLevel;//Do a Public and Secured search
            public string siteCollection;
            public string frontEnd;
            public string enableInfoLogging;
            public Boolean bUseGSAStyling = true;
            public string xslGSA2SP;
            public string xslSP2result;
            public string temp="true";
			public const String PRODUCTNAME = "GSBS";
            // Embedded mode related query string arguments, if enabled.
            public string embeddedModeQueryArg = "";
            public LOG_LEVEL currentLogLevel = LOG_LEVEL.ERROR;

            /**
             * Block Logging. The flag is used to avoid the cyclic conditions. 
             **/
            public bool BLOCK_LOGGING = false;            
            
            /*
             * The default location points to the 12 hive location where SharePoint usually logs all its messages
             * User can always override this location and point to a different location.
             */
            public const String DEFAULT_LOG_LOCATION = @"C:\program files\Common Files\Microsoft Shared\web server extensions\14\LOGS\";
            public string LogLocation = DEFAULT_LOG_LOCATION;

            /*For Internal Transformations*/
            public XslTransform xslt1 = null;
            public XslTransform xslt2 = null;
            
            public GoogleSearchBox()
            {
                GSALocation = "";
                siteCollection = "default_collection";
                frontEnd = "default_frontend";
                xslGSA2SP = "";
                xslSP2result = "";
            }
            
            //Method to extract configuration properties into GoogleSearchBox
            public void initGoogleSearchBox()
            {
                /*Parameter validatations and NULL checks*/
                GSALocation = WebConfigurationManager.AppSettings["GSALocation"];
                if((GSALocation==null) || (GSALocation.Trim().Equals("")))
                {
                    log("Google Search Appliance location is not specified", LOG_LEVEL.ERROR);//log error
                    HttpContext.Current.Response.Write("Google Search Appliance location is not specified");
                    HttpContext.Current.Response.End();
                }
                
                siteCollection = WebConfigurationManager.AppSettings["siteCollection"];
                if((siteCollection==null) || (siteCollection.Trim().Equals("")))
                {
                    log("Site collection value for Google Search Appliance is not specified", LOG_LEVEL.ERROR);//log error
                    HttpContext.Current.Response.Write("Site collection value for Google Search Appliance is not specified");
                    HttpContext.Current.Response.End();
                }

                // Embedded mode settings. We should enable this by default
                // if it is not specified.
                string enableEmbeddedMode =
                    WebConfigurationManager.AppSettings["EnableEmbeddedMode"];
                if (enableEmbeddedMode == null ||
                    enableEmbeddedMode.Trim().Equals("true")) {
                  embeddedModeQueryArg = "&emsingleres=" +
                      HttpUtility.UrlEncode("/_layouts/GSAForward.aspx?forward=") +
                      "&emmain=" +
                      HttpUtility.UrlEncode("/_layouts/GSASearchresults.aspx");
                }
                string useContainerTheme =
                    WebConfigurationManager.AppSettings["UseContainerTheme"];
                if (useContainerTheme == null ||
                    useContainerTheme.Trim().Equals("true")) {
                  embeddedModeQueryArg = embeddedModeQueryArg +
                      "&emdstyle=true";
                }

                //set the log location
                LogLocation = getLogLocationFromConfig();

                //set the current log level
                currentLogLevel = getLogLevel();
                
                frontEnd = WebConfigurationManager.AppSettings["frontEnd"];
                if((frontEnd==null) || (frontEnd.Trim().Equals("")))
                {
                    log("Front end value for Google Search Appliance is not specified", LOG_LEVEL.ERROR);//log error
                    HttpContext.Current.Response.Write("Front end value for Google Search Appliance is not specified");
                    HttpContext.Current.Response.End();
                }
                
                String GSAConfigStyle = WebConfigurationManager.AppSettings["GSAStyle"];
                if((GSAConfigStyle==null) || (GSAConfigStyle.Trim().Equals("")))
                {
                    log("Please specify value for GSA Style. Specify 'true' to use Front end's style for rendering search results. Specify 'False' to use the locally deployed stylesheet for rendering search results", LOG_LEVEL.ERROR);//log error
                    HttpContext.Current.Response.Write("Please specify value for GSA Style. Specify 'true' to use Front end's style for rendering search results. Specify 'False' to use the locally deployed stylesheet for rendering search results");
                    HttpContext.Current.Response.End();
                }
                
                if (GSAConfigStyle.ToLower().Equals("true"))
                {
                    bUseGSAStyling = true;
                }
                else
                {
                    bUseGSAStyling = false;
                }
                
                xslGSA2SP = WebConfigurationManager.AppSettings["xslGSA2SP"];
                xslSP2result = WebConfigurationManager.AppSettings["xslSP2result"];
                
                if(bUseGSAStyling ==false)
                {
                    if((xslGSA2SP==null) || (xslGSA2SP.Trim().Equals("")))
                    {
                        log("Please specify the value for stylesheet to convert GSA results to SharePoint like results", LOG_LEVEL.ERROR);//log error
                        HttpContext.Current.Response.Write("Please specify the value for stylesheet to convert GSA results to SharePoint like results");
                        HttpContext.Current.Response.End();
                    }
                    
                    if((xslSP2result==null) || (xslSP2result.Trim().Equals("")))
                    {
                        log("Please specify the value for stylesheet to be applied on search  results", LOG_LEVEL.ERROR);//log error
                        HttpContext.Current.Response.Write("Please specify the value for stylesheet to be applied on search  results");
                        HttpContext.Current.Response.End();
                    }
                }
                
                //Handling for slash in GSA
                if (null != GSALocation)
                {
                    if ((GSALocation.EndsWith("/")) || (GSALocation.EndsWith("\\")))
                    {
                        GSALocation = GSALocation.Substring(0, GSALocation.Length - 1);
                    }
                }
                try
                {
                    xslt1 = new XslTransform();/*preload the stylesheet.. for performance reasons*/
                    xslt1.Load(xslGSA2SP);//read XSLT
                    xslt2 = new XslTransform();
                    xslt2.Load(xslSP2result);//read XSLT
                }
                catch (Exception e)
                {
                    log("problems while loading stylesheet, message=" + e.Message + "\nTrace: " + e.StackTrace, LOG_LEVEL.ERROR);
                }
                
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
                        log("Cookie Name= " + tempCookieName+ "| Value= " + value+ "| Domain= " + domain+ "| Expires= " + c.Expires, LOG_LEVEL.INFO);
                        
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
                Stream objStream = null;
                StreamReader objSR = null;
                
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
                        log("Request Header Key="+requestHeaderKeys[i]+"| Value= " + HttpContext.Current.Request.Headers[requestHeaderKeys[i]], LOG_LEVEL.INFO);
		                
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
                cc = SetCookies(cc,HttpContext.Current.Request.Cookies, objReq.RequestUri.Host);
                objReq.CookieContainer = cc;//Set GSA request cookiecontainer
                requestHeaderKeys = null;
                /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                objReq.Method = "GET";//Make a Get Request (idempotent)
                objResp = (HttpWebResponse)objReq.GetResponse();//fire getresponse
                return objResp;
            }
            
            /// <summary>
            /// Transform the XML Page basing on the XSLStylesheet.
            /// </summary>
            /// <param name="XMLPage">Raw search result page</param>
            /// <param name="XSLStylesheet">stylesheet file</param>
            /// <returns></returns>
            public static string transform(String XMLPage, XslTransform xslt)
            {
                TextReader tr1 = new StringReader(XMLPage);//read XML
                XmlTextReader tr11 = new XmlTextReader(tr1);
                XPathDocument xPathDocument = new XPathDocument(tr11);

                //create the output stream
                StringBuilder sb = new StringBuilder();
                TextWriter tw = new StringWriter(sb);
                xslt.Transform(xPathDocument, null, tw);
                return sb.ToString();//get result
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
                if ((BLOCK_LOGGING==false)&&(logLevel >= currentLogLevel))
                {
                    try
                    {
                        String time = DateTime.Today.ToString("yyyy_MM_dd");
                        string WebAppName="";

                        /**
                         * If possible get the web app name to be appended in log file name. If exception skip it.
                         * Note: If we breakup create a unction to get the web app name it fails with 'Unknown error' in SharePoint
                         **/
                        try
                        {
                             
                            WebAppName=SPContext.Current.Site.WebApplication.Name;
                            if ((WebAppName == null) || (WebAppName.Trim().Equals("")))
                            {
                                /**
                                 * This is generally the acse with the SharePoint central web application.
                                 * e.g. DefaultServerComment = "SharePoint Central Administration v3"
                                 **/
                                WebAppName= SPContext.Current.Site.WebApplication.DefaultServerComment;
                            }
                        }
                        catch(Exception){}


                        int portNumber = -1;

                        /**
                         * If possible get the port number to be appended in log file name. If exception skip it
                         **/
                        try
                        {
                            portNumber= SPContext.Current.Site.WebApplication.AlternateUrls[0].Uri.Port;
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
                                
                                logger.WriteLine("[ {0} ]  [{1}] :- {2}", DateTime.Now.ToString(), logLevel, msg);
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

<script type="text/javascript">
    function SetPageTitle()
	{
	   /*
	   * Call the javascript function 'SearchTextOnBlur' already defined in GSASearchArea.ascx page, which will set the 
	   * background image for the search box accordingly.
	   */
	   SearchTextOnBlur();
	   var Query = "";
	   if (window.top.location.search != 0) {
	       Query = window.top.location.search;
	       var keywordQuery = getParameter(Query, 'k');
	       if (keywordQuery != null)
	       {
	           //set the title of the document
	           if (keywordQuery != "")
	           {
	               var titlePrefix = '<asp:Literal runat="server" text="<%$Resources:wss,searchresults_pagetitle%>"/>';
	               document.title = titlePrefix + ": " + keywordQuery;
	           }
	       }
	   }
    }

	function getParameter (queryString, parameterNameWithoutEquals)
	{
	   var parameterName = parameterNameWithoutEquals + "=";
	   if (queryString.length > 0)
	   {
		 var begin = queryString.indexOf (parameterName);
		 if (begin != -1)
		 {
			begin += parameterName.length;
			var end = queryString.indexOf ("&" , begin);
			if (end == -1)
			{
			   end = queryString.length;
			}
			return decodeURIComponent(queryString.substring (begin, end));
		 }
	   }
	   return null;
	}

	
if (document.addEventListener)
{
	document.addEventListener("DOMContentLoaded", SetPageTitle, false);
}
else if(document.attachEvent)
{
	document.attachEvent("onreadystatechange", SetPageTitle);
}
</script>


</asp:content>
<asp:content id="Content3" contentplaceholderid="PlaceHolderTitleAreaClass" runat="server">
  
</asp:content>
<asp:content id="Content4" contentplaceholderid="PlaceHolderNavSpacer" runat="server">
</asp:content>
<asp:content id="Content5" contentplaceholderid="PlaceHolderTitleBreadcrumb" runat="server">
    <a name="mainContent"></a>
    <table width="100%" cellpadding="2" cellspacing="0" border="0">
        <tr>
            <td style="height: 5px"> <img src="/_layouts/images/blank.gif" width="1" height="1" alt=""></td>
        </tr>
        <tr>
            <td style="height: 5px"> <img src="/_layouts/images/blank.gif" width="1" height="1" alt=""></td>
        </tr>
        <tr>
        
            <td colspan="8">
                <SharePoint:DelegateControl ID="DelegateControl1" runat="server" ControlId="SmallSearchInputBox"  />
            </td>
            
        </tr>
        
        <!--
        <tr>
            <td valign="top" class="ms-descriptiontext" style="padding-bottom: 5px">
                <b>
                    <label for="<%SPHttpUtility.AddQuote(SPHttpUtility.NoEncode(SearchString.ClientID),Response.Output);%>">
                        <SharePoint:EncodedLiteral ID="EncodedLiteral2" runat="server" Text="<%$Resources:wss,searchresults_searchforitems%>"
                            EncodeMethod='HtmlEncode' />
                    </label>
                </b>
            </td>
        </tr>
        
        <tr>
            <td class="ms-vb">
                <table border="0" cellpadding="0" cellspacing="0">
                    <tr>
                        <td>
                            <asp:DropDownList ID="SearchScope" class="ms-searchbox" ToolTip="<%$Resources:wss,search_searchscope%>"
                                runat="server" />
                        </td>
                        <td>
                            <asp:TextBox ID="SearchString" Columns="40" class="ms-searchbox" AccessKey="S" MaxLength="255"
                                ToolTip="<%$Resources:wss,searchresults_SearchBoxToolTip%>" runat="server" />
                        </td>
                        <td valign="center">
                            <div class="ms-searchimage" style="padding-bottom: 3px">
                                <asp:ImageButton ID="ImgGoSearch" BorderWidth="0" AlternateText="<%$Resources:wss,searchresults_AlternateText%>"
                                    ImageUrl="/_layouts/images/gosearch.gif" runat="server" /></div>
                        </td>\\F:\softwares
                    </tr>
                </table>
            </td>
        </tr>
        -->
        <tr>
            <td colspan="8"> <img src="/_layouts/images/blank.gif" width="1" height="1" alt=""></td>
        </tr>
       
    </table>
</asp:content>
<asp:content id="Content6" contentplaceholderid="PlaceHolderMain" runat="server">
    <asp:PlaceHolder runat="server" ID="SearchSummary">
        
        <table id="TABLE1" width="100%" cellpadding="4" cellspacing="0" border="0">
       
        <tr>
           
            <td id="TD1" colspan="4" >
            
                
                <%
                    
                    
                    GoogleSearchBox gProps = new GoogleSearchBox();
                    NameValueCollection inquery = HttpContext.Current.Request.QueryString;
                    string searchResp;
                    string sitelevel = "";
                    string searchReq = string.Empty;
                    string qQuery = string.Empty;
                    gProps.initGoogleSearchBox();//initialize the SearchBox parameters
                    string finalURL = "";
                    string strURL = "";
                    
                    ////////////////////////////CONSTRUCT THE SEARCH QUERY FOR GOOGLE SEARCH APPLIANCE ///////////////////////////////////
                    //The search query comes in 'k' parameter
                    if (inquery["k"] != null)
                    {
                        qQuery = inquery["k"];
                        if (inquery["cachedurl"] != null)
                        {
                            qQuery = inquery["cachedurl"];
                        }
                        myquery = qQuery;//for paging in custom stylesheet
                        
                        //Using U parameter to create scoped searches on the GSA
                        string temp = "";
                        if (inquery["selectedScope"] != "Enterprise")
                        {
                            /* 
                             * This code will be executed whenever search is performed by the user, except when search is performed
                             * for any suggestions listed, if any.
                             */
                            if ((inquery["u"] != null))
                            {


                                temp = System.Web.HttpUtility.UrlDecode(inquery["u"]);
                                strURL = System.Web.HttpUtility.UrlDecode(inquery["scopeUrl"]);
                            }
                            else if (inquery["sitesearch"] != null) /* This code will be executed only when suggestions are
                                                                         * provided by the GSA. Here, the scope url's value
                                                                         * will be retrieved from the GSA's search request 
                                                                         * 'sitesearch' parameter.
                                                                         */
                            {
                                temp = System.Web.HttpUtility.UrlDecode(inquery["sitesearch"]);
                                strURL = temp;
                            }

                            temp = temp.ToLower();
                            temp = temp.Replace("http://", "");// Delete http from url

                            // The finalURL contains complete URL for the currently selected scope
                            finalURL = strURL;
                            finalURL = finalURL.Replace("'", "");// Removing the single quotes from the URL
                            qQuery += "&sitesearch=" + finalURL;
                        }


                        // Code to log error message if accesslevel parameter value is other than 'a' or 'p'

                        if (WebConfigurationManager.AppSettings["accesslevel"].ToString().Equals("a"))
                        {
                            gProps.log("Access Level parameter value is " + WebConfigurationManager.AppSettings["accesslevel"].ToString() + ", which indicates that both 'public and secure' and 'public' searches can be performed. Enable the Public Search checkbox to perform a public search, and disable the checkbox to perform a public and secure search.", LOG_LEVEL.INFO);
                        }
                        else if (WebConfigurationManager.AppSettings["accesslevel"].ToString().Equals("p"))
                        {
                            gProps.log("Access Level parameter value is " + WebConfigurationManager.AppSettings["accesslevel"].ToString() + ", which indicates that only 'public' search can be performed.", LOG_LEVEL.INFO);
                        }
                        else
                        {
                            gProps.log("Access Level parameter value is " + WebConfigurationManager.AppSettings["accesslevel"].ToString() + ". Permitted values are only 'a' and 'p'. Change the value to either 'a' or 'p'. 'a' indicates a public and secure search, and 'p' indicates a public search.", LOG_LEVEL.ERROR);
                        }

                        /* 
                         * This code will be executed whenever search is performed by the user, except when search is performed
                         * for any suggestions listed, if any. Here, the value for the public search parameter will be retrieved 
                         * from the querystring's 'isPublicSearch' parameter.
                         */
                        if (inquery["isPublicSearch"] != null)
                        {
                            /* 
                            * Here the value for the access parameter will be decided on the basis of the value of
                            * isPublicSearch and the saved web.config file settings.
                            */
                            if (WebConfigurationManager.AppSettings["accesslevel"].ToString().Equals("a"))
                            {
                                if (inquery["isPublicSearch"] == "false")
                                {
                                    gProps.accessLevel = "a"; // Perform 'public and secure search'
                                }
                                else if (inquery["isPublicSearch"] == "true")
                                {
                                    gProps.accessLevel = "p";  // Perform 'public search'
                                }
                                else if(Session["PublicSearchStatus"] != null)
                                {
                                    /*
                                     * If querystring parameter value is null, assign value from the
                                     * Session to the accesslevel search parameter.
                                     */
                                    if (Convert.ToString(Session["PublicSearchStatus"]) == "false")
                                    {
                                        gProps.accessLevel = "a"; // Perform 'public and secure search'
                                    }
                                    else if (Convert.ToString(Session["PublicSearchStatus"]) == "true")
                                    {
                                        gProps.accessLevel = "p";  // Perform 'public search'
                                    }
                                }
                            }
                            else if (WebConfigurationManager.AppSettings["accesslevel"].ToString().Equals("p"))
                            {
                                gProps.accessLevel = "p";  // Perform 'public search'
                            }
                        }
                        else     /* 
                                  * This code will be executed only when suggestions are provided by the GSA. Here, the scope url's value
                                  * will be retrieved from the GSA's search request 'access' parameter.
                                  */
                        {
                            if (inquery["access"] != null)
                            {
                                string publicSearchCheckboxStatus = inquery["access"].ToString();
                                if (publicSearchCheckboxStatus == "a")
                                {
                                    gProps.accessLevel = "a"; // Perform 'public and secure search'
                                }
                                else
                                {
                                    gProps.accessLevel = "p";  // Perform 'public search'
                                }
                            }
                        }
                       

                        /*
                         * Adding filter parameter to the search request sent to GSA. Value for the filter parameter will be set into the 
                         * web.config file of SharePoint web application, at the time of installation, and can be updated using search 
                         * box configuration wizard screen, at post-installation time. 
                         */
                        searchReq = "?q=" + qQuery + "&access=" + gProps.accessLevel + "&getfields=*&output=xml_no_dtd&ud=1" + "&oe=UTF-8&ie=UTF-8&exclude_apps=1&site=" + gProps.siteCollection + "&filter=" + WebConfigurationManager.AppSettings["filterParameter"].ToString();
                        if (gProps.frontEnd.Trim() != "")
                        {
                            //check for the flag whether to enable custom styling locally or use GSA style
                            if (gProps.bUseGSAStyling == true)
                            { 
                                searchReq += "&proxystylesheet=" + gProps.frontEnd /*+ "&proxyreload=1"*/;
                            }
                            searchReq += "&client=" + gProps.frontEnd;
                        }

                        /*For supporting paging using GSA stylesheet on GSA search results*/
                        if (inquery["start1"] != null)
                        {
                            searchReq = searchReq + "&start=" + inquery["start1"] /*+ "&num=" + num*/ ;// XenL - fixing MH Paging solution
                        }

                        /*sorting of search results*/
                        if ((inquery["v1"] != null) && (inquery["v1"] == "date"))
                        {
                            searchReq += "&sort=date%3AD%3AS%3Ad1";//Sorting by date
                        }
                        if ((inquery["v1"] != null) && (inquery["v1"] == "relevance"))
                        {
                            searchReq += "&sort=relevance";//Sorting by relevance
                        }

                        /*Handle paging for the custom stayle sheet which is deployed locally*/
                        if (gProps.bUseGSAStyling == false)
                        {
                            if (inquery["start"] != null)
                            {
                                try
                                {
                                    start = Int32.Parse(inquery["start"]);
                                }
                                catch (Exception e)
                                {
                                    gProps.log("Unable to get value for start of page, Error= " + e.Message + "\n Trace=" + e.StackTrace, LOG_LEVEL.ERROR);
                                }
                            }
                            searchReq += "&start=" + start  + "&num=" + num ;
                        }
                    }
                    else
                    {
                        searchReq = HttpContext.Current.Request.Url.Query;
                        if (gProps.embeddedModeQueryArg == "") {
                          // Decoding the URL received from the current request
                          // if we are NOT running in embedded mode.
                          searchReq = HttpUtility.UrlDecode(searchReq); 
                        }
                    }
                    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                    /////////////////////MAKING THE SEARCH REQUEST TO GOOGLE SEARCH APPLIANCE /////////////////////////////////////////////
                    try
                    {
                        HttpWebRequest objReq = null;
                        HttpWebResponse objResp = null;
                        Stream objStream = null;
                        StreamReader objSR = null;
                        CookieContainer cc = new CookieContainer();
                        int i;
			String GSASearchUrl= gProps.GSALocation + "/search" +
                            searchReq + gProps.embeddedModeQueryArg;
                        ////////////////////////////// PROCESSING THE RESULTS FROM THE GSA/////////////////
                        objResp = (HttpWebResponse)gProps.GetResponse(false, GSASearchUrl,null,null);//fire getresponse
                        string contentEncoding = objResp.Headers["Content-Encoding"];
                        string returnstring = "";//initialize the return string
                        objStream = objResp.GetResponseStream();//if the request is successful, get the results in returnstring
                        returnstring = gProps.GetContentFromStream(contentEncoding, objStream);
                        gProps.log("Return Status from GSA: " + objResp.StatusCode,LOG_LEVEL.INFO);
                        int FirstResponseCode = (int)objResp.StatusCode;//check the response code from the reply from 
                        
                        //*********************************************************************
                        //Manually handling the Redirect from SAML bridge. Need to extract the Location and the GSA session Cookie
                        string newURL = objResp.Headers["Location"]; 
                        string GSASessionCookie = objResp.Headers["Set-Cookie"]; 
                        //*********************************************************************

                        CookieContainer newcc = new CookieContainer();//Added for SAML
                        //if(GSASessionCookie!=null){
                        /*handling for redirect*/
                        if (FirstResponseCode==302)
                        {
                            gProps.log("The Response is being redirected to location " + newURL, LOG_LEVEL.INFO);
                            Cookie responseCookies= new Cookie();;//add cookies in GSA response to current response
                            int j;
                            for (j = 0; j < objResp.Cookies.Count -1; j++)
                            {
                                responseCookies.Name = objResp.Cookies[j].Name;
                                Encoding utf8 = Encoding.GetEncoding("utf-8");
                                string value = objResp.Cookies[j].Value;
                                responseCookies.Value = HttpUtility.UrlEncode(value, utf8); 
                                responseCookies.Domain = objReq.RequestUri.Host;
                                responseCookies.Expires = objResp.Cookies[j].Expires;
                                
                                /*Cookie Information*/
                                gProps.log("Cookie Name= " + responseCookies.Name + "| Value= " + value + "| Domain= " + responseCookies.Domain
                                    + "| Expires= " + responseCookies.Expires.ToString(), LOG_LEVEL.INFO);
                                
                                ///* 
                                // * The 'secure' cookie issue - Setting for secure cookie, which will decide whether the secure cookie should be passed on for processing or not.
                                // * Value 'false' indicates that cookie will be not be dropped, and value 'true' indicates that the cookie will be dropped.
                                // */

                                if (responseCookies.Name.ToLower() == secureCookieToBeDiscarded)
                                {
                                    bool secureCookieDiscardDecision = gProps.CheckCookieToBeDroppedAndLogMessage(WebConfigurationManager.AppSettings["omitSecureCookie"], responseCookies.Name.ToLower(), secureCookieToBeDiscarded, value);
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
                            gProps.log("Adding cookies: " + GSASessionCookie, LOG_LEVEL.INFO);
                            
                            /*Break multiple cookie based on semi-colon as separator*/
                            Char[] seps = {';'};
                            if (GSASessionCookie != null)
                            {
                                
                                String[] key_val = GSASessionCookie.Split(seps);
                                
                                /*check if there is atleast one cookie in the set-cookie header*/
                                if ((key_val != null) && (key_val[0] != null))
                                {
                                    foreach(String one_cookie in key_val)
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

                                        if(one_cookie.Contains("="))
                    					{
                        					String[] Cookie_Key_Val = one_cookie.Trim().Split(Seps_Each_Cookie, 2);
                                            name = Cookie_Key_Val[0];
                        					value = Cookie_Key_Val[1];
                    					}
                                        else
                                        {
                                            name=one_cookie.Trim();
                                            gProps.log("The cookie contains only key '"+name+"'without any value", LOG_LEVEL.INFO);
                                            value="";
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
                                            bool secureCookieDiscardDecision = gProps.CheckCookieToBeDroppedAndLogMessage(WebConfigurationManager.AppSettings["omitSecureCookie"], responseCookies.Name.ToLower(), secureCookieToBeDiscarded, value);
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
                                        gProps.log("Cookie Name= " + responseCookies.Name
                                            + "| Value= " + value
                                            + "| Domain= " + GoogleUri.Host
                                            + "| Expires= " + responseCookies.Expires, LOG_LEVEL.INFO);
                                    }
                                }//end: if ((key_val != null) && (key_val[0] != null))
                            }

                            HttpWebResponse objNewResp =  (HttpWebResponse)gProps.GetResponse(true, GSASearchUrl, newcc, objResp);//fire getresponse
                            contentEncoding = objResp.Headers["Content-Encoding"];
                            returnstring = "";//initialize the return string
                            Stream objNewStream = objNewResp.GetResponseStream();//if the request is successful, get the results in returnstring
                            returnstring = gProps.GetContentFromStream(contentEncoding, objNewStream);
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
                                    bool secureCookieDiscardDecision = gProps.CheckCookieToBeDroppedAndLogMessage(WebConfigurationManager.AppSettings["omitSecureCookie"], objResp.Cookies[j].Name.ToLower(), secureCookieToBeDiscarded, objResp.Cookies[j].Value);
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
                                
                                responseCookies = null;

                                /*Cookie Information*/
                                gProps.log("Cookie Name= " + objResp.Cookies[j].Name
                                    + "| Value= " + objResp.Cookies[j].Value
                                    + "| Domain= " + objReq.RequestUri.Host
                                    + "| Expires= " + responseCookies.Expires, LOG_LEVEL.INFO);
                            }                         
                         }//end if condition for SAML
                        // ********************************************


                        int statusCode;// get the statusCode of the GSA response
                        try
                        {
                            statusCode = (int)objResp.StatusCode;
                            statusCode=200; //Set the status code as OK. 
                                            //In case of error from call to GSA, just display the error message to user
                        }
                        catch (WebException ex)
                        {
                            isNext = false;//hide next
                            if (ex.Response == null)
                                throw;// nested throw

                            statusCode = (int)((HttpWebResponse)ex.Response).StatusCode;
                            if (statusCode != 200)
                            {
                                gProps.log("Returning the result, Status code=" + statusCode, LOG_LEVEL.ERROR);
                                Response.Write(ex.Message);
                            }
                        }
                        
                        
                        ////////////////process results////////
                        if (gProps.bUseGSAStyling == false)
                        {
                            try
                            {
                                XmlDocument xd = new XmlDocument();
                                try
                                {
                                    xd.LoadXml(returnstring);//load the results for parsing
                                }
                                catch (XmlException e)
                                {
                                    gProps.log("Unable to load the GSA result", LOG_LEVEL.ERROR);
                                }
                                
                                ////////////////start and end boundaries/////////////////////////
                                try
                                {
                                    //for getting search time                                    
                                    /*string myPattern = "/GSP/TM";
                                    XmlNode node = xd.SelectSingleNode(myPattern);
                                    searchtime = node.InnerText;*/

                                    string myPattern = "/GSP/RES";//pre start    
                                    XmlNode node = xd.SelectSingleNode(myPattern);
                                    if ((node != null) && (node.Attributes != null))
                                    {
                                        try
                                        {
                                            endB = Int32.Parse(node.Attributes["EN"].Value);
                                            if (endB < (start + num))
                                            {
                                                isNext = false;//page is ended
                                            }
                                            else
                                            {
                                                isNext = true;//more pages
                                            }
                                        }
                                        catch (Exception e)
                                        {
                                            gProps.log("Problems while parsing end search result boundary. \nTrace:" + e.StackTrace, LOG_LEVEL.ERROR);
                                            endB = 0;
                                            isNext = false;//hide next
                                        }
                                    }
                                    else 
                                    {
                                        isNext = false;//hide next
                                    }
                                    
                                }
                                catch (Exception e)
                                {
                                    gProps.log("Problems while parsing GSA results. \nTrace:" + e.StackTrace, LOG_LEVEL.ERROR);
                                    isNext = false;//hide next
                                }
                                
                                /////////////////////////////////////
                                string res = GoogleSearchBox.transform(returnstring, gProps.xslt1);//Transform1: From GSA result to SP-like result (xml)
                                returnstring = GoogleSearchBox.transform(res, gProps.xslt2);//Transform2: From SP-like result(xml) to SP-Like (HTML) result
                            }
                            catch (Exception e)
                            {
                                gProps.log("Exception while applying transformations to GSA results: " + e.Message + "\nStack Trace: " + e.StackTrace, LOG_LEVEL.ERROR);
                                isNext = false;//hide next
                            }
                        }
                        /////////////////////////////
                        
                        HttpContext.Current.Response.StatusCode = statusCode;//set the GSA response status code to current response

                        /*close and dispose the stream*/
                        objResp.Close();
                        objStream.Close();
                        objStream.Dispose();
                        
                        if(null!=objSR)
                        {
                            objSR.Close();
                            objSR.Dispose();
                        }

                        if (statusCode == 200)
                        {
                            HttpContext.Current.Response.Write(returnstring);
                        }

                    }
                    catch (Exception ex)
                    {
                        isNext = false;//hide next
                        gProps.log("Exception while searching on GSA: " + ex.Message+"\nException Trace: " + ex.StackTrace,LOG_LEVEL.ERROR);
                        HttpContext.Current.Response.Write(ex.Message);
                    }
        
                %>
                
                 
            </td>
           
        </tr>
        
        <tr>
            <td id="prevPage" colspan="2" style="width:auto;height:auto;" align="right">
            <% 
                tempvar = "";

                if (gProps.bUseGSAStyling == false)
                {
                    if (start < 1)
                    {
                        tempvar = "";//do not show prev
                    }
                    else
                    {
                        tempvar = "PreviousPage";//show prev
                    }
                }
            %>
            
              <a href="<%=PAGENAME%>?k=<%=myquery%>&start=<%=start-num%>"><%=tempvar%></a>    
             </td>
             
             <td id="NextPage" colspan="4" style="width:auto;height:auto;" align="left">
             
            <% 
                if ((gProps.bUseGSAStyling == false) && (isNext ==true))
                {
                    if (start >= 1)
                    {
                        tempvar = "| NextPage";//show next page tag
                    }
                    else {
                        tempvar = "NextPage";//show next page tag
                    }
                }
                else
                {
                    tempvar = "";//hide next page
                }
                
            %>
            
              <a href="<%=PAGENAME%>?k=<%=myquery%>&start=<%=start+num%>"><%=tempvar %></a> 
              
             </td>
             
             
        </tr>
        </table>
    </asp:PlaceHolder>
</asp:content>
