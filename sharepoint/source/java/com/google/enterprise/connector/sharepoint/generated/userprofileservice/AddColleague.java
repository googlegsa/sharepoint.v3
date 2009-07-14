/**
 * AddColleague.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.userprofileservice;

public class AddColleague  implements java.io.Serializable {
    private java.lang.String accountName;

    private java.lang.String colleagueAccountName;

    private java.lang.String group;

    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy privacy;

    private boolean isInWorkGroup;

    public AddColleague() {
    }

    public AddColleague(
           java.lang.String accountName,
           java.lang.String colleagueAccountName,
           java.lang.String group,
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy privacy,
           boolean isInWorkGroup) {
           this.accountName = accountName;
           this.colleagueAccountName = colleagueAccountName;
           this.group = group;
           this.privacy = privacy;
           this.isInWorkGroup = isInWorkGroup;
    }


    /**
     * Gets the accountName value for this AddColleague.
     * 
     * @return accountName
     */
    public java.lang.String getAccountName() {
        return accountName;
    }


    /**
     * Sets the accountName value for this AddColleague.
     * 
     * @param accountName
     */
    public void setAccountName(java.lang.String accountName) {
        this.accountName = accountName;
    }


    /**
     * Gets the colleagueAccountName value for this AddColleague.
     * 
     * @return colleagueAccountName
     */
    public java.lang.String getColleagueAccountName() {
        return colleagueAccountName;
    }


    /**
     * Sets the colleagueAccountName value for this AddColleague.
     * 
     * @param colleagueAccountName
     */
    public void setColleagueAccountName(java.lang.String colleagueAccountName) {
        this.colleagueAccountName = colleagueAccountName;
    }


    /**
     * Gets the group value for this AddColleague.
     * 
     * @return group
     */
    public java.lang.String getGroup() {
        return group;
    }


    /**
     * Sets the group value for this AddColleague.
     * 
     * @param group
     */
    public void setGroup(java.lang.String group) {
        this.group = group;
    }


    /**
     * Gets the privacy value for this AddColleague.
     * 
     * @return privacy
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy getPrivacy() {
        return privacy;
    }


    /**
     * Sets the privacy value for this AddColleague.
     * 
     * @param privacy
     */
    public void setPrivacy(com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy privacy) {
        this.privacy = privacy;
    }


    /**
     * Gets the isInWorkGroup value for this AddColleague.
     * 
     * @return isInWorkGroup
     */
    public boolean isIsInWorkGroup() {
        return isInWorkGroup;
    }


    /**
     * Sets the isInWorkGroup value for this AddColleague.
     * 
     * @param isInWorkGroup
     */
    public void setIsInWorkGroup(boolean isInWorkGroup) {
        this.isInWorkGroup = isInWorkGroup;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AddColleague)) return false;
        AddColleague other = (AddColleague) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.accountName==null && other.getAccountName()==null) || 
             (this.accountName!=null &&
              this.accountName.equals(other.getAccountName()))) &&
            ((this.colleagueAccountName==null && other.getColleagueAccountName()==null) || 
             (this.colleagueAccountName!=null &&
              this.colleagueAccountName.equals(other.getColleagueAccountName()))) &&
            ((this.group==null && other.getGroup()==null) || 
             (this.group!=null &&
              this.group.equals(other.getGroup()))) &&
            ((this.privacy==null && other.getPrivacy()==null) || 
             (this.privacy!=null &&
              this.privacy.equals(other.getPrivacy()))) &&
            this.isInWorkGroup == other.isIsInWorkGroup();
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
        if (getAccountName() != null) {
            _hashCode += getAccountName().hashCode();
        }
        if (getColleagueAccountName() != null) {
            _hashCode += getColleagueAccountName().hashCode();
        }
        if (getGroup() != null) {
            _hashCode += getGroup().hashCode();
        }
        if (getPrivacy() != null) {
            _hashCode += getPrivacy().hashCode();
        }
        _hashCode += (isIsInWorkGroup() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AddColleague.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", ">AddColleague"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("accountName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "accountName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("colleagueAccountName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "colleagueAccountName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("group");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "group"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("privacy");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "privacy"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "Privacy"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isInWorkGroup");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "isInWorkGroup"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
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
