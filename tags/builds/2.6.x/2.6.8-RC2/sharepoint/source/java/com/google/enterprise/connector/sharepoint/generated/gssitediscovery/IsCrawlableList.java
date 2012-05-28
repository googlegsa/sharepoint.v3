/**
 * IsCrawlableList.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssitediscovery;

public class IsCrawlableList  implements java.io.Serializable {
    private java.lang.String listGUID;

    public IsCrawlableList() {
    }

    public IsCrawlableList(
           java.lang.String listGUID) {
           this.listGUID = listGUID;
    }


    /**
     * Gets the listGUID value for this IsCrawlableList.
     *
     * @return listGUID
     */
    public java.lang.String getListGUID() {
        return listGUID;
    }


    /**
     * Sets the listGUID value for this IsCrawlableList.
     *
     * @param listGUID
     */
    public void setListGUID(java.lang.String listGUID) {
        this.listGUID = listGUID;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof IsCrawlableList)) return false;
        IsCrawlableList other = (IsCrawlableList) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
            ((this.listGUID==null && other.getListGUID()==null) ||
             (this.listGUID!=null &&
              this.listGUID.equals(other.getListGUID())));
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
        if (getListGUID() != null) {
            _hashCode += getListGUID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(IsCrawlableList.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", ">IsCrawlableList"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("listGUID");
        elemField.setXmlName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "listGUID"));
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
