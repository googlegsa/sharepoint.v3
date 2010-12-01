<%@ Control Language="C#" Inherits="System.Web.UI.UserControl"     compilationMode="Always"  AutoEventWireup="false"%>
<%@ Register Tagprefix="wssawc" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> 
<%@ Register Tagprefix="SharePoint" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register Tagprefix="SharePoint" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> 
<%@ Register Tagprefix="Utilities" Namespace="Microsoft.SharePoint.Utilities" Assembly="Microsoft.SharePoint, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> 
<%@ Import Namespace="Microsoft.SharePoint" %>
<%@ Import Namespace="System.Web.Configuration" %>

<!--Author: Amit Agrawal-->
<%
    // The forward slash is used to append to the sitesearch parameter. 
    string forwardSlash = "/";
    
    if (Request.QueryString["scopeUrl"] != null)
    {
        // The ViewState allows ASP.NET to repopulate form fields on each postback to the server. Hence storing the value for the selected scope
        // in Viewstate, so that the  scope is persisted while searching.
        ViewState["ScopeURL"] = Request.QueryString["scopeUrl"].ToString();
    }
    
    string strScopeWeb = null;
    string strScopeList = null;
    string strWebSelected = null;
    string strScopeFolder = null;
    string siteUrl = "";
    string listType = "";
    string listUrl = "";

    if (WebConfigurationManager.AppSettings["accesslevel"].ToString().Equals("a"))
    {
        chkPublicSearch.Visible = true;
    }
    else if (WebConfigurationManager.AppSettings["accesslevel"].ToString().Equals("p"))
    {
        chkPublicSearch.Visible = false;
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
    hfSelectedScope.Value = strScopeWeb + forwardSlash;

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
    
    // Disabling the respective items from the dropdownlist, as the user is browsing at the site level
    idSearchScope.Items.FindByText(currentList).Attributes.Add("disabled", "disabled");
    idSearchScope.Items.FindByText(currentFolder).Attributes.Add("disabled", "disabled");
    idSearchScope.Items.FindByText(currentFolderAndAllSubfolders).Attributes.Add("disabled", "disabled");
    
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
        
        if (this.Context.Request.QueryString["RootFolder"] != null)
        {
            strScopeFolder = siteUrl + this.Context.Request.QueryString["RootFolder"].ToString(); // Retrieve the folder path
            lstItem4.Value = strScopeFolder + forwardSlash;
            lstItem5.Value = strScopeFolder;
            hfSelectedScope.Value = strScopeFolder + forwardSlash;
        }
    }
    else
    {
        strWebSelected = "SELECTED";
    }
    
    // Code for populating the search query text value
    if (Request.QueryString["k"] != null)
    {
        txtSearch.Text = Request.QueryString["k"].ToString();
    }
    if (Request.QueryString["selectedScope"] != null)
    {
        for (int i = 0; i < idSearchScope.Items.Count; i++)
        {
            // Finding the dropdown's text value equivalent to the scope selected by the user, so that the respective value will be set as 'SELECTED' in the dropdown.
            if (idSearchScope.Items[i].Text == Request.QueryString["selectedScope"].ToString())
            {
                if (Request.QueryString["selectedScope"].ToString() == currentList || Request.QueryString["selectedScope"].ToString() == currentFolder || Request.QueryString["selectedScope"].ToString() == currentFolderAndAllSubfolders)
                {
                    // Code to set the respective dropdown item as 'ENABLED', when the user selects 'Current List', 'Current Folder' and 'Current Folder And All Subfolders' in the dropdown. Needs to be done as these scope urls are not
                    // available on the GSASearchresults.aspx page in SharePoint.(Browsing occurs at site level by default when search is redirected to GSASearchResults.aspx page)
                    idSearchScope.Items[i].Attributes.Clear();
                }
                if (ViewState["ScopeURL"] != null)
                {
                    //Setting the dropdown's  scope value to the value stored within the ViewState. 
                    idSearchScope.Items[i].Value = ViewState["ScopeURL"].ToString();
                }

                //Setting the dropdown's  selected value to the value from the querystring object.
                idSearchScope.Items.FindByText(Request.QueryString["selectedScope"].ToString()).Selected = true;
                break;
            }
        }
    }
