<?xml version="1.0" encoding="utf-8"?>
<discovery xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns="http://schemas.xmlsoap.org/disco/">
  <contractRef ref=""
  <% SPEncode.WriteHtmlEncodeWithQuote(Response, SPWeb.OriginalBaseUrl(Request) + "?wsdl", '"'); %>  docRef=<% SPEncode.WriteHtmlEncodeWithQuote(Response, SPWeb.OriginalBaseUrl(Request), '"'); %>  xmlns="http://schemas.xmlsoap.org/disco/scl/" />
  <soap address=''
  <% SPEncode.WriteHtmlEncodeWithQuote(Response, SPWeb.OriginalBaseUrl(Request), '"'); %>  xmlns:q1="http://tempuri.org/" binding="q1:SPFilesSoap" xmlns="http://schemas.xmlsoap.org/disco/soap/" />
</discovery>