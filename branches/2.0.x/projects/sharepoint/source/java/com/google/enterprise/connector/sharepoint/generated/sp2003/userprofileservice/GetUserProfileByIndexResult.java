/**
 * GetUserProfileByIndexResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice;

public class GetUserProfileByIndexResult  implements java.io.Serializable {
    private java.lang.String nextValue;

    private com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice.PropertyData[] userProfile;

    public GetUserProfileByIndexResult() {
    }

    public GetUserProfileByIndexResult(
           java.lang.String nextValue,
           com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice.PropertyData[] userProfile) {
           this.nextValue = nextValue;
           this.userProfile = userProfile;
    }


    /**
     * Gets the nextValue value for this GetUserProfileByIndexResult.
     * 
     * @return nextValue
     */
    public java.lang.String getNextValue() {
        return nextValue;
    }


    /**
     * Sets the nextValue value for this GetUserProfileByIndexResult.
     * 
     * @param nextValue
     */
    public void setNextValue(java.lang.String nextValue) {
        this.nextValue = nextValue;
    }


    /**
     * Gets the userProfile value for this GetUserProfileByIndexResult.
     * 
     * @return userProfile
     */
    public com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice.PropertyData[] getUserProfile() {
        return userProfile;
    }


    /**
     * Sets the userProfile value for this GetUserProfileByIndexResult.
     * 
     * @param userProfile
     */
    public void setUserProfile(com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice.PropertyData[] userProfile) {
        this.userProfile = userProfile;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetUserProfileByIndexResult)) return false;
        GetUserProfileByIndexResult other = (GetUserProfileByIndexResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.nextValue==null && other.getNextValue()==null) || 
             (this.nextValue!=null &&
              this.nextValue.equals(other.getNextValue()))) &&
            ((this.userProfile==null && other.getUserProfile()==null) || 
             (this.userProfile!=null &&
              java.util.Arrays.equals(this.userProfile, other.getUserProfile())));
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
        if (getNextValue() != null) {
            _hashCode += getNextValue().hashCode();
        }
        if (getUserProfile() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getUserProfile());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getUserProfile(), i);
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
        new org.apache.axis.description.TypeDesc(GetUserProfileByIndexResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "GetUserProfileByIndexResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("nextValue");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "NextValue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userProfile");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "UserProfile"));
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
