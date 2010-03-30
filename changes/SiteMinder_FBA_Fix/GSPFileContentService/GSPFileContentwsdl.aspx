<%@ Page Language="C#" Inherits="System.Web.UI.Page"
%> <%@ Assembly Name="Microsoft.SharePoint, Microsoft.SharePoint, Version=11.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> <%@ Import Namespace="Microsoft.SharePoint.Utilities" %> <%@ Import Namespace="Microsoft.SharePoint" %>
<% Response.ContentType = "text/xml"; %>
<wsdl:definitions xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:tm="http://microsoft.com/wsdl/mime/textMatching/" xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/" xmlns:mime="http://schemas.xmlsoap.org/wsdl/mime/" xmlns:tns="http://tempuri.org/" xmlns:s="http://www.w3.org/2001/XMLSchema" xmlns:soap12="http://schemas.xmlsoap.org/wsdl/soap12/" xmlns:http="http://schemas.xmlsoap.org/wsdl/http/" targetNamespace="http://tempuri.org/" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/">
  <wsdl:types>
    <s:schema elementFormDefault="qualified" targetNamespace="http://tempuri.org/">
      <s:element name="HelloWorld">
        <s:complexType />
      </s:element>
      <s:element name="HelloWorldResponse">
        <s:complexType>
          <s:sequence>
            <s:element minOccurs="0" maxOccurs="1" name="HelloWorldResult" type="s:string" />
          </s:sequence>
        </s:complexType>
      </s:element>
      <s:element name="GetFileContents">
        <s:complexType>
          <s:sequence>
            <s:element minOccurs="0" maxOccurs="1" name="fileURL" type="s:string" />
          </s:sequence>
        </s:complexType>
      </s:element>
      <s:element name="GetFileContentsResponse">
        <s:complexType>
          <s:sequence>
            <s:element minOccurs="0" maxOccurs="1" name="GetFileContentsResult" type="s:base64Binary" />
          </s:sequence>
        </s:complexType>
      </s:element>
    </s:schema>
  </wsdl:types>
  <wsdl:message name="HelloWorldSoapIn">
    <wsdl:part name="parameters" element="tns:HelloWorld" />
  </wsdl:message>
  <wsdl:message name="HelloWorldSoapOut">
    <wsdl:part name="parameters" element="tns:HelloWorldResponse" />
  </wsdl:message>
  <wsdl:message name="GetFileContentsSoapIn">
    <wsdl:part name="parameters" element="tns:GetFileContents" />
  </wsdl:message>
  <wsdl:message name="GetFileContentsSoapOut">
    <wsdl:part name="parameters" element="tns:GetFileContentsResponse" />
  </wsdl:message>
  <wsdl:portType name="GSPFileContentSoap">
    <wsdl:operation name="HelloWorld">
      <wsdl:input message="tns:HelloWorldSoapIn" />
      <wsdl:output message="tns:HelloWorldSoapOut" />
    </wsdl:operation>
    <wsdl:operation name="GetFileContents">
      <wsdl:input message="tns:GetFileContentsSoapIn" />
      <wsdl:output message="tns:GetFileContentsSoapOut" />
    </wsdl:operation>
  </wsdl:portType>
  <wsdl:binding name="GSPFileContentSoap" type="tns:GSPFileContentSoap">
    <soap:binding transport="http://schemas.xmlsoap.org/soap/http" />
    <wsdl:operation name="HelloWorld">
      <soap:operation soapAction="http://tempuri.org/HelloWorld" style="document" />
      <wsdl:input>
        <soap:body use="literal" />
      </wsdl:input>
      <wsdl:output>
        <soap:body use="literal" />
      </wsdl:output>
    </wsdl:operation>
    <wsdl:operation name="GetFileContents">
      <soap:operation soapAction="http://tempuri.org/GetFileContents" style="document" />
      <wsdl:input>
        <soap:body use="literal" />
      </wsdl:input>
      <wsdl:output>
        <soap:body use="literal" />
      </wsdl:output>
    </wsdl:operation>
  </wsdl:binding>
  <wsdl:binding name="GSPFileContentSoap12" type="tns:GSPFileContentSoap">
    <soap12:binding transport="http://schemas.xmlsoap.org/soap/http" />
    <wsdl:operation name="HelloWorld">
      <soap12:operation soapAction="http://tempuri.org/HelloWorld" style="document" />
      <wsdl:input>
        <soap12:body use="literal" />
      </wsdl:input>
      <wsdl:output>
        <soap12:body use="literal" />
      </wsdl:output>
    </wsdl:operation>
    <wsdl:operation name="GetFileContents">
      <soap12:operation soapAction="http://tempuri.org/GetFileContents" style="document" />
      <wsdl:input>
        <soap12:body use="literal" />
      </wsdl:input>
      <wsdl:output>
        <soap12:body use="literal" />
      </wsdl:output>
    </wsdl:operation>
  </wsdl:binding>
  <wsdl:service name="GSPFileContent">
    <wsdl:port name="GSPFileContentSoap" binding="tns:GSPFileContentSoap">
	  <soap:address location=<% SPEncode.WriteHtmlEncodeWithQuote(Response,SPWeb.OriginalBaseUrl(Request), '"'); %> />
    </wsdl:port>
    <wsdl:port name="GSPFileContentSoap12" binding="tns:GSPFileContentSoap12">
      <soap12:address location="http://localhost:4806/GSPFileContents/GSPFileContent.asmx" />
    </wsdl:port>
  </wsdl:service>
</wsdl:definitions>