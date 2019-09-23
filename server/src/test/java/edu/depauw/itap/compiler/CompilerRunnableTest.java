package edu.depauw.itap.compiler;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CompilerRunnableTest {
  CompilerRunnable compilerRunnable;

  @Before
  public void setup() {
    compilerRunnable = new CompilerRunnable();
  }

  @Test
  public void testCompilingWithoutSaving() {
    String source = "public class Test {\n" + "public static void main(String[] args) {\n"
        + "    System.out.println(\"Hello World!\");\n" + " }\n" + "}\n";

    List<String> sources = Collections.singletonList(source);

    List<CompilerResult> results = compilerRunnable.compileWithoutSaving("test", sources);

    assertThat(results).isNotNull();
    assertThat(results).isEmpty();
    File tempRoot = new File(Paths.get("").resolve("temp").toAbsolutePath().toString(), "test");
    assertThat(tempRoot).doesNotExist();
  }

  @Test
  public void testCompiling() {
    String source = "public class Test {\n" + "public static void main(String[] args) {\n"
        + "    System.out.println(\"Hello World!\");\n" + " }\n" + "}\n";

    List<String> sources = Collections.singletonList(source);

    List<CompilerResult> results = compilerRunnable.compile("test", sources);

    assertThat(results).isNotNull();
    assertThat(results).isEmpty();
    File tempRoot = new File(Paths.get("").resolve("temp").toAbsolutePath().toString(), "test");
    assertThat(tempRoot).exists();
  }

  @After
  public void teardown() {
    File tempRoot = new File(Paths.get("").resolve("temp").toAbsolutePath().toString(), "test");
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
