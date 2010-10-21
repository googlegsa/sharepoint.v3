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
    string strScopeFolder = null;
    string siteUrl = "";
    string listType = "";
    string listUrl = "";
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
		var isPublicSearch = document.getElementById("hfPublicSearch");
		
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
		else if(selectedScopeUrl == "Farm" && selectedScopeText == "Farm")
		{
		    strUrl=strUrl + "&selectedScope=" + selectedScopeUrl + "&isPublicSearch=" + isPublicSearch.value;
		    frm.action = strUrl;
		    frm.submit();
		}
		else
		{
		    strUrl = strUrl + "&selectedScope=" +  selectedScopeText + "&scopeUrl=" + selectedScopeUrl + "&isPublicSearch=" + isPublicSearch.value;
		    frm.action = strUrl;
		    document.forms
		    frm.submit();
		}
	}
}

// Function to change the value of hiddenfield whenever the checkbox is checked or unchecked
function checkPublicSearch(chk)
{
    var isPublicSearch = document.getElementById("hfPublicSearch");
    if(chk.checked == true)
    {
        isPublicSearch.value = "true";
    }
    else if(chk.checked == false)
    {
        isPublicSearch.value = "false";
    }
}

</script>



<table border="0" cellpadding="0" cellspacing="0" class='ms-searchform'>
<tr>
<!-- Checkbox for enabling Public Search -->
    <td colspan="2" align="right" style="height:20px">
    <input type="hidden" id="hfPublicSearch" name="PublicSearch" />
        <input type="checkbox" name="chk" id="chkPublicSearch" onclick="checkPublicSearch(this);"  />Public Search <br />
    </td>
</tr>
    <tr>
        <td>
            <select id='idSearchScope' name='SearchScope' class='ms-searchbox' title="<%SPHttpUtility.AddQuote(SPHttpUtility.HtmlEncode(SearchScopeToolTip),Response.Output);%>">
                <option value="Farm">Farm </option>
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
        <!--column#1 Search Box-->
        <td>
            <div style="background-image: url(/_layouts/images/google_custom_search_watermark.gif);
                background-repeat: no-repeat; background-position: center; background-color: Transparent">
                <input type='text' id='idSearchString' size='28' value="" name='SearchString' display='inline'
                    maxlength='255' accesskey='S' class='ms-searchbox' style="width: auto; height: auto;
                    background-color: transparent" onfocus="javascript: var f = document.getElementById('idSearchString'); f.style.background = '#ffffff';"
                    onblur="javascript: var f = document.getElementById('idSearchString');f.style.background = 'background-color: transparent';"
                    onkeydown="return SearchKeyDown(event, <%=strEncodedUrl%>);" title="<%SPHttpUtility.AddQuote(SPHttpUtility.HtmlEncode(SearchTextToolTip),Response.Output);%>" />
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