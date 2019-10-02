package edu.depauw.itap.runner;

import edu.depauw.itap.compiler.CompilerSources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class CodeRunnerComponent {

  @Autowired
  private CodeRunnerService codeRunnerService;

  /**
   * Runs the given sources in the server.
   * 
   * @param sources        the sources to run
   * @param headerAccessor the headers of the message
   */
  @MessageMapping("/run")
  public void runCode(@Payload CompilerSources sources, SimpMessageHeaderAccessor headerAccessor) {
    codeRunnerService.createThread(headerAccessor.getSessionId(), sources.getSources(),
        headerAccessor.getMessageHeaders());
    System.out.println("Received request: " + headerAccessor.getSessionId());
  }

  @MessageMapping("/runner/input")
  public void runCode(@Payload CodeRunnerInput input, SimpMessageHeaderAccessor headerAccessor) {
    if (codeRunnerService.getRunner(headerAccessor.getSessionId()) != null) {
      codeRunnerService.addInput(headerAccessor.getSessionId(), input.getInput());
    }
  }
}
