package eu.interedition.text.xml;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLNodePathTest {

  @Test
  public void comparision() {
    final XMLNodePath np1 = new XMLNodePath();
    np1.push(1);
    np1.push(2);

    final XMLNodePath np2 = new XMLNodePath();
    np2.push(1);
    np2.push(2);
    np2.push(3);

    Assert.assertTrue(np1.equals(np1));
    Assert.assertFalse(np1.equals(np2));
    Assert.assertEquals(0, np1.compareTo(np1));
    Assert.assertEquals(-1, np1.compareTo(np2));
    Assert.assertEquals(1, np2.compareTo(np1));
  }
}
