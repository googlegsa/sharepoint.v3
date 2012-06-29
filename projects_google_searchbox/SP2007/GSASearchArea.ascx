<%@ Control Language="C#" Inherits="Microsoft.SharePoint.WebControls.SearchArea,Microsoft.SharePoint,Version=12.0.0.0,Culture=neutral,PublicKeyToken=71e9bce111e9429c"
    CompilationMode="Always" AutoEventWireup="false"  %>
<%@ Register TagPrefix="wssawc" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=12.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register TagPrefix="SharePoint" Namespace="Microsoft.SharePoint.WebControls"
    Assembly="Microsoft.SharePoint, Version=12.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register TagPrefix="SharePoint" Namespace="Microsoft.SharePoint.WebControls"
    Assembly="Microsoft.SharePoint, Version=12.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register TagPrefix="Utilities" Namespace="Microsoft.SharePoint.Utilities" Assembly="Microsoft.SharePoint, Version=12.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Import Namespace="Microsoft.SharePoint" %>
<%@ Import Namespace="System.Web.Configuration" %>
<%@ Import Namespace="System.IO" %>

<%
    const string PublicAndSecureSearch = "publicAndSecure";
    const string PublicSearch = "public";
    const string SecureOnly = "secureOnly";
    string accessStatus = "";
    string searchQuery = "";
    
    // Setting the URL for the Search Tips Link 
    string SearchTipsHtmlPageURL = WebConfigurationManager.AppSettings["GSALocation"].ToString() + "/" + WebConfigurationManager.AppSettings["SearchTipsHTMLFileName"].ToString();
    linkSearchTips.NavigateUrl = SearchTipsHtmlPageURL;
      
    /*
     * Checking if session value is null for setting the initial type of search. Session value null means 
     * either the user is done with the searching or he/ she has opened the web application for the first time
     * in the browser.
     */
    string defaultSearchType = WebConfigurationManager.AppSettings["defaultSearchType"].ToString();
    if (Session["PublicSearchStatus"] == null)
    {
        // Getting the default search type from web.config file.
        
        if (defaultSearchType == PublicAndSecureSearch)
        {
            /*
             * If default search type is 'public and secure', set the status of the 
             * controls, namely, public search checkbox the hfPublicSearch hiddenfield
             * to boolean value false.
             */
            setInitialStatusForUIControlsAsPerSearchType(false);
        }
        else if (defaultSearchType == PublicSearch)
        {
            /*
             * If default search type is 'public', set the status of the 
             * controls, namely, public search checkbox the hfPublicSearch hiddenfield
             * to boolean value true.
             */
            setInitialStatusForUIControlsAsPerSearchType(true);
        }
    }
    if (defaultSearchType == SecureOnly)
    {
        setInitialStatusForUIControlsAsPerSearchType(false);
        hideControl(chkPublicSearch);
        hideControl(divPublicSearch);

    }
    string showScope = WebConfigurationManager.AppSettings["showScope"];
    if (!String.IsNullOrEmpty(showScope) && (String.Compare(showScope, "false", true) == 0))
    {
        hideControl(idSearchScope);
    }

    string showSearchTips = WebConfigurationManager.AppSettings["showSearchTips"];
    if (!String.IsNullOrEmpty(showSearchTips) && (String.Compare(showSearchTips, "false", true) == 0))
    {
        hideControl(linkSearchTips);
    }
    
    
    /*
     * Call the function for checkbox checked changed event, so that the latest status for the 
     * public search checkbox is assigned to 'hfPublicSearch' hiddenfield variable.
     */
    checkPublicSearch(this, EventArgs.Empty);
     
    // Registering the event handler for public search checkboxcheck
    chkPublicSearch.CheckedChanged += new EventHandler(this.checkPublicSearch);

    
   // The forward slash is used to append to the sitesearch parameter. 
    string forwardSlash = "/";
    
    if (Request.QueryString["scopeUrl"] != null)
    {
        /*
         * Def of Session - Variables stored in a Session object hold information about one single user, and are available to all pages in one application. 
         * Hence storing the value for the selected scope in Session, so that the  scope is persisted while searching.
         */
        Session["ScopeURL"] = Request.QueryString["scopeUrl"].ToString();
    }

    /* 
    * Definition for IsPostBack :- "IsPostBack" is a read-only Boolean property that indicates if the page or control is being loaded for the first time. 
    * So, if the page is loaded for the first time after search, assign a value to the Session which is equal to the value of the Querystring parameter
    * 'isPublicSearch'
    */



    if (!IsPostBack)
    {
        string publicSearchStatus = "";
        if (Request.QueryString["isPublicSearch"] != null)
        {
            // This code will be executed for the first search request sent to GSA.
            publicSearchStatus = Request.QueryString["isPublicSearch"].ToString();
            if (publicSearchStatus != "")
            {
                hfPublicSearch.Value = publicSearchStatus;
                Session["PublicSearchStatus"] = publicSearchStatus;
            }
            else if (Session["PublicSearchStatus"] != null) /*
                                                            * If querystring parameter value is empty string, assign
                                                            * the value from session variable to the hiddenfield.
                                                            */
            {
                hfPublicSearch.Value = Convert.ToString(Session["PublicSearchStatus"]);
            }

            /*
             * Set the Public Search checkbox status as per the value of the hiddenfield 'hfPublicSearch'
             */
            if (hfPublicSearch.Value == "true")
            {
                chkPublicSearch.Checked = true;
            }
            else
            {
                chkPublicSearch.Checked = false;
            }
        }
        else // This code will be executed for all successive searches performed by the user.
        {
            /*
             * For successive searches, it is possible to get the type of search performed by the user using the 'access' parameter. 
             * The access parameter is one of the search parameters in a search request to the GSA. Search parameters can be found on
             * url - http://code.google.com/apis/searchappliance/documentation/68/xml_reference.html#request_parameters
             * Value 'a' means public and secure search; value 'p' means public search.
             */
            if (Request.QueryString["access"] != null)
            {
                accessStatus = Request.QueryString["access"].ToString();
                if (accessStatus == "a")
                {
                    chkPublicSearch.Checked = false;
                    Session["PublicSearchStatus"] = "false"; // Assign same value to the session variable, so that it it persisted.
                }
                else if (accessStatus == "p") // Means only public search is performed by the user (i.e. access = p)
                {
                    chkPublicSearch.Checked = true;
                    Session["PublicSearchStatus"] = "true"; // Assign same value to the session variable, so that it it persisted.
                }
            }
            else if (Session["PublicSearchStatus"] != null)/*
                                                            * If querystring parameter value is empty string, assign
                                                            * the value from session variable to the hiddenfield.
                                                            */
            {
                accessStatus = Convert.ToString(Session["PublicSearchStatus"]);
                if (accessStatus == "false")
                {
                    chkPublicSearch.Checked = false;
                }
                else if (accessStatus == "true")
                {
                    chkPublicSearch.Checked = true;
                }
            }     
        }
    }




    SPSite site = null;
    string strScopeWeb = null;
    string strScopeList = null;
    string strWebSelected = null;
    string strScopeFolder = null;
    string siteUrl = "";
    string listType = "";
    string listUrl = "";
    string listParentWebUrl = "";

    // Display the Public Search checkbox only if the user has selected 'public ans secure' search while installing the search box.
    if (WebConfigurationManager.AppSettings["accesslevel"].ToString().Equals("a"))
    {
        chkPublicSearch.Visible = true;
        divPublicSearch.Visible = true;
    }
    else 
    {
        chkPublicSearch.Visible = false;
        divPublicSearch.Visible = false;
    }
    
    // Declaring the scopes that will be displayed in the scopes dropdown
    string enterprise = "Enterprise";
    string currentSite = "Current Site";
    string currentSiteAndAllSubsites = "Current Site and all subsites";
    string currentList = "Current List";
    string currentFolder = "Current Folder";
    string currentFolderAndAllSubfolders = "Current Folder and all subfolders";
    
    string scopeText = "";
    string scopeValue = "";

	SPWeb web = SPControl.GetContextWeb(Context);
    
    // Code to retrieve the site url if list is selected, as when list is selected, the complete
    // site url is not retrieved.
    SPContext ctx = SPContext.Current;
    if (ctx != null)
    {
        // Retrieve the URL of the SharePoint site collection
        site = ctx.Site;
        siteUrl = site.Url;
    }
        
    string strEncodedUrl = SPHttpUtility.EcmaScriptStringLiteralEncode(
        SPHttpUtility.UrlPathEncode(web.Url + "/_layouts/GSASearchresults.aspx", false, false)
        );
   
	strEncodedUrl = "'" + strEncodedUrl + "'";

    // Hiddenfiled 'hfStrEncodedUrl' will store the value of the url, so as to be passed as a parameter for SendSearchRequesttoGSA function whenever user presses the Enter key.
    hfStrEncodedUrl.Value = web.Url + "/_layouts/GSASearchresults.aspx";
    
    strScopeWeb = "'" + SPHttpUtility.HtmlEncode(web.Url) + "'";
    
    // Adding the items to the dropdown 
    ListItem lstItem = new ListItem();
    lstItem.Text = enterprise;
    lstItem.Value = enterprise;
    idSearchScope.Items.Add(lstItem);
    

    ListItem lstItem1 = new ListItem();
    string sitename = strScopeWeb.Replace("'", "");
    lstItem1.Text = currentSite;
    lstItem1.Value = sitename + forwardSlash;
    idSearchScope.Items.Add(lstItem1);

    // The hiddenfield named 'hfSelectedScope' is used to store the 'scope text' of the current context selected by the user, so that 
    // appropriate scope can be enabled in the dropdown.    
    ListItem lstItem2 = new ListItem();
    lstItem2.Text = currentSiteAndAllSubsites;
    lstItem2.Value = sitename;
    idSearchScope.Items.Add(lstItem2);
    hfSelectedScope.Value = sitename;
        
    ListItem lstItem3 = new ListItem();
    lstItem3.Text = currentList;
    idSearchScope.Items.Add(lstItem3);

    ListItem lstItem4 = new ListItem();
    lstItem4.Text = currentFolder;
    idSearchScope.Items.Add(lstItem4);

    ListItem lstItem5 = new ListItem();
    lstItem5.Text = currentFolderAndAllSubfolders;
    idSearchScope.Items.Add(lstItem5);

   
    idSearchScope.Items.FindByText(currentList).Enabled = false;
    idSearchScope.Items.FindByText(currentFolder).Enabled = false;
    idSearchScope.Items.FindByText(currentFolderAndAllSubfolders).Enabled = false;
    
    
    SPList list = SPContext.Current.List;
    if (list != null)
    {
              
        listUrl = list.DefaultViewUrl.ToString();   // Retrieve list url
        listParentWebUrl = list.ParentWeb.Url.ToString(); // Get the parent website for the list
        listType = list.GetType().ToString();       // Get type for the current list and accordingly construct the url

        switch (listType)
        {
            case "Microsoft.SharePoint.SPDocumentLibrary":
                int iStartIndex = listUrl.LastIndexOf("/"); //  Remove the string occurring after the last slash(/) i.e. "Alltems.aspx ", and then repeat the same all over once again, for removing the other string after the second last slash (/) i.e. "Forms"
                //  The last part of the string needs to be discarded so as to obtain the correct path for document library, till the name part.
                listUrl = listUrl.Remove(iStartIndex);
                int iStartIndex1 = listUrl.LastIndexOf("/");
                listUrl = listUrl.Remove(iStartIndex1);
                listUrl = listUrl.Substring(listUrl.LastIndexOf("/"));
                strScopeList = listParentWebUrl + listUrl; // Construct the url for current list browsed by the user
                /*
                 * For example, if the document library url is http://SharePointSiteURL:portnumber/sites/site1/doclib/Forms/AllItems.aspx, then 
                 * listParentWebUrl will be "http://SharePointSiteURL:portnumber/sites/site1" and listUrl will be "/doclib". 
                 */
                break;

            case "Microsoft.SharePoint.SPList":

                listUrl = list.DefaultView.Url.Substring(0, list.DefaultView.Url.LastIndexOf("/"));// Remove the string "/AllItems.aspx" from the url
                strScopeList = listParentWebUrl + "/" + listUrl; // Construct the url for current list browsed by the user
                break;
        } // end switch-case statement

        
        hfSelectedScope.Value = strScopeList;
        lstItem3.Value = strScopeList;
        idSearchScope.Items.FindByText(currentList).Enabled = true;

        if (this.Context.Request.QueryString["RootFolder"] != null)
        {
            web = SPControl.GetContextWeb(Context); // Get the current SPWeb object
            string rootFolder = this.Context.Request.QueryString["RootFolder"].ToString();
            SPFolder folder = web.GetFolder(rootFolder); // Get folder located at the url represented by "rootFolder"    

            strScopeFolder = listParentWebUrl + "/" + folder.Url; // Construct the url for current folder browsed by the user
            lstItem4.Value = strScopeFolder + forwardSlash;
            lstItem5.Value = strScopeFolder;
            hfSelectedScope.Value = strScopeFolder;
            idSearchScope.Items.FindByText(currentFolder).Enabled = true;
            idSearchScope.Items.FindByText(currentFolderAndAllSubfolders).Enabled = true;

        }
    }
    else
    {
        strWebSelected = "SELECTED";
    }
    
    // Contains code for persisting the search query term and selected scope.
        
    // Code for populating the search query text value. 
    if (Request.QueryString["k"] != null)
    {
        // If this is the first request for search, populate the search query text with querystring 'k' parameter.
        searchQuery = "";
        searchQuery = Request.QueryString["k"];
        int strCache = searchQuery.IndexOf("cache:");

        if (strCache > -1)
        {
            /*
             * The search query string contains the term 'cache', means that user has clicked either the
             * 'Cached' or 'Text Version' link for a search result. Hence, set the text for the 
             * search box using the value from the Session variable.
             */
            if (Session["SearchQuery"] != null)
            {
                txtSearch.Text = Convert.ToString(Session["SearchQuery"]);
            }
        }
        else
        {
            txtSearch.Text = searchQuery;
            Session["SearchQuery"] = txtSearch.Text; /* 
                                                      * Store search query in Session variable, and then retrieve
                                                      * at the time when user clicks on the 'Cached'/ 'Text Version'
                                                      * Links
                                                      */

        }
    }
    else if(Request.QueryString["q"] != null)
    {
        /* If this is not the first request for search, populate search query text with querystring 'q' parameter.
         * This code will also be executed when user clicks on the "All Results" and "Clear" links, which are displayed 
         * for dynamic navigation search results.
         */
        string strInMeta = Convert.ToString(Request.QueryString["q"]); // Search Query exists in querystring "q" parameter
        /*
         * The string inmeta:<AttributeName>=<matchingAttributeValue> gets appended to the search query term when user
         * clicks on any of the dynamic navigation search results. This inserts arbitrary characters into the search box.
         * Hence, adding the code to get the search query term from the session variable. 
         */ 
        if (strInMeta.IndexOf("inmeta:") != -1)
        {
            if (Session["SearchQuery"] != null)
            {
                txtSearch.Text = Convert.ToString(Session["SearchQuery"]);
            }
        }
        else
        {
            txtSearch.Text = Request.QueryString["q"].ToString();
        }
    }
    else /*
          * This code will be executed when the user clicks on any one of the dynamic navigation results displayed 
          * in the left sidebar. Here, the search query textbox will be populated with the value from the "SearchQuery"
          * session variable.
          */
    {
        if (Session["SearchQuery"] != null)
        {
            txtSearch.Text = Convert.ToString(Session["SearchQuery"]);
        }
    }
    
    /* This code is executed for the very first search performed on the site. This code will also execute when the user performs  
     * the first search, after changing the scope from the scopes dropdown.
     */
    if (Request.QueryString["selectedScope"] != null)
    {
        // Store the value in a session variable
        Session["ScopeText"] = Request.QueryString["selectedScope"].ToString();
        // If this is the first request for search,get the dropdown text value from the 'selectedScope' querystring parameter.
        for (int i = 0; i < idSearchScope.Items.Count; i++)
        {
            string selectedScopeTextValue = Request.QueryString["selectedScope"].ToString();
            // Finding the dropdown's text value equivalent to the scope selected by the user, so that the respective value will be set as 'SELECTED' in the dropdown.
            if (idSearchScope.Items[i].Text == selectedScopeTextValue)
            {
                if (selectedScopeTextValue == currentList || selectedScopeTextValue == currentFolder || selectedScopeTextValue == currentFolderAndAllSubfolders)
                {
                    // Code to set the respective dropdown item as 'ENABLED', when the user selects 'Current List', 'Current Folder' and 'Current Folder And All Subfolders' in the dropdown. Needs to be done as these scope urls are not
                    // available on the GSASearchresults.aspx page in SharePoint.(Browsing occurs at site level by default when search is redirected to GSASearchResults.aspx page)
                    
                    idSearchScope.Items[i].Enabled = true;
                    idSearchScope.Items[i].Selected = true;
                }
                if (Session["ScopeURL"] != null)
                {
                    // Setting the dropdown's  scope value to the value stored within the ViewState. 
                    idSearchScope.Items[i].Value = System.Web.HttpUtility.UrlDecode(Session["ScopeURL"].ToString());
                }
                
                // Setting the dropdown's  selected value to the value from the querystring object.
                idSearchScope.Items.FindByText(Request.QueryString["selectedScope"].ToString()).Selected = true;
                break;
            }
        }
    } 
    else if (Session["ScopeText"] != null) /* For successive searches, this code will be executed. This code will also be executed
                                            * when there is a suggestion on the search results page to 'repeat the search with the
                                            * omitted results included'. Here, the session variable's value can be retrieved to get
                                            * the scope text value.
                                            */
    {
        
        scopeText = Session["ScopeText"].ToString();
        scopeValue = "";
        if(Request.QueryString["sitesearch"] != null) // The sitesearch parameter comes in the GSA search URL.
        {
            scopeValue = System.Web.HttpUtility.UrlDecode(Request.QueryString["sitesearch"].ToString());
        }
         else /* This code will be executed when the user clicks on any of the dynamic navigation results displayed in 
              * left sidebar. Hence, retrieve the values for the scope value and scope text from the current request and 
              * make the item as selected in the dropdown. With this, the scope value is persisted when user is using 
              * dynamic navigation.
              */
        {
            string searchReq = HttpContext.Current.Request.Url.Query;
            searchReq = HttpUtility.UrlDecode(searchReq); // Decoding the URL received from the current request 
            /*
             * If sitesearch is present in the current request, execute code for extracting its value and 
             * assign the same to scopeValue variable.
             */
            if (searchReq.IndexOf("sitesearch") > -1) // Means sitesearch is present
            {
                // Getting index for question mark (?). Code for extracting the sitesearch parameter's value
                if (searchReq.IndexOf("?") > -1)
                {
                    searchReq = searchReq.Substring(searchReq.IndexOf("?"));

                    /*
                     * Get  "?sitesearch=<sitesearchParameterValue>" where "sitesearchParameterValue"
                     * represents the scope value for a site/ list or folder
                     */
                    if (!(searchReq.Substring(0, searchReq.IndexOf("&")).Contains("sitesearch")))
                    {
                        /*
                         * If "sitesearch" parameter is not present by doing 'searchReq.Substring(0, searchReq.IndexOf("&")', then
                         *  employ another way to extract the substring.
                         */
                        searchReq = searchReq.Substring(searchReq.IndexOf("&"));
                    }
                    else
                    {
                        searchReq = searchReq.Substring(0, searchReq.IndexOf("&"));
                    }       
                    
                    if (searchReq.IndexOf("=") > -1)
                    {
                        searchReq = searchReq.Substring(searchReq.IndexOf("=")); // Gets "=<sitesearchParameterValue>"
                        searchReq = searchReq.Replace("=", "");// Gets "<sitesearchParameterValue>"
                        scopeValue = searchReq;
                    }
                }
            }
        }
        if (scopeValue != "")
        {
            // Creating a new listitem for the respective scope text and value retrieved
            ListItem lstItemScopeItem = new ListItem();
            lstItemScopeItem.Text = scopeText;
            lstItemScopeItem.Value = scopeValue;

            /* Checking whether the respective listitem already exists in the scopes dropdown. If it already exists, just make the 
             * respective listitem as selected in the dropdown. If the listitem does not exist (which is true in the case the 
             * user gets option of 'repeat the search with the omitted results included' while searching for the scopes - 
             * Current List, Current folder and current folder and all subfolders), then add the item into the scopes dropdown and
             * make the item as selected.
             */
            if (!idSearchScope.Items.Contains(lstItemScopeItem))
            {
                idSearchScope.Items.Add(lstItemScopeItem);
            }
            idSearchScope.Items.FindByValue(scopeValue).Selected = true;
        }
    }
