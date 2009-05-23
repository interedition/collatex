package eu.interedition.collatex.superbase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.NoSuchElementException;

import org.junit.Test;


import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessBuilder;
import eu.interedition.collatex.input.Word;

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

}
