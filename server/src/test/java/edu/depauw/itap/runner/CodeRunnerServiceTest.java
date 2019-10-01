package edu.depauw.itap.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import edu.depauw.itap.compiler.CompilerService;
import edu.depauw.itap.util.TestData;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;

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

    CodeRunner codeRunner = mock(CodeRunnerImpl.class);

    when(codeRunnerFactory.createCodeRunner(eq("test"), any(), any(), any()))
        .thenReturn(codeRunner);

    codeRunnerService.createThread("test", sourceList, messageHeaders);

    verify(codeRunnerFactory, times(1)).createCodeRunner(eq("test"), same(messageHeaders),
        same(compilerService), same(messagingTemplate));
    verify(codeRunner, times(1)).setSources(same(sourceList));
    verify(codeRunner, timeout(1000).times(1)).run();
  }

  @Test
  public void testAnyThreadRunning() {
    sourceList.add(TestData.VALID_SOURCE);

    CodeRunner codeRunner = spy(new FakeCodeRunner());

    when(codeRunnerFactory.createCodeRunner(eq("test"), any(), any(), any()))
        .thenReturn(codeRunner);

    assertThat(codeRunnerService.anyRunning()).isFalse();

    codeRunnerService.createThread("test", sourceList, messageHeaders);

    while (!codeRunner.getStatus().equals(RunnerStatus.RUNNING)) {
      try {
        Thread.sleep(250);
      } catch (InterruptedException e) {
        // Ignore
      }
    }

    assertThat(codeRunnerService.anyRunning()).isTrue();

    synchronized (codeRunner) {
      codeRunner.notify();
    }

    while (!codeRunner.getStatus().equals(RunnerStatus.STOPPED)) {
      try {
        Thread.sleep(250);
      } catch (InterruptedException e) {
        // Ignore
      }
    }

    assertThat(codeRunnerService.anyRunning()).isFalse();
  }

  private class FakeCodeRunner implements CodeRunner {
    private RunnerStatus status = RunnerStatus.STOPPED;

    public FakeCodeRunner() {
    }

    @Override
    public void run() {
      this.status = RunnerStatus.RUNNING;
      synchronized (this) {
        try {
          this.wait();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      this.status = RunnerStatus.STOPPED;
    }

    @Override
    public void setSources(List<String> sources) {
    }

    @Override
    public RunnerStatus getStatus() {
      return status;
    }
  }
}
