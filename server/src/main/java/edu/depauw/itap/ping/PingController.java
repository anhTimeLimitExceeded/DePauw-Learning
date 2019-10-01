package edu.depauw.itap.ping;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/ping")
public class PingController {

  @GetMapping(path = {"", "/"})
  public String getPing(@RequestParam(value = "ping", defaultValue = "pong") String ping) {
    return modifyString(ping);
  }

  /**
   * Returns a modified string from the query.
   * 
   * @param ping the string to modifiy
   * @return the modified string
   */
  @MessageMapping("/ping")
  @SendToUser("/topic/ping")
  public String sendPing(@Payload String ping, SimpMessageHeaderAccessor headerAccessor) {
    System.out.print("Session: ");
    System.out.print(headerAccessor.getSessionId());
    System.out.print(" Message: ");
    System.out.println(ping);
    return modifyString(ping);
  }

  private String modifyString(String s) {
    StringBuilder builder = new StringBuilder();
    char[] charArray = s.toCharArray();
    for (int i = 0; i < charArray.length; i++) {
      if (i % 2 == 0) {
        builder.append(Character.toUpperCase(charArray[i]));
      } else {
        builder.append(Character.toLowerCase(charArray[i]));
      }
    }
    return builder.toString();
  }
}
