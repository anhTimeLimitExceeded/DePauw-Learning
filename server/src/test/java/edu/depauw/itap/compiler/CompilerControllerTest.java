package edu.depauw.itap.compiler;

import static org.assertj.core.api.Assertions.assertThat;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompilerControllerTest {
  @LocalServerPort
  int randomPort;

  private StompSession stompSession;

  private CompletableFuture<CompilerResponse> completableFuture;

  @Before
  public void setup() throws InterruptedException, ExecutionException, TimeoutException {
    String hostUrl = "ws://localhost:" + randomPort + "/socket";

    WebSocketStompClient stompClient =
        new WebSocketStompClient(new SockJsClient(createTransportClient()));
    stompClient.setMessageConverter(new MappingJackson2MessageConverter());

    stompSession = stompClient.connect(hostUrl, new StompSessionHandlerAdapter() {
    }).get(1, TimeUnit.SECONDS);

    completableFuture = new CompletableFuture<>();
  }

  @Test
  public void testCompileEndpoint() throws Exception {
    String source = "public class Test {\n" + "public static void main(String[] args) {\n"
        + "    System.out.println(\"Hello World!\");\n" + " }\n" + "}\n";

    stompSession.subscribe("/user/topic/compile", new CompileStompFrameHandler());
    stompSession.send("/app/compile",
        new CompilerSources().setSources(Collections.singletonList(source)));

    CompilerResponse compilerResults = completableFuture.get(5, TimeUnit.SECONDS);

    assertThat(compilerResults).isNotNull();
    assertThat(compilerResults.getResults()).isEmpty();
  }

  @Test
  public void testCompileEndpointBadCode() throws Exception {
    String source = "public class Test {\n" + "public static void main(String[] args) {\n"
        + "    System.out.printaln(\"Hello World!\");\n" + " }\n" + "}\n";

    stompSession.subscribe("/user/topic/compile", new CompileStompFrameHandler());
    stompSession.send("/app/compile",
        new CompilerSources().setSources(Collections.singletonList(source)));

    CompilerResponse compilerResults = completableFuture.get(5, TimeUnit.SECONDS);

    assertThat(compilerResults).isNotNull();
    assertThat(compilerResults.getResults()).isNotEmpty();
    assertThat(compilerResults.getResults().get(0).getClassName()).isEqualTo("Test");
    assertThat(compilerResults.getResults().get(0).getMessage()).isNotEmpty();
    assertThat(compilerResults.getResults().get(0).getSeverity()).isEqualTo("ERROR");
    assertThat(compilerResults.getResults().get(0).getStartLineNumber()).isGreaterThan(0);
    assertThat(compilerResults.getResults().get(0).getStartColumnNumber()).isGreaterThan(0);
    assertThat(compilerResults.getResults().get(0).getEndLineNumber()).isGreaterThan(0);
    assertThat(compilerResults.getResults().get(0).getEndColumnNumber()).isGreaterThan(0);
  }

  private class CompileStompFrameHandler implements StompFrameHandler {
    @Override
    public Type getPayloadType(StompHeaders stompHeaders) {
      return CompilerResponse.class;
    }

    @Override
    public void handleFrame(StompHeaders stompHeaders, Object o) {
      completableFuture.complete((CompilerResponse) o);
    }
  }

  private List<Transport> createTransportClient() {
    List<Transport> transports = new ArrayList<>(1);
    transports.add(new WebSocketTransport(new StandardWebSocketClient()));
    return transports;
  }
}
