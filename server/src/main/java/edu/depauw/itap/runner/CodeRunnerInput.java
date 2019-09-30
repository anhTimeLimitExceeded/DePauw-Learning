package edu.depauw.itap.runner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class CodeRunnerInput {
  private String input;

  public String getInput() {
    return input;
  }

  public CodeRunnerInput setInput(String input) {
    this.input = input;
    return this;
  }
}
