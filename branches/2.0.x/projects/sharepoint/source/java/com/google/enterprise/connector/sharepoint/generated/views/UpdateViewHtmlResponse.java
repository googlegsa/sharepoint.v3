/**
 * UpdateViewHtmlResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.views;

public class UpdateViewHtmlResponse  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtmlResponseUpdateViewHtmlResult updateViewHtmlResult;

    public UpdateViewHtmlResponse() {
    }

    public UpdateViewHtmlResponse(
           com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtmlResponseUpdateViewHtmlResult updateViewHtmlResult) {
           this.updateViewHtmlResult = updateViewHtmlResult;
    }


    /**
     * Gets the updateViewHtmlResult value for this UpdateViewHtmlResponse.
     * 
     * @return updateViewHtmlResult
     */
    public com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtmlResponseUpdateViewHtmlResult getUpdateViewHtmlResult() {
        return updateViewHtmlResult;
    }


    /**
     * Sets the updateViewHtmlResult value for this UpdateViewHtmlResponse.
     * 
     * @param updateViewHtmlResult
     */
    public void setUpdateViewHtmlResult(com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtmlResponseUpdateViewHtmlResult updateViewHtmlResult) {
        this.updateViewHtmlResult = updateViewHtmlResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UpdateViewHtmlResponse)) return false;
        UpdateViewHtmlResponse other = (UpdateViewHtmlResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.updateViewHtmlResult==null && other.getUpdateViewHtmlResult()==null) || 
             (this.updateViewHtmlResult!=null &&
              this.updateViewHtmlResult.equals(other.getUpdateViewHtmlResult())));
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
        if (getUpdateViewHtmlResult() != null) {
            _hashCode += getUpdateViewHtmlResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(UpdateViewHtmlResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">UpdateViewHtmlResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("updateViewHtmlResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "UpdateViewHtmlResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateViewHtmlResponse>UpdateViewHtmlResult"));
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
