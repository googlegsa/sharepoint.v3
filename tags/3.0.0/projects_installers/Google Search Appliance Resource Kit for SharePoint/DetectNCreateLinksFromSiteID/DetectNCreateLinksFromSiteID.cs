// Copyright (C) 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

using System;
using System.Collections.Generic;
using System.Text;
using System.DirectoryServices;
using System.Windows.Forms;
using Microsoft.Win32;
using System.IO;

namespace GoogleResourceKitForSharePoint
{
    /*Author: Amit Agrawal*/
    class DetectNCreateLinksFromSiteID
    {
        static void Main(string[] args)
        {
            string SITENAME = "gsa-resource-kit";//required to do search for the site
            string GSA_SIMULATOR = "GSASimulator";
            string SAML_BRIDGE = "SAMLBridge";
            string SEARCH_BOX_TEST_UTILITY = "SearchBoxTestUtility";
            int siteID = GetWebSiteId(SITENAME);
            if (siteID == -1)
            {
                MessageBox.Show("Site '" + SITENAME + "' is not available.", "DetectNCreateLinksFromSiteID.exe", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return;
            }

            String port = GetPortFromSiteID(siteID);

            if (port == string.Empty)
            {
                MessageBox.Show("Could not detect the port for '" + SITENAME + "'", "DetectNCreateLinksFromSiteID.exe", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return;
            }

            String host = GetHostName();
            String PROTOCOL = "http://";
            String TEST_UTILITY_CONSTANT = "/SearchSite.aspx";
            String GSA_SIMULATOR_CONSTANT = "/gsa-simulator/default.aspx";
            String SAML_BRIDGE_CONSTANT = "/saml-bridge/Login.aspx";

            String url = PROTOCOL + host + ":" + port;

            /**
             * The port received could have extra colons 
             * e.g. Discuss Project-Level Execution issues
             **/

            if ((args != null) && (args.Length > 0))
            {
                if (args[0].Equals(GSA_SIMULATOR))
                {
                    url += GSA_SIMULATOR_CONSTANT;//constructing url of GSA Simulator

                }
                else if (args[0].Equals(SAML_BRIDGE))
                {
                    url += SAML_BRIDGE_CONSTANT;//constructing url of SAML Bridge

                }
                else if (args[0].Equals(SEARCH_BOX_TEST_UTILITY))
                {
                    url += TEST_UTILITY_CONSTANT;//constructing url of the test utility

                }
            }
            System.Diagnostics.Process.Start(url);//launch a web site in default browser

        }

        /// <summary>
        /// Get the WebSiteID from the port
        /// </summary>
        /// <param name="serverName"></param>
        /// <param name="websiteName"></param>
        /// <returns></returns>
        public static int GetWebSiteId(string websiteName)
        {
            int result = -1;

            try
            {
                DirectoryEntry w3svc = new DirectoryEntry(string.Format("IIS://localhost/w3svc"));

                foreach (DirectoryEntry site in w3svc.Children)
                {
                    try
                    {
                        if (site.Properties["ServerComment"] != null)
                        {
                            if (site.Properties["ServerComment"].Value != null)
                            {
                                if (string.Compare(site.Properties["ServerComment"].Value.ToString(), websiteName, false) == 0)
                                {
                                    result = Int32.Parse(site.Name);
                                    break;
                                }
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        HandleException(e);
                    }
                }
            }
            catch (Exception e)
            {
                HandleException(e);
            }

            return result;
        }

        /// <summary>
        /// Get the port from site ID
        /// </summary>
        /// <param name="SiteID">IIS Site Identifier</param>
        /// <returns>Port of the given site. e.g. :80</returns>
        public static String GetPortFromSiteID(int SiteID)
        {
            try
            {
                DirectoryEntry site = new DirectoryEntry("IIS://localhost/w3svc/" + SiteID);

                //Get everything currently in the serverbindings propery.
                PropertyValueCollection serverBindings = site.Properties["ServerBindings"];
                String PortString = serverBindings[0].ToString();

                //cleanup the port string
                if (null != PortString)
                {
                    PortString = PortString.Replace(":", "");
                }

                return PortString;//Get the first port
            }
            catch (Exception e)
            {
                HandleException(e);
                return string.Empty;
            }

        }

        /// <summary>
        /// Gets the current HostName
        /// </summary>
        /// <returns></returns>
        public static String GetHostName()
        {
            return System.Environment.MachineName;
        }

        public static void HandleException(Exception e)
        {
            StringBuilder errStr = new StringBuilder(string.Empty);

            try
            {
                errStr.AppendLine(e.Message.Trim() + "\n");
                RegistryKey hklm = Registry.LocalMachine;
                RegistryKey subKeyInetStp = hklm.OpenSubKey("SOFTWARE\\Microsoft\\InetStp", false);
                RegistryKey subKeyInetStpComponents = hklm.OpenSubKey("SOFTWARE\\Microsoft\\InetStp\\Components", false);
                RegistryKey subKeyAspNet = hklm.OpenSubKey("SOFTWARE\\Microsoft\\ASP.NET", false);
                Object objIISv = null;
                Object objMetabase = null;
                Object objASPNETEnabled = null;
                Object objASPNETv = null;
                Int16 IISv = 0;
                Int16 Metabase = 0;
                Int16 ASPNETEnabled = 0;
                Int16 ASPv = 0;

                if (subKeyInetStp != null)
                {
                    objIISv = subKeyInetStp.GetValue("MajorVersion");
                    if (objIISv != null)
                    {
                        IISv = Convert.ToInt16(objIISv);
                    }
                }

                if (subKeyInetStpComponents != null)
                {
                    objMetabase = subKeyInetStpComponents.GetValue("Metabase");
                    if (objMetabase != null)
                    {
                        Metabase = Convert.ToInt16(objMetabase);
                    }

                    objASPNETEnabled = subKeyInetStpComponents.GetValue("ASPNET");
                    if (objASPNETEnabled != null)
                    {
                        ASPNETEnabled = Convert.ToInt16(objASPNETEnabled);
                    }
                }

                if (IISv < 6)
                {
                    errStr.AppendLine("IIS is either not installed or does not meet minimum requirements.  IIS must be version 6.0 or higher.\n");
                }
                else if (IISv == 6)
                {
                    //Check for ASP.NET 2.0
                    if (subKeyAspNet != null)
                    {
                        objASPNETv = subKeyAspNet.GetValue("RootVer");
                        if (objASPNETv != null)
                        {
                            string sASPv = Convert.ToString(objASPNETv);
                            try
                            {
                                //Get the ASP.NET version
                                ASPv = Convert.ToInt16(sASPv.Substring(0, sASPv.IndexOf('.')));
                                if (!(ASPv >= 2))
                                {
                                    errStr.AppendLine("ASP.NET must be version 2.0 or higher.\n");
                                }
                            }
                            catch
                            {
                                errStr.AppendLine("ASP.NET must be version 2.0 or higher.\n");
                            }
                        }
                        else
                        {
                            errStr.AppendLine("Confirm ASP.NET 2.0 or higher is installed.\n");
                        }
                    }
                    else
                    {
                        errStr.AppendLine("Confirm ASP.NET 2.0 or higher is installed.\n");
                    }
                }
                else // IISv > 6
                {
                    /* IIS 6 Metabase Compatibility role service and ASP.NET must be enabled*/
                    if (!(Metabase > 0))
                    {
                        errStr.AppendLine("IIS 6 Metabase Compatibility role service not installed.\n");
                    }
                    if (!(ASPNETEnabled > 0))
                    {
                        errStr.AppendLine("ASP.NET not enabled.\n");
                    }
                }
            }

            catch (Exception) { }

            finally
            {
                if (errStr.ToString() != string.Empty)
                {
                    MessageBox.Show("The following issues were encountered:\n\n" + errStr, "DetectNCreateLinksFromSiteID.exe", MessageBoxButtons.OK, MessageBoxIcon.Error);
                }
            }
        }
    }
}
