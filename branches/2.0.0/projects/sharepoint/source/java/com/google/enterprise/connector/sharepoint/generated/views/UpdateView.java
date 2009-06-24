/**
 * UpdateView.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.views;

public class UpdateView  implements java.io.Serializable {
    private java.lang.String listName;

    private java.lang.String viewName;

    private com.google.enterprise.connector.sharepoint.generated.views.UpdateViewViewProperties viewProperties;

    private com.google.enterprise.connector.sharepoint.generated.views.UpdateViewQuery query;

    private com.google.enterprise.connector.sharepoint.generated.views.UpdateViewViewFields viewFields;

    private com.google.enterprise.connector.sharepoint.generated.views.UpdateViewAggregations aggregations;

    private com.google.enterprise.connector.sharepoint.generated.views.UpdateViewFormats formats;

    private com.google.enterprise.connector.sharepoint.generated.views.UpdateViewRowLimit rowLimit;

    public UpdateView() {
    }

    public UpdateView(
           java.lang.String listName,
           java.lang.String viewName,
           com.google.enterprise.connector.sharepoint.generated.views.UpdateViewViewProperties viewProperties,
           com.google.enterprise.connector.sharepoint.generated.views.UpdateViewQuery query,
           com.google.enterprise.connector.sharepoint.generated.views.UpdateViewViewFields viewFields,
           com.google.enterprise.connector.sharepoint.generated.views.UpdateViewAggregations aggregations,
           com.google.enterprise.connector.sharepoint.generated.views.UpdateViewFormats formats,
           com.google.enterprise.connector.sharepoint.generated.views.UpdateViewRowLimit rowLimit) {
           this.listName = listName;
           this.viewName = viewName;
           this.viewProperties = viewProperties;
           this.query = query;
           this.viewFields = viewFields;
           this.aggregations = aggregations;
           this.formats = formats;
           this.rowLimit = rowLimit;
    }


    /**
     * Gets the listName value for this UpdateView.
     * 
     * @return listName
     */
    public java.lang.String getListName() {
        return listName;
    }


    /**
     * Sets the listName value for this UpdateView.
     * 
     * @param listName
     */
    public void setListName(java.lang.String listName) {
        this.listName = listName;
    }


    /**
     * Gets the viewName value for this UpdateView.
     * 
     * @return viewName
     */
    public java.lang.String getViewName() {
        return viewName;
    }


    /**
     * Sets the viewName value for this UpdateView.
     * 
     * @param viewName
     */
    public void setViewName(java.lang.String viewName) {
        this.viewName = viewName;
    }


    /**
     * Gets the viewProperties value for this UpdateView.
     * 
     * @return viewProperties
     */
    public com.google.enterprise.connector.sharepoint.generated.views.UpdateViewViewProperties getViewProperties() {
        return viewProperties;
    }


    /**
     * Sets the viewProperties value for this UpdateView.
     * 
     * @param viewProperties
     */
    public void setViewProperties(com.google.enterprise.connector.sharepoint.generated.views.UpdateViewViewProperties viewProperties) {
        this.viewProperties = viewProperties;
    }


    /**
     * Gets the query value for this UpdateView.
     * 
     * @return query
     */
    public com.google.enterprise.connector.sharepoint.generated.views.UpdateViewQuery getQuery() {
        return query;
    }


    /**
     * Sets the query value for this UpdateView.
     * 
     * @param query
     */
    public void setQuery(com.google.enterprise.connector.sharepoint.generated.views.UpdateViewQuery query) {
        this.query = query;
    }


    /**
     * Gets the viewFields value for this UpdateView.
     * 
     * @return viewFields
     */
    public com.google.enterprise.connector.sharepoint.generated.views.UpdateViewViewFields getViewFields() {
        return viewFields;
    }


    /**
     * Sets the viewFields value for this UpdateView.
     * 
     * @param viewFields
     */
    public void setViewFields(com.google.enterprise.connector.sharepoint.generated.views.UpdateViewViewFields viewFields) {
        this.viewFields = viewFields;
    }


    /**
     * Gets the aggregations value for this UpdateView.
     * 
     * @return aggregations
     */
    public com.google.enterprise.connector.sharepoint.generated.views.UpdateViewAggregations getAggregations() {
        return aggregations;
    }


    /**
     * Sets the aggregations value for this UpdateView.
     * 
     * @param aggregations
     */
    public void setAggregations(com.google.enterprise.connector.sharepoint.generated.views.UpdateViewAggregations aggregations) {
        this.aggregations = aggregations;
    }


    /**
     * Gets the formats value for this UpdateView.
     * 
     * @return formats
     */
    public com.google.enterprise.connector.sharepoint.generated.views.UpdateViewFormats getFormats() {
        return formats;
    }


    /**
     * Sets the formats value for this UpdateView.
     * 
     * @param formats
     */
    public void setFormats(com.google.enterprise.connector.sharepoint.generated.views.UpdateViewFormats formats) {
        this.formats = formats;
    }


    /**
     * Gets the rowLimit value for this UpdateView.
     * 
     * @return rowLimit
     */
    public com.google.enterprise.connector.sharepoint.generated.views.UpdateViewRowLimit getRowLimit() {
        return rowLimit;
    }


    /**
     * Sets the rowLimit value for this UpdateView.
     * 
     * @param rowLimit
     */
    public void setRowLimit(com.google.enterprise.connector.sharepoint.generated.views.UpdateViewRowLimit rowLimit) {
        this.rowLimit = rowLimit;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UpdateView)) return false;
        UpdateView other = (UpdateView) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.listName==null && other.getListName()==null) || 
             (this.listName!=null &&
              this.listName.equals(other.getListName()))) &&
            ((this.viewName==null && other.getViewName()==null) || 
             (this.viewName!=null &&
              this.viewName.equals(other.getViewName()))) &&
            ((this.viewProperties==null && other.getViewProperties()==null) || 
             (this.viewProperties!=null &&
              this.viewProperties.equals(other.getViewProperties()))) &&
            ((this.query==null && other.getQuery()==null) || 
             (this.query!=null &&
              this.query.equals(other.getQuery()))) &&
            ((this.viewFields==null && other.getViewFields()==null) || 
             (this.viewFields!=null &&
              this.viewFields.equals(other.getViewFields()))) &&
            ((this.aggregations==null && other.getAggregations()==null) || 
             (this.aggregations!=null &&
              this.aggregations.equals(other.getAggregations()))) &&
            ((this.formats==null && other.getFormats()==null) || 
             (this.formats!=null &&
              this.formats.equals(other.getFormats()))) &&
            ((this.rowLimit==null && other.getRowLimit()==null) || 
             (this.rowLimit!=null &&
              this.rowLimit.equals(other.getRowLimit())));
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
        if (getListName() != null) {
            _hashCode += getListName().hashCode();
        }
        if (getViewName() != null) {
            _hashCode += getViewName().hashCode();
        }
        if (getViewProperties() != null) {
            _hashCode += getViewProperties().hashCode();
        }
        if (getQuery() != null) {
            _hashCode += getQuery().hashCode();
        }
        if (getViewFields() != null) {
            _hashCode += getViewFields().hashCode();
        }
        if (getAggregations() != null) {
            _hashCode += getAggregations().hashCode();
        }
        if (getFormats() != null) {
            _hashCode += getFormats().hashCode();
        }
        if (getRowLimit() != null) {
            _hashCode += getRowLimit().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(UpdateView.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">UpdateView"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("listName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("viewName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "viewName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("viewProperties");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "viewProperties"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateView>viewProperties"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("query");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "query"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateView>query"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("viewFields");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "viewFields"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateView>viewFields"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("aggregations");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "aggregations"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateView>aggregations"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("formats");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "formats"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateView>formats"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("rowLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "rowLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateView>rowLimit"));
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
