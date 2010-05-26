<%@ Page Language="C#" Inherits="System.Web.UI.Page"%>
<%@ Assembly Name="Microsoft.SharePoint, Version=11.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> <%@ Import Namespace="Microsoft.SharePoint.Utilities" %> <%@ Import Namespace="Microsoft.SharePoint" %>
<% Response.ContentType = "text/xml"; %>
<wsdl:definitions xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:tm="http://microsoft.com/wsdl/mime/textMatching/" xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/" xmlns:mime="http://schemas.xmlsoap.org/wsdl/mime/" xmlns:tns="gssitediscovery.generated.sharepoint.connector.enterprise.google.com" xmlns:s="http://www.w3.org/2001/XMLSchema" xmlns:soap12="http://schemas.xmlsoap.org/wsdl/soap12/" xmlns:http="http://schemas.xmlsoap.org/wsdl/http/" targetNamespace="gssitediscovery.generated.sharepoint.connector.enterprise.google.com" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/">
  <wsdl:types>
    <s:schema elementFormDefault="qualified" targetNamespace="gssitediscovery.generated.sharepoint.connector.enterprise.google.com">
      <s:element name="CheckConnectivity">
        <s:complexType />
      </s:element>
      <s:element name="CheckConnectivityResponse">
        <s:complexType>
          <s:sequence>
            <s:element minOccurs="0" maxOccurs="1" name="CheckConnectivityResult" type="s:string" />
          </s:sequence>
        </s:complexType>
      </s:element>
      <s:element name="GetAllSiteCollectionFromAllWebApps">
        <s:complexType />
      </s:element>
      <s:element name="GetAllSiteCollectionFromAllWebAppsResponse">
        <s:complexType>
          <s:sequence>
            <s:element minOccurs="0" maxOccurs="1" name="GetAllSiteCollectionFromAllWebAppsResult" type="tns:ArrayOfAnyType" />
          </s:sequence>
        </s:complexType>
      </s:element>
      <s:complexType name="ArrayOfAnyType">
        <s:sequence>
          <s:element minOccurs="0" maxOccurs="unbounded" name="anyType" nillable="true" />
        </s:sequence>
      </s:complexType>
      <s:element name="GetWebCrawlInfo">
        <s:complexType />
      </s:element>
      <s:complexType name="WebCrawlInfo">
        <s:sequence>
          <s:element minOccurs="1" maxOccurs="1" name="CrawlAspaxPages" type="s:boolean" />
          <s:element minOccurs="1" maxOccurs="1" name="NoCrawl" type="s:boolean" />
        </s:sequence>
      </s:complexType>
      <s:element name="GetWebCrawlInfoResponse">
        <s:complexType>
          <s:sequence>
            <s:element minOccurs="0" maxOccurs="1" name="GetWebCrawlInfoResult" type="tns:WebCrawlInfo" />
          </s:sequence>
        </s:complexType>
      </s:element>
      <s:element name="GetListCrawlInfo">
        <s:complexType>
          <s:sequence>
            <s:element minOccurs="0" maxOccurs="1" name="listGuids" type="tns:ArrayOfString" />
          </s:sequence>
        </s:complexType>
      </s:element>
      <s:complexType name="ArrayOfString">
        <s:sequence>
          <s:element minOccurs="0" maxOccurs="unbounded" name="string" nillable="true" type="s:string" />
        </s:sequence>
      </s:complexType>
      <s:complexType name="ListCrawlInfo">
        <s:sequence>
          <s:element minOccurs="0" maxOccurs="1" name="ListGuid" type="s:string" />
          <s:element minOccurs="1" maxOccurs="1" name="NoCrawl" type="s:boolean" />
          <s:element minOccurs="1" maxOccurs="1" name="Status" type="s:boolean" />
          <s:element minOccurs="0" maxOccurs="1" name="Error" type="s:string" />
        </s:sequence>
      </s:complexType>
      <s:complexType name="ArrayOfListCrawlInfo">
        <s:sequence>
          <s:element minOccurs="0" maxOccurs="unbounded" name="ListCrawlInfo" nillable="true" type="tns:ListCrawlInfo" />
        </s:sequence>
      </s:complexType>
      <s:element name="GetListCrawlInfoResponse">
        <s:complexType>
          <s:sequence>
            <s:element minOccurs="0" maxOccurs="1" name="GetListCrawlInfoResult" type="tns:ArrayOfListCrawlInfo" />
          </s:sequence>
        </s:complexType>
      </s:element>
      <s:element name="IsCrawlableList">
        <s:complexType>
          <s:sequence>
            <s:element minOccurs="0" maxOccurs="1" name="listGUID" type="s:string" />
          </s:sequence>
        </s:complexType>
      </s:element>
      <s:element name="IsCrawlableListResponse">
        <s:complexType>
          <s:sequence>
            <s:element minOccurs="1" maxOccurs="1" name="IsCrawlableListResult" type="s:boolean" />
          </s:sequence>
        </s:complexType>
      </s:element>
    </s:schema>
  </wsdl:types>
  <wsdl:message name="CheckConnectivitySoapIn">
    <wsdl:part name="parameters" element="tns:CheckConnectivity" />
  </wsdl:message>
  <wsdl:message name="CheckConnectivitySoapOut">
    <wsdl:part name="parameters" element="tns:CheckConnectivityResponse" />
  </wsdl:message>
  <wsdl:message name="GetAllSiteCollectionFromAllWebAppsSoapIn">
    <wsdl:part name="parameters" element="tns:GetAllSiteCollectionFromAllWebApps" />
  </wsdl:message>
  <wsdl:message name="GetAllSiteCollectionFromAllWebAppsSoapOut">
    <wsdl:part name="parameters" element="tns:GetAllSiteCollectionFromAllWebAppsResponse" />
  </wsdl:message>
  <wsdl:message name="GetWebCrawlInfoSoapIn">
    <wsdl:part name="parameters" element="tns:GetWebCrawlInfo" />
  </wsdl:message>
  <wsdl:message name="GetWebCrawlInfoSoapOut">
    <wsdl:part name="parameters" element="tns:GetWebCrawlInfoResponse" />
  </wsdl:message>
  <wsdl:message name="GetListCrawlInfoSoapIn">
    <wsdl:part name="parameters" element="tns:GetListCrawlInfo" />
  </wsdl:message>
  <wsdl:message name="GetListCrawlInfoSoapOut">
    <wsdl:part name="parameters" element="tns:GetListCrawlInfoResponse" />
  </wsdl:message>
  <wsdl:message name="IsCrawlableListSoapIn">
    <wsdl:part name="parameters" element="tns:IsCrawlableList" />
  </wsdl:message>
  <wsdl:message name="IsCrawlableListSoapOut">
    <wsdl:part name="parameters" element="tns:IsCrawlableListResponse" />
  </wsdl:message>
  <wsdl:portType name="SiteDiscoverySoap">
    <wsdl:operation name="CheckConnectivity">
      <wsdl:input message="tns:CheckConnectivitySoapIn" />
      <wsdl:output message="tns:CheckConnectivitySoapOut" />
    </wsdl:operation>
    <wsdl:operation name="GetAllSiteCollectionFromAllWebApps">
      <wsdl:input message="tns:GetAllSiteCollectionFromAllWebAppsSoapIn" />
      <wsdl:output message="tns:GetAllSiteCollectionFromAllWebAppsSoapOut" />
    </wsdl:operation>
    <wsdl:operation name="GetWebCrawlInfo">
      <wsdl:input message="tns:GetWebCrawlInfoSoapIn" />
      <wsdl:output message="tns:GetWebCrawlInfoSoapOut" />
    </wsdl:operation>
    <wsdl:operation name="GetListCrawlInfo">
      <wsdl:input message="tns:GetListCrawlInfoSoapIn" />
      <wsdl:output message="tns:GetListCrawlInfoSoapOut" />
    </wsdl:operation>
    <wsdl:operation name="IsCrawlableList">
      <wsdl:input message="tns:IsCrawlableListSoapIn" />
      <wsdl:output message="tns:IsCrawlableListSoapOut" />
    </wsdl:operation>
  </wsdl:portType>
  <wsdl:binding name="SiteDiscoverySoap" type="tns:SiteDiscoverySoap">
    <soap:binding transport="http://schemas.xmlsoap.org/soap/http" />
    <wsdl:operation name="CheckConnectivity">
      <soap:operation soapAction="gssitediscovery.generated.sharepoint.connector.enterprise.google.com/CheckConnectivity" style="document" />
      <wsdl:input>
        <soap:body use="literal" />
      </wsdl:input>
      <wsdl:output>
        <soap:body use="literal" />
      </wsdl:output>
    </wsdl:operation>
    <wsdl:operation name="GetAllSiteCollectionFromAllWebApps">
      <soap:operation soapAction="gssitediscovery.generated.sharepoint.connector.enterprise.google.com/GetAllSiteCollectionFromAllWebApps" style="document" />
      <wsdl:input>
        <soap:body use="literal" />
      </wsdl:input>
      <wsdl:output>
        <soap:body use="literal" />
      </wsdl:output>
    </wsdl:operation>
    <wsdl:operation name="GetWebCrawlInfo">
      <soap:operation soapAction="gssitediscovery.generated.sharepoint.connector.enterprise.google.com/GetWebCrawlInfo" style="document" />
      <wsdl:input>
        <soap:body use="literal" />
      </wsdl:input>
      <wsdl:output>
        <soap:body use="literal" />
      </wsdl:output>
    </wsdl:operation>
    <wsdl:operation name="GetListCrawlInfo">
      <soap:operation soapAction="gssitediscovery.generated.sharepoint.connector.enterprise.google.com/GetListCrawlInfo" style="document" />
      <wsdl:input>
        <soap:body use="literal" />
      </wsdl:input>
      <wsdl:output>
        <soap:body use="literal" />
      </wsdl:output>
    </wsdl:operation>
    <wsdl:operation name="IsCrawlableList">
      <soap:operation soapAction="gssitediscovery.generated.sharepoint.connector.enterprise.google.com/IsCrawlableList" style="document" />
      <wsdl:input>
        <soap:body use="literal" />
      </wsdl:input>
      <wsdl:output>
        <soap:body use="literal" />
      </wsdl:output>
    </wsdl:operation>
  </wsdl:binding>
  <wsdl:binding name="SiteDiscoverySoap12" type="tns:SiteDiscoverySoap">
    <soap12:binding transport="http://schemas.xmlsoap.org/soap/http" />
    <wsdl:operation name="CheckConnectivity">
      <soap12:operation soapAction="gssitediscovery.generated.sharepoint.connector.enterprise.google.com/CheckConnectivity" style="document" />
      <wsdl:input>
        <soap12:body use="literal" />
      </wsdl:input>
      <wsdl:output>
        <soap12:body use="literal" />
      </wsdl:output>
    </wsdl:operation>
    <wsdl:operation name="GetAllSiteCollectionFromAllWebApps">
      <soap12:operation soapAction="gssitediscovery.generated.sharepoint.connector.enterprise.google.com/GetAllSiteCollectionFromAllWebApps" style="document" />
      <wsdl:input>
        <soap12:body use="literal" />
      </wsdl:input>
      <wsdl:output>
        <soap12:body use="literal" />
      </wsdl:output>
    </wsdl:operation>
    <wsdl:operation name="GetWebCrawlInfo">
      <soap12:operation soapAction="gssitediscovery.generated.sharepoint.connector.enterprise.google.com/GetWebCrawlInfo" style="document" />
      <wsdl:input>
        <soap12:body use="literal" />
      </wsdl:input>
      <wsdl:output>
        <soap12:body use="literal" />
      </wsdl:output>
    </wsdl:operation>
    <wsdl:operation name="GetListCrawlInfo">
      <soap12:operation soapAction="gssitediscovery.generated.sharepoint.connector.enterprise.google.com/GetListCrawlInfo" style="document" />
      <wsdl:input>
        <soap12:body use="literal" />
      </wsdl:input>
      <wsdl:output>
        <soap12:body use="literal" />
      </wsdl:output>
    </wsdl:operation>
    <wsdl:operation name="IsCrawlableList">
      <soap12:operation soapAction="gssitediscovery.generated.sharepoint.connector.enterprise.google.com/IsCrawlableList" style="document" />
      <wsdl:input>
        <soap12:body use="literal" />
      </wsdl:input>
      <wsdl:output>
        <soap12:body use="literal" />
      </wsdl:output>
    </wsdl:operation>
  </wsdl:binding>
  <wsdl:service name="SiteDiscovery">
    <wsdl:port name="SiteDiscoverySoap" binding="tns:SiteDiscoverySoap">
      <soap:address location=<% SPEncode.WriteHtmlEncodeWithQuote(Response, SPWeb.OriginalBaseUrl(Request), '"'); %> />
    </wsdl:port>
    <wsdl:port name="SiteDiscoverySoap12" binding="tns:SiteDiscoverySoap12">
      <soap12:address location=<% SPEncode.WriteHtmlEncodeWithQuote(Response, SPWeb.OriginalBaseUrl(Request), '"'); %> />
    </wsdl:port>
  </wsdl:service>
</wsdl:definitions>