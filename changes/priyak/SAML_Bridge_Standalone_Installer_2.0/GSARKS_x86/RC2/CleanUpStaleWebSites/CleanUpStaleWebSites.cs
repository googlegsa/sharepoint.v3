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
using System.Text;
using System.DirectoryServices;
using System.Windows.Forms;
using System.IO;

namespace GoogleResourceKitForSharePoint
{
    class CleanUpStaleWebSites
    {
        static void Main(string[] args)
        {
            string SITENAME = "saml-bridge-for-windows";//required to do search for the site
            try
            {
                DeleteWebSiteFromName(SITENAME);

                //Delete the folder for the IIS web site and Virtual Directory
                if (args != null)
                {
                    String location = args[0];
                    if (location != null)
                    {
                        try
                        {
                            //Note: due to a bug in installshield, we get additional Quote(") in the path. It needs to be handled
                            if (location.EndsWith("\""))
                            {
                                location = location.Substring(0, location.Length - 1);
                            }

                            DirectoryInfo di = new DirectoryInfo(location);
                            if (di.Exists)
                            {
                                di.Delete(true);//recursively delete all
                            }
                        }
                        catch (Exception)
                        { }

                    }
                }
            }
            catch (Exception) { }
        }

        public static void DeleteWebSiteFromName(string websiteName)
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
                                site.DeleteTree();
                                break;
                            }


                        }
                    }
                }
                catch (Exception) { }

            }

        }
    }
}
