package eu.interedition.collatex.superbase;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.AlignmentTableCreator;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSet;
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

  @Test
  public void testAddition() {
    final Witness w1 = builder.build("A", "the cat");
    final Witness w2 = builder.build("B", "before the cat");
    final Witness w3 = builder.build("C", "the black cat");
    final Witness w4 = builder.build("D", "the cat walks");
    final WitnessSet set = new WitnessSet(w1, w2, w3, w4);
    final AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    String expected = "A:  |the| |cat| \n";
    expected += "B: before|the| |cat| \n";
    expected += "C:  |the|black|cat| \n";
    expected += "D:  |the| |cat|walks\n";

    assertEquals(expected, table.toString());
  }

  // TODO: make the tostring on the alignmenttable
  // TODO: work with multiple spaces for an empty cell
  // TODO: fix the gap bug for the last gap

  @Test
  public void testGenSuperbase() {
    final Witness w1 = builder.build("A", "the cat");
    final Witness w2 = builder.build("B", "before the cat");
    final Witness w3 = builder.build("C", "the black cat");
    final Witness w4 = builder.build("D", "just before midnight the cat walks");
    final WitnessSet set = new WitnessSet(w1, w2, w3, w4);
    final AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    String expected = "A:  | | |the| |cat| \n";
    expected += "B:  |before| |the| |cat| \n";
    expected += "C:  | | |the|black|cat| \n";
    expected += "D: just|before|midnight|the| |cat|walks\n";

    assertEquals(expected, table.toString());
  }

  @Test
  public void testVariation() {
    final Witness w1 = builder.build("A", "the black cat");
    final Witness w2 = builder.build("B", "the black and white cat");
    final Witness w3 = builder.build("C", "the black very special cat");
    final Witness w4 = builder.build("D", "the black not very special cat");
    final WitnessSet set = new WitnessSet(w1, w2, w3, w4);
    final AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    String expected = "A: the|black| | | |cat\n";
    expected += "B: the|black| |and|white|cat\n";
    expected += "C: the|black| |very|special|cat\n";
    expected += "D: the|black|not|very|special|cat\n";

    assertEquals(expected, table.toString());
  }

  @Test
  public void testWitnessReorder() {
    final Witness w1 = builder.build("A", "the black cat");
    final Witness w2 = builder.build("B", "the black and white cat");
    final Witness w3 = builder.build("C", "the black not very special cat");
    final Witness w4 = builder.build("D", "the black very special cat");
    final WitnessSet set = new WitnessSet(w1, w2, w3, w4);
    final AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    String expected = "A: the|black| | | |cat\n";
    expected += "B: the|black|and|white| |cat\n";
    expected += "C: the|black|not|very|special|cat\n";
    expected += "D: the|black| |very|special|cat\n";

    assertEquals(expected, table.toString());
  }

  // TODO: add this for replacements in comb. with transposit.
  //  // Note: this is with an unequal transposition sequence size!
  //  @Test
  //  public void testAdditionInCombinationWithTransposition() {
  //    Witness w1 = builder.build("A", "the cat is black");
  //    Witness w2 = builder.build("B", "black is the cat");
  //    Witness w3 = builder.build("C", "black and white is the cat");
  //    SuperbaseAlgorithm magic = new SuperbaseAlgorithm(w1, w2, w3);
  //    AlignmentTable2 table = magic.createAlignmentTable();
  //    String expected;
  //    expected = "A: the|cat| |is|black| |\n";
  //    expected += "B: black| | |is||the|cat\n";
  //    expected += "C: black|and|white|is|the|cat\n";
  //
  //    assertEquals(expected, table.toString());
  //  }

  // TODO: test a variant with multiple words
  // TODO: hint: more words than the original string it replaces

  // TODO: test a variant where one words turns out
  // todo: to be a match later
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
