<%@ Page Language="C#" %>

<%@ Import Namespace="System.IO" %>
<%@ Import Namespace="System.Net" %>
<%@ Import Namespace="System.Web.Configuration" %>

<%
  // Compose the embedded mode specific query string arguments. We don't need
  // to check the application settings related to "enable/disable embedded mode"
  // as any request to this file would mean we are running in embedded mode
  // only. This will not do any harm.
  string embeddedModeQueryArg = "";
  embeddedModeQueryArg = "&emsingleres=" +
      HttpUtility.UrlEncode("/_layouts/GSAForward.aspx?forward=") +
      "&emmain=" +
      HttpUtility.UrlEncode("/_layouts/GSASearchresults.aspx");
  // Read the theme setting as this should be notified forward requests so that
  // GSA respects the theme setting.
  string useContainerTheme =
      WebConfigurationManager.AppSettings["UseContainerTheme"];
  if (useContainerTheme == null ||
      useContainerTheme.Trim().Equals("true")) {
    embeddedModeQueryArg = embeddedModeQueryArg +
        "&emdstyle=true";
  }

  string GSALocation = WebConfigurationManager.AppSettings["GSALocation"];
  if((GSALocation == null) || (GSALocation.Trim().Equals(""))) {
    HttpContext.Current.Response.Write(
        "Google Search Appliance location is not specified");
    HttpContext.Current.Response.End();
    return;
  }
  // Read to which URI endpoint the request should be made on the GSA.
  string strForward = HttpContext.Current.Request.QueryString["forward"];
  // Compose the absolute URL where request to be sent.
  string gsaUrl = GSALocation + "/" + strForward;

  // If the forward request is a search request we should add the search and
  // static root path prefix as well.
  if (strForward.StartsWith("/search")) {
    gsaUrl = gsaUrl + embeddedModeQueryArg;
  }
  // To prevent 417 expectation failed.
  System.Net.ServicePointManager.Expect100Continue = false;
  // Create an HTTP request to the composed GSA URL.
  HttpWebRequest fwdHttpReq = (HttpWebRequest) WebRequest.Create(gsaUrl);
  if (HttpContext.Current.Request.HttpMethod == "POST") {
    // If this is a POST request then copy the body and required request
    // headers.
    fwdHttpReq.Method = HttpContext.Current.Request.HttpMethod;
    fwdHttpReq.ContentType  = HttpContext.Current.Request.ContentType;
    fwdHttpReq.ContentLength  = HttpContext.Current.Request.ContentLength;
    Stream reqStream = fwdHttpReq.GetRequestStream();
    StreamReader sr = new StreamReader(HttpContext.Current.Request.InputStream);
    byte[] bytes = sr.CurrentEncoding.GetBytes(sr.ReadToEnd());
    reqStream.Write(bytes, 0, bytes.Length);
    reqStream.Close();
    sr.Close();
  }

  // Read the response from the forwarded HTTP request and populate the current
  // open response with the same.
  HttpWebResponse response = (HttpWebResponse) fwdHttpReq.GetResponse();
  
  // Copy all HTTP headers.
  for (int i = 0; i < response.Headers.Count; i++) {
    HttpContext.Current.Response.AddHeader(
        response.Headers.Keys[i], response.Headers[i]);
  }
  // Copy all Cookies.
  foreach (Cookie c in response.Cookies) {
    HttpContext.Current.Response.SetCookie(new HttpCookie(c.Name, c.Value));
  }
  // Copy the response body.
  Stream objStream = response.GetResponseStream();
  byte[] buffer = new byte[4096];
  int len = 0;
  while ((len = objStream.Read(buffer, 0, 4096)) > 0 ) {
    if (len < 4096) {
      byte[] buffer2 = new byte[len];
      for (int j = 0; j < len; j++) {
        buffer2[j] = buffer[j];
      }
      HttpContext.Current.Response.BinaryWrite(buffer2);
    } else {
      HttpContext.Current.Response.BinaryWrite(buffer);
    }
  }
  Response.Cache.SetCacheability(HttpCacheability.NoCache);
  HttpContext.Current.Response.StatusCode = (int)response.StatusCode;
  HttpContext.Current.Response.End(); 
%>
