using System;
using System.Text;
using GSBControlPanel;
using System.IO;
using Microsoft.SharePoint;
using Microsoft.SharePoint.Administration;


namespace GSBCleanUpGSASettings
{
    class Program
    {
        static void Main(string[] args)
        {
            try
            {
                foreach (SPWebApplication wa in SPWebService.AdministrationService.WebApplications)
                {
                    try
                    {
                        if (GSBControlPanel.frmWebApplicationList.isLocalWebApplication(wa))
                        {
                            //TODO: need to check if the web application are from the same machine
                            string path = wa.IisSettings[0].Path.ToString();
                            if (null != path)
                            {
                                if (!path.EndsWith("\\"))
                                {
                                    path += "\\" + "web.config";
                                }
                                else
                                {
                                    path += "web.config";
                                }

                                #region delete APP setting nodes
                                GSBApplicationConfigManager mgr = new GSBApplicationConfigManager();
                                mgr.LoadXML(path);
                                mgr.DeleteNode("/configuration/appSettings/add[@key='siteCollection']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='GSALocation']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='frontEnd']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='verbose']");

                                //for custom stylesheet
                                mgr.DeleteNode("/configuration/appSettings/add[@key='xslGSA2SP']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='xslSP2result']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='GSAStyle']");

                                mgr.SaveXML();
                                #endregion save results to file
                            }
                        }//if (isLocalWebApplication(wa))
                    }
                    catch { }
                }
            }
            catch { }

            try
            {
                foreach (SPWebApplication wa in SPWebService.ContentService.WebApplications)
                {
                    try
                    {
                        //need to check if the web application are from the same machine
                        if (GSBControlPanel.frmWebApplicationList.isLocalWebApplication(wa))
                        {
                            string path = wa.IisSettings[0].Path.ToString();
                            if (null != path)
                            {
                                if (!path.EndsWith("\\"))
                                {
                                    path += "\\" + "web.config";
                                }
                                else
                                {
                                    path += "web.config";
                                }
                                #region delete APP setting nodes
                                GSBApplicationConfigManager mgr = new GSBApplicationConfigManager();
                                mgr.LoadXML(path);
                                mgr.DeleteNode("/configuration/appSettings/add[@key='siteCollection']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='GSALocation']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='frontEnd']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='verbose']");

                                //for custom stylesheet
                                mgr.DeleteNode("/configuration/appSettings/add[@key='xslGSA2SP']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='xslSP2result']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='GSAStyle']");

                                mgr.SaveXML();
                                #endregion delete APP setting nodes
                            }
                        }
                    }
                    catch { }
                }
            }
            catch { }
        }
    }
}
