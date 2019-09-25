package edu.depauw.itap.runner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.StepRequest;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import edu.depauw.itap.compiler.CompilerResult;
import edu.depauw.itap.compiler.CompilerService;

enum RunnerStatus {
  LOADED, RUNNING, STOPPED;
}


public class CodeRunner implements Runnable {

  public static final Duration MAX_EXECUTION_TIME = Duration.ofSeconds(15);

  private final CompilerService compilerService;

  private final SimpMessagingTemplate messagingTemplate;

  private final String session;
  private final MessageHeaders messageHeaders;
  private final Clock clock;

  private Map<String, String> classNameTosource;
  private RunnerStatus status;

  public CodeRunner(String session, MessageHeaders messageHeaders, CompilerService compilerService,
      SimpMessagingTemplate messagingTemplate, Clock clock) {
    this.session = session;
    this.messageHeaders = messageHeaders;
    this.status = RunnerStatus.STOPPED;
    this.compilerService = compilerService;
    this.messagingTemplate = messagingTemplate;
    this.clock = clock;
  }

  public void setSources(List<String> sources) {
    this.classNameTosource = sources.stream().collect(Collectors
        .toMap(source -> CompilerService.getFullyQualifiedClassName(source), Function.identity()));
    this.status = RunnerStatus.LOADED;
  }

  public RunnerStatus getStatus() {
    return this.status;
  }

  @Override
  public void run() {
    this.status = RunnerStatus.RUNNING;

    Path classRoot = CompilerService.getDirectoryPath(this.session);

    String sourceClass = findMainClass(this.classNameTosource).orElseThrow();

    List<CompilerResult> compilingResults =
        compilerService.compile(this.session, this.classNameTosource.entrySet().stream()
            .map(Map.Entry::getValue).collect(Collectors.toList()));

    if (!compilingResults.isEmpty()) {
      return;
    }

    System.out.println(sourceClass);
    System.out.println(classRoot.toString());

    /*
     * Prepare connector, set class to debug & launch VM.
     */
    LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager().defaultConnector();
    Map<String, Connector.Argument> env = launchingConnector.defaultArguments();
    env.get("main").setValue(sourceClass);
    env.get("options").setValue("-cp \"" + classRoot.toString() + "\"");
    VirtualMachine vm;
    try {
      vm = launchingConnector.launch(env);
    } catch (IOException | IllegalConnectorArgumentsException | VMStartException e) {
      System.out.println(e.getMessage());
      return;
    }

    /*
     * Request VM to trigger event when HelloWorld class is prepared.
     */
    ClassPrepareRequest classPrepareRequest = vm.eventRequestManager().createClassPrepareRequest();
    classPrepareRequest.addClassFilter(sourceClass);
    classPrepareRequest.enable();

    EventSet eventSet = null;

    // Get streams to retrieve the output of the VM
    InputStreamReader reader = new InputStreamReader(vm.process().getInputStream());
    ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();

    // Set up the variable to keep track of the time that the VM has run for
    Duration executionTime = Duration.ofMillis(0);

    try {
      Instant startTime = Instant.now(this.clock);
      while (true) {
        eventSet = vm.eventQueue().remove(500);
        Instant currentTime = Instant.now(this.clock);
        if (eventSet != null) {
          for (Event event : eventSet) {

            /*
             * If this is ClassPrepareEvent, then set breakpoint
             */
            if (event instanceof ClassPrepareEvent) {
              StepRequest entryRequest =
                  vm.eventRequestManager().createStepRequest(((ClassPrepareEvent) event).thread(),
                      StepRequest.STEP_MIN, StepRequest.STEP_INTO);
              for (String className : this.classNameTosource.keySet()) {
                entryRequest.addClassFilter(className);
              }
              entryRequest.enable();
              event.request().disable();

            }

            if (event instanceof StepEvent) {

              // Code to print out all the stack frame information

              // StackFrame stackFrame = ((StepEvent) event).thread().frame(0);
              // System.out.println(stackFrame.location());
              // System.out.println(stackFrame.location().method());
              // System.out.println(stackFrame.location().declaringType().fields());
              // System.out.println(stackFrame.thisObject());
              // System.out.println(stackFrame.getArgumentValues());
              // try {
              // Map<LocalVariable, Value> visibleVariables =
              // stackFrame.getValues(stackFrame.visibleVariables());
              // System.out.println("Local Variables =");
              // for (Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()) {
              // System.out.println(" " + entry.getKey().name() + " = " + entry.getValue());
              // }
              // } catch (AbsentInformationException e) {
              // System.out.println(e);
              // }

            }
          }

          vm.resume();
        }

        if (reader.ready()) {
          while (reader.ready()) {
            outputBuffer.write((char) reader.read());
          }
          CodeRunnerStatus status = new CodeRunnerStatus().setOutput(outputBuffer.toString())
              .setStatus(RunnerStatus.RUNNING);
          this.messagingTemplate.convertAndSendToUser(this.session, "/topic/runner/status", status,
              messageHeaders);
        }


        executionTime =
            executionTime.plusMillis(Duration.between(startTime, currentTime).toMillis());
        startTime = currentTime;
        if (executionTime.compareTo(MAX_EXECUTION_TIME) > 0) {
          vm.exit(1);
        }
      }
    } catch (VMDisconnectedException e) {
      System.out.println(this.session + ": VM is now disconnected.");
      try {
        CodeRunnerStatus status = new CodeRunnerStatus();
        if (reader.ready()) {
          while (reader.ready()) {
            outputBuffer.write((char) reader.read());
          }
          status.setOutput(outputBuffer.toString());
        }
        this.messagingTemplate.convertAndSendToUser(this.session, "/topic/runner/status",
            status.setStatus(RunnerStatus.STOPPED), messageHeaders);
      } catch (IOException e0) {
        e0.printStackTrace();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    CompilerService.deleteDirectory(classRoot.toFile());

    this.status = RunnerStatus.STOPPED;
  }

  private static Optional<String> findMainClass(Map<String, String> classNameToSource) {
    Pattern r = Pattern.compile("\\s*(?:public\\s+)?static\\s+void\\s+main\\s*\\(String");
    return classNameToSource.entrySet().stream().filter((entry) -> {
      Matcher m = r.matcher(entry.getValue());
      return m.find();
    }).findFirst().map(Map.Entry::getKey);
  }
}
