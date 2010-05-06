<%@ Control Language="C#" Inherits="Microsoft.SharePoint.WebControls.SearchArea,Microsoft.SharePoint,Version=12.0.0.0,Culture=neutral,PublicKeyToken=71e9bce111e9429c"    compilationMode="Always"  AutoEventWireup="false"%>
<%@ Register Tagprefix="wssawc" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=12.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> 
<%@ Register Tagprefix="SharePoint" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=12.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register Tagprefix="SharePoint" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=12.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> 
<%@ Register Tagprefix="Utilities" Namespace="Microsoft.SharePoint.Utilities" Assembly="Microsoft.SharePoint, Version=12.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> 
<%@ Import Namespace="Microsoft.SharePoint" %>

<%
	string strScopeWeb = null;
	string strScopeList = null;
	string strWebSelected = null;
	SPWeb web = SPControl.GetContextWeb(Context);
    string strEncodedUrl = SPHttpUtility.EcmaScriptStringLiteralEncode(
        
        SPHttpUtility.UrlPathEncode(web.Url + "/_layouts/GSASearchresults.aspx", false, false)
        );
    
    
	strEncodedUrl = "'" + strEncodedUrl + "'";
	strScopeWeb = "'" + SPHttpUtility.HtmlEncode( web.Url ) + "'";
	SPList list = SPContext.Current.List;
	if ( list != null &&
			 ((list.BaseTemplate != SPListTemplateType.DocumentLibrary && list.BaseTemplate != SPListTemplateType.WebPageLibrary) ||
			  (SPContext.Current.ListItem == null) ||
			  (SPContext.Current.ListItem.ParentList == null) ||
			  (SPContext.Current.ListItem.ParentList != list))
	   )
	{
		strScopeList = list.ID.ToString();
	}
	else
	{
		strWebSelected = "SELECTED";
	}
%>

<!--Amit: overridded the SubmitSearchRedirect function of core.js. Else it fails for aspx pages when doing serach from cached result-->
<script type="text/javascript">
function SubmitSearchRedirect(strUrl)
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
		frm.action=strUrl;
		document.forms
		frm.submit();
	}
}

</script>


<table border="0" cellpadding="0" cellspacing="0" class='ms-searchform'>

<tr>
<!--column#1 Search Box-->
<td>
<div style="background-image: url(/_layouts/images/google_custom_search_watermark.gif); background-repeat: no-repeat; background-position:center;background-color:Transparent">
<input type='text' id='idSearchString' size='28' value="" name='SearchString' display='inline' maxlength='255' ACCESSKEY='S' class='ms-searchbox' style="width:auto;height:auto;background-color:transparent"  onfocus="javascript: var f = document.getElementById('idSearchString'); f.style.background = '#ffffff';" onblur="javascript: var f = document.getElementById('idSearchString');f.style.background = 'background-color: transparent';" onKeyDown="return SearchKeyDown(event, <%=strEncodedUrl%>);" title=<%SPHttpUtility.AddQuote(SPHttpUtility.HtmlEncode(SearchTextToolTip),Response.Output);%>>
</div>
</td>

<!--column#2 Search Button-->
<td>
<div class="ms-searchimage"><a target='_self' href='javascript:' onClick="javascript:SubmitSearchRedirect(<%=strEncodedUrl%>);javascript:return false;" title=<%SPHttpUtility.AddQuote(SPHttpUtility.HtmlEncode(SearchImageToolTip),Response.Output);%> ID=onetIDGoSearch><img border='0' src="/_layouts/images/gosearch.gif" alt=<%SPHttpUtility.AddQuote(SPHttpUtility.HtmlEncode(SearchImageToolTip),Response.Output);%>></a></div>
</td>
</tr>

</table>

