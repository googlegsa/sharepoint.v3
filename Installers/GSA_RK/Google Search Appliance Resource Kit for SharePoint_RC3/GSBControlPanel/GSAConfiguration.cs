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
using System.IO;
using System.Windows.Forms;

namespace GSBControlPanel
{
    /// <summary>
    /// Stores the value of GSA configuration as string
    /// </summary>
    public class GSAConfiguration
    {
        private string GSALocation;
        private string siteCollection;
        private string frontEnd;
        private string enableLogging;

        //Additional parameters for custom stylesheet
        private string xslGSA2SP = "";
        private string xslSP2result = "";
        private string GSAStyle = "true";
        private string logLocation="";

        public string LogLocation
        {
            get{return logLocation;}
            set{logLocation=value;}
        }

        public string ApplianceLocation
        {
            get { return GSALocation; }
            set { GSALocation = value; }
        }

        public string SiteCollection
        {
            get { return siteCollection; }
            set { siteCollection = value; }
        }

        public string FrontEnd
        {
            get { return frontEnd; }
            set { frontEnd = value; }
        }

        public string EnableLogging
        {
            get { return enableLogging; }
            set { enableLogging = value; }
        }


        public void SaveConfigurationsToFile(string webConfigFilePath,bool isInstaller)
        {
            GSBApplicationConfigManager gcm = new GSBApplicationConfigManager();

            gcm.LoadXML(webConfigFilePath);
            gcm.EnsureParentNodesForGSAParameters();//ensure that all the nodes are in place
                                                    //else create them

            gcm.ModifyNode("/configuration/appSettings", "siteCollection", SiteCollection);
            gcm.ModifyNode("/configuration/appSettings", "GSALocation", ApplianceLocation);
            gcm.ModifyNode("/configuration/appSettings", "frontEnd", FrontEnd);
            gcm.ModifyNode("/configuration/appSettings", "verbose", EnableLogging);
            gcm.ModifyNode("/configuration/appSettings", "GSAStyle", UseGsaStyling);//for custom stylesheet

            
            //this needs to be saved only during installation. should be unchnaged otherwise
            if (isInstaller == true)
            {
                gcm.ModifyNode("/configuration/appSettings", "xslGSA2SP", GsaToSpStyle);//for custom stylesheet
                gcm.ModifyNode("/configuration/appSettings", "xslSP2result", SpToResultStyle);//for custom stylesheet
                
                //add for logging
                gcm.ModifyNode("/configuration/appSettings", "logLocation", logLocation);//for custom stylesheet
            }
            else
            {
                #region Scenario and Sample
                /*
                 Consider scenario: new web app created or user has clicked cancel on the "configuration wizard form" while putting the GSA parameters
                    -E.g. user has clicked cancel while saving the configuration
                    -so as a result the custom stylesheet #1 and #2 location is not getting saved.
                    Note: we can get the location while installation (as per folder location chosen by user). 
                 */

                //Steps to ResolveEventArgs it:
                //1. Read the respective nodes form the web.config of as given web ApplicationException.
                //2. if ValueType = blank or null put value, get current dir and put the values accordingly

                
                //sample values
                //<add key="xslGSA2SP" value="C:\Program Files\Common Files\Microsoft Shared\Web Server Extensions\12\TEMPLATE\GSA2SP.xsl" />
                //<add key="xslSP2result" value="C:\Program Files\Common Files\Microsoft Shared\Web Server Extensions\12\TEMPLATE\SP_Actual.xsl" />
                #endregion Scenario and Sample

                string StylesheetPath= "";
                string CurrentDir = Directory.GetCurrentDirectory();//Get the current Directory value (common for all)
                
                //read style#1
                StylesheetPath = gcm.GetNodeValue("/configuration/appSettings/add[@key='xslGSA2SP']");
                if ((null == StylesheetPath) || (StylesheetPath.Trim().Equals("")))
                {
                    StylesheetPath = CurrentDir + "\\GSA2SP.xsl";//Get the value from web.config
                    gcm.ModifyNode("/configuration/appSettings", "xslGSA2SP", StylesheetPath);//modify node
                }

                //read style#2
                StylesheetPath = gcm.GetNodeValue("/configuration/appSettings/add[@key='xslSP2result']");
                if ((null == StylesheetPath) || (StylesheetPath.Trim().Equals("")))
                {
                    StylesheetPath = CurrentDir + "\\SP_Actual.xsl";//Get the value from web.config
                    gcm.ModifyNode("/configuration/appSettings", "xslSP2result", StylesheetPath);//modify node
                }
            }
            gcm.SaveXML();//finally save the resultant modified values
        }

        

        #region additional parameters for custom styling
        public string GsaToSpStyle
        {
            get { return xslGSA2SP; }
            set { xslGSA2SP = value; }
        }

        public string SpToResultStyle
        {
            get { return xslSP2result; }
            set { xslSP2result = value; }
        }

        public string UseGsaStyling
        {
            get { return GSAStyle; }
            set {
                if ((null!=value)&&(value.ToLower().Equals("false")))
                {
                    GSAStyle = "false";
                }
                else 
                {
                    GSAStyle = "true";
                }
            }
        }
        #endregion

    }
}
