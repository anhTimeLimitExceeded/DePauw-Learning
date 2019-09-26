package edu.depauw.itap.compiler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

@RunWith(MockitoJUnitRunner.class)
public class CompilerControllerTest {
  private static String VALID_SOURCE =
      "public class Test {\n" + "public static void main(String[] args) {\n"
          + "    System.out.println(\"Hello World!\");\n" + " }\n" + "}\n";

  @InjectMocks
  CompilerController compilerController;

  @Mock
  CompilerService compilerService;

  @Mock
  SimpMessageHeaderAccessor headerAccessor;

  @Test
  public void testControllerCompiles() {
    when(headerAccessor.getSessionId()).thenReturn("test");
    when(compilerService.compileWithoutSaving(anyString(), any())).thenReturn(new ArrayList<>());

    CompilerSources compilerSources =
        new CompilerSources().setSources(Collections.singletonList(VALID_SOURCE));

    CompilerResponse response = compilerController.compile(compilerSources, headerAccessor);

    assertThat(response).isNotNull();
    assertThat(response.getResults()).isEmpty();
  }

}
