/**
 * GssSharepointPermission.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssacl;

public class GssSharepointPermission  implements java.io.Serializable {
    private java.lang.String[] grantRightMask;

    private java.lang.String[] denyRightMask;

    public GssSharepointPermission() {
    }

    public GssSharepointPermission(
           java.lang.String[] grantRightMask,
           java.lang.String[] denyRightMask) {
           this.grantRightMask = grantRightMask;
           this.denyRightMask = denyRightMask;
    }


    /**
     * Gets the grantRightMask value for this GssSharepointPermission.
     *
     * @return grantRightMask
     */
    public java.lang.String[] getGrantRightMask() {
        return grantRightMask;
    }


    /**
     * Sets the grantRightMask value for this GssSharepointPermission.
     *
     * @param grantRightMask
     */
    public void setGrantRightMask(java.lang.String[] grantRightMask) {
        this.grantRightMask = grantRightMask;
    }


    /**
     * Gets the denyRightMask value for this GssSharepointPermission.
     *
     * @return denyRightMask
     */
    public java.lang.String[] getDenyRightMask() {
        return denyRightMask;
    }


    /**
     * Sets the denyRightMask value for this GssSharepointPermission.
     *
     * @param denyRightMask
     */
    public void setDenyRightMask(java.lang.String[] denyRightMask) {
        this.denyRightMask = denyRightMask;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GssSharepointPermission)) return false;
        GssSharepointPermission other = (GssSharepointPermission) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
            ((this.grantRightMask==null && other.getGrantRightMask()==null) ||
             (this.grantRightMask!=null &&
              java.util.Arrays.equals(this.grantRightMask, other.getGrantRightMask()))) &&
            ((this.denyRightMask==null && other.getDenyRightMask()==null) ||
             (this.denyRightMask!=null &&
              java.util.Arrays.equals(this.denyRightMask, other.getDenyRightMask())));
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
        if (getGrantRightMask() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getGrantRightMask());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getGrantRightMask(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getDenyRightMask() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getDenyRightMask());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getDenyRightMask(), i);
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
        new org.apache.axis.description.TypeDesc(GssSharepointPermission.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssSharepointPermission"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("grantRightMask");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GrantRightMask"));
        elemField.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "SPBasePermissions"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("denyRightMask");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "DenyRightMask"));
        elemField.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "SPBasePermissions"));
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
