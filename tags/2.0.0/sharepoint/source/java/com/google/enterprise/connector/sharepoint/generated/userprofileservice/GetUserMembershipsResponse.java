/**
 * GetUserMembershipsResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.userprofileservice;

public class GetUserMembershipsResponse  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData[] getUserMembershipsResult;

    public GetUserMembershipsResponse() {
    }

    public GetUserMembershipsResponse(
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData[] getUserMembershipsResult) {
           this.getUserMembershipsResult = getUserMembershipsResult;
    }


    /**
     * Gets the getUserMembershipsResult value for this GetUserMembershipsResponse.
     * 
     * @return getUserMembershipsResult
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData[] getGetUserMembershipsResult() {
        return getUserMembershipsResult;
    }


    /**
     * Sets the getUserMembershipsResult value for this GetUserMembershipsResponse.
     * 
     * @param getUserMembershipsResult
     */
    public void setGetUserMembershipsResult(com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData[] getUserMembershipsResult) {
        this.getUserMembershipsResult = getUserMembershipsResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetUserMembershipsResponse)) return false;
        GetUserMembershipsResponse other = (GetUserMembershipsResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.getUserMembershipsResult==null && other.getGetUserMembershipsResult()==null) || 
             (this.getUserMembershipsResult!=null &&
              java.util.Arrays.equals(this.getUserMembershipsResult, other.getGetUserMembershipsResult())));
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
        if (getGetUserMembershipsResult() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getGetUserMembershipsResult());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getGetUserMembershipsResult(), i);
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
        new org.apache.axis.description.TypeDesc(GetUserMembershipsResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", ">GetUserMembershipsResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("getUserMembershipsResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "GetUserMembershipsResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "MembershipData"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "MembershipData"));
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
