using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Configuration.Install;
using System.Collections;
using System.Windows.Forms;

namespace WindowsApplication1
{
    [RunInstaller(true)]
    public partial class GSSInstaller : Installer
    {
        public GSSInstaller()
        {
            InitializeComponent();
        }
        // Override the 'Install' method.
        // The Installation will call this method to run the Custom Action
        public override void Install(IDictionary savedState)
        {
            base.Install(savedState);
            frmVerifyInstallation myForm = new frmVerifyInstallation();
            myForm.ShowDialog();
        }
    }
}