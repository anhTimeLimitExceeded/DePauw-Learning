package edu.depauw.itap.ping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/ping")
public class PingController {

  @GetMapping(path = { "", "/" })
  public String addEmployee(@RequestParam(value = "ping", defaultValue = "pong") String ping) {
    StringBuilder builder = new StringBuilder();
    char[] charArray = ping.toCharArray();
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
