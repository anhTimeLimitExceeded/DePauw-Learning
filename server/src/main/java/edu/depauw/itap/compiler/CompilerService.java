package edu.depauw.itap.compiler;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompilerService {
  @Autowired
  private CompilerRunnable compilerRunnable;

  public List<CompilerResult> compile(String session, List<String> sources) {
    return compilerRunnable.compileWithoutSaving(session, sources);
  }
}
