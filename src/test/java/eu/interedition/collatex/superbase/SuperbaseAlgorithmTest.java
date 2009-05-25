package eu.interedition.collatex.superbase;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessBuilder;

public class SuperbaseAlgorithmTest {
  private static WitnessBuilder builder;

  @BeforeClass
  public static void setUp() {
    builder = new WitnessBuilder();
  }

  @Test
  public void testEverythingMatches() {
    Witness w1 = builder.build("A", "the black cat");
    Witness w2 = builder.build("B", "the black cat");
    Witness w3 = builder.build("C", "the black cat");
    SuperbaseAlgorithm magic = new SuperbaseAlgorithm(w1, w2, w3);
    AlignmentTable2 table = magic.createAlignmentTable();
    String expected = "A: the|black|cat\n";
    expected += "B: the|black|cat\n";
    expected += "C: the|black|cat\n";
    assertEquals(expected, table.toString());
  }

  @Test
  public void testOmission() {
    Witness w1 = builder.build("A", "the black cat");
    Witness w2 = builder.build("B", "the cat");
    Witness w3 = builder.build("C", "the black cat");
    SuperbaseAlgorithm magic = new SuperbaseAlgorithm(w1, w2, w3);
    AlignmentTable2 table = magic.createAlignmentTable();
    String expected = "A: the|black|cat\n";
    expected += "B: the| |cat\n";
    expected += "C: the|black|cat\n";
    assertEquals(expected, table.toString());
  }

  @Test
  public void testVariant() {
    Witness w1 = builder.build("A", "the black cat");
    Witness w2 = builder.build("B", "the white cat");
    Witness w3 = builder.build("C", "the green cat");
    Witness w4 = builder.build("D", "the red cat");
    Witness w5 = builder.build("E", "the yellow cat");
    SuperbaseAlgorithm magic = new SuperbaseAlgorithm(w1, w2, w3, w4, w5);
    AlignmentTable2 table = magic.createAlignmentTable();
    String expected = "A: the|black|cat\n";
    expected += "B: the|white|cat\n";
    expected += "C: the|green|cat\n";
    expected += "D: the|red|cat\n";
    expected += "E: the|yellow|cat\n";
    assertEquals(expected, table.toString());
  }

  @Test
  public void testAddition() {
    Witness w1 = builder.build("A", "the cat");
    Witness w2 = builder.build("B", "before the cat");
    Witness w3 = builder.build("C", "the black cat");
    Witness w4 = builder.build("D", "the cat walks");
    SuperbaseAlgorithm magic = new SuperbaseAlgorithm(w1, w2, w3, w4);
    AlignmentTable2 table = magic.createAlignmentTable();
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
    Witness w1 = builder.build("A", "the cat");
    Witness w2 = builder.build("B", "before the cat");
    Witness w3 = builder.build("C", "the black cat");
    Witness w4 = builder.build("D", "just before midnight the cat walks");
    SuperbaseAlgorithm magic = new SuperbaseAlgorithm(w1, w2, w3, w4);
    AlignmentTable2 table = magic.createAlignmentTable();
    String expected = "A:  | | |the| |cat| \n";
    expected += "B:  |before| |the| |cat| \n";
    expected += "C:  | | |the|black|cat| \n";
    expected += "D: just|before|midnight|the| |cat|walks\n";

    assertEquals(expected, table.toString());
  }

  @Test
  public void testVariation() {
    Witness w1 = builder.build("A", "the black cat");
    Witness w2 = builder.build("B", "the black and white cat");
    Witness w3 = builder.build("C", "the black very special cat");
    Witness w4 = builder.build("D", "the black not very special cat");
    SuperbaseAlgorithm magic = new SuperbaseAlgorithm(w1, w2, w3, w4);
    AlignmentTable2 table = magic.createAlignmentTable();
    String expected = "A: the|black| | | |cat\n";
    expected += "B: the|black| |and|white|cat\n";
    expected += "C: the|black| |very|special|cat\n";
    expected += "D: the|black|not|very|special|cat\n";

    assertEquals(expected, table.toString());
  }

  @Test
  public void testWitnessReorder() {
    Witness w1 = builder.build("A", "the black cat");
    Witness w2 = builder.build("B", "the black and white cat");
    Witness w3 = builder.build("C", "the black not very special cat");
    Witness w4 = builder.build("D", "the black very special cat");
    SuperbaseAlgorithm magic = new SuperbaseAlgorithm(w1, w2, w3, w4);
    AlignmentTable2 table = magic.createAlignmentTable();
    String expected = "A: the|black| | | |cat\n";
    expected += "B: the|black|and|white| |cat\n";
    expected += "C: the|black|not|very|special|cat\n";
    expected += "D: the|black| |very|special|cat\n";

    assertEquals(expected, table.toString());
  }
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
