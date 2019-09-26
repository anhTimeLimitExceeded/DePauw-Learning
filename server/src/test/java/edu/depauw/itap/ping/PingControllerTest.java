package edu.depauw.itap.ping;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(PingController.class)
public class PingControllerTest {

  @Autowired
  private MockMvc mvc;

  @Test
  public void testPingEndpoint() throws Exception {
    mvc.perform(get("/ping?ping=Hello")).andExpect(content().string(equalTo("HeLlO")));
  }

  @Test
  public void testPingEndpointLongerString() throws Exception {
    mvc.perform(get("/ping?ping=HelloWorld")).andExpect(content().string(equalTo("HeLlOwOrLd")));
  }
}
