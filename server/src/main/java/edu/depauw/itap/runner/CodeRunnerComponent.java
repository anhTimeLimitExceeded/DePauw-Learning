package edu.depauw.itap.runner;

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
  private CodeRunnerService codeRunnerService;

  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  @MessageMapping("/run")
  public void runCode(@Payload CompilerSources sources, SimpMessageHeaderAccessor headerAccessor) {
    codeRunnerService.createThread(headerAccessor.getSessionId(), sources.getSources(),
        headerAccessor.getMessageHeaders(), compilerService, messagingTemplate);
    System.out.println("Received request: " + headerAccessor.getSessionId());
  }

  @MessageMapping("/runner/input")
  public void runCode(@Payload CodeRunnerInput input, SimpMessageHeaderAccessor headerAccessor) {
    if (codeRunnerService.getRunner(headerAccessor.getSessionId()) != null) {
      codeRunnerService.addInput(headerAccessor.getSessionId(), input.getInput());
    }
  }
}
