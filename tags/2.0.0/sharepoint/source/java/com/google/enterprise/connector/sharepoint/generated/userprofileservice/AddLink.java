/**
 * AddLink.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.userprofileservice;

public class AddLink  implements java.io.Serializable {
    private java.lang.String accountName;

    private java.lang.String name;

    private java.lang.String url;

    private java.lang.String group;

    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy privacy;

    public AddLink() {
    }

    public AddLink(
           java.lang.String accountName,
           java.lang.String name,
           java.lang.String url,
           java.lang.String group,
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy privacy) {
           this.accountName = accountName;
           this.name = name;
           this.url = url;
           this.group = group;
           this.privacy = privacy;
    }


    /**
     * Gets the accountName value for this AddLink.
     * 
     * @return accountName
     */
    public java.lang.String getAccountName() {
        return accountName;
    }


    /**
     * Sets the accountName value for this AddLink.
     * 
     * @param accountName
     */
    public void setAccountName(java.lang.String accountName) {
        this.accountName = accountName;
    }


    /**
     * Gets the name value for this AddLink.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this AddLink.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the url value for this AddLink.
     * 
     * @return url
     */
    public java.lang.String getUrl() {
        return url;
    }


    /**
     * Sets the url value for this AddLink.
     * 
     * @param url
     */
    public void setUrl(java.lang.String url) {
        this.url = url;
    }


    /**
     * Gets the group value for this AddLink.
     * 
     * @return group
     */
    public java.lang.String getGroup() {
        return group;
    }


    /**
     * Sets the group value for this AddLink.
     * 
     * @param group
     */
    public void setGroup(java.lang.String group) {
        this.group = group;
    }


    /**
     * Gets the privacy value for this AddLink.
     * 
     * @return privacy
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy getPrivacy() {
        return privacy;
    }


    /**
     * Sets the privacy value for this AddLink.
     * 
     * @param privacy
     */
    public void setPrivacy(com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy privacy) {
        this.privacy = privacy;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AddLink)) return false;
        AddLink other = (AddLink) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.accountName==null && other.getAccountName()==null) || 
             (this.accountName!=null &&
              this.accountName.equals(other.getAccountName()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.url==null && other.getUrl()==null) || 
             (this.url!=null &&
              this.url.equals(other.getUrl()))) &&
            ((this.group==null && other.getGroup()==null) || 
             (this.group!=null &&
              this.group.equals(other.getGroup()))) &&
            ((this.privacy==null && other.getPrivacy()==null) || 
             (this.privacy!=null &&
              this.privacy.equals(other.getPrivacy())));
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
        if (getAccountName() != null) {
            _hashCode += getAccountName().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getUrl() != null) {
            _hashCode += getUrl().hashCode();
        }
        if (getGroup() != null) {
            _hashCode += getGroup().hashCode();
        }
        if (getPrivacy() != null) {
            _hashCode += getPrivacy().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AddLink.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", ">AddLink"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("accountName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "accountName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("url");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "url"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("group");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "group"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("privacy");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "privacy"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "Privacy"));
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
