<%@ Page Language="C#" Inherits="System.Web.UI.Page"%>
<%@ Assembly Name="Microsoft.SharePoint, Version=11.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> <%@ Import Namespace="Microsoft.SharePoint.Utilities" %> <%@ Import Namespace="Microsoft.SharePoint" %>
<% Response.ContentType = "text/xml"; %>
<wsdl:definitions xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:tm="http://microsoft.com/wsdl/mime/textMatching/" xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/" xmlns:mime="http://schemas.xmlsoap.org/wsdl/mime/" xmlns:tns="gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com" xmlns:s="http://www.w3.org/2001/XMLSchema" xmlns:soap12="http://schemas.xmlsoap.org/wsdl/soap12/" xmlns:http="http://schemas.xmlsoap.org/wsdl/http/" targetNamespace="gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/">
  <wsdl:types>
    <s:schema elementFormDefault="qualified" targetNamespace="gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com">
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
      <s:element name="GetGSSVersion">
        <s:complexType />
      </s:element>
      <s:element name="GetGSSVersionResponse">
        <s:complexType>
          <s:sequence>
            <s:element minOccurs="0" maxOccurs="1" name="GetGSSVersionResult" type="s:string" />
          </s:sequence>
        </s:complexType>
      </s:element>      
      <s:element name="Authorize">
        <s:complexType>
          <s:sequence>
            <s:element minOccurs="0" maxOccurs="1" name="authDataPacketArray" type="tns:ArrayOfAuthDataPacket" />
            <s:element minOccurs="0" maxOccurs="1" name="username" type="s:string" />
          </s:sequence>
        </s:complexType>
      </s:element>
      <s:complexType name="ArrayOfAuthDataPacket">
        <s:sequence>
          <s:element minOccurs="0" maxOccurs="unbounded" name="AuthDataPacket" nillable="true" type="tns:AuthDataPacket" />
        </s:sequence>
      </s:complexType>
      <s:complexType name="AuthDataPacket">
        <s:sequence>
          <s:element minOccurs="0" maxOccurs="1" name="AuthDataArray" type="tns:ArrayOfAuthData" />
          <s:element minOccurs="0" maxOccurs="1" name="Container" type="tns:Container" />
          <s:element minOccurs="0" maxOccurs="1" name="Message" type="s:string" />
          <s:element minOccurs="1" maxOccurs="1" name="IsDone" type="s:boolean" />
        </s:sequence>
      </s:complexType>
      <s:complexType name="ArrayOfAuthData">
        <s:sequence>
          <s:element minOccurs="0" maxOccurs="unbounded" name="AuthData" nillable="true" type="tns:AuthData" />
        </s:sequence>
      </s:complexType>
      <s:complexType name="AuthData">
        <s:sequence>
          <s:element minOccurs="0" maxOccurs="1" name="Container" type="tns:Container" />
          <s:element minOccurs="0" maxOccurs="1" name="ItemId" type="s:string" />
          <s:element minOccurs="1" maxOccurs="1" name="IsAllowed" type="s:boolean" />
          <s:element minOccurs="0" maxOccurs="1" name="Message" type="s:string" />
          <s:element minOccurs="0" maxOccurs="1" name="ComplexDocId" type="s:string" />
          <s:element minOccurs="1" maxOccurs="1" name="IsDone" type="s:boolean" />
          <s:element minOccurs="1" maxOccurs="1" name="Type" type="tns:EntityType" />
        </s:sequence>
      </s:complexType>
      <s:complexType name="Container">
        <s:sequence>
          <s:element minOccurs="0" maxOccurs="1" name="Url" type="s:string" />
          <s:element minOccurs="1" maxOccurs="1" name="Type" type="tns:ContainerType" />
        </s:sequence>
      </s:complexType>
      <s:simpleType name="ContainerType">
        <s:restriction base="s:string">
          <s:enumeration value="NA" />
          <s:enumeration value="SITE_COLLECTION" />
          <s:enumeration value="SITE" />
          <s:enumeration value="LIST" />
        </s:restriction>
      </s:simpleType>
      <s:simpleType name="EntityType">
        <s:restriction base="s:string">
          <s:enumeration value="LISTITEM" />
          <s:enumeration value="LIST" />
          <s:enumeration value="ALERT" />
          <s:enumeration value="SITE" />
        </s:restriction>
      </s:simpleType>
      <s:element name="AuthorizeResponse">
        <s:complexType>
          <s:sequence>
            <s:element minOccurs="0" maxOccurs="1" name="authDataPacketArray" type="tns:ArrayOfAuthDataPacket" />
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
    <wsdl:message name="GetGSSVersionSoapIn">
    <wsdl:part name="parameters" element="tns:GetGSSVersion" />
  </wsdl:message>
  <wsdl:message name="GetGSSVersionSoapOut">
    <wsdl:part name="parameters" element="tns:GetGSSVersionResponse" />
  </wsdl:message>  
  <wsdl:message name="AuthorizeSoapIn">
    <wsdl:part name="parameters" element="tns:Authorize" />
  </wsdl:message>
  <wsdl:message name="AuthorizeSoapOut">
    <wsdl:part name="parameters" element="tns:AuthorizeResponse" />
  </wsdl:message>
  <wsdl:portType name="BulkAuthorizationSoap">
    <wsdl:operation name="CheckConnectivity">
      <wsdl:input message="tns:CheckConnectivitySoapIn" />
      <wsdl:output message="tns:CheckConnectivitySoapOut" />
    </wsdl:operation>
        <wsdl:operation name="GetGSSVersion">
      <wsdl:input message="tns:GetGSSVersionSoapIn" />
      <wsdl:output message="tns:GetGSSVersionSoapOut" />
    </wsdl:operation>
    <wsdl:operation name="Authorize">
      <wsdl:input message="tns:AuthorizeSoapIn" />
      <wsdl:output message="tns:AuthorizeSoapOut" />
    </wsdl:operation>
  </wsdl:portType>
  <wsdl:binding name="BulkAuthorizationSoap" type="tns:BulkAuthorizationSoap">
    <soap:binding transport="http://schemas.xmlsoap.org/soap/http" />
    <wsdl:operation name="CheckConnectivity">
      <soap:operation soapAction="gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com/CheckConnectivity" style="document" />
      <wsdl:input>
        <soap:body use="literal" />
      </wsdl:input>
      <wsdl:output>
        <soap:body use="literal" />
      </wsdl:output>
    </wsdl:operation>
    <wsdl:operation name="GetGSSVersion">
      <soap:operation soapAction="gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com/GetGSSVersion" style="document" />
      <wsdl:input>
        <soap:body use="literal" />
      </wsdl:input>
      <wsdl:output>
        <soap:body use="literal" />
      </wsdl:output>
    </wsdl:operation>
    <wsdl:operation name="Authorize">
      <soap:operation soapAction="gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com/Authorize" style="document" />
      <wsdl:input>
        <soap:body use="literal" />
      </wsdl:input>
      <wsdl:output>
        <soap:body use="literal" />
      </wsdl:output>
    </wsdl:operation>
  </wsdl:binding>
  <wsdl:binding name="BulkAuthorizationSoap12" type="tns:BulkAuthorizationSoap">
    <soap12:binding transport="http://schemas.xmlsoap.org/soap/http" />
    <wsdl:operation name="CheckConnectivity">
      <soap12:operation soapAction="gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com/CheckConnectivity" style="document" />
      <wsdl:input>
        <soap12:body use="literal" />
      </wsdl:input>
      <wsdl:output>
        <soap12:body use="literal" />
      </wsdl:output>
    </wsdl:operation>
        <wsdl:operation name="GetGSSVersion">
      <soap12:operation soapAction="gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com/GetGSSVersion" style="document" />
      <wsdl:input>
        <soap12:body use="literal" />
      </wsdl:input>
      <wsdl:output>
        <soap12:body use="literal" />
      </wsdl:output>
    </wsdl:operation>
    <wsdl:operation name="Authorize">
      <soap12:operation soapAction="gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com/Authorize" style="document" />
      <wsdl:input>
        <soap12:body use="literal" />
      </wsdl:input>
      <wsdl:output>
        <soap12:body use="literal" />
      </wsdl:output>
    </wsdl:operation>
  </wsdl:binding>
  <wsdl:service name="BulkAuthorization">
    <wsdl:port name="BulkAuthorizationSoap" binding="tns:BulkAuthorizationSoap">
      <soap:address location=<% SPEncode.WriteHtmlEncodeWithQuote(Response, SPWeb.OriginalBaseUrl(Request), '"'); %> />
    </wsdl:port>
    <wsdl:port name="BulkAuthorizationSoap12" binding="tns:BulkAuthorizationSoap12">
      <soap12:address location=<% SPEncode.WriteHtmlEncodeWithQuote(Response, SPWeb.OriginalBaseUrl(Request), '"'); %> />
    </wsdl:port>
  </wsdl:service>
</wsdl:definitions>