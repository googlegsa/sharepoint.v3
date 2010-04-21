/**
 * GssGetListItemsWithInheritingRoleAssignments.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssacl;

public class GssGetListItemsWithInheritingRoleAssignments  extends com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclBaseResult  implements java.io.Serializable {
    private java.lang.String docXml;

    private boolean moreDocs;

    public GssGetListItemsWithInheritingRoleAssignments() {
    }

    public GssGetListItemsWithInheritingRoleAssignments(
           java.lang.String siteCollectionUrl,
           java.lang.String siteCollectionGuid,
           java.lang.String docXml,
           boolean moreDocs) {
        super(
            siteCollectionUrl,
            siteCollectionGuid);
        this.docXml = docXml;
        this.moreDocs = moreDocs;
    }


    /**
     * Gets the docXml value for this GssGetListItemsWithInheritingRoleAssignments.
     *
     * @return docXml
     */
    public java.lang.String getDocXml() {
        return docXml;
    }


    /**
     * Sets the docXml value for this GssGetListItemsWithInheritingRoleAssignments.
     *
     * @param docXml
     */
    public void setDocXml(java.lang.String docXml) {
        this.docXml = docXml;
    }


    /**
     * Gets the moreDocs value for this GssGetListItemsWithInheritingRoleAssignments.
     *
     * @return moreDocs
     */
    public boolean isMoreDocs() {
        return moreDocs;
    }


    /**
     * Sets the moreDocs value for this GssGetListItemsWithInheritingRoleAssignments.
     *
     * @param moreDocs
     */
    public void setMoreDocs(boolean moreDocs) {
        this.moreDocs = moreDocs;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GssGetListItemsWithInheritingRoleAssignments)) return false;
        GssGetListItemsWithInheritingRoleAssignments other = (GssGetListItemsWithInheritingRoleAssignments) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) &&
            ((this.docXml==null && other.getDocXml()==null) ||
             (this.docXml!=null &&
              this.docXml.equals(other.getDocXml()))) &&
            this.moreDocs == other.isMoreDocs();
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        if (getDocXml() != null) {
            _hashCode += getDocXml().hashCode();
        }
        _hashCode += (isMoreDocs() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GssGetListItemsWithInheritingRoleAssignments.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssGetListItemsWithInheritingRoleAssignments"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("docXml");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "DocXml"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("moreDocs");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "MoreDocs"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
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
