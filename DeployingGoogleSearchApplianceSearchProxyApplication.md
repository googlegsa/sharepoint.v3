# Introduction #

This document covers the steps for deploying Google Search Appliance Search Proxy Application on IIS 6.0.

## Instructions for manual deployment of Google Search Appliance Search Proxy Application ##

  * The Google Search Appliance Search Proxy Application can be deployed on machine with Internet Information Services (IIS) installed.
  * The documents should be fed and indexed on appliance before performing search .
  * Modify web.config file to include your GSA specific settings to be used by the Google Search Appliance Search Proxy Application.
```
        <appSettings>
		<add key="GSACollection" value="default_collection" />
		<add key="GSALocation" value="http://myappliance.mydomain.com" />
		<add key="frontEnd" value="default_frontend" />

                <!--Optional parameter: Cookie domain parameter gives you the flexibility to change\configure the domain of the newly added cookies -->
		<!--add key="cookieDomain" value=".mydomain.com" /-->

                <!--set verbose value="True" to enable information level logs-->
		<add key="verbose" value="False" />

                <!--Specify location for the log files -->
		<add key="logLocation" value="C:\" />
	</appSettings>
```

## Steps: ##

  1. Open IIS Manager.
  1. In IIS, right click on the "Web Sites" folder and click New->Web Site..
  1. Click on Next.
  1. Enter "Google Search Appliance Search Proxy Application" under Description.
  1. Enter port number.
  1. Enter port number. Click on Next
  1. Enter the Path to point to the proxy application directory.
  1. Uncheck "Allow anonymous access to this web site". Click Next.
  1. Check options Read, Run scripts, Browse and Execute. Click Next.
  1. Click Finish.

## Customization of Proxy Application ##

This is a general search proxy application. You can create you own custom search box and then fire a HTTP GET request on GSASearchProxy.aspx with query parameters same as GSA. If no query parameters are entered then search proxy application constructs the GSA query from the parameters mentioned in web.config file

The proxy application will internally fire this query to GSA  and return secure results with the same user context.

The search proxy application handles redirects from GSA internally and renders the final search results from GSA.

If you want to customize the rendered results, you can directly modify the GSA Frontend and configure the search proxy application with the new frontend.

The web.config exposes parameters such as GSA frontend, target cookie domain, log location etc. which is used internally by the search proxy application while firing search to GSA.