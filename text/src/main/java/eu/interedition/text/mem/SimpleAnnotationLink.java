package eu.interedition.text.mem;

import eu.interedition.text.Annotation;
import eu.interedition.text.AnnotationLink;
import eu.interedition.text.QName;

import java.util.HashSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleAnnotationLink extends HashSet<Annotation> implements AnnotationLink {
  private final QName name;

  public SimpleAnnotationLink(QName name) {
    this.name = name;
  }

  public QName getName() {
    return name;
  }
}
