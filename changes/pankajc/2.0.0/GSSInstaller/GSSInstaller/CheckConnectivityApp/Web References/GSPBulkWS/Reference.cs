﻿//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by a tool.
//     Runtime Version:2.0.50727.3082
//
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

// 
// This source code was auto-generated by Microsoft.VSDesigner, Version 2.0.50727.3082.
// 
#pragma warning disable 1591

namespace WindowsApplication1.GSPBulkWS {
    using System.Diagnostics;
    using System.Web.Services;
    using System.ComponentModel;
    using System.Web.Services.Protocols;
    using System;
    using System.Xml.Serialization;
    
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("System.Web.Services", "2.0.50727.3053")]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.ComponentModel.DesignerCategoryAttribute("code")]
    [System.Web.Services.WebServiceBindingAttribute(Name="BulkAuthorizationSoap", Namespace="gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com")]
    public partial class BulkAuthorization : System.Web.Services.Protocols.SoapHttpClientProtocol {
        
        private System.Threading.SendOrPostCallback AuthorizeOperationCompleted;
        
        private System.Threading.SendOrPostCallback BulkAuthorizeOperationCompleted;
        
        private System.Threading.SendOrPostCallback CheckConnectivityOperationCompleted;
        
        private bool useDefaultCredentialsSetExplicitly;
        
        /// <remarks/>
        public BulkAuthorization() {
            this.Url = global::WindowsApplication1.Properties.Settings.Default.WindowsApplication1_GSPBulkWS_BulkAuthorization;
            if ((this.IsLocalFileSystemWebService(this.Url) == true)) {
                this.UseDefaultCredentials = true;
                this.useDefaultCredentialsSetExplicitly = false;
            }
            else {
                this.useDefaultCredentialsSetExplicitly = true;
            }
        }
        
        public new string Url {
            get {
                return base.Url;
            }
            set {
                if ((((this.IsLocalFileSystemWebService(base.Url) == true) 
                            && (this.useDefaultCredentialsSetExplicitly == false)) 
                            && (this.IsLocalFileSystemWebService(value) == false))) {
                    base.UseDefaultCredentials = false;
                }
                base.Url = value;
            }
        }
        
        public new bool UseDefaultCredentials {
            get {
                return base.UseDefaultCredentials;
            }
            set {
                base.UseDefaultCredentials = value;
                this.useDefaultCredentialsSetExplicitly = true;
            }
        }
        
        /// <remarks/>
        public event AuthorizeCompletedEventHandler AuthorizeCompleted;
        
        /// <remarks/>
        public event BulkAuthorizeCompletedEventHandler BulkAuthorizeCompleted;
        
        /// <remarks/>
        public event CheckConnectivityCompletedEventHandler CheckConnectivityCompleted;
        
        /// <remarks/>
        [System.Web.Services.Protocols.SoapDocumentMethodAttribute("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com/Authoriz" +
            "e", RequestNamespace="gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", ResponseNamespace="gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", Use=System.Web.Services.Description.SoapBindingUse.Literal, ParameterStyle=System.Web.Services.Protocols.SoapParameterStyle.Wrapped)]
        public void Authorize(AuthData authData, string loginId) {
            this.Invoke("Authorize", new object[] {
                        authData,
                        loginId});
        }
        
        /// <remarks/>
        public void AuthorizeAsync(AuthData authData, string loginId) {
            this.AuthorizeAsync(authData, loginId, null);
        }
        
        /// <remarks/>
        public void AuthorizeAsync(AuthData authData, string loginId, object userState) {
            if ((this.AuthorizeOperationCompleted == null)) {
                this.AuthorizeOperationCompleted = new System.Threading.SendOrPostCallback(this.OnAuthorizeOperationCompleted);
            }
            this.InvokeAsync("Authorize", new object[] {
                        authData,
                        loginId}, this.AuthorizeOperationCompleted, userState);
        }
        
        private void OnAuthorizeOperationCompleted(object arg) {
            if ((this.AuthorizeCompleted != null)) {
                System.Web.Services.Protocols.InvokeCompletedEventArgs invokeArgs = ((System.Web.Services.Protocols.InvokeCompletedEventArgs)(arg));
                this.AuthorizeCompleted(this, new System.ComponentModel.AsyncCompletedEventArgs(invokeArgs.Error, invokeArgs.Cancelled, invokeArgs.UserState));
            }
        }
        
        /// <remarks/>
        [System.Web.Services.Protocols.SoapDocumentMethodAttribute("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com/BulkAuth" +
            "orize", RequestNamespace="gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", ResponseNamespace="gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", Use=System.Web.Services.Description.SoapBindingUse.Literal, ParameterStyle=System.Web.Services.Protocols.SoapParameterStyle.Wrapped)]
        public AuthData[] BulkAuthorize(AuthData[] authData, string loginId) {
            object[] results = this.Invoke("BulkAuthorize", new object[] {
                        authData,
                        loginId});
            return ((AuthData[])(results[0]));
        }
        
        /// <remarks/>
        public void BulkAuthorizeAsync(AuthData[] authData, string loginId) {
            this.BulkAuthorizeAsync(authData, loginId, null);
        }
        
        /// <remarks/>
        public void BulkAuthorizeAsync(AuthData[] authData, string loginId, object userState) {
            if ((this.BulkAuthorizeOperationCompleted == null)) {
                this.BulkAuthorizeOperationCompleted = new System.Threading.SendOrPostCallback(this.OnBulkAuthorizeOperationCompleted);
            }
            this.InvokeAsync("BulkAuthorize", new object[] {
                        authData,
                        loginId}, this.BulkAuthorizeOperationCompleted, userState);
        }
        
        private void OnBulkAuthorizeOperationCompleted(object arg) {
            if ((this.BulkAuthorizeCompleted != null)) {
                System.Web.Services.Protocols.InvokeCompletedEventArgs invokeArgs = ((System.Web.Services.Protocols.InvokeCompletedEventArgs)(arg));
                this.BulkAuthorizeCompleted(this, new BulkAuthorizeCompletedEventArgs(invokeArgs.Results, invokeArgs.Error, invokeArgs.Cancelled, invokeArgs.UserState));
            }
        }
        
        /// <remarks/>
        [System.Web.Services.Protocols.SoapDocumentMethodAttribute("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com/CheckCon" +
            "nectivity", RequestNamespace="gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", ResponseNamespace="gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", Use=System.Web.Services.Description.SoapBindingUse.Literal, ParameterStyle=System.Web.Services.Protocols.SoapParameterStyle.Wrapped)]
        public string CheckConnectivity() {
            object[] results = this.Invoke("CheckConnectivity", new object[0]);
            return ((string)(results[0]));
        }
        
        /// <remarks/>
        public void CheckConnectivityAsync() {
            this.CheckConnectivityAsync(null);
        }
        
        /// <remarks/>
        public void CheckConnectivityAsync(object userState) {
            if ((this.CheckConnectivityOperationCompleted == null)) {
                this.CheckConnectivityOperationCompleted = new System.Threading.SendOrPostCallback(this.OnCheckConnectivityOperationCompleted);
            }
            this.InvokeAsync("CheckConnectivity", new object[0], this.CheckConnectivityOperationCompleted, userState);
        }
        
        private void OnCheckConnectivityOperationCompleted(object arg) {
            if ((this.CheckConnectivityCompleted != null)) {
                System.Web.Services.Protocols.InvokeCompletedEventArgs invokeArgs = ((System.Web.Services.Protocols.InvokeCompletedEventArgs)(arg));
                this.CheckConnectivityCompleted(this, new CheckConnectivityCompletedEventArgs(invokeArgs.Results, invokeArgs.Error, invokeArgs.Cancelled, invokeArgs.UserState));
            }
        }
        
        /// <remarks/>
        public new void CancelAsync(object userState) {
            base.CancelAsync(userState);
        }
        
        private bool IsLocalFileSystemWebService(string url) {
            if (((url == null) 
                        || (url == string.Empty))) {
                return false;
            }
            System.Uri wsUri = new System.Uri(url);
            if (((wsUri.Port >= 1024) 
                        && (string.Compare(wsUri.Host, "localHost", System.StringComparison.OrdinalIgnoreCase) == 0))) {
                return true;
            }
            return false;
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("System.Xml", "2.0.50727.3082")]
    [System.SerializableAttribute()]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.ComponentModel.DesignerCategoryAttribute("code")]
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com")]
    public partial class AuthData {
        
        private string listURLField;
        
        private string listItemIdField;
        
        private bool isAllowedField;
        
        private string errorField;
        
        private string complexDocIdField;
        
        /// <remarks/>
        public string listURL {
            get {
                return this.listURLField;
            }
            set {
                this.listURLField = value;
            }
        }
        
        /// <remarks/>
        public string listItemId {
            get {
                return this.listItemIdField;
            }
            set {
                this.listItemIdField = value;
            }
        }
        
        /// <remarks/>
        public bool isAllowed {
            get {
                return this.isAllowedField;
            }
            set {
                this.isAllowedField = value;
            }
        }
        
        /// <remarks/>
        public string error {
            get {
                return this.errorField;
            }
            set {
                this.errorField = value;
            }
        }
        
        /// <remarks/>
        public string complexDocId {
            get {
                return this.complexDocIdField;
            }
            set {
                this.complexDocIdField = value;
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("System.Web.Services", "2.0.50727.3053")]
    public delegate void AuthorizeCompletedEventHandler(object sender, System.ComponentModel.AsyncCompletedEventArgs e);
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("System.Web.Services", "2.0.50727.3053")]
    public delegate void BulkAuthorizeCompletedEventHandler(object sender, BulkAuthorizeCompletedEventArgs e);
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("System.Web.Services", "2.0.50727.3053")]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.ComponentModel.DesignerCategoryAttribute("code")]
    public partial class BulkAuthorizeCompletedEventArgs : System.ComponentModel.AsyncCompletedEventArgs {
        
        private object[] results;
        
        internal BulkAuthorizeCompletedEventArgs(object[] results, System.Exception exception, bool cancelled, object userState) : 
                base(exception, cancelled, userState) {
            this.results = results;
        }
        
        /// <remarks/>
        public AuthData[] Result {
            get {
                this.RaiseExceptionIfNecessary();
                return ((AuthData[])(this.results[0]));
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("System.Web.Services", "2.0.50727.3053")]
    public delegate void CheckConnectivityCompletedEventHandler(object sender, CheckConnectivityCompletedEventArgs e);
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("System.Web.Services", "2.0.50727.3053")]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.ComponentModel.DesignerCategoryAttribute("code")]
    public partial class CheckConnectivityCompletedEventArgs : System.ComponentModel.AsyncCompletedEventArgs {
        
        private object[] results;
        
        internal CheckConnectivityCompletedEventArgs(object[] results, System.Exception exception, bool cancelled, object userState) : 
                base(exception, cancelled, userState) {
            this.results = results;
        }
        
        /// <remarks/>
        public string Result {
            get {
                this.RaiseExceptionIfNecessary();
                return ((string)(this.results[0]));
            }
        }
    }
}

#pragma warning restore 1591