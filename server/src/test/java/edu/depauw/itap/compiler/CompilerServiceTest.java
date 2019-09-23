package edu.depauw.itap.compiler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

@RunWith(JUnit4.class)
public class CompilerServiceTest {
  @InjectMocks
  CompilerService compilerService;

  @Spy
  CompilerRunnable compilerRunnable;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testCompilingSource() {
    String source = "public class Test {\n" + "public static void main(String[] args) {\n"
        + "    System.out.println(\"Hello World!\");\n" + " }\n" + "}\n";

    List<String> sources = Collections.singletonList(source);

    List<CompilerResult> results = compilerService.compile("test", sources);

    verify(compilerRunnable).compileWithoutSaving("test", sources);
    assertThat(results).isEmpty();
  }

  @Test
  public void testCompilingBadSource() {
    String source = "public class Test {\n" + "public statics void main(String[] args) {\n"
        + "    System.out.println(\"Hello World!\");\n" + " }\n" + "}\n";

    List<String> sources = Collections.singletonList(source);

    List<CompilerResult> results = compilerService.compile("test", sources);

    verify(compilerRunnable).compileWithoutSaving("test", sources);
    assertThat(results).isNotEmpty();
    assertThat(results.get(0).getClassName()).isEqualTo("Test");
    assertThat(results.get(0).getMessage()).isNotEmpty();
    assertThat(results.get(0).getSeverity()).isEqualTo("ERROR");
    assertThat(results.get(0).getStartLineNumber()).isGreaterThan(0);
    assertThat(results.get(0).getStartColumnNumber()).isGreaterThan(0);
    assertThat(results.get(0).getEndLineNumber()).isGreaterThan(0);
    assertThat(results.get(0).getEndColumnNumber()).isGreaterThan(0);
  }
}
