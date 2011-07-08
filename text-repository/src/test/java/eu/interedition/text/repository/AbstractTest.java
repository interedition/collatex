package eu.interedition.text.repository;

import eu.interedition.text.rdbms.RelationalQNameRepository;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.AfterTransaction;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/eu/interedition/text/rdbms/repository-context.xml", "classpath:/eu/interedition/text/repository/service-context.xml"})
public abstract class AbstractTest {
  @Autowired
  protected RelationalQNameRepository nameRepository;

  @AfterTransaction
  public void clearNameCache() {
    nameRepository.clearCache();
  }
}
