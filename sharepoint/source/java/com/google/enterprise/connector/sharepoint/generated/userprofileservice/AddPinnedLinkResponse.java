/**
 * AddPinnedLinkResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.userprofileservice;

public class AddPinnedLinkResponse  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.userprofileservice.PinnedLinkData addPinnedLinkResult;

    public AddPinnedLinkResponse() {
    }

    public AddPinnedLinkResponse(
           com.google.enterprise.connector.sharepoint.generated.userprofileservice.PinnedLinkData addPinnedLinkResult) {
           this.addPinnedLinkResult = addPinnedLinkResult;
    }


    /**
     * Gets the addPinnedLinkResult value for this AddPinnedLinkResponse.
     * 
     * @return addPinnedLinkResult
     */
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.PinnedLinkData getAddPinnedLinkResult() {
        return addPinnedLinkResult;
    }


    /**
     * Sets the addPinnedLinkResult value for this AddPinnedLinkResponse.
     * 
     * @param addPinnedLinkResult
     */
    public void setAddPinnedLinkResult(com.google.enterprise.connector.sharepoint.generated.userprofileservice.PinnedLinkData addPinnedLinkResult) {
        this.addPinnedLinkResult = addPinnedLinkResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AddPinnedLinkResponse)) return false;
        AddPinnedLinkResponse other = (AddPinnedLinkResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.addPinnedLinkResult==null && other.getAddPinnedLinkResult()==null) || 
             (this.addPinnedLinkResult!=null &&
              this.addPinnedLinkResult.equals(other.getAddPinnedLinkResult())));
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
        if (getAddPinnedLinkResult() != null) {
            _hashCode += getAddPinnedLinkResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AddPinnedLinkResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", ">AddPinnedLinkResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("addPinnedLinkResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "AddPinnedLinkResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService", "PinnedLinkData"));
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
