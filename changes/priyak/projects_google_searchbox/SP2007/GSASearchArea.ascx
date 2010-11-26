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
<%
    
  
    
    string strScopeWeb = null;
    string strScopeList = null;
    string strWebSelected = null;
    string strScopeFolder = null;
    string siteUrl = "";
    string listType = "";
    string listUrl = "";
 
    SPWeb web = SPControl.GetContextWeb(Context);

    // The Public Search checkbox will be displayed only if the user has selected the option of 'Public and secure search'
    // while installing the google search box. If user selects 'Public Search', then the checkbox will not be displayed.
    
	if (WebConfigurationManager.AppSettings["accesslevel"].ToString().Equals("a"))
    {
        chkPublicSearch.Visible = true;
    }
    else
    {
        chkPublicSearch.Visible = false;
    }
	
	
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
        if (this.Context.Request.QueryString["RootFolder"] != null)
        {
            strScopeFolder = siteUrl + this.Context.Request.QueryString["RootFolder"].ToString(); // Retrieve the folder path
        }
    }
    else
    {
        strWebSelected = "SELECTED";
        
    }

    
%>
<!--Amit: overridded the SubmitSearchRedirect function of core.js. Else it fails for aspx pages when doing serach from cached result-->

<script type="text/javascript">


// This javascript function's default name is 'SubmitSearchRedirect' which can be found in 'CORE.JS' file (path "C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\TEMPLATE\LAYOUTS\1033")
// Renaming the function to 'SendSearchRequesttoGSA'. The function will send the search request to GSA.
    function SendSearchRequesttoGSA(strUrl)
    {

        var dropdownScope = document.getElementById("idSearchScope");
        var selectedScopeText = dropdownScope.options[dropdownScope.selectedIndex].text;
        var selectedScopeUrl = dropdownScope.options[dropdownScope.selectedIndex].value;
        var isPublicSearch = document.getElementById("<%=hfPublicSearch.ClientID%>").value;
        
	    var frm=document.forms["frmSiteSearch"];
	    if (frm==null)
	    {
		    if (typeof(MSOWebPartPageFormName) !="undefined")
			    frm=document.forms[MSOWebPartPageFormName];
	    }
	    if (frm !=null)
	    {
		    strUrl=strUrl+"?k="+escapeProperly(document.getElementById('idSearchString').value);

		    var searchScope = frm.elements["SearchScope"];
		    if (searchScope != null && selectedScopeText != "Enterprise")
		    {
			    var searchScopeUrl = searchScope.value;
			    if (searchScopeUrl != "Enterprise")
			    {
				    strUrl = strUrl+"&u="+escapeProperly(searchScopeUrl);
			    }
		    }
		
		
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
		        strUrl=strUrl + "&selectedScope=" + selectedScopeText + "&isPublicSearch=" + isPublicSearch;
		        frm.action = strUrl;
		        frm.submit();
		    }
		    else
		    {
		        strUrl = strUrl + "&selectedScope=" +  selectedScopeText + "&scopeUrl=" + selectedScopeUrl + "&isPublicSearch=" + isPublicSearch;
		        frm.action = strUrl;
		        document.forms
		        frm.submit();
	    	}
	    }
    }

// This function will call the SendSearchRequesttoGSA Javascript function whenever 'Enter' key is pressed.
function SearchKeyDown1(event, strUrl)
{
    if (event.keyCode == 13)
    {
        SendSearchRequesttoGSA(strUrl);
        return false;
    }
    return true;
}
</script>


<script type="text/C#" runat="server">

    // Function that will change the value of hiddenfiled whenever checkbox is checked/ unchecked
    protected void ChangeHiddenfiled(object sender, EventArgs e)
    {
        if (chkPublicSearch.Checked == true)
        {
            hfPublicSearch.Value = "true";
        }
        else if(chkPublicSearch.Checked == false)
        {
            hfPublicSearch.Value = "false";
        }
    }
    
    
    </script>


