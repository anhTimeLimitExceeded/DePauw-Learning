package edu.depauw.itap.compiler;

import java.util.List;

public class CompilerResponse {
  private List<CompilerResult> results;

  public List<CompilerResult> getResults() {
    return results;
  }

  public CompilerResponse setResults(List<CompilerResult> results) {
    this.results = results;
    return this;
  }
}
