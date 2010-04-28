/**
 * GssPrincipal.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssacl;

public class GssPrincipal  implements java.io.Serializable {
    private java.lang.String name;

    private com.google.enterprise.connector.sharepoint.generated.gssacl.PrincipalType type;

    private com.google.enterprise.connector.sharepoint.generated.gssacl.GssPrincipal[] members;

    private com.google.enterprise.connector.sharepoint.generated.gssacl.StringBuilder logMessage;

    public GssPrincipal() {
    }

    public GssPrincipal(
           java.lang.String name,
           com.google.enterprise.connector.sharepoint.generated.gssacl.PrincipalType type,
           com.google.enterprise.connector.sharepoint.generated.gssacl.GssPrincipal[] members,
           com.google.enterprise.connector.sharepoint.generated.gssacl.StringBuilder logMessage) {
           this.name = name;
           this.type = type;
           this.members = members;
           this.logMessage = logMessage;
    }


    /**
     * Gets the name value for this GssPrincipal.
     *
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this GssPrincipal.
     *
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the type value for this GssPrincipal.
     *
     * @return type
     */
    public com.google.enterprise.connector.sharepoint.generated.gssacl.PrincipalType getType() {
        return type;
    }


    /**
     * Sets the type value for this GssPrincipal.
     *
     * @param type
     */
    public void setType(com.google.enterprise.connector.sharepoint.generated.gssacl.PrincipalType type) {
        this.type = type;
    }


    /**
     * Gets the members value for this GssPrincipal.
     *
     * @return members
     */
    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssPrincipal[] getMembers() {
        return members;
    }


    /**
     * Sets the members value for this GssPrincipal.
     *
     * @param members
     */
    public void setMembers(com.google.enterprise.connector.sharepoint.generated.gssacl.GssPrincipal[] members) {
        this.members = members;
    }


    /**
     * Gets the logMessage value for this GssPrincipal.
     *
     * @return logMessage
     */
    public com.google.enterprise.connector.sharepoint.generated.gssacl.StringBuilder getLogMessage() {
        return logMessage;
    }


    /**
     * Sets the logMessage value for this GssPrincipal.
     *
     * @param logMessage
     */
    public void setLogMessage(com.google.enterprise.connector.sharepoint.generated.gssacl.StringBuilder logMessage) {
        this.logMessage = logMessage;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GssPrincipal)) return false;
        GssPrincipal other = (GssPrincipal) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
            ((this.name==null && other.getName()==null) ||
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.type==null && other.getType()==null) ||
             (this.type!=null &&
              this.type.equals(other.getType()))) &&
            ((this.members==null && other.getMembers()==null) ||
             (this.members!=null &&
              java.util.Arrays.equals(this.members, other.getMembers()))) &&
            ((this.logMessage==null && other.getLogMessage()==null) ||
             (this.logMessage!=null &&
              this.logMessage.equals(other.getLogMessage())));
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
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getType() != null) {
            _hashCode += getType().hashCode();
        }
        if (getMembers() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getMembers());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getMembers(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getLogMessage() != null) {
            _hashCode += getLogMessage().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GssPrincipal.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssPrincipal"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "Name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("type");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "Type"));
        elemField.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "PrincipalType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("members");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "Members"));
        elemField.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssPrincipal"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssPrincipal"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("logMessage");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "LogMessage"));
        elemField.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "StringBuilder"));
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
