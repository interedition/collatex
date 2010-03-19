package eu.interedition.collatex2.indexing;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mortbay.log.Log;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;

public class IndexingTest {
  private Factory factory;

  @Before
  public void setup() {
    factory = new Factory();
  }

  @Test
  public void test1() {
    final IWitness a = factory.createWitness("A", "tobe or not tobe");
    final IWitnessIndex index = Factory.createWitnessIndex(a);
    assertEquals(6, index.size());
    assertTrue(index.contains("# tobe"));
    assertTrue(index.contains("tobe or"));
    assertTrue(index.contains("or"));
    assertFalse(index.contains("or not"));
    assertTrue(index.contains("not"));
    assertFalse(index.contains("or tobe"));
    assertTrue(index.contains("not tobe"));
    assertTrue(index.contains("tobe #"));
  }

  //  @Ignore
  @Test
  public void test2() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    Log.info("witness = [the big black cat and the big black rat]");
    final IWitnessIndex index = Factory.createWitnessIndex(a);
    assertTrue(index.contains("# the big black"));
    assertTrue(index.contains("the big black cat"));
    assertTrue(index.contains("cat"));
    assertTrue(index.contains("and"));
    assertTrue(index.contains("and the big black"));
    assertTrue(index.contains("the big black rat"));
    assertTrue(index.contains("rat"));
    assertEquals(7, index.size());
  }

  //  @Test
  //  public void test3() {
  //    final IWitness a = factory.createWitness("A", "X C A B Y C A Z A B W");
  //    Log.info("witness = [X C A B Y C A Z A B W]");
  //    final IWitnessIndex index = Factory.createWitnessIndex(a);
  //    assertTrue(index.contains("# the big black"));
  //    assertTrue(index.contains("the big black cat"));
  //    assertTrue(index.contains("and"));
  //    assertTrue(index.contains("and the big black"));
  //    assertTrue(index.contains("the big black rat"));
  //    assertEquals(5, index.size());
  //  }
}
