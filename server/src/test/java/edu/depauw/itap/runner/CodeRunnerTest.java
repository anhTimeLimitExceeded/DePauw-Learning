package edu.depauw.itap.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import edu.depauw.itap.compiler.CompilerResponse;
import edu.depauw.itap.compiler.CompilerService;
import edu.depauw.itap.util.TestData;
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
import org.mockito.stubbing.Answer;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;

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

  private List<String> sourceList;

  @Before
  public void setup() {
    sourceList = new ArrayList<>();
    codeRunner =
        new CodeRunnerImpl("test", messageHeaders, compilerService, messagingTemplate, clock);
  }

  @Test
  public void testCompilesCode() {
    when(clock.instant()).thenReturn(Instant.ofEpochSecond(1000000));

    sourceList.add(TestData.createValidSource());
    codeRunner.setSources(sourceList);
    codeRunner.run();

    verify(compilerService, times(1)).compile("test", sourceList);
  }

  @Test
  public void testSendStatusUpdates() {
    when(clock.instant()).thenReturn(Instant.ofEpochSecond(1000000));

    // Check that the calls were made in order
    InOrder inOrder = Mockito.inOrder(messagingTemplate);

    sourceList.add(TestData.createValidSource());
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

    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testSendOutput() {
    when(clock.instant()).thenReturn(Instant.ofEpochSecond(1000000));

    sourceList.add(TestData.createValidSource());
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

    sourceList.add(TestData.getForLoopSource());
    codeRunner.setSources(sourceList);
    codeRunner.run();

    for (int i = 0; i < 5; i++) {
      final int n = i;
      inOrder.verify(messagingTemplate, timeout(100).times(1)).convertAndSendToUser(eq("test"),
          eq("/topic/runner/status"), argThat((CodeRunnerStatus arg) -> arg.getOutput() != null
              && arg.getOutput().equals(Integer.toString(n) + "\n")),
          same(messageHeaders));
    }
  }

  @Test
  public void testInvalidCode() {
    sourceList.add(TestData.getInvalidSource());
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
  public void testTimesOut() {
    when(clock.instant()).thenAnswer(new Answer<Instant>() {
      private long count = 1000000;

      public Instant answer(InvocationOnMock invocation) {
        count += 5;
        return Instant.ofEpochSecond(count);
      }
    });

    sourceList.add(TestData.getTimeOutSource());
    codeRunner.setSources(sourceList);
    try {
      codeRunner.run();
    } catch (RuntimeException e) {
      // Ignore as it is expected
    }

    verify(messagingTemplate, times(1)).convertAndSendToUser(eq("test"), eq("/topic/runner/status"),
        argThat((CodeRunnerStatus arg) -> arg.getStatus() != null
            && arg.getStatus().equals(RunnerStatus.TIMED_OUT)),
        same(messageHeaders));
  }

  @Test
  public void testUsesInput() {
    when(clock.instant()).thenReturn(Instant.ofEpochSecond(1000000));

    StringBuilder cummulativeOutput = new StringBuilder();

    doAnswer(invocation -> {
      Object[] args = invocation.getArguments();
      for (Object object : args) {
        if (object instanceof CodeRunnerStatus) {
          CodeRunnerStatus status = (CodeRunnerStatus) object;
          if (status.getOutput() != null) {
            cummulativeOutput.append(status.getOutput());
          }
        }
      }
      return null;
    }).when(messagingTemplate).convertAndSendToUser(eq("test"), eq("/topic/runner/status"), any(),
        same(messageHeaders));

    sourceList.add(TestData.getInputIdentitySource());
    codeRunner.setSources(sourceList);
    codeRunner.addInput("Test input\n");
    try {
      codeRunner.run();
    } catch (RuntimeException e) {
      // Ignore as it is expected
    }

    String output = cummulativeOutput.toString();

    assertThat(output).isEqualTo("Test input");
  }

  @Test
  public void testMaliciousCode() {
    when(clock.instant()).thenAnswer(new Answer<Instant>() {
      private long count = 1000000;

      public Instant answer(InvocationOnMock invocation) {
        count += 2;
        return Instant.ofEpochSecond(count);
      }
    });

    InOrder inOrder = Mockito.inOrder(messagingTemplate);

    sourceList.add(TestData.getMaliciousSource());
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
    inOrder.verify(messagingTemplate, times(1)).convertAndSendToUser(eq("test"),
        eq("/topic/runner/status"), argThat((CodeRunnerStatus arg) -> arg.getStatus() != null
            && arg.getStatus().equals(RunnerStatus.TIMED_OUT)),
        same(messageHeaders));
  }

  @Test
  public void testFilesProperlyCleanedUp() {
    when(clock.instant()).thenReturn(Instant.ofEpochSecond(1000000));

    sourceList.add(TestData.createValidSource());
    codeRunner.setSources(sourceList);
    codeRunner.run();

    assertThat(CompilerService.getDirectoryPath("test").toFile()).doesNotExist();
  }
}
