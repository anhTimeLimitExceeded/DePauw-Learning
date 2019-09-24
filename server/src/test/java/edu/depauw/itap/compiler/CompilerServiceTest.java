package edu.depauw.itap.compiler;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.File;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CompilerServiceTest {
  CompilerService compilerService;

  private static String VALID_SOURCE =
      "public class Test {\n" + "public static void main(String[] args) {\n"
          + "    System.out.println(\"Hello World!\");\n" + " }\n" + "}\n";

  private static String INVALID_SOURCE =
      "public class Test {\n" + "public static void main(String[] args) {\n"
          + "    NOT_VALID_FUNCTION(\"Hello World!\");\n" + " }\n" + "}\n";

  private static String VALID_SOURCE_WITH_PACKAGE = "package test.moretest;\n" + VALID_SOURCE;

  @Before
  public void setup() {
    compilerService = new CompilerService();
  }

  @Test
  public void testCompilingWithoutSaving() {
    List<String> sources = Collections.singletonList(VALID_SOURCE);

    List<CompilerResult> results = compilerService.compileWithoutSaving("test", sources);

    assertThat(results).isNotNull();
    assertThat(results).isEmpty();
  }

  @Test
  public void testCompilingWithoutSavingDoesNotSaveFiles() {
    List<String> sources = Collections.singletonList(VALID_SOURCE);

    compilerService.compileWithoutSaving("test", sources);

    File tempRoot = CompilerService.getDirectoryPath("test");
    assertThat(tempRoot).doesNotExist();
  }

  @Test
  public void testCompiling() {
    List<String> sources = Collections.singletonList(VALID_SOURCE);

    List<CompilerResult> results = compilerService.compile("test", sources);

    assertThat(results).isNotNull();
    assertThat(results).isEmpty();
  }

  @Test
  public void testCompilingSaveFiles() {
    List<String> sources = Collections.singletonList(VALID_SOURCE);

    compilerService.compile("test", sources);

    File tempRoot = CompilerService.getDirectoryPath("test");
    File tempClass = tempRoot.toPath().resolve("Test.class").toFile();
    assertThat(tempRoot).exists();
    assertThat(tempRoot.listFiles()).isNotEmpty();
    assertThat(tempClass).exists();
  }

  @Test
  public void testCompilingInvalidSource() {
    List<String> sources = Collections.singletonList(INVALID_SOURCE);

    List<CompilerResult> results = compilerService.compile("test", sources);

    assertThat(results).isNotNull();
    assertThat(results).isNotEmpty();
    assertThat(results.get(0).getClassName()).isEqualTo("Test");
    assertThat(results.get(0).getMessage()).isNotEmpty();
    assertThat(results.get(0).getSeverity()).isEqualTo("ERROR");
    assertThat(results.get(0).getStartLineNumber()).isGreaterThan(0);
    assertThat(results.get(0).getStartColumnNumber()).isGreaterThan(0);
    assertThat(results.get(0).getEndLineNumber()).isGreaterThan(0);
    assertThat(results.get(0).getEndColumnNumber()).isGreaterThan(0);
  }

  @Test
  public void testGetClassName() {
    String className = CompilerService.getFullyQualifiedClassName(VALID_SOURCE);

    assertThat(className).isEqualTo("Test");
  }

  @Test
  public void testGetClassNameWithPackage() {
    String className = CompilerService.getFullyQualifiedClassName(VALID_SOURCE_WITH_PACKAGE);

    assertThat(className).isEqualTo("test.moretest.Test");
  }

  @After
  public void teardown() {
    File tempRoot = CompilerService.getDirectoryPath("test");
    if (tempRoot.exists()) {
      deleteDirectory(tempRoot);
    }
  }

  private static boolean deleteDirectory(File directoryToBeDeleted) {
    File[] allContents = directoryToBeDeleted.listFiles();
    if (allContents != null) {
      for (File file : allContents) {
        deleteDirectory(file);
      }
    }
    return directoryToBeDeleted.delete();
  }
}