%>
    

<!--Amit: overridded the SubmitSearchRedirect function of core.js. Else it fails for aspx pages when doing serach from cached result-->

<script type="text/javascript">

    // Function which will activate the respective scopes, as per the scope the user is browsing currently
    function EnableSelectiveScopeOptions()
    {
        var currentFolder = "Current Folder";
        var currentFolderAndAllSubfolders = "Current Folder and all subfolders";
        var hfselectedscope = document.getElementById("<%=hfSelectedScope.ClientID%>");
        var searchScope = document.getElementById("<%=idSearchScope.ClientID%>");

        for (var i = 0; i < searchScope.options.length; i = i + 1)
        {
            searchScope.options[i].disabled = false; // Enabling the scope the user is currently browsing.
            if (searchScope.options[i].value == hfselectedscope.value) // Enabling the options for 'folder search'
            {
                if (searchScope.options[i].text == currentFolder)
                {
                    // Need to enable the 'Current Folder and all subfolders', if user is browsing through a folder.
                    searchScope.options[i + 1].disabled = false;
                }
                break; // Need to break the for loop if the currently selected scope is other than 'Current Folder',as for instance, 
                // if the currently selected scope is 'Current List', don't enable the remaining scopes at lower level.
            }
        }
    }
    if (document.addEventListener)
    {
        document.addEventListener("DOMContentLoaded", EnableSelectiveScopeOptions, false);
    }
    else if (document.attachEvent)
    {
        document.attachEvent("onreadystatechange", EnableSelectiveScopeOptions);
    }

    // This javascript function's default name is 'SubmitSearchRedirect' which can be found in 'CORE.JS' file (path "C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\TEMPLATE\LAYOUTS\1033")
    // Renaming the function to 'SendSearchRequesttoGSA'. The function will send the search request to GSA.
    function SendSearchRequesttoGSA(strUrl)
    {
        var frm = document.forms["frmSiteSearch"];
        if (frm == null)
        {
            if (typeof (MSOWebPartPageFormName) != "undefined")
                frm = document.forms[MSOWebPartPageFormName];
        }
        if (frm != null)
        {

            strUrl = strUrl + "?k=" + escapeProperly(document.getElementById("<%=txtSearch.ClientID%>").value);
            var searchScope = document.getElementById("<%=idSearchScope.ClientID%>");
            if (searchScope != null)
            {
                var searchScopeUrl = searchScope.options[searchScope.selectedIndex].value;
                if (searchScopeUrl != "Enterprise")
                {
                    strUrl = strUrl + "&u=" + escapeProperly(searchScopeUrl);
                }
            }

            var selectedScopeText = searchScope.options[searchScope.selectedIndex].text;
            var selectedScopeUrl = searchScope.options[searchScope.selectedIndex].value;
            var isPublicSearch = document.getElementById("<%=hfPublicSearch.ClientID%>").value;
            /* Checking whether user has selected a list within a site, while selecting option as 'My List'.
            If user is not browsing the list, then display an eror message as in this case the scope url will be retrieved as "" */

            if (selectedScopeUrl == "" && selectedScopeText == "Current List")
            {
                alert('Please select a list first !');
            }
            else if (selectedScopeUrl == "" && (selectedScopeText == "Current Site" || selectedScopeText == "Current Site and all subsites"))
            {
                alert('Please select a site first !');
            }
            else if (selectedScopeUrl == "Enterprise" && selectedScopeText == "Enterprise")
            {
                strUrl = strUrl + "&selectedScope=" + selectedScopeUrl + "&isPublicSearch=" + isPublicSearch;
                window.document.location.href = strUrl;
            }
            else
            {
                strUrl = strUrl + "&selectedScope=" + selectedScopeText + "&scopeUrl=" + selectedScopeUrl + "&isPublicSearch=" + isPublicSearch;
                window.document.location.href = strUrl;
            }
        }
    }


    // This function will call the SendSearchRequesttoGSA Javascript function whenever 'Enter' key is pressed.
    function SendSearchRequesttoGSAOnEnterClick()
    {
        if (event.keyCode == 13)
        {
            hfStrEncodedUrl = document.getElementById('<%=hfStrEncodedUrl.ClientID%>').value;
            SendSearchRequesttoGSA(hfStrEncodedUrl);
        }
    }
    
    // Function that will change the background of the search query textbox to plain white background whenever the user begins to type inside the query textbox.
    function SearchTextOnFocus()
    {
         var f = document.getElementById("<%=txtSearch.ClientID%>");
         f.style.background = '#ffffff';
    }
    
    // Function that will change the background of the search query textbox 
    function SearchTextOnBlur()
    {
         var f = document.getElementById("<%=txtSearch.ClientID%>");
         if(f.value == "")
         {
            // Display the Google Search watermark image in searchbox when the searchbox is empty
            f.style.background = 'background-color: transparent'; 
         }
         else
         {
            /*
             * Do not display the Google Search watermark image in searchbox, when the searchbox contains text.
             * Instead set background colour to white.
             */
            f.style.background = '#ffffff';
         }
    }
    
