/**
 * AuthData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization;

public class AuthData  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.Container container;

    private java.lang.String itemId;

    private boolean isAllowed;

    private java.lang.String message;

    private java.lang.String complexDocId;

    private boolean isDone;

    private com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.EntityType type;

    public AuthData() {
    }

    public AuthData(
           com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.Container container,
           java.lang.String itemId,
           boolean isAllowed,
           java.lang.String message,
           java.lang.String complexDocId,
           boolean isDone,
           com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.EntityType type) {
           this.container = container;
           this.itemId = itemId;
           this.isAllowed = isAllowed;
           this.message = message;
           this.complexDocId = complexDocId;
           this.isDone = isDone;
           this.type = type;
    }


    /**
     * Gets the container value for this AuthData.
     * 
     * @return container
     */
    public com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.Container getContainer() {
        return container;
    }


    /**
     * Sets the container value for this AuthData.
     * 
     * @param container
     */
    public void setContainer(com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.Container container) {
        this.container = container;
    }


    /**
     * Gets the itemId value for this AuthData.
     * 
     * @return itemId
     */
    public java.lang.String getItemId() {
        return itemId;
    }


    /**
     * Sets the itemId value for this AuthData.
     * 
     * @param itemId
     */
    public void setItemId(java.lang.String itemId) {
        this.itemId = itemId;
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
     * Gets the message value for this AuthData.
     * 
     * @return message
     */
    public java.lang.String getMessage() {
        return message;
    }


    /**
     * Sets the message value for this AuthData.
     * 
     * @param message
     */
    public void setMessage(java.lang.String message) {
        this.message = message;
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


    /**
     * Gets the isDone value for this AuthData.
     * 
     * @return isDone
     */
    public boolean isIsDone() {
        return isDone;
    }


    /**
     * Sets the isDone value for this AuthData.
     * 
     * @param isDone
     */
    public void setIsDone(boolean isDone) {
        this.isDone = isDone;
    }


    /**
     * Gets the type value for this AuthData.
     * 
     * @return type
     */
    public com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.EntityType getType() {
        return type;
    }


    /**
     * Sets the type value for this AuthData.
     * 
     * @param type
     */
    public void setType(com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.EntityType type) {
        this.type = type;
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
            ((this.container==null && other.getContainer()==null) || 
             (this.container!=null &&
              this.container.equals(other.getContainer()))) &&
            ((this.itemId==null && other.getItemId()==null) || 
             (this.itemId!=null &&
              this.itemId.equals(other.getItemId()))) &&
            this.isAllowed == other.isIsAllowed() &&
            ((this.message==null && other.getMessage()==null) || 
             (this.message!=null &&
              this.message.equals(other.getMessage()))) &&
            ((this.complexDocId==null && other.getComplexDocId()==null) || 
             (this.complexDocId!=null &&
              this.complexDocId.equals(other.getComplexDocId()))) &&
            this.isDone == other.isIsDone() &&
            ((this.type==null && other.getType()==null) || 
             (this.type!=null &&
              this.type.equals(other.getType())));
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
        if (getContainer() != null) {
            _hashCode += getContainer().hashCode();
        }
        if (getItemId() != null) {
            _hashCode += getItemId().hashCode();
        }
        _hashCode += (isIsAllowed() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getMessage() != null) {
            _hashCode += getMessage().hashCode();
        }
        if (getComplexDocId() != null) {
            _hashCode += getComplexDocId().hashCode();
        }
        _hashCode += (isIsDone() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getType() != null) {
            _hashCode += getType().hashCode();
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
        elemField.setFieldName("container");
        elemField.setXmlName(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "Container"));
        elemField.setXmlType(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "Container"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("itemId");
        elemField.setXmlName(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "ItemId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isAllowed");
        elemField.setXmlName(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "IsAllowed"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
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
        elemField.setFieldName("complexDocId");
        elemField.setXmlName(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "ComplexDocId"));
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
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("type");
        elemField.setXmlName(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "Type"));
        elemField.setXmlType(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "EntityType"));
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
