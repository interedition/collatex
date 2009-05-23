package com.sd_editions.collatex.permutations;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.google.common.collect.Sets;
import com.sd_editions.collatex.Web.Alignment;
import com.sd_editions.collatex.Web.AlignmentView;

import eu.interedition.collatex.collation.CollateCore;
import eu.interedition.collatex.input.WitnessBuilder;
import eu.interedition.collatex.input.Word;

public class AlignmentTest extends TestCase {

  private WitnessBuilder builder;

  @Override
  protected void setUp() throws Exception {
    builder = new WitnessBuilder();
    super.setUp();
  }

  public void testAlignmentSimply() {
    CollateCore colors = new CollateCore(builder.build("a"), builder.build("b"));
    AlignmentView view = new AlignmentView(colors);
    Map<Word, Alignment> determineAlignment = view.determineAlignment();
    Collection<Alignment> alignments = determineAlignment.values();
    assertEquals(0, alignments.size());
  }

  public void testAlignmentOne() {
    CollateCore colors = new CollateCore(builder.build("a"), builder.build("a"));
    AlignmentView view = new AlignmentView(colors);
    Map<Word, Alignment> determineAlignment = view.determineAlignment();
    Set<Alignment> alignments = Sets.newLinkedHashSet(determineAlignment.values());
    assertEquals(1, alignments.size());
  }

  public void testAlignmentTwo() {
    CollateCore colors = new CollateCore(builder.build("a b"), builder.build("a b"));
    AlignmentView view = new AlignmentView(colors);
    Map<Word, Alignment> determineAlignment = view.determineAlignment();
    Set<Alignment> alignments = Sets.newLinkedHashSet(determineAlignment.values());
    assertEquals(2, alignments.size());
  }

  public void testAlignmentTwoAsHTML() {
    CollateCore colors = new CollateCore(builder.build("a b c"), builder.build("a b c"));
    AlignmentView view = new AlignmentView(colors);
    String bla = view.toHtml();
    assertEquals(
        "<span class=\"color1\" title=\"color1\">a</span> <span class=\"color2\" title=\"color2\">b</span> <span class=\"color3\" title=\"color3\">c</span><br/><span class=\"color1\" title=\"color1\">a</span> <span class=\"color2\" title=\"color2\">b</span> <span class=\"color3\" title=\"color3\">c</span><br/>",
        bla);
  }

  public void testAlignmentMultipleWitnesses() {
    CollateCore colors = new CollateCore(builder.build("a", "a b"), builder.build("a b c"), builder.build("a b c d"));
    AlignmentView view = new AlignmentView(colors);
    Map<Word, Alignment> determineAlignment = view.determineAlignment();
    Set<Alignment> alignments = Sets.newLinkedHashSet(determineAlignment.values());
    assertEquals(3, alignments.size());
  }
}
