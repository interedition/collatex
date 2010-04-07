package eu.interedition.collatex.alignmenttable;

import org.junit.BeforeClass;

import eu.interedition.collatex.input.builders.WitnessBuilder;

// Note: this test are very similar to the alignment table 2 tests!
// Note: since the superbase algorithm class becomes more like a container, and does not contain any 
// Note: responsibility the tests should just move to there!
public class SuperbaseAlgorithmTest {
  private static WitnessBuilder builder;

  @BeforeClass
  public static void setUp() {
    builder = new WitnessBuilder();
  }

  // TODO test a variant where one words turns out
  // TODO to be a match later
  // this is an addition.. not the easiest test
  //  @Test
  //  public void testTwoWitnesses() {
  //    Witness w1 = builder.build("A", "the black cat");
  //    Witness w2 = builder.build("B", "the white and black cat");
  //    MagicClass2 magic = new MagicClass2(w1, w2);
  //    AlignmentTable2 table = magic.createAlignmentTable();
  //    String expected = "A: the| | |black|cat";
  //    expected += "B: the|white|and|black|cat";
  //    System.out.println(table.toString());
  //  }

}
