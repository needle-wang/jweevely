package jweevely;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Test {

  public static void main(String[] args) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Runtime rt = Runtime.getRuntime();
    Process proc;

    Method m = rt.getClass().getMethod(new String(new char[]{'e', 'x', 'e', 'c'}), String[].class, String[].class, File.class);
    proc = (Process) m.invoke(rt, new String[]{"pwd"}, null, null);

    BufferedReader br = new BufferedReader(
        new InputStreamReader(proc.getInputStream()),
        4096);
    String outputline;
    while ((outputline = br.readLine()) != null) {
      System.out.println(outputline);
    }
    br.close();
    System.out.println(Float.valueOf(System.getProperty("java.version").substring(0, 3)) > 1.6);

  }

}
