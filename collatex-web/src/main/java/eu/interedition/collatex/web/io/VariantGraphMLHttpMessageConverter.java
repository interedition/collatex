package eu.interedition.collatex.web.io;

import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import eu.interedition.collatex.implementation.graph.VariantGraph;
import eu.interedition.collatex.implementation.graph.VariantGraphEdge;
import eu.interedition.collatex.implementation.graph.VariantGraphTransposition;
import eu.interedition.collatex.implementation.graph.VariantGraphVertex;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphMLHttpMessageConverter extends AbstractHttpMessageConverter<VariantGraph> {
  /**
   * The GraphML MIME type.
   */
  protected static final MediaType APPLICATION_XML_GRAPHML = new MediaType("application", "graphml+xml");

  private final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();

  public VariantGraphMLHttpMessageConverter() {
    super(APPLICATION_XML_GRAPHML);
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return VariantGraph.class.isAssignableFrom(clazz);
  }

  @Override
  protected boolean canRead(MediaType mediaType) {
    return false;
  }

  @Override
  protected VariantGraph readInternal(Class<? extends VariantGraph> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    throw new HttpMessageNotReadableException(clazz.toString());
  }

  @Override
  protected void writeInternal(VariantGraph graph, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    final OutputStream body = outputMessage.getBody();
    XMLStreamWriter xml = null;
    try {
      xml = xmlOutputFactory.createXMLStreamWriter(body);
      
      xml.writeStartDocument();
      xml.writeStartElement("", GRAPHML_TAG, GRAPHML_NS);
      xml.writeNamespace("", GRAPHML_NS);
      xml.writeAttribute(XMLNSXSI_ATT, GRAPHML_XMLNSXSI);
      xml.writeAttribute(XSISL_ATT, GRAPHML_XSISL);

      for (Property p : Property.values()) {
        p.declare(xml);
      }

      xml.writeStartElement(GRAPHML_NS, GRAPH_TAG);
      xml.writeAttribute(ID_ATT, GRAPH_ID);
      xml.writeAttribute(EDGEDEFAULT_ATT, EDGEDEFAULT_DEFAULT_VALUE);
      xml.writeAttribute(PARSENODEIDS_ATT, PARSENODEIDS_DEFAULT_VALUE);
      xml.writeAttribute(PARSEEDGEIDS_ATT, PARSEEDGEIDS_DEFAULT_VALUE);
      xml.writeAttribute(PARSEORDER_ATT, PARSEORDER_DEFAULT_VALUE);

      final Map<VariantGraphVertex, String> vertexToId = Maps.newHashMap();
      int vertexNumber = 0;
      for (VariantGraphVertex vertex : graph.vertices()) {
        final String vertexNodeID = "n" + vertexNumber;
        xml.writeStartElement(GRAPHML_NS, NODE_TAG);
        xml.writeAttribute(ID_ATT, vertexNodeID);
        Property.NODE_NUMBER.write(Integer.toString(vertexNumber++), xml);
        Property.NODE_TOKEN.write(VariantGraphVertex.TO_CONTENTS.apply(vertex), xml);
        xml.writeEndElement();
        vertexToId.put(vertex, vertexNodeID);
      }

      int edgeNumber = 0;
      for (VariantGraphEdge edge : graph.edges()) {
        xml.writeStartElement(GRAPHML_NS, EDGE_TAG);
        xml.writeAttribute(ID_ATT, "e" + edgeNumber);
        xml.writeAttribute(SOURCE_ATT, vertexToId.get(edge.from()));
        xml.writeAttribute(TARGET_ATT, vertexToId.get(edge.to()));
        Property.EDGE_NUMBER.write(Integer.toString(edgeNumber++), xml);
        Property.EDGE_TYPE.write(EDGE_TYPE_PATH, xml);
        Property.EDGE_WITNESSES.write(VariantGraphEdge.TO_CONTENTS.apply(edge), xml);
        xml.writeEndElement();
      }
      for (VariantGraphTransposition transposition : graph.transpositions()) {
        xml.writeStartElement(GRAPHML_NS, EDGE_TAG);
        xml.writeAttribute(ID_ATT, "e" + edgeNumber);
        xml.writeAttribute(SOURCE_ATT, vertexToId.get(transposition.from()));
        xml.writeAttribute(TARGET_ATT, vertexToId.get(transposition.to()));
        Property.EDGE_NUMBER.write(Integer.toString(edgeNumber++), xml);
        Property.EDGE_TYPE.write(EDGE_TYPE_TRANSPOSITION, xml);
        xml.writeEndElement();

      }

      xml.writeEndElement();

      xml.writeEndElement();
      xml.writeEndDocument();
    } catch (XMLStreamException e) {
      throw new HttpMessageNotWritableException(e.getMessage(), e);
    } finally {
      if (xml != null) {
        try {
          xml.close();
        } catch (XMLStreamException e) {
        }
      }
      Closeables.closeQuietly(body);
    }
  }

  private static final String NODE_TAG = "node";
  private static final String WITNESS_ID_PREFIX = "w";
  private static final String TARGET_ATT = "target";
  private static final String SOURCE_ATT = "source";
  private static final String EDGE_TAG = "edge";
  private static final String EDGE_TYPE_PATH = "path";
  private static final String EDGE_TYPE_TRANSPOSITION = "transposition";
  private static final String EDGEDEFAULT_DEFAULT_VALUE = "directed";
  private static final String EDGEDEFAULT_ATT = "edgedefault";
  private static final String GRAPH_ID = "g0";
  private static final String GRAPH_TAG = "graph";
  private static final String GRAPHML_NS = "http://graphml.graphdrawing.org/xmlns";
  private static final String GRAPHML_TAG = "graphml";
  private static final String XMLNSXSI_ATT = "xmlns:xsi";
  private static final String XSISL_ATT = "xsi:schemaLocation";
  private static final String GRAPHML_XMLNSXSI = "http://www.w3.org/2001/XMLSchema-instance";
  private static final String GRAPHML_XSISL = "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd";
  private static final String PARSENODEIDS_ATT = "parse.nodeids";
  private static final String PARSENODEIDS_DEFAULT_VALUE = "canonical";
  private static final String PARSEEDGEIDS_ATT = "parse.edgeids";
  private static final String PARSEEDGEIDS_DEFAULT_VALUE = "canonical";
  private static final String PARSEORDER_ATT = "parse.order";
  private static final String PARSEORDER_DEFAULT_VALUE = "nodesfirst";

  private static final String ATTR_TYPE_ATT = "attr.type";
  private static final String ATTR_NAME_ATT = "attr.name";
  private static final String FOR_ATT = "for";
  private static final String ID_ATT = "id";
  private static final String KEY_TAG = "key";
  private static final String DATA_TAG = "data";

  private enum Property {
    NODE_NUMBER(NODE_TAG, "number", "int"),//
    NODE_TOKEN(NODE_TAG, "tokens", "string"),//
    EDGE_NUMBER(EDGE_TAG, "number", "int"),//
    EDGE_TYPE(EDGE_TAG, "type", "string"),//
    EDGE_WITNESSES(EDGE_TAG, "witnesses", "string");

    private String name;
    private String forElement;
    private String type;

    private Property(String forElement, String name, String type) {
      this.name = name;
      this.forElement = forElement;
      this.type = type;
    }

    public void write(String data, XMLStreamWriter xml) throws XMLStreamException {
      xml.writeStartElement(GRAPHML_NS, DATA_TAG);
      xml.writeAttribute(KEY_TAG, "d" + ordinal());
      xml.writeCharacters(data);
      xml.writeEndElement();
    }

    public void declare(XMLStreamWriter xml) throws XMLStreamException {
      xml.writeEmptyElement(GRAPHML_NS, KEY_TAG);
      xml.writeAttribute(ID_ATT, "d" + ordinal());
      xml.writeAttribute(FOR_ATT, forElement);
      xml.writeAttribute(ATTR_NAME_ATT, name);
      xml.writeAttribute(ATTR_TYPE_ATT, type);
    }
  }

}
