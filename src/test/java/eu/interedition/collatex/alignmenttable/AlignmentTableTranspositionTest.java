package eu.interedition.collatex.alignmenttable;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.AlignmentTableCreator;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class AlignmentTableTranspositionTest {

  private WitnessBuilder builder;

  @Before
  public void setup() {
    builder = new WitnessBuilder();
  }

  // TODO change expectations and make this work!
  @Ignore
  @Test
  public void testTranspositionsAreNotStoredInAlignmentTable() {
    final Witness a = builder.build("A", "the black and white cat");
    final Witness b = builder.build("B", "the white and black cat");
    final WitnessSet set = new WitnessSet(a, b);
    final AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    String expected = "A: the|black|and|white|cat\n";
    expected += "B: the|black|and|white|cat\n";
    assertEquals(expected, table.toString());
  }

  // THIS SHOULD NOT REALLY MATTER BECAUSE OF THE SEQUENCE DETECTION!
  //  @Test
  //  public void testMultipleTransposition() {
  //    final Witness a = builder.build("a b c");
  //    final Witness b = builder.build("b c a");
  //    final Alignment<Word> al = Matcher.align(a, b);
  //    Assert.assertTrue(al.getTranpositions().isEmpty());
  //
  //  }

  // HERE THERE IS NO TRANSPOSITION!
  //  @Test
  //  public void testGarbage() {
  //    final Witness a = builder.build("a b");
  //    final Witness b = builder.build("c a");
  //  }

}
