package eu.interedition.text.mem;

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import eu.interedition.text.*;
import eu.interedition.text.util.Annotations;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleAnnotation implements Annotation {
  private final Text text;
  private final QName name;
  private final Range range;
  private final Set<AnnotationLink> links = Sets.newHashSet();

  public SimpleAnnotation(Text text, QName name, Range range) {
    this.text = text;
    this.name = name;
    this.range = range;
  }

  public Text getText() {
    return text;
  }

  public QName getName() {
    return name;
  }

  public Range getRange() {
    return range;
  }

  public Set<AnnotationLink> getLinks() {
    return links;
  }

  public int compareTo(Annotation o) {
    return Annotations.compare(this, o).compare(this, o, Ordering.arbitrary()).result();
  }
}
