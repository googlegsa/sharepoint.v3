<%@ Page Language="VB" Debug="true"%>

<html runat="server">
<head>
<title>Sample Claims Login</title>
</head>
<body runat="server">
<script runat="server">
    Protected Sub Page_Load(ByVal sender As Object, ByVal e As EventArgs) Handles Me.Load
        Dim sReturn As String = "/"
        sReturn = Request.QueryString().ToString()
        If Not String.IsNullOrEmpty(sReturn) Then
            Me.Page.Response.Redirect("/_windows/default.aspx?" + sReturn)
        Else
            ErrorMSG.Visible = True
        End If
    End Sub
</script>
<span id="ErrorMSG" runat="server" visible="false">Unable to authenticate you :( <br />Please try later...</span> 
</body>
</html>
