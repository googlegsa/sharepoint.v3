/**
 * GetListCrawlInfoResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssitediscovery;

public class GetListCrawlInfoResponse  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.gssitediscovery.ListCrawlInfo[] getListCrawlInfoResult;

    public GetListCrawlInfoResponse() {
    }

    public GetListCrawlInfoResponse(
           com.google.enterprise.connector.sharepoint.generated.gssitediscovery.ListCrawlInfo[] getListCrawlInfoResult) {
           this.getListCrawlInfoResult = getListCrawlInfoResult;
    }


    /**
     * Gets the getListCrawlInfoResult value for this GetListCrawlInfoResponse.
     * 
     * @return getListCrawlInfoResult
     */
    public com.google.enterprise.connector.sharepoint.generated.gssitediscovery.ListCrawlInfo[] getGetListCrawlInfoResult() {
        return getListCrawlInfoResult;
    }


    /**
     * Sets the getListCrawlInfoResult value for this GetListCrawlInfoResponse.
     * 
     * @param getListCrawlInfoResult
     */
    public void setGetListCrawlInfoResult(com.google.enterprise.connector.sharepoint.generated.gssitediscovery.ListCrawlInfo[] getListCrawlInfoResult) {
        this.getListCrawlInfoResult = getListCrawlInfoResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetListCrawlInfoResponse)) return false;
        GetListCrawlInfoResponse other = (GetListCrawlInfoResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.getListCrawlInfoResult==null && other.getGetListCrawlInfoResult()==null) || 
             (this.getListCrawlInfoResult!=null &&
              java.util.Arrays.equals(this.getListCrawlInfoResult, other.getGetListCrawlInfoResult())));
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
        if (getGetListCrawlInfoResult() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getGetListCrawlInfoResult());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getGetListCrawlInfoResult(), i);
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
        new org.apache.axis.description.TypeDesc(GetListCrawlInfoResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", ">GetListCrawlInfoResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("getListCrawlInfoResult");
        elemField.setXmlName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "GetListCrawlInfoResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "ListCrawlInfo"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "ListCrawlInfo"));
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
