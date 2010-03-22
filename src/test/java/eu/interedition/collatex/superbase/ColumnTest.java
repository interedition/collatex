package eu.interedition.collatex.superbase;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.interedition.collatex.alignment.multiple_witness.Column;
import eu.interedition.collatex.alignment.multiple_witness.Superbase;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class ColumnTest {

  @Test
  public void testInverseWordMap2() {
    final WitnessBuilder builder = new WitnessBuilder();
    final Witness witness = builder.build("A", "first");
    final Witness witnessB = builder.build("B", "match");
    final Witness witnessC = builder.build("C", "match");
    final Word word = witness.getFirstSegment().getElementOnWordPosition(1);
    final Word wordB = witnessB.getFirstSegment().getElementOnWordPosition(1);
    final Word wordC = witnessC.getFirstSegment().getElementOnWordPosition(1);
    final Column column = new Column(word);
    column.addVariant(wordB);
    column.addMatch(wordC);
    final Superbase superbase = new Superbase();
    column.addToSuperbase(superbase);
    assertEquals("first match", superbase.toString());
  }

}
