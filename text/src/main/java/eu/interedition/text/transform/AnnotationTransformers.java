package eu.interedition.text.transform;

import eu.interedition.text.Text;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationTransformers {

  public static AdoptAnnotationTransformer adopt(Text text) {
    return new AdoptAnnotationTransformer(text);
  }

  public static ShiftAnnotationTransformer shift(long delta) {
    return new ShiftAnnotationTransformer(delta);
  }
}
