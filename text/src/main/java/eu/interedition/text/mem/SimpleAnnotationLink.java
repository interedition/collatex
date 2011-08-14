package eu.interedition.text.mem;

import eu.interedition.text.AnnotationLink;
import eu.interedition.text.QName;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleAnnotationLink implements AnnotationLink {
  private final QName name;

  public SimpleAnnotationLink(QName name) {
    this.name = name;
  }

  public QName getName() {
    return name;
  }
}
