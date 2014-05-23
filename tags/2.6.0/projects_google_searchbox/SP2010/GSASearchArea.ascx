<%@ Control Language="C#" Inherits="System.Web.UI.UserControl"    compilationMode="Always"  AutoEventWireup="false"%>
<%@ Register Tagprefix="wssawc" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> 
<%@ Register Tagprefix="SharePoint" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register Tagprefix="SharePoint" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> 
<%@ Register Tagprefix="Utilities" Namespace="Microsoft.SharePoint.Utilities" Assembly="Microsoft.SharePoint, Version=14.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> 
<%@ Import Namespace="Microsoft.SharePoint" %>

<!--Author: Amit Agrawal-->
<%
	SPWeb web = SPControl.GetContextWeb(Context);
    string strEncodedUrl = SPHttpUtility.EcmaScriptStringLiteralEncode(
        SPHttpUtility.UrlPathEncode(web.Url + "/_layouts/GSASearchresults.aspx", false, false)
        );
	strEncodedUrl = "'" + strEncodedUrl + "'";
%>

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
		strUrl=strUrl+"?k="+escapeProperly(document.getElementById('ctl00_PlaceHolderSearchArea_ctl01_txtSearch').value);
		frm.action=strUrl;
		document.forms
		frm.submit();
	}
}
</script>


<!--Search Controls-->
<asp:Panel ID="pnlSearchBoxPanel" runat="server">
    <asp:TextBox ID="txtSearch" runat="server" Width="242px" size='28'  Text="" style="background-image: url('/_layouts/images/google_custom_search_watermark.gif'); background-repeat: no-repeat; background-position:center;background-color:Transparent"></asp:TextBox>
</asp:Panel>

<div class="ms-searchimage">
<a target='_self' 
	href='javascript:' 
	onClick="javascript:SubmitSearchRedirect(<%=strEncodedUrl%>);javascript:return false;" 
	ID=onetIDGoSearch>
	<img border='0' src="/_layouts/images/gosearch.gif" alt='Go!'>
</a>
</div>