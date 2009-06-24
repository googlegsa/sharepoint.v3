/**
 * AddView.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.views;

public class AddView  implements java.io.Serializable {
    private java.lang.String listName;

    private java.lang.String viewName;

    private com.google.enterprise.connector.sharepoint.generated.views.AddViewViewFields viewFields;

    private com.google.enterprise.connector.sharepoint.generated.views.AddViewQuery query;

    private com.google.enterprise.connector.sharepoint.generated.views.AddViewRowLimit rowLimit;

    private java.lang.String type;

    private boolean makeViewDefault;

    public AddView() {
    }

    public AddView(
           java.lang.String listName,
           java.lang.String viewName,
           com.google.enterprise.connector.sharepoint.generated.views.AddViewViewFields viewFields,
           com.google.enterprise.connector.sharepoint.generated.views.AddViewQuery query,
           com.google.enterprise.connector.sharepoint.generated.views.AddViewRowLimit rowLimit,
           java.lang.String type,
           boolean makeViewDefault) {
           this.listName = listName;
           this.viewName = viewName;
           this.viewFields = viewFields;
           this.query = query;
           this.rowLimit = rowLimit;
           this.type = type;
           this.makeViewDefault = makeViewDefault;
    }


    /**
     * Gets the listName value for this AddView.
     * 
     * @return listName
     */
    public java.lang.String getListName() {
        return listName;
    }


    /**
     * Sets the listName value for this AddView.
     * 
     * @param listName
     */
    public void setListName(java.lang.String listName) {
        this.listName = listName;
    }


    /**
     * Gets the viewName value for this AddView.
     * 
     * @return viewName
     */
    public java.lang.String getViewName() {
        return viewName;
    }


    /**
     * Sets the viewName value for this AddView.
     * 
     * @param viewName
     */
    public void setViewName(java.lang.String viewName) {
        this.viewName = viewName;
    }


    /**
     * Gets the viewFields value for this AddView.
     * 
     * @return viewFields
     */
    public com.google.enterprise.connector.sharepoint.generated.views.AddViewViewFields getViewFields() {
        return viewFields;
    }


    /**
     * Sets the viewFields value for this AddView.
     * 
     * @param viewFields
     */
    public void setViewFields(com.google.enterprise.connector.sharepoint.generated.views.AddViewViewFields viewFields) {
        this.viewFields = viewFields;
    }


    /**
     * Gets the query value for this AddView.
     * 
     * @return query
     */
    public com.google.enterprise.connector.sharepoint.generated.views.AddViewQuery getQuery() {
        return query;
    }


    /**
     * Sets the query value for this AddView.
     * 
     * @param query
     */
    public void setQuery(com.google.enterprise.connector.sharepoint.generated.views.AddViewQuery query) {
        this.query = query;
    }


    /**
     * Gets the rowLimit value for this AddView.
     * 
     * @return rowLimit
     */
    public com.google.enterprise.connector.sharepoint.generated.views.AddViewRowLimit getRowLimit() {
        return rowLimit;
    }


    /**
     * Sets the rowLimit value for this AddView.
     * 
     * @param rowLimit
     */
    public void setRowLimit(com.google.enterprise.connector.sharepoint.generated.views.AddViewRowLimit rowLimit) {
        this.rowLimit = rowLimit;
    }


    /**
     * Gets the type value for this AddView.
     * 
     * @return type
     */
    public java.lang.String getType() {
        return type;
    }


    /**
     * Sets the type value for this AddView.
     * 
     * @param type
     */
    public void setType(java.lang.String type) {
        this.type = type;
    }


    /**
     * Gets the makeViewDefault value for this AddView.
     * 
     * @return makeViewDefault
     */
    public boolean isMakeViewDefault() {
        return makeViewDefault;
    }


    /**
     * Sets the makeViewDefault value for this AddView.
     * 
     * @param makeViewDefault
     */
    public void setMakeViewDefault(boolean makeViewDefault) {
        this.makeViewDefault = makeViewDefault;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AddView)) return false;
        AddView other = (AddView) obj;
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
            ((this.viewFields==null && other.getViewFields()==null) || 
             (this.viewFields!=null &&
              this.viewFields.equals(other.getViewFields()))) &&
            ((this.query==null && other.getQuery()==null) || 
             (this.query!=null &&
              this.query.equals(other.getQuery()))) &&
            ((this.rowLimit==null && other.getRowLimit()==null) || 
             (this.rowLimit!=null &&
              this.rowLimit.equals(other.getRowLimit()))) &&
            ((this.type==null && other.getType()==null) || 
             (this.type!=null &&
              this.type.equals(other.getType()))) &&
            this.makeViewDefault == other.isMakeViewDefault();
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
        if (getViewFields() != null) {
            _hashCode += getViewFields().hashCode();
        }
        if (getQuery() != null) {
            _hashCode += getQuery().hashCode();
        }
        if (getRowLimit() != null) {
            _hashCode += getRowLimit().hashCode();
        }
        if (getType() != null) {
            _hashCode += getType().hashCode();
        }
        _hashCode += (isMakeViewDefault() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AddView.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">AddView"));
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
        elemField.setFieldName("viewFields");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "viewFields"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>AddView>viewFields"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("query");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "query"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>AddView>query"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("rowLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "rowLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>AddView>rowLimit"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("type");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "type"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("makeViewDefault");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "makeViewDefault"));
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
