/**
 * GetUserPinnedLinksResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.userprofileservice;

public class GetUserPinnedLinksResponse  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.PinnedLinkData[] getUserPinnedLinksResult;

    public GetUserPinnedLinksResponse() {
    }

    public GetUserPinnedLinksResponse(
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.PinnedLinkData[] getUserPinnedLinksResult) {
           this.getUserPinnedLinksResult = getUserPinnedLinksResult;
    }


    /**
     * Gets the getUserPinnedLinksResult value for this GetUserPinnedLinksResponse.
     * 
     * @return getUserPinnedLinksResult
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.PinnedLinkData[] getGetUserPinnedLinksResult() {
        return getUserPinnedLinksResult;
    }


    /**
     * Sets the getUserPinnedLinksResult value for this GetUserPinnedLinksResponse.
     * 
     * @param getUserPinnedLinksResult
     */
    public void setGetUserPinnedLinksResult(com.google.enterprise.connector.sharepoint.generated.userprofileservice.PinnedLinkData[] getUserPinnedLinksResult) {
        this.getUserPinnedLinksResult = getUserPinnedLinksResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetUserPinnedLinksResponse)) return false;
        GetUserPinnedLinksResponse other = (GetUserPinnedLinksResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.getUserPinnedLinksResult==null && other.getGetUserPinnedLinksResult()==null) || 
             (this.getUserPinnedLinksResult!=null &&
              java.util.Arrays.equals(this.getUserPinnedLinksResult, other.getGetUserPinnedLinksResult())));
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
        if (getGetUserPinnedLinksResult() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getGetUserPinnedLinksResult());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getGetUserPinnedLinksResult(), i);
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
        new org.apache.axis.description.TypeDesc(GetUserPinnedLinksResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", ">GetUserPinnedLinksResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("getUserPinnedLinksResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "GetUserPinnedLinksResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "PinnedLinkData"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "PinnedLinkData"));
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
