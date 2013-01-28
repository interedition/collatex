package eu.interedition.collatex;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.junit.Test;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ScriptEngineTest {

  @Test
  public void functions() throws ScriptException, NoSuchMethodException {
    final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    for (ScriptEngineFactory scriptEngineFactory : scriptEngineManager.getEngineFactories()) {
      System.out.println(Joiner.on("; ").join(
              scriptEngineFactory.getEngineName(),
              scriptEngineFactory.getEngineVersion(),
              scriptEngineFactory.getLanguageName(),
              scriptEngineFactory.getLanguageVersion(),
              Iterables.toString(scriptEngineFactory.getExtensions())
      ));
    }

    final Compilable compiler = (Compilable) Preconditions.checkNotNull(scriptEngineManager.getEngineByExtension("js"));
    final CompiledScript script = compiler.compile("function compare(a, b) { return a == b }\nfunction cost(a) { return 1; }");

    script.eval();

    System.out.println(((Invocable) script.getEngine()).invokeFunction("compare", "1", "0"));
  }
}
