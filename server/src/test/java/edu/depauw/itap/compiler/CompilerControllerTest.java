package edu.depauw.itap.compiler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import edu.depauw.itap.util.TestData;
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
        new CompilerSources().setSources(Collections.singletonList(TestData.createValidSource()));

    CompilerResponse response = compilerController.compile(compilerSources, headerAccessor);

    assertThat(response).isNotNull();
    assertThat(response.getResults()).isEmpty();
  }

}
