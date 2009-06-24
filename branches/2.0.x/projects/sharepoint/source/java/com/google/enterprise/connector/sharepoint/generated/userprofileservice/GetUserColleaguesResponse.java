/**
 * GetUserColleaguesResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.userprofileservice;

public class GetUserColleaguesResponse  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.ContactData[] getUserColleaguesResult;

    public GetUserColleaguesResponse() {
    }

    public GetUserColleaguesResponse(
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.ContactData[] getUserColleaguesResult) {
           this.getUserColleaguesResult = getUserColleaguesResult;
    }


    /**
     * Gets the getUserColleaguesResult value for this GetUserColleaguesResponse.
     * 
     * @return getUserColleaguesResult
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.ContactData[] getGetUserColleaguesResult() {
        return getUserColleaguesResult;
    }


    /**
     * Sets the getUserColleaguesResult value for this GetUserColleaguesResponse.
     * 
     * @param getUserColleaguesResult
     */
    public void setGetUserColleaguesResult(com.google.enterprise.connector.sharepoint.generated.userprofileservice.ContactData[] getUserColleaguesResult) {
        this.getUserColleaguesResult = getUserColleaguesResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetUserColleaguesResponse)) return false;
        GetUserColleaguesResponse other = (GetUserColleaguesResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.getUserColleaguesResult==null && other.getGetUserColleaguesResult()==null) || 
             (this.getUserColleaguesResult!=null &&
              java.util.Arrays.equals(this.getUserColleaguesResult, other.getGetUserColleaguesResult())));
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
        if (getGetUserColleaguesResult() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getGetUserColleaguesResult());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getGetUserColleaguesResult(), i);
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
        new org.apache.axis.description.TypeDesc(GetUserColleaguesResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", ">GetUserColleaguesResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("getUserColleaguesResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "GetUserColleaguesResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "ContactData"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "ContactData"));
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
