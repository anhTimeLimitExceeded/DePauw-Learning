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
import edu.depauw.itap.util.TestData;

@RunWith(JUnit4.class)
public class CompilerServiceTest {
  CompilerService compilerService;

  @Before
  public void setup() {
    compilerService = new CompilerService();
  }

  @Test
  public void testCompilingWithoutSaving() {
    List<String> sources = Collections.singletonList(TestData.createValidSource());

    List<CompilerResult> results = compilerService.compileWithoutSaving("test", sources);

    assertThat(results).isNotNull();
    assertThat(results).isEmpty();
  }

  @Test
  public void testCompilingWithoutSavingDoesNotSaveFiles() {
    List<String> sources = Collections.singletonList(TestData.createValidSource());

    compilerService.compileWithoutSaving("test", sources);

    File tempRoot = CompilerService.getDirectoryPath("test").toFile();
    assertThat(tempRoot).doesNotExist();
  }

  @Test
  public void testCompiling() {
    List<String> sources = Collections.singletonList(TestData.createValidSource());

    List<CompilerResult> results = compilerService.compile("test", sources);

    assertThat(results).isNotNull();
    assertThat(results).isEmpty();
  }

  @Test
  public void testCompilingSaveFiles() {
    List<String> sources = Collections.singletonList(TestData.createValidSource());

    compilerService.compile("test", sources);

    File tempRoot = CompilerService.getDirectoryPath("test").toFile();
    File tempClass = tempRoot.toPath().resolve("Test.class").toFile();
    assertThat(tempRoot).exists();
    assertThat(tempRoot.listFiles()).isNotEmpty();
    assertThat(tempClass).exists();
  }

  @Test
  public void testCompilingInvalidSource() {
    List<String> sources = Collections.singletonList(TestData.getInvalidSource());

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
    String className = CompilerService.getFullyQualifiedClassName(TestData.createValidSource());

    assertThat(className).isEqualTo("Test");
  }

  @Test
  public void testGetClassNameWithPackage() {
    String className =
        CompilerService.getFullyQualifiedClassName(TestData.getValidSourceWithPackage());

    assertThat(className).isEqualTo("test.moretest.Test");
  }

  @After
  public void teardown() {
    File tempRoot = CompilerService.getDirectoryPath("test").toFile();
    if (tempRoot.exists()) {
      CompilerService.deleteDirectory(tempRoot);
    }
  }
}
