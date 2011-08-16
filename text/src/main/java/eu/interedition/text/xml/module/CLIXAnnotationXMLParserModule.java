package eu.interedition.text.xml.module;

import com.google.common.collect.Maps;
import eu.interedition.text.AnnotationRepository;
import eu.interedition.text.QName;
import eu.interedition.text.Range;
import eu.interedition.text.TextConstants;
import eu.interedition.text.mem.SimpleAnnotation;
import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLParserState;

import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class CLIXAnnotationXMLParserModule extends AbstractAnnotationXMLParserModule {
  private final ThreadLocal<Map<String, SimpleAnnotation>> annotations = new ThreadLocal<Map<String, SimpleAnnotation>>();
  private final ThreadLocal<Map<String, Map<QName, String>>> attributes = new ThreadLocal<Map<String, Map<QName, String>>>();

  public CLIXAnnotationXMLParserModule(AnnotationRepository annotationRepository, int batchSize) {
    super(annotationRepository, batchSize);
  }

  @Override
  public void start(XMLParserState state) {
    super.start(state);
    annotations.set(Maps.<String, SimpleAnnotation>newHashMap());
    attributes.set(Maps.<String, Map<QName, String>>newHashMap());
  }

  @Override
  public void end(XMLParserState state) {
    attributes.remove();
    annotations.remove();
    super.end(state);
  }

  @Override
  public void start(XMLEntity entity, XMLParserState state) {
    super.start(entity, state);

    final Map<QName, String> entityAttributes = Maps.newHashMap(entity.getAttributes());
    final String startId = entityAttributes.remove(TextConstants.CLIX_START_ATTR_NAME);
    final String endId = entityAttributes.remove(TextConstants.CLIX_END_ATTR_NAME);
    if (startId == null && endId == null) {
      return;
    }

    final Map<String, SimpleAnnotation> annotations = this.annotations.get();
    final Map<String, Map<QName, String>> attributes = this.attributes.get();
    final long textOffset = state.getTextOffset();

    if (startId != null) {
      annotations.put(startId, new SimpleAnnotation(state.getTarget(), entity.getName(), new Range(textOffset, textOffset)));
      attributes.put(startId, entityAttributes);
    }
    if (endId != null) {
      final SimpleAnnotation a = annotations.remove(endId);
      final Map<QName, String> attr = attributes.remove(endId);
      if (a != null && attr != null) {
        add(new SimpleAnnotation(a.getText(), a.getName(), new Range(a.getRange().getStart(), textOffset)), attr);
      }
    }
  }
}
