﻿//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by a tool.
//     Runtime Version:2.0.50727.3607
//
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

namespace WindowsApplication1.Properties {
    
    
    [global::System.Runtime.CompilerServices.CompilerGeneratedAttribute()]
    [global::System.CodeDom.Compiler.GeneratedCodeAttribute("Microsoft.VisualStudio.Editors.SettingsDesigner.SettingsSingleFileGenerator", "9.0.0.0")]
    internal sealed partial class Settings : global::System.Configuration.ApplicationSettingsBase {
        
        private static Settings defaultInstance = ((Settings)(global::System.Configuration.ApplicationSettingsBase.Synchronized(new Settings())));
        
        public static Settings Default {
            get {
                return defaultInstance;
            }
        }
        
        [global::System.Configuration.ApplicationScopedSettingAttribute()]
        [global::System.Diagnostics.DebuggerNonUserCodeAttribute()]
        [global::System.Configuration.SpecialSettingAttribute(global::System.Configuration.SpecialSetting.WebServiceUrl)]
        [global::System.Configuration.DefaultSettingValueAttribute("http://gdc04.persistent.co.in:4444/_vti_bin/GSBulkAuthorization.asmx")]
        public string WindowsApplication1_GSPBulkWS_BulkAuthorization {
            get {
                return ((string)(this["WindowsApplication1_GSPBulkWS_BulkAuthorization"]));
            }
        }
        
        [global::System.Configuration.ApplicationScopedSettingAttribute()]
        [global::System.Diagnostics.DebuggerNonUserCodeAttribute()]
        [global::System.Configuration.SpecialSettingAttribute(global::System.Configuration.SpecialSetting.WebServiceUrl)]
        [global::System.Configuration.DefaultSettingValueAttribute("http://gdc05.persistent.co.in:40000/sites/basic/_vti_bin/GSSiteDiscovery.asmx")]
        public string Verify_Installation_GSPSiteDiscoveryWS_GSPSiteDiscovery {
            get {
                return ((string)(this["Verify_Installation_GSPSiteDiscoveryWS_GSPSiteDiscovery"]));
            }
        }
        
        [global::System.Configuration.ApplicationScopedSettingAttribute()]
        [global::System.Diagnostics.DebuggerNonUserCodeAttribute()]
        [global::System.Configuration.SpecialSettingAttribute(global::System.Configuration.SpecialSetting.WebServiceUrl)]
        [global::System.Configuration.DefaultSettingValueAttribute("http://10.88.45.94:29951/_vti_bin/GssAcl.asmx")]
        public string Verify_Installation_GSPAclWS_GssAclMonitor {
            get {
                return ((string)(this["Verify_Installation_GSPAclWS_GssAclMonitor"]));
            }
        }
    }
}