package eu.interedition.text.transform;

import com.google.common.base.Function;
import eu.interedition.text.Annotation;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface AnnotationTransformer extends Function<Annotation, Annotation> {
}
