package com.sd_editions.collatex.output;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.permutations.Modification;
import com.sd_editions.collatex.permutations.Modifications;
import com.sd_editions.collatex.permutations.Phrase;
import com.sd_editions.collatex.permutations.WitnessBuilder;
import com.sd_editions.collatex.permutations.collate.Addition;
import com.sd_editions.collatex.permutations.collate.Omission;

public class XMLAlignmentViewTest {

  private static WitnessBuilder builder;

  @BeforeClass
  public static void setUp() throws Exception {
    builder = new WitnessBuilder();
  }

  private XMLAlignmentView setupAlignmentView(Modification modification) {
    List<Modification> modificationsList = Lists.newArrayList(modification);
    Modifications modifications = new Modifications(modificationsList, null, null);
    XMLAlignmentView alignmentView = new XMLAlignmentView(modifications);
    return alignmentView;
  }

  @Test
  public void testModificationsView() {
    Addition addition = new Addition(1, new Phrase(builder.build("some addition its longer than that"), 1, 2));
    XMLAlignmentView alignmentView = setupAlignmentView(addition);

    String result = alignmentView.modificationsView(-1);
    String expected = "<modifications><addition position=\"1\">some addition</addition></modifications>";
    assertEquals(expected, result);

  }

  @Test
  public void testModificationsViewOmissions() {
    Omission omission = new Omission(new Phrase(builder.build("some deletion has occurred"), 2, 2));
    XMLAlignmentView alignmentView = setupAlignmentView(omission);

    String result = alignmentView.modificationsView(-1);
    String expected = "<modifications><omission position=\"2\">deletion</omission></modifications>";
    assertEquals(expected, result);
  }
}
