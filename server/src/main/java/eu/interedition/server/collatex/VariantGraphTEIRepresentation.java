package eu.interedition.server.collatex;

import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.simple.SimpleVariantGraphSerializer;
import eu.interedition.text.xml.XML;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.channels.WritableByteChannel;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class VariantGraphTEIRepresentation extends WriterRepresentation implements VariantGraphRepresentation {
  static final MediaType APPLICATION_TEI_XML = new MediaType("application/tei+xml");

  private static final XMLOutputFactory XML_OUTPUT_FACTORY = XML.createXMLOutputFactory();

  private VariantGraph graph;

  public VariantGraphTEIRepresentation() {
    super(APPLICATION_TEI_XML);
  }

  @Override
  public Representation forGraph(VariantGraph graph) {
    this.graph = graph;
    return this;
  }

  @Transactional
  @Override
  public void write(WritableByteChannel writableChannel) throws IOException {
    super.write(writableChannel);
  }

  @Transactional
  @Override
  public void write(OutputStream outputStream) throws IOException {
    super.write(outputStream);
  }

  @Transactional
  @Override
  public void write(Writer writer) throws IOException {
    XMLStreamWriter xml = null;
    try {
      new SimpleVariantGraphSerializer(graph).toTEI(xml = XML_OUTPUT_FACTORY.createXMLStreamWriter(writer));
    } catch (XMLStreamException e) {
      throw new IOException(e.getMessage(), e);
    } finally {
      XML.closeQuietly(xml);
    }
  }
}
