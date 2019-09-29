package edu.depauw.itap.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import edu.depauw.itap.compiler.CompilerResponse;
import edu.depauw.itap.compiler.CompilerService;
import edu.depauw.itap.util.TestData;

@RunWith(MockitoJUnitRunner.class)
public class CodeRunnerTest {
  @Spy
  private CompilerService compilerService;

  @Mock
  private MessageHeaders messageHeaders;

  @Mock
  private SimpMessagingTemplate messagingTemplate;

  @Mock
  private Clock clock;

  private CodeRunner codeRunner;

  private List<String> sourceList = new ArrayList<>();

  @Before
  public void setup() {
    codeRunner = new CodeRunner("test", messageHeaders, compilerService, messagingTemplate, clock);
  }

  @Test
  public void testCompilesCode() {
    when(clock.instant()).thenReturn(Instant.ofEpochSecond(1000000));

    sourceList.add(TestData.VALID_SOURCE);
    codeRunner.setSources(sourceList);
    codeRunner.run();

    verify(compilerService, times(1)).compile("test", sourceList);
  }

  @Test
  public void testSendStatusUpdates() {
    when(clock.instant()).thenReturn(Instant.ofEpochSecond(1000000));

    // Check that the calls were made in order
    InOrder inOrder = Mockito.inOrder(messagingTemplate);

    sourceList.add(TestData.VALID_SOURCE);
    codeRunner.setSources(sourceList);
    codeRunner.run();

    inOrder.verify(messagingTemplate, atLeast(1)).convertAndSendToUser(eq("test"),
        eq("/topic/runner/status"),
        argThat((CodeRunnerStatus arg) -> arg.getStatus().equals(RunnerStatus.RUNNING)),
        same(messageHeaders));

    inOrder.verify(messagingTemplate, times(1)).convertAndSendToUser(eq("test"),
        eq("/topic/runner/status"),
        argThat((CodeRunnerStatus arg) -> arg.getStatus().equals(RunnerStatus.STOPPED)),
        same(messageHeaders));

    verifyNoMoreInteractions(messagingTemplate);
  }

  @Test
  public void testSendOutput() {
    when(clock.instant()).thenReturn(Instant.ofEpochSecond(1000000));

    sourceList.add(TestData.VALID_SOURCE);
    codeRunner.setSources(sourceList);
    codeRunner.run();

    verify(messagingTemplate, times(1)).convertAndSendToUser(eq("test"), eq("/topic/runner/status"),
        argThat((CodeRunnerStatus arg) -> arg.getOutput() != null
            && arg.getOutput().equals("Hello World!\n")),
        same(messageHeaders));
  }

  @Test
  public void testSendOutputInOrder() {
    when(clock.instant()).thenReturn(Instant.ofEpochSecond(1000000));

    InOrder inOrder = Mockito.inOrder(messagingTemplate);

    sourceList.add(TestData.FOR_LOOP_SOURCE);
    codeRunner.setSources(sourceList);
    codeRunner.run();

    for (int i = 0; i < 5; i++) {
      final int n = i;
      inOrder.verify(messagingTemplate, times(1)).convertAndSendToUser(eq("test"),
          eq("/topic/runner/status"), argThat((CodeRunnerStatus arg) -> arg.getOutput() != null
              && arg.getOutput().equals(Integer.toString(n) + "\n")),
          same(messageHeaders));
    }
  }

  @Test
  public void testInvalidCode() {
    sourceList.add(TestData.INVALID_SOURCE);
    codeRunner.setSources(sourceList);
    try {
      codeRunner.run();
    } catch (RuntimeException e) {
      // Ignore as it is expected
    }

    verify(messagingTemplate, times(1)).convertAndSendToUser(eq("test"), eq("/topic/compile"),
        argThat((CompilerResponse arg) -> arg.getResults() != null && !arg.getResults().isEmpty()),
        same(messageHeaders));
  }

  @Test
  public void testMaliciousCode() {
    when(clock.instant()).thenAnswer((InvocationOnMock invocation) -> Instant.now());

    InOrder inOrder = Mockito.inOrder(messagingTemplate);

    sourceList.add(TestData.MALICIOUS_SOURCE);
    codeRunner.setSources(sourceList);
    codeRunner.run();

    inOrder.verify(messagingTemplate, times(1)).convertAndSendToUser(eq("test"),
        eq("/topic/runner/status"), argThat((CodeRunnerStatus arg) -> arg.getOutput() != null
            && arg.getOutput().equals("Running Test")),
        same(messageHeaders));
    // Check that there was no output after writing and reading a file.
    inOrder.verify(messagingTemplate, never()).convertAndSendToUser(eq("test"),
        eq("/topic/runner/status"), argThat((CodeRunnerStatus arg) -> arg.getOutput() != null),
        same(messageHeaders));
  }

  @Test
  public void testFilesProperlyCleanedUp() {
    when(clock.instant()).thenReturn(Instant.ofEpochSecond(1000000));

    sourceList.add(TestData.VALID_SOURCE);
    codeRunner.setSources(sourceList);
    codeRunner.run();

    assertThat(CompilerService.getDirectoryPath("test").toFile()).doesNotExist();
  }
}
