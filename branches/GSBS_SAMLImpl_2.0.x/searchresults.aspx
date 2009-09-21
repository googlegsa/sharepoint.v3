<%@ Assembly Name="Microsoft.SharePoint.ApplicationPages, Version=12.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Page Language="C#" Inherits="Microsoft.SharePoint.ApplicationPages.SearchResultsPage"
    MasterPageFile="/_layouts/application.master" EnableViewState="false" EnableViewStateMac="false"
    ValidateRequest="false" %>
<%@ Register TagPrefix="wssawc" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=12.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register TagPrefix="SharePoint" Namespace="Microsoft.SharePoint.WebControls"
    Assembly="Microsoft.SharePoint, Version=12.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register TagPrefix="SearchWC" Namespace="Microsoft.SharePoint.Search.Internal.WebControls"
    Assembly="Microsoft.SharePoint.Search, Version=12.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register TagPrefix="SharePoint" Namespace="Microsoft.SharePoint.WebControls"
    Assembly="Microsoft.SharePoint, Version=12.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register TagPrefix="Utilities" Namespace="Microsoft.SharePoint.Utilities" Assembly="Microsoft.SharePoint, Version=12.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>

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
    /**
     * Author: Amit Agrawal
     * Email: amit.persistent@gmail.com
     **/
        
        public const int num = 10;//page size
        public string myquery = "";
        public const String PREV = "Previous";
        public const String NEXT = "Next";
        public const String PAGENAME = "searchresults.aspx";
        public string tempvar = "";
        public const string PREVSTMT = "";//initially prev should be hidden
        public const string NEXTSTMT = "";
        public bool isNext = true;
        public int start = 0;/* E.g. start = 1 and num =5 (return 11-15 results)*/
        int endB = 0;
        
        
        /*Google Search Box for SharePoint*/
        class GoogleSearchBox
        {
            public string GSALocation;
            public string accessLevel="a";//Do a Public and Secured search
            public string siteCollection;
            public string frontEnd;
            public string enableInfoLogging;
            public Boolean bUseGSAStyling = true;
            public string xslGSA2SP;
            public string xslSP2result;
            public string temp="true";
			public const String PRODUCTNAME = "Google Search Box for SharePoint";

            /*For Internal Transformations*/
            public XslTransform xslt1 = null;
            public XslTransform xslt2 = null;
            
            public GoogleSearchBox()
            {
                GSALocation = "";
                siteCollection = "default_collection";
                frontEnd = "default_frontend";
                enableInfoLogging = "true";
                xslGSA2SP = "";
                xslSP2result = "";
            }

            //Method to extract configuration properties into GoogleSearchBox
            public void initGoogleSearchBox()
            {
                GSALocation = WebConfigurationManager.AppSettings["GSALocation"];
                if((GSALocation==null) || (GSALocation.Trim().Equals("")))
                {
                    log("Google Search Appliance location is not specified", EventLogEntryType.Error);//log error
                    HttpContext.Current.Response.Write("Google Search Appliance location is not specified");
                    HttpContext.Current.Response.End();
                }
                
                siteCollection = WebConfigurationManager.AppSettings["siteCollection"];
                if((siteCollection==null) || (siteCollection.Trim().Equals("")))
                {
                    log("Site collection value for Google Search Appliance is not specified", EventLogEntryType.Error);//log error
                    HttpContext.Current.Response.Write("Site collection value for Google Search Appliance is not specified");
                    HttpContext.Current.Response.End();
                }
                
                enableInfoLogging = WebConfigurationManager.AppSettings["verbose"];
                if((enableInfoLogging==null) || (enableInfoLogging.Trim().Equals("")))
                {
                    log("Log level is not specified", EventLogEntryType.Error);//log error
                    HttpContext.Current.Response.Write("Log level is not specified");
                    HttpContext.Current.Response.End();
                }
                
                frontEnd = WebConfigurationManager.AppSettings["frontEnd"];
                if((frontEnd==null) || (frontEnd.Trim().Equals("")))
                {
                    log("Front end value for Google Search Appliance is not specified", EventLogEntryType.Error);//log error
                    HttpContext.Current.Response.Write("Front end value for Google Search Appliance is not specified");
                    HttpContext.Current.Response.End();
                }
                
                String temp = WebConfigurationManager.AppSettings["GSAStyle"];
                if((temp==null) || (temp.Trim().Equals("")))
                {
                    log("Please specify value for GSA Style. Specify 'true' to use Front end's style for rendering search results. Specify 'False' to use the locally deployed stylesheet for rendering search results", EventLogEntryType.Error);//log error
                    HttpContext.Current.Response.Write("Please specify value for GSA Style. Specify 'true' to use Front end's style for rendering search results. Specify 'False' to use the locally deployed stylesheet for rendering search results");
                    HttpContext.Current.Response.End();
                }
                
                if (temp.ToLower().Equals("true"))
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
                        log("Please specify the value for stylesheet to convert GSA results to SharePoint like results", EventLogEntryType.Error);//log error
                        HttpContext.Current.Response.Write("Please specify the value for stylesheet to convert GSA results to SharePoint like results");
                        HttpContext.Current.Response.End();
                    }
                    
                    
                    if((xslSP2result==null) || (xslSP2result.Trim().Equals("")))
                    {
                        log("Please specify the value for stylesheet to be applied on search  results", EventLogEntryType.Error);//log error
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
                    /*preload the stylesheet.. for performance reasons*/
                    xslt1 = new XslTransform();
                    xslt1.Load(xslGSA2SP);//read XSLT

                    xslt2 = new XslTransform();
                    xslt2.Load(xslSP2result);//read XSLT
                }
                catch (Exception e)
                {
                    log("problems while loading stylesheet, message=" + e.Message + "\nTrace: " + e.StackTrace, EventLogEntryType.Error);
                }
                
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
            public void log(String msg, EventLogEntryType logLevel)
            {
                if(null!=msg)
                {
					//Non-error messages should be displayed only if verbose =true
					if (logLevel != EventLogEntryType.Error)
					{
						if ((enableInfoLogging != null) && (enableInfoLogging.ToLower().Equals("true")))
						{
							System.Diagnostics.EventLog.WriteEntry(PRODUCTNAME,msg, logLevel);
						}
					}
					else
					{
						System.Diagnostics.EventLog.WriteEntry(PRODUCTNAME,msg, logLevel);
					}
				}
            }

            
            ///<summary>
            ///Perform User Impersonation 
            /// </summary>
            /// <param name="userPrincipalName"></param>
            /// <returns></returns>
            public System.Security.Principal.WindowsImpersonationContext Impersonate(string userPrincipalName)
            {
                System.Security.Principal.WindowsImpersonationContext impersonationContext = null;
                try
                {
                    log("Impersonating user " + userPrincipalName, EventLogEntryType.Information);
                    if ((userPrincipalName != null) && (!userPrincipalName.Trim().Equals("")))
                    {
                        try
                        {
                            string currentuser = getCurrentUser();
                            log("CurrentUser(Before Impersonation):" + currentuser, EventLogEntryType.Information);
                            WindowsIdentity userId = new WindowsIdentity(userPrincipalName);// create new identity using new primary token
                            impersonationContext = userId.Impersonate();
                            //impersonationContext.Undo();//undo the Impersonation
                            log("Impersonation succeeded: UserToken: " + userId.Token + ", AuthenticationType=" + userId.AuthenticationType + "Impersonation level= " + userId.ImpersonationLevel, EventLogEntryType.Information);
                        }
                        catch (Exception e)
                        {
                            log("Impersonation exception: " + e.Message + "\nStack Trace=" + e.StackTrace, EventLogEntryType.Error);
                        }
                    }
                    else
                    {
                        log("Impersonation Failed: Incorrect user name specified", EventLogEntryType.Error);
                    }
                }
                catch (Exception e)
                {
                    log("Impersonation Failed"+e.Message+"\nStack Trace: "+e.StackTrace, EventLogEntryType.Error);
                }
                
                return impersonationContext;
            }

            /// <summary>
            /// Gets the currently logged-in user
            /// </summary>
            /// <returns></returns>
            public String getCurrentUser()
            {
                return WindowsIdentity.GetCurrent().Name;
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

    <!--custom script-->

    <script type="text/javascript">
   
  	function _spFormOnSubmit()
	{
		return GoSearch();
	}
	function SetPageTitle()
	{
	   var Query = "";
	   if (window.top.location.search != 0)
	   {
		  Query = window.top.location.search;
		  var keywordQuery = getParameter(Query, 'k');
		  if(keywordQuery != null)
		  {
            
            //set the value of query
            var myTextField = document.getElementById('idSearchString');
        	if(myTextField.value != "")
        	{
		        myTextField.value=keywordQuery;
		    }

		    if(keywordQuery!="")
		    {
			 var titlePrefix = '<asp:Literal runat="server" text="<%$Resources:wss,searchresults_pagetitle%>"/>';
			 document.title = titlePrefix + ": " +keywordQuery;
			 }
		  }
	   }	 
	}
		
	function getParameter (queryString, parameterName)
	{
	   
	   var parameterName = parameterName + "=";
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
			
			//alert('querystring='+queryString);
			//alert('parameterName='+parameterName);
			//alert('get param:'+decodeURIComponent(queryString.substring (begin, end)));
			
			var x = document.getElementById("idSearchString");
			var mystring = decodeURIComponent(queryString.substring (begin, end))
			x.value=mystring;
			
			var myindex = mystring.indexOf('cache:');
			if(myindex>-1)
			{
			    x.value="";//for cached result do not show the search string as it looks wierd
			}
			//alert(myindex);
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
    ms-searchresultsareaSeparator
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
                <SharePoint:DelegateControl ID="DelegateControl1" runat="server" ControlId="SmallSearchInputBox" />
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
                    
                    ////////////////////////////CONSTRUCT THE SEARCH QUERY FOR GOOGLE SEARCH APPLIANCE ///////////////////////////////////
                    //Amit: The search query comes in 'k' parameter
                    if (inquery["k"] != null)
                    {
                        qQuery = inquery["k"];
                        if (inquery["cachedurl"] != null)
                        {
                            qQuery = inquery["cachedurl"];
                        }
                        myquery = qQuery;//for paging in custom stylesheet

                        //XenL:Using U parameter to create scoped searches on the GSA
                        if ((inquery["u"] != null))
                        {
                            string port = "";
                            string temp = System.Web.HttpUtility.UrlDecode(inquery["u"]);
                            temp = temp.ToLower();
                            temp = temp.Replace("http://", "");// Delete http from url
                            qQuery += " inurl:\"" + temp + "\"";//  Change functionality to use "&sitesearch="  - when GSA Bug 11882 has been closed
                        }

                        /*Get the user suppiled parameters from the web.config file*/
                        searchReq = "?q=" + qQuery + "&access=" + gProps.accessLevel + "&getfields=*&output=xml_no_dtd&ud=1" + "&oe=UTF-8&ie=UTF-8&site=" + gProps.siteCollection;
                        if (gProps.frontEnd.Trim() != "")
                        {
                            //Amit: check for the flag whether to enable custom styling locally or use GSA style
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
                                    gProps.log("Unable to get value for start of page, Error= " + e.Message +"\n Trace="+e.StackTrace,EventLogEntryType.Error);
                                }
                            }
                            searchReq += "&start=" + start  + "&num=" + num ;
                        }
                    }
                    else
                    {
                        searchReq = HttpContext.Current.Request.Url.Query;
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
						gProps.log("Search Request to GSA:" + gProps.GSALocation + "/search" + searchReq,EventLogEntryType.Information);
						String GSASearchUrl= gProps.GSALocation + "/search" + searchReq;
						
                        objReq = (HttpWebRequest)HttpWebRequest.Create(GSASearchUrl);
                        objReq.KeepAlive =true;//objReq.KeepAlive =true;
                        objReq.AllowAutoRedirect=false;//objReq.AllowAutoRedirect=true;
                        objReq.MaximumAutomaticRedirections=100;
                        objReq.Credentials = System.Net.CredentialCache.DefaultCredentials;//set credentials
                        //objReq.PreAuthenticate = true;//preauthenticate the request incase ifauthentication header is available with the request
                        
                        /*handling for the certificates*/
                        ServicePointManager.ServerCertificateValidationCallback += new System.Net.Security.RemoteCertificateValidationCallback(customXertificateValidation);
                        objReq.Method = "GET";//specify the request handler
                        
                        
                        //////////////////////COPYING THE CURRENT REQUEST PARAMETRS, HEADERS AND COOKIES ///////////////////////                        
                        /*Amit: Copying all the current request headers to the new request to GSA. Some headers might not be copied .. skip those headers and copy the rest*/
                        String[] requestHeaderKeys = HttpContext.Current.Request.Headers.AllKeys;//add headers available in current request to the GSA request
                        for (i = 0; i < HttpContext.Current.Request.Headers.Count - 1; i++)
                        {
                            try
                            {
                                objReq.Headers.Add(requestHeaderKeys[i], HttpContext.Current.Request.Headers[requestHeaderKeys[i]]);
                            }
                            catch (Exception HeaderEx)
                            { 
                                //just skipping the header information if any exception occures while adding to the GSA request
                                //gProps.log("Exception while adding headers to request: " + HeaderEx.Message+"::Stack Trace: " + HeaderEx.StackTrace, EventLogEntryType.Error);
                            }
                        }
                        
                        /*Amit: copying all the cookies from current request to the new request to GSA*/
                        Cookie c = new Cookie();//add cookies available in current request to the GSA request
                        for (i = 0; i < HttpContext.Current.Request.Cookies.Count - 1; i++)
                        {
                            string tempCookieName= HttpContext.Current.Request.Cookies[i].Name;
                            
                            /* skip GSA session..for SAML bridge.. This will do authN and AuthZ again.
                            If we do not skip this cookie then it return garbage search results for the consecutive searches from SearchBox */
                            
                            //if(!tempCookieName.Equals("GSA_SESSION_ID")){
                            c.Name = tempCookieName;
                            Encoding utf8 = Encoding.GetEncoding("utf-8");
                            string value = HttpContext.Current.Request.Cookies[i].Value;
                            c.Value = HttpUtility.UrlEncode(value, utf8); 
                            c.Domain = objReq.RequestUri.Host;
                            c.Expires = HttpContext.Current.Request.Cookies[i].Expires;
                            cc.Add(c);
                            //}
                        }
                        
                        
                        //////////////////////////////////////////////////////////////
						//UserImpersonation
						//System.Security.Principal.WindowsImpersonationContext impersonationContext = gProps.Impersonate("googlesp@gdc-psl.net");
                        //impersonationContext.Undo();//undo the Impersonation
						//gProps.Impersonate("farmadmin@gdc-psl.net");
						////////////////////////////////////////////////////////////////
						
                        objReq.CookieContainer = cc;//Amit: Set GSA request cookiecontainer
                        requestHeaderKeys = null;
                        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                        
                        ////////////////////////////// PROCESSING THE RESULTS FROM THE GSA/////////////////
                        objResp = (HttpWebResponse)objReq.GetResponse();//fire getresponse
                        objStream = objResp.GetResponseStream();//if the request is successful, get the results in returnstring
                        objSR = new StreamReader(objStream, Encoding.UTF8);//we have set in the URL to get the result in the UTF-8 encoding format
                        string returnstring = (objSR.ReadToEnd());// read the content from the stream
                                         
                        //impersonationContext.Undo();//undo the Impersonation
                        gProps.log("Return Status from GSA: " + objResp.StatusCode,EventLogEntryType.Information);
                        int FirstResponseCode = (int)objResp.StatusCode;//check the response code from the reply from 
                        
                        //*********************************************************************
                        //Manually handling the Redirect from SAML bridge. Need to extract the Location and the GSA session Cookie
                        string newURL = objResp.Headers["Location"]; 
                        string GSASessionCookie = objResp.Headers["Set-Cookie"]; 
                        //gProps.log("GSA Session Cookie: " + GSASessionCookie,EventLogEntryType.Information);
                        //*********************************************************************

                        //gProps.log("We see the cookies.. "+objResp.Cookies.Count, EventLogEntryType.Information);
                        CookieContainer newcc = new CookieContainer();//Amit: added for SAML
                        
                        if(GSASessionCookie!=null){
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
                                newcc.Add(responseCookies);                            
                                //gProps.log("Response Cookie:: Name:"+responseCookies.Name+"::Value:"+responseCookies.Value+"::Domain:"+responseCookies.Domain,EventLogEntryType.Error);
                            }

                            // ********************************************
                            
                            /*Amit: We need to check if there is a cookie or not. For the initial request to GSA in case of SAML it will be there. Forget it for afterwards*/
                        
                            Char[] seps = {'='};//Adding the session cookie
                            String[] key_val = GSASessionCookie.Split(seps);
                            
                            if((key_val!=null)&& (key_val[0]!=null))
                            {
                                    responseCookies.Name = objResp.Cookies[j].Name;
                                    responseCookies.Value = key_val[1];
                                    responseCookies.Domain = objReq.RequestUri.Host;
                                    responseCookies.Expires = DateTime.Now.AddDays(1);//add 1 day from now 
                                    newcc.Add(responseCookies);
                                    //gProps.log("Cookie"+key_val[0]+"::"+key_val[1]+"::"+objReq.RequestUri.Host+"::"+responseCookies.Expires, EventLogEntryType.Information);
                            }
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
                                HttpContext.Current.Response.Cookies.Add(responseCookies);
                                responseCookies = null;
                            }                         
                         }//end if condition for SAML
                         
                         
                        // ********************************************

                        // *********************** Now we make another request ************
                        HttpWebRequest objNewReq = null;
                        if(newURL!=null){
                            objNewReq = (HttpWebRequest)HttpWebRequest.Create(newURL);
                            objNewReq.KeepAlive =true;
                            objNewReq.AllowAutoRedirect=true;
                            objNewReq.MaximumAutomaticRedirections=100;
                            objNewReq.Credentials = System.Net.CredentialCache.DefaultCredentials;//set credentials
                            ServicePointManager.ServerCertificateValidationCallback += new System.Net.Security.RemoteCertificateValidationCallback(customXertificateValidation);//security certificate handler
                            objNewReq.Method = "GET";//specify the request handler
                            objNewReq.CookieContainer = newcc;//Amit: Set GSA request cookiecontainer
                         }
                            
                            // ******************************************


                            //gProps.log("Now we see the Response Headers.. "+objResp.Headers.Count, EventLogEntryType.Information);
                            string[] headerKeys = objResp.Headers.AllKeys;//add headers in GSA response to current response
                            for (i = 0; i < objResp.Headers.Count; i++)
                            {
                                try
                                {
                                    //skip the Set-Cookie header
                                    if((headerKeys[i]!="Set-Cookie")&&(headerKeys[i]!="Location"))
                                    {
                                        objNewReq.Headers.Add(headerKeys[i], objResp.Headers[headerKeys[i]]);//added for SAML
                                    
                                        //skipping the content-length
                                        if (headerKeys[i] != "Content-Length")
                                        {
                                            HttpContext.Current.Response.AppendHeader(headerKeys[i], objResp.Headers[headerKeys[i]]);
                                        }
                                    }
                                }
                                catch (Exception e)//just skipping the header information if any exception occures while adding headers received from GSA response
                                {
                                    //gProps.log("Exception while appending headers to response: " + e.Message+ "\nStack Trace: " + e.StackTrace, EventLogEntryType.Error);    
                                }
                            }

                            headerKeys = null;
                            
                            
                            // ************************ making and checking the newResponse ********************************************
                             
                            if(GSASessionCookie!=null){
                                HttpWebResponse objNewResp = (HttpWebResponse)objNewReq.GetResponse();//fire getresponse
                                Stream objNewStream = objNewResp.GetResponseStream();//if the request is successful, get the results in returnstring
                                StreamReader objNewSR = new StreamReader(objNewStream, Encoding.UTF8);//we have set in the URL to get the result in the UTF-8 encoding format
                                //string returnstringNew = (objSR.ReadToEnd());// read the content from the stream                                              
                                returnstring = (objNewSR.ReadToEnd());// read the content from the stream 
                            }   
                                                                
                        // **********************************************************************
                        
                        int statusCode;// get the statusCode of the GSA response
                        try
                        {
                            statusCode = (int)objResp.StatusCode;
                            statusCode=200;//amit
                        }
                        catch (WebException ex)
                        {
                            isNext = false;//hide next
                            if (ex.Response == null)
                                throw;// nested throw

                            statusCode = (int)((HttpWebResponse)ex.Response).StatusCode;
                            if (statusCode != 200)
                            {
                                gProps.log("Returning the result, Status code=" + statusCode, EventLogEntryType.Error);
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
                                    gProps.log("Unable to load the GSA result", EventLogEntryType.Error);
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
                                            gProps.log("Problems while parsing end search result boundary. \nTrace:" + e.StackTrace, EventLogEntryType.Error);
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
                                    gProps.log("Problems while parsing GSA results. \nTrace:" + e.StackTrace, EventLogEntryType.Error);
                                    isNext = false;//hide next
                                }
                                
                                /////////////////////////////////////
                                string res = GoogleSearchBox.transform(returnstring, gProps.xslt1);//Transform1: From GSA result to SP-like result (xml)
                                returnstring = GoogleSearchBox.transform(res, gProps.xslt2);//Transform2: From SP-like result(xml) to SP-Like (HTML) result
                            }
                            catch (Exception e)
                            {
                                gProps.log("Exception while applying transformations to GSA results: " + e.Message + "\nStack Trace: " + e.StackTrace, EventLogEntryType.Error);
                                isNext = false;//hide next
                            }
                        }
                        /////////////////////////////
                        
                        HttpContext.Current.Response.StatusCode = statusCode;//set the GSA response status code to current response

                        /*close and dispose the stream*/
                        objResp.Close();
                        objStream.Close();
                        objStream.Dispose();
                        objSR.Close();
                        objSR.Dispose();

                        if (statusCode == 200)
                        {
                            HttpContext.Current.Response.Write(returnstring);
                        }

                    }
                    catch (Exception ex)
                    {
                        isNext = false;//hide next
                        gProps.log("Exception while searching on GSA: " + ex.Message+"\nException Trace: " + ex.StackTrace,EventLogEntryType.Error);
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
