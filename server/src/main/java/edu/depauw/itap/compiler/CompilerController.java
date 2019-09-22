package edu.depauw.itap.compiler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class CompilerController {

  @Autowired
  private CompilerService compilerService;

  @MessageMapping("/compile")
  @SendToUser("/topic/compile")
  public List<CompilerResult> compile(@Payload CompilerSources sources,
      SimpMessageHeaderAccessor headerAccessor) {
    return compilerService.compile(headerAccessor.getSessionId(), sources.getSources());
  }
}
