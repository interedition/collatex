package eu.interedition.collatex.superbase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.NoSuchElementException;

import org.junit.Test;

import eu.interedition.collatex.alignment.multiple_witness.Column;
import eu.interedition.collatex.alignment.multiple_witness.Superbase;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class ColumnTest {
  @Test(expected = NoSuchElementException.class)
  public void testGetWordNonExistingGivesException() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness witness = builder.build("A", "a test string");
    Witness witnessB = builder.build("B", "different");
    Word word = witness.getWordOnPosition(1);
    Column column = new Column(word);
    column.getWord(witnessB);
  }

  @Test
  public void testContainsWitness() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness witness = builder.build("A", "a test string");
    Witness witnessB = builder.build("B", "different");
    Word word = witness.getWordOnPosition(1);
    Column column = new Column(word);
    assertTrue(column.containsWitness(witness));
    assertFalse(column.containsWitness(witnessB));
  }

  @Test
  public void testInverseWordMap() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness witness = builder.build("A", "first");
    Witness witnessB = builder.build("B", "second");
    Witness witnessC = builder.build("C", "third");
    Word word = witness.getWordOnPosition(1);
    Word wordB = witnessB.getWordOnPosition(1);
    Word wordC = witnessC.getWordOnPosition(1);
    Column column = new Column(word);
    column.addVariant(wordB);
    column.addVariant(wordC);
    Superbase superbase = new Superbase();
    column.addToSuperbase(superbase);
    assertEquals("first second third", superbase.toString());
  }

  @Test
  public void testInverseWordMap2() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness witness = builder.build("A", "first");
    Witness witnessB = builder.build("B", "match");
    Witness witnessC = builder.build("C", "match");
    Word word = witness.getWordOnPosition(1);
    Word wordB = witnessB.getWordOnPosition(1);
    Word wordC = witnessC.getWordOnPosition(1);
    Column column = new Column(word);
    column.addVariant(wordB);
    column.addMatch(wordC);
    Superbase superbase = new Superbase();
    column.addToSuperbase(superbase);
    assertEquals("first match", superbase.toString());
  }

}
