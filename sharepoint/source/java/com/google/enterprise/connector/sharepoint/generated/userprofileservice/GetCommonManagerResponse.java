/**
 * GetCommonManagerResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.userprofileservice;

public class GetCommonManagerResponse  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.ContactData getCommonManagerResult;

    public GetCommonManagerResponse() {
    }

    public GetCommonManagerResponse(
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.ContactData getCommonManagerResult) {
           this.getCommonManagerResult = getCommonManagerResult;
    }


    /**
     * Gets the getCommonManagerResult value for this GetCommonManagerResponse.
     * 
     * @return getCommonManagerResult
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.ContactData getGetCommonManagerResult() {
        return getCommonManagerResult;
    }


    /**
     * Sets the getCommonManagerResult value for this GetCommonManagerResponse.
     * 
     * @param getCommonManagerResult
     */
    public void setGetCommonManagerResult(com.google.enterprise.connector.sharepoint.generated.userprofileservice.ContactData getCommonManagerResult) {
        this.getCommonManagerResult = getCommonManagerResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetCommonManagerResponse)) return false;
        GetCommonManagerResponse other = (GetCommonManagerResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.getCommonManagerResult==null && other.getGetCommonManagerResult()==null) || 
             (this.getCommonManagerResult!=null &&
              this.getCommonManagerResult.equals(other.getGetCommonManagerResult())));
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
        if (getGetCommonManagerResult() != null) {
            _hashCode += getGetCommonManagerResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetCommonManagerResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", ">GetCommonManagerResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("getCommonManagerResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "GetCommonManagerResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "ContactData"));
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
