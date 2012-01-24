package eu.interedition.web.io;

import com.google.common.io.Closeables;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.web.collatex.GraphVizService;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphVizHttpMessageConverter extends AbstractHttpMessageConverter<VariantGraph> {
  /**
   * The SVG MIME type.
   */
  protected static final MediaType IMAGE_SVG_XML = new MediaType("image", "svg+xml");

  private GraphVizService graphVizService;

  public VariantGraphVizHttpMessageConverter(GraphVizService graphVizService) {
    super(MediaType.TEXT_PLAIN, IMAGE_SVG_XML);
    this.graphVizService = graphVizService;
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
    final MediaType contentType = outputMessage.getHeaders().getContentType();
    if (contentType != null && contentType.isCompatibleWith(IMAGE_SVG_XML) && !graphVizService.isSvgAvailable()) {
      throw new HttpMessageNotWritableException("SVG generation via GraphViz' \"dot\" not available on this server");
    }

    final OutputStream body = outputMessage.getBody();
    try {
      if (contentType != null && contentType.isCompatibleWith(IMAGE_SVG_XML)) {
        graphVizService.toSvg(graph, body);
      } else {
        graphVizService.toDot(graph, new OutputStreamWriter(body, Charset.forName("UTF-8")));
      }
    } finally {
      Closeables.closeQuietly(body);
    }
  }
}
