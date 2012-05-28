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
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using System.Runtime.InteropServices;
using System.Net;
using System.Security.Cryptography.X509Certificates;





namespace GSBControlPanel
{
    public partial class frmGSAParams : Form
    {
        //public const String LOG_FILE_NAME = "GSCLog.txt";
        public const String SLASH = "\\";
        public const String XSLGSA2SP = "Template\\GSA2SP.xsl";//For Custom Stylesheet
        public const String XSLSP2RESULT = "Template\\SP_Actual.xsl";//For Custom Stylesheet
        
        //for logging
        public const String LOGGING_PATH = "LOGS";//For Custom Stylesheet

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

        public frmGSAParams()
        {
            InitializeComponent();
        }

        private void btnOk_Click(object sender, EventArgs e)
        {
            String GsaUrl = ""; 
            try
            {
                if(tbGSALocation.Text!=null){
                    if( tbGSALocation.Text.EndsWith("/"))
                    {
                        GsaUrl = tbGSALocation.Text + "search?q=&access=p&client=" + tbFrontEnd.Text + "&output=xml_no_dtd&proxystylesheet=" + tbFrontEnd.Text + "&sort=date%3AD%3AL%3Ad1&entqr=0&oe=UTF-8&ie=UTF-8&ud=1&site=" + tbSiteCollection.Text;
                    }
                    else
                    {
                        GsaUrl = tbGSALocation.Text + "/search?q=&access=p&client=" + tbFrontEnd.Text + "&output=xml_no_dtd&proxystylesheet=" + tbFrontEnd.Text + "&sort=date%3AD%3AL%3Ad1&entqr=0&oe=UTF-8&ie=UTF-8&ud=1&site=" + tbSiteCollection.Text;
                    }
                }
                 
                HttpWebRequest request = (HttpWebRequest)HttpWebRequest.Create(GsaUrl);
                request.Credentials = CredentialCache.DefaultCredentials;//added for SearchSite
                request.Method = "Head";
                
                //added validation for the secured site. This would be the default case for the Search Appliance
                ServicePointManager.ServerCertificateValidationCallback += new System.Net.Security.RemoteCertificateValidationCallback(customXertificateValidation);//security certificate handler

                HttpWebResponse response = (HttpWebResponse)request.GetResponse();
                if (response.StatusCode == HttpStatusCode.OK)
                {
                    this.DialogResult = DialogResult.OK;//indicates that OK is clicked
                    this.Close();//close our form
                }
                else
                {
                    lblCheckConnectivityStatus.Visible = true;
                    lblCause.Text = "StatusCode: "+response.StatusCode.ToString();
                }
            }
            catch (Exception ew)
            {
                lblCheckConnectivityStatus.Visible = true;//In case of Exception do not allow to save the values
                lblCause.Text = "Reason: "+ew.Message.ToString();
                toolTip1.SetToolTip(lblCause, ew.Message.ToString());
            }
        }


        private static bool customXertificateValidation(object sender, X509Certificate cert, X509Chain chain, System.Net.Security.SslPolicyErrors error)
        {
            return true;
        }

        public void LoadConfigurationFromFileToForm(string webConfigFilePath)
        {
            GSBApplicationConfigManager gcm = new GSBApplicationConfigManager();
            gcm.LoadXML(webConfigFilePath);

            string myVal = gcm.GetNodeValue("/configuration/appSettings/add[@key='GSALocation']");

            if (null == myVal)
            {
                myVal = "";
            }
            tbGSALocation.Text = myVal;

            myVal = gcm.GetNodeValue("/configuration/appSettings/add[@key='siteCollection']");
            if (null == myVal)
            {
                myVal = "";
            }
            tbSiteCollection.Text = myVal;

            myVal = gcm.GetNodeValue("/configuration/appSettings/add[@key='frontEnd']");
            if (null == myVal)
            {
                myVal = "";
            }
            tbFrontEnd.Text = myVal;

            myVal = gcm.GetNodeValue("/configuration/appSettings/add[@key='GSAStyle']");
            if (null == myVal)
            {
                myVal = "";
            }
            if (myVal.ToLower().Equals("true"))
            {
                rbGSAFrontEnd.Checked = true;
            }
            else
            {
                rbCustomStylesheet.Checked = true;
            }

            myVal = gcm.GetNodeValue("/configuration/appSettings/add[@key='verbose']");
            if (null == myVal)
            {
                myVal = "";
            }
            if (myVal.ToLower().Equals("true"))
            {
                cbEnableLogging.Checked = true;
            }
            else
            {
                cbEnableLogging.Checked = false;
            }

            // Get value for accesslevel key from web.config and accordingly populate the radiobuttons
            myVal = gcm.GetNodeValue("/configuration/appSettings/add[@key='accesslevel']");
            if (null == myVal)
            {
                myVal = "";
            }
            if (myVal.ToLower().Equals("a"))
            {
                rbPublicAndSecure.Checked = true;
            }
            else if(myVal.ToLower().Equals("p"))
            {
                rbPublic.Checked = true;
            }

            // Get value for defaultSearchType key from web.config and accordingly populate the radiobuttons
            myVal = gcm.GetNodeValue("/configuration/appSettings/add[@key='defaultSearchType']");
            if (null == myVal)
            {
                myVal = "";
            }
            if (myVal.ToLower().Equals("publicAndSecure"))
            {
                rbPublicAndSecureDefaultSearchType.Checked = true;
            }
            else if (myVal.ToLower().Equals("public"))
            {
                rbPublicDefaultSearchType.Checked = true;
            }
        }

