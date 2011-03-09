//Copyright 2009 Google Inc.

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
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using System.Runtime.InteropServices;
using System.Net;
using System.Security.Cryptography.X509Certificates;

namespace SAMLConfiguration
{
    public partial class frmSAMLConfiguration : Form
    {
        public const String SLASH = "\\";
        public String SamlPath = "saml-bridge\\web.config";
        public const String PROVIDER="provider";
        public const String LOG_LEVEL="log_level";
        public const String ARTIFACT_CONSUMER = "artifact_consumer";

        #region API declarations and constants
        private const int MF_BYPOSITION = 0x400;
        private const int MF_REMOVE = 0x1000;
        private const int MF_DISABLED = 0x2;
        [DllImport("user32.Dll")]
        public static extern IntPtr RemoveMenu(int hMenu, int nPosition, long wFlags);
        [DllImport("User32.Dll")]
        public static extern IntPtr GetSystemMenu(int hWnd, bool bRevert);
        [DllImport("User32.Dll")]
        public static extern IntPtr GetMenuItemCount(int hMenu);
        [DllImport("User32.Dll")]
        public static extern IntPtr DrawMenuBar(int hwnd);
        #endregion API declarations and constants

        //disable the Close button
        public void DisableCloseButton(int hWnd)
        {
            IntPtr hMenu;
            IntPtr menuItemCount;
            hMenu = GetSystemMenu(hWnd, false);//Obtain the handle to the form's system menu
            menuItemCount = GetMenuItemCount(hMenu.ToInt32());// Get the count of the items in the system menu
            RemoveMenu(hMenu.ToInt32(), menuItemCount.ToInt32() - 1, MF_DISABLED | MF_BYPOSITION);// Remove the close menuitem
            RemoveMenu(hMenu.ToInt32(), menuItemCount.ToInt32() - 2, MF_DISABLED | MF_BYPOSITION);// Remove the Separator 
            DrawMenuBar(hWnd);// redraw the menu bar
        }

        /// <summary>
        /// </summary>
        /// <param name="isLoadOldConfiguration">false: When running from the installer , true : otherwise</param>
        public frmSAMLConfiguration(String inSamlPath/*, bool isLoadOldConfiguration*/)
        {
            SamlPath = inSamlPath;
            
            InitializeComponent();
            //if (isLoadOldConfiguration == true)
            //{
                LoadConfigurationFromFileToForm(inSamlPath);
            //}
        }
       
        /// <summary>
        /// Loads the SAML Bridge configuration from web.config to the Form
        /// </summary>
        /// <param name="webConfigFilePath">Relative path to SAMl web.config file</param>
        public void LoadConfigurationFromFileToForm(string webConfigFilePath)
        {
            SAMLConfigurationManager scm = new SAMLConfigurationManager();
            scm.LoadXML(webConfigFilePath);//load the web.config file

            //load and set the value of artifact_consumer
            string myVal = scm.GetNodeValue("/configuration/appSettings/add[@key='" + ARTIFACT_CONSUMER + "']");
            if (null == myVal)
            {
                myVal = "";
            }
            tbArtifactConsumerURL.Text = myVal;

            
            //load and set the value of provider
            //myVal = scm.GetNodeValue("/configuration/appSettings/add[@key='"+PROVIDER+"']");
            //if (null == myVal)
            //{
            //    myVal = "";
            //}
            //tbSAMLProvider.Text = myVal;


            //load and set the value of log level
            myVal = scm.GetNodeValue("/configuration/appSettings/add[@key='"+LOG_LEVEL+"']");
            cbSetLogLevel.Checked = false;    
            if ((null != myVal) && (myVal.ToLower().Equals("debug")))
            {
                cbSetLogLevel.Checked = true;
            }
        }

        private void btnCancel_Click(object sender, EventArgs e)
        {
            this.Close();//close the form
        }
      

        private void tbGSALocation_TextChanged(object sender, EventArgs e)
        {
            if (tbArtifactConsumerURL.Text.Trim().Equals(""))
            {
                btnSave.Enabled = false;
            }
            else
            {
                btnSave.Enabled = true;
            }
        }

        //Save the configuratons to the web.config file of SAML Bridge
        private void btnSave_Click(object sender, EventArgs e)
        {
            SAMLConfigurationManager scm = new SAMLConfigurationManager();
            scm.LoadXML(SamlPath);//load the web.config file
            scm.EnsureParentNodesForSAMLParameters();//ensure that all the nodes are in place
                                                    //else create them

            String LogLevel = cbSetLogLevel.Checked == true ? "debug" : "error";
            scm.ModifyNode("/configuration/appSettings", ARTIFACT_CONSUMER, tbArtifactConsumerURL.Text);
            scm.ModifyNode("/configuration/appSettings", LOG_LEVEL, LogLevel);

            scm.SaveXML();//persist the chnages to the web.config file
            this.Close();//close the form
        }

    }
}