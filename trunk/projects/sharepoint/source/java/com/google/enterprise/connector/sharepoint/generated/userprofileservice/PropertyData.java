/**
 * PropertyData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.userprofileservice;

public class PropertyData  implements java.io.Serializable {
    private java.lang.String name;

    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy privacy;

    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.ValueData[] values;

    private boolean isPrivacyChanged;

    private boolean isValueChanged;

    public PropertyData() {
    }

    public PropertyData(
           java.lang.String name,
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy privacy,
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.ValueData[] values,
           boolean isPrivacyChanged,
           boolean isValueChanged) {
           this.name = name;
           this.privacy = privacy;
           this.values = values;
           this.isPrivacyChanged = isPrivacyChanged;
           this.isValueChanged = isValueChanged;
    }


    /**
     * Gets the name value for this PropertyData.
     *
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this PropertyData.
     *
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the privacy value for this PropertyData.
     *
     * @return privacy
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy getPrivacy() {
        return privacy;
    }


    /**
     * Sets the privacy value for this PropertyData.
     *
     * @param privacy
     */
    public void setPrivacy(com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy privacy) {
        this.privacy = privacy;
    }


    /**
     * Gets the values value for this PropertyData.
     *
     * @return values
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.ValueData[] getValues() {
        return values;
    }


    /**
     * Sets the values value for this PropertyData.
     *
     * @param values
     */
    public void setValues(com.google.enterprise.connector.sharepoint.generated.userprofileservice.ValueData[] values) {
        this.values = values;
    }


    /**
     * Gets the isPrivacyChanged value for this PropertyData.
     *
     * @return isPrivacyChanged
     */
    public boolean isIsPrivacyChanged() {
        return isPrivacyChanged;
    }


    /**
     * Sets the isPrivacyChanged value for this PropertyData.
     *
     * @param isPrivacyChanged
     */
    public void setIsPrivacyChanged(boolean isPrivacyChanged) {
        this.isPrivacyChanged = isPrivacyChanged;
    }


    /**
     * Gets the isValueChanged value for this PropertyData.
     *
     * @return isValueChanged
     */
    public boolean isIsValueChanged() {
        return isValueChanged;
    }


    /**
     * Sets the isValueChanged value for this PropertyData.
     *
     * @param isValueChanged
     */
    public void setIsValueChanged(boolean isValueChanged) {
        this.isValueChanged = isValueChanged;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof PropertyData)) return false;
        PropertyData other = (PropertyData) obj;
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
            ((this.privacy==null && other.getPrivacy()==null) ||
             (this.privacy!=null &&
              this.privacy.equals(other.getPrivacy()))) &&
            ((this.values==null && other.getValues()==null) ||
             (this.values!=null &&
              java.util.Arrays.equals(this.values, other.getValues()))) &&
            this.isPrivacyChanged == other.isIsPrivacyChanged() &&
            this.isValueChanged == other.isIsValueChanged();
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
        if (getPrivacy() != null) {
            _hashCode += getPrivacy().hashCode();
        }
        if (getValues() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getValues());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getValues(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        _hashCode += (isIsPrivacyChanged() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += (isIsValueChanged() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(PropertyData.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "PropertyData"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "Name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("privacy");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "Privacy"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "Privacy"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("values");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "Values"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "ValueData"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "ValueData"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isPrivacyChanged");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "IsPrivacyChanged"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isValueChanged");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "IsValueChanged"));
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
