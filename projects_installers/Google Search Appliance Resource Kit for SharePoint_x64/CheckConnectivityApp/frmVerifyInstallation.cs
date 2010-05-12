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
using System.Net;
using System.Web;
using System.Runtime.InteropServices;

/*
 *Author: Amit Agrawal
 *Module: Custom web service installer 
 */
namespace WindowsApplication1
{
    public partial class frmVerifyInstallation : Form
    {
        private const String TITLE = "Google Services for SharePoint- Verify Installation";
        private const String TITLE_VERIFYING = TITLE + " (verifying)";

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

        public frmVerifyInstallation()
        {
            InitializeComponent();
        }

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

        private void Form1_Load(object sender, EventArgs e)
        {
            DisableCloseButton(this.Handle.ToInt32());
            lblWarning.Visible = false;//disable the warning message
        }

        private void btnCheckConnectivity_Click(object sender, EventArgs e)
        {
            this.Text = TITLE_VERIFYING;//temporary chnage due to cnnection
            lblWarning.Visible = false;
            lblWarning.ForeColor = Color.Red;

            if (txtUrl.Text.Trim().Equals(""))
            {
                lblWarning.Text = "ERROR: Web Site URL cannot be blank. Please enter valid web site URL";
                lblWarning.Visible = true;
            }
            else if (cbUseWindowsSessionCreds.Checked != true)
            {
                //check for the default credentails
                if (txtUserName.Text.Trim().Equals(""))
                {
                    lblWarning.Text = "ERROR: User name cannot be blank. Please enter valid user name";
                    lblWarning.Visible = true;
                }
                else if (txtPassword.Text.Trim().Equals(""))
                {
                    lblWarning.Text = "ERROR: Password cannot be blank. Please enter valid password";
                    lblWarning.Visible = true;
                }
            }//end: else

            String webServiceEndPointURL = "";
            if (lblWarning.Visible == false)
            {

                if (txtUrl.Text.EndsWith("/"))
                {
                    webServiceEndPointURL = txtUrl.Text + "_vti_bin/GSSiteDiscovery.asmx";
                }
                else
                {
                    webServiceEndPointURL = txtUrl.Text + "/_vti_bin/GSSiteDiscovery.asmx";
                }
                try
                {
                    GSPSiteDiscoveryWS.SiteDiscovery customser = new GSPSiteDiscoveryWS.SiteDiscovery();
                    customser.Url = webServiceEndPointURL;

                    //Check: if the user is using the default credentials
                    if (cbUseWindowsSessionCreds.Checked == true)
                    {
                        customser.Credentials = CredentialCache.DefaultCredentials;
                    }
                    else
                    {
                        customser.Credentials = new NetworkCredential(txtUserName.Text, txtPassword.Text, txtDomain.Text);
                    }

                    String val = customser.CheckConnectivity();
                    if (!val.ToLower().Equals("success"))
                    {
                        lblWarning.Text = "ERROR: Connection to Google Services for SharePoint failed";
                        lblWarning.Visible = true;
                    }
                }
                catch (Exception)
                {
                    lblWarning.Text = "ERROR: Connection to Google Services for SharePoint failed";
                    lblWarning.Visible = true;
                }

                //case of bulkWS
                if (lblWarning.Visible == false)
                {
                    if (txtUrl.Text.EndsWith("/"))
                    {
                        webServiceEndPointURL = txtUrl.Text + "_vti_bin/GSBulkAuthorization.asmx";
                    }
                    else
                    {
                        webServiceEndPointURL = txtUrl.Text + "/_vti_bin/GSBulkAuthorization.asmx";
                    }
                    try
                    {
                        GSPBulkWS.BulkAuthorization bulkWs = new GSPBulkWS.BulkAuthorization();
                        bulkWs.Url = webServiceEndPointURL;
                        //Check: if the user is using the default credentials
                        if (cbUseWindowsSessionCreds.Checked == true)
                        {
                            bulkWs.Credentials = CredentialCache.DefaultCredentials;
                        }
                        else
                        {
                            bulkWs.Credentials = new NetworkCredential(txtUserName.Text, txtPassword.Text, txtDomain.Text);
                        }

                        //bulkWs.Credentials = new NetworkCredential(txtUserName.Text, txtPassword.Text, txtDomain.Text);
                        String val = bulkWs.CheckConnectivity();
                        if (!val.ToLower().Equals("success"))
                        {
                            lblWarning.Text = "ERROR: Connection to Google Services for SharePoint failed";
                            lblWarning.Visible = true;
                        }
                    }
                    catch (Exception ee)
                    {
                        //lblWarning.Text = "ERROR: Connection to Google Services for SharePoint failed";
                        lblWarning.Text = ee.Message;
                        lblWarning.Visible = true;
                    }
                }

                //case of ACL WS
                if (lblWarning.Visible == false)
                {
                    if (txtUrl.Text.EndsWith("/"))
                    {
                        webServiceEndPointURL = txtUrl.Text + "_vti_bin/GssAcl.asmx";
                    }
                    else
                    {
                        webServiceEndPointURL = txtUrl.Text + "/_vti_bin/GssAcl.asmx";
                    }
                    try
                    {

                        GSPAclWS.GssAclMonitor aclWS = new GSPAclWS.GssAclMonitor();
                        aclWS.Url = webServiceEndPointURL;
                        
                        //Check: if the user is using the default credentials
                        if (cbUseWindowsSessionCreds.Checked == true)
                        {
                            aclWS.Credentials = CredentialCache.DefaultCredentials;
                        }
                        else
                        {
                            aclWS.Credentials = new NetworkCredential(txtUserName.Text, txtPassword.Text, txtDomain.Text);
                        }
                        
                        String val = aclWS.CheckConnectivity();
                        if (!val.ToLower().Equals("success"))
                        {
                            lblWarning.Text = "ERROR: Connection to Google Services for SharePoint failed";
                            lblWarning.Visible = true;
                        }
                    }
                    catch (Exception ee)
                    {
                        lblWarning.Text = ee.Message;
                        lblWarning.Visible = true;
                    }
                }

            }

            if (lblWarning.Visible == false)
            {
                //check if the operation completed successfully
                lblWarning.ForeColor = Color.Green;
                lblWarning.Text = "Connection to Google Services for SharePoint succeeded";
                lblWarning.Visible = true;
            }

            this.Text = TITLE;//restore the original title
        }

        //skip button
        private void button1_Click(object sender, EventArgs e)
        {
            this.Close();//close the form
        }

        private void cbUseWindowsSessionCreds_CheckedChanged(object sender, EventArgs e)
        {
            bool status = !cbUseWindowsSessionCreds.Checked;

            txtDomain.Enabled = status;
            txtPassword.Enabled = status;
            txtUserName.Enabled = status;
        }

    }
}