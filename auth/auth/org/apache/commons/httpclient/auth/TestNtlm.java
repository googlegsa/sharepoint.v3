package org.apache.commons.httpclient.auth;

import junit.framework.TestCase;

public class TestNtlm extends TestCase {

  public static void main(String[] args) {
  }

  public void atestType3() throws Exception{
    NTLM ntlm = new NTLM();
    String rightRes = "TlRMTVNTUAADAAAAGAAYAIAAAAAYABgAmAAAABAAEABAAAAAHgAeAFAAAAASABIAbgAAAAAAAACwAAAAAYIAAEkAVABEAFMAUABEAEUAVgBTAGgAYQByAGUAUABvAGkAbgB0AEEAZABtAGkAbgBHAE8ATwBHAEwARQBCAE8AVACY1RO0o8iOh3sED4ZUY6btbQLADmk6+RZxaKnGxRLP1DL9ovTcdZlIeCOvNtxSDqk=";
    String type3 = ntlm.getResponseFor("TlRMTVNTUAACAAAAAAAAADgAAAABggACvYlb7wQ1qzIAAAAAAAAAAAAAAAA4AAAABQLODgAAAA8=", "SHAREPOINTADMIN", "SharePointAdmin", "itdspdev.coj.net",
      "ITDSPDEV");
    System.out.println("Actual length = " + type3.length());
    System.out.println("Right length = " + rightRes.length());
    this.assertEquals(type3, rightRes);
  }

  public void testType3() throws Exception{
    NTLM ntlm = new NTLM();
    //String rightRes = "TlRMTVNTUAACAAAAAAAAADgAAAABggACvYlb7wQ1qzIAAAAAAAAAAAAAAAA4AAAABQLODgAAAA8=";
    ntlm.setCredentialCharset("UTF-16");
    String type3 = ntlm.getResponseFor("TlRMTVNTUAACAAAAAAAAADgAAAABggACvYlb7wQ1qzIAAAAAAAAAAAAAAAA4AAAABQLODgAAAA8=", "SHAREPOINTADMIN", "SharePointAdmin", "itdspdev",
      "ITDSPDEV");
    System.out.println(type3);
    //System.out.println("Actual length = " + type3.length());
//    System.out.println("Right length = " + rightRes.length());
  //  this.assertEquals(type3, rightRes);
  }

  public void testNTLM2() throws Exception
  {
    NTLM2 ntlm = new NTLM2("ent-sales-d2", "sales-admin", "SECRET01", "columbus");
    ntlm.getType3Message("TlRMTVNTUAACAAAAAAAAADgAAAABggACvYlb7wQ1qzIAAAAAAAAAAAAAAAA4AAAABQLODgAAAA8=", true);
  }
}
