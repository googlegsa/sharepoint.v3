/**
 * MemberGroupData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.userprofileservice;

public class MemberGroupData  implements java.io.Serializable {
    private java.lang.String sourceInternal;

    private java.lang.String sourceReference;

    public MemberGroupData() {
    }

    public MemberGroupData(
           java.lang.String sourceInternal,
           java.lang.String sourceReference) {
           this.sourceInternal = sourceInternal;
           this.sourceReference = sourceReference;
    }


    /**
     * Gets the sourceInternal value for this MemberGroupData.
     * 
     * @return sourceInternal
     */
    public java.lang.String getSourceInternal() {
        return sourceInternal;
    }


    /**
     * Sets the sourceInternal value for this MemberGroupData.
     * 
     * @param sourceInternal
     */
    public void setSourceInternal(java.lang.String sourceInternal) {
        this.sourceInternal = sourceInternal;
    }


    /**
     * Gets the sourceReference value for this MemberGroupData.
     * 
     * @return sourceReference
     */
    public java.lang.String getSourceReference() {
        return sourceReference;
    }


    /**
     * Sets the sourceReference value for this MemberGroupData.
     * 
     * @param sourceReference
     */
    public void setSourceReference(java.lang.String sourceReference) {
        this.sourceReference = sourceReference;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof MemberGroupData)) return false;
        MemberGroupData other = (MemberGroupData) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.sourceInternal==null && other.getSourceInternal()==null) || 
             (this.sourceInternal!=null &&
              this.sourceInternal.equals(other.getSourceInternal()))) &&
            ((this.sourceReference==null && other.getSourceReference()==null) || 
             (this.sourceReference!=null &&
              this.sourceReference.equals(other.getSourceReference())));
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
        if (getSourceInternal() != null) {
            _hashCode += getSourceInternal().hashCode();
        }
        if (getSourceReference() != null) {
            _hashCode += getSourceReference().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(MemberGroupData.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "MemberGroupData"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sourceInternal");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "SourceInternal"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sourceReference");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "SourceReference"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
