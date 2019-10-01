package edu.depauw.itap.runner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import edu.depauw.itap.compiler.CompilerService;
import edu.depauw.itap.util.TestData;

@RunWith(MockitoJUnitRunner.class)
public class CodeRunnerServiceIntegrationTest {
  @InjectMocks
  private CodeRunnerService codeRunnerService;

  @Spy
  private CompilerService compilerService;

  @Spy
  private CodeRunnerFactory codeRunnerFactory;

  @Mock
  private MessageHeaders messageHeaders;

  @Mock
  private SimpMessagingTemplate messagingTemplate;

  @Mock
  private Clock clock;

  @Test
  public void testCreateThread() {
    when(clock.instant()).thenReturn(Instant.ofEpochSecond(1000000));

    List<String> source = Collections.singletonList(TestData.VALID_SOURCE);

    CodeRunner codeRunner =
        spy(new CodeRunnerImpl("test", messageHeaders, compilerService, messagingTemplate, clock));

    when(codeRunnerFactory.createCodeRunner(eq("test"), any(), any(), any()))
        .thenReturn(codeRunner);

    codeRunnerService.createThread("test", source, messageHeaders, compilerService,
        messagingTemplate);

    while (codeRunnerService.anyRunning()) {
    }

    verify(codeRunnerFactory, times(1)).createCodeRunner(eq("test"), same(messageHeaders),
        same(compilerService), same(messagingTemplate));
    verify(codeRunner, times(1)).setSources(same(source));
    verify(codeRunner, times(1)).run();
    verify(messagingTemplate, times(1)).convertAndSendToUser(eq("test"), eq("/topic/runner/status"),
        argThat((CodeRunnerStatus arg) -> arg.getOutput() != null
            && arg.getOutput().equals("Hello World!\n")),
        same(messageHeaders));
  }

  @Test
  public void testCreateMultipleThreads() {
    when(clock.instant()).thenReturn(Instant.ofEpochSecond(1000000));

    CodeRunner codeRunnerFirst =
        spy(new CodeRunnerImpl("test1", messageHeaders, compilerService, messagingTemplate, clock));

    CodeRunner codeRunnerSecond =
        spy(new CodeRunnerImpl("test2", messageHeaders, compilerService, messagingTemplate, clock));

    List<String> sourceFirst =
        Collections.singletonList(TestData.createValidSource("Hello Test 1!"));

    List<String> sourceSecond =
        Collections.singletonList(TestData.createValidSource("Hello Test 2!"));

    when(codeRunnerFactory.createCodeRunner(eq("test1"), any(), any(), any()))
        .thenReturn(codeRunnerFirst);

    when(codeRunnerFactory.createCodeRunner(eq("test2"), any(), any(), any()))
        .thenReturn(codeRunnerSecond);

    codeRunnerService.createThread("test1", sourceFirst, messageHeaders, compilerService,
        messagingTemplate);

    codeRunnerService.createThread("test2", sourceSecond, messageHeaders, compilerService,
        messagingTemplate);

    while (codeRunnerService.anyRunning()) {
    }

    verify(codeRunnerFactory, times(1)).createCodeRunner(eq("test1"), same(messageHeaders),
        same(compilerService), same(messagingTemplate));
    verify(codeRunnerFactory, times(1)).createCodeRunner(eq("test2"), same(messageHeaders),
        same(compilerService), same(messagingTemplate));

    verify(codeRunnerFirst, times(1)).setSources(same(sourceFirst));
    verify(codeRunnerFirst, times(1)).run();
    verify(messagingTemplate, times(1)).convertAndSendToUser(eq("test1"),
        eq("/topic/runner/status"), argThat((CodeRunnerStatus arg) -> arg.getOutput() != null
            && arg.getOutput().equals("Hello Test 1!\n")),
        same(messageHeaders));

    verify(codeRunnerSecond, times(1)).setSources(same(sourceSecond));
    verify(codeRunnerSecond, times(1)).run();
    verify(messagingTemplate, times(1)).convertAndSendToUser(eq("test2"),
        eq("/topic/runner/status"), argThat((CodeRunnerStatus arg) -> arg.getOutput() != null
            && arg.getOutput().equals("Hello Test 2!\n")),
        same(messageHeaders));
  }
}
