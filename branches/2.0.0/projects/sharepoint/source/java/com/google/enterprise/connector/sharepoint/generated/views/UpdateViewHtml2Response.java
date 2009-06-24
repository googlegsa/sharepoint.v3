/**
 * UpdateViewHtml2Response.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.views;

public class UpdateViewHtml2Response  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ResponseUpdateViewHtml2Result updateViewHtml2Result;

    public UpdateViewHtml2Response() {
    }

    public UpdateViewHtml2Response(
           com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ResponseUpdateViewHtml2Result updateViewHtml2Result) {
           this.updateViewHtml2Result = updateViewHtml2Result;
    }


    /**
     * Gets the updateViewHtml2Result value for this UpdateViewHtml2Response.
     * 
     * @return updateViewHtml2Result
     */
    public com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ResponseUpdateViewHtml2Result getUpdateViewHtml2Result() {
        return updateViewHtml2Result;
    }


    /**
     * Sets the updateViewHtml2Result value for this UpdateViewHtml2Response.
     * 
     * @param updateViewHtml2Result
     */
    public void setUpdateViewHtml2Result(com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ResponseUpdateViewHtml2Result updateViewHtml2Result) {
        this.updateViewHtml2Result = updateViewHtml2Result;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UpdateViewHtml2Response)) return false;
        UpdateViewHtml2Response other = (UpdateViewHtml2Response) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.updateViewHtml2Result==null && other.getUpdateViewHtml2Result()==null) || 
             (this.updateViewHtml2Result!=null &&
              this.updateViewHtml2Result.equals(other.getUpdateViewHtml2Result())));
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
        if (getUpdateViewHtml2Result() != null) {
            _hashCode += getUpdateViewHtml2Result().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(UpdateViewHtml2Response.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">UpdateViewHtml2Response"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("updateViewHtml2Result");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "UpdateViewHtml2Result"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateViewHtml2Response>UpdateViewHtml2Result"));
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
