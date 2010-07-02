/**
 * MembershipData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.userprofileservice;

public class MembershipData  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipGroupType groupType;

    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipSource source;

    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy privacy;

    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.MemberGroupData memberGroup;

    private java.lang.String displayName;

    private java.lang.String mailNickname;

    private java.lang.String url;

    private long ID;

    private long memberGroupID;

    private java.lang.String group;

    public MembershipData() {
    }

    public MembershipData(
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipGroupType groupType,
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipSource source,
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy privacy,
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.MemberGroupData memberGroup,
           java.lang.String displayName,
           java.lang.String mailNickname,
           java.lang.String url,
           long ID,
           long memberGroupID,
           java.lang.String group) {
           this.groupType = groupType;
           this.source = source;
           this.privacy = privacy;
           this.memberGroup = memberGroup;
           this.displayName = displayName;
           this.mailNickname = mailNickname;
           this.url = url;
           this.ID = ID;
           this.memberGroupID = memberGroupID;
           this.group = group;
    }


    /**
     * Gets the groupType value for this MembershipData.
     *
     * @return groupType
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipGroupType getGroupType() {
        return groupType;
    }


    /**
     * Sets the groupType value for this MembershipData.
     *
     * @param groupType
     */
    public void setGroupType(com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipGroupType groupType) {
        this.groupType = groupType;
    }


    /**
     * Gets the source value for this MembershipData.
     *
     * @return source
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipSource getSource() {
        return source;
    }


    /**
     * Sets the source value for this MembershipData.
     *
     * @param source
     */
    public void setSource(com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipSource source) {
        this.source = source;
    }


    /**
     * Gets the privacy value for this MembershipData.
     *
     * @return privacy
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy getPrivacy() {
        return privacy;
    }


    /**
     * Sets the privacy value for this MembershipData.
     *
     * @param privacy
     */
    public void setPrivacy(com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy privacy) {
        this.privacy = privacy;
    }


    /**
     * Gets the memberGroup value for this MembershipData.
     *
     * @return memberGroup
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.MemberGroupData getMemberGroup() {
        return memberGroup;
    }


    /**
     * Sets the memberGroup value for this MembershipData.
     *
     * @param memberGroup
     */
    public void setMemberGroup(com.google.enterprise.connector.sharepoint.generated.userprofileservice.MemberGroupData memberGroup) {
        this.memberGroup = memberGroup;
    }


    /**
     * Gets the displayName value for this MembershipData.
     *
     * @return displayName
     */
    public java.lang.String getDisplayName() {
        return displayName;
    }


    /**
     * Sets the displayName value for this MembershipData.
     *
     * @param displayName
     */
    public void setDisplayName(java.lang.String displayName) {
        this.displayName = displayName;
    }


    /**
     * Gets the mailNickname value for this MembershipData.
     *
     * @return mailNickname
     */
    public java.lang.String getMailNickname() {
        return mailNickname;
    }


    /**
     * Sets the mailNickname value for this MembershipData.
     *
     * @param mailNickname
     */
    public void setMailNickname(java.lang.String mailNickname) {
        this.mailNickname = mailNickname;
    }


    /**
     * Gets the url value for this MembershipData.
     *
     * @return url
     */
    public java.lang.String getUrl() {
        return url;
    }


    /**
     * Sets the url value for this MembershipData.
     *
     * @param url
     */
    public void setUrl(java.lang.String url) {
        this.url = url;
    }


    /**
     * Gets the ID value for this MembershipData.
     *
     * @return ID
     */
    public long getID() {
        return ID;
    }


    /**
     * Sets the ID value for this MembershipData.
     *
     * @param ID
     */
    public void setID(long ID) {
        this.ID = ID;
    }


    /**
     * Gets the memberGroupID value for this MembershipData.
     *
     * @return memberGroupID
     */
    public long getMemberGroupID() {
        return memberGroupID;
    }


    /**
     * Sets the memberGroupID value for this MembershipData.
     *
     * @param memberGroupID
     */
    public void setMemberGroupID(long memberGroupID) {
        this.memberGroupID = memberGroupID;
    }


    /**
     * Gets the group value for this MembershipData.
     *
     * @return group
     */
    public java.lang.String getGroup() {
        return group;
    }


    /**
     * Sets the group value for this MembershipData.
     *
     * @param group
     */
    public void setGroup(java.lang.String group) {
        this.group = group;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof MembershipData)) return false;
        MembershipData other = (MembershipData) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
            ((this.groupType==null && other.getGroupType()==null) ||
             (this.groupType!=null &&
              this.groupType.equals(other.getGroupType()))) &&
            ((this.source==null && other.getSource()==null) ||
             (this.source!=null &&
              this.source.equals(other.getSource()))) &&
            ((this.privacy==null && other.getPrivacy()==null) ||
             (this.privacy!=null &&
              this.privacy.equals(other.getPrivacy()))) &&
            ((this.memberGroup==null && other.getMemberGroup()==null) ||
             (this.memberGroup!=null &&
              this.memberGroup.equals(other.getMemberGroup()))) &&
            ((this.displayName==null && other.getDisplayName()==null) ||
             (this.displayName!=null &&
              this.displayName.equals(other.getDisplayName()))) &&
            ((this.mailNickname==null && other.getMailNickname()==null) ||
             (this.mailNickname!=null &&
              this.mailNickname.equals(other.getMailNickname()))) &&
            ((this.url==null && other.getUrl()==null) ||
             (this.url!=null &&
              this.url.equals(other.getUrl()))) &&
            this.ID == other.getID() &&
            this.memberGroupID == other.getMemberGroupID() &&
            ((this.group==null && other.getGroup()==null) ||
             (this.group!=null &&
              this.group.equals(other.getGroup())));
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
        if (getGroupType() != null) {
            _hashCode += getGroupType().hashCode();
        }
        if (getSource() != null) {
            _hashCode += getSource().hashCode();
        }
        if (getPrivacy() != null) {
            _hashCode += getPrivacy().hashCode();
        }
        if (getMemberGroup() != null) {
            _hashCode += getMemberGroup().hashCode();
        }
        if (getDisplayName() != null) {
            _hashCode += getDisplayName().hashCode();
        }
        if (getMailNickname() != null) {
            _hashCode += getMailNickname().hashCode();
        }
        if (getUrl() != null) {
            _hashCode += getUrl().hashCode();
        }
        _hashCode += new Long(getID()).hashCode();
        _hashCode += new Long(getMemberGroupID()).hashCode();
        if (getGroup() != null) {
            _hashCode += getGroup().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(MembershipData.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "MembershipData"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("groupType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "GroupType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "MembershipGroupType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("source");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "Source"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "MembershipSource"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("privacy");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "Privacy"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "Privacy"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("memberGroup");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "MemberGroup"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "MemberGroupData"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("displayName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "DisplayName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mailNickname");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "MailNickname"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("url");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "Url"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "ID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("memberGroupID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "MemberGroupID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("group");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "Group"));
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
