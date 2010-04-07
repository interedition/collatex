package eu.interedition.collatex.alignmenttable;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.AlignmentTableCreator;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class FutureTranspositionHolderTest {

  private static WitnessBuilder builder;

  @BeforeClass
  public static void setUp() {
    builder = new WitnessBuilder();
  }

  @Test
  @Ignore
  public void testAdditionInCombinationWithTransposition() {
    final Witness w1 = builder.build("A", "the cat is very happy");
    final Witness w2 = builder.build("B", "very happy is the cat");
    final Witness w3 = builder.build("C", "very delitied and happy is the cat");
    final WitnessSet set = new WitnessSet(w1, w2, w3);
    final AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    String expected;
    expected = "A: the| | |cat|is|very|happy\n";
    expected += "B: very| | |happy|is|the|cat\n";
    expected += "C: very|delitied|and|happy|is|the|cat\n";

    assertEquals(expected, table.toString());
  }

}
