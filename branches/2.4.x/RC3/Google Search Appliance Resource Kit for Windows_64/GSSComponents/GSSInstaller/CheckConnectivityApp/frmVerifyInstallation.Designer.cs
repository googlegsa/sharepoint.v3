namespace WindowsApplication1
{
    partial class frmVerifyInstallation
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
            this.btnCheckConnectivity = new System.Windows.Forms.Button();
            this.txtUrl = new System.Windows.Forms.TextBox();
            this.txtUserName = new System.Windows.Forms.TextBox();
            this.txtPassword = new System.Windows.Forms.TextBox();
            this.txtDomain = new System.Windows.Forms.TextBox();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.button1 = new System.Windows.Forms.Button();
            this.panel1 = new System.Windows.Forms.Panel();
            this.lblGSPBulkWSStatus = new System.Windows.Forms.Label();
            this.lblGSPBulkWS = new System.Windows.Forms.Label();
            this.lblGSPCustomService = new System.Windows.Forms.Label();
            this.lblGSPCustomWSStatus = new System.Windows.Forms.Label();
            this.lblWarning = new System.Windows.Forms.Label();
            this.panel1.SuspendLayout();
            this.SuspendLayout();
            // 
            // btnCheckConnectivity
            // 
            this.btnCheckConnectivity.AutoSize = true;
            this.btnCheckConnectivity.Location = new System.Drawing.Point(387, 62);
            this.btnCheckConnectivity.Name = "btnCheckConnectivity";
            this.btnCheckConnectivity.Size = new System.Drawing.Size(91, 23);
            this.btnCheckConnectivity.TabIndex = 0;
            this.btnCheckConnectivity.Text = "Verify";
            this.btnCheckConnectivity.UseVisualStyleBackColor = true;
            this.btnCheckConnectivity.Click += new System.EventHandler(this.btnCheckConnectivity_Click);
            // 
            // txtUrl
            // 
            this.txtUrl.Location = new System.Drawing.Point(164, 32);
            this.txtUrl.Name = "txtUrl";
            this.txtUrl.Size = new System.Drawing.Size(318, 20);
            this.txtUrl.TabIndex = 1;
            // 
            // txtUserName
            // 
            this.txtUserName.Location = new System.Drawing.Point(164, 58);
            this.txtUserName.Name = "txtUserName";
            this.txtUserName.Size = new System.Drawing.Size(206, 20);
            this.txtUserName.TabIndex = 2;
            // 
            // txtPassword
            // 
            this.txtPassword.Location = new System.Drawing.Point(164, 84);
            this.txtPassword.Name = "txtPassword";
            this.txtPassword.PasswordChar = '*';
            this.txtPassword.Size = new System.Drawing.Size(206, 20);
            this.txtPassword.TabIndex = 3;
            // 
            // txtDomain
            // 
            this.txtDomain.Location = new System.Drawing.Point(164, 110);
            this.txtDomain.Name = "txtDomain";
            this.txtDomain.Size = new System.Drawing.Size(206, 20);
            this.txtDomain.TabIndex = 4;
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(4, 32);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(155, 13);
            this.label1.TabIndex = 5;
            this.label1.Text = "Local SharePoint Website URL";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(4, 61);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(55, 13);
            this.label2.TabIndex = 6;
            this.label2.Text = "Username";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(4, 87);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(53, 13);
            this.label3.TabIndex = 7;
            this.label3.Text = "Password";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(4, 113);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(43, 13);
            this.label4.TabIndex = 8;
            this.label4.Text = "Domain";
            // 
            // button1
            // 
            this.button1.Location = new System.Drawing.Point(387, 95);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(91, 23);
            this.button1.TabIndex = 9;
            this.button1.Text = "Close";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.button1_Click);
            // 
            // panel1
            // 
            this.panel1.Controls.Add(this.lblGSPBulkWSStatus);
            this.panel1.Controls.Add(this.lblGSPBulkWS);
            this.panel1.Controls.Add(this.lblGSPCustomService);
            this.panel1.Controls.Add(this.lblGSPCustomWSStatus);
            this.panel1.Location = new System.Drawing.Point(7, 136);
            this.panel1.Name = "panel1";
            this.panel1.Size = new System.Drawing.Size(473, 73);
            this.panel1.TabIndex = 10;
            // 
            // lblGSPBulkWSStatus
            // 
            this.lblGSPBulkWSStatus.AutoSize = true;
            this.lblGSPBulkWSStatus.ForeColor = System.Drawing.SystemColors.ControlDarkDark;
            this.lblGSPBulkWSStatus.Location = new System.Drawing.Point(351, 48);
            this.lblGSPBulkWSStatus.Name = "lblGSPBulkWSStatus";
            this.lblGSPBulkWSStatus.Size = new System.Drawing.Size(0, 13);
            this.lblGSPBulkWSStatus.TabIndex = 4;
            // 
            // lblGSPBulkWS
            // 
            this.lblGSPBulkWS.AutoSize = true;
            this.lblGSPBulkWS.Location = new System.Drawing.Point(6, 44);
            this.lblGSPBulkWS.Name = "lblGSPBulkWS";
            this.lblGSPBulkWS.Size = new System.Drawing.Size(293, 13);
            this.lblGSPBulkWS.TabIndex = 3;
            this.lblGSPBulkWS.Text = "Google SharePoint connector bulk authorization web service";
            // 
            // lblGSPCustomService
            // 
            this.lblGSPCustomService.AutoSize = true;
            this.lblGSPCustomService.Location = new System.Drawing.Point(6, 17);
            this.lblGSPCustomService.Name = "lblGSPCustomService";
            this.lblGSPCustomService.Size = new System.Drawing.Size(274, 13);
            this.lblGSPCustomService.TabIndex = 2;
            this.lblGSPCustomService.Text = "Google SharePoint connector site discovery web service";
            // 
            // lblGSPCustomWSStatus
            // 
            this.lblGSPCustomWSStatus.AutoSize = true;
            this.lblGSPCustomWSStatus.ForeColor = System.Drawing.SystemColors.ControlDarkDark;
            this.lblGSPCustomWSStatus.Location = new System.Drawing.Point(351, 17);
            this.lblGSPCustomWSStatus.Name = "lblGSPCustomWSStatus";
            this.lblGSPCustomWSStatus.Size = new System.Drawing.Size(0, 13);
            this.lblGSPCustomWSStatus.TabIndex = 0;
            // 
            // lblWarning
            // 
            this.lblWarning.AutoSize = true;
            this.lblWarning.ForeColor = System.Drawing.Color.Red;
            this.lblWarning.Location = new System.Drawing.Point(4, 9);
            this.lblWarning.Name = "lblWarning";
            this.lblWarning.Size = new System.Drawing.Size(49, 13);
            this.lblWarning.TabIndex = 11;
            this.lblWarning.Text = "ERROR:";
            this.lblWarning.Visible = false;
            // 
            // frmVerifyInstallation
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(489, 219);
            this.Controls.Add(this.lblWarning);
            this.Controls.Add(this.panel1);
            this.Controls.Add(this.button1);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.txtDomain);
            this.Controls.Add(this.txtPassword);
            this.Controls.Add(this.txtUserName);
            this.Controls.Add(this.txtUrl);
            this.Controls.Add(this.btnCheckConnectivity);
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "frmVerifyInstallation";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Text = "Verify Installation";
            this.Load += new System.EventHandler(this.Form1_Load);
            this.panel1.ResumeLayout(false);
            this.panel1.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button btnCheckConnectivity;
        private System.Windows.Forms.TextBox txtUrl;
        private System.Windows.Forms.TextBox txtUserName;
        private System.Windows.Forms.TextBox txtPassword;
        private System.Windows.Forms.TextBox txtDomain;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.Panel panel1;
        private System.Windows.Forms.Label lblWarning;
        private System.Windows.Forms.Label lblGSPBulkWS;
        private System.Windows.Forms.Label lblGSPCustomService;
        private System.Windows.Forms.Label lblGSPCustomWSStatus;
        private System.Windows.Forms.Label lblGSPBulkWSStatus;

    }
}

