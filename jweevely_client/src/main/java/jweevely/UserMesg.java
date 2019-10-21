package jweevely;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

//import sun.misc.BASE64Encoder;

/**
 * @author needle wang
 * 2013年 12月 08日 星期日 22:56:07 CST
 */
public class UserMesg {
  // Warn: the results of BASE64Encoder and apache_base64 are both depend on
  // each self OS such as linux and win!
  // public static BASE64Encoder encoder_64 = new BASE64Encoder();
  public static Base64 encoder_64 = new Base64();

  public UserMesg(String password) {
    this.httpclient = HttpClients.createDefault();
    this.password = password;
    String pa_md5 = to_md5(password);
    pa_identify = pa_md5.substring(pa_md5.length() - 4);
  }

  public CloseableHttpClient getHttpclient() {
    return httpclient;
  }

  public void setHttpclient(CloseableHttpClient httpclient) {
    this.httpclient = httpclient;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPa_identify() {
    return pa_identify;
  }

  public static String to_md5(String clearText) {
    MessageDigest _md5 = null;
    try {
      _md5 = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
    }
    byte digestBytes[] = _md5.digest(clearText.getBytes());
    StringBuffer digestBuffer = new StringBuffer();
    digestBuffer.setLength(0);

    for (int i = 0; i < digestBytes.length; i++) {
      String byte_hex = Integer.toHexString(digestBytes[i] & 0xFF);
      if (byte_hex.length() == 1) {
        digestBuffer.append(0);
      }
      digestBuffer.append(byte_hex);
    }
    return digestBuffer.toString();

  }

  public static String encode_to_base64(String str) {
    byte[] b = str.getBytes();
    b = new Base64().encode(b);
    return new String(b);
  }

  public static String decode_from_base64(String str) {
    byte[] b = str.getBytes();
    b = new Base64().decode(b);
    return new String(b);
  }

  /**
   * encode a String by base64, then insert it with !@#$...
   * BASE64Encoder.encodeBuffer(str.getBytes()) will add \n like od~. now
   * change to use org.apache.commons.codec.binary.Base64;
   *
   * @param str String
   * @return StringBuffer
   */
  public static StringBuffer getShuffBase64(String str) {
    char sess_cmd_charArray[] = UserMesg.encode_to_base64(str)
        .toCharArray();
    StringBuffer cool_cmd = new StringBuffer();
    char fuze_list[] = {'!', '@', '#', '$', '%', '^', '&', '*', '(', ')'};
    for (int i = 0; i < sess_cmd_charArray.length; i++) {
      if (Math.random() < 0.7) {
        cool_cmd.append(sess_cmd_charArray[i]);
      } else {
        cool_cmd.append(sess_cmd_charArray[i]).append(
            fuze_list[(int) (Math.random() * 10)]);
        // don't be so small, because the cookie encode 4 - 1 times...
        // it could be so huge... and the server would response bad
        // request,
        // so reduce the length. then unicode encoding should be
        // removed.
        if (Math.random() >= 0.91) {
          cool_cmd.append(fuze_list[(int) (Math.random() * 10)]);
        }
      }
    }
    return cool_cmd;
  }

  private CloseableHttpClient httpclient;
  private String password;
  private String pa_identify;
}
