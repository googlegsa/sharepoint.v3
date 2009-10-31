using System;
using System.Windows.Forms;
using Microsoft.Deployment.WindowsInstaller;
using GSBControlPanel;
using System.IO;
using Microsoft.SharePoint;
using Microsoft.SharePoint.Administration;

namespace GSBCustomAction
{
    public class CustomActions
    {
        [CustomAction]
        public static ActionResult FileRestore(Session session)
        {
            session.Log("restore the files");

            try
            {
                String myBasePath = session["INSTALLDIR"];//12 hive
                myBasePath += "Template";
                if (File.Exists(myBasePath + "\\CONTROLTEMPLATES\\SearchArea.ascx.backup"))
                {
                    if (File.Exists(myBasePath + "\\CONTROLTEMPLATES\\SearchArea.ascx"))
                    {
                        File.Delete(myBasePath + "\\CONTROLTEMPLATES\\SearchArea.ascx");
                    }

                    File.Move(myBasePath + "\\CONTROLTEMPLATES\\SearchArea.ascx.backup", myBasePath + "\\CONTROLTEMPLATES\\SearchArea.ascx");//restore original search control file
                }

                if (File.Exists(myBasePath + "\\LAYOUTS\\searchresults.aspx.backup"))
                {
                    if (File.Exists(myBasePath + "\\LAYOUTS\\searchresults.aspx"))
                    {
                        File.Delete(myBasePath + "\\LAYOUTS\\searchresults.aspx");
                    }
                    File.Move(myBasePath + "\\LAYOUTS\\searchresults.aspx.backup", myBasePath + "\\LAYOUTS\\searchresults.aspx");//restore original search control file
                }
            }
            catch (Exception e) 
            {
                session.Log(e.Message);
                session.Log(e.StackTrace);
            }

            return ActionResult.Success;
        }

        [CustomAction]
        public static ActionResult CleanUpWebAppSettings(Session session)
        {
            session.Log("Begin CleanUpWebAppSettings");
            
            

            ///////////////////////////////////
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


            return ActionResult.Success;
        }


        [CustomAction]
        public static ActionResult LaunchWebAppList(Session session)
        {
            session.Log("Begin LaunchWebAppList");
            //DialogResult result = MessageBox.Show("Hello I am custom action", "Serach Box", MessageBoxButtons.OKCancel);

            //Get the Installer location
            String location = session["INSTALLDIR"];
            //MessageBox.Show("Path", location);
//            frmWebApplicationList webAppForm = new frmWebApplicationList("c:", true);
            frmWebApplicationList webAppForm = new frmWebApplicationList(location+"Template", true);
            webAppForm.BringToFront();//Get the web applications for the sharepoint installation
            DialogResult result = webAppForm.ShowDialog();



            if (result == DialogResult.Cancel)
            {
                //throw new InstallerException("Cancelling Installation ....");//does not work
                //session.SetMode(InstallRunMode.Rollback, true);//does not work
                //Application.Exit();//does not work
                //return ActionResult.UserExit; // does the same thing like fauilure

                /*This is useful when you want to show an error messgae before comming out*/
                Record record = new Record(1);
                record[1] = "Terminating & Rollback the Installation...";
                session.Message(InstallMessage.Info, record);
                Application.DoEvents();

                return ActionResult.Failure;//just interrupts the installation... the files are still installed                
            }
            return ActionResult.Success;
        }
    }
}
