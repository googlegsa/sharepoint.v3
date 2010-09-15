<%@ Control Language="C#" Inherits="Microsoft.SharePoint.WebControls.SearchArea,Microsoft.SharePoint,Version=12.0.0.0,Culture=neutral,PublicKeyToken=71e9bce111e9429c"
    CompilationMode="Always" AutoEventWireup="false" %>
<%@ Register TagPrefix="wssawc" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=12.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register TagPrefix="SharePoint" Namespace="Microsoft.SharePoint.WebControls"
    Assembly="Microsoft.SharePoint, Version=12.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register TagPrefix="SharePoint" Namespace="Microsoft.SharePoint.WebControls"
    Assembly="Microsoft.SharePoint, Version=12.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register TagPrefix="Utilities" Namespace="Microsoft.SharePoint.Utilities" Assembly="Microsoft.SharePoint, Version=12.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Import Namespace="Microsoft.SharePoint" %>
<%
    
  
    
    string strScopeWeb = null;
    string strScopeList = null;
    string strWebSelected = null;
    SPWeb web = SPControl.GetContextWeb(Context);
    
    
    hfIsFolder.Value = "false"; // Variable to be passed as a Querystring to the GSASearchResults page. Used to find whether 
                                // the user is currently searching within a folder.

    string strEncodedUrl = SPHttpUtility.EcmaScriptStringLiteralEncode(

        SPHttpUtility.UrlPathEncode(web.Url + "/_layouts/GSASearchresults.aspx", false, false)
        );


    strEncodedUrl = "'" + strEncodedUrl + "'";
    strScopeWeb = "'" + SPHttpUtility.HtmlEncode(web.Url) + "'";
    SPList list = SPContext.Current.List;
    if (list != null)
    {
        strScopeList = list.DefaultViewUrl.ToString();
        hfListType.Value = list.GetType().ToString();

        if (this.Context.Request.QueryString["RootFolder"] != null)
        {
            strScopeList = this.Context.Request.QueryString["RootFolder"].ToString();
            hfIsFolder.Value = "true";
        }
    }
    else
    {
        strWebSelected = "SELECTED";
        hfListType.Value = web.GetType().ToString();
    }
%>
<!--Amit: overridded the SubmitSearchRedirect function of core.js. Else it fails for aspx pages when doing serach from cached result-->

<script type="text/javascript">

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
		
		strUrl=strUrl+"?k="+escapeProperly(document.getElementById('idSearchString').value);

		var searchScope = frm.elements["SearchScope"];
		if (searchScope !=null && searchScope != "Farm")
		{
			var searchScopeUrl = searchScope.value;
			if (searchScopeUrl != "Farm")
			{
				strUrl = strUrl+"&u="+escapeProperly(searchScopeUrl);
			}
		}
		var dropdownScope = document.getElementById("idSearchScope");
		var selectedScopeText = dropdownScope.options[dropdownScope.selectedIndex].text;
		var selectedScopeUrl = dropdownScope.options[dropdownScope.selectedIndex].value;
		
		/* Checking whether user has selected a list within a site, while selecting option as 'My List'.
		 If user is not browsing the list, then display an eror message as in this case the scope url will be retrieved as "" */
		
		if(selectedScopeUrl == "" && selectedScopeText == "This List") 
		{
		    alert('Please select a list first !');
		}
		else if(selectedScopeUrl == "" && selectedScopeText == "This Site")
		{
		    alert('Please select a site first !');
		}
		else if(selectedScopeUrl == "Farm" && selectedScopeText == "Farm")
		{
		    strUrl=strUrl + "&selectedScope=" + selectedScopeUrl;
		    frm.action = strUrl;
		    frm.submit();
		}
		else
		{
		    // Getting the list type for the current context and passing the value as a querystring
		    listType = document.getElementById("<%=hfListType.ClientID%>").value;
		    
		    // Used to find whether the user is currently searching within a folder.
		    isFolder = document.getElementById("<%=hfIsFolder.ClientID%>").value;
		    		    
		    strUrl = strUrl + "&selectedScope=" +  selectedScopeText + "&scopeUrl=" + selectedScopeUrl + "&listType=" + listType + "&isFolder=" + isFolder;
		    frm.action = strUrl;
		    document.forms
		    frm.submit();
		}
	}
}

</script>

<table border="0" cellpadding="0" cellspacing="0" class='ms-searchform'>
    <tr>
        <td>
            <asp:HiddenField ID="hfListType" runat="server" />
            <asp:HiddenField ID="hfIsFolder" runat="server" />
            <select id='idSearchScope' name='SearchScope' class='ms-searchbox' title="<%SPHttpUtility.AddQuote(SPHttpUtility.HtmlEncode(SearchScopeToolTip),Response.Output);%>">
                <option value="Farm">Farm </option>
                <option value="<%=strScopeWeb%>">
                    <SharePoint:EncodedLiteral runat="server" Text="<%$Resources:wss,search_Scope_Site%>"
                        EncodeMethod='HtmlEncode' ID='idSearchScopeSite' />
                </option>
                <%
                    
                    //if (strScopeList != null)
                    //{
                %>
                <option value="<%=strScopeList%>">
                    <SharePoint:EncodedLiteral runat="server" Text="<%$Resources:wss,search_Scope_List%>"
                        EncodeMethod='HtmlEncode' ID='idSearchScopeList' />
                </option>
                <%
                    //}
                %>
            </select>
        </td>
        <!--column#1 Search Box-->
        <td>
            <div style="background-image: url(/_layouts/images/google_custom_search_watermark.gif);
                background-repeat: no-repeat; background-position: center; background-color: Transparent">
                <input type='text' id='idSearchString' size='28' value="" name='SearchString' display='inline'
                    maxlength='255' accesskey='S' class='ms-searchbox' style="width: auto; height: auto;
                    background-color: transparent" onfocus="javascript: var f = document.getElementById('idSearchString'); f.style.background = '#ffffff';"
                    onblur="javascript: var f = document.getElementById('idSearchString');f.style.background = 'background-color: transparent';"
                    onkeydown="return SearchKeyDown(event, <%=strEncodedUrl%>);" title="<%SPHttpUtility.AddQuote(SPHttpUtility.HtmlEncode(SearchTextToolTip),Response.Output);%>">
            </div>
        </td>
        <!--column#2 Search Button-->
        <td>
            <div class="ms-searchimage">
                <a target='_self' href='javascript:' onclick="javascript:SubmitSearchRedirect1(<%=strEncodedUrl%>);javascript:return false;"
                    title="<%SPHttpUtility.AddQuote(SPHttpUtility.HtmlEncode(SearchImageToolTip),Response.Output);%>"
                    id="onetIDGoSearch">
                    <img border='0' src="/_layouts/images/gosearch.gif" alt="<%SPHttpUtility.AddQuote(SPHttpUtility.HtmlEncode(SearchImageToolTip),Response.Output);%>"></a></div>
        </td>
    </tr>
</table>