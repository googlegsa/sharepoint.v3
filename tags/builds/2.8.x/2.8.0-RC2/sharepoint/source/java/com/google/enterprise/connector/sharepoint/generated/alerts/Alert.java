/**
 * Alert.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.alerts;

public class Alert  implements java.io.Serializable {
    private java.lang.String id;

    private java.lang.String title;

    private boolean active;

    private java.lang.String eventType;

    private java.lang.String alertForTitle;

    private java.lang.String alertForUrl;

    private java.lang.String editAlertUrl;

    private com.google.enterprise.connector.sharepoint.generated.alerts.DeliveryChannel[] deliveryChannels;

    public Alert() {
    }

    public Alert(
           java.lang.String id,
           java.lang.String title,
           boolean active,
           java.lang.String eventType,
           java.lang.String alertForTitle,
           java.lang.String alertForUrl,
           java.lang.String editAlertUrl,
           com.google.enterprise.connector.sharepoint.generated.alerts.DeliveryChannel[] deliveryChannels) {
           this.id = id;
           this.title = title;
           this.active = active;
           this.eventType = eventType;
           this.alertForTitle = alertForTitle;
           this.alertForUrl = alertForUrl;
           this.editAlertUrl = editAlertUrl;
           this.deliveryChannels = deliveryChannels;
    }


    /**
     * Gets the id value for this Alert.
     *
     * @return id
     */
    public java.lang.String getId() {
        return id;
    }


    /**
     * Sets the id value for this Alert.
     *
     * @param id
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }


    /**
     * Gets the title value for this Alert.
     *
     * @return title
     */
    public java.lang.String getTitle() {
        return title;
    }


    /**
     * Sets the title value for this Alert.
     *
     * @param title
     */
    public void setTitle(java.lang.String title) {
        this.title = title;
    }


    /**
     * Gets the active value for this Alert.
     *
     * @return active
     */
    public boolean isActive() {
        return active;
    }


    /**
     * Sets the active value for this Alert.
     *
     * @param active
     */
    public void setActive(boolean active) {
        this.active = active;
    }


    /**
     * Gets the eventType value for this Alert.
     *
     * @return eventType
     */
    public java.lang.String getEventType() {
        return eventType;
    }


    /**
     * Sets the eventType value for this Alert.
     *
     * @param eventType
     */
    public void setEventType(java.lang.String eventType) {
        this.eventType = eventType;
    }


    /**
     * Gets the alertForTitle value for this Alert.
     *
     * @return alertForTitle
     */
    public java.lang.String getAlertForTitle() {
        return alertForTitle;
    }


    /**
     * Sets the alertForTitle value for this Alert.
     *
     * @param alertForTitle
     */
    public void setAlertForTitle(java.lang.String alertForTitle) {
        this.alertForTitle = alertForTitle;
    }


    /**
     * Gets the alertForUrl value for this Alert.
     *
     * @return alertForUrl
     */
    public java.lang.String getAlertForUrl() {
        return alertForUrl;
    }


    /**
     * Sets the alertForUrl value for this Alert.
     *
     * @param alertForUrl
     */
    public void setAlertForUrl(java.lang.String alertForUrl) {
        this.alertForUrl = alertForUrl;
    }


    /**
     * Gets the editAlertUrl value for this Alert.
     *
     * @return editAlertUrl
     */
    public java.lang.String getEditAlertUrl() {
        return editAlertUrl;
    }


    /**
     * Sets the editAlertUrl value for this Alert.
     *
     * @param editAlertUrl
     */
    public void setEditAlertUrl(java.lang.String editAlertUrl) {
        this.editAlertUrl = editAlertUrl;
    }


    /**
     * Gets the deliveryChannels value for this Alert.
     *
     * @return deliveryChannels
     */
    public com.google.enterprise.connector.sharepoint.generated.alerts.DeliveryChannel[] getDeliveryChannels() {
        return deliveryChannels;
    }


    /**
     * Sets the deliveryChannels value for this Alert.
     *
     * @param deliveryChannels
     */
    public void setDeliveryChannels(com.google.enterprise.connector.sharepoint.generated.alerts.DeliveryChannel[] deliveryChannels) {
        this.deliveryChannels = deliveryChannels;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Alert)) return false;
        Alert other = (Alert) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
            ((this.id==null && other.getId()==null) ||
             (this.id!=null &&
              this.id.equals(other.getId()))) &&
            ((this.title==null && other.getTitle()==null) ||
             (this.title!=null &&
              this.title.equals(other.getTitle()))) &&
            this.active == other.isActive() &&
            ((this.eventType==null && other.getEventType()==null) ||
             (this.eventType!=null &&
              this.eventType.equals(other.getEventType()))) &&
            ((this.alertForTitle==null && other.getAlertForTitle()==null) ||
             (this.alertForTitle!=null &&
              this.alertForTitle.equals(other.getAlertForTitle()))) &&
            ((this.alertForUrl==null && other.getAlertForUrl()==null) ||
             (this.alertForUrl!=null &&
              this.alertForUrl.equals(other.getAlertForUrl()))) &&
            ((this.editAlertUrl==null && other.getEditAlertUrl()==null) ||
             (this.editAlertUrl!=null &&
              this.editAlertUrl.equals(other.getEditAlertUrl()))) &&
            ((this.deliveryChannels==null && other.getDeliveryChannels()==null) ||
             (this.deliveryChannels!=null &&
              java.util.Arrays.equals(this.deliveryChannels, other.getDeliveryChannels())));
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
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        if (getTitle() != null) {
            _hashCode += getTitle().hashCode();
        }
        _hashCode += (isActive() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getEventType() != null) {
            _hashCode += getEventType().hashCode();
        }
        if (getAlertForTitle() != null) {
            _hashCode += getAlertForTitle().hashCode();
        }
        if (getAlertForUrl() != null) {
            _hashCode += getAlertForUrl().hashCode();
        }
        if (getEditAlertUrl() != null) {
            _hashCode += getEditAlertUrl().hashCode();
        }
        if (getDeliveryChannels() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getDeliveryChannels());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getDeliveryChannels(), i);
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
        new org.apache.axis.description.TypeDesc(Alert.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "Alert"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "Id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("title");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "Title"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("active");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "Active"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("eventType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "EventType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("alertForTitle");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "AlertForTitle"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("alertForUrl");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "AlertForUrl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("editAlertUrl");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "EditAlertUrl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("deliveryChannels");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "DeliveryChannels"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "DeliveryChannel"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "DeliveryChannel"));
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
