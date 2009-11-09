package com.sd_editions.collatex.output;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.permutations.collate.Addition;
import com.sd_editions.collatex.permutations.collate.Omission;
import com.sd_editions.collatex.permutations.collate.Transposition;

import eu.interedition.collatex.input.BaseContainerPart;
import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.input.builders.WitnessBuilder;
import eu.interedition.collatex.visualization.Modification;
import eu.interedition.collatex.visualization.Modifications;

public class XMLAlignmentViewTest {

  private static WitnessBuilder builder;

  @BeforeClass
  public static void setUp() throws Exception {
    builder = new WitnessBuilder();
  }

  private XMLAlignmentView setupAlignmentView(Modification modification) {
    List<Modification> modificationsList = Lists.newArrayList(modification);
    List<Transposition> transpositionList = Lists.newArrayList();
    Modifications modifications = new Modifications(modificationsList, transpositionList, null);
    XMLAlignmentView alignmentView = new XMLAlignmentView(modifications);
    return alignmentView;
  }

  @Test
  public void testModificationsView() {
    Witness witness = builder.build("some addition its longer than that");
    Segment segment = witness.getFirstSegment();
    Word next = segment.getWordOnPosition(3);
    Word previous = null;
    Addition addition = new Addition(1, new BaseContainerPart(segment, 2, 1, 2, previous, next));
    XMLAlignmentView alignmentView = setupAlignmentView(addition);

    String result = alignmentView.modificationsView(-1);
    String expected = "<modifications><addition position=\"1\">some addition</addition></modifications>";
    assertEquals(expected, result);

  }

  @Test
  public void testModificationsViewOmissions() {
    Witness witness = builder.build("some deletion has occurred");
    Segment segment = witness.getFirstSegment();
    Word next = segment.getWordOnPosition(3);
    Word previous = segment.getWordOnPosition(1);
    Omission omission = new Omission(new BaseContainerPart(segment, 1, 2, 2, previous, next));
    XMLAlignmentView alignmentView = setupAlignmentView(omission);

    String result = alignmentView.modificationsView(-1);
    String expected = "<modifications><omission position=\"2\">deletion</omission></modifications>";
    assertEquals(expected, result);
  }
}
