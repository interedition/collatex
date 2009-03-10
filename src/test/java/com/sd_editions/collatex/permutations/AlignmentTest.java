package com.sd_editions.collatex.permutations;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.google.common.collect.Sets;
import com.sd_editions.collatex.Web.Alignment;
import com.sd_editions.collatex.Web.AlignmentView;
import com.sd_editions.collatex.permutations.CollateCore;
import com.sd_editions.collatex.permutations.Word;

public class AlignmentTest extends TestCase {
  public void testAlignmentSimply() {
    CollateCore colors = new CollateCore("a", "b");
    AlignmentView view = new AlignmentView(colors);
    Map<Word, Alignment> determineAlignment = view.determineAlignment();
    Collection<Alignment> alignments = determineAlignment.values();
    assertEquals(0, alignments.size());
  }

  public void testAlignmentOne() {
    CollateCore colors = new CollateCore("a", "a");
    AlignmentView view = new AlignmentView(colors);
    Map<Word, Alignment> determineAlignment = view.determineAlignment();
    Set<Alignment> alignments = Sets.newLinkedHashSet(determineAlignment.values());
    assertEquals(1, alignments.size());
  }

  public void testAlignmentTwo() {
    CollateCore colors = new CollateCore("a b", "a b");
    AlignmentView view = new AlignmentView(colors);
    Map<Word, Alignment> determineAlignment = view.determineAlignment();
    Set<Alignment> alignments = Sets.newLinkedHashSet(determineAlignment.values());
    assertEquals(2, alignments.size());
  }

  public void testAlignmentTwoAsHTML() {
    CollateCore colors = new CollateCore("a b c", "a b c");
    AlignmentView view = new AlignmentView(colors);
    String bla = view.toHtml();
    assertEquals(
        "<span class=\"color1\" title=\"color1\">a</span> <span class=\"color2\" title=\"color2\">b</span> <span class=\"color3\" title=\"color3\">c</span><br/><span class=\"color1\" title=\"color1\">a</span> <span class=\"color2\" title=\"color2\">b</span> <span class=\"color3\" title=\"color3\">c</span><br/>",
        bla);
  }

  public void testAlignmentMultipleWitnesses() {
    CollateCore colors = new CollateCore("a", "a b", "a b c", "a b c d");
    AlignmentView view = new AlignmentView(colors);
    Map<Word, Alignment> determineAlignment = view.determineAlignment();
    Set<Alignment> alignments = Sets.newLinkedHashSet(determineAlignment.values());
    assertEquals(3, alignments.size());
  }
}
