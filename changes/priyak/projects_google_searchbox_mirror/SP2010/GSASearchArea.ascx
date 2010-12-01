<%@ Control Language="C#" Inherits="System.Web.UI.UserControl"    compilationMode="Always"  AutoEventWireup="false"%>
<%@ Register Tagprefix="wssawc" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> 
<%@ Register Tagprefix="SharePoint" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register Tagprefix="SharePoint" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> 
<%@ Register Tagprefix="Utilities" Namespace="Microsoft.SharePoint.Utilities" Assembly="Microsoft.SharePoint, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> 
<%@ Import Namespace="Microsoft.SharePoint" %>

<!--Author: Amit Agrawal-->
<%
    string strScopeWeb = null;
    string strScopeList = null;
    string strWebSelected = null;
    string strScopeFolder = null;
    string siteUrl = "";
    string listType = "";
    string listUrl = "";
    
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
    lstItem1.Value = strScopeWeb;
    idSearchScope.Items.Add(lstItem1);
    hfSelectedScope.Value = strScopeWeb;

    ListItem lstItem2 = new ListItem();
    lstItem2.Text = currentSiteAndAllSubsites;
    lstItem2.Value = strScopeWeb;
    idSearchScope.Items.Add(lstItem2);

    ListItem lstItem3 = new ListItem();
    lstItem3.Text = currentList;
    idSearchScope.Items.Add(lstItem3);

    ListItem lstItem4 = new ListItem();
    lstItem4.Text = currentFolder;
    idSearchScope.Items.Add(lstItem4);

    ListItem lstItem5 = new ListItem();
    lstItem5.Text = currentFolderAndAllSubfolders;
    idSearchScope.Items.Add(lstItem5);
    
    // Changing the font colour for Active scopes in the dropdown to make them appear as more precise
    idSearchScope.Items.FindByText(enterprise).Attributes.Add("style", "color:Black");
    idSearchScope.Items.FindByText(currentSite).Attributes.Add("style", "color:Black");
    idSearchScope.Items.FindByText(currentSiteAndAllSubsites).Attributes.Add("style", "color:Black");
    
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
            lstItem4.Value = strScopeFolder;
            lstItem5.Value = strScopeFolder;
            hfSelectedScope.Value = strScopeFolder;
        }
    }
    else
    {
        strWebSelected = "SELECTED";
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
                searchScope.options[i].style.color = "Black";
                
                if(searchScope.options[i].text == currentFolder || searchScope.options[i].text == currentFolderAndAllSubfolders)
                {
                    // Need to enable the 'Current List' option as well, if user is browsing through a folder. Because the hiddenfield 'hfselectedscope' is holding the text value for the folder.
                    searchScope.options[i - 1].disabled = false;  
                    searchScope.options[i-1].style.color = "Black";
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

function SubmitSearchRedirect1(strUrl)
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
		if (searchScope != null && searchScope != "Enterprise")
		{
			var searchScopeUrl = searchScope.options[searchScope.selectedIndex].value;
			if (searchScopeUrl != "Enterprise")
			{
				strUrl = strUrl+"&u="+escapeProperly(searchScopeUrl);
			}
		}
		
		var selectedScopeText = searchScope.options[searchScope.selectedIndex].text;
		var selectedScopeUrl = searchScope.options[searchScope.selectedIndex].value;
		
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
		else if (selectedScopeUrl == "Enterprise" && selectedScopeText == "Enterprise")
		{
		    strUrl = strUrl + "&selectedScope=" + selectedScopeUrl;
		    frm.action = strUrl;
		    alert(strUrl);
		    frm.submit();
		}
		else
		{
		    strUrl = strUrl + "&selectedScope=" + selectedScopeText + "&scopeUrl=" + selectedScopeUrl;
		    frm.action=strUrl;
		    alert(strUrl);
		    document.forms
		    frm.submit();
		}
	}
}


</script>


<asp:Panel ID="pnlSearchBoxPanel" runat="server" >   
<!--Search Controls-->
          <asp:DropDownList  ID="idSearchScope" runat="server"    Width="240px" >
          </asp:DropDownList>
          <asp:HiddenField ID="hfSelectedScope" runat="server" />
    <asp:TextBox ID="txtSearch" runat="server" Width="242px" size='28'  Text="" style="background-image: url('/_layouts/images/google_custom_search_watermark.gif'); background-repeat: no-repeat; background-position:center;background-color:Transparent"></asp:TextBox>
</asp:Panel>


<div class="ms-searchimage">
<a target='_self' 
	href='javascript:' 
	onClick="javascript:SubmitSearchRedirect1(<%=strEncodedUrl%>);javascript:return false;" 
	ID=onetIDGoSearch>
	<img border='0' src="/_layouts/images/gosearch.gif" alt='Go!'>
</a>

</div>
