package eu.interedition.collatex.implementation.graph.db;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphFactoryTest {

  @Test
  public void distinctGraphsCreated() throws IOException {
    final VariantGraphFactory gf = new VariantGraphFactory();
    Assert.assertFalse(gf.create().equals(gf.create()));
  }
}
