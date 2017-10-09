/*
 * Copyright (c) 2015 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.tools;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.simple.SimpleToken;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public class PluginScript {

    static final Charset SCRIPT_CHARSET = Charset.forName("UTF-8");

    static final String TOKENIZER_FUNCTION = "tokenize";
    static final String NORMALIZER_FUNCTION = "normalize";
    static final String COMPARATOR_FUNCTION = "compare";

    final Invocable script;
    final boolean tokenizer;
    final boolean normalizer;
    final boolean comparator;

    public static PluginScript read(URL source) throws ScriptException, IOException {
        try (Reader sourceReader = new BufferedReader(new InputStreamReader(source.openStream(), SCRIPT_CHARSET))) {
            return read(source.toString(), sourceReader);
        }
    }

    public static PluginScript read(String filename, Reader source) throws ScriptException, IOException {
        final ScriptEngine scriptEngine = Objects.requireNonNull(new ScriptEngineManager().getEngineByExtension("js"));
        scriptEngine.put(ScriptEngine.FILENAME, filename);

        final CompiledScript script = ((Compilable) scriptEngine).compile(source);
        script.eval();

        return new PluginScript((Invocable) script.getEngine());
    }

    PluginScript(Invocable script) throws ScriptException {
        this.script = script;
        tokenizer = hasFunction(TOKENIZER_FUNCTION, "");
        normalizer = hasFunction(NORMALIZER_FUNCTION, "");
        comparator = hasFunction(COMPARATOR_FUNCTION, "", "");
    }

    Function<String, Stream<String>> tokenizer() {
        return (tokenizer ? input -> {
            final Object result = invoke(TOKENIZER_FUNCTION, input);
            if (!(result instanceof Iterable)) {
                throw new PluginScriptExecutionException("Wrong result type of " +
                    TOKENIZER_FUNCTION + "(); expected an iterable type, found " +
                    result.getClass());
            }
            final List<String> tokens = new LinkedList<>();
            for (Object token : (Iterable<?>) result) {
                if (token == null) {
                    throw new PluginScriptExecutionException(TOKENIZER_FUNCTION + "() returned null token");
                }
                if (!(token instanceof String)) {
                    throw new PluginScriptExecutionException("Wrong result type of " +
                        TOKENIZER_FUNCTION + "(); expected tokens of type string, found " +
                        token.getClass());
                }
                tokens.add((String) token);
            }

            return tokens.stream();
        } : null);
    }

    Function<String, String> normalizer() {
        return (normalizer ? input -> {
            final Object result = invoke(NORMALIZER_FUNCTION, input);
            if (!(result instanceof String)) {
                throw new PluginScriptExecutionException("Wrong result type of " +
                    NORMALIZER_FUNCTION + "(); expected a string, found " +
                    result.getClass());
            }
            return (String) result;
        } : null);
    }

    Comparator<Token> comparator() {
        return (comparator ? (o1, o2) -> {
            if (!(o1 instanceof SimpleToken)) {
                throw new PluginScriptExecutionException(COMPARATOR_FUNCTION + "() called with wrong token type '" + o1.getClass());
            }
            if (!(o2 instanceof SimpleToken)) {
                throw new PluginScriptExecutionException(COMPARATOR_FUNCTION + "() called with wrong token type '" + o2.getClass());
            }

            final Object result = invoke(COMPARATOR_FUNCTION, ((SimpleToken) o1).getNormalized(), ((SimpleToken) o2).getNormalized());
            if (result instanceof Number) {
                return ((Number) result).intValue();
            }
            if (result instanceof Boolean) {
                return (((Boolean) result) ? 0 : 1);
            }

            throw new PluginScriptExecutionException("Wrong result type of " +
                COMPARATOR_FUNCTION + "(); expected a number or boolean type, found " +
                result.getClass());
        } : null);
    }

    boolean hasFunction(String name, Object... testArgs) throws ScriptException {
        try {
            script.invokeFunction(name, testArgs);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    Object invoke(String function, Object... args) throws PluginScriptExecutionException {
        try {
            Object result = script.invokeFunction(function, args);
            if (result == null) {
                throw new PluginScriptExecutionException(function + "() returned null");
            }
            return result;
        } catch (ScriptException e) {
            throw new PluginScriptExecutionException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    static class PluginScriptExecutionException extends RuntimeException {

        PluginScriptExecutionException(String message) {
            super(message);
        }

        PluginScriptExecutionException(ScriptException scriptException) {
            super(scriptException);
        }
    }
}
