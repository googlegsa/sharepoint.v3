/**
 * AddMembership.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.userprofileservice;

public class AddMembership  implements java.io.Serializable {
    private java.lang.String accountName;

    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData membershipInfo;

    private java.lang.String group;

    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy privacy;

    public AddMembership() {
    }

    public AddMembership(
           java.lang.String accountName,
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData membershipInfo,
           java.lang.String group,
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy privacy) {
           this.accountName = accountName;
           this.membershipInfo = membershipInfo;
           this.group = group;
           this.privacy = privacy;
    }


    /**
     * Gets the accountName value for this AddMembership.
     *
     * @return accountName
     */
    public java.lang.String getAccountName() {
        return accountName;
    }


    /**
     * Sets the accountName value for this AddMembership.
     *
     * @param accountName
     */
    public void setAccountName(java.lang.String accountName) {
        this.accountName = accountName;
    }


    /**
     * Gets the membershipInfo value for this AddMembership.
     *
     * @return membershipInfo
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData getMembershipInfo() {
        return membershipInfo;
    }


    /**
     * Sets the membershipInfo value for this AddMembership.
     *
     * @param membershipInfo
     */
    public void setMembershipInfo(com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData membershipInfo) {
        this.membershipInfo = membershipInfo;
    }


    /**
     * Gets the group value for this AddMembership.
     *
     * @return group
     */
    public java.lang.String getGroup() {
        return group;
    }


    /**
     * Sets the group value for this AddMembership.
     *
     * @param group
     */
    public void setGroup(java.lang.String group) {
        this.group = group;
    }


    /**
     * Gets the privacy value for this AddMembership.
     *
     * @return privacy
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy getPrivacy() {
        return privacy;
    }


    /**
     * Sets the privacy value for this AddMembership.
     *
     * @param privacy
     */
    public void setPrivacy(com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy privacy) {
        this.privacy = privacy;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AddMembership)) return false;
        AddMembership other = (AddMembership) obj;
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
            ((this.membershipInfo==null && other.getMembershipInfo()==null) ||
             (this.membershipInfo!=null &&
              this.membershipInfo.equals(other.getMembershipInfo()))) &&
            ((this.group==null && other.getGroup()==null) ||
             (this.group!=null &&
              this.group.equals(other.getGroup()))) &&
            ((this.privacy==null && other.getPrivacy()==null) ||
             (this.privacy!=null &&
              this.privacy.equals(other.getPrivacy())));
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
        if (getMembershipInfo() != null) {
            _hashCode += getMembershipInfo().hashCode();
        }
        if (getGroup() != null) {
            _hashCode += getGroup().hashCode();
        }
        if (getPrivacy() != null) {
            _hashCode += getPrivacy().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AddMembership.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", ">AddMembership"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("accountName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "accountName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("membershipInfo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "membershipInfo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "MembershipData"));
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
