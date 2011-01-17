<%@ Control Language="C#" Inherits="Microsoft.SharePoint.WebControls.SearchArea,Microsoft.SharePoint,Version=12.0.0.0,Culture=neutral,PublicKeyToken=71e9bce111e9429c"
    CompilationMode="Always" AutoEventWireup="false" %>
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
    
   // The forward slash is used to append to the sitesearch parameter to Search Request
    string forwardSlash = "/";
    
    if (Request.QueryString["scopeUrl"] != null)
    {
        /*
         * The ViewState allows ASP.NET to repopulate form fields on each postback to the server. Hence storing the value for the selected scope
         * in Viewstate, so that the  scope is persisted while searching.
         */
        ViewState["ScopeURL"] = Request.QueryString["scopeUrl"].ToString();
    }

    /* 
    * Definition for IsPostBack :- "IsPostBack" is a read-only Boolean property that indicates if the page or control is being loaded for the first time. 
    * So, if the page is loaded for the first time after search, assign a value to the ViewState which is equal to the value of the Querystring parameter
    * 'isPublicSearch'
    */

    if (!IsPostBack)
    {
        if (Request.QueryString["isPublicSearch"] != null)
        {
            ViewState["PublicSearchStatus"] = Request.QueryString["isPublicSearch"].ToString();
        }
    }
    /* 
     * If the page is not loaded for the first time after search, assign a value to the ViewState which is equal to the value of the Hiddenfield.
     */
    else
    {
        if (hfPublicSearch.Value != null)
        {
            ViewState["PublicSearchStatus"] = hfPublicSearch.Value;
        }
    }
   
    
    
    
    string strScopeWeb = null;
    string strScopeList = null;
    string strWebSelected = null;
    string strScopeFolder = null;
    string siteUrl = "";
    string listType = "";
    string listUrl = "";

    // Display the Public Search checkbox only if the user has selected 'public ans secure' search while installing the search box.
    if (WebConfigurationManager.AppSettings["accesslevel"].ToString().Equals("a"))
    {
        divPublicSearch.Visible = true;
    }
    else 
    {
        divPublicSearch.Visible = false;
    }
    
    // Declaring the scopes that will be displayed in the scopes dropdown
    string enterprise = "Enterprise";
    string currentSite = "Current Site";
    string currentSiteAndAllSubsites = "Current Site and all subsites";
    string currentList = "Current List";
    string currentFolder = "Current Folder";
    string currentFolderAndAllSubfolders = "Current Folder and all subfolders";

	SPWeb web = SPControl.GetContextWeb(Context);
    
    // Code to retrieve the site url if list is selected, as when list is selected, the complete
    // site url is not retrieved.
    SPContext ctx = SPContext.Current;
    if (ctx != null)
    {
        // Retrieve the URL of the SharePoint site collection
        SPSite site = ctx.Site;
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
    sitename = ctx.ListItemDisplayName;
    lstItem1.Text = currentSite;
    lstItem1.Value = strScopeWeb + forwardSlash;
    idSearchScope.Items.Add(lstItem1);

    // The hiddenfield named 'hfSelectedScope' is used to store the 'scope text' of the current context selected by the user, so that 
    // appropriate scope can be enabled in the dropdown.    
    ListItem lstItem2 = new ListItem();
    lstItem2.Text = currentSiteAndAllSubsites;
    lstItem2.Value = strScopeWeb;
    idSearchScope.Items.Add(lstItem2);
    hfSelectedScope.Value = strScopeWeb;
        
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
        listType = list.GetType().ToString();       // Get type for the current list and accordingly construct the url

        switch (listType)
        {
            case "Microsoft.SharePoint.SPDocumentLibrary":
                int iStartIndex = listUrl.LastIndexOf("/"); //  Remove the string occurring after the last slash(/) i.e. "Alltems.aspx ", and then repeat the same all over once again, for removing the other string after the second last slash (/) i.e. "Forms"
                //  The last part of the string needs to be discarded so as to obtain the correct path for document library, till the name part.
                listUrl = listUrl.Remove(iStartIndex);
                int iStartIndex1 = listUrl.LastIndexOf("/");
                listUrl = listUrl.Remove(iStartIndex1);
                break;

            case "Microsoft.SharePoint.SPList":

                int iStartIndex2 = listUrl.LastIndexOf("/");
                listUrl = listUrl.Remove(iStartIndex2);
                break;
        } // end switch-case statement

        strScopeList = siteUrl + listUrl;
        hfSelectedScope.Value = strScopeList;
        lstItem3.Value = strScopeList;
        idSearchScope.Items.FindByText(currentList).Enabled = true;
        
        if (this.Context.Request.QueryString["RootFolder"] != null)
        {
            strScopeFolder = siteUrl + this.Context.Request.QueryString["RootFolder"].ToString(); // Retrieve the folder path
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
        txtSearch.Text = Request.QueryString["k"].ToString();
    }
    else if(Request.QueryString["q"] != null)
    {
        // If this is not the first request for search, populate search query text with querystring 'q' parameter.
        txtSearch.Text = Request.QueryString["q"].ToString();
    }
    
    // Code for persisting the Public search checkbox status
    if (ViewState["PublicSearchStatus"] != null)
    {
        bool publicSearchStatus = Convert.ToBoolean(ViewState["PublicSearchStatus"].ToString());
        chkPublicSearch.Checked = publicSearchStatus;
    }
    
    if (Request.QueryString["selectedScope"] != null)
    {
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
                if (ViewState["ScopeURL"] != null)
                {
                    // Setting the dropdown's  scope value to the value stored within the ViewState. 
                    idSearchScope.Items[i].Value = ViewState["ScopeURL"].ToString();
                }

                // Setting the dropdown's  selected value to the value from the querystring object.
                idSearchScope.Items.FindByText(Request.QueryString["selectedScope"].ToString()).Selected = true;
                break;
            }
        }
    }
    // If this is not the first request for search,get the dropdown text value from the 'sitesearch' querystring parameter. Here 
    else if (Request.QueryString["sitesearch"] != null)
    {
        // Decode the value for sitesearch parameter
        string sitesearchStrValue = System.Web.HttpUtility.UrlDecode(Request.QueryString["sitesearch"].ToString());
        for (int i = 0; i < idSearchScope.Items.Count; i++)
        {
            string searchScopeValue = idSearchScope.Items[i].Value.Replace("'", "");// Removing the single quotes from the URL
            if (searchScopeValue == sitesearchStrValue)
            {
                idSearchScope.Items[i].Selected = true;
                break;
            }
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
                frm.action = strUrl;
                frm.submit();
            }
            else
            {
                strUrl = strUrl + "&selectedScope=" + selectedScopeText + "&scopeUrl=" + selectedScopeUrl + "&isPublicSearch=" + isPublicSearch;
                frm.action = strUrl;
                document.forms
                frm.submit();
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
</script>


<script type="text/C#" runat="server">

    // Function that will change the value of hiddenfiled whenever checkbox is checked/ unchecked
    protected void checkPublicSearch(object sender, EventArgs e)
    {
        if (chkPublicSearch.Checked == true)
        {
            hfPublicSearch.Value = "true";
        }
        else 
        {
            hfPublicSearch.Value = "false";            
        }
        
    }
    
</script>


<table border="0" cellpadding="0" cellspacing="0" class='ms-searchform'>
    <tr class='ms-searchbox'>
        <td>
        <asp:HiddenField ID="hfPublicSearch" runat="server"  Value="true"/>
        <asp:HiddenField ID="hfStrEncodedUrl" runat="server"/>
        <asp:HiddenField id="hfUserSelectedScope" runat="server" />
        <div  style="color:#003399" id="divPublicSearch" runat="server"><asp:CheckBox ID="chkPublicSearch" runat="server"  Checked="true" OnCheckedChanged="checkPublicSearch" AutoPostBack="true"  ToolTip="Check this to search public content"   />Public&nbsp;Search</div> 
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
             ToolTip="Enter search query term" onKeyPress="SendSearchRequesttoGSAOnEnterClick()"></asp:TextBox>
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
    </tr>
</table>