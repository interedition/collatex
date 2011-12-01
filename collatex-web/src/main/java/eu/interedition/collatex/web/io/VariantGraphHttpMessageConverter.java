package eu.interedition.collatex.web.io;

import eu.interedition.collatex.implementation.CollateXEngine;
import eu.interedition.collatex.implementation.output.GraphMLBuilder;
import eu.interedition.collatex.interfaces.IVariantGraph;
import eu.interedition.collatex.web.GraphVizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.xml.TransformerUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;

/**
 * Converts variant graphs into HTTP messages.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphHttpMessageConverter extends AbstractHttpMessageConverter<IVariantGraph> {
  /**
   * The GraphML MIME type.
   */
  protected static final MediaType APPLICATION_XML_GRAPHML = new MediaType("application", "graphml+xml");

  /**
   * The SVG MIME type.
   */
  protected static final MediaType IMAGE_SVG_XML = new MediaType("image", "svg+xml");

  /**
   * CollateX custom namespace.
   */
  protected static final String COLLATEX_NS = "http://interedition.eu/collatex/ns/1.0";

  /**
   * The TEI P5 namespace.
   */
  protected static final String TEI_NS = "http://www.tei-c.org/ns/1.0";

  @Autowired
  private GraphVizService graphVizService;

  /**
   * Constructs a converter that supports XML and GraphML MIME types.
   */
  public VariantGraphHttpMessageConverter() {
    super(MediaType.APPLICATION_XML, IMAGE_SVG_XML, APPLICATION_XML_GRAPHML);
  }

  /**
   * We can only write messages.
   *
   * @param mediaType ignored
   * @return always <code>false</code>
   */
  @Override
  protected boolean canRead(MediaType mediaType) {
    return false;
  }

  /**
   * We support variant graphs.
   *
   * @param clazz the class of the object to convert
   * @return <code>true</code> if the object implements {@link IVariantGraph}
   */
  @Override
  protected boolean supports(Class<?> clazz) {
    return IVariantGraph.class.isAssignableFrom(clazz);
  }

  @Override
  protected IVariantGraph readInternal(Class<? extends IVariantGraph> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    throw new HttpMessageNotReadableException(clazz.toString());
  }

  @Override
  protected void writeInternal(IVariantGraph vg, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    try {
      final MediaType contentType = outputMessage.getHeaders().getContentType();

      if (contentType != null && contentType.isCompatibleWith(APPLICATION_XML_GRAPHML)) {
        final Document graphXML = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        GraphMLBuilder.build(vg, graphXML);

        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        TransformerUtils.enableIndenting(transformer, 4);
        transformer.transform(new DOMSource(graphXML), new StreamResult(outputMessage.getBody()));

      } else if (contentType != null && contentType.isCompatibleWith(IMAGE_SVG_XML)) {
        if (graphVizService.isSvgAvailable()) {
          graphVizService.toSvg(vg, outputMessage.getBody());
        } else {
          throw new HttpMessageNotWritableException(String.format("%s dot not available for SVG serialization", graphVizService.getConfiguredDotPath()));
        }
      } else if (contentType != null && contentType.isCompatibleWith(MediaType.APPLICATION_XML)) {
        final Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        final Element root = xml.createElementNS(COLLATEX_NS, "collatex:apparatus");
        xml.appendChild(root);
        root.setAttribute("xmlns", TEI_NS);

        // FIXME: new CollateXEngine().createApparatus(vg).serialize(root);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        TransformerUtils.enableIndenting(transformer, 4);
        transformer.transform(new DOMSource(xml), new StreamResult(outputMessage.getBody()));

      } else {
        throw new HttpMessageNotWritableException(contentType == null ? "No content type" : contentType.toString());
      }
    } catch (ParserConfigurationException e) {
      throw new HttpMessageNotWritableException("Error while serializing variant graph", e);
    } catch (TransformerException e) {
      throw new HttpMessageNotWritableException("Error while serializing variant graph", e);
    }
  }
}
