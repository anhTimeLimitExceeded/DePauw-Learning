package edu.depauw.itap.runner;

import edu.depauw.itap.compiler.CompilerService;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Configuration
public class CodeRunnerFactory {

  @Autowired
  private Clock clock;

  public CodeRunner createCodeRunner(String session, MessageHeaders messageHeaders,
      CompilerService compilerService, SimpMessagingTemplate messagingTemplate) {
    return new CodeRunner(session, messageHeaders, compilerService, messagingTemplate, clock);
  }
}
