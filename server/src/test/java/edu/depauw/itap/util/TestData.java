package edu.depauw.itap.util;

public class TestData {
  private static final String VALID_SOURCE_TEMPLATE =
      "public class Test {\n" + "public static void main(String[] args) {\n"
          + "    System.out.println(\"%s\");\n" + " }\n" + "}\n";

  public static final String VALID_SOURCE = String.format(VALID_SOURCE_TEMPLATE, "Hello World!");

  public static final String FOR_LOOP_SOURCE = "public class Test {\n"
      + "public static void main(String[] args) {\n" + "    for(int i = 0; i < 5; i++) {\n"
      + "        System.out.println(i);\n" + "    }\n" + " }\n" + "}\n";

  public static final String MALICIOUS_SOURCE = "import java.io.BufferedWriter;\n"
      + "import java.io.BufferedReader;\n" + "import java.io.FileWriter;\n"
      + "import java.io.BufferedReader;\n" + "import java.io.FileReader;\n"
      + "public class Test {\n" + "public static void main(String[] args) throws Exception {\n"
      + "String str = \"Test\";\n" + "System.out.print(\"Running Test\");\n"
      + "String file = \"testFile.txt\";\n"
      + "BufferedWriter writer = new BufferedWriter(new FileWriter(file));\n"
      + "writer.write(str);\n" + "writer.close();\n"
      + "BufferedReader reader = new BufferedReader(new FileReader(file));\n"
      + "String currentLine = reader.readLine();\n" + "reader.close();\n"
      + "System.out.print(currentLine);\n" + "}\n" + "};\n";

  public static final String INVALID_SOURCE =
      "public class Test {\n" + "public static void main(String[] args) {\n"
          + "    NOT_VALID_FUNCTION(\"Hello World!\");\n" + " }\n" + "}\n";

  public static final String VALID_SOURCE_WITH_PACKAGE = "package test.moretest;\n" + VALID_SOURCE;

  /**
   * Creates a source file that prints the given string.
   * 
   * @param print the string to print from the source file
   * @return the compilable source file
   */
  public static String createValidSource(String print) {
    return String.format(VALID_SOURCE_TEMPLATE, print);
  }
}
