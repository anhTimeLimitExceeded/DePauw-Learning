package edu.depauw.itap.runner;

import java.util.List;

enum RunnerStatus {
  LOADED, RUNNING, STOPPED;
}


public interface CodeRunner extends Runnable {
  public void setSources(List<String> sources);

  public void addInput(String input);

  public RunnerStatus getStatus();
}
