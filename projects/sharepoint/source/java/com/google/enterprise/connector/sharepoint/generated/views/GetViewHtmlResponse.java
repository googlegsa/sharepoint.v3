/**
 * GetViewHtmlResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.views;

public class GetViewHtmlResponse  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.views.GetViewHtmlResponseGetViewHtmlResult getViewHtmlResult;

    public GetViewHtmlResponse() {
    }

    public GetViewHtmlResponse(
           com.google.enterprise.connector.sharepoint.generated.views.GetViewHtmlResponseGetViewHtmlResult getViewHtmlResult) {
           this.getViewHtmlResult = getViewHtmlResult;
    }


    /**
     * Gets the getViewHtmlResult value for this GetViewHtmlResponse.
     * 
     * @return getViewHtmlResult
     */
    public com.google.enterprise.connector.sharepoint.generated.views.GetViewHtmlResponseGetViewHtmlResult getGetViewHtmlResult() {
        return getViewHtmlResult;
    }


    /**
     * Sets the getViewHtmlResult value for this GetViewHtmlResponse.
     * 
     * @param getViewHtmlResult
     */
    public void setGetViewHtmlResult(com.google.enterprise.connector.sharepoint.generated.views.GetViewHtmlResponseGetViewHtmlResult getViewHtmlResult) {
        this.getViewHtmlResult = getViewHtmlResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetViewHtmlResponse)) return false;
        GetViewHtmlResponse other = (GetViewHtmlResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.getViewHtmlResult==null && other.getGetViewHtmlResult()==null) || 
             (this.getViewHtmlResult!=null &&
              this.getViewHtmlResult.equals(other.getGetViewHtmlResult())));
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
        if (getGetViewHtmlResult() != null) {
            _hashCode += getGetViewHtmlResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetViewHtmlResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">GetViewHtmlResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("getViewHtmlResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetViewHtmlResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetViewHtmlResponse>GetViewHtmlResult"));
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
