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
using System.Windows.Forms;
using System.IO;

namespace SAMLConfiguration
{
    static class Program
    {
        public const String SAML_RELATIVE_PATH = "\\saml-bridge\\web.config";

        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main(String[] args)
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            if ((args != null) && (args.Length > 0))
            {
                Application.Run(new frmSAMLConfiguration(args[0]+SAML_RELATIVE_PATH, false));
            }
            else 
            {
                String FilePath = System.Reflection.Assembly.GetExecutingAssembly().Location;
                FileInfo f = new FileInfo(FilePath);
                if (f.Exists)
                {
                    String pwd = f.Directory.ToString();
                    Application.Run(new frmSAMLConfiguration(pwd + SAML_RELATIVE_PATH, true));
                }
            }
        }
    }
}
