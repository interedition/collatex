package eu.interedition.collatex.io;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.simple.SimpleVariantGraphSerializer;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Created by ronald on 5/6/15.
 */
@Provider
@Produces("text/plain")
public class VariantGraphDotMessageBodyWriter implements MessageBodyWriter<VariantGraph> {
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
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "text/plain");
        httpHeaders.add(HttpHeaders.CONTENT_ENCODING, "utf-8");
        try (final Writer out = new OutputStreamWriter(entityStream)) {
            new SimpleVariantGraphSerializer(graph).toDot(out);
        }
    }
}
