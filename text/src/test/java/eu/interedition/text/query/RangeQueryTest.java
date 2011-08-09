package eu.interedition.text.query;

import com.google.common.base.Joiner;
import eu.interedition.text.AbstractTestResourceTest;
import eu.interedition.text.Annotation;
import org.junit.Test;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class RangeQueryTest extends AbstractTestResourceTest {

  @Test
  public void searchEmptyRanges() {
    final Iterable<Annotation> empties = annotationRepository.find(Criteria.and(Criteria.text(text()), Criteria.rangeLength(0)));
    LOG.debug(Joiner.on('\n').join(empties));
  }
}
