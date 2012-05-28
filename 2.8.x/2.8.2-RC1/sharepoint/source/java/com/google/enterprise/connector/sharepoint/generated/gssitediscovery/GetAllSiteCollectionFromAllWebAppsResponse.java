/**
 * GetAllSiteCollectionFromAllWebAppsResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssitediscovery;

public class GetAllSiteCollectionFromAllWebAppsResponse  implements java.io.Serializable {
    private java.lang.Object[] getAllSiteCollectionFromAllWebAppsResult;

    public GetAllSiteCollectionFromAllWebAppsResponse() {
    }

    public GetAllSiteCollectionFromAllWebAppsResponse(
           java.lang.Object[] getAllSiteCollectionFromAllWebAppsResult) {
           this.getAllSiteCollectionFromAllWebAppsResult = getAllSiteCollectionFromAllWebAppsResult;
    }


    /**
     * Gets the getAllSiteCollectionFromAllWebAppsResult value for this GetAllSiteCollectionFromAllWebAppsResponse.
     * 
     * @return getAllSiteCollectionFromAllWebAppsResult
     */
    public java.lang.Object[] getGetAllSiteCollectionFromAllWebAppsResult() {
        return getAllSiteCollectionFromAllWebAppsResult;
    }


    /**
     * Sets the getAllSiteCollectionFromAllWebAppsResult value for this GetAllSiteCollectionFromAllWebAppsResponse.
     * 
     * @param getAllSiteCollectionFromAllWebAppsResult
     */
    public void setGetAllSiteCollectionFromAllWebAppsResult(java.lang.Object[] getAllSiteCollectionFromAllWebAppsResult) {
        this.getAllSiteCollectionFromAllWebAppsResult = getAllSiteCollectionFromAllWebAppsResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetAllSiteCollectionFromAllWebAppsResponse)) return false;
        GetAllSiteCollectionFromAllWebAppsResponse other = (GetAllSiteCollectionFromAllWebAppsResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.getAllSiteCollectionFromAllWebAppsResult==null && other.getGetAllSiteCollectionFromAllWebAppsResult()==null) || 
             (this.getAllSiteCollectionFromAllWebAppsResult!=null &&
              java.util.Arrays.equals(this.getAllSiteCollectionFromAllWebAppsResult, other.getGetAllSiteCollectionFromAllWebAppsResult())));
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
        if (getGetAllSiteCollectionFromAllWebAppsResult() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getGetAllSiteCollectionFromAllWebAppsResult());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getGetAllSiteCollectionFromAllWebAppsResult(), i);
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
        new org.apache.axis.description.TypeDesc(GetAllSiteCollectionFromAllWebAppsResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", ">GetAllSiteCollectionFromAllWebAppsResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("getAllSiteCollectionFromAllWebAppsResult");
        elemField.setXmlName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "GetAllSiteCollectionFromAllWebAppsResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "anyType"));
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
