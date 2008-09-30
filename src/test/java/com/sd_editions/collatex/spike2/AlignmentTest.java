package com.sd_editions.collatex.spike2;

import java.util.List;

import junit.framework.TestCase;

import com.sd_editions.collatex.Web.Alignment;
import com.sd_editions.collatex.Web.AlignmentView;
import com.sd_editions.collatex.spike2.Colors;

public class AlignmentTest extends TestCase {
  public void testAlignmentSimply() {
    Colors colors = new Colors("a", "b");
    AlignmentView view = new AlignmentView(colors);
    List<Alignment> alignments = view.determineAlignment();
    assertEquals(0, alignments.size());
  }

  public void testAlignmentOne() {
    Colors colors = new Colors("a", "a");
    AlignmentView view = new AlignmentView(colors);
    List<Alignment> alignments = view.determineAlignment();
    assertEquals(1, alignments.size());
  }

  public void testAlignmentTwo() {
    Colors colors = new Colors("a b", "a b");
    AlignmentView view = new AlignmentView(colors);
    List<Alignment> alignments = view.determineAlignment();
    assertEquals(2, alignments.size());
  }

  //  public void testAlignmentDS() {
  //    Colors colors = new Colors("a a", "a b a", "a b c");
  //    AlignmentView view = new AlignmentView(colors);
  //    List<Alignment> alignment = view.determineAlignment();
  //    assertEquals(2, alignment.size());
  //    //    List<FWitness> determineAlignment = view.determineAlignment();
  //    //    FWitness firstWitness = determineAlignment.get(0);
  //    //    List<FWord> words = firstWitness.getWords();
  //
  //    // oh nee andere expectations..
  //
  //  }
}
