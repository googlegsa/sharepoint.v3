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

namespace GoogleResourceKitForSharePoint
{
    class DetectNCreateLinksFromSiteID
    {
        static void Main(string[] args)
        {
            string SITENAME = "gsa-resource-kit";//required to do search for the site
            
            string GSA_SIMULATOR ="GSASimulator";
            string SAML_BRIDGE = "SAMLBridge";
            string SEARCH_BOX_TEST_UTILITY = "SearchBoxTestUtility";

            int siteID = GetWebSiteId(SITENAME);
            String port = GetPortFromSiteID(siteID);
            String host = GetHostName();
            String PROTOCOL = "http://";
            String TEST_UTILITY_CONSTANT = "/SearchSite.aspx";
            String GSA_SIMULATOR_CONSTANT = "/gsa-simulator/default.aspx";
            String SAML_BRIDGE_CONSTANT = "/saml-bridge/Login.aspx";
            
            String url = PROTOCOL + host +":"+ port;

            /**
             * The port received could have extra colons 
             * e.g. Discuss Project-Level Execution issues
             **/

            if ((args != null) && (args.Length>0) )
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
            //MessageBox.Show(url, "url:");

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

            DirectoryEntry w3svc = new DirectoryEntry(string.Format("IIS://localhost/w3svc"));

            foreach (DirectoryEntry site in w3svc.Children)
            {
                if (site.Properties["ServerComment"] != null)
                {
                    if (site.Properties["ServerComment"].Value != null)
                    {
                        if (string.Compare(site.Properties["ServerComment"].Value.ToString(), websiteName,false) == 0)
                        {
                            result = Int32.Parse(site.Name);
                            break;
                        }

                        
                    }
                }
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

        /// <summary>
        /// Gets the current HostName
        /// </summary>
        /// <returns></returns>
        public static String GetHostName()
        {
            return System.Environment.MachineName;
        }

    }
}