</script>


<script runat="server"  type="text/C#">

    // Function that will change the value of hiddenfield and session variable whenever checkbox is checked/ unchecked.
    public void checkPublicSearch(object sender, EventArgs e)
    {
        if (IsPostBack)
        {
            if (chkPublicSearch.Checked == true)
            {
                hfPublicSearch.Value = "true";
                Session["PublicSearchStatus"] = "true";
            }
            else
            {
                hfPublicSearch.Value = "false";
                Session["PublicSearchStatus"] = "false";
            }
        }
    }


    /// <summary>
    /// Function that will set the initial status of UI control as per the Search Type
    /// </summary>
    /// <param name="publicSearchCheckBoxStatus">Boolean value either true or false</param>
    void setInitialStatusForUIControlsAsPerSearchType(bool isInitialSearchTypeSetToPublic)
    {
        chkPublicSearch.Checked = isInitialSearchTypeSetToPublic;
        hfPublicSearch.Value = Convert.ToString(isInitialSearchTypeSetToPublic);
    }

    void hideControl(WebControl controlToHide)
    {
        if (controlToHide != null)
        {
            controlToHide.Style.Add("display", "none");
        }
    }

    void hideControl(HtmlControl controlToHide)
    {
        if (controlToHide != null)
        {
            controlToHide.Style.Add("display", "none");
        }
    }
