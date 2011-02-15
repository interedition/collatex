package eu.interedition.collatex2.implementation.vg_alignment;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.ITokenIndex;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphIndexTest {
  private CollateXEngine factory;
  private Logger log = LoggerFactory.getLogger(VariantGraphIndexTest.class);
  
  @Before
  public void setup() {
    factory = new CollateXEngine();
  }
  
  @Test
  public void testIndexingRepeatedTokens() {
    IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    IVariantGraph graph = factory.graph(a);
    ITokenIndex index = graph.getTokenIndex(graph.getRepeatedTokens());
    assertTrue(index.contains("# the"));
    assertTrue(index.contains("the big black cat"));
    assertTrue(index.contains("# the big"));
    assertTrue(index.contains("big black cat"));
    assertTrue(index.contains("# the big black"));
    assertTrue(index.contains("black cat"));
    assertTrue(index.contains("cat"));
    assertTrue(index.contains("and"));
    assertTrue(index.contains("and the"));
    assertTrue(index.contains("the big black rat"));
    assertTrue(index.contains("and the big"));
    assertTrue(index.contains("big black rat"));
    assertTrue(index.contains("and the big black")); 
    assertTrue(index.contains("black rat"));
    assertTrue(index.contains("rat"));
    assertEquals(15, index.size());
  }

}
