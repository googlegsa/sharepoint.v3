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
        private string logLocation = "";
        private string accesslevel = "a";
        private string omitSecureCookie = "false";
        private string sessionStateModule = "System.Web.SessionState.SessionStateModule";
        private string defaultSearchType = "publicAndSecure";
        private string searchTipsHTMLFileName = "user_help.html";

        private Boolean useSamlPost;

        public Boolean UseSamlPost
        {
            get { return useSamlPost; }
            set { useSamlPost = value; }
        }
        private String artifactConsumer;

        public String ArtifactConsumer
        {
            get { return artifactConsumer; }
            set { artifactConsumer = value; }
        }
        private String certificateName;

        public String CertificateName
        {
            get { return certificateName; }
            set { certificateName = value; }
        }

        private String idpEntityId;
        public String IDPEntityID
        {
            get { return idpEntityId; }
            set { idpEntityId = value; }
        }

        private int trustDuration;
        public int TrustDuration
        {
            get { return trustDuration; }
            set { trustDuration = value; }
        }

        private Boolean enableEmbeddedMode = false;
        public Boolean EnableEmbeddedMode
        {
            get { return enableEmbeddedMode; }
            set { enableEmbeddedMode = value; }
        }

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

        public string AccessLevel
        {
            get { return accesslevel; }
            set { accesslevel = value; }
        }

        public string OmitSecureCookie
        {
            get { return omitSecureCookie; }
            set { omitSecureCookie = value; }
        }

        public string SessionStateModule
        {
            get { return sessionStateModule; }
            set { sessionStateModule = value; }
        }

        public string DefaultSearchType
        {
            get { return defaultSearchType; }
            set { defaultSearchType = value; }
        }

        public string SearchTipsHTMLFileName
        {
            get { return searchTipsHTMLFileName; }
            set { searchTipsHTMLFileName = value; }
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
            gcm.ModifyNode("/configuration/appSettings", "accesslevel", AccessLevel);//for 'public' or 'public and secure' search with GSBS
            gcm.ModifyNode("/configuration/appSettings", "omitSecureCookie", OmitSecureCookie);// Included for the BoA secure cookie issue. Will decide whether to process the cookie or discard the same.

            /*
            * Denotes the default search type used. Value "publicAndSecure" denotes the default search type will be 'public and secure',
            * and value "public" denotes the default search type will be 'public' search.
            */
            gcm.ModifyNode("/configuration/appSettings", "defaultSearchType", DefaultSearchType);

            // Denotes the name for the help file for search tips
            gcm.ModifyNode("/configuration/appSettings", "SearchTipsHTMLFileName", SearchTipsHTMLFileName);

            // Code for enabling Session State on SharePoint Web Application
            gcm.ModifyNodeForHttpModule("//httpModules", "Session", SessionStateModule);

            gcm.ModifyNode("/configuration/appSettings", "EnableEmbeddedMode", EnableEmbeddedMode.ToString().ToLower());
            // UseContainerTheme configuartion parameter value should be same as EnableEmbeddedMode
            gcm.ModifyNode("/configuration/appSettings", "UseContainerTheme", EnableEmbeddedMode.ToString().ToLower());

            gcm.ModifyNode("/configuration/appSettings", "UseSamlPost", UseSamlPost.ToString().ToLower());
            gcm.ModifyNode("/configuration/appSettings", "assertion_consumer", artifactConsumer);
            gcm.ModifyNode("/configuration/appSettings", "certificate_friendly_name", CertificateName);
            gcm.ModifyNode("/configuration/appSettings", "idp_entity_id", IDPEntityID);
            gcm.ModifyNode("/configuration/appSettings", "trust_duration", TrustDuration.ToString());
            
            //this needs to be saved only during installation. should be unchnaged otherwise
            if (isInstaller == true)
            {
                gcm.ModifyNode("/configuration/appSettings", "xslGSA2SP", GsaToSpStyle);//for custom stylesheet
                gcm.ModifyNode("/configuration/appSettings", "xslSP2result", SpToResultStyle);//for custom stylesheet
                
                //add for logging
                gcm.ModifyNode("/configuration/appSettings", "logLocation", logLocation);//for custom stylesheet
                // Setting filterParameter to "p" only during initial deployment.
                gcm.ModifyNode("/configuration/appSettings", "filterParameter", "p");
                // ShowScope and ShowSearchTips parameters to True only during initial deployment
                gcm.ModifyNode("/configuration/appSettings", "showScope", "true");
                gcm.ModifyNode("/configuration/appSettings", "showSearchTips", "true");
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
