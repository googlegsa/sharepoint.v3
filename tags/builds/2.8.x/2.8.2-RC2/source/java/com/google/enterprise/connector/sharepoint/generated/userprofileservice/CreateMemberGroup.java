/**
 * CreateMemberGroup.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.userprofileservice;

public class CreateMemberGroup  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData membershipInfo;

    public CreateMemberGroup() {
    }

    public CreateMemberGroup(
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData membershipInfo) {
           this.membershipInfo = membershipInfo;
    }


    /**
     * Gets the membershipInfo value for this CreateMemberGroup.
     *
     * @return membershipInfo
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData getMembershipInfo() {
        return membershipInfo;
    }


    /**
     * Sets the membershipInfo value for this CreateMemberGroup.
     *
     * @param membershipInfo
     */
    public void setMembershipInfo(com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData membershipInfo) {
        this.membershipInfo = membershipInfo;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CreateMemberGroup)) return false;
        CreateMemberGroup other = (CreateMemberGroup) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
            ((this.membershipInfo==null && other.getMembershipInfo()==null) ||
             (this.membershipInfo!=null &&
              this.membershipInfo.equals(other.getMembershipInfo())));
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
        if (getMembershipInfo() != null) {
            _hashCode += getMembershipInfo().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CreateMemberGroup.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", ">CreateMemberGroup"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("membershipInfo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "membershipInfo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "MembershipData"));
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
