/**
 * CheckConnectivityResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssitediscovery;

public class CheckConnectivityResponse  implements java.io.Serializable {
    private java.lang.String checkConnectivityResult;

    public CheckConnectivityResponse() {
    }

    public CheckConnectivityResponse(
           java.lang.String checkConnectivityResult) {
           this.checkConnectivityResult = checkConnectivityResult;
    }


    /**
     * Gets the checkConnectivityResult value for this CheckConnectivityResponse.
     *
     * @return checkConnectivityResult
     */
    public java.lang.String getCheckConnectivityResult() {
        return checkConnectivityResult;
    }


    /**
     * Sets the checkConnectivityResult value for this CheckConnectivityResponse.
     *
     * @param checkConnectivityResult
     */
    public void setCheckConnectivityResult(java.lang.String checkConnectivityResult) {
        this.checkConnectivityResult = checkConnectivityResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CheckConnectivityResponse)) return false;
        CheckConnectivityResponse other = (CheckConnectivityResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
            ((this.checkConnectivityResult==null && other.getCheckConnectivityResult()==null) ||
             (this.checkConnectivityResult!=null &&
              this.checkConnectivityResult.equals(other.getCheckConnectivityResult())));
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
        if (getCheckConnectivityResult() != null) {
            _hashCode += getCheckConnectivityResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CheckConnectivityResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", ">CheckConnectivityResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("checkConnectivityResult");
        elemField.setXmlName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "CheckConnectivityResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
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
