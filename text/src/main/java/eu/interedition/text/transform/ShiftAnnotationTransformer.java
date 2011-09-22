package eu.interedition.text.transform;

import eu.interedition.text.Annotation;
import eu.interedition.text.mem.SimpleAnnotation;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ShiftAnnotationTransformer implements AnnotationTransformer {
  private final long delta;

  public ShiftAnnotationTransformer(long delta) {

    this.delta = delta;
  }
  @Override
  public Annotation apply(Annotation input) {
    return new SimpleAnnotation(input.getText(), input.getName(), input.getRange().shift(delta));
  }
}
