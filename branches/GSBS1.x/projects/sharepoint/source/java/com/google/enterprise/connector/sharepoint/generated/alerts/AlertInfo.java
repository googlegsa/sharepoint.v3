/**
 * AlertInfo.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.alerts;

public class AlertInfo  implements java.io.Serializable {
    private java.lang.String currentUser;

    private java.lang.String alertServerName;

    private java.lang.String alertServerUrl;

    private java.lang.String alertServerType;

    private java.lang.String alertsManagementUrl;

    private java.lang.String alertWebTitle;

    private java.lang.String newAlertUrl;

    private java.lang.String alertWebId;

    private com.google.enterprise.connector.sharepoint.generated.alerts.Alert[] alerts;

    public AlertInfo() {
    }

    public AlertInfo(
           java.lang.String currentUser,
           java.lang.String alertServerName,
           java.lang.String alertServerUrl,
           java.lang.String alertServerType,
           java.lang.String alertsManagementUrl,
           java.lang.String alertWebTitle,
           java.lang.String newAlertUrl,
           java.lang.String alertWebId,
           com.google.enterprise.connector.sharepoint.generated.alerts.Alert[] alerts) {
           this.currentUser = currentUser;
           this.alertServerName = alertServerName;
           this.alertServerUrl = alertServerUrl;
           this.alertServerType = alertServerType;
           this.alertsManagementUrl = alertsManagementUrl;
           this.alertWebTitle = alertWebTitle;
           this.newAlertUrl = newAlertUrl;
           this.alertWebId = alertWebId;
           this.alerts = alerts;
    }


    /**
     * Gets the currentUser value for this AlertInfo.
     * 
     * @return currentUser
     */
    public java.lang.String getCurrentUser() {
        return currentUser;
    }


    /**
     * Sets the currentUser value for this AlertInfo.
     * 
     * @param currentUser
     */
    public void setCurrentUser(java.lang.String currentUser) {
        this.currentUser = currentUser;
    }


    /**
     * Gets the alertServerName value for this AlertInfo.
     * 
     * @return alertServerName
     */
    public java.lang.String getAlertServerName() {
        return alertServerName;
    }


    /**
     * Sets the alertServerName value for this AlertInfo.
     * 
     * @param alertServerName
     */
    public void setAlertServerName(java.lang.String alertServerName) {
        this.alertServerName = alertServerName;
    }


    /**
     * Gets the alertServerUrl value for this AlertInfo.
     * 
     * @return alertServerUrl
     */
    public java.lang.String getAlertServerUrl() {
        return alertServerUrl;
    }


    /**
     * Sets the alertServerUrl value for this AlertInfo.
     * 
     * @param alertServerUrl
     */
    public void setAlertServerUrl(java.lang.String alertServerUrl) {
        this.alertServerUrl = alertServerUrl;
    }


    /**
     * Gets the alertServerType value for this AlertInfo.
     * 
     * @return alertServerType
     */
    public java.lang.String getAlertServerType() {
        return alertServerType;
    }


    /**
     * Sets the alertServerType value for this AlertInfo.
     * 
     * @param alertServerType
     */
    public void setAlertServerType(java.lang.String alertServerType) {
        this.alertServerType = alertServerType;
    }


    /**
     * Gets the alertsManagementUrl value for this AlertInfo.
     * 
     * @return alertsManagementUrl
     */
    public java.lang.String getAlertsManagementUrl() {
        return alertsManagementUrl;
    }


    /**
     * Sets the alertsManagementUrl value for this AlertInfo.
     * 
     * @param alertsManagementUrl
     */
    public void setAlertsManagementUrl(java.lang.String alertsManagementUrl) {
        this.alertsManagementUrl = alertsManagementUrl;
    }


    /**
     * Gets the alertWebTitle value for this AlertInfo.
     * 
     * @return alertWebTitle
     */
    public java.lang.String getAlertWebTitle() {
        return alertWebTitle;
    }


    /**
     * Sets the alertWebTitle value for this AlertInfo.
     * 
     * @param alertWebTitle
     */
    public void setAlertWebTitle(java.lang.String alertWebTitle) {
        this.alertWebTitle = alertWebTitle;
    }


    /**
     * Gets the newAlertUrl value for this AlertInfo.
     * 
     * @return newAlertUrl
     */
    public java.lang.String getNewAlertUrl() {
        return newAlertUrl;
    }


    /**
     * Sets the newAlertUrl value for this AlertInfo.
     * 
     * @param newAlertUrl
     */
    public void setNewAlertUrl(java.lang.String newAlertUrl) {
        this.newAlertUrl = newAlertUrl;
    }


    /**
     * Gets the alertWebId value for this AlertInfo.
     * 
     * @return alertWebId
     */
    public java.lang.String getAlertWebId() {
        return alertWebId;
    }


    /**
     * Sets the alertWebId value for this AlertInfo.
     * 
     * @param alertWebId
     */
    public void setAlertWebId(java.lang.String alertWebId) {
        this.alertWebId = alertWebId;
    }


    /**
     * Gets the alerts value for this AlertInfo.
     * 
     * @return alerts
     */
    public com.google.enterprise.connector.sharepoint.generated.alerts.Alert[] getAlerts() {
        return alerts;
    }


    /**
     * Sets the alerts value for this AlertInfo.
     * 
     * @param alerts
     */
    public void setAlerts(com.google.enterprise.connector.sharepoint.generated.alerts.Alert[] alerts) {
        this.alerts = alerts;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AlertInfo)) return false;
        AlertInfo other = (AlertInfo) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.currentUser==null && other.getCurrentUser()==null) || 
             (this.currentUser!=null &&
              this.currentUser.equals(other.getCurrentUser()))) &&
            ((this.alertServerName==null && other.getAlertServerName()==null) || 
             (this.alertServerName!=null &&
              this.alertServerName.equals(other.getAlertServerName()))) &&
            ((this.alertServerUrl==null && other.getAlertServerUrl()==null) || 
             (this.alertServerUrl!=null &&
              this.alertServerUrl.equals(other.getAlertServerUrl()))) &&
            ((this.alertServerType==null && other.getAlertServerType()==null) || 
             (this.alertServerType!=null &&
              this.alertServerType.equals(other.getAlertServerType()))) &&
            ((this.alertsManagementUrl==null && other.getAlertsManagementUrl()==null) || 
             (this.alertsManagementUrl!=null &&
              this.alertsManagementUrl.equals(other.getAlertsManagementUrl()))) &&
            ((this.alertWebTitle==null && other.getAlertWebTitle()==null) || 
             (this.alertWebTitle!=null &&
              this.alertWebTitle.equals(other.getAlertWebTitle()))) &&
            ((this.newAlertUrl==null && other.getNewAlertUrl()==null) || 
             (this.newAlertUrl!=null &&
              this.newAlertUrl.equals(other.getNewAlertUrl()))) &&
            ((this.alertWebId==null && other.getAlertWebId()==null) || 
             (this.alertWebId!=null &&
              this.alertWebId.equals(other.getAlertWebId()))) &&
            ((this.alerts==null && other.getAlerts()==null) || 
             (this.alerts!=null &&
              java.util.Arrays.equals(this.alerts, other.getAlerts())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getCurrentUser() != null) {
            _hashCode += getCurrentUser().hashCode();
        }
        if (getAlertServerName() != null) {
            _hashCode += getAlertServerName().hashCode();
        }
        if (getAlertServerUrl() != null) {
            _hashCode += getAlertServerUrl().hashCode();
        }
        if (getAlertServerType() != null) {
            _hashCode += getAlertServerType().hashCode();
        }
        if (getAlertsManagementUrl() != null) {
            _hashCode += getAlertsManagementUrl().hashCode();
        }
        if (getAlertWebTitle() != null) {
            _hashCode += getAlertWebTitle().hashCode();
        }
        if (getNewAlertUrl() != null) {
            _hashCode += getNewAlertUrl().hashCode();
        }
        if (getAlertWebId() != null) {
            _hashCode += getAlertWebId().hashCode();
        }
        if (getAlerts() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAlerts());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAlerts(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AlertInfo.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "AlertInfo"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("currentUser");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "CurrentUser"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("alertServerName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "AlertServerName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("alertServerUrl");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "AlertServerUrl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("alertServerType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "AlertServerType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("alertsManagementUrl");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "AlertsManagementUrl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("alertWebTitle");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "AlertWebTitle"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("newAlertUrl");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "NewAlertUrl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("alertWebId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "AlertWebId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("alerts");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "Alerts"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "Alert"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "Alert"));
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
