package edu.depauw.itap.runner;

import static org.assertj.core.api.Assertions.assertThat;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import edu.depauw.itap.compiler.CompilerSources;
import edu.depauw.itap.util.TestData;
import edu.depauw.itap.util.TestSocket;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CodeRunnerControllerSocketTest {
  @LocalServerPort
  int randomPort;

  private StompSession stompSession;

  private CompletableFuture<CodeRunnerStatus> completableFuture;

  @Before
  public void setup() throws InterruptedException, ExecutionException, TimeoutException {
    String hostUrl = "ws://localhost:" + randomPort + "/socket";

    WebSocketStompClient stompClient =
        new WebSocketStompClient(new SockJsClient(TestSocket.createTransportClient()));
    stompClient.setMessageConverter(new MappingJackson2MessageConverter());

    stompSession = stompClient.connect(hostUrl, new StompSessionHandlerAdapter() {
    }).get(1, TimeUnit.SECONDS);

    completableFuture = new CompletableFuture<>();
  }

  @Test
  public void testRunCode() throws Exception {
    stompSession.subscribe("/user/topic/runner/status", new StatusStompFrameHandler());

    stompSession.send("/app/run",
        new CompilerSources().setSources(Collections.singletonList(TestData.VALID_SOURCE)));

    CodeRunnerStatus status = null;
    List<CodeRunnerStatus> results = new ArrayList<>();

    try {
      while (true) {
        status = completableFuture.get(3, TimeUnit.SECONDS);
        completableFuture = new CompletableFuture<>();
        results.add(status);
      }
    } catch (TimeoutException e) {
      // Ignore
    }

    assertThat(results).isNotEmpty();
    assertThat(results.stream()).anyMatch((result) -> {
      return result.getOutput().equals("Hello World!\n");
    });

    assertThat(results.stream()).noneMatch((result) -> {
      return result.getErrorOutput() != null;
    });

    assertThat(results.stream().filter((result) -> result.getStatus().equals(RunnerStatus.STOPPED))
        .collect(Collectors.toList())).hasSize(1);
  }

  private class StatusStompFrameHandler implements StompFrameHandler {
    @Override
    public Type getPayloadType(StompHeaders stompHeaders) {
      return CodeRunnerStatus.class;
    }

    @Override
    public void handleFrame(StompHeaders stompHeaders, Object o) {
      completableFuture.complete((CodeRunnerStatus) o);
    }
  }
}
