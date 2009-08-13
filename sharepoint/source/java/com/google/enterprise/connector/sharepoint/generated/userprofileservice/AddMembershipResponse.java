/**
 * AddMembershipResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.userprofileservice;

public class AddMembershipResponse  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData addMembershipResult;

    public AddMembershipResponse() {
    }

    public AddMembershipResponse(
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData addMembershipResult) {
           this.addMembershipResult = addMembershipResult;
    }


    /**
     * Gets the addMembershipResult value for this AddMembershipResponse.
     * 
     * @return addMembershipResult
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData getAddMembershipResult() {
        return addMembershipResult;
    }


    /**
     * Sets the addMembershipResult value for this AddMembershipResponse.
     * 
     * @param addMembershipResult
     */
    public void setAddMembershipResult(com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData addMembershipResult) {
        this.addMembershipResult = addMembershipResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AddMembershipResponse)) return false;
        AddMembershipResponse other = (AddMembershipResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.addMembershipResult==null && other.getAddMembershipResult()==null) || 
             (this.addMembershipResult!=null &&
              this.addMembershipResult.equals(other.getAddMembershipResult())));
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
        if (getAddMembershipResult() != null) {
            _hashCode += getAddMembershipResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AddMembershipResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", ">AddMembershipResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("addMembershipResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "AddMembershipResult"));
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
