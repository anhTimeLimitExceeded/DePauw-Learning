package edu.depauw.itap.compiler;

public class CompilerResult {
  private long startLineNumber;

  private long startColumnNumber;

  private long endLineNumber;

  private long endColumnNumber;

  private String severity;

  private String message;

  private String className;

  public long getStartLineNumber() {
    return startLineNumber;
  }

  public CompilerResult setStartLineNumber(long startLineNumber) {
    this.startLineNumber = startLineNumber;
    return this;
  }

  public long getStartColumnNumber() {
    return startColumnNumber;
  }

  public CompilerResult setStartColumnNumber(long startColumnNumber) {
    this.startColumnNumber = startColumnNumber;
    return this;
  }

  public long getEndLineNumber() {
    return endLineNumber;
  }

  public CompilerResult setEndLineNumber(long endLineNumber) {
    this.endLineNumber = endLineNumber;
    return this;
  }

  public long getEndColumnNumber() {
    return endColumnNumber;
  }

  public CompilerResult setEndColumnNumber(long endColumnNumber) {
    this.endColumnNumber = endColumnNumber;
    return this;
  }

  public String getSeverity() {
    return severity;
  }

  public CompilerResult setSeverity(String severity) {
    this.severity = severity;
    return this;
  }

  public String getMessage() {
    return message;
  }

  public CompilerResult setMessage(String message) {
    this.message = message;
    return this;
  }

  public String getClassName() {
    return className;
  }

  public CompilerResult setClassName(String className) {
    this.className = className;
    return this;
  }
}
