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
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Created by ronald on 5/6/15.
 */
@Provider
@Produces("application/graphml+xml")
public class VariantGraphGraphMLMessageBodyWriter implements MessageBodyWriter<VariantGraph> {
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
        XMLStreamWriter xml = null;
        try {
            httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/graphml+xml");
            try (OutputStream responseStream = entityStream) {
                xml = XMLOutputFactory.newInstance().createXMLStreamWriter(responseStream);
                xml.writeStartDocument();
                new SimpleVariantGraphSerializer(graph).toGraphML(xml);
                xml.writeEndDocument();
            } finally {
                if (xml != null) {
                    xml.close();
                }
            }
        } catch (XMLStreamException e) {
            throw new WebApplicationException(e);
        }

    }
}
