/**
 * GetWebCrawlInfoResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssitediscovery;

public class GetWebCrawlInfoResponse  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo getWebCrawlInfoResult;

    public GetWebCrawlInfoResponse() {
    }

    public GetWebCrawlInfoResponse(
           com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo getWebCrawlInfoResult) {
           this.getWebCrawlInfoResult = getWebCrawlInfoResult;
    }


    /**
     * Gets the getWebCrawlInfoResult value for this GetWebCrawlInfoResponse.
     *
     * @return getWebCrawlInfoResult
     */
    public com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo getGetWebCrawlInfoResult() {
        return getWebCrawlInfoResult;
    }


    /**
     * Sets the getWebCrawlInfoResult value for this GetWebCrawlInfoResponse.
     *
     * @param getWebCrawlInfoResult
     */
    public void setGetWebCrawlInfoResult(com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo getWebCrawlInfoResult) {
        this.getWebCrawlInfoResult = getWebCrawlInfoResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetWebCrawlInfoResponse)) return false;
        GetWebCrawlInfoResponse other = (GetWebCrawlInfoResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
            ((this.getWebCrawlInfoResult==null && other.getGetWebCrawlInfoResult()==null) ||
             (this.getWebCrawlInfoResult!=null &&
              this.getWebCrawlInfoResult.equals(other.getGetWebCrawlInfoResult())));
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
        if (getGetWebCrawlInfoResult() != null) {
            _hashCode += getGetWebCrawlInfoResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetWebCrawlInfoResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", ">GetWebCrawlInfoResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("getWebCrawlInfoResult");
        elemField.setXmlName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "GetWebCrawlInfoResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "WebCrawlInfo"));
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
