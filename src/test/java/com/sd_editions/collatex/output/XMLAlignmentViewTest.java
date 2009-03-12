package com.sd_editions.collatex.output;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.permutations.Modification;
import com.sd_editions.collatex.permutations.Modifications;
import com.sd_editions.collatex.permutations.Phrase;
import com.sd_editions.collatex.permutations.Witness;
import com.sd_editions.collatex.permutations.collate.Addition;
import com.sd_editions.collatex.permutations.collate.Removal;

public class XMLAlignmentViewTest {

  @Test
  public void testModificationsView() {
    Addition addition = new Addition(1, new Phrase(new Witness("some addition its longer than that"), 1, 2));
    List<Modification> modificationsList = Lists.newArrayList((Modification) addition);
    Modifications modifications = new Modifications(modificationsList, null, null);
    XMLAlignmentView alignmentView = new XMLAlignmentView(modifications);
    String result = alignmentView.modificationsView(-1);

    String expected = "<modifications><addition position=\"1\">some addition</addition></modifications>";
    assertEquals(expected, result);

  }

  @Test
  public void testModificationsViewOmissions() {
    Removal removal = new Removal(new Phrase(new Witness("some deletion has occurred"), 2, 2));
    List<Modification> modificationList = Lists.newArrayList((Modification) removal);
    Modifications modifications = new Modifications(modificationList, null, null);
    XMLAlignmentView alignmentView = new XMLAlignmentView(modifications);
    String result = alignmentView.modificationsView(-1);
    String expected = "<modifications><omission position=\"2\">deletion</omission></modifications>";
    assertEquals(expected, result);
  }
}
