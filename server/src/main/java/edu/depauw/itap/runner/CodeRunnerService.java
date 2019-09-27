package edu.depauw.itap.runner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import edu.depauw.itap.compiler.CompilerService;

@Service
public class CodeRunnerService {
  @Autowired
  private CodeRunnerFactory codeRunnerFactory;

  private final Map<String, CodeRunner> sessionToCodeRunner;
  private final Map<String, Thread> sessionToThread;

  private final BlockingQueue<Thread> threadQueue;

  private final Thread queueRunner;

  public CodeRunnerService() {
    this.sessionToCodeRunner = new HashMap<>();
    this.sessionToThread = new HashMap<>();
    this.threadQueue = new LinkedBlockingQueue<>();

    this.queueRunner = new Thread(new Runnable() {
      @Override
      public void run() {
        while (true) {
          try {
            Thread currentThread = threadQueue.poll();
            currentThread.start();
            currentThread.join();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    });
    this.queueRunner.start();
  }

  public boolean anyRunning() {
    return sessionToThread.entrySet().stream().map(Map.Entry::getValue)
        .anyMatch((thread) -> thread.isAlive());
  }

  public void createThread(String session, List<String> sources, MessageHeaders messageHeaders,
      CompilerService compilerService, SimpMessagingTemplate messagingTemplate) {
    CodeRunner runner = sessionToCodeRunner.computeIfAbsent(session, (k) -> codeRunnerFactory
        .createCodeRunner(session, messageHeaders, compilerService, messagingTemplate));
    runner.setSources(sources);
    Thread runnerThread = new Thread(runner);
    sessionToThread.put(session, runnerThread);
  }

  public void removeSession(String session) {
    if (sessionToCodeRunner.containsKey(session)) {
      sessionToCodeRunner.remove(session);
      if (sessionToThread.containsKey(session)) {
        Thread sessionThread = sessionToThread.remove(session);
        threadQueue.remove(sessionThread);
      }
    }
  }
}
