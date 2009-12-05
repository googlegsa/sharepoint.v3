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
using System.Windows.Forms;
using System.IO;
using System.Diagnostics;

namespace GSBControlPanel
{
    static class GSBMain
    {
        public const String SEARCHCONTROL = "TEMPLATE\\CONTROLTEMPLATES\\SearchArea.ascx";
        public const String SEARCHRESULTS = "TEMPLATE\\LAYOUTS\\searchresults.aspx";
        public const String SEARCHCONTROL_BACKUP = "TEMPLATE\\CONTROLTEMPLATES\\SearchArea.ascx.backup";
        public const String SEARCHRESULTS_BACKUP = "TEMPLATE\\LAYOUTS\\searchresults.aspx.backup";
        public const String CURRENT_SEARCHRESULTS = "TEMPLATE\\searchresults.aspx";
        public const String CURRENT_SEARCHCONTROL = "TEMPLATE\\SearchArea.ascx";
        public const String TEMPLATE = "TEMPLATE";

        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main(String[] args)
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            frmWebApplicationList webAppForm = null;

            //case of Installer
            if ((args != null) && (args.Length > 0))
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
                    InstallFeature(myBasePath);//Install and Activate Feature
                }
                catch (Exception)
                {
                    //do nothing...as can't do much when running from installer
                }

                //DO FILE OPERATIONS...... 
                /* *************************************************************
                 * 1. Backup the older file(s)
                 * 2. Copy the new search file(s) to the respective location
                 * *************************************************************/
                try
                {
                    ///////////////////////////STEPS FOR SEARCHCONTROL//////////////////////////////////////
                    if (File.Exists(myBasePath + SEARCHCONTROL))
                    {
                        File.Move(myBasePath+SEARCHCONTROL,myBasePath+SEARCHCONTROL_BACKUP); //backup
                    }
                    if (File.Exists(myBasePath + CURRENT_SEARCHCONTROL))
                    {
                        File.Copy(myBasePath + CURRENT_SEARCHCONTROL, myBasePath + SEARCHCONTROL);//new search control
                    }

                    ///////////////////////////Steps for SearchBox//////////////////////////////////////
                    if (File.Exists(myBasePath + SEARCHRESULTS))
                    {
                        File.Move(myBasePath + SEARCHRESULTS, myBasePath + SEARCHRESULTS_BACKUP); //backup
                    }
                    if (File.Exists(myBasePath + CURRENT_SEARCHRESULTS))
                    {
                        File.Copy(myBasePath + CURRENT_SEARCHRESULTS, myBasePath + SEARCHRESULTS);//new search control
                    }
                }
                catch (Exception){
                    //do nothing ... as can't log exception while installation
                }

                webAppForm = new frmWebApplicationList(myBasePath /*+ TEMPLATE*/, true);//set the search box deployment location
            }

            else //case of Shortcut
            {
                webAppForm = new frmWebApplicationList("c:", false);
            }
            Application.Run(webAppForm);
            //webAppForm.ShowDialog();
        }


        /// <summary>
        /// Installs and activates the search control feature
        /// </summary>
        public static void InstallFeature(string _sharepointInstallPath)
        {
            //Ref: http://social.msdn.microsoft.com/Forums/en-US/sharepointdevelopment/thread/faa60753-32a6-404e-9e3f-9220b03b79fa/
            string processName = _sharepointInstallPath + @"Bin\stsadm.exe";
            ExecuteProcessCommand(processName, @" -o installfeature -name GSAFeature -force", _sharepointInstallPath + @"\Bin");
            ExecuteProcessCommand(processName, @" -o activatefeature -name GSAFeature -force", _sharepointInstallPath + @"\Bin");
        }

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
    }//end: class
}//end: namespace