</script>



<table border="0" cellpadding="0" cellspacing="0">
    <tr class='ms-searchbox'>
        <td>
        <asp:HiddenField ID="hfPublicSearch" runat="server"   />
        <asp:HiddenField ID="hfStrEncodedUrl" runat="server"/>
        <asp:HiddenField id="hfUserSelectedScope" runat="server" />
        <div  style="color:#003399" id="divPublicSearch" runat="server"><asp:CheckBox ID="chkPublicSearch" runat="server"  OnCheckedChanged="checkPublicSearch" AutoPostBack="true"  ToolTip="Check this to search public content"   />Public&nbsp;Search</div> 
        </td>
        <td>
             
            <asp:DropDownList  ID="idSearchScope" runat="server" ToolTip="Select scope"  Width="218"  CssClass="ms-searchbox" >
            </asp:DropDownList>
            <asp:HiddenField ID="hfSelectedScope" runat="server" />
        </td>
        <td>
            <div style="background-image: url(/_layouts/images/google_custom_search_watermark.gif);
                background-repeat: no-repeat; background-position: left; background-color: Transparent">
          <asp:TextBox ID="txtSearch" runat="server"  Width="190px" style="height:auto; background-color: transparent; color:#003399" 
             ToolTip="Enter search query term" onKeyPress="SendSearchRequesttoGSAOnEnterClick()" onfocus="javascript:return SearchTextOnFocus()" onblur="javascript:return SearchTextOnBlur();" ></asp:TextBox>
            </div>
        </td>
        
        <td>
            <div class="ms-searchimage">
                <a target='_self' href='javascript:' onclick="javascript:SendSearchRequesttoGSA(<%=strEncodedUrl%>);javascript:return false;"
                title="<%SPHttpUtility.AddQuote(SPHttpUtility.HtmlEncode(SearchImageToolTip),Response.Output);%>"
                id="onetIDGoSearch">
                <img border='0' src="/_layouts/images/gosearch.gif" alt="<%SPHttpUtility.AddQuote(SPHttpUtility.HtmlEncode(SearchImageToolTip),Response.Output);%>"></a>
            </div>
        </td>
        <td>            
            <asp:HyperLink id="linkSearchTips" Text="Search Tips" runat="server" style="font-size:xx-small; color:#003399; text-decoration:underline"/>  
        </td>
    </tr>
</table>