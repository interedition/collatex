package com.sd_editions.collatex.spike2;

import java.util.List;

import junit.framework.TestCase;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sd_editions.collatex.spike2.collate.Transposition;

public class ColorsTest extends TestCase {
  @SuppressWarnings("boxing")
  public void testFirstUseCasePeter() {
    String[] witnesses = new String[] { "The black cat", "The black and white cat", "The black and green cat" };
    Colors colors = new Colors(witnesses);
    assertEquals(6, colors.numberOfColors());
    assertEquals(Sets.newHashSet(1, 2, 3), colors.getWitnessIndex(1).getWordCodes());
    assertEquals(Sets.newHashSet(1, 2, 4, 5, 3), colors.getWitnessIndex(2).getWordCodes());
    assertEquals(Sets.newHashSet(1, 2, 4, 6, 3), colors.getWitnessIndex(3).getWordCodes());
    Comparison c1 = colors.compareWitness(1, 2);
    assertEquals(Lists.newArrayList("and", "white"), c1.getAddedWords());
    Comparison c2 = colors.compareWitness(1, 3);
    assertEquals(Lists.newArrayList("and", "green"), c2.getAddedWords());
    Comparison c3 = colors.compareWitness(2, 3);
    assertEquals(Lists.newArrayList("white/green"), c3.getReplacedWords());
  }

  @SuppressWarnings("boxing")
  public void testUseCase2() {
    String[] witnesses = new String[] { "the black cat", "THE BLACK CAT", "The black cat" };
    Colors colors = new Colors(witnesses);
    assertEquals(3, colors.numberOfColors());
    assertEquals(Sets.newHashSet(1, 2, 3), colors.getWitnessIndex(1).getWordCodes());
    assertEquals(Sets.newHashSet(1, 2, 3), colors.getWitnessIndex(2).getWordCodes());
    assertEquals(Sets.newHashSet(1, 2, 3), colors.getWitnessIndex(3).getWordCodes());
  }

  public void testThirdUseCasePeter() {
    String[] witnesses = new String[] { "He was agast so", "He was agast", "So he was agast" };
    Colors colors = new Colors(witnesses);
    Comparison c1 = colors.compareWitness(1, 2);
    assertEquals(Lists.newArrayList("so"), c1.getRemovedWords());
    Comparison c2 = colors.compareWitness(1, 3);
    assertTrue(c2.getRemovedWords().toString(), c2.getRemovedWords().isEmpty());
    assertTrue(c2.getAddedWords().toString(), c2.getAddedWords().isEmpty());
    List<Transposition> transpositions = c2.getTranspositions();
    Transposition transposition = transpositions.get(0);
    assertEquals(4, transposition.getTransposedWord());
    assertEquals(3, transposition.getTranspositionDistance());
  }

}
