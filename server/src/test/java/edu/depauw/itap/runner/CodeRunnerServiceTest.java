package edu.depauw.itap.runner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import edu.depauw.itap.compiler.CompilerService;
import edu.depauw.itap.util.TestData;

@RunWith(MockitoJUnitRunner.class)
public class CodeRunnerServiceTest {
  @InjectMocks
  private CodeRunnerService codeRunnerService;

  @Mock
  private CompilerService compilerService;

  @Mock
  private CodeRunnerFactory codeRunnerFactory;

  @Mock
  private MessageHeaders messageHeaders;

  @Mock
  private SimpMessagingTemplate messagingTemplate;

  private List<String> sourceList = new ArrayList<>();

  @Test
  public void testCreateThread() {
    sourceList.add(TestData.VALID_SOURCE);

    CodeRunner codeRunner = mock(CodeRunner.class);

    when(codeRunnerFactory.createCodeRunner(eq("test"), any(), any(), any()))
        .thenReturn(codeRunner);

    codeRunnerService.createThread("test", sourceList, messageHeaders, compilerService,
        messagingTemplate);

    verify(codeRunnerFactory, times(1)).createCodeRunner(eq("test"), same(messageHeaders),
        same(compilerService), same(messagingTemplate));
    verify(codeRunner, times(1)).setSources(same(sourceList));
    verify(codeRunner, timeout(1000).times(1)).run();
  }
}