<table border="0" cellpadding="0" cellspacing="0" class='ms-searchform'>
<tr class='ms-searchbox'>
    <td>
    <asp:HiddenField ID="hfPublicSearch" runat="server"  Value="true"/>
        <!-- Checkbox for enabling Public Search -->
        <!-- column#1 Checkbox -->
       <asp:CheckBox ID="chkPublicSearch" runat="server"  AutoPostBack="true" Checked="true" OnCheckedChanged="ChangeHiddenfiled" TextAlign="Right"  style="vertical-align:bottom;"  ToolTip="Check this to search only public content"  />Public&nbsp;Search
    </td>
    <td>
         <!-- column#2 Search Scope Dropdown bearing scopes '' -->
       <select id='idSearchScope' name='SearchScope' style="width:211px" class='ms-searchbox' title="<%SPHttpUtility.AddQuote(SPHttpUtility.HtmlEncode(SearchScopeToolTip),Response.Output);%>">
       <option value="Enterprise">Enterprise</option>
       <%
            if (strScopeWeb != null)
            {
        %>
        <option value="<%=strScopeWeb%>">
            <SharePoint:EncodedLiteral runat="server" Text="Current Site"
                EncodeMethod='HtmlEncode' ID='idSearchScopeSite' />
        </option>
        <option value="<%=strScopeWeb%>">
            <SharePoint:EncodedLiteral runat="server" Text="Current Site and all subsites" EncodeMethod='HtmlEncode'
                ID='idSearchScopeSiteandSubsite' />
        </option>
        <%
            }
        %>
        <%
            if (strScopeList != null)
            {    
        %>
        <option value="<%=strScopeList%>">
            <SharePoint:EncodedLiteral runat="server" Text="Current List"
                EncodeMethod='HtmlEncode' ID='idSearchScopeList' />
        </option>
        <%
            }
        %>
        <%
            if (strScopeFolder != null)
            { 
        %>
        <option value="<%=strScopeFolder%>">
            <SharePoint:EncodedLiteral runat="server" Text="Current Folder" EncodeMethod='HtmlEncode'
                ID='idSearchScopeFolder' />
        </option>
        <option value="<%=strScopeFolder%>">
            <SharePoint:EncodedLiteral runat="server" Text="Current Folder and all subfolders"
                EncodeMethod='HtmlEncode' ID='idSearchScopeFolderandSubfolders' />
        </option>
        <%
            }
        %>
    </select>
   </td>
        <!--column#3 Search Box-->
    <td>
        <div style="background-image: url(/_layouts/images/google_custom_search_watermark.gif);
            background-repeat: no-repeat; background-position: left; background-color: Transparent">
            <input type='text' id='idSearchString' size='28' value="" name='SearchString' display='inline'
                maxlength='255' accesskey='S' class='ms-searchbox' style="width: auto; height: auto;
                background-color: transparent" onfocus="javascript: var f = document.getElementById('idSearchString'); f.style.background = '#ffffff';"
                onblur="javascript: var f = document.getElementById('idSearchString');f.style.background = 'background-color: transparent';"
                onkeydown="return SearchKeyDown1(event, <%=strEncodedUrl%>);" title="<%SPHttpUtility.AddQuote(SPHttpUtility.HtmlEncode(SearchTextToolTip),Response.Output);%>" />
        </div>
    </td>
    <!--column#4 Search Button-->
    <td>
        <div class="ms-searchimage">
            <a target='_self' href='javascript:' onclick="javascript:SendSearchRequesttoGSA(<%=strEncodedUrl%>);javascript:return false;"
                title="<%SPHttpUtility.AddQuote(SPHttpUtility.HtmlEncode(SearchImageToolTip),Response.Output);%>"
                id="onetIDGoSearch">
                <img border='0' src="/_layouts/images/gosearch.gif" alt="<%SPHttpUtility.AddQuote(SPHttpUtility.HtmlEncode(SearchImageToolTip),Response.Output);%>"></a></div>
    </td>
    </tr>
</table>