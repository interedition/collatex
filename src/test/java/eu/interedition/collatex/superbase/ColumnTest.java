package eu.interedition.collatex.superbase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.interedition.collatex.alignment.multiple_witness.Column;
import eu.interedition.collatex.alignment.multiple_witness.Superbase;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class ColumnTest {
  @Test
  public void testContainsWitness() {
    final WitnessBuilder builder = new WitnessBuilder();
    final Witness witness = builder.build("A", "a test string");
    final Witness witnessB = builder.build("B", "different");
    final Word word = witness.getFirstSegment().getElementOnWordPosition(1);
    final Column column = new Column(word);
    assertTrue(column.containsWitness(witness.getFirstSegment()));
    assertFalse(column.containsWitness(witnessB.getFirstSegment()));
  }

  @Test
  public void testInverseWordMap() {
    final WitnessBuilder builder = new WitnessBuilder();
    final Witness witness = builder.build("A", "first");
    final Witness witnessB = builder.build("B", "second");
    final Witness witnessC = builder.build("C", "third");
    final Word word = witness.getFirstSegment().getElementOnWordPosition(1);
    final Word wordB = witnessB.getFirstSegment().getElementOnWordPosition(1);
    final Word wordC = witnessC.getFirstSegment().getElementOnWordPosition(1);
    final Column column = new Column(word);
    column.addVariant(wordB);
    column.addVariant(wordC);
    final Superbase superbase = new Superbase();
    column.addToSuperbase(superbase);
    assertEquals("first second third", superbase.toString());
  }

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
