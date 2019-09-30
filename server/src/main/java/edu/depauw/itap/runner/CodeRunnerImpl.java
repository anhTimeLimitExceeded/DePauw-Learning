package edu.depauw.itap.runner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.StepRequest;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import edu.depauw.itap.compiler.CompilerResponse;
import edu.depauw.itap.compiler.CompilerResult;
import edu.depauw.itap.compiler.CompilerService;

public class CodeRunnerImpl implements CodeRunner {

  public static final Duration MAX_EXECUTION_TIME = Duration.ofSeconds(30);

  private final CompilerService compilerService;

  private final SimpMessagingTemplate messagingTemplate;

  private final String session;
  private final MessageHeaders messageHeaders;
  private final Clock clock;

  private final ByteArrayOutputStream inputBuffer;

  private Map<String, String> classNameToSource;
  private RunnerStatus status;

  public CodeRunnerImpl(String session, MessageHeaders messageHeaders,
      CompilerService compilerService, SimpMessagingTemplate messagingTemplate, Clock clock) {
    this.session = session;
    this.messageHeaders = messageHeaders;
    this.status = RunnerStatus.STOPPED;
    this.compilerService = compilerService;
    this.messagingTemplate = messagingTemplate;
    this.clock = clock;

    this.inputBuffer = new ByteArrayOutputStream();
  }

  public void setSources(List<String> sources) {
    this.classNameToSource = sources.stream().collect(Collectors
        .toMap(source -> CompilerService.getFullyQualifiedClassName(source), Function.identity()));
    this.status = RunnerStatus.LOADED;
  }

  @Override
  public void addInput(String input) {
    synchronized (this.inputBuffer) {
      for (char c : input.toCharArray()) {
        this.inputBuffer.write(c);
      }
    }
  }

  public RunnerStatus getStatus() {
    return this.status;
  }

