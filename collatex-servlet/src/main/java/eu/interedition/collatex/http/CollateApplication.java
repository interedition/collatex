package eu.interedition.collatex.http;

import eu.interedition.collatex.io.*;

import javax.ws.rs.core.Application;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ronald on 5/3/15.
 */
public class CollateApplication extends Application {
    private static final Logger LOG = Logger.getLogger(CollateApplication.class.getName());

    private static String detectDotPath() {
        for (String detectionCommand : new String[] { "which dot", "where dot.exe" }) {
            try {
                final Process process = Runtime.getRuntime().exec(detectionCommand);
                try (BufferedReader processReader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
                    final CompletableFuture<Optional<String>> path = CompletableFuture.supplyAsync(() -> processReader.lines()
                        .map(String::trim)
                        .filter(l -> l.toLowerCase().contains("dot"))
                        .findFirst());
                    process.waitFor();
                    final String dotPath = path.get().get();
                    LOG.info(() -> "Detected GraphViz' dot at '" + dotPath + "'");
                    return dotPath;
                }
            } catch (Throwable t) {
                LOG.log(Level.FINE, detectionCommand, t);
            }
        }
        return null;
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> s = new HashSet<>();
        s.add(SimpleCollationJSONMessageBodyReader.class);
        s.add(VariantGraphJSONMessageBodyWriter.class);
        s.add(VariantGraphTEIMessageBodyWriter.class);
        s.add(VariantGraphGraphMLMessageBodyWriter.class);
        s.add(VariantGraphDotMessageBodyWriter.class);
        s.add(IOExceptionMapper.class);
        return s;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = new HashSet<>();
        singletons.add(new CollateResource("", 10, 0));
        singletons.add(new VariantGraphSVGMessageBodyWriter(detectDotPath()));
        return singletons;
    }
}


