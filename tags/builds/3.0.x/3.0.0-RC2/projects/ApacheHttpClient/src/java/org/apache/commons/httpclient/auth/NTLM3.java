/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/java/org/apache/commons/httpclient/auth/NTLM.java,v 1.11 2004/05/13 04:02:00 mbecke Exp $
 * $Revision: 155418 $
 * $Date: 2005-02-26 08:01:52 -0500 (Sat, 26 Feb 2005) $
 *
 * ====================================================================
 *
 *  Copyright 2002-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.commons.httpclient.auth;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

import jcifs.Config;
import jcifs.http.NtlmSsp;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;

/**
 * Provides an implementation of the NTLM authentication protocol.
 * <p>
 * This class provides methods for generating authentication
 * challenge responses for the NTLM authentication protocol.  The NTLM
 * protocol is a proprietary Microsoft protocol and as such no RFC
 * exists for it.  This class is based upon the reverse engineering
 * efforts of a wide range of people.</p>
 *
 * <p>Please note that an implementation of JCE must be correctly installed and configured when
 * using NTLM support.</p>
 *
 * <p>This class should not be used externally to HttpClient as it's API is specifically
 * designed to work with HttpClient's use case, in particular it's connection management.</p>
 *
 * @author amit_kagrawal
 */
final class NTLM3 {

    /** Character encoding */
    public static final String DEFAULT_CHARSET = "ASCII";

    /** The current response */
    private byte[] currentResponse;

    /** The current position */
    private int currentPosition = 0;

    /** The character set to use for encoding the credentials */
    private String credentialCharset = DEFAULT_CHARSET;
    
    private static final Log LOG = LogFactory.getLog(NTLM3.class);
    static
    {
      Config.setProperty("jcifs.encoding", "UTF-8");
      Config.setProperty("jcifs.smb.lmCompatibility", "5");
    }
    /**
     * Returns the response for the given message.
     *
     * @param message the message that was received from the server.
     * @param username the username to authenticate with.
     * @param password the password to authenticate with.
     * @param host The host.
     * @param domain the NT domain to authenticate in.
     * @return The response.
     * @throws HttpException If the messages cannot be retrieved.
     */
    public final String getResponseFor(String message,
            String username, String password, String host, String domain)
            throws AuthenticationException {
                
        final String response;
        if (message == null || message.trim().equals("")) {
          Type1Message t1m = new Type1Message();
          t1m.setSuppliedDomain(domain);
          t1m.setSuppliedWorkstation(host);
          
          /*
           * Following constants provided by JCIFS library has been added to support message integrity,
           * confidentiality, session security and 128-bit encryption
           */
          t1m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_SIGN,true);
          t1m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_SEAL,true);
          t1m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_NTLM2,true);
          t1m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_128,true);
          
          return EncodingUtil.getAsciiString(Base64.encodeBase64(t1m.toByteArray()));

        } else {
          try
          {
          Type2Message t2m = parseType2Message(message);
          
          t2m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_SIGN,true);
          t2m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_SEAL,true);
          t2m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_NTLM2,true);
          t2m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_128,true);
          // The below constructor of Type3Message of jcifs library has been changed
          // to fix issues while updating latest jcifs-1.3.5.jar in SP connector.
          // The extra parameter int represents flag to support ntlm2, support message integrity,
          // confidentiality, session security and 128-bit encryption while creating TYpe3Message. 
          // As the immediate lines of code below to the constructor set all these required flags, passing 0 as a parameter/falg to create 
          // Type3Message object is valid. Please refer the below URL for more details.
          // http://code.google.com/p/google-enterprise-connector-sharepoint/issues/detail?id=63

		  Type3Message t3m = new Type3Message(t2m, password, domain, username, host, 0);

          t3m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_SIGN,true);
          t3m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_SEAL,true);
          t3m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_NTLM2,true);
          t3m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_128,true);

          return EncodingUtil.getAsciiString(Base64.encodeBase64(t3m.toByteArray()));
          }catch(IOException e)
          {
            throw new AuthenticationException(e.getMessage());
          }
        }
        
    }

    public final String getType1Message(String host, String domain)
        throws AuthenticationException {
      Type1Message t1m = new Type1Message();
      t1m.setSuppliedDomain(domain);
      t1m.setSuppliedWorkstation(host);
      
      t1m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_SIGN,true);
      t1m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_SEAL,true);
      t1m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_NTLM2,true);
      t1m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_128,true);

      
      return EncodingUtil.getAsciiString(Base64.encodeBase64(t1m.toByteArray()));
    }
    
    public final String getType3Message( String username, String password, String host, String domain, String message)
        throws AuthenticationException {
    final String response;
    if (message == null || message.trim().equals("")) {
      Type1Message t1m = new Type1Message();
      t1m.setSuppliedDomain(domain);
      t1m.setSuppliedWorkstation(host);
      
      t1m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_SIGN,true);
      t1m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_SEAL,true);
      t1m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_NTLM2,true);
      t1m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_128,true);

      
      return t1m.toString();
    } else {
      try
      {
      Type2Message t2m = parseType2Message(message);
      
      t2m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_SIGN,true);
      t2m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_SEAL,true);
      t2m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_NTLM2,true);
      t2m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_128,true);
      // The below constructor of Type3Message of jcifs library has been changed
      // to fix issues while updating latest jcifs-1.3.5.jar in SP connector 2.8v.
      // The extra parameter int represents flag to support ntlm2, support message integrity,
      // confidentiality, session security and 128-bit encryption while creating TYpe3Message 
      // object.as the immediate lines of code below to this constructor set all these required flags, passing 0 as a parameter/falg to create 
      // Type3Message object is valid. Please refer the below URL for more details.
      // http://code.google.com/p/google-enterprise-connector-sharepoint/issues/detail?id=63

      Type3Message t3m = new Type3Message(t2m, password, domain, username, host, 0);

      t3m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_SIGN,true);
      t3m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_SEAL,true);
      t3m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_NTLM2,true);
      t3m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_128,true);
      
      return EncodingUtil.getAsciiString(Base64.encodeBase64(t3m.toByteArray()));
      }catch(IOException e)
      {
        throw new AuthenticationException(e.getMessage());
      }
    }
    
}

    /** 
     * Extracts the server nonce out of the given message type 2.
     * 
     * @param message the String containing the base64 encoded message.
     * @return an array of 8 bytes that the server sent to be used when
     * hashing the password.
     */
    public Type2Message parseType2Message(String message) throws IOException{
        // Decode the message first.
        byte[] msg = Base64.decodeBase64(EncodingUtil.getBytes(message, DEFAULT_CHARSET));
        Type2Message t2m = new Type2Message(msg); 
        
        t2m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_SIGN,true);
        t2m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_SEAL,true);
        t2m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_NTLM2,true);
        t2m.setFlag(NtlmSsp.NTLMSSP_NEGOTIATE_128,true);

        return t2m;
    }

    
    /**
     * @return Returns the credentialCharset.
     */
    public String getCredentialCharset() {
        return credentialCharset;
    }

    /**
     * @param credentialCharset The credentialCharset to set.
     */
    public void setCredentialCharset(String credentialCharset) {
        this.credentialCharset = credentialCharset;
    }

}
