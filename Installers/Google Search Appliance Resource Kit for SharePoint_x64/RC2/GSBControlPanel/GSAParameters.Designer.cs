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
            this.label2 = new System.Windows.Forms.Label();
            this.gbSelectFrontEnd = new System.Windows.Forms.GroupBox();
            this.label3 = new System.Windows.Forms.Label();
            this.lblCheckConnectivityStatus = new System.Windows.Forms.Label();
            this.lblCause = new System.Windows.Forms.Label();
            this.rbPublicAndSecure = new System.Windows.Forms.RadioButton();
            this.lblServeMethod = new System.Windows.Forms.Label();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.rbPublic = new System.Windows.Forms.RadioButton();
            this.gbSelectFrontEnd.SuspendLayout();
            this.groupBox1.SuspendLayout();
            this.SuspendLayout();
            // 
            // btnOk
            // 
            this.btnOk.Location = new System.Drawing.Point(311, 242);
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
            this.tbGSALocation.Location = new System.Drawing.Point(93, 43);
            this.tbGSALocation.Name = "tbGSALocation";
            this.tbGSALocation.Size = new System.Drawing.Size(376, 20);
            this.tbGSALocation.TabIndex = 1;
            this.tbGSALocation.Text = "https://your_gsa_url";
            this.toolTip1.SetToolTip(this.tbGSALocation, "Enter the Google Search appliance URL. ");
            this.tbGSALocation.TextChanged += new System.EventHandler(this.tbGSALocation_TextChanged);
            // 
            // tbSiteCollection
            // 
            this.tbSiteCollection.Location = new System.Drawing.Point(92, 77);
            this.tbSiteCollection.Name = "tbSiteCollection";
            this.tbSiteCollection.Size = new System.Drawing.Size(377, 20);
            this.tbSiteCollection.TabIndex = 2;
            this.tbSiteCollection.Text = "default_collection";
            this.toolTip1.SetToolTip(this.tbSiteCollection, "Enter the collection name. For entering multiple collections use pipe (|) as sepa" +
                    "rator. E.g. colln1|colln2");
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
            this.tbFrontEnd.Location = new System.Drawing.Point(92, 110);
            this.tbFrontEnd.Name = "tbFrontEnd";
            this.tbFrontEnd.Size = new System.Drawing.Size(377, 20);
            this.tbFrontEnd.TabIndex = 3;
            this.tbFrontEnd.Tag = "";
            this.tbFrontEnd.Text = "default_frontend";
            this.toolTip1.SetToolTip(this.tbFrontEnd, "Enter Front End name from the appliance");
            this.tbFrontEnd.TextChanged += new System.EventHandler(this.tbFrontEnd_TextChanged);
            // 
            // lblFrontEnd
            // 
            this.lblFrontEnd.AutoSize = true;
            this.lblFrontEnd.Location = new System.Drawing.Point(9, 113);
            this.lblFrontEnd.Name = "lblFrontEnd";
            this.lblFrontEnd.Size = new System.Drawing.Size(53, 13);
            this.lblFrontEnd.TabIndex = 0;
            this.lblFrontEnd.Text = "Front End";
            // 
            // cbEnableLogging
            // 
            this.cbEnableLogging.AutoSize = true;
            this.cbEnableLogging.Location = new System.Drawing.Point(94, 241);
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
            this.btnCancel.Location = new System.Drawing.Point(398, 241);
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
            this.rbGSAFrontEnd.TabIndex = 65;
            this.rbGSAFrontEnd.Text = "Use Search Appliance\'s Front End";
            this.toolTip1.SetToolTip(this.rbGSAFrontEnd, "Select to use Appliance\'s Frontend for rendering the search result");
            this.rbGSAFrontEnd.UseVisualStyleBackColor = true;
            // 
            // rbCustomStylesheet
            // 
            this.rbCustomStylesheet.AutoSize = true;
            this.rbCustomStylesheet.Checked = true;
            this.rbCustomStylesheet.Location = new System.Drawing.Point(3, 14);
            this.rbCustomStylesheet.Name = "rbCustomStylesheet";
            this.rbCustomStylesheet.Size = new System.Drawing.Size(119, 17);
            this.rbCustomStylesheet.TabIndex = 4;
            this.rbCustomStylesheet.TabStop = true;
            this.rbCustomStylesheet.Text = "Use local stylesheet";
            this.toolTip1.SetToolTip(this.rbCustomStylesheet, "Select to use local stylesheet deployed on your machine. ");
            this.rbCustomStylesheet.UseVisualStyleBackColor = true;
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(10, 241);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(56, 13);
            this.label2.TabIndex = 0;
            this.label2.Text = "Event Log";
            // 
            // gbSelectFrontEnd
            // 
            this.gbSelectFrontEnd.Controls.Add(this.rbGSAFrontEnd);
            this.gbSelectFrontEnd.Controls.Add(this.rbCustomStylesheet);
            this.gbSelectFrontEnd.Location = new System.Drawing.Point(92, 136);
            this.gbSelectFrontEnd.Name = "gbSelectFrontEnd";
            this.gbSelectFrontEnd.Size = new System.Drawing.Size(377, 41);
            this.gbSelectFrontEnd.TabIndex = 33;
            this.gbSelectFrontEnd.TabStop = false;
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(7, 145);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(56, 13);
            this.label3.TabIndex = 0;
            this.label3.Text = "Stylesheet";
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
            // rbPublicAndSecure
            // 
            this.rbPublicAndSecure.AutoSize = true;
            this.rbPublicAndSecure.Location = new System.Drawing.Point(178, 13);
            this.rbPublicAndSecure.Name = "rbPublicAndSecure";
            this.rbPublicAndSecure.Size = new System.Drawing.Size(112, 17);
            this.rbPublicAndSecure.TabIndex = 65;
            this.rbPublicAndSecure.Text = "Public and Secure";
            this.toolTip1.SetToolTip(this.rbPublicAndSecure, "Select to use Appliance\'s Frontend for rendering the search result");
            this.rbPublicAndSecure.UseVisualStyleBackColor = true;
            // 
            // lblServeMethod
            // 
            this.lblServeMethod.AutoSize = true;
            this.lblServeMethod.Location = new System.Drawing.Point(7, 196);
            this.lblServeMethod.Name = "lblServeMethod";
            this.lblServeMethod.Size = new System.Drawing.Size(74, 13);
            this.lblServeMethod.TabIndex = 41;
            this.lblServeMethod.Text = "Serve Method";
            // 
            // groupBox1
            // 
            this.groupBox1.Controls.Add(this.rbPublicAndSecure);
            this.groupBox1.Controls.Add(this.rbPublic);
            this.groupBox1.Location = new System.Drawing.Point(92, 183);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(377, 41);
            this.groupBox1.TabIndex = 40;
            this.groupBox1.TabStop = false;
            // 
            // rbPublic
            // 
            this.rbPublic.AutoSize = true;
            this.rbPublic.Checked = true;
            this.rbPublic.Location = new System.Drawing.Point(6, 13);
            this.rbPublic.Name = "rbPublic";
            this.rbPublic.Size = new System.Drawing.Size(54, 17);
            this.rbPublic.TabIndex = 4;
            this.rbPublic.TabStop = true;
            this.rbPublic.Text = "Public";
            this.toolTip1.SetToolTip(this.rbPublic, "Select to use local stylesheet deployed on your machine. ");
            this.rbPublic.UseVisualStyleBackColor = true;
            // 
            // frmGSAParams
            // 
            this.AcceptButton = this.btnOk;
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.CancelButton = this.btnCancel;
            this.ClientSize = new System.Drawing.Size(514, 289);
            this.Controls.Add(this.lblServeMethod);
            this.Controls.Add(this.groupBox1);
            this.Controls.Add(this.lblCause);
            this.Controls.Add(this.lblCheckConnectivityStatus);
            this.Controls.Add(this.label3);
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
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label lblCheckConnectivityStatus;
        private System.Windows.Forms.Label lblCause;
        private System.Windows.Forms.RadioButton rbPublicAndSecure;
        private System.Windows.Forms.Label lblServeMethod;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.RadioButton rbPublic;
    }
}

