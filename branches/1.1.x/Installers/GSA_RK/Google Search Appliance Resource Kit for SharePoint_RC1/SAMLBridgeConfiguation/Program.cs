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
