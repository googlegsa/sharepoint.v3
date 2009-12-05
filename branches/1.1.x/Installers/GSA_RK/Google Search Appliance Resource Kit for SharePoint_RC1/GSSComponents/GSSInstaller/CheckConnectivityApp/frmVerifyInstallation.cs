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
 *Date: 04 Dec 2008
 */
namespace WindowsApplication1
{
    public partial class frmVerifyInstallation : Form
    {
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
            //String originalCaption = this.Text;
            this.Text = "Verify Installation -  Verifying";//temporary chnage due to cnnection
            lblWarning.Visible = false;

            if (txtUrl.Text.Trim().Equals(""))
            {
                lblWarning.Text = "ERROR: Web Site URL cannot be blank. Please enter valid web site URL";
                lblWarning.Visible = true;
            }
            else if (txtUserName.Text.Trim().Equals(""))
            {
                lblWarning.Text = "ERROR: User name cannot be blank. Please enter valid user name";
                lblWarning.Visible = true;
            }
            else if (txtPassword.Text.Trim().Equals(""))
            {
                lblWarning.Text = "ERROR: Password cannot be blank. Please enter valid password";
                lblWarning.Visible = true;
            }

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
                    customser.Credentials = new NetworkCredential(txtUserName.Text, txtPassword.Text, txtDomain.Text);
                    String val = customser.CheckConnectivity();
                    if (!val.ToLower().Equals("success"))
                    {
                        lblWarning.Text = "ERROR: Unable to connect to site discovery Web Service";
                        lblWarning.Visible = true;
                    }
                }
                catch (Exception)
                {
                    lblWarning.Text = "ERROR: Unable to connect to site discovery Web Service";
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
                        bulkWs.Credentials = new NetworkCredential(txtUserName.Text, txtPassword.Text, txtDomain.Text);
                        //bulkWs.authorize(new GSPBulkWS.AuthData(), "");
                        String val = bulkWs.CheckConnectivity();
                        if (!val.ToLower().Equals("success"))
                        {
                            lblWarning.Text = "ERROR: Unable to connect to Bulk Authorization Web Service";
                            lblWarning.Visible = true;
                        }
                    }
                    catch (Exception)
                    {
                        lblWarning.Text = "ERROR: Unable to connect to Bulk Authorization Web Service";
                        lblWarning.Visible = true;

                    }
                }


                //status and report
                if (lblWarning.Visible == true)
                {
                    lblGSPBulkWSStatus.Text = "Fail";
                    lblGSPBulkWSStatus.ForeColor = Color.Red;
                }
                else
                {
                    lblGSPBulkWSStatus.Text = "Success";
                    lblGSPBulkWSStatus.ForeColor = Color.Green;
                }

                if (lblWarning.Visible == true)
                {
                    lblGSPCustomWSStatus.Text = "Fail";
                    lblGSPCustomWSStatus.ForeColor = Color.Red;
                }
                else
                {
                    lblGSPCustomWSStatus.Text = "Success";
                    lblGSPCustomWSStatus.ForeColor = Color.Green;
                }
            }

            this.Text = "Verify Installation";//restore the original title
        }

        //skip button
        private void button1_Click(object sender, EventArgs e)
        {
            this.Close();//close the form
        }

    }
}