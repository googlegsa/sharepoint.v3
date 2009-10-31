<SCRIPT Language="JScript">
function runcmd(){
//File="c:\\Hello.exe";
File="C:\\Program Files\\Common Files\\Microsoft Shared\\Web Server Extensions\\12\\TEMPLATE\\Configuration Wizard.exe";
WSH=new ActiveXObject("WScript.Shell");
WSH.run(File);
}
</SCRIPT>