/**
 * GetUserProfileByNameResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.userprofileservice;

public class GetUserProfileByNameResponse  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.PropertyData[] getUserProfileByNameResult;

    public GetUserProfileByNameResponse() {
    }

    public GetUserProfileByNameResponse(
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.PropertyData[] getUserProfileByNameResult) {
           this.getUserProfileByNameResult = getUserProfileByNameResult;
    }


    /**
     * Gets the getUserProfileByNameResult value for this GetUserProfileByNameResponse.
     *
     * @return getUserProfileByNameResult
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.PropertyData[] getGetUserProfileByNameResult() {
        return getUserProfileByNameResult;
    }


    /**
     * Sets the getUserProfileByNameResult value for this GetUserProfileByNameResponse.
     *
     * @param getUserProfileByNameResult
     */
    public void setGetUserProfileByNameResult(com.google.enterprise.connector.sharepoint.generated.userprofileservice.PropertyData[] getUserProfileByNameResult) {
        this.getUserProfileByNameResult = getUserProfileByNameResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetUserProfileByNameResponse)) return false;
        GetUserProfileByNameResponse other = (GetUserProfileByNameResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
            ((this.getUserProfileByNameResult==null && other.getGetUserProfileByNameResult()==null) ||
             (this.getUserProfileByNameResult!=null &&
              java.util.Arrays.equals(this.getUserProfileByNameResult, other.getGetUserProfileByNameResult())));
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
        if (getGetUserProfileByNameResult() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getGetUserProfileByNameResult());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getGetUserProfileByNameResult(), i);
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
        new org.apache.axis.description.TypeDesc(GetUserProfileByNameResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", ">GetUserProfileByNameResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("getUserProfileByNameResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "GetUserProfileByNameResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "PropertyData"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "PropertyData"));
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
