package edu.depauw.itap.ping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/ping")
public class PingController {
  @GetMapping(path = "/{ping}")
  public String addEmployee(@PathVariable("ping") String ping) {
    return ping;
  }
}
