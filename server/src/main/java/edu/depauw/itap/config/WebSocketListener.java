package edu.depauw.itap.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import edu.depauw.itap.runner.CodeRunnerService;

@Component
public class WebSocketListener implements ApplicationListener<SessionDisconnectEvent> {

  @Autowired
  private CodeRunnerService codeRunnerService;

  @Override
  public void onApplicationEvent(SessionDisconnectEvent event) {
    codeRunnerService.removeSession(event.getSessionId());
  }
}
