/**
 * AuthData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization;

public class AuthData  implements java.io.Serializable {
    private java.lang.String listURL;

    private java.lang.String listItemId;

    private boolean isAllowed;

    private java.lang.String error;

    private java.lang.String complexDocId;

    public AuthData() {
    }

    public AuthData(
           java.lang.String listURL,
           java.lang.String listItemId,
           boolean isAllowed,
           java.lang.String error,
           java.lang.String complexDocId) {
           this.listURL = listURL;
           this.listItemId = listItemId;
           this.isAllowed = isAllowed;
           this.error = error;
           this.complexDocId = complexDocId;
    }


    /**
     * Gets the listURL value for this AuthData.
     *
     * @return listURL
     */
    public java.lang.String getListURL() {
        return listURL;
    }


    /**
     * Sets the listURL value for this AuthData.
     *
     * @param listURL
     */
    public void setListURL(java.lang.String listURL) {
        this.listURL = listURL;
    }


    /**
     * Gets the listItemId value for this AuthData.
     *
     * @return listItemId
     */
    public java.lang.String getListItemId() {
        return listItemId;
    }


    /**
     * Sets the listItemId value for this AuthData.
     *
     * @param listItemId
     */
    public void setListItemId(java.lang.String listItemId) {
        this.listItemId = listItemId;
    }


    /**
     * Gets the isAllowed value for this AuthData.
     *
     * @return isAllowed
     */
    public boolean isIsAllowed() {
        return isAllowed;
    }


    /**
     * Sets the isAllowed value for this AuthData.
     *
     * @param isAllowed
     */
    public void setIsAllowed(boolean isAllowed) {
        this.isAllowed = isAllowed;
    }


    /**
     * Gets the error value for this AuthData.
     *
     * @return error
     */
    public java.lang.String getError() {
        return error;
    }


    /**
     * Sets the error value for this AuthData.
     *
     * @param error
     */
    public void setError(java.lang.String error) {
        this.error = error;
    }


    /**
     * Gets the complexDocId value for this AuthData.
     *
     * @return complexDocId
     */
    public java.lang.String getComplexDocId() {
        return complexDocId;
    }


    /**
     * Sets the complexDocId value for this AuthData.
     *
     * @param complexDocId
     */
    public void setComplexDocId(java.lang.String complexDocId) {
        this.complexDocId = complexDocId;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AuthData)) return false;
        AuthData other = (AuthData) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
            ((this.listURL==null && other.getListURL()==null) ||
             (this.listURL!=null &&
              this.listURL.equals(other.getListURL()))) &&
            ((this.listItemId==null && other.getListItemId()==null) ||
             (this.listItemId!=null &&
              this.listItemId.equals(other.getListItemId()))) &&
            this.isAllowed == other.isIsAllowed() &&
            ((this.error==null && other.getError()==null) ||
             (this.error!=null &&
              this.error.equals(other.getError()))) &&
            ((this.complexDocId==null && other.getComplexDocId()==null) ||
             (this.complexDocId!=null &&
              this.complexDocId.equals(other.getComplexDocId())));
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
        if (getListURL() != null) {
            _hashCode += getListURL().hashCode();
        }
        if (getListItemId() != null) {
            _hashCode += getListItemId().hashCode();
        }
        _hashCode += (isIsAllowed() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getError() != null) {
            _hashCode += getError().hashCode();
        }
        if (getComplexDocId() != null) {
            _hashCode += getComplexDocId().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AuthData.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "AuthData"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("listURL");
        elemField.setXmlName(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "listURL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("listItemId");
        elemField.setXmlName(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "listItemId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isAllowed");
        elemField.setXmlName(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "isAllowed"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("error");
        elemField.setXmlName(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "error"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("complexDocId");
        elemField.setXmlName(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "complexDocId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
