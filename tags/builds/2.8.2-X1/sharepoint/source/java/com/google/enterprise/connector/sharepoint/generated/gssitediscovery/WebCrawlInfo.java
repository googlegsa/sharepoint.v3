/**
 * WebCrawlInfo.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssitediscovery;

public class WebCrawlInfo  implements java.io.Serializable {
    private java.lang.String webKey;

    private boolean crawlAspxPages;

    private boolean noCrawl;

    private boolean status;

    private java.lang.String error;

    public WebCrawlInfo() {
    }

    public WebCrawlInfo(
           java.lang.String webKey,
           boolean crawlAspxPages,
           boolean noCrawl,
           boolean status,
           java.lang.String error) {
           this.webKey = webKey;
           this.crawlAspxPages = crawlAspxPages;
           this.noCrawl = noCrawl;
           this.status = status;
           this.error = error;
    }


    /**
     * Gets the webKey value for this WebCrawlInfo.
     * 
     * @return webKey
     */
    public java.lang.String getWebKey() {
        return webKey;
    }


    /**
     * Sets the webKey value for this WebCrawlInfo.
     * 
     * @param webKey
     */
    public void setWebKey(java.lang.String webKey) {
        this.webKey = webKey;
    }


    /**
     * Gets the crawlAspxPages value for this WebCrawlInfo.
     * 
     * @return crawlAspxPages
     */
    public boolean isCrawlAspxPages() {
        return crawlAspxPages;
    }


    /**
     * Sets the crawlAspxPages value for this WebCrawlInfo.
     * 
     * @param crawlAspxPages
     */
    public void setCrawlAspxPages(boolean crawlAspxPages) {
        this.crawlAspxPages = crawlAspxPages;
    }


    /**
     * Gets the noCrawl value for this WebCrawlInfo.
     * 
     * @return noCrawl
     */
    public boolean isNoCrawl() {
        return noCrawl;
    }


    /**
     * Sets the noCrawl value for this WebCrawlInfo.
     * 
     * @param noCrawl
     */
    public void setNoCrawl(boolean noCrawl) {
        this.noCrawl = noCrawl;
    }


    /**
     * Gets the status value for this WebCrawlInfo.
     * 
     * @return status
     */
    public boolean isStatus() {
        return status;
    }


    /**
     * Sets the status value for this WebCrawlInfo.
     * 
     * @param status
     */
    public void setStatus(boolean status) {
        this.status = status;
    }


    /**
     * Gets the error value for this WebCrawlInfo.
     * 
     * @return error
     */
    public java.lang.String getError() {
        return error;
    }


    /**
     * Sets the error value for this WebCrawlInfo.
     * 
     * @param error
     */
    public void setError(java.lang.String error) {
        this.error = error;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WebCrawlInfo)) return false;
        WebCrawlInfo other = (WebCrawlInfo) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.webKey==null && other.getWebKey()==null) || 
             (this.webKey!=null &&
              this.webKey.equals(other.getWebKey()))) &&
            this.crawlAspxPages == other.isCrawlAspxPages() &&
            this.noCrawl == other.isNoCrawl() &&
            this.status == other.isStatus() &&
            ((this.error==null && other.getError()==null) || 
             (this.error!=null &&
              this.error.equals(other.getError())));
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
        if (getWebKey() != null) {
            _hashCode += getWebKey().hashCode();
        }
        _hashCode += (isCrawlAspxPages() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += (isNoCrawl() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += (isStatus() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getError() != null) {
            _hashCode += getError().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(WebCrawlInfo.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "WebCrawlInfo"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("webKey");
        elemField.setXmlName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "WebKey"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("crawlAspxPages");
        elemField.setXmlName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "CrawlAspxPages"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("noCrawl");
        elemField.setXmlName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "NoCrawl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "Status"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("error");
        elemField.setXmlName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "Error"));
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
