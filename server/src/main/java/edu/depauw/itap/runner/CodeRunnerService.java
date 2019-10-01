package edu.depauw.itap.runner;

import edu.depauw.itap.compiler.CompilerService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class CodeRunnerService {
  @Autowired
  private CompilerService compilerService;

  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  @Autowired
  private CodeRunnerFactory codeRunnerFactory;

  private final Map<String, CodeRunner> sessionToCodeRunner;
  private final Map<String, Thread> sessionToThread;

  public CodeRunnerService() {
    this.sessionToCodeRunner = new HashMap<>();
    this.sessionToThread = new HashMap<>();
  }

  /**
   * Creates a code running thread for a given session and sources with the given message headers
   * and the messaging template.
   */
  public void createThread(String session, List<String> sources, MessageHeaders messageHeaders) {
    CodeRunner runner = sessionToCodeRunner.computeIfAbsent(session, (k) -> codeRunnerFactory
        .createCodeRunner(session, messageHeaders, compilerService, messagingTemplate));
    runner.setSources(sources);
    Thread runnerThread = new Thread(runner);
    sessionToThread.put(session, runnerThread);
    runnerThread.start();
  }
}
