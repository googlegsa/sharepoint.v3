using System;
using System.Windows.Forms;
using System.IO;

namespace SAMLConfiguration
{
    static class SAMLMain
    {
        public const String SAML_RELATIVE_PATH = "\\web.config";

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
                String SAMLLocation = args[0];
                
                //Note: due to a bug in installshield, we get additional Quote(") in the path. It needs to be handled
                if (SAMLLocation.EndsWith("\""))
                {
                    SAMLLocation = SAMLLocation.Substring(0, SAMLLocation.Length - 1);
                }

                Application.Run(new frmSAMLConfiguration(SAMLLocation + SAML_RELATIVE_PATH));
            }
            else 
            {
                String FilePath = System.Reflection.Assembly.GetExecutingAssembly().Location;
                FileInfo f = new FileInfo(FilePath);
                if (f.Exists)
                {
                    String pwd = f.Directory.ToString();
                    Application.Run(new frmSAMLConfiguration(pwd + SAML_RELATIVE_PATH));
                }
            }
        }
    }
}
