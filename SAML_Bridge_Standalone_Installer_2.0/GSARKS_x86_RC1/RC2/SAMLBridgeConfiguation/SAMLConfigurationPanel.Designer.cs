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
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmSAMLConfiguration));
            this.btnSave = new System.Windows.Forms.Button();
            this.lblArtifactConsumer = new System.Windows.Forms.Label();
            this.tbArtifactConsumerURL = new System.Windows.Forms.TextBox();
            this.cbSetLogLevel = new System.Windows.Forms.CheckBox();
            this.btnCancel = new System.Windows.Forms.Button();
            this.toolTip1 = new System.Windows.Forms.ToolTip(this.components);
            this.lblLogLevel = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // btnSave
            // 
            this.btnSave.Location = new System.Drawing.Point(300, 48);
            this.btnSave.Name = "btnSave";
            this.btnSave.Size = new System.Drawing.Size(75, 23);
            this.btnSave.TabIndex = 3;
            this.btnSave.Text = "&Save";
            this.toolTip1.SetToolTip(this.btnSave, "Save Search Appliance settings");
            this.btnSave.UseVisualStyleBackColor = true;
            this.btnSave.Click += new System.EventHandler(this.btnSave_Click);
            // 
            // lblArtifactConsumer
            // 
            this.lblArtifactConsumer.AutoSize = true;
            this.lblArtifactConsumer.Location = new System.Drawing.Point(9, 13);
            this.lblArtifactConsumer.Name = "lblArtifactConsumer";
            this.lblArtifactConsumer.Size = new System.Drawing.Size(90, 13);
            this.lblArtifactConsumer.TabIndex = 0;
            this.lblArtifactConsumer.Text = "Artifact Consumer";
            this.toolTip1.SetToolTip(this.lblArtifactConsumer, "SAML Bridge artifact consumer URL");
            // 
            // tbArtifactConsumerURL
            // 
            this.tbArtifactConsumerURL.Location = new System.Drawing.Point(105, 13);
            this.tbArtifactConsumerURL.Name = "tbArtifactConsumerURL";
            this.tbArtifactConsumerURL.Size = new System.Drawing.Size(369, 20);
            this.tbArtifactConsumerURL.TabIndex = 1;
            this.tbArtifactConsumerURL.Text = "https://gsa-host/security-manager/samlassertionconsumer";
            this.toolTip1.SetToolTip(this.tbArtifactConsumerURL, "Enter the SAML Bridge artifact consumer URL");
            this.tbArtifactConsumerURL.TextChanged += new System.EventHandler(this.tbGSALocation_TextChanged);
            // 
            // cbSetLogLevel
            // 
            this.cbSetLogLevel.AutoSize = true;
            this.cbSetLogLevel.Location = new System.Drawing.Point(105, 47);
            this.cbSetLogLevel.Name = "cbSetLogLevel";
            this.cbSetLogLevel.Size = new System.Drawing.Size(114, 17);
            this.cbSetLogLevel.TabIndex = 2;
            this.cbSetLogLevel.Text = "Enable debug logs";
            this.cbSetLogLevel.UseVisualStyleBackColor = true;
            // 
            // btnCancel
            // 
            this.btnCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.btnCancel.Location = new System.Drawing.Point(400, 48);
            this.btnCancel.Name = "btnCancel";
            this.btnCancel.Size = new System.Drawing.Size(75, 23);
            this.btnCancel.TabIndex = 4;
            this.btnCancel.Text = "&Cancel";
            this.toolTip1.SetToolTip(this.btnCancel, "Do not save Search Appliance settings");
            this.btnCancel.UseVisualStyleBackColor = true;
            this.btnCancel.Click += new System.EventHandler(this.btnCancel_Click);
            // 
            // lblLogLevel
            // 
            this.lblLogLevel.AutoSize = true;
            this.lblLogLevel.Location = new System.Drawing.Point(10, 48);
            this.lblLogLevel.Name = "lblLogLevel";
            this.lblLogLevel.Size = new System.Drawing.Size(54, 13);
            this.lblLogLevel.TabIndex = 0;
            this.lblLogLevel.Text = "Log Level";
            // 
            // frmSAMLConfiguration
            // 
            this.AcceptButton = this.btnSave;
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.CancelButton = this.btnCancel;
            this.ClientSize = new System.Drawing.Size(484, 89);
            this.Controls.Add(this.cbSetLogLevel);
            this.Controls.Add(this.lblLogLevel);
            this.Controls.Add(this.btnCancel);
            this.Controls.Add(this.tbArtifactConsumerURL);
            this.Controls.Add(this.lblArtifactConsumer);
            this.Controls.Add(this.btnSave);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "frmSAMLConfiguration";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Text = "Google SAML Bridge for Windows 2.0 - Configuration Wizard";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button btnSave;
        private System.Windows.Forms.Label lblArtifactConsumer;
        private System.Windows.Forms.TextBox tbArtifactConsumerURL;
        private System.Windows.Forms.CheckBox cbSetLogLevel;
        private System.Windows.Forms.Button btnCancel;
        private System.Windows.Forms.ToolTip toolTip1;
        private System.Windows.Forms.Label lblLogLevel;
    }
}