%>


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
            if (searchScope.options[i].value == hfselectedscope.value) // Enabling the options for 'folder search'
            {
                searchScope.options[i].disabled = false;
                if(searchScope.options[i].text == currentFolder)
                {
                    // Need to enable the 'Current Folder and all subfolders'  and 'Current List' options as well, if user is browsing through a folder.
                    searchScope.options[i - 1].disabled = false;
                    searchScope.options[i + 1].disabled = false;
                }
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
	var frm=document.forms["frmSiteSearch"];
	if (frm==null)
	{
		if (typeof(MSOWebPartPageFormName) !="undefined")
			frm=document.forms[MSOWebPartPageFormName];
	}
	if (frm !=null)
	{
		
		strUrl=strUrl+"?k="+escapeProperly(document.getElementById("<%=txtSearch.ClientID%>").value);
		var searchScope = document.getElementById("<%=idSearchScope.ClientID%>");
		if (searchScope != null)
		{
			var searchScopeUrl = searchScope.options[searchScope.selectedIndex].value;
			if (searchScopeUrl != "Enterprise")
			{
				strUrl = strUrl+"&u="+escapeProperly(searchScopeUrl);
			}
		}
		
		var selectedScopeText = searchScope.options[searchScope.selectedIndex].text;
		var selectedScopeUrl = searchScope.options[searchScope.selectedIndex].value;
		var userSelectedScope = document.getElementById("<%=hfUserSelectedScope.ClientID%>").value;
		var isPublicSearch = document.getElementById("<%=hfPublicSearch.ClientID%>").value;
		/* Checking whether user has selected a list within a site, while selecting option as 'My List'.
		 If user is not browsing the list, then display an eror message as in this case the scope url will be retrieved as "" */
		
		if(selectedScopeUrl == "" && selectedScopeText == "Current List") 
		{
		    alert('Please select a list first !');
		}
		else if(selectedScopeUrl == "" && (selectedScopeText == "Current Site" || selectedScopeText == "Current Site and all subsites"))
		{
		    alert('Please select a site first !');
		}
		else if (selectedScopeUrl == "Enterprise" && selectedScopeText == "Enterprise") {
		    strUrl = strUrl + "&selectedScope=" + selectedScopeUrl + "&isPublicSearch=" + isPublicSearch;
		    frm.action = strUrl;
		    //hfStrURL = strUrl;
		    frm.submit();
		}
		else
		{
		    strUrl = strUrl + "&selectedScope=" + selectedScopeText + "&scopeUrl=" + selectedScopeUrl + "&isPublicSearch=" + isPublicSearch;
		    frm.action = strUrl;
		   // hfStrURL = strUrl;
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

// Function that will change the value of hiddenfiled whenever checkbox is checked/ unchecked
function checkPublicSearch(chk)
{

    var isPublicSearch = document.getElementById("<%=hfPublicSearch.ClientID%>").value;
    if (chk.checked == true) 
    {
        isPublicSearch = "true";
    }
    else 
    {
        isPublicSearch = "false";
    }
}

</script>

<div style="float:left;font-size:small; color:Black">
<asp:HiddenField ID="hfPublicSearch" runat="server"  Value="true"/>
<asp:HiddenField ID="hfStrEncodedUrl" runat="server"/>
<asp:HiddenField id="hfUserSelectedScope" runat="server" />
<asp:CheckBox ID="chkPublicSearch" runat="server"  Width="120px" Checked="true" onclick="checkPublicSearch(this);"  TextAlign="Right"  style="vertical-align:bottom;"  ToolTip="Check this to search public content"   />
Public Search &nbsp;&nbsp;
</div>

<asp:Panel ID="pnlSearchBoxPanel" runat="server" >   
<!--Search Controls--> 


            <asp:DropDownList  ID="idSearchScope" runat="server"  ForeColor="Black" >
            </asp:DropDownList>
            <asp:HiddenField ID="hfSelectedScope" runat="server" />
            <asp:TextBox ID="txtSearch" runat="server" size='28' style="background-image: url('/_layouts/images/google_custom_search_watermark.gif'); background-repeat: no-repeat; background-position:center;background-color:Transparent" onKeyPress="SendSearchRequesttoGSAOnEnterClick()" ></asp:TextBox></asp:Panel>

<div  class="ms-searchimage">
<a target='_self' 
	href='javascript:' 
	onClick="javascript:SendSearchRequesttoGSA(<%=strEncodedUrl%>);javascript:return false;" 
	ID=onetIDGoSearch>
	<img border='0' src="/_layouts/images/gosearch.gif" alt='Go!'>
</a>
</div>




