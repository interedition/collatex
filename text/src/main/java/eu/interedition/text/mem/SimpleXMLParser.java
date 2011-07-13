package eu.interedition.text.mem;

import eu.interedition.text.Annotation;
import eu.interedition.text.QName;
import eu.interedition.text.Range;
import eu.interedition.text.xml.XMLParser;

import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleXMLParser extends XMLParser {
  @Override
  protected Annotation startAnnotation(Session session, QName name, Map<QName, String> attrs, int start) {
    return new SimpleAnnotation(session.target, name, new Range(start, start));
  }

  @Override
  protected Annotation endAnnotation(Annotation annotation, int end) {
    return new SimpleAnnotation(((SimpleAnnotation) annotation).getText(), annotation.getName(),//
            new Range(annotation.getRange().getStart(), end));
  }
}
