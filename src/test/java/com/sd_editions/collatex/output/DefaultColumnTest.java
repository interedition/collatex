package com.sd_editions.collatex.output;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.NoSuchElementException;

import org.junit.Test;

import com.sd_editions.collatex.permutations.Witness;
import com.sd_editions.collatex.permutations.WitnessBuilder;
import com.sd_editions.collatex.permutations.Word;

public class DefaultColumnTest {
  @Test(expected = NoSuchElementException.class)
  public void testGetWordNonExistingGivesException() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness witness = builder.build("A", "a test string");
    Witness witnessB = builder.build("B", "different");
    Word word = witness.getWordOnPosition(1);
    DefaultColumn column = new DefaultColumn(word);
    column.getWord(witnessB);
  }

  @Test
  public void testContainsWitness() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness witness = builder.build("A", "a test string");
    Witness witnessB = builder.build("B", "different");
    Word word = witness.getWordOnPosition(1);
    DefaultColumn column = new DefaultColumn(word);
    assertTrue(column.containsWitness(witness));
    assertFalse(column.containsWitness(witnessB));
  }

}
