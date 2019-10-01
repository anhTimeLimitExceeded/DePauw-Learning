package edu.depauw.itap.runner;

import edu.depauw.itap.compiler.CompilerService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
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

  private final BlockingQueue<Thread> threadQueue;

  private final Thread queueRunner;

  private boolean running = true;

  private volatile boolean waitingForThreadToFinish = false;

  public CodeRunnerService() {
    this.sessionToCodeRunner = new HashMap<>();
    this.sessionToThread = new HashMap<>();
    this.threadQueue = new LinkedBlockingQueue<>();

    this.queueRunner = new Thread(new Runnable() {
      @Override
      public void run() {
        while (running) {
          try {
            Thread currentThread;
            synchronized (threadQueue) {
              currentThread = threadQueue.poll(500, TimeUnit.MILLISECONDS);
            }
            if (currentThread != null) {
              currentThread.start();
              waitingForThreadToFinish = true;
              currentThread.join();
              waitingForThreadToFinish = false;
            }
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    });
    this.queueRunner.start();
  }

  public boolean anyRunning() {
    synchronized (this.threadQueue) {
      return this.waitingForThreadToFinish || !this.threadQueue.isEmpty() || this.sessionToThread
          .entrySet().stream().map(Map.Entry::getValue).anyMatch((thread) -> thread.isAlive());
    }
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
    try {
      threadQueue.put(runnerThread);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
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

  @PreDestroy
  public void cleanUp() {
    this.running = false;
  }
}
