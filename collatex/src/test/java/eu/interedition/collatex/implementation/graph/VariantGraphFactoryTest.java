package eu.interedition.collatex.implementation.graph;

import com.google.common.collect.Iterables;
import eu.interedition.collatex.AbstractTest;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphFactoryTest extends AbstractTest {

  @Test
  public void distinctGraphsCreated() throws IOException {
    Assert.assertFalse(graphFactory.newVariantGraph().equals(graphFactory.newVariantGraph()));
  }

  @Test
  public void emptyGraphCanBeTraversed() {
    Assert.assertEquals(2, Iterables.size(graphFactory.newVariantGraph().vertices()));
  }
}
