package edu.depauw.itap.compiler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CompilerControllerTest {
  @InjectMocks
  CompilerController compilerController;

  @Mock
  CompilerService compilerService;

  @Test
  public void testControllerCompiles() {

  }

}
