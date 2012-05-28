<%@ page language="C#" autoeventwireup="true" inherits="_Default, App_Web_smsovkgr" %>
<%@ Import Namespace="System.Security.Cryptography.X509Certificates" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head id="Head1" runat="server">
    <title></title>
</head>
<body>
    <form id="form1" runat="server">
    <div>
        <asp:Repeater ID="repeater1" runat="server">
            <HeaderTemplate>
                <table>
                    <tr>
                        <td>
                            Cert
                        </td>
                        <td>
                            Public Key
                        </td>
                        <td>
                            Private Key
                        </td>
                    </tr>
            </HeaderTemplate>
            <ItemTemplate>
                <tr>
                    <td>
                    <%#((X509Certificate2)Container.DataItem).GetNameInfo(X509NameType.SimpleName, false) %>
                    </td>
                    <td>
                    <%#TestCert.HasPublicKeyAccess((X509Certificate2)Container.DataItem) %>
                    </td>
                    <td>
                    <%#TestCert.HasPrivateKeyAccess((X509Certificate2)Container.DataItem)%>
                    </td>
                </tr>
            </ItemTemplate>
            <FooterTemplate>
                </table></FooterTemplate>
        </asp:Repeater>
    </div>
    </form>
</body>
</html>