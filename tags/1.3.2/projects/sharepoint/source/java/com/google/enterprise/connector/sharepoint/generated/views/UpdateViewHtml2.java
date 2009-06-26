/**
 * UpdateViewHtml2.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.views;

public class UpdateViewHtml2  implements java.io.Serializable {
    private java.lang.String listName;

    private java.lang.String viewName;

    private com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewProperties viewProperties;

    private com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2Toolbar toolbar;

    private com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewHeader viewHeader;

    private com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewBody viewBody;

    private com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewFooter viewFooter;

    private com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewEmpty viewEmpty;

    private com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2RowLimitExceeded rowLimitExceeded;

    private com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2Query query;

    private com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewFields viewFields;

    private com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2Aggregations aggregations;

    private com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2Formats formats;

    private com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2RowLimit rowLimit;

    private java.lang.String openApplicationExtension;

    public UpdateViewHtml2() {
    }

    public UpdateViewHtml2(
           java.lang.String listName,
           java.lang.String viewName,
           com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewProperties viewProperties,
           com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2Toolbar toolbar,
           com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewHeader viewHeader,
           com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewBody viewBody,
           com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewFooter viewFooter,
           com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewEmpty viewEmpty,
           com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2RowLimitExceeded rowLimitExceeded,
           com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2Query query,
           com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewFields viewFields,
           com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2Aggregations aggregations,
           com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2Formats formats,
           com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2RowLimit rowLimit,
           java.lang.String openApplicationExtension) {
           this.listName = listName;
           this.viewName = viewName;
           this.viewProperties = viewProperties;
           this.toolbar = toolbar;
           this.viewHeader = viewHeader;
           this.viewBody = viewBody;
           this.viewFooter = viewFooter;
           this.viewEmpty = viewEmpty;
           this.rowLimitExceeded = rowLimitExceeded;
           this.query = query;
           this.viewFields = viewFields;
           this.aggregations = aggregations;
           this.formats = formats;
           this.rowLimit = rowLimit;
           this.openApplicationExtension = openApplicationExtension;
    }


    /**
     * Gets the listName value for this UpdateViewHtml2.
     * 
     * @return listName
     */
    public java.lang.String getListName() {
        return listName;
    }


    /**
     * Sets the listName value for this UpdateViewHtml2.
     * 
     * @param listName
     */
    public void setListName(java.lang.String listName) {
        this.listName = listName;
    }


    /**
     * Gets the viewName value for this UpdateViewHtml2.
     * 
     * @return viewName
     */
    public java.lang.String getViewName() {
        return viewName;
    }


    /**
     * Sets the viewName value for this UpdateViewHtml2.
     * 
     * @param viewName
     */
    public void setViewName(java.lang.String viewName) {
        this.viewName = viewName;
    }


    /**
     * Gets the viewProperties value for this UpdateViewHtml2.
     * 
     * @return viewProperties
     */
    public com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewProperties getViewProperties() {
        return viewProperties;
    }


    /**
     * Sets the viewProperties value for this UpdateViewHtml2.
     * 
     * @param viewProperties
     */
    public void setViewProperties(com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
    }


    /**
     * Gets the toolbar value for this UpdateViewHtml2.
     * 
     * @return toolbar
     */
    public com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2Toolbar getToolbar() {
        return toolbar;
    }


    /**
     * Sets the toolbar value for this UpdateViewHtml2.
     * 
     * @param toolbar
     */
    public void setToolbar(com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2Toolbar toolbar) {
        this.toolbar = toolbar;
    }


    /**
     * Gets the viewHeader value for this UpdateViewHtml2.
     * 
     * @return viewHeader
     */
    public com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewHeader getViewHeader() {
        return viewHeader;
    }


    /**
     * Sets the viewHeader value for this UpdateViewHtml2.
     * 
     * @param viewHeader
     */
    public void setViewHeader(com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewHeader viewHeader) {
        this.viewHeader = viewHeader;
    }


    /**
     * Gets the viewBody value for this UpdateViewHtml2.
     * 
     * @return viewBody
     */
    public com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewBody getViewBody() {
        return viewBody;
    }


    /**
     * Sets the viewBody value for this UpdateViewHtml2.
     * 
     * @param viewBody
     */
    public void setViewBody(com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewBody viewBody) {
        this.viewBody = viewBody;
    }


    /**
     * Gets the viewFooter value for this UpdateViewHtml2.
     * 
     * @return viewFooter
     */
    public com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewFooter getViewFooter() {
        return viewFooter;
    }


    /**
     * Sets the viewFooter value for this UpdateViewHtml2.
     * 
     * @param viewFooter
     */
    public void setViewFooter(com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewFooter viewFooter) {
        this.viewFooter = viewFooter;
    }


    /**
     * Gets the viewEmpty value for this UpdateViewHtml2.
     * 
     * @return viewEmpty
     */
    public com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewEmpty getViewEmpty() {
        return viewEmpty;
    }


    /**
     * Sets the viewEmpty value for this UpdateViewHtml2.
     * 
     * @param viewEmpty
     */
    public void setViewEmpty(com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewEmpty viewEmpty) {
        this.viewEmpty = viewEmpty;
    }


    /**
     * Gets the rowLimitExceeded value for this UpdateViewHtml2.
     * 
     * @return rowLimitExceeded
     */
    public com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2RowLimitExceeded getRowLimitExceeded() {
        return rowLimitExceeded;
    }


    /**
     * Sets the rowLimitExceeded value for this UpdateViewHtml2.
     * 
     * @param rowLimitExceeded
     */
    public void setRowLimitExceeded(com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2RowLimitExceeded rowLimitExceeded) {
        this.rowLimitExceeded = rowLimitExceeded;
    }


    /**
     * Gets the query value for this UpdateViewHtml2.
     * 
     * @return query
     */
    public com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2Query getQuery() {
        return query;
    }


    /**
     * Sets the query value for this UpdateViewHtml2.
     * 
     * @param query
     */
    public void setQuery(com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2Query query) {
        this.query = query;
    }


    /**
     * Gets the viewFields value for this UpdateViewHtml2.
     * 
     * @return viewFields
     */
    public com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewFields getViewFields() {
        return viewFields;
    }


    /**
     * Sets the viewFields value for this UpdateViewHtml2.
     * 
     * @param viewFields
     */
    public void setViewFields(com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2ViewFields viewFields) {
        this.viewFields = viewFields;
    }


    /**
     * Gets the aggregations value for this UpdateViewHtml2.
     * 
     * @return aggregations
     */
    public com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2Aggregations getAggregations() {
        return aggregations;
    }


    /**
     * Sets the aggregations value for this UpdateViewHtml2.
     * 
     * @param aggregations
     */
    public void setAggregations(com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2Aggregations aggregations) {
        this.aggregations = aggregations;
    }


    /**
     * Gets the formats value for this UpdateViewHtml2.
     * 
     * @return formats
     */
    public com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2Formats getFormats() {
        return formats;
    }


    /**
     * Sets the formats value for this UpdateViewHtml2.
     * 
     * @param formats
     */
    public void setFormats(com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2Formats formats) {
        this.formats = formats;
    }


    /**
     * Gets the rowLimit value for this UpdateViewHtml2.
     * 
     * @return rowLimit
     */
    public com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2RowLimit getRowLimit() {
        return rowLimit;
    }


    /**
     * Sets the rowLimit value for this UpdateViewHtml2.
     * 
     * @param rowLimit
     */
    public void setRowLimit(com.google.enterprise.connector.sharepoint.generated.views.UpdateViewHtml2RowLimit rowLimit) {
        this.rowLimit = rowLimit;
    }


    /**
     * Gets the openApplicationExtension value for this UpdateViewHtml2.
     * 
     * @return openApplicationExtension
     */
    public java.lang.String getOpenApplicationExtension() {
        return openApplicationExtension;
    }


    /**
     * Sets the openApplicationExtension value for this UpdateViewHtml2.
     * 
     * @param openApplicationExtension
     */
    public void setOpenApplicationExtension(java.lang.String openApplicationExtension) {
        this.openApplicationExtension = openApplicationExtension;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UpdateViewHtml2)) return false;
        UpdateViewHtml2 other = (UpdateViewHtml2) obj;
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
            ((this.toolbar==null && other.getToolbar()==null) || 
             (this.toolbar!=null &&
              this.toolbar.equals(other.getToolbar()))) &&
            ((this.viewHeader==null && other.getViewHeader()==null) || 
             (this.viewHeader!=null &&
              this.viewHeader.equals(other.getViewHeader()))) &&
            ((this.viewBody==null && other.getViewBody()==null) || 
             (this.viewBody!=null &&
              this.viewBody.equals(other.getViewBody()))) &&
            ((this.viewFooter==null && other.getViewFooter()==null) || 
             (this.viewFooter!=null &&
              this.viewFooter.equals(other.getViewFooter()))) &&
            ((this.viewEmpty==null && other.getViewEmpty()==null) || 
             (this.viewEmpty!=null &&
              this.viewEmpty.equals(other.getViewEmpty()))) &&
            ((this.rowLimitExceeded==null && other.getRowLimitExceeded()==null) || 
             (this.rowLimitExceeded!=null &&
              this.rowLimitExceeded.equals(other.getRowLimitExceeded()))) &&
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
              this.rowLimit.equals(other.getRowLimit()))) &&
            ((this.openApplicationExtension==null && other.getOpenApplicationExtension()==null) || 
             (this.openApplicationExtension!=null &&
              this.openApplicationExtension.equals(other.getOpenApplicationExtension())));
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
        if (getToolbar() != null) {
            _hashCode += getToolbar().hashCode();
        }
        if (getViewHeader() != null) {
            _hashCode += getViewHeader().hashCode();
        }
        if (getViewBody() != null) {
            _hashCode += getViewBody().hashCode();
        }
        if (getViewFooter() != null) {
            _hashCode += getViewFooter().hashCode();
        }
        if (getViewEmpty() != null) {
            _hashCode += getViewEmpty().hashCode();
        }
        if (getRowLimitExceeded() != null) {
            _hashCode += getRowLimitExceeded().hashCode();
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
        if (getOpenApplicationExtension() != null) {
            _hashCode += getOpenApplicationExtension().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(UpdateViewHtml2.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">UpdateViewHtml2"));
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
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateViewHtml2>viewProperties"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("toolbar");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "toolbar"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateViewHtml2>toolbar"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("viewHeader");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "viewHeader"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateViewHtml2>viewHeader"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("viewBody");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "viewBody"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateViewHtml2>viewBody"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("viewFooter");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "viewFooter"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateViewHtml2>viewFooter"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("viewEmpty");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "viewEmpty"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateViewHtml2>viewEmpty"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("rowLimitExceeded");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "rowLimitExceeded"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateViewHtml2>rowLimitExceeded"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("query");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "query"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateViewHtml2>query"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("viewFields");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "viewFields"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateViewHtml2>viewFields"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("aggregations");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "aggregations"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateViewHtml2>aggregations"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("formats");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "formats"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateViewHtml2>formats"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("rowLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "rowLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateViewHtml2>rowLimit"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("openApplicationExtension");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "openApplicationExtension"));
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
