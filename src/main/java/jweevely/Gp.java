package jweevely;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * it comes from:
 * http://www.java2s.com/Code/Java/Regular-Expressions/Acommandlinegreplikeprogram.htm
 * <p>
 * A command-line grep-like program. No options,
 * but takes a pattern and an arbitrary list of text files.
 */
public class Gp {
  /**
   * The pattern we're looking for
   */
  protected Pattern pattern;
  /**
   * The matcher for this pattern
   */
  protected Matcher matcher;

  /**
   * Construct a Gp program
   */
  public Gp(String patt) {
    pattern = Pattern.compile(patt);
    matcher = pattern.matcher("");
  }

  /**
   * Do the work of scanning one file
   *
   * @param inputFile     BufferedReader object already open
   * @param fileName      String Name of the input file
   * @param printFileName Boolean - true to print filename before lines that match.
   */
  public void process(BufferedReader inputFile, String fileName,
                      boolean printFileName) {

    String inputLine;

    try {
      while ((inputLine = inputFile.readLine()) != null) {
        matcher.reset(inputLine);
        if (matcher.lookingAt()) {
          if (printFileName) {
            System.out.print(fileName + ": ");
          }
          System.out.println(inputLine);
        }
      }
      inputFile.close();
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }

  /**
   * Main will make a Grep object for the pattern, and run it on all input
   * files listed in argv.
   */
  public static void main(String[] argv) throws Exception {

    if (argv.length < 1) {
      System.err.println("Usage: Gp pattern [filename]");
      System.exit(1);
    }

    Gp gp = new Gp(argv[0]);

    if (argv.length == 1)
      gp.process(new BufferedReader(new InputStreamReader(System.in)),
          "(standard input)", false);
    else
      for (int i = 1; i < argv.length; i++) {
        gp.process(new BufferedReader(new FileReader(argv[i])),
            argv[i], true);
      }
  }

}