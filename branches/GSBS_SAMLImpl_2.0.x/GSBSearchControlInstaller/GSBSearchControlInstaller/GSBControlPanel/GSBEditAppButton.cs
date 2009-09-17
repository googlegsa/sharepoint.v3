using System;
using System.Collections.Generic;
using System.Text;
using System.Windows.Forms;
using System.Drawing;

namespace GSBControlPanel
{
    class GSBEditAppButton : Button
    {
        private string webConfigpath = null;
        const string SLASH = "\\";
        const string WEB_CONFIG_FILE = "web.config";
        const string EDIT = "Edit";

        public GSBEditAppButton()
        {
            Visible = true;
            Enabled = true;
            Text = EDIT;
            //Size = new Size(57, 21);
            this.AutoSize = true;
            this.Anchor = AnchorStyles.None;
            this.TextAlign = ContentAlignment.MiddleCenter;
            this.Click += new System.EventHandler(this.btnEdit_Click);
        }

        public string WebConfigPath
        {
            get { return webConfigpath; }
            set
            {
                if (value != null)
                {
                    if (!value.EndsWith(SLASH))
                    {
                        webConfigpath = value + SLASH + WEB_CONFIG_FILE;
                    }
                    else
                    {
                        webConfigpath = value + WEB_CONFIG_FILE;
                    }
                }
            }
        }

        /// <summary>
        /// Handler for click of edit button.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void btnEdit_Click(object sender, EventArgs e)
        {
            //Note: when you click button it is sure that it is during edit only na dnot during installation
            frmGSAParams GSAParamForm = new frmGSAParams();
            //LoadConfigurationFromFileToForm(webConfigpath, GSAParamForm);
            GSAParamForm.LoadConfigurationFromFileToForm(WebConfigPath);
            
            DialogResult result = GSAParamForm.ShowDialog(); //show the dialog

            //if OK save it to file
            if (result == DialogResult.OK)
            {
                GSAConfiguration gc = GSAParamForm.PopulateGSAConfiguration(null);
                gc.SaveConfigurationsToFile(webConfigpath,false);//do not save the location of custom stylesheet
            }

            GSAParamForm.Dispose();//dispose off the form
        }
    }
}
