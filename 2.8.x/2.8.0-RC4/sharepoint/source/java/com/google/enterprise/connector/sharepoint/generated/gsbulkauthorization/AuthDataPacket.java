/**
 * AuthDataPacket.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization;

public class AuthDataPacket  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.AuthData[] authDataArray;

    private com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.Container container;

    private java.lang.String message;

    private boolean isDone;

    public AuthDataPacket() {
    }

    public AuthDataPacket(
           com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.AuthData[] authDataArray,
           com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.Container container,
           java.lang.String message,
           boolean isDone) {
           this.authDataArray = authDataArray;
           this.container = container;
           this.message = message;
           this.isDone = isDone;
    }


    /**
     * Gets the authDataArray value for this AuthDataPacket.
     * 
     * @return authDataArray
     */
    public com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.AuthData[] getAuthDataArray() {
        return authDataArray;
    }


    /**
     * Sets the authDataArray value for this AuthDataPacket.
     * 
     * @param authDataArray
     */
    public void setAuthDataArray(com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.AuthData[] authDataArray) {
        this.authDataArray = authDataArray;
    }


    /**
     * Gets the container value for this AuthDataPacket.
     * 
     * @return container
     */
    public com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.Container getContainer() {
        return container;
    }


    /**
     * Sets the container value for this AuthDataPacket.
     * 
     * @param container
     */
    public void setContainer(com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.Container container) {
        this.container = container;
    }


    /**
     * Gets the message value for this AuthDataPacket.
     * 
     * @return message
     */
    public java.lang.String getMessage() {
        return message;
    }


    /**
     * Sets the message value for this AuthDataPacket.
     * 
     * @param message
     */
    public void setMessage(java.lang.String message) {
        this.message = message;
    }


    /**
     * Gets the isDone value for this AuthDataPacket.
     * 
     * @return isDone
     */
    public boolean isIsDone() {
        return isDone;
    }


    /**
     * Sets the isDone value for this AuthDataPacket.
     * 
     * @param isDone
     */
    public void setIsDone(boolean isDone) {
        this.isDone = isDone;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AuthDataPacket)) return false;
        AuthDataPacket other = (AuthDataPacket) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.authDataArray==null && other.getAuthDataArray()==null) || 
             (this.authDataArray!=null &&
              java.util.Arrays.equals(this.authDataArray, other.getAuthDataArray()))) &&
            ((this.container==null && other.getContainer()==null) || 
             (this.container!=null &&
              this.container.equals(other.getContainer()))) &&
            ((this.message==null && other.getMessage()==null) || 
             (this.message!=null &&
              this.message.equals(other.getMessage()))) &&
            this.isDone == other.isIsDone();
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
        if (getAuthDataArray() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAuthDataArray());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAuthDataArray(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getContainer() != null) {
            _hashCode += getContainer().hashCode();
        }
        if (getMessage() != null) {
            _hashCode += getMessage().hashCode();
        }
        _hashCode += (isIsDone() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AuthDataPacket.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "AuthDataPacket"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("authDataArray");
        elemField.setXmlName(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "AuthDataArray"));
        elemField.setXmlType(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "AuthData"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "AuthData"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("container");
        elemField.setXmlName(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "Container"));
        elemField.setXmlType(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "Container"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("message");
        elemField.setXmlName(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "Message"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isDone");
        elemField.setXmlName(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "IsDone"));
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
