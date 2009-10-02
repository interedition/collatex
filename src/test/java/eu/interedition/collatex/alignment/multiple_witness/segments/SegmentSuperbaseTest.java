package eu.interedition.collatex.alignment.multiple_witness.segments;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class SegmentSuperbaseTest {
  private static WitnessBuilder builder;

  @BeforeClass
  public static void setUp() {
    builder = new WitnessBuilder();
  }

  @Test
  public void testCreateSuperBase() {
    Witness a = builder.build("A", "the first witness");
    AlignmentTable3 alignmentTable = new AlignmentTable3();
    alignmentTable.addWitness(a);
    SegmentSuperbase superbase = SegmentSuperbase.create(alignmentTable);
    assertEquals("the first witness", superbase.toString());
  }

}
