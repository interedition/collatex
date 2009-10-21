package eu.interedition.collatex.superbase;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.AlignmentTableCreator;
import eu.interedition.collatex.input.Segment;
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
  public void testTransposition() {
    Segment w1 = builder.build("A", "the cat is black");
    Segment w2 = builder.build("B", "black is the cat");
    WitnessSet set = new WitnessSet(w1, w2);
    AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    String expected;
    expected = "A: the|cat|is|black\n";
    expected += "B: black|is|the|cat\n";

    assertEquals(expected, table.toString());
  }

  @Test
  @Ignore
  public void testAdditionInCombinationWithTransposition() {
    Segment w1 = builder.build("A", "the cat is very happy");
    Segment w2 = builder.build("B", "very happy is the cat");
    Segment w3 = builder.build("C", "very delitied and happy is the cat");
    WitnessSet set = new WitnessSet(w1, w2, w3);
    AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    String expected;
    expected = "A: the| | |cat|is|very|happy\n";
    expected += "B: very| | |happy|is|the|cat\n";
    expected += "C: very|delitied|and|happy|is|the|cat\n";

    assertEquals(expected, table.toString());
  }

}
