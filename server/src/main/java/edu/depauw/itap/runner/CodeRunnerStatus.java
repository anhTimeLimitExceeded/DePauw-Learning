package edu.depauw.itap.runner;

public class CodeRunnerStatus {
    private String output;
    private String errorOutput;

    private RunnerStatus status;

    public String getOutput() {
        return output;
    }

    public CodeRunnerStatus setOutput(String output) {
        this.output = output;
        return this;
    }

    public String getErrorOutput() {
        return errorOutput;
    }

    public CodeRunnerStatus setErrorOutput(String errorOutput) {
        this.errorOutput = errorOutput;
        return this;
    }

    public RunnerStatus getStatus() {
        return status;
    }

    public CodeRunnerStatus setStatus(RunnerStatus status) {
        this.status = status;
        return this;
    }
}
