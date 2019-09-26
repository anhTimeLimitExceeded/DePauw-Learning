package edu.depauw.itap.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Providers {
  @Bean
  public static Clock defaultClock() {
    return Clock.systemDefaultZone();
  }
}
