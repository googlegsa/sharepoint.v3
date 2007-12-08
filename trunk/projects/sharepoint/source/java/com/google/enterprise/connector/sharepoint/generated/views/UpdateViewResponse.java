/**
 * UpdateViewResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.views;

public class UpdateViewResponse  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.views.UpdateViewResponseUpdateViewResult updateViewResult;

    public UpdateViewResponse() {
    }

    public UpdateViewResponse(
           com.google.enterprise.connector.sharepoint.generated.views.UpdateViewResponseUpdateViewResult updateViewResult) {
           this.updateViewResult = updateViewResult;
    }


    /**
     * Gets the updateViewResult value for this UpdateViewResponse.
     * 
     * @return updateViewResult
     */
    public com.google.enterprise.connector.sharepoint.generated.views.UpdateViewResponseUpdateViewResult getUpdateViewResult() {
        return updateViewResult;
    }


    /**
     * Sets the updateViewResult value for this UpdateViewResponse.
     * 
     * @param updateViewResult
     */
    public void setUpdateViewResult(com.google.enterprise.connector.sharepoint.generated.views.UpdateViewResponseUpdateViewResult updateViewResult) {
        this.updateViewResult = updateViewResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UpdateViewResponse)) return false;
        UpdateViewResponse other = (UpdateViewResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.updateViewResult==null && other.getUpdateViewResult()==null) || 
             (this.updateViewResult!=null &&
              this.updateViewResult.equals(other.getUpdateViewResult())));
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
        if (getUpdateViewResult() != null) {
            _hashCode += getUpdateViewResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(UpdateViewResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">UpdateViewResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("updateViewResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "UpdateViewResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateViewResponse>UpdateViewResult"));
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
