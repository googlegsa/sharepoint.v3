/**
 * GetWebCrawlInfoInBatchResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssitediscovery;

public class GetWebCrawlInfoInBatchResponse  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo[] getWebCrawlInfoInBatchResult;

    public GetWebCrawlInfoInBatchResponse() {
    }

    public GetWebCrawlInfoInBatchResponse(
           com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo[] getWebCrawlInfoInBatchResult) {
           this.getWebCrawlInfoInBatchResult = getWebCrawlInfoInBatchResult;
    }


    /**
     * Gets the getWebCrawlInfoInBatchResult value for this GetWebCrawlInfoInBatchResponse.
     *
     * @return getWebCrawlInfoInBatchResult
     */
    public com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo[] getGetWebCrawlInfoInBatchResult() {
        return getWebCrawlInfoInBatchResult;
    }


    /**
     * Sets the getWebCrawlInfoInBatchResult value for this GetWebCrawlInfoInBatchResponse.
     *
     * @param getWebCrawlInfoInBatchResult
     */
    public void setGetWebCrawlInfoInBatchResult(com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo[] getWebCrawlInfoInBatchResult) {
        this.getWebCrawlInfoInBatchResult = getWebCrawlInfoInBatchResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetWebCrawlInfoInBatchResponse)) return false;
        GetWebCrawlInfoInBatchResponse other = (GetWebCrawlInfoInBatchResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
            ((this.getWebCrawlInfoInBatchResult==null && other.getGetWebCrawlInfoInBatchResult()==null) ||
             (this.getWebCrawlInfoInBatchResult!=null &&
              java.util.Arrays.equals(this.getWebCrawlInfoInBatchResult, other.getGetWebCrawlInfoInBatchResult())));
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
        if (getGetWebCrawlInfoInBatchResult() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getGetWebCrawlInfoInBatchResult());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getGetWebCrawlInfoInBatchResult(), i);
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
        new org.apache.axis.description.TypeDesc(GetWebCrawlInfoInBatchResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", ">GetWebCrawlInfoInBatchResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("getWebCrawlInfoInBatchResult");
        elemField.setXmlName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "GetWebCrawlInfoInBatchResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "WebCrawlInfo"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "WebCrawlInfo"));
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
