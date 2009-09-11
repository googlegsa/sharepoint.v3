/**
 * InCommonData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.userprofileservice;

public class InCommonData  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.ContactData manager;

    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.ContactData[] colleagues;

    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData[] memberships;

    public InCommonData() {
    }

    public InCommonData(
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.ContactData manager,
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.ContactData[] colleagues,
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData[] memberships) {
           this.manager = manager;
           this.colleagues = colleagues;
           this.memberships = memberships;
    }


    /**
     * Gets the manager value for this InCommonData.
     * 
     * @return manager
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.ContactData getManager() {
        return manager;
    }


    /**
     * Sets the manager value for this InCommonData.
     * 
     * @param manager
     */
    public void setManager(com.google.enterprise.connector.sharepoint.generated.userprofileservice.ContactData manager) {
        this.manager = manager;
    }


    /**
     * Gets the colleagues value for this InCommonData.
     * 
     * @return colleagues
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.ContactData[] getColleagues() {
        return colleagues;
    }


    /**
     * Sets the colleagues value for this InCommonData.
     * 
     * @param colleagues
     */
    public void setColleagues(com.google.enterprise.connector.sharepoint.generated.userprofileservice.ContactData[] colleagues) {
        this.colleagues = colleagues;
    }


    /**
     * Gets the memberships value for this InCommonData.
     * 
     * @return memberships
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData[] getMemberships() {
        return memberships;
    }


    /**
     * Sets the memberships value for this InCommonData.
     * 
     * @param memberships
     */
    public void setMemberships(com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData[] memberships) {
        this.memberships = memberships;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof InCommonData)) return false;
        InCommonData other = (InCommonData) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.manager==null && other.getManager()==null) || 
             (this.manager!=null &&
              this.manager.equals(other.getManager()))) &&
            ((this.colleagues==null && other.getColleagues()==null) || 
             (this.colleagues!=null &&
              java.util.Arrays.equals(this.colleagues, other.getColleagues()))) &&
            ((this.memberships==null && other.getMemberships()==null) || 
             (this.memberships!=null &&
              java.util.Arrays.equals(this.memberships, other.getMemberships())));
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
        if (getManager() != null) {
            _hashCode += getManager().hashCode();
        }
        if (getColleagues() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getColleagues());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getColleagues(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getMemberships() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getMemberships());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getMemberships(), i);
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
        new org.apache.axis.description.TypeDesc(InCommonData.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "InCommonData"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("manager");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "Manager"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "ContactData"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("colleagues");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "Colleagues"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "ContactData"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "ContactData"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("memberships");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "Memberships"));
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
