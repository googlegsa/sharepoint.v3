/**
 * ModifyUserPropertyByAccountName.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.userprofileservice;

public class ModifyUserPropertyByAccountName  implements java.io.Serializable {
    private java.lang.String accountName;

    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.PropertyData[] newData;

    public ModifyUserPropertyByAccountName() {
    }

    public ModifyUserPropertyByAccountName(
           java.lang.String accountName,
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.PropertyData[] newData) {
           this.accountName = accountName;
           this.newData = newData;
    }


    /**
     * Gets the accountName value for this ModifyUserPropertyByAccountName.
     *
     * @return accountName
     */
    public java.lang.String getAccountName() {
        return accountName;
    }


    /**
     * Sets the accountName value for this ModifyUserPropertyByAccountName.
     *
     * @param accountName
     */
    public void setAccountName(java.lang.String accountName) {
        this.accountName = accountName;
    }


    /**
     * Gets the newData value for this ModifyUserPropertyByAccountName.
     *
     * @return newData
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.PropertyData[] getNewData() {
        return newData;
    }


    /**
     * Sets the newData value for this ModifyUserPropertyByAccountName.
     *
     * @param newData
     */
    public void setNewData(com.google.enterprise.connector.sharepoint.generated.userprofileservice.PropertyData[] newData) {
        this.newData = newData;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ModifyUserPropertyByAccountName)) return false;
        ModifyUserPropertyByAccountName other = (ModifyUserPropertyByAccountName) obj;
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
            ((this.newData==null && other.getNewData()==null) ||
             (this.newData!=null &&
              java.util.Arrays.equals(this.newData, other.getNewData())));
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
        if (getNewData() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getNewData());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getNewData(), i);
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
        new org.apache.axis.description.TypeDesc(ModifyUserPropertyByAccountName.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", ">ModifyUserPropertyByAccountName"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("accountName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "accountName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("newData");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "newData"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "PropertyData"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "PropertyData"));
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
