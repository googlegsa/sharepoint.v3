/**
 * LoginResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.authentication;

public class LoginResult  implements java.io.Serializable {
    private java.lang.String cookieName;

    private com.google.enterprise.connector.sharepoint.generated.authentication.LoginErrorCode errorCode;

    public LoginResult() {
    }

    public LoginResult(
           java.lang.String cookieName,
           com.google.enterprise.connector.sharepoint.generated.authentication.LoginErrorCode errorCode) {
           this.cookieName = cookieName;
           this.errorCode = errorCode;
    }


    /**
     * Gets the cookieName value for this LoginResult.
     *
     * @return cookieName
     */
    public java.lang.String getCookieName() {
        return cookieName;
    }


    /**
     * Sets the cookieName value for this LoginResult.
     *
     * @param cookieName
     */
    public void setCookieName(java.lang.String cookieName) {
        this.cookieName = cookieName;
    }


    /**
     * Gets the errorCode value for this LoginResult.
     *
     * @return errorCode
     */
    public com.google.enterprise.connector.sharepoint.generated.authentication.LoginErrorCode getErrorCode() {
        return errorCode;
    }


    /**
     * Sets the errorCode value for this LoginResult.
     *
     * @param errorCode
     */
    public void setErrorCode(com.google.enterprise.connector.sharepoint.generated.authentication.LoginErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof LoginResult)) return false;
        LoginResult other = (LoginResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
            ((this.cookieName==null && other.getCookieName()==null) ||
             (this.cookieName!=null &&
              this.cookieName.equals(other.getCookieName()))) &&
            ((this.errorCode==null && other.getErrorCode()==null) ||
             (this.errorCode!=null &&
              this.errorCode.equals(other.getErrorCode())));
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
        if (getCookieName() != null) {
            _hashCode += getCookieName().hashCode();
        }
        if (getErrorCode() != null) {
            _hashCode += getErrorCode().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(LoginResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "LoginResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("cookieName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "CookieName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("errorCode");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "ErrorCode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "LoginErrorCode"));
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
