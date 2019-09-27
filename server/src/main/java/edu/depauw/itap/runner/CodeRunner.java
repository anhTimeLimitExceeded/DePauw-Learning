package edu.depauw.itap.runner;

import java.util.List;

public interface CodeRunner extends Runnable {
  public void setSources(List<String> sources);

  public RunnerStatus getStatus();
}
