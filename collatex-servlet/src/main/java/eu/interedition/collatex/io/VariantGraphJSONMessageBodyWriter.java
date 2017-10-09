package eu.interedition.collatex.io;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleWitness;
import eu.interedition.collatex.util.ParallelSegmentationApparatus;
import eu.interedition.collatex.util.VariantGraphRanking;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

/**
 * Created by ronald on 5/5/15.
 * Based on code written by Gregor Middell,
 */

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class VariantGraphJSONMessageBodyWriter implements MessageBodyWriter<VariantGraph> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return VariantGraph.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(VariantGraph variantGraph, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return 0;
    }

    @Override
    public void writeTo(VariantGraph graph, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        try (final JsonGenerator jgen = Json.createGenerator(entityStream)) {
            write(graph, jgen);
        }
    }

    private static void write(final VariantGraph graph, final JsonGenerator jgen) {
        ParallelSegmentationApparatus.generate(VariantGraphRanking.of(graph), new ParallelSegmentationApparatus.GeneratorCallback() {
            @Override
            public void start() {
                jgen.writeStartObject();

                jgen.writeStartArray("witnesses");
                graph.witnesses().stream().sorted(Witness.SIGIL_COMPARATOR).map(Witness::getSigil).forEach(jgen::write);
                jgen.writeEnd();

                jgen.writeStartArray("table");
            }

            @Override
            public void segment(SortedMap<Witness, Iterable<eu.interedition.collatex.Token>> contents) {
                jgen.writeStartArray();
                contents.values().forEach(tokens -> {
                    jgen.writeStartArray();
                    StreamSupport.stream(Spliterators.spliteratorUnknownSize(tokens.iterator(), Spliterator.NONNULL | Spliterator.IMMUTABLE), false)
                        .filter(t -> t instanceof SimpleToken)
                        .map(t -> (SimpleToken) t)
                        .sorted()
                        .forEach(t -> {
                            if (t instanceof Token) {
                                jgen.write(((Token) t).getJsonNode());
                            } else {
                                jgen.write(t.getContent());
                            }
                        });
                    jgen.writeEnd();
                });
                jgen.writeEnd();
            }

            @Override
            public void end() {
                jgen.writeEnd();
                jgen.writeEnd();
            }
        });
    }

    public static class Token extends SimpleToken {
        private final JsonObject jsonNode;

        public Token(SimpleWitness witness, String content, String normalized, JsonObject jsonNode) {
            super(witness, content, normalized);
            this.jsonNode = jsonNode;
        }

        public JsonObject getJsonNode() {
            return jsonNode;
        }
    }
}

