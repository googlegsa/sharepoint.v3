/**
 * GetUserProfileSchemaResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.userprofileservice;

public class GetUserProfileSchemaResponse  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.PropertyInfo[] getUserProfileSchemaResult;

    public GetUserProfileSchemaResponse() {
    }

    public GetUserProfileSchemaResponse(
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.PropertyInfo[] getUserProfileSchemaResult) {
           this.getUserProfileSchemaResult = getUserProfileSchemaResult;
    }


    /**
     * Gets the getUserProfileSchemaResult value for this GetUserProfileSchemaResponse.
     * 
     * @return getUserProfileSchemaResult
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.PropertyInfo[] getGetUserProfileSchemaResult() {
        return getUserProfileSchemaResult;
    }


    /**
     * Sets the getUserProfileSchemaResult value for this GetUserProfileSchemaResponse.
     * 
     * @param getUserProfileSchemaResult
     */
    public void setGetUserProfileSchemaResult(com.google.enterprise.connector.sharepoint.generated.userprofileservice.PropertyInfo[] getUserProfileSchemaResult) {
        this.getUserProfileSchemaResult = getUserProfileSchemaResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetUserProfileSchemaResponse)) return false;
        GetUserProfileSchemaResponse other = (GetUserProfileSchemaResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.getUserProfileSchemaResult==null && other.getGetUserProfileSchemaResult()==null) || 
             (this.getUserProfileSchemaResult!=null &&
              java.util.Arrays.equals(this.getUserProfileSchemaResult, other.getGetUserProfileSchemaResult())));
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
        if (getGetUserProfileSchemaResult() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getGetUserProfileSchemaResult());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getGetUserProfileSchemaResult(), i);
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
        new org.apache.axis.description.TypeDesc(GetUserProfileSchemaResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", ">GetUserProfileSchemaResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("getUserProfileSchemaResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "GetUserProfileSchemaResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "PropertyInfo"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "PropertyInfo"));
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