        /// <summary>
        /// Map user parameters in configuration form to the values to store in web.config file.
        /// </summary>
        /// <returns></returns>
        public GSAConfiguration PopulateGSAConfiguration(string ApplicationBasePath)
        {
            GSAConfiguration gc = new GSAConfiguration();

            
            if (tbGSALocation.Text != null)
            { 
                //remove the trailing slash if present
                if (tbGSALocation.Text.EndsWith("/"))
                {
                    int length = tbGSALocation.Text.Length;
                    if (length > 2)
                    {
                        gc.ApplianceLocation = tbGSALocation.Text.Substring(0, length - 1);
                    }
                }
                else
                {
                    gc.ApplianceLocation = tbGSALocation.Text;
                }
            }
            else
            {
                gc.ApplianceLocation = "";
            }

            gc.SiteCollection = tbSiteCollection.Text;
            gc.EnableLogging = cbEnableLogging.Checked.ToString();
            gc.FrontEnd = tbFrontEnd.Text;

            //additonal parameters
            if (null != ApplicationBasePath)
            {
                gc.GsaToSpStyle = ApplicationBasePath +  XSLGSA2SP;
                gc.SpToResultStyle = ApplicationBasePath + XSLSP2RESULT;
                
                //for logging
                gc.LogLocation = ApplicationBasePath + LOGGING_PATH;
            }
            if (rbCustomStylesheet.Checked == true)
            {
                gc.UseGsaStyling = "false";
            }
            else if (rbGSAFrontEnd.Checked == true)
            {
                gc.UseGsaStyling = "true";
            }

            if (rbPublic.Checked == true)
            {
                gc.AccessLevel = "p";
            }
            else if (rbPublicAndSecure.Checked == true)
            {
                gc.AccessLevel = "a";
            }

            if (rbPublicAndSecureDefaultSearchType.Checked == true)
            {
                gc.DefaultSearchType = "publicAndSecure";
            }
            else if (rbPublicDefaultSearchType.Checked == true)
            {
                gc.DefaultSearchType = "public";
            }

            return gc;
        }

        private void btnCancel_Click(object sender, EventArgs e)
        {
            this.Close();//close the form
        }
      

        private void tbGSALocation_TextChanged(object sender, EventArgs e)
        {
            if ((tbSiteCollection.Text.Trim().Equals("")) || (tbGSALocation.Text.Trim().Equals("")) || (tbFrontEnd.Text.Trim().Equals("")))
            {
                btnOk.Enabled = false;
            }
            else
            {
                btnOk.Enabled = true;
            }
        }

        private void tbSiteCollection_TextChanged(object sender, EventArgs e)
        {
            if ((tbSiteCollection.Text.Trim().Equals("")) || (tbGSALocation.Text.Trim().Equals("")) || (tbFrontEnd.Text.Trim().Equals("")))
            {
                btnOk.Enabled = false;
            }
            else
            {
                btnOk.Enabled = true;
            }
        }

        private void tbFrontEnd_TextChanged(object sender, EventArgs e)
        {
            if ((tbSiteCollection.Text.Trim().Equals("")) || (tbGSALocation.Text.Trim().Equals("")) || (tbFrontEnd.Text.Trim().Equals("")))
            {
                btnOk.Enabled = false;
            }
            else
            {
                btnOk.Enabled = true;
            }
        }
    }
}