package edu.depauw.itap.runner;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import edu.depauw.itap.compiler.CompilerService;

@Service
public class CodeRunnerFactory {

  @Autowired
  private Clock clock;

  public CodeRunner createCodeRunner(String session, MessageHeaders messageHeaders,
      CompilerService compilerService, SimpMessagingTemplate messagingTemplate) {
    return new CodeRunner(session, messageHeaders, compilerService, messagingTemplate, clock);
  }
}
