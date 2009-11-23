<%@ WebService Language="C#" Class="BulkAuthorization" %>
using System;
using System.Net;
using System.IO;
using System.Web.Services;
using Microsoft.SharePoint;
using Microsoft.SharePoint.Utilities;
using Microsoft.SharePoint.Administration;
using System.Diagnostics;

[WebService(Namespace = "gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
public class BulkAuthorization : System.Web.Services.WebService
{
    public enum LOG_LEVEL
    {
        ERROR,
        INFO,
        DEBUG
    }
    public LOG_LEVEL DEFAULT_LOG_LEVEL = LOG_LEVEL.INFO;
    public const String PRODUCTNAME = "GSS";

    private int LOG_MAX_SIZE = 52428800;
    private int LOG_MAX_COUNT = 10;
    private String logFile = null;
    
    /// <summary>
    /// Authorizes an user against a single authData
    /// </summary>
    /// <param name="authData"></param>
    /// <param name="loginId"></param>
    [WebMethod]
    public void Authorize(AuthData authData, string loginId)
    {
        SPSite site = null;
        SPWeb web = null;
        try
        {
            bool isAlert = false;
      String listURL = authData.listURL;

      SPSecurity.RunWithElevatedPrivileges(delegate()
            {
                // Let's try creating the SPSite object for the incoming URL. If fails, try again by changing the URL format FQDN to Non-FQDN or vice-versa.
            try
            {
              site = new SPSite(listURL);
                    if (site == null)
                    {
                        site = new SPSite(SwitchURLFormat(listURL));
                    }
            }
            catch (Exception e)
            {
                    site = new SPSite(SwitchURLFormat(listURL));
            }
          });

            web = site.OpenWeb();
            SPPrincipalInfo userInfo = SPUtility.ResolveWindowsPrincipal(site.WebApplication, loginId, SPPrincipalType.All, false);
            if (userInfo == null)
            {
                authData.error += "User " + loginId + " can not be resolved. ";
                string logMsg = "Authorization failed because User " + loginId + " can not be resolved into a valid SharePoint user.";
                try
                {
                    log(logMsg, LOG_LEVEL.ERROR);
                }
                catch (Exception e)
                {
                    authData.error += "WS logging attempt failed!! Exception [ " + e.Message + " ]. ";
                }
                return;
            }

            // First ensure that the current user has rights to view pages or list items on the web. This will ensure that SPUser object can be constructed for this username.
            bool web_auth = web.DoesUserHavePermissions(userInfo.LoginName, SPBasePermissions.ViewPages | SPBasePermissions.ViewListItems);

            SPUser user = GetSPUser(web, userInfo.LoginName);
            if (user == null)
            {
                authData.error += "User " + loginId + " not found against web " + web.Url;
                return;
            }

            if (authData.complexDocId != null && authData.complexDocId.StartsWith("[ALERT]"))
            {
                Guid alert_guid = new Guid(authData.listItemId);
                SPAlert alert = web.Alerts[alert_guid];
                if (alert != null)
                {
                    if (alert.User.LoginName.ToUpper().Equals(user.LoginName.ToUpper()))
                    {
                        authData.isAllowed = true;
                    }
                    else
                    {
                        string SPSystemUser = null;
                        SPSecurity.RunWithElevatedPrivileges(delegate()
                        {
                            SPSystemUser = site.WebApplication.ApplicationPool.Username;
                        });

                        if (SPSystemUser != null && SPSystemUser.ToUpper().Equals(user.LoginName.ToUpper()))
                        {
                            // The logged in user is a SHAREPOINT\\system user
                            if (alert.User.LoginName.ToUpper().Equals("SHAREPOINT\\SYSTEM"))
                            {
                                authData.isAllowed = true;
                            }
                        }
                    }
                }
                else
                {
                    authData.error += "Alert not found.";
                    log("Alert not found for AuthData [ " + authData.complexDocId + " ]. ", LOG_LEVEL.ERROR);
                }
                return;
            }

            SPList list = web.GetListFromUrl(listURL);

            if (authData.listItemId == null || authData.listItemId == "" || authData.listItemId.StartsWith("{"))
            {
                bool isAllowed = list.DoesUserHavePermissions(user, SPBasePermissions.ViewListItems);
                authData.isAllowed = isAllowed;                
            }
            else
            {
                int itemId = int.Parse(authData.listItemId);
                SPListItem item = list.GetItemById(itemId);
                bool isAllowed = item.DoesUserHavePermissions(user, SPBasePermissions.ViewListItems);
                authData.isAllowed = isAllowed;
            }
        }
        catch (Exception e)
        {
            authData.error = e.Message;
            string logMsg = "Following error occurred while authorizing user " + loginId + " while authorizing against " + authData.listURL + "|" + authData.listItemId + " :\n" + authData.error;
            try
            {
                log(logMsg, LOG_LEVEL.ERROR);
            }
            catch (Exception e1)
            {
                authData.error += "WS logging attemp failed!! Exception [ " + e1.Message + " ]. ";
            }
        }
        finally
        {
            if (site != null)
            {
                site.Dispose();
            }
            if (web != null)
            {
                web.Dispose();
            }
        }
    }

    /// <summary>
    /// Calls Authorize for each individual authData
    /// </summary>
    /// <param name="authData"></param>
    /// <param name="loginId"></param>
    /// <returns></returns>
    [WebMethod]
    public AuthData[] BulkAuthorize(AuthData[] authData, string loginId)
    {
        string logMsg = "Authorization Request received for user " + loginId + " against #" + authData.Length + " items";
        try
        {
            logFile = getLogFileToWrite();
            log(logMsg, LOG_LEVEL.INFO);
        }
        catch (Exception e)
        {
            authData[0].error += "WS logging attemp failed!! Exception [ " + e.Message + " ]. ";
        }
        foreach (AuthData ad in authData)
        {
            Authorize(ad, loginId);
            try
            {
                log("Authorization status for User [ " + loginId + "]. docID [ " + ad.complexDocId + " ], status [ " + ad.isAllowed + " ]. ", LOG_LEVEL.DEBUG);
            }
            catch (Exception e)
            {
                authData[0].error += "WS logging attemp failed!! Exception [ " + e.Message + " ]. ";
            }
        }
        return authData;
    }

    /// <summary>
    /// Checks if this web service can be called
    /// </summary>
    /// <returns></returns>
    [WebMethod]
    public string CheckConnectivity() {
      try {
        SPSecurity.RunWithElevatedPrivileges(delegate() {        
        });
      }
      catch (Exception e)
      {
        return e.Message;
      }
      return "success";
    }

    /// <summary>
    /// There might be some cases when SPWeb.AllUsers can not return the SPUser object. Hence, we must try all the three: AllUsers, SiteUsers and, Users.
    /// Also, these three properties of SPWeb has been accessed in try blocks in the decreasing order of the possibility of success. AllUsers has the highest possibility to succeed.
    /// </summary>
    /// <param name="username"></param>
    /// <returns></returns>
    private SPUser GetSPUser(SPWeb web, string username)
    {
        try
        {
            return web.AllUsers[username];
        }
        catch (Exception e1)
        {
            try
            {
                return web.SiteUsers[username];
            }
            catch (Exception e2)
            {
                try
                {
                    return web.Users[username];
                }
                catch (Exception e3)
                {
                    return null;
                }
            }
        }
    }

    /// <summary>
    /// Switches the URL format between FQDN and Non-FQDN.
    /// </summary>
    /// <param name="url"></param>
    /// <returns></returns>
    private string SwitchURLFormat(string SiteURL)
    {
        Uri url = new Uri(SiteURL);
        string host = url.Host;
        if (host.Contains("."))
        {
            host = host.Split('.')[0];
        }
        else
        {
            IPHostEntry hostEntry = Dns.GetHostEntry(host);
            host = hostEntry.HostName;
        }
        SiteURL = url.Scheme + "://" + host + ":" + url.Port + url.AbsolutePath;
        return SiteURL;
    }

    /// <summary>
    /// Implements custom logging in a file created under the 12 hive LOGS directory
    /// </summary>
    /// <param name="msg"></param>
    /// <param name="logLevel"></param>
    public void log(String msg, LOG_LEVEL logLevel)
    {
        if (logLevel <= DEFAULT_LOG_LEVEL)
        {
            /*
             * We need to make even a normal user with 'reader' access to be able to log messages
             * This requires to elevate the user temporarily for write operation.
             */
            SPSecurity.RunWithElevatedPrivileges(delegate()
            {
                StreamWriter logger = File.AppendText(logFile);
                logger.WriteLine("[ {0} ]  [{1}] :- {2}", DateTime.Now.ToString(), logLevel, msg);
                logger.Flush();
                logger.Close();
            });
        }
    }

    /// <summary>
    /// Returns a valid log file path which can be written. 
    /// Also takes care of rotating the logs when the maximum count is reached. 
    /// </summary>
    /// <returns></returns>
    private String getLogFileToWrite()
    {
        String log_location = SPUtility.GetGenericSetupPath("LOGS\\");
        String current_log = log_location + PRODUCTNAME + ".0.log";
        FileInfo fileInfo = new FileInfo(current_log);
        if (fileInfo.Exists && fileInfo.Length > LOG_MAX_SIZE)
        {
            // rotate the logs                    
            File.Delete(log_location + PRODUCTNAME + "." + LOG_MAX_COUNT + ".log");
            for (int i = LOG_MAX_COUNT - 1; i >= 0; --i)
            {
                if (File.Exists(log_location + PRODUCTNAME + "." + i + ".log"))
                {
                    int j = i + 1;
                    File.Move(log_location + PRODUCTNAME + "." + i + ".log", log_location + PRODUCTNAME + "." + j + ".log");
                }
            }
        }
        return current_log;
    }
}

/// <summary>
/// The basic authorization unit
/// </summary>
[WebService(Namespace = "BulkAuthorization.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class AuthData
{
    public string listURL;
    public string listItemId;
    public bool isAllowed;
    public string error;
    public string complexDocId;
}