package eu.interedition.collatex.implementation.graph.db;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import eu.interedition.collatex.AbstractTest;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphFactoryTest extends AbstractTest {

  @Test
  public void distinctGraphsCreated() throws IOException {
    Assert.assertFalse(variantGraphFactory.create().equals(variantGraphFactory.create()));
  }

  @Test
  public void emptyGraphCanBeTraversed() {
    Assert.assertEquals(2, Iterables.size(variantGraphFactory.create().traverseVertices(null)));
  }
}
