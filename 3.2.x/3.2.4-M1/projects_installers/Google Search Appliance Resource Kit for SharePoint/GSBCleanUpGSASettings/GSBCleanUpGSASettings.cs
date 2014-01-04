﻿//Copyright 2009 Google Inc.

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

using System;
using System.Text;
using GSBControlPanel;
using System.IO;
using Microsoft.SharePoint;
using Microsoft.SharePoint.Administration;
using System.Diagnostics;

namespace GSBCleanUpGSASettings
{
    /*
     *Author: Amit Agrawal
     *Module: Google Search Box for SharePoint clean up operations during uninstallation
     */
    class GSBCleanUpGSASettings
    {
        static void Main(string[] args)
        {
            #region CleanupGsaWebApplicationSettings
            try
            {
                foreach (SPWebApplication wa in SPWebService.AdministrationService.WebApplications)
                {
                    try
                    {
                        if (GSBControlPanel.frmWebApplicationList.isLocalWebApplication(wa))
                        {
                            //Check if the web application are from the same machine
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
                                mgr.DeleteNode("/configuration/appSettings/add[@key='accesslevel']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='omitSecureCookie']");


                                //for custom stylesheet
                                mgr.DeleteNode("/configuration/appSettings/add[@key='xslGSA2SP']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='xslSP2result']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='GSAStyle']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='logLocation']");
                                
                                // Deleting the HTTPModule corresponding to the session state from web.config file
                                mgr.DeleteNode("//httpModules//add[@name='Session']");

                                // Deleting the node corresponding to default search type
                                mgr.DeleteNode("/configuration/appSettings/add[@key='defaultSearchType']");

                                // Deleting the node corresponding to the HTML help file for Search Tips
                                mgr.DeleteNode("/configuration/appSettings/add[@key='SearchTipsHTMLFileName']");
								
								 // Embedded mode related app settings
                                mgr.DeleteNode("/configuration/appSettings/add[@key='EnableEmbeddedMode']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='UseContainerTheme']");

                                // filterParameter app settings
                                mgr.DeleteNode("/configuration/appSettings/add[@key='filterParameter']");
								
							    // Deleting the node corresponding to the SAML POST
								mgr.DeleteNode("/configuration/appSettings/add[@key='UseSamlPost']");
								mgr.DeleteNode("/configuration/appSettings/add[@key='assertion_consumer']");
								mgr.DeleteNode("/configuration/appSettings/add[@key='certificate_friendly_name']");

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
                                mgr.DeleteNode("/configuration/appSettings/add[@key='accesslevel']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='omitSecureCookie']");

                                //for custom stylesheet
                                mgr.DeleteNode("/configuration/appSettings/add[@key='xslGSA2SP']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='xslSP2result']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='GSAStyle']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='logLocation']");

                                // Deleting the HTTPModule corresponding to the session state from web.config file
                                mgr.DeleteNode("//httpModules//add[@name='Session']");

                                // Deleting the node corresponding to default search type
                                mgr.DeleteNode("/configuration/appSettings/add[@key='defaultSearchType']");

                                // Deleting the node corresponding to the HTML help file for Search Tips
                                mgr.DeleteNode("/configuration/appSettings/add[@key='SearchTipsHTMLFileName']");

                                // Embedded mode related app settings
                                mgr.DeleteNode("/configuration/appSettings/add[@key='EnableEmbeddedMode']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='UseContainerTheme']");

                                // filterParameter app settings
                                mgr.DeleteNode("/configuration/appSettings/add[@key='filterParameter']");

                                // Deleting the node corresponding to the SAML POST
                                mgr.DeleteNode("/configuration/appSettings/add[@key='UseSamlPost']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='assertion_consumer']");
                                mgr.DeleteNode("/configuration/appSettings/add[@key='certificate_friendly_name']");

                                mgr.SaveXML();
                                #endregion delete APP setting nodes
                            }
                        }
                    }
                    catch { }
                }
            }
            catch { }
            #endregion CleanupGsaWebApplicationSettings

            try
            {
                String myBasePath = args[0];//12 hive
                //Note: due to a bug in installshield, we get additional Quote(") in the path. It needs to be handled
                if (myBasePath.EndsWith("\""))
                {
                    myBasePath = myBasePath.Substring(0, myBasePath.Length - 1);
                }

                if (!myBasePath.EndsWith("\\"))
                {
                    myBasePath += "\\";
                }
                try
                {
                    UnInstallFeature(myBasePath);//Deactivate and Un-Install GSA search feature
                }
                catch (Exception)
                { }
            }
            catch (Exception)
            {
                //do nothing ... as can't log exception while installation
            }

        }//end: Main()

        public static void ExecuteProcessCommand(string FileName, string arguments, string WorkingDirectory)
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
        public static void UnInstallFeature(string _sharepointInstallPath)
        {
            //Ref: http://social.msdn.microsoft.com/Forums/en-US/sharepointdevelopment/thread/faa60753-32a6-404e-9e3f-9220b03b79fa/
            string processName = _sharepointInstallPath + @"Bin\stsadm.exe";
            ExecuteProcessCommand(processName, @" -o deactivatefeature -name GSAFeature -force", _sharepointInstallPath + @"\Bin");
            ExecuteProcessCommand(processName, @" -o uninstallfeature -name GSAFeature -force", _sharepointInstallPath + @"\Bin");
        }
    }
}
