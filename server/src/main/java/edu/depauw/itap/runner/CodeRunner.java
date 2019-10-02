package edu.depauw.itap.runner;

import java.util.List;

public interface CodeRunner extends Runnable {
  public void setSources(List<String> sources);

  public void addInput(String input);

  public RunnerStatus getStatus();
}
