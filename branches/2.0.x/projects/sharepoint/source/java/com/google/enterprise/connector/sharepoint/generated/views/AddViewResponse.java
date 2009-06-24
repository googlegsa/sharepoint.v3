/**
 * AddViewResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.views;

public class AddViewResponse  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.views.AddViewResponseAddViewResult addViewResult;

    public AddViewResponse() {
    }

    public AddViewResponse(
           com.google.enterprise.connector.sharepoint.generated.views.AddViewResponseAddViewResult addViewResult) {
           this.addViewResult = addViewResult;
    }


    /**
     * Gets the addViewResult value for this AddViewResponse.
     * 
     * @return addViewResult
     */
    public com.google.enterprise.connector.sharepoint.generated.views.AddViewResponseAddViewResult getAddViewResult() {
        return addViewResult;
    }


    /**
     * Sets the addViewResult value for this AddViewResponse.
     * 
     * @param addViewResult
     */
    public void setAddViewResult(com.google.enterprise.connector.sharepoint.generated.views.AddViewResponseAddViewResult addViewResult) {
        this.addViewResult = addViewResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AddViewResponse)) return false;
        AddViewResponse other = (AddViewResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.addViewResult==null && other.getAddViewResult()==null) || 
             (this.addViewResult!=null &&
              this.addViewResult.equals(other.getAddViewResult())));
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
        if (getAddViewResult() != null) {
            _hashCode += getAddViewResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AddViewResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">AddViewResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("addViewResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "AddViewResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>AddViewResponse>AddViewResult"));
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
