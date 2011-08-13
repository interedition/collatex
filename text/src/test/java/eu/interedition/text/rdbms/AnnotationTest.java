package eu.interedition.text.rdbms;

import com.google.common.collect.Iterables;
import eu.interedition.text.AbstractTestResourceTest;
import eu.interedition.text.Annotation;
import eu.interedition.text.AnnotationRepository;
import eu.interedition.text.Text;
import eu.interedition.text.query.Criteria;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static junit.framework.Assert.assertTrue;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationTest extends AbstractTestResourceTest {

  @Autowired
  private AnnotationRepository annotationRepository;

  @Test
  public void deleteAnnotations() {
    final Text text = text();
    try {
      annotationRepository.delete(Criteria.text(text));
      final Iterable<Annotation> remaining = annotationRepository.find(Criteria.text(text));
      assertTrue(Integer.toString(Iterables.size(remaining)), Iterables.isEmpty(remaining));
    } finally {
      unload();
    }
  }
}
