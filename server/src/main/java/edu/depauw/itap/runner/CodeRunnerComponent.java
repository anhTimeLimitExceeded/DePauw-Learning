package edu.depauw.itap.runner;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import edu.depauw.itap.compiler.CompilerService;
import edu.depauw.itap.compiler.CompilerSources;

@Controller
public class CodeRunnerComponent {

  @Autowired
  private CompilerService compilerService;

  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  @MessageMapping("/run")
  public void compile(@Payload CompilerSources sources, SimpMessageHeaderAccessor headerAccessor) {
    CodeRunner codeRunner =
        new CodeRunner(headerAccessor.getSessionId(), headerAccessor.getMessageHeaders(),
            compilerService, messagingTemplate, Clock.systemDefaultZone());
    codeRunner.setSources(sources.getSources());
    new Thread(codeRunner).start();
    System.out.println("Received request: " + headerAccessor.getSessionId());
    return;
  }
}
