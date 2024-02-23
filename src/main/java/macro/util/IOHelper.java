package macro.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public final class IOHelper {

  public static void createFolder(String path) {
    File destDir = new File(path);
    destDir.mkdirs();
  }

  public static Set<String> readFile(String file) throws IOException {
    return readFile(new File(file));
  }

  public static Set<String> readFile(File file) throws IOException {
    Set<String> elements = new HashSet<>();

    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = br.readLine()) != null) {
        elements.add(line);
      }
    }

    return elements;
  }

  public static LinkedList<String> readTextFile(File file) throws IOException {

    LinkedList<String> textfile = new LinkedList<String>();

    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = br.readLine()) != null) {
        textfile.add(line);
      }
    }
    return textfile;
  }

  public static void writeString(String destFile, String s) {
    if (s != null) {
      try {
        BufferedWriter bw = new BufferedWriter(new FileWriter(destFile));
        bw.write(s);
        bw.close();
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }

  public static void writeAppendSet(Set<String> l, String destFile) {
    Iterator<String> lIter = l.iterator();
    while (lIter.hasNext()) {
      String line = lIter.next();
      writeAppend(line, destFile);
    }
  }

  public static void writeAppendList(List<String> l, String destFile) {
    Iterator<String> lIter = l.iterator();
    while (lIter.hasNext()) {
      String line = lIter.next();
      writeAppend(line, destFile);
    }
  }

  public static void writeAppend(String s, String destFile) {
    try {
      BufferedWriter bw = new BufferedWriter(new FileWriter(destFile, true));
      bw.write(s);
      bw.newLine();
      bw.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public static void writeStringSet(String destFile, Set<String> patterns) {
    if (!patterns.isEmpty()) {
      try {
        BufferedWriter bw = new BufferedWriter(new FileWriter(destFile));
        Iterator it = patterns.iterator();

        while (it.hasNext()) {
          bw.write((String) it.next());
          bw.newLine();
        }
        bw.close();
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }
}
