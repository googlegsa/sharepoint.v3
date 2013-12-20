namespace GSBControlPanel
{
    partial class frmGSAParams
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
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmGSAParams));
            this.btnOk = new System.Windows.Forms.Button();
            this.lblGSALocation = new System.Windows.Forms.Label();
            this.tbGSALocation = new System.Windows.Forms.TextBox();
            this.tbSiteCollection = new System.Windows.Forms.TextBox();
            this.lblSiteCollection = new System.Windows.Forms.Label();
            this.tbFrontEnd = new System.Windows.Forms.TextBox();
            this.lblFrontEnd = new System.Windows.Forms.Label();
            this.cbEnableLogging = new System.Windows.Forms.CheckBox();
            this.btnCancel = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.toolTip1 = new System.Windows.Forms.ToolTip(this.components);
            this.rbGSAFrontEnd = new System.Windows.Forms.RadioButton();
            this.rbCustomStylesheet = new System.Windows.Forms.RadioButton();
            this.rbPublicAndSecure = new System.Windows.Forms.RadioButton();
            this.rbPublic = new System.Windows.Forms.RadioButton();
            this.rbPublicAndSecureDefaultSearchType = new System.Windows.Forms.RadioButton();
            this.rbPublicDefaultSearchType = new System.Windows.Forms.RadioButton();
            this.label2 = new System.Windows.Forms.Label();
            this.gbSelectFrontEnd = new System.Windows.Forms.GroupBox();
            this.lblStylesheet = new System.Windows.Forms.Label();
            this.lblCheckConnectivityStatus = new System.Windows.Forms.Label();
            this.lblCause = new System.Windows.Forms.Label();
            this.lblServeMethod = new System.Windows.Forms.Label();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.lblDefaultSearchType = new System.Windows.Forms.Label();
            this.groupBox2 = new System.Windows.Forms.GroupBox();
            this.groupBox3 = new System.Windows.Forms.GroupBox();
            this.chkUseSAMLPost = new System.Windows.Forms.CheckBox();
            this.txtArtifactConsumerURL = new System.Windows.Forms.TextBox();
            this.lblArtifactConsumer = new System.Windows.Forms.Label();
            this.lblCertName = new System.Windows.Forms.Label();
            this.txtCertName = new System.Windows.Forms.TextBox();
            this.gbSelectFrontEnd.SuspendLayout();
            this.groupBox1.SuspendLayout();
            this.groupBox2.SuspendLayout();
            this.groupBox3.SuspendLayout();
            this.SuspendLayout();
            // 
            // btnOk
            // 
            this.btnOk.Location = new System.Drawing.Point(311, 413);
            this.btnOk.Name = "btnOk";
            this.btnOk.Size = new System.Drawing.Size(71, 23);
            this.btnOk.TabIndex = 7;
            this.btnOk.Text = "&Save";
            this.toolTip1.SetToolTip(this.btnOk, "Save Search Appliance settings");
            this.btnOk.UseVisualStyleBackColor = true;
            this.btnOk.Click += new System.EventHandler(this.btnOk_Click);
            // 
            // lblGSALocation
            // 
            this.lblGSALocation.AutoSize = true;
            this.lblGSALocation.Location = new System.Drawing.Point(8, 45);
            this.lblGSALocation.Name = "lblGSALocation";
            this.lblGSALocation.Size = new System.Drawing.Size(79, 13);
            this.lblGSALocation.TabIndex = 0;
            this.lblGSALocation.Text = "Appliance URL";
            // 
            // tbGSALocation
            // 
            this.tbGSALocation.Location = new System.Drawing.Point(115, 43);
            this.tbGSALocation.Name = "tbGSALocation";
            this.tbGSALocation.Size = new System.Drawing.Size(376, 20);
            this.tbGSALocation.TabIndex = 0;
            this.tbGSALocation.Text = "https://your_gsa_url";
            this.toolTip1.SetToolTip(this.tbGSALocation, "Enter the Google Search appliance URL. ");
            this.tbGSALocation.TextChanged += new System.EventHandler(this.tbGSALocation_TextChanged);
            // 
            // tbSiteCollection
            // 
            this.tbSiteCollection.Location = new System.Drawing.Point(114, 77);
            this.tbSiteCollection.Name = "tbSiteCollection";
            this.tbSiteCollection.Size = new System.Drawing.Size(377, 20);
            this.tbSiteCollection.TabIndex = 1;
            this.tbSiteCollection.Text = "default_collection";
            this.toolTip1.SetToolTip(this.tbSiteCollection, "Enter the GSA collection name. For entering multiple collections use pipe (|) as " +
                    "separator. E.g. colln1|colln2");
            this.tbSiteCollection.TextChanged += new System.EventHandler(this.tbSiteCollection_TextChanged);
            // 
            // lblSiteCollection
            // 
            this.lblSiteCollection.AutoSize = true;
            this.lblSiteCollection.Location = new System.Drawing.Point(8, 80);
            this.lblSiteCollection.Name = "lblSiteCollection";
            this.lblSiteCollection.Size = new System.Drawing.Size(53, 13);
            this.lblSiteCollection.TabIndex = 0;
            this.lblSiteCollection.Text = "Collection";
            // 
            // tbFrontEnd
            // 
            this.tbFrontEnd.Location = new System.Drawing.Point(115, 152);
            this.tbFrontEnd.Name = "tbFrontEnd";
            this.tbFrontEnd.Size = new System.Drawing.Size(377, 20);
            this.tbFrontEnd.TabIndex = 2;
            this.tbFrontEnd.Tag = "";
            this.tbFrontEnd.Text = "default_frontend";
            this.toolTip1.SetToolTip(this.tbFrontEnd, "Enter Front End name from the appliance");
            this.tbFrontEnd.TextChanged += new System.EventHandler(this.tbFrontEnd_TextChanged);
            // 
            // lblFrontEnd
            // 
            this.lblFrontEnd.AutoSize = true;
            this.lblFrontEnd.Location = new System.Drawing.Point(8, 155);
            this.lblFrontEnd.Name = "lblFrontEnd";
            this.lblFrontEnd.Size = new System.Drawing.Size(53, 13);
            this.lblFrontEnd.TabIndex = 0;
            this.lblFrontEnd.Text = "Front End";
            // 
            // cbEnableLogging
            // 
            this.cbEnableLogging.AutoSize = true;
            this.cbEnableLogging.Location = new System.Drawing.Point(122, 408);
            this.cbEnableLogging.Name = "cbEnableLogging";
            this.cbEnableLogging.Size = new System.Drawing.Size(65, 17);
            this.cbEnableLogging.TabIndex = 6;
            this.cbEnableLogging.Text = "Verbose";
            this.toolTip1.SetToolTip(this.cbEnableLogging, "Select to enable Information level logging for the Search Box");
            this.cbEnableLogging.UseVisualStyleBackColor = true;
            // 
            // btnCancel
            // 
            this.btnCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.btnCancel.Location = new System.Drawing.Point(398, 412);
            this.btnCancel.Name = "btnCancel";
            this.btnCancel.Size = new System.Drawing.Size(71, 23);
            this.btnCancel.TabIndex = 8;
            this.btnCancel.Text = "&Cancel";
            this.toolTip1.SetToolTip(this.btnCancel, "Do not save Search Appliance settings");
            this.btnCancel.UseVisualStyleBackColor = true;
            this.btnCancel.Click += new System.EventHandler(this.btnCancel_Click);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(5, 6);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(167, 13);
            this.label1.TabIndex = 0;
            this.label1.Text = "Google Search Appliance settings";
            // 
            // rbGSAFrontEnd
            // 
            this.rbGSAFrontEnd.AutoSize = true;
            this.rbGSAFrontEnd.Location = new System.Drawing.Point(179, 14);
            this.rbGSAFrontEnd.Name = "rbGSAFrontEnd";
            this.rbGSAFrontEnd.Size = new System.Drawing.Size(187, 17);
            this.rbGSAFrontEnd.TabIndex = 1;
            this.rbGSAFrontEnd.Text = "Use Search Appliance\'s Front End";
            this.toolTip1.SetToolTip(this.rbGSAFrontEnd, "Select to use Appliance\'s Frontend for rendering the search result");
            this.rbGSAFrontEnd.UseVisualStyleBackColor = true;
            this.rbGSAFrontEnd.CheckedChanged += new System.EventHandler(this.rbGSAFrontEnd_CheckedChanged);
            // 
            // rbCustomStylesheet
            // 
            this.rbCustomStylesheet.AutoSize = true;
            this.rbCustomStylesheet.Checked = true;
            this.rbCustomStylesheet.Location = new System.Drawing.Point(3, 14);
            this.rbCustomStylesheet.Name = "rbCustomStylesheet";
            this.rbCustomStylesheet.Size = new System.Drawing.Size(119, 17);
            this.rbCustomStylesheet.TabIndex = 0;
            this.rbCustomStylesheet.TabStop = true;
            this.rbCustomStylesheet.Text = "Use local stylesheet";
            this.toolTip1.SetToolTip(this.rbCustomStylesheet, "Select to use local stylesheet deployed on your machine. ");
            this.rbCustomStylesheet.UseVisualStyleBackColor = true;
            this.rbCustomStylesheet.CheckedChanged += new System.EventHandler(this.rbCustomStylesheet_CheckedChanged);
            // 
            // rbPublicAndSecure
            // 
            this.rbPublicAndSecure.AutoSize = true;
            this.rbPublicAndSecure.Checked = true;
            this.rbPublicAndSecure.Location = new System.Drawing.Point(178, 13);
            this.rbPublicAndSecure.Name = "rbPublicAndSecure";
            this.rbPublicAndSecure.Size = new System.Drawing.Size(112, 17);
            this.rbPublicAndSecure.TabIndex = 1;
            this.rbPublicAndSecure.TabStop = true;
            this.rbPublicAndSecure.Text = "Public and Secure";
            this.toolTip1.SetToolTip(this.rbPublicAndSecure, "Select to configure Search Box to serve  public as well as secured search results" +
                    ".");
            this.rbPublicAndSecure.UseVisualStyleBackColor = true;
            this.rbPublicAndSecure.CheckedChanged += new System.EventHandler(this.rbPublicAndSecure_CheckedChanged);
            // 
            // rbPublic
            // 
            this.rbPublic.AutoSize = true;
            this.rbPublic.Location = new System.Drawing.Point(6, 13);
            this.rbPublic.Name = "rbPublic";
            this.rbPublic.Size = new System.Drawing.Size(54, 17);
            this.rbPublic.TabIndex = 0;
            this.rbPublic.Text = "Public";
            this.toolTip1.SetToolTip(this.rbPublic, "Select to configure Search Box to serve public only search results.");
            this.rbPublic.UseVisualStyleBackColor = true;
            this.rbPublic.CheckedChanged += new System.EventHandler(this.rbPublic_CheckedChanged);
            // 
            // rbPublicAndSecureDefaultSearchType
            // 
            this.rbPublicAndSecureDefaultSearchType.AutoSize = true;
            this.rbPublicAndSecureDefaultSearchType.Checked = true;
            this.rbPublicAndSecureDefaultSearchType.Location = new System.Drawing.Point(178, 13);
            this.rbPublicAndSecureDefaultSearchType.Name = "rbPublicAndSecureDefaultSearchType";
            this.rbPublicAndSecureDefaultSearchType.Size = new System.Drawing.Size(149, 17);
            this.rbPublicAndSecureDefaultSearchType.TabIndex = 1;
            this.rbPublicAndSecureDefaultSearchType.TabStop = true;
            this.rbPublicAndSecureDefaultSearchType.Text = "Public and Secure Search";
            this.toolTip1.SetToolTip(this.rbPublicAndSecureDefaultSearchType, "Select to configure Public and Secure Search as default search type.");
            this.rbPublicAndSecureDefaultSearchType.UseVisualStyleBackColor = true;
            // 
            // rbPublicDefaultSearchType
            // 
            this.rbPublicDefaultSearchType.AutoSize = true;
            this.rbPublicDefaultSearchType.Location = new System.Drawing.Point(6, 13);
            this.rbPublicDefaultSearchType.Name = "rbPublicDefaultSearchType";
            this.rbPublicDefaultSearchType.Size = new System.Drawing.Size(91, 17);
            this.rbPublicDefaultSearchType.TabIndex = 0;
            this.rbPublicDefaultSearchType.Text = "Public Search";
            this.toolTip1.SetToolTip(this.rbPublicDefaultSearchType, "Select to configure Public Search as default search type.");
            this.rbPublicDefaultSearchType.UseVisualStyleBackColor = true;
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(10, 412);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(56, 13);
            this.label2.TabIndex = 0;
            this.label2.Text = "Event Log";
            // 
            // gbSelectFrontEnd
            // 
            this.gbSelectFrontEnd.Controls.Add(this.rbGSAFrontEnd);
            this.gbSelectFrontEnd.Controls.Add(this.rbCustomStylesheet);
            this.gbSelectFrontEnd.Location = new System.Drawing.Point(115, 103);
            this.gbSelectFrontEnd.Name = "gbSelectFrontEnd";
            this.gbSelectFrontEnd.Size = new System.Drawing.Size(377, 41);
            this.gbSelectFrontEnd.TabIndex = 3;
            this.gbSelectFrontEnd.TabStop = false;
            // 
            // lblStylesheet
            // 
            this.lblStylesheet.AutoSize = true;
            this.lblStylesheet.Location = new System.Drawing.Point(8, 121);
            this.lblStylesheet.Name = "lblStylesheet";
            this.lblStylesheet.Size = new System.Drawing.Size(56, 13);
            this.lblStylesheet.TabIndex = 0;
            this.lblStylesheet.Text = "Stylesheet";
            // 
            // lblCheckConnectivityStatus
            // 
            this.lblCheckConnectivityStatus.AutoSize = true;
            this.lblCheckConnectivityStatus.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblCheckConnectivityStatus.ForeColor = System.Drawing.Color.DarkRed;
            this.lblCheckConnectivityStatus.Location = new System.Drawing.Point(168, 7);
            this.lblCheckConnectivityStatus.Name = "lblCheckConnectivityStatus";
            this.lblCheckConnectivityStatus.Size = new System.Drawing.Size(194, 13);
            this.lblCheckConnectivityStatus.TabIndex = 0;
            this.lblCheckConnectivityStatus.Text = "Unable to connect to Search Appliance";
            this.lblCheckConnectivityStatus.Visible = false;
            // 
            // lblCause
            // 
            this.lblCause.AutoSize = true;
            this.lblCause.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblCause.ForeColor = System.Drawing.Color.DarkRed;
            this.lblCause.Location = new System.Drawing.Point(6, 23);
            this.lblCause.Name = "lblCause";
            this.lblCause.Size = new System.Drawing.Size(0, 13);
            this.lblCause.TabIndex = 36;
            // 
            // lblServeMethod
            // 
            this.lblServeMethod.AutoSize = true;
            this.lblServeMethod.Location = new System.Drawing.Point(8, 198);
            this.lblServeMethod.Name = "lblServeMethod";
            this.lblServeMethod.Size = new System.Drawing.Size(74, 13);
            this.lblServeMethod.TabIndex = 41;
            this.lblServeMethod.Text = "Serve Method";
            // 
            // groupBox1
            // 
            this.groupBox1.Controls.Add(this.rbPublicAndSecure);
            this.groupBox1.Controls.Add(this.rbPublic);
            this.groupBox1.Location = new System.Drawing.Point(114, 183);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(377, 41);
            this.groupBox1.TabIndex = 4;
            this.groupBox1.TabStop = false;
            // 
            // lblDefaultSearchType
            // 
            this.lblDefaultSearchType.AutoSize = true;
            this.lblDefaultSearchType.Location = new System.Drawing.Point(8, 243);
            this.lblDefaultSearchType.Name = "lblDefaultSearchType";
            this.lblDefaultSearchType.Size = new System.Drawing.Size(105, 13);
            this.lblDefaultSearchType.TabIndex = 43;
            this.lblDefaultSearchType.Text = "Default Search Type";
            // 
            // groupBox2
            // 
            this.groupBox2.Controls.Add(this.rbPublicAndSecureDefaultSearchType);
            this.groupBox2.Controls.Add(this.rbPublicDefaultSearchType);
            this.groupBox2.Location = new System.Drawing.Point(114, 230);
            this.groupBox2.Name = "groupBox2";
            this.groupBox2.Size = new System.Drawing.Size(377, 41);
            this.groupBox2.TabIndex = 5;
            this.groupBox2.TabStop = false;
            // 
            // groupBox3
            // 
            this.groupBox3.Controls.Add(this.txtCertName);
            this.groupBox3.Controls.Add(this.lblCertName);
            this.groupBox3.Controls.Add(this.txtArtifactConsumerURL);
            this.groupBox3.Controls.Add(this.lblArtifactConsumer);
            this.groupBox3.Controls.Add(this.chkUseSAMLPost);
            this.groupBox3.Location = new System.Drawing.Point(8, 277);
            this.groupBox3.Name = "groupBox3";
            this.groupBox3.Size = new System.Drawing.Size(484, 125);
            this.groupBox3.TabIndex = 44;
            this.groupBox3.TabStop = false;
            this.groupBox3.Text = "SAML Bridge Post Configuration";
            // 
            // chkUseSAMLPost
            // 
            this.chkUseSAMLPost.AutoSize = true;
            this.chkUseSAMLPost.Location = new System.Drawing.Point(13, 26);
            this.chkUseSAMLPost.Name = "chkUseSAMLPost";
            this.chkUseSAMLPost.Size = new System.Drawing.Size(101, 17);
            this.chkUseSAMLPost.TabIndex = 0;
            this.chkUseSAMLPost.Text = "Use SAML Post";
            this.chkUseSAMLPost.UseVisualStyleBackColor = true;
            this.chkUseSAMLPost.CheckedChanged += new System.EventHandler(this.chkUseSAMLPost_CheckedChanged);
            // 
            // txtArtifactConsumerURL
            // 
            this.txtArtifactConsumerURL.Location = new System.Drawing.Point(112, 49);
            this.txtArtifactConsumerURL.Name = "txtArtifactConsumerURL";
            this.txtArtifactConsumerURL.Size = new System.Drawing.Size(369, 20);
            this.txtArtifactConsumerURL.TabIndex = 3;
            this.txtArtifactConsumerURL.Text = "https://yourgsa/security-manager/samlassertionconsumer";
            this.toolTip1.SetToolTip(this.txtArtifactConsumerURL, "Enter the SAML Bridge artifact consumer URL");
            // 
            // lblArtifactConsumer
            // 
            this.lblArtifactConsumer.Location = new System.Drawing.Point(10, 52);
            this.lblArtifactConsumer.Name = "lblArtifactConsumer";
            this.lblArtifactConsumer.Size = new System.Drawing.Size(105, 17);
            this.lblArtifactConsumer.TabIndex = 2;
            this.lblArtifactConsumer.Text = "Assertion Consumer";
            this.toolTip1.SetToolTip(this.lblArtifactConsumer, "SAML Bridge artifact consumer URL");
            // 
            // lblCertName
            // 
            this.lblCertName.Location = new System.Drawing.Point(10, 86);
            this.lblCertName.Name = "lblCertName";
            this.lblCertName.Size = new System.Drawing.Size(131, 19);
            this.lblCertName.TabIndex = 4;
            this.lblCertName.Text = "Certificate Friendly Name";
            // 
            // txtCertName
            // 
            this.txtCertName.Location = new System.Drawing.Point(136, 84);
            this.txtCertName.Name = "txtCertName";
            this.txtCertName.Size = new System.Drawing.Size(193, 20);
            this.txtCertName.TabIndex = 5;
            // 
            // frmGSAParams
            // 
            this.AcceptButton = this.btnOk;
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.CancelButton = this.btnCancel;
            this.ClientSize = new System.Drawing.Size(522, 501);
            this.Controls.Add(this.groupBox3);
            this.Controls.Add(this.lblDefaultSearchType);
            this.Controls.Add(this.groupBox2);
            this.Controls.Add(this.lblServeMethod);
            this.Controls.Add(this.groupBox1);
            this.Controls.Add(this.lblCause);
            this.Controls.Add(this.lblCheckConnectivityStatus);
            this.Controls.Add(this.lblStylesheet);
            this.Controls.Add(this.gbSelectFrontEnd);
            this.Controls.Add(this.cbEnableLogging);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.btnCancel);
            this.Controls.Add(this.tbFrontEnd);
            this.Controls.Add(this.lblFrontEnd);
            this.Controls.Add(this.tbSiteCollection);
            this.Controls.Add(this.lblSiteCollection);
            this.Controls.Add(this.tbGSALocation);
            this.Controls.Add(this.lblGSALocation);
            this.Controls.Add(this.btnOk);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "frmGSAParams";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
            this.Text = "Google Search Box for SharePoint- Configuration Wizard";
            this.gbSelectFrontEnd.ResumeLayout(false);
            this.gbSelectFrontEnd.PerformLayout();
            this.groupBox1.ResumeLayout(false);
            this.groupBox1.PerformLayout();
            this.groupBox2.ResumeLayout(false);
            this.groupBox2.PerformLayout();
            this.groupBox3.ResumeLayout(false);
            this.groupBox3.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button btnOk;
        private System.Windows.Forms.Label lblGSALocation;
        private System.Windows.Forms.TextBox tbGSALocation;
        private System.Windows.Forms.TextBox tbSiteCollection;
        private System.Windows.Forms.Label lblSiteCollection;
        private System.Windows.Forms.TextBox tbFrontEnd;
        private System.Windows.Forms.Label lblFrontEnd;
        private System.Windows.Forms.CheckBox cbEnableLogging;
        private System.Windows.Forms.Button btnCancel;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.ToolTip toolTip1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.GroupBox gbSelectFrontEnd;
        private System.Windows.Forms.RadioButton rbGSAFrontEnd;
        private System.Windows.Forms.RadioButton rbCustomStylesheet;
        private System.Windows.Forms.Label lblStylesheet;
        private System.Windows.Forms.Label lblCheckConnectivityStatus;
        private System.Windows.Forms.Label lblCause;
        private System.Windows.Forms.RadioButton rbPublicAndSecure;
        private System.Windows.Forms.Label lblServeMethod;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.RadioButton rbPublic;
        private System.Windows.Forms.RadioButton rbPublicAndSecureDefaultSearchType;
        private System.Windows.Forms.Label lblDefaultSearchType;
        private System.Windows.Forms.GroupBox groupBox2;
        private System.Windows.Forms.RadioButton rbPublicDefaultSearchType;
        private System.Windows.Forms.GroupBox groupBox3;
        private System.Windows.Forms.CheckBox chkUseSAMLPost;
        private System.Windows.Forms.TextBox txtArtifactConsumerURL;
        private System.Windows.Forms.Label lblArtifactConsumer;
        private System.Windows.Forms.Label lblCertName;
        private System.Windows.Forms.TextBox txtCertName;
    }
}

