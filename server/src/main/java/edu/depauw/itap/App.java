package edu.depauw.itap;

import java.nio.file.Paths;
import javax.annotation.PreDestroy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import edu.depauw.itap.compiler.CompilerService;

@SpringBootApplication
public class App {
  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }

  @PreDestroy
  public void onExit() {
    CompilerService.deleteDirectory(Paths.get(".").resolve("temp").normalize().toFile());
  }
}
