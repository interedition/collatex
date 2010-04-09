package eu.interedition.collatex2.implementation.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;

public class WitnessIndexTest {
  private static final Log LOG = LogFactory.getLog(WitnessIndexTest.class);
  private Factory factory;

  @Before
  public void setup() {
    factory = new Factory();
  }

  @Test
  public void test() {
    final IWitness witnessA = factory.createWitness("A", "the big black cat and the big black rat");
    final IWitnessIndex index = new WitnessIndex(witnessA, witnessA.findRepeatingTokens());
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
    final IWitnessIndex index = new WitnessIndex(witnessA, Lists.newArrayList("the", "big", "black"));
    LOG.info(index.getPhrases());
    assertEquals(6, index.size());
    assertTrue(index.contains("# the"));
    assertTrue(index.contains("# the big"));
    assertTrue(index.contains("# the big black"));
    assertTrue(index.contains("the big black #"));
    assertTrue(index.contains("big black #"));
    assertTrue(index.contains("black #"));
    assertEquals(6, index.size());
  }
}
