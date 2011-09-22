package eu.interedition.text.transform;

import eu.interedition.text.Annotation;
import eu.interedition.text.Text;
import eu.interedition.text.mem.SimpleAnnotation;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AdoptAnnotationTransformer implements AnnotationTransformer {
  private final Text text;

  public AdoptAnnotationTransformer(Text text) {
    this.text = text;
  }

  @Override
  public Annotation apply(Annotation input) {
    return new SimpleAnnotation(text, input.getName(), input.getRange());
  }
}
