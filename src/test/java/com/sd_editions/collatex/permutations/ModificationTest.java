package com.sd_editions.collatex.permutations;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sd_editions.collatex.permutations.collate.Addition;

public class ModificationTest {
  @Test
  public void testOutput() {
    Addition addition = new Addition(1, new Phrase(new Witness("some addition its longer than that"), 1, 2));
    String xml = addition.toXML();
    assertEquals("<addition position=\"1\">some addition</addition>", xml);
  }
}
