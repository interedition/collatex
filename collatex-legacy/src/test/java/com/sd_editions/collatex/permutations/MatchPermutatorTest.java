package com.sd_editions.collatex.permutations;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.input.Word;

public class MatchPermutatorTest extends TestCase {

  private MatchPermutator permutator;
  private Set<Match> testSet;
  private Match<Word> match_1_2;
  private Match<Word> match_1_3;
  private Match<Word> match_2_1;
  private Match<Word> match_2_3;
  private Match<Word> match_3_4;
  private PMatch pmatch_1_2;
  private PMatch pmatch_1_3;
  private PMatch pmatch_2_1;
  private PMatch pmatch_2_3;
  private PMatch pmatch_3_4;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    testSet = Sets.newLinkedHashSet();
    String witnessId1 = "A";
    String witnessId2 = "B";
    match_1_2 = new Match<Word>(new Word(witnessId1, "een", 1), new Word(witnessId2, "een", 2));
    match_1_3 = new Match<Word>(new Word(witnessId1, "een", 1), new Word(witnessId2, "tween", 3));
    match_2_1 = new Match<Word>(new Word(witnessId1, "twee", 2), new Word(witnessId2, "twee", 1));
    match_2_3 = new Match<Word>(new Word(witnessId1, "twee", 2), new Word(witnessId2, "tween", 3));
    match_3_4 = new Match<Word>(new Word(witnessId1, "drie", 3), new Word(witnessId2, "drie", 4));
    pmatch_1_2 = new PMatch(match_1_2);
    pmatch_1_3 = new PMatch(match_1_3);
    pmatch_2_1 = new PMatch(match_2_1);
    pmatch_2_3 = new PMatch(match_2_3);
    pmatch_3_4 = new PMatch(match_3_4);
    testSet.add(match_1_2);
    testSet.add(match_1_3);
    testSet.add(match_2_1);
    testSet.add(match_2_3);
    testSet.add(match_3_4);
    permutator = new MatchPermutator(testSet);
  }

  public void testFindAlternatives1() {
    List<PMatch> pmatches = Lists.newArrayList(pmatch_1_2, pmatch_1_3, pmatch_2_1, pmatch_2_3, pmatch_3_4);
    ArrayList<PMatch> alternatives = Lists.newArrayList(permutator.findAlternatives(pmatches, pmatch_1_3));
    ArrayList<PMatch> expected = Lists.newArrayList(pmatch_1_2, pmatch_1_3, pmatch_2_3);
    assertEquals(expected.size(), alternatives.size());
    assertEquals(expected, alternatives);
  }

  public void testFindAlternatives2() {
    List<PMatch> pmatches = Lists.newArrayList(pmatch_1_2, pmatch_1_3, pmatch_2_1, pmatch_2_3, pmatch_3_4);
    ArrayList<PMatch> alternatives = Lists.newArrayList(permutator.findAlternatives(pmatches, pmatch_1_2));
    ArrayList<PMatch> expected = Lists.newArrayList(pmatch_1_2, pmatch_1_3);
    assertEquals(expected.size(), alternatives.size());
    assertEquals(expected, alternatives);
  }

  public void testFixCell1() {
    List<PMatch> pmatches = Lists.newArrayList(pmatch_1_2, pmatch_1_3, pmatch_2_1, pmatch_2_3, pmatch_3_4);
    List<PMatch> expected = Lists.newArrayList(pmatch_1_3, pmatch_2_1, pmatch_3_4);
    ArrayList<PMatch> fixCell = Lists.newArrayList(permutator.fixPMatch(pmatches, pmatch_1_3));
    assertEquals(expected.size(), fixCell.size());
    //    assertEquals(expected, fixCell);
  }

  public void testFixCell2() {
    List<PMatch> pmatches = Lists.newArrayList(pmatch_1_2, pmatch_1_3, pmatch_3_4);
    PMatch fpmatch_1_2 = pmatch_1_2.copy();
    fpmatch_1_2.fix();
    List<PMatch> expected = Lists.newArrayList(fpmatch_1_2, pmatch_3_4);
    ArrayList<PMatch> fixCell = Lists.newArrayList(permutator.fixPMatch(pmatches, pmatch_1_2));
    assertEquals(expected.size(), fixCell.size());
    //    assertEquals(expected, fixCell);
  }

}
