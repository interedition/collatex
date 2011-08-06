package eu.interedition.text.util;

import eu.interedition.text.AnnotationLink;
import eu.interedition.text.AnnotationLinkRepository;

import java.util.Arrays;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class AbstractAnnotationLinkRepository implements AnnotationLinkRepository {

  public void delete(AnnotationLink... links) {
    delete(Arrays.asList(links));
  }
}