  @Override
  public void run() {
    this.status = RunnerStatus.STARTING_UP;
    this.messagingTemplate.convertAndSendToUser(this.session, "/topic/runner/status",
        new CodeRunnerStatus().setStatus(this.status), messageHeaders);

    Path classRoot = CompilerService.getDirectoryPath(this.session);

    String sourceClass = findMainClass(this.classNameToSource).orElseThrow();

    try {
      List<CompilerResult> compilingResults =
          compilerService.compile(this.session, this.classNameToSource.entrySet().stream()
              .map(Map.Entry::getValue).collect(Collectors.toList()));

      if (!compilingResults.isEmpty()) {
        CompilerResponse response = new CompilerResponse().setResults(compilingResults);
        this.messagingTemplate.convertAndSendToUser(this.session, "/topic/compile", response,
            messageHeaders);
        throw new RuntimeException(compilingResults.stream().map((result) -> result.getMessage())
            .collect(Collectors.toList()).toString());
      }

      /*
       * Prepare connector, set class to debug & launch VM.
       */
      LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager().defaultConnector();
      Map<String, Connector.Argument> env = launchingConnector.defaultArguments();
      env.get("main").setValue(sourceClass);
      env.get("options").setValue("-Djava.security.manager -cp \"" + classRoot.toString() + "\"");
      VirtualMachine vm;
      try {
        vm = launchingConnector.launch(env);
      } catch (IOException | IllegalConnectorArgumentsException | VMStartException e) {
        System.out.println(e.getMessage());
        throw new RuntimeException(e);
      }

      /*
       * Request VM to trigger event when HelloWorld class is prepared.
       */
      ClassPrepareRequest classPrepareRequest =
          vm.eventRequestManager().createClassPrepareRequest();
      classPrepareRequest.addClassFilter(sourceClass);
      classPrepareRequest.enable();

      EventSet eventSet = null;

      // Get streams to retrieve the output of the VM
      InputStreamReader reader = new InputStreamReader(vm.process().getInputStream());
      InputStreamReader errorReader = new InputStreamReader(vm.process().getErrorStream());
      OutputStreamWriter inputStream = new OutputStreamWriter(vm.process().getOutputStream());
      ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();

      try {
        // Set up the variable to keep track of the time that the VM has run for
        Duration executionTime = Duration.ofMillis(0);

        Instant startTime = null;
        while (true) {
          eventSet = vm.eventQueue().remove(150);
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
                for (String className : this.classNameToSource.keySet()) {
                  entryRequest.addClassFilter(className);
                }
                entryRequest.enable();
                event.request().disable();
                startTime = Instant.now(this.clock);
                this.status = RunnerStatus.RUNNING;
                this.messagingTemplate.convertAndSendToUser(this.session, "/topic/runner/status",
                    new CodeRunnerStatus().setStatus(this.status), messageHeaders);
              }

              // if (event instanceof StepEvent) {

              // // Code to print out all the stack frame information

              // StackFrame stackFrame = ((StepEvent) event).thread().frame(0);
              // System.out.println(stackFrame.location());
              // System.out.println(stackFrame.location().method());
              // System.out.println(stackFrame.location().declaringType().fields());
              // System.out.println(stackFrame.thisObject());
              // System.out.println(stackFrame.getArgumentValues());
              // Map<LocalVariable, Value> visibleVariables =
              // stackFrame.getValues(stackFrame.visibleVariables());
              // System.out.println("Local Variables =");
              // for (Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()) {
              // System.out.println(" " + entry.getKey().name() + " = " + entry.getValue());
              // }

              // }
            }

            vm.resume();
          }

          CodeRunnerStatus status = null;

          if (this.inputBuffer.size() > 0) {
            synchronized (this.inputBuffer) {
              for (char c : this.inputBuffer.toString().toCharArray()) {
                inputStream.write(c);
              }
              this.inputBuffer.reset();
              inputStream.flush();
            }
          }

          if (reader.ready()) {
            while (reader.ready()) {
              outputBuffer.write((char) reader.read());
            }
            status = new CodeRunnerStatus().setOutput(outputBuffer.toString());
            outputBuffer.reset();
          }

          if (errorReader.ready()) {
            while (errorReader.ready()) {
              outputBuffer.write((char) errorReader.read());
            }
            if (status == null) {
              status = new CodeRunnerStatus();
            }
            status.setErrorOutput(outputBuffer.toString());
            outputBuffer.reset();
          }

          if (status != null) {
            status.setStatus(this.status);
            this.messagingTemplate.convertAndSendToUser(this.session, "/topic/runner/status",
                status, messageHeaders);
          }

          if (startTime != null) {
            executionTime =
                executionTime.plusMillis(Duration.between(startTime, currentTime).toMillis());
            startTime = currentTime;
            if (executionTime.compareTo(MAX_EXECUTION_TIME) > 0) {
              vm.exit(1);
              this.status = RunnerStatus.TIMED_OUT;
            }
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
          if (this.status.equals(RunnerStatus.RUNNING)) {
            this.status = RunnerStatus.STOPPED;
          }
          this.messagingTemplate.convertAndSendToUser(this.session, "/topic/runner/status",
              status.setStatus(this.status), messageHeaders);
        } catch (IOException e0) {
          e0.printStackTrace();
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        try {
          inputStream.close();
          reader.close();
          errorReader.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } finally {
      CompilerService.deleteDirectory(classRoot.toFile());

      this.inputBuffer.reset();
      this.status = RunnerStatus.STOPPED;
    }
  }

  private static Optional<String> findMainClass(Map<String, String> classNameToSource) {
    Pattern r = Pattern.compile("\\s*(?:public\\s+)?static\\s+void\\s+main\\s*\\(String");
    return classNameToSource.entrySet().stream().filter((entry) -> {
      Matcher m = r.matcher(entry.getValue());
      return m.find();
    }).findFirst().map(Map.Entry::getKey);
  }
}
