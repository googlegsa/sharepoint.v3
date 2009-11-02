// Copyright (C) 2007 Google Inc.
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

namespace GSBControlPanel
{
    static class GSBMain
    {
        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main(String[] args)
        {
            //const string DEFAULT_PATH = "c:";
            //const string DEFAULT_PATH = args[0];
            //MessageBox.Show(args[0]);
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);

            frmWebApplicationList webAppForm = null;
            if ((args != null) && (args.Length > 0))
            {
                //MessageBox.Show("Got path", args[0]);
                webAppForm = new frmWebApplicationList(args[0], true);
            }
            else 
            {
                webAppForm = new frmWebApplicationList("c:", false);
            }


            
            //webAppForm.isInstaller = false;
            Application.Run(webAppForm);
        }
    }
}