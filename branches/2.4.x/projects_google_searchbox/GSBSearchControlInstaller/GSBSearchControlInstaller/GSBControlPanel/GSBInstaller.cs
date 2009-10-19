using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Configuration.Install;
using System.Collections;
using System.Windows.Forms;

using Microsoft.SharePoint;
using Microsoft.SharePoint.Administration;
using System.IO;
using System.Diagnostics;


namespace GSBControlPanel
{
    [RunInstaller(true)]
    public partial class GSBInstaller : Installer
    {
        //const string dirName = "GSPTempDir";
        string searchControlFile = "";
        string searchResultsFile = "";

        /*Default 12 hive path for SharePoint 2007*/
        string myBasePath = @"C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\TEMPLATE\";
        


        public GSBInstaller()
        {
            InitializeComponent();
        }

        /// <summary>
        /// Create temporary directory for dumping original sharepoint files
        /// </summary>
        /// <param name="path">
        /// Adsolute directory path
        /// </param>
        public void CreateDir(string path)
        {
            //check if the directory with the same name exists
            if (!Directory.Exists(path))
            {
                DirectoryInfo di = Directory.CreateDirectory(path);//create a directory
            }
        }


       /* public override void Install(IDictionary savedState)
        {
            base.Install(savedState);
            string myBasePath = Context.Parameters["rootdir"];
            frmWebApplicationList webAppForm = new frmWebApplicationList(myBasePath, true);
            webAppForm.BringToFront();
            DialogResult result = webAppForm.ShowDialog();

            if (result == DialogResult.Cancel)
            {
                myBasePath = Context.Parameters["rootdir"];
                if (!myBasePath.EndsWith("\\"))
                {
                    myBasePath += "\\";
                }

                searchControlFile = myBasePath + @"\CONTROLTEMPLATES\SearchArea.ascx";//search control file
                searchResultsFile = myBasePath + @"\LAYOUTS\searchresults.aspx";//search results files

                if (File.Exists(searchControlFile))
                {
                    File.Delete(searchControlFile);
                }
                if (File.Exists(searchResultsFile))
                {
                    File.Delete(searchResultsFile);
                }

                if (File.Exists(myBasePath + "\\SearchArea.ascx.old"))
                {
                    File.Move(myBasePath + "\\SearchArea.ascx.old", searchControlFile);//restore original search control file
                }

                if (File.Exists(myBasePath + "\\searchresults.aspx.old"))
                {
                    File.Move(myBasePath + "\\searchresults.aspx.old", searchResultsFile);//restore original search results file
                }


                throw new InstallException("Cancelling Installation ....");
            }
           
        }*/

        protected override void OnAfterInstall(System.Collections.IDictionary savedState)
        //public override void Install(IDictionary savedState)
        {
            /*
             * Sequence of Installation:
             * 1. Show List of web applications - if cancel rollback
             *      if OK. show the GSA configuration screen
             * 2. Backup the original search control files
             * 3. Copy the new files
             * 4. Install the Search Box feature
             */

            base.OnAfterInstall(savedState);//call the default behaviour
            string myBasePath = Context.Parameters["rootdir"];//the installation directory

            frmWebApplicationList webAppForm = new frmWebApplicationList(myBasePath, true);
            webAppForm.BringToFront();//Get the web applications for the sharepoint installation
            DialogResult result = webAppForm.ShowDialog();

            if (result == DialogResult.Cancel)
            {
                throw new InstallException("Cancelling Installation ....");
            }

            searchControlFile = myBasePath + @"\CONTROLTEMPLATES\SearchArea.ascx";//search control file
            searchResultsFile = myBasePath + @"\LAYOUTS\searchresults.aspx";//search result file

            try
            {
                
                /////////////////////////////////////// Backing up the Old search control files ////////////////
                if (File.Exists(searchControlFile))
                {
                    //MessageBox.Show("Install: Backup the search control file");
                    //need to check if the file in target is already there .. if so move will fail
                    if (!File.Exists(myBasePath + "\\SearchArea.ascx.old"))
                    {
                        File.Move(searchControlFile, myBasePath + "\\SearchArea.ascx.old");//backup original search control file
                    }
                    else
                    {
                        File.Delete(searchControlFile);// duplicate copy.. delete it
                    }
                }
                if (File.Exists(searchResultsFile))
                {
                    //MessageBox.Show("Install: Backup the search results file");
                    //need to check if the file in target is already there .. if so move will fail
                    if (!File.Exists(myBasePath + "\\searchresults.aspx.old"))
                    {
                        File.Move(searchResultsFile, myBasePath + "\\searchresults.aspx.old");//backup original search results file
                    }
                    else 
                    {
                        File.Delete(searchResultsFile);//duplicate copy .. remove it
                    }
                }
                /////////////////////////////////////// End: Backing up the Old search control files ////////////////

                
                if (File.Exists(myBasePath + "\\GSPTemp\\SearchArea.ascx.new"))
                {
//                    MessageBox.Show("Installer: copying the new search control");
                    
                    //if old file resides .. delete it
                    if (File.Exists(searchControlFile))
                    {
                        File.Delete(searchControlFile);
                    }

                    File.Copy(myBasePath + "\\GSPTemp\\SearchArea.ascx.new", searchControlFile);//copy new search control
                }

                if (File.Exists(myBasePath + "\\GSPTemp\\searchresults.aspx.new"))
                {
//                    MessageBox.Show("Installer: copying the new search results");

                    //if old file resides .. delete it
                    if (File.Exists(searchResultsFile))
                    {
                        File.Delete(searchResultsFile);
                    }

                    File.Copy(myBasePath + "\\GSPTemp\\searchresults.aspx.new", searchResultsFile);//copy new search results page
                }
            }
            catch (Exception) { }


//            MessageBox.Show("Installer: installing the feature");
            InstallFeature(myBasePath + "/..");//Install the search Feature
        }


