package org.apache.commons.httpclient.auth;

import org.apache.commons.codec.binary.Base64;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

// Type 1 Message
class Type1Msg {
  char protocol[] = new char[8];
  char type;
  char zero1[] = new char[3];
  short flags;
  char zero2[] = new char[2];
  short dom_len1;
  short dom_len2;
  short dom_off;
  char zero3[] = new char[2];
  short host_len1;
  short host_len2;
  short host_off;
  char zero4[] = new char[2];
}

// Type 2 Message
class Type2Msg {
  char protocol[] = new char[8];
  char type;
  char zero1[] = new char[7];
  short msg_len;
  char zero2[] = new char[2];
  short flags;
  char zero3[] = new char[2];
  char nonce[] = new char[8];
  char zero4[] = new char[8];
}

// Type 3 Message
class Type3Msg {
  char protocol[] = new char[8];
  char type;
  char zero1[] = new char[3];
  short lm_resp_len1;
  short lm_resp_len2;
  short lm_resp_off;
  char zero2[] = new char[2];
  short nt_resp_len1;
  short nt_resp_len2;
  short nt_resp_off;
  char zero3[] = new char[2];
  short dom_len1;
  short dom_len2;
  short dom_off;
  char zero4[] = new char[2];
  short user_len1;
  short user_len2;
  short user_off;
  char zero5[] = new char[2];
  short host_len1;
  short host_len2;
  short host_off;
  char zero6[] = new char[6];
  short msg_len;
  char zero7[] = new char[2];
  short flags;
  char zero8[] = new char[2];
}

public class NTLM2 {
  String domainName, userName, password, hostName;
  static Cipher cipher;

  static {
    try {
      cipher = Cipher.getInstance("DES/ECB/NOPadding");// //"DESede/CBC/PKCS5Padding"
    } catch (Exception e) {

    }
  }

  public NTLM2(String domainname, String username, String password,
      String hostname) {
    domainName = domainname.toUpperCase();
    userName = username.toUpperCase();
    this.password = password;
    hostName = hostname;
  }

  // A Type1Message is sent first to the server requesting a nonce
  // It contains only the hostname and domainname
  String getType1Message(boolean base64) {
    Type1Msg t1m = new Type1Msg();

    "NTLMSSP".getChars(0, 7, t1m.protocol, 0);
    t1m.type = 1;
    t1m.flags = (short) 0xb203;
    t1m.host_len1 = (short) hostName.length();
    t1m.host_len2 = t1m.host_len1;
    t1m.host_off = 0x20;
    t1m.dom_len2 = (short) domainName.length();
    t1m.dom_len1 = t1m.dom_len2;
    t1m.dom_off = (short) (t1m.host_off + t1m.host_len1);
    return null;
  }

  // The server will reply with a Type2Message, this will contain the
  // nounce. The user's password is then used to encrypt the nounce
  // in two ways. This is then sent back to the server in the Type3Message
  String getType3Message(String type2Message, boolean base64) {
    byte[] t2m;
    /* decode the message from the server */
    if (base64) {

      t2m = Base64.decodeBase64(type2Message.getBytes());
    } else {
      int size = type2Message.getBytes().length;
      t2m = new byte[size];
      System.arraycopy(type2Message.getBytes(), 0, t2m, 0, size);
    }

    byte[] nonce = new byte[8];
    System.arraycopy(this.hexStringToByte("0123456789abcdef".toCharArray()), 0, nonce, 0, 8);
    
    /* create responses */
    byte[] lm_resp = CreateLanManagerResponse(t2m, nonce);
    // char []nt_resp = CreateNTResponse(t2m, nt_resp);

    /* create response message */
    Type3Msg t3m = new Type3Msg();

    /* create unicode versions of the strings */
    String unicode_domainname = ASCII7BITToUnicode(domainName);
    String unicode_hostname = ASCII7BITToUnicode(hostName);
    String unicode_username = ASCII7BITToUnicode(userName);
    /* initialize the message */
    /*
     * "NTLMSSP".getChars(0, 7, t3m.protocol, 0); t3m.type = 3; t3m.dom_len1 =
     * t3m.dom_len2 = (short)unicode_domainname.length(); t3m.dom_off = 0x40;
     * t3m.user_len1 = t3m.user_len2 = unicode_username.length(); t3m.user_off =
     * t3m.dom_off + t3m.dom_len1; t3m.host_len1 = t3m.host_len2 =
     * unicode_hostname.length(); t3m.host_off = t3m.user_off + t3m.user_len1;
     * t3m.lm_resp_len1 = t3m.lm_resp_len2 = 0x18; t3m.lm_resp_off =
     * t3m.host_off + t3m.host_len1; t3m.nt_resp_len1 = t3m.nt_resp_len2 = 0x18;
     * t3m.nt_resp_off = t3m.lm_resp_off + t3m.lm_resp_len1; t3m.msg_len =
     * t3m.nt_resp_off + t3m.nt_resp_len1; t3m.flags = 0x8201;
     */
    return null;
  }

