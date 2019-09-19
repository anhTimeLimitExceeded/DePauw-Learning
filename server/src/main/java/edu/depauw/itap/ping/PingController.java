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
    return ping;
  }
}
