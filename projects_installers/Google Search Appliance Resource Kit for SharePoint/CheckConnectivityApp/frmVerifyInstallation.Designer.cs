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
            this.lblWarning = new System.Windows.Forms.Label();
            this.cbUseWindowsSessionCreds = new System.Windows.Forms.CheckBox();
            this.SuspendLayout();
            // 
            // btnCheckConnectivity
            // 
            this.btnCheckConnectivity.AutoSize = true;
            this.btnCheckConnectivity.Location = new System.Drawing.Point(382, 61);
            this.btnCheckConnectivity.Name = "btnCheckConnectivity";
            this.btnCheckConnectivity.Size = new System.Drawing.Size(91, 23);
            this.btnCheckConnectivity.TabIndex = 4;
            this.btnCheckConnectivity.Text = "Verify";
            this.btnCheckConnectivity.UseVisualStyleBackColor = true;
            this.btnCheckConnectivity.Click += new System.EventHandler(this.btnCheckConnectivity_Click);
            // 
            // txtUrl
            // 
            this.txtUrl.Location = new System.Drawing.Point(165, 32);
            this.txtUrl.Name = "txtUrl";
            this.txtUrl.Size = new System.Drawing.Size(311, 20);
            this.txtUrl.TabIndex = 0;
            // 
            // txtUserName
            // 
            this.txtUserName.Location = new System.Drawing.Point(165, 84);
            this.txtUserName.Name = "txtUserName";
            this.txtUserName.Size = new System.Drawing.Size(177, 20);
            this.txtUserName.TabIndex = 2;
            // 
            // txtPassword
            // 
            this.txtPassword.Location = new System.Drawing.Point(165, 110);
            this.txtPassword.Name = "txtPassword";
            this.txtPassword.PasswordChar = '*';
            this.txtPassword.Size = new System.Drawing.Size(177, 20);
            this.txtPassword.TabIndex = 3;
            // 
            // txtDomain
            // 
            this.txtDomain.Location = new System.Drawing.Point(165, 58);
            this.txtDomain.Name = "txtDomain";
            this.txtDomain.Size = new System.Drawing.Size(177, 20);
            this.txtDomain.TabIndex = 1;
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(5, 32);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(155, 13);
            this.label1.TabIndex = 6;
            this.label1.Text = "Local SharePoint Website URL";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(5, 87);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(55, 13);
            this.label2.TabIndex = 8;
            this.label2.Text = "Username";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(5, 113);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(53, 13);
            this.label3.TabIndex = 9;
            this.label3.Text = "Password";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(5, 61);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(43, 13);
            this.label4.TabIndex = 7;
            this.label4.Text = "Domain";
            // 
            // button1
            // 
            this.button1.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.button1.Location = new System.Drawing.Point(382, 94);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(91, 23);
            this.button1.TabIndex = 5;
            this.button1.Text = "Close";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.button1_Click);
            // 
            // lblWarning
            // 
            this.lblWarning.AutoSize = true;
            this.lblWarning.ForeColor = System.Drawing.Color.Red;
            this.lblWarning.Location = new System.Drawing.Point(5, 9);
            this.lblWarning.Name = "lblWarning";
            this.lblWarning.Size = new System.Drawing.Size(49, 13);
            this.lblWarning.TabIndex = 10;
            this.lblWarning.Text = "ERROR:";
            this.lblWarning.Visible = false;
            // 
            // cbUseWindowsSessionCreds
            // 
            this.cbUseWindowsSessionCreds.AutoSize = true;
            this.cbUseWindowsSessionCreds.Location = new System.Drawing.Point(165, 137);
            this.cbUseWindowsSessionCreds.Name = "cbUseWindowsSessionCreds";
            this.cbUseWindowsSessionCreds.Size = new System.Drawing.Size(189, 17);
            this.cbUseWindowsSessionCreds.TabIndex = 11;
            this.cbUseWindowsSessionCreds.Text = "Use Windowss session credentials";
            this.cbUseWindowsSessionCreds.UseVisualStyleBackColor = true;
            this.cbUseWindowsSessionCreds.Visible = false;
            this.cbUseWindowsSessionCreds.CheckedChanged += new System.EventHandler(this.cbUseWindowsSessionCreds_CheckedChanged);
            // 
            // frmVerifyInstallation
            // 
            this.AcceptButton = this.btnCheckConnectivity;
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.CancelButton = this.button1;
            this.ClientSize = new System.Drawing.Size(482, 157);
            this.Controls.Add(this.cbUseWindowsSessionCreds);
            this.Controls.Add(this.lblWarning);
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
            this.Text = "Google Services for SharePoint- Verify Installation";
            this.Load += new System.EventHandler(this.Form1_Load);
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
        private System.Windows.Forms.Label lblWarning;
        private System.Windows.Forms.CheckBox cbUseWindowsSessionCreds;

    }
}