        //protected override void OnAfterRollback(IDictionary savedState)
        //{
        //    MessageBox.Show("Rolling back the installation");
        //    base.OnAfterRollback(savedState);
        //}

        // Override the 'OnAfterUninstall' method.
        protected override void OnAfterUninstall(IDictionary savedState)
        {
            myBasePath = Context.Parameters["rootdir"];
            if (!myBasePath.EndsWith("\\"))
            {
                myBasePath += "\\";
            }

            searchControlFile = myBasePath + @"\CONTROLTEMPLATES\SearchArea.ascx";//search control file
            searchResultsFile = myBasePath + @"\LAYOUTS\searchresults.aspx";//search results files

            
           

            if (File.Exists(myBasePath + "\\SearchArea.ascx.old"))
            {
//                MessageBox.Show("Uninstall: found old search control file");
                
                if (File.Exists(searchControlFile))
                {
//                    MessageBox.Show("Uninstall: deleting google search control");
                    File.Delete(searchControlFile);
                }

                File.Move(myBasePath + "\\SearchArea.ascx.old", searchControlFile);//restore original search control file
            }

            if (File.Exists(myBasePath + "\\searchresults.aspx.old"))
            {
//                MessageBox.Show("Uninstall: found old search result file");
                if (File.Exists(searchResultsFile))
                {
//                    MessageBox.Show("Uninstall: deleting google search result");
                    File.Delete(searchResultsFile);
                }

                File.Move(myBasePath + "\\searchresults.aspx.old", searchResultsFile);//restore original search results file
            }

//            MessageBox.Show("Uninstall: uninstall feature");
            UnInstallFeature((myBasePath + "/.."));//uninstall the search feature

            
            
            //Detect all installed sharepoint web applications
            //Find corresponding web.config files
            //Delete all the GSA Parameters
            DeleteGSAParameters();
            
            base.OnAfterUninstall(savedState);
        }


        //For deletion of GSA parameters
        //Logic: find all web applications and remove their GSA parameter
        public void DeleteGSAParameters()
        {
            try
            {
                foreach (SPWebApplication wa in SPWebService.AdministrationService.WebApplications)
                {
                    try
                    {
                        if (isLocalWebApplication(wa))
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
                        if (isLocalWebApplication(wa))
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

        /// <summary>
        /// Determines whether the given sharepoint web application is hosted locally or remotely
        /// </summary>
        /// <param name="wa">SharePoitn web application to test</param>
        /// <returns></returns>
        public static bool isLocalWebApplication(SPWebApplication wa)
        {
            string computerName = SystemInformation.ComputerName.ToLower();
            if ((null != wa) && (null != computerName))
            {

                SPVirtualServer vser = wa.Sites.VirtualServer;

                if (vser != null)
                {
                    //string hostName = vser.HostName;
                    string fqdnHost = vser.Url.Host;
                    char[] separator = {'.'};
                    string onlyhost = fqdnHost.Split(separator)[0];

                    if ((null != onlyhost) && (onlyhost.ToLower().Equals(computerName)))
                    {
                        return true;
                    }
                }
            }

            return false;
        }
        /// <summary>
        /// Installs and activates the search control feature
        /// </summary>
        public void InstallFeature(string _sharepointInstallPath)
        {
            //Ref: http://social.msdn.microsoft.com/Forums/en-US/sharepointdevelopment/thread/faa60753-32a6-404e-9e3f-9220b03b79fa/
            string processName = _sharepointInstallPath + @"\Bin\stsadm.exe";
            ExecuteProcessCommand(processName, @" -o installfeature -name GSAFeature -force", _sharepointInstallPath + @"\Bin");
            ExecuteProcessCommand(processName, @" -o activatefeature -name GSAFeature -force", _sharepointInstallPath + @"\Bin");
        }

        public void ExecuteProcessCommand(string FileName, string arguments, string WorkingDirectory)
        {
            using (Process process = new Process())
            {
                process.StartInfo.FileName = FileName;
                process.StartInfo.Arguments = arguments;
                process.StartInfo.WorkingDirectory = WorkingDirectory;
                process.StartInfo.UseShellExecute = false;
                process.StartInfo.RedirectStandardOutput = true;
                process.StartInfo.RedirectStandardError = true;
                process.StartInfo.CreateNoWindow = false;
                process.Start();
                process.WaitForExit(30000);

                string executeResults = process.StandardOutput.ReadToEnd();
            }
        }


        /// <summary>
        /// Deactivate and UnInstall the Feature
        /// </summary>
        public void UnInstallFeature(string _sharepointInstallPath)
        {
            //Ref: http://social.msdn.microsoft.com/Forums/en-US/sharepointdevelopment/thread/faa60753-32a6-404e-9e3f-9220b03b79fa/
            string processName = _sharepointInstallPath + @"\Bin\stsadm.exe";
            ExecuteProcessCommand(processName, @" -o deactivatefeature -name GSAFeature -force", _sharepointInstallPath + @"\Bin");
            ExecuteProcessCommand(processName, @" -o uninstallfeature -name GSAFeature -force", _sharepointInstallPath + @"\Bin");
        }
    }
}