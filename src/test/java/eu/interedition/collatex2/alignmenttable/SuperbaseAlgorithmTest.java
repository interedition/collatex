package eu.interedition.collatex2.alignmenttable;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;

//Note: this test are very similar to the alignment table 2 tests!
//Note: since the superbase algorithm class becomes more like a container, and does not contain any 
//Note: responsibility the tests should just move to there!
public class SuperbaseAlgorithmTest {
  private static Factory factory;

  @BeforeClass
  public static void setUp() {
    factory = new Factory();
  }

  @Test
  public void testFirstWitness() {
    final IWitness w1 = factory.createWitness("A", "the black cat");
    //final WitnessSet set = new WitnessSet(w1);
    final List<IWitness> set = Lists.newArrayList(w1);
    final IAlignmentTable table = factory.createNewAlignmentTable(set);
    final String expected = "A: the|black|cat\n";
    assertEquals(expected, table.toString());
  }

  @Ignore
  @Test
  public void testEverythingMatches() {
    final IWitness w1 = factory.createWitness("A", "the black cat");
    final IWitness w2 = factory.createWitness("B", "the black cat");
    final IWitness w3 = factory.createWitness("C", "the black cat");
    //final WitnessSet set = new WitnessSet(w1, w2, w3);
    final List<IWitness> set = Lists.newArrayList(w1, w2, w3);
    final IAlignmentTable table = factory.createNewAlignmentTable(set);
    String expected = "A: the|black|cat\n";
    expected += "B: the|black|cat\n";
    expected += "C: the|black|cat\n";
    assertEquals(expected, table.toString());
  }

}
