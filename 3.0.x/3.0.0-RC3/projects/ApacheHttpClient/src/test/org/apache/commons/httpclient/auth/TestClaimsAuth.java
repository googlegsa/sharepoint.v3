// Copyright 2012 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.commons.httpclient.auth;

import junit.framework.TestCase;

import org.apache.commons.httpclient.FakeHttpMethod;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.server.HttpService;
import org.apache.commons.httpclient.server.RequestLine;
import org.apache.commons.httpclient.server.SimpleHttpServer;
import org.apache.commons.httpclient.server.SimpleRequest;
import org.apache.commons.httpclient.server.SimpleResponse;

import java.io.IOException;

public class TestClaimsAuth extends TestCase {
  
    // ------------------------------------------------------------ Constructor
    public TestClaimsAuth(String testName) {
      super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestClaimsAuth.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods
    public void testNoClaimsBasedAuthentication() throws Exception {
        // configure the server
        SimpleHttpServer server = new SimpleHttpServer(); // use arbitrary port
        server.setTestname(getName());
        server.setHttpService(new NoClaimsService());
        
        // configure the client
        HttpClient client = new HttpClient();
        client.getHostConfiguration().setHost(
                server.getLocalAddress(), server.getLocalPort(),
                Protocol.getProtocol("http"));
        
        client.getState().setCredentials(AuthScope.ANY, 
            new NTCredentials("username", "password", "host", "domain"));
        
        FakeHttpMethod httpget = new FakeHttpMethod("/");
        try {
            assertEquals(403, client.executeMethod(httpget));
        } catch (Exception e) {
            fail("Exception caught: " + e.getMessage());
        }
        finally {
            httpget.releaseConnection();
        }
        server.destroy();
    }
    
    public void testClaimsBasedAuthn() throws Exception {
        // configure the server
        SimpleHttpServer server = new SimpleHttpServer(); // use arbitrary port
        server.setTestname(getName());
        server.setHttpService(new ClaimsService(
            "http://" + server.getLocalAddress() + ":" + server.getLocalPort()
        ));
      
        // configure the client
        HttpClient client = new HttpClient();
        client.getHostConfiguration().setHost(
                server.getLocalAddress(), server.getLocalPort(),
                Protocol.getProtocol("http"));
      
        client.getState().setCredentials(AuthScope.ANY, 
            new NTCredentials("username", "password", "host", "domain"));
        
        GetMethod httpget = new GetMethod(
            "http://" + server.getLocalAddress() + ":" + server.getLocalPort() + "/");
        try {
            assertEquals(200, client.executeMethod(httpget));
        } 
        finally {
            httpget.releaseConnection();
        }
        server.destroy();
    }
    
    public void testClaimsBasedAuthnInvalidCredentials() throws Exception {
        // configure the server
        SimpleHttpServer server = new SimpleHttpServer(); // use arbitrary port
        server.setTestname(getName());
        server.setHttpService(new ClaimsService(
            "http://" + server.getLocalAddress() + ":" + server.getLocalPort()
        ));
    
        // configure the client
        HttpClient client = new HttpClient();
        client.getHostConfiguration().setHost(
                server.getLocalAddress(), server.getLocalPort(),
                Protocol.getProtocol("http"));
    
        client.getState().setCredentials(AuthScope.ANY, 
            new NTCredentials("nonexisting", "wrongpassword", "host", "domain"));
      
        GetMethod httpget = new GetMethod(
            "http://" + server.getLocalAddress() + ":" + server.getLocalPort() + "/");
        try {
            assertEquals(401, client.executeMethod(httpget));
        } 
        finally {
            httpget.releaseConnection();
        }
        server.destroy();      
    }
    
    private class NoClaimsService implements HttpService {
        public NoClaimsService() {
            super();
        }
        
        @Override
        public boolean process(final SimpleRequest request, final SimpleResponse response)
            throws IOException {
            RequestLine requestLine = request.getRequestLine();
            HttpVersion ver = requestLine.getHttpVersion();
            response.setStatusLine(ver, HttpStatus.SC_FORBIDDEN);
            response.setBodyString("403 UNAUTHORIZED");
            return true;
        }
    }
    
    private class ClaimsService implements HttpService {

      private String base;
        final private String customLogin = 
            "/_vti_bin/CustomLogin.aspx?ReturnUrl=/_layouts/Error.aspx";
        
        final private String moved = 
            "<html><head><title>Object moved</title></head><body>\n    <h2>Ob" +
            "ject moved to <a href=\"%2f_windows%2fdefault.aspx%3fReturnUrl%3" +
            "d%252f_layouts%252fError.aspx\">here</a>.</h2>\n</body></html>";
        
        final private String windowsLogin = 
            "/_windows/default.aspx?ReturnUrl=%2f_layouts%2fError.aspx";
        
        final private String fedAuthCookie = "FedAuth=77u/PD94bWwgdmVyc2lvbj0" +
            "iMS4wIiBlbmNvZGluZz0idXRmLTgiPz48U1A+MCMud3xlbnRlbWVhXGFkbWluaXN" +
            "0cmF0b3IsMTI5NzQ5ODg5NzYwMzA1Mzk0LG11cFhqazIyZzF0em1GY0dCZGYxUVN" +
            "mVzRtSTVrd0hWQVEwa3RQdGJ0d01UVkVESGtTZERNWHdHNEtGeGR6MDdKOG5XMmt" +
            "5akt4ZjBSY2dtYkRab1daUENWTzdOWitDMFFrV2tYUVVPdXc3d3Awd2xSYzVKQkl" +
            "jWnJGUmhwblpiNVBTZW5DT21weUU1bDlMY01yNDFJZFBkVFo1MlZpNUhOaWFYOXR" +
            "KVE5nQ3pNRlU3TkJWdGhBR2JGeXgxc2Uva2NiQTMyc3V0eEFvNU40MUtCQXV2RFB" +
            "GSnFtaytNS2RRRWN0QWp3bjlxRFRkT2FJS3hoRUsyVlVGRzhGeVNvV1gwZDR3MFU" +
            "zNmxKcitMQ2xTaERLWU5EVzVBdnplSHBLWW8rSk9SUm5hRlRid01hTmJnQnlnYXk" +
            "yQnhROVIxVmFoVlczeDZUbDB6QmVTL1BHaW5ZZ2QrUT09LGh0dHA6Ly93azgwMDA" +
            "yNy5lbnRlbWVhLmdvb2dsZS5jb206MTIzNC9fd2luZG93cy9kZWZhdWx0LmFzcHg" +
            "/UmV0dXJuVXJsPS9fbGF5b3V0cy9FcnJvci5hc3B4JmFtcDtTb3VyY2U9L192dGl" +
            "fYmluL0dTU2l0ZURpc2NvdmVyeS5hc214PC9TUD4=; expires=Wed, 29-Feb-2" +
            "050 21:08:35 GMT; path=/; HttpOnly";
        
        final private String negotiate = 
            "NTLM TlRMTVNTUAABAAAAMTIIIAYABgAgAAAABAAEACYAAABET01BSU5IT1NU";
        
        final private String challenge = 
            "NTLM TlRMTVNTUAACAAAADAAMADgAAAA1AooiqsgsY+9WQBYAAAAAAAAAAFAAUAB" + 
            "EAAAABgGxHQAAAA9EAE8ATQBBAEkATgACAAwARABPAE0AQQBJAE4AAQAMAEQATwB" +
            "NAEEASQBOAAQADABEAE8ATQBBAEkATgADAAwARABPAE0AQQBJAE4ABwAIAMBy2+0" +
            "u98wBAAAAAA==";
        
        final private String authorization = 
            "vAG0AYQBpAG4AdQBzAGUAcgBuAGEAbQBlAGgAbwBzAHQA";        
        
        public ClaimsService(String base) {
            super();
            this.base = base;
        }
      
        @Override
        public boolean process(final SimpleRequest request, final SimpleResponse response) {
            RequestLine requestLine = request.getRequestLine();
            HttpVersion ver = requestLine.getHttpVersion();
            Header auth = request.getFirstHeader("Authorization");
            Header cookie = request.getFirstHeader("Cookie");
            
            if (!request.getFirstHeader("User-Agent").getValue().contains("Mozilla")) {
                // 1st request - rejected by non-browser useragent 
                response.setStatusLine(ver, HttpStatus.SC_FORBIDDEN);
                response.addHeader(new Header("X-Forms_Based_Auth_Required", base + customLogin));
                response.setBodyString("403 UNAUTHORIZED");
                return true;
            } else {
                if (requestLine.getUri().equals(customLogin)) {
                    // 2nd request - forward from custom login page to WIA login
                    response.setStatusLine(ver, HttpStatus.SC_MOVED_TEMPORARILY);
                    response.addHeader(new Header("Location", windowsLogin));
                    response.setBodyString(moved);
                    return true;
                } else if (requestLine.getUri().equals(windowsLogin)) {
                    if (auth == null) {
                        // 3rd request - no authentication - offer NTLM 
                        response.setStatusLine(ver, HttpStatus.SC_UNAUTHORIZED);
                        response.addHeader(new Header("WWW-Authenticate", "NTLM"));
                        return true;
                    }
                    if (auth != null && auth.getValue().equals(negotiate)) {
                        // 4th request - negotiate accepted, challenge client
                        response.setStatusLine(ver, HttpStatus.SC_UNAUTHORIZED);
                        response.addHeader(new Header("WWW-Authenticate", challenge));
                        return true;
                    } else if (auth != null && auth.getValue().endsWith(authorization)){
                        // 5th request - authn complete, set cookie
                        response.setStatusLine(ver, HttpStatus.SC_MOVED_TEMPORARILY);
                        response.addHeader(new Header("Set-Cookie", fedAuthCookie));
                        response.addHeader(new Header("Location", "/"));
                        response.setBodyString(moved);
                        return true;
                    } else {
                        // invalid credentials
                        response.setStatusLine(ver, HttpStatus.SC_UNAUTHORIZED);
                        response.addHeader(new Header("WWW-Authenticate", "NTLM"));
                        return true;
                    }
                } else if (cookie != null && 
                    cookie.getValue().contains(fedAuthCookie.substring(0, 100))) {
                    // 6th request - with cookie finish successfully
                    response.setStatusLine(ver, HttpStatus.SC_OK);
                    response.setBodyString("Success");
                    return true;
                }
            }
            response.setStatusLine(ver, HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setBodyString("Error");
            return false;
        }
    }
}
