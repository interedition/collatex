package eu.interedition.collatex.xml;

import org.junit.Assert;
import org.junit.Test;
import org.lmnl.AnnotationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Transactional
public class DatabaseSetupTest extends AbstractTest {

  @Autowired
  private AnnotationRepository annotationRepository;

  @Test
  public void checkSetup() {
    Assert.assertNotNull(annotationRepository);
  }
}
