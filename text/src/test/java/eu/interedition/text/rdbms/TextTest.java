package eu.interedition.text.rdbms;

import eu.interedition.text.AbstractTextTest;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextTest extends AbstractTextTest {
  @Test
  public void digesting() throws Exception {
    text.getDigest().equals(DigestUtils.sha512Hex(TEST_TEXT));
  }
}
