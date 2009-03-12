package com.sd_editions.collatex.permutations;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.permutations.collate.Addition;

public class ModificationTest {
  @Test
  public void testOutput() {
    Addition addition = new Addition(1, new Phrase(new Witness("some addition its longer than that"), 1, 2));
    List<Modification> modificationsList = Lists.newArrayList((Modification) addition);
    Modifications modifications = new Modifications(modificationsList, null, null);
    String xml = modifications.toXML();
    assertEquals("<witnesssegment><addition>some addition</addition> its longer than that</witnesssegment>", xml);
  }
}
