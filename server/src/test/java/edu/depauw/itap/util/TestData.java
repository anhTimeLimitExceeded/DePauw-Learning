package edu.depauw.itap.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestData {
  /**
   * Creates a source file that prints the given string.
   * 
   * @param print the string to print from the source file
   * @return the compilable source file
   */
  public static String createValidSource(String print) {
    return String.format(getSource("ValidSourceTemplate.txt"), print);
  }

  /**
   * Creates a source file that prints "Hello World!".
   * 
   * @return the compilable source file
   */
  public static String createValidSource() {
    return createValidSource("Hello World!");
  }

  public static String getValidSourceWithPackage() {
    return "package test.moretest;\n" + createValidSource();
  }

  public static String getForLoopSource() {
    return getSource("ForLoopSource.txt");
  }

  public static String getInvalidSource() {
    return getSource("InvalidSource.txt");
  }

  public static String getMaliciousSource() {
    return getSource("MaliciousSource.txt");
  }

  private static String getSource(String file) {
    // get file from classpath, resources folder

    ClassLoader classLoader = TestData.class.getClassLoader();

    URL resource = classLoader.getResource("data/" + file);

    if (resource == null) {
      throw new IllegalArgumentException("File is not found!");
    }

    try {
      return new String(Files.readAllBytes(Paths.get(resource.toURI())));
    } catch (IOException | URISyntaxException e) {
      e.printStackTrace();
      return null;
    }
  }
}
