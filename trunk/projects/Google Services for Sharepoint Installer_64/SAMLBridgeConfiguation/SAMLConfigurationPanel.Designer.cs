namespace SAMLConfiguration
{
    partial class frmSAMLConfiguration
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            this.btnSave = new System.Windows.Forms.Button();
            this.lblArtifactConsumer = new System.Windows.Forms.Label();
            this.tbArtifactConsumerURL = new System.Windows.Forms.TextBox();
            this.tbSAMLProvider = new System.Windows.Forms.TextBox();
            this.lblProvider = new System.Windows.Forms.Label();
            this.cbSetLogLevel = new System.Windows.Forms.CheckBox();
            this.btnCancel = new System.Windows.Forms.Button();
            this.toolTip1 = new System.Windows.Forms.ToolTip(this.components);
            this.lblLogLevel = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // btnSave
            // 
            this.btnSave.Location = new System.Drawing.Point(268, 71);
            this.btnSave.Name = "btnSave";
            this.btnSave.Size = new System.Drawing.Size(75, 23);
            this.btnSave.TabIndex = 0;
            this.btnSave.Text = "&Save";
            this.toolTip1.SetToolTip(this.btnSave, "Save Search Appliance settings");
            this.btnSave.UseVisualStyleBackColor = true;
            this.btnSave.Click += new System.EventHandler(this.btnSave_Click);
            // 
            // lblArtifactConsumer
            // 
            this.lblArtifactConsumer.AutoSize = true;
            this.lblArtifactConsumer.Location = new System.Drawing.Point(11, 10);
            this.lblArtifactConsumer.Name = "lblArtifactConsumer";
            this.lblArtifactConsumer.Size = new System.Drawing.Size(90, 13);
            this.lblArtifactConsumer.TabIndex = 3;
            this.lblArtifactConsumer.Text = "Artifact Consumer";
            this.toolTip1.SetToolTip(this.lblArtifactConsumer, "SAML Bridge artifact consumer URL");
            // 
            // tbArtifactConsumerURL
            // 
            this.tbArtifactConsumerURL.Location = new System.Drawing.Point(107, 10);
            this.tbArtifactConsumerURL.Name = "tbArtifactConsumerURL";
            this.tbArtifactConsumerURL.Size = new System.Drawing.Size(324, 20);
            this.tbArtifactConsumerURL.TabIndex = 4;
            this.tbArtifactConsumerURL.Text = "https://gsa_host/SamlArtifactConsumer";
            this.toolTip1.SetToolTip(this.tbArtifactConsumerURL, "Enter the SAML Bridge artifact consumer URL");
            this.tbArtifactConsumerURL.TextChanged += new System.EventHandler(this.tbGSALocation_TextChanged);
            // 
            // tbSAMLProvider
            // 
            this.tbSAMLProvider.Location = new System.Drawing.Point(106, 45);
            this.tbSAMLProvider.Name = "tbSAMLProvider";
            this.tbSAMLProvider.Size = new System.Drawing.Size(325, 20);
            this.tbSAMLProvider.TabIndex = 10;
            this.tbSAMLProvider.Text = "SAMLServices.Wia.AuthImpl";
            this.toolTip1.SetToolTip(this.tbSAMLProvider, "Enter the collection name. For entering multiple collections use pipe (|) as sepa" +
                    "rator. E.g. colln1|colln2");
            this.tbSAMLProvider.TextChanged += new System.EventHandler(this.tbSAMLProvider_TextChanged);
            // 
            // lblProvider
            // 
            this.lblProvider.AutoSize = true;
            this.lblProvider.Location = new System.Drawing.Point(12, 42);
            this.lblProvider.Name = "lblProvider";
            this.lblProvider.Size = new System.Drawing.Size(46, 13);
            this.lblProvider.TabIndex = 9;
            this.lblProvider.Text = "Provider";
            // 
            // cbSetLogLevel
            // 
            this.cbSetLogLevel.AutoSize = true;
            this.cbSetLogLevel.Location = new System.Drawing.Point(106, 69);
            this.cbSetLogLevel.Name = "cbSetLogLevel";
            this.cbSetLogLevel.Size = new System.Drawing.Size(65, 17);
            this.cbSetLogLevel.TabIndex = 0;
            this.cbSetLogLevel.Text = "Verbose";
            this.cbSetLogLevel.UseVisualStyleBackColor = true;
            // 
            // btnCancel
            // 
            this.btnCancel.Location = new System.Drawing.Point(355, 70);
            this.btnCancel.Name = "btnCancel";
            this.btnCancel.Size = new System.Drawing.Size(75, 23);
            this.btnCancel.TabIndex = 29;
            this.btnCancel.Text = "&Cancel";
            this.toolTip1.SetToolTip(this.btnCancel, "Do not save Search Appliance settings");
            this.btnCancel.UseVisualStyleBackColor = true;
            this.btnCancel.Click += new System.EventHandler(this.btnCancel_Click);
            // 
            // lblLogLevel
            // 
            this.lblLogLevel.AutoSize = true;
            this.lblLogLevel.Location = new System.Drawing.Point(11, 67);
            this.lblLogLevel.Name = "lblLogLevel";
            this.lblLogLevel.Size = new System.Drawing.Size(54, 13);
            this.lblLogLevel.TabIndex = 32;
            this.lblLogLevel.Text = "Log Level";
            // 
            // frmSAMLConfiguration
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(441, 106);
            this.Controls.Add(this.cbSetLogLevel);
            this.Controls.Add(this.lblLogLevel);
            this.Controls.Add(this.btnCancel);
            this.Controls.Add(this.tbSAMLProvider);
            this.Controls.Add(this.lblProvider);
            this.Controls.Add(this.tbArtifactConsumerURL);
            this.Controls.Add(this.lblArtifactConsumer);
            this.Controls.Add(this.btnSave);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "frmSAMLConfiguration";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
            this.Text = "SAML Configuration Wizard";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button btnSave;
        private System.Windows.Forms.Label lblArtifactConsumer;
        private System.Windows.Forms.TextBox tbArtifactConsumerURL;
        private System.Windows.Forms.TextBox tbSAMLProvider;
        private System.Windows.Forms.Label lblProvider;
        private System.Windows.Forms.CheckBox cbSetLogLevel;
        private System.Windows.Forms.Button btnCancel;
        private System.Windows.Forms.ToolTip toolTip1;
        private System.Windows.Forms.Label lblLogLevel;
    }
}

