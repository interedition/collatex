/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sd_editions.collatex.permutations;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.google.common.collect.Sets;
import com.sd_editions.collatex.Web.Alignment;
import com.sd_editions.collatex.Web.AlignmentView;

import eu.interedition.collatex.collation.CollateCore;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.input.builders.WitnessBuilder;

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
