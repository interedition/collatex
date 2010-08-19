package eu.interedition.collatex2.implementation.indexing;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.containers.witness.WitnessIndex;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.ITokenIndex;

public class WitnessIndexTest {
  private static final Logger LOG = LoggerFactory.getLogger(WitnessIndexTest.class);
  private CollateXEngine factory;

  @Before
  public void setup() {
    factory = new CollateXEngine();
  }

  @Test
  public void test() {
    final IWitness witnessA = factory.createWitness("A", "the big black cat and the big black rat");
    final ITokenIndex index = new WitnessIndex(witnessA, witnessA.findRepeatingTokens());
    // The index should contain all unique n-grams with 
    //   0 or more tokens         occurring multiple times in the witness, and
    //   exactly 1 token (or '#') occurring only once      in the witness
    assertTrue(index.contains("# the"));
    assertTrue(index.contains("# the big"));
    assertTrue(index.contains("# the big black"));
    assertTrue(index.contains("the big black cat"));
    assertTrue(index.contains("big black cat"));
    assertTrue(index.contains("black cat"));
    assertTrue(index.contains("cat"));
    assertTrue(index.contains("and"));
    assertTrue(index.contains("and the"));
    assertTrue(index.contains("and the big"));
    assertTrue(index.contains("and the big black"));
    assertTrue(index.contains("the big black rat"));
    assertTrue(index.contains("big black rat"));
    assertTrue(index.contains("black rat"));
    assertTrue(index.contains("rat"));
    assertEquals(15, index.size());
  }

  @Test
  public void test2() {
    final IWitness witnessA = factory.createWitness("A", "the big black");
    final ITokenIndex index = new WitnessIndex(witnessA, Lists.newArrayList("the", "big", "black"));
    LOG.debug(index.keys().toString());
    assertEquals(6, index.size());
    assertTrue(index.contains("# the"));
    assertTrue(index.contains("# the big"));
    assertTrue(index.contains("# the big black"));
    assertTrue(index.contains("the big black #"));
    assertTrue(index.contains("big black #"));
    assertTrue(index.contains("black #"));
    assertEquals(6, index.size());
  }

  @Test
  public void test1() {
    final IWitness a = factory.createWitness("A", "tobe or not tobe");
    final ITokenIndex index = CollateXEngine.createWitnessIndex(a);
    assertEquals(6, index.size());
    assertTrue(index.contains("# tobe"));
    assertTrue(index.contains("tobe or"));
    assertTrue(index.contains("or"));
    assertTrue(!index.contains("or not"));
    assertTrue(index.contains("not"));
    assertTrue(!index.contains("or tobe"));
    assertTrue(index.contains("not tobe"));
    assertTrue(index.contains("tobe #"));
  }

}