  // Helper for encrypting the nonce using the LanManager scheme based on DES
  byte[] CreateLanManagerResponse(byte t2m[], byte[] nonce) {
    byte magic[] = {0x4B, 0x47, 0x53, 0x21, 0x40, 0x23, 0x24, 0x25};

    /* setup LanManager password */
    byte[] lm_pw = new byte[14];
    System.arraycopy(password.getBytes(), 0, lm_pw, 0,
      password.getBytes().length);
    int len = password.length();
    if (len > 14)
      len = 14;
    /* create LanManager hashed password */
    try {
      byte[] key7 = new byte[7];
      byte[] lmhash = new byte[21];
      for (int i = 0; i < 2; ++i) {
        System.arraycopy(lm_pw, i * 7, key7, 0, 7);
        byte[] key8 = setupDesKey(key7);
        byte[] lm_hpw = encrypt(key8, magic);
        System.arraycopy(lm_hpw, 0, lmhash, i * 8, 8);
      }
      // now use the lower 7
      dumpHex(lmhash);
      byte[] lm_resp = new byte[24];
      for (int i = 0; i < 3; ++i) {
        System.arraycopy(lmhash, i * 7, key7, 0, 7);
        byte[] key8 = setupDesKey(key7);
        dumpBinary(key8);
        byte[] lm_hpw = encrypt(key8, nonce);
        dumpHex(lm_hpw);
        System.arraycopy(lm_hpw, 0, lm_resp, i * 8, 8);
      }
      dumpHex(nonce);
      dumpHex(lm_resp);
      return lm_resp;
    } catch (Exception e) {
      return null;
    }
  }

  private byte[] encrypt(byte[] key, byte[] data)
      throws NoSuchPaddingException, NoSuchAlgorithmException,
      InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    byte[] e8 = new byte[8];
    SecretKeySpec skey = new SecretKeySpec(key, "DES");
    // ,
    // DES/CBC/NoPadding,DES/ECB/PKCS5Padding
    cipher.init(Cipher.ENCRYPT_MODE, skey);
    return cipher.doFinal(data);
  }

  private byte[] setupDesKey(byte[] key7) {
    byte[] key8 = new byte[8];
    // non-parity-adjusted
    // expand from 7 to 8 bytes by shifting lower bits, and pad with 0s
    // 01010011 01000101 01000011 01010010 01000101 01010100 00110000
    // 01010010 10100010 01010000 01101010 00100100 00101010 01010000 01100000
    key8[7] = (byte) (key7[6] << 1);
    for (int i = 6; i > 0; --i) {
      key8[i] = (byte) (((key7[i] >>> i + 1 | key7[i - 1] << (8 - i - 1))) << 1);
    }
    key8[0] = (byte) ((key7[0] >>> 1) << 1);

    // apply odd-parity
    for (int i = 0; i < key8.length; ++i) {
      boolean bOdd = false;
      int count = 0;
      for (int j = 7; j >= 0; --j) {
        count += key8[i] >>> j & 1;
      }
      if ((count & 1) == 0)
        key8[i] += 1;
    }
    return key8;
  }

  String ASCII7BITToUnicode(String ansi) {
    return null;
  }

  public void dumpHex(byte[] dump) {
    String sDump = "";
    System.out.print("0x");
    for (int i = 0; i < dump.length; ++i) {
      sDump += byteToHex(dump[i]);
    }
    System.out.println(sDump);

  }

  public String byteToHex(byte b) {
    int i = b & 0xFF;
    return Integer.toHexString(i);
  }

  public byte hexTobyte(String ch) {
    return (byte) Integer.parseInt(ch, 16);
  }

  public String byteToHex(String fieldName, byte b) {
    return fieldName + ": " + byteToHex(b);
  }

  public void dumpBinary(byte[] b) {
    for (int i = 0; i < b.length; ++i) {
      for (int j = 7; j >= 0; --j) {
        System.out.print(b[i] >> j & 1);
      }
      System.out.print(" ");
    }
    System.out.println("");

  }

  public static byte[] hexStringToByte(char str[]) {
    int i, cnt = 0;
    byte result[] = new byte[str.length / 2];
    char c = 0;
    for (i = 0; i < str.length; i++) {
      if (str[i] >= '0' && str[i] <= '9') {
        c |= (str[i] - '0');
      } else if (str[i] >= 'A' && str[i] <= 'F') {
        c |= ((str[i] - 'A') + 10);
      } else if (str[i] >= 'a' && str[i] <= 'f') {
        c |= ((str[i] - 'a') + 10);
      }
      if (i % 2 == 1) {
        result[cnt++] = (byte) c;
        c = 0;
      } else {
        c <<= 4;
      }
    }
    return result;

  } // End of function read_hex_chars

  public static void main (String[]args)
  {
    //String type2 = "TlRMTVNTUAACAAAAAAAAADgAAAABAgAClp+uFDz4LdkAAAAAAAAAAAAAAAA4AAAABQLODgAAAA8="; 
    //"TlRMTVNTUAACAAAAAAAAADgAAAABAgAC6hcnH8BqLXUAAAAAAAAAAAAAAAA4AAAABQLODgAAAA8=";
//    String type2 = "1";
    byte[]t2m = new byte[1]; //Base64.decodeBase64(type2.getBytes());
    t2m[0] = 1;
    NTLM2 ntlm2 = new NTLM2("", "", "", "");
    ntlm2.dumpBinary(t2m);
    //ntlm2.dumpHex(t2m);
  }
}
