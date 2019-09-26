package edu.depauw.itap.compiler;

import java.util.List;

public class CompilerSources {
  private List<String> sources;

  public List<String> getSources() {
    return sources;
  }

  public CompilerSources setSources(List<String> sources) {
    this.sources = sources;
    return this;
  }
}
