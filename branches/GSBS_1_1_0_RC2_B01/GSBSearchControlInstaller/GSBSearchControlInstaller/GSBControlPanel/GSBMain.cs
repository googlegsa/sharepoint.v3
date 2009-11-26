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
        static void Main()
        {
            const string DEFAULT_PATH = "c:";
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            frmWebApplicationList webAppForm = new frmWebApplicationList(DEFAULT_PATH,false);
            //webAppForm.isInstaller = false;
            Application.Run(webAppForm);
        }
    }
}