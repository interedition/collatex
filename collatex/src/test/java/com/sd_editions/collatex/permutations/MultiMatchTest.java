package com.sd_editions.collatex.permutations;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import eu.interedition.collatex.input.Word;

public class MultiMatchTest extends TestCase {
  private final String witnessId1 = "A";
  private final String witnessId2 = "B";

  @Test
  public void testMultiMatch() {
    /*
     * A: Hello, Hello
     * B: Hello oops
     */
    Word hello1in1 = new Word(witnessId1, "Hello,", 1);
    Word hello2in1 = new Word(witnessId1, "Hello", 2);
    Word hello1in2 = new Word(witnessId2, "Hello", 1);
    Word oops1in1 = new Word(witnessId2, "oops", 2);
    MultiMatch mm = new MultiMatch(hello1in1, hello1in2);
    mm.addMatchingWord(hello2in1);
    mm.addMatchingWord(oops1in1);
    assertEquals("hello", mm.name);
    List<Word> helloIn1 = mm.getOccurancesInWitness(witnessId1);
    List<Word> helloIn2 = mm.getOccurancesInWitness(witnessId2);
    assertEquals(2, helloIn1.size());
    assertEquals(hello1in1, helloIn1.get(0));
    assertEquals(hello2in1, helloIn1.get(1));
    assertEquals(1, helloIn2.size());
    assertEquals(hello1in2, helloIn2.get(0));
  }
}
