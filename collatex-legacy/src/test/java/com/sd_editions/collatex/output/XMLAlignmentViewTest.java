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

package com.sd_editions.collatex.output;

import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.permutations.collate.Transposition;

import eu.interedition.collatex.experimental.ngrams.alignment.Modification;
import eu.interedition.collatex.input.builders.WitnessBuilder;
import eu.interedition.collatex.visualization.Modifications;

public class XMLAlignmentViewTest {

  private static WitnessBuilder builder;

  @BeforeClass
  public static void setUp() throws Exception {
    builder = new WitnessBuilder();
  }

  private XMLAlignmentView setupAlignmentView(final Modification modification) {
    final List<Modification> modificationsList = Lists.newArrayList(modification);
    final List<Transposition> transpositionList = Lists.newArrayList();
    final Modifications modifications = new Modifications(modificationsList, transpositionList, null);
    final XMLAlignmentView alignmentView = new XMLAlignmentView(modifications);
    return alignmentView;
  }

  // TODO REWRITE!
  //  @Test
  //  public void testModificationsView() {
  //    Witness witness = builder.build("some addition its longer than that");
  //    Segment segment = witness.getFirstSegment();
  //    Word next = segment.getElementOnWordPosition(3);
  //    Word previous = null;
  //    Addition addition = new Addition(1, new BaseContainerPart(segment, 2, 1, 2));
  //    XMLAlignmentView alignmentView = setupAlignmentView(addition);
  //
  //    String result = alignmentView.modificationsView(-1);
  //    String expected = "<modifications><addition position=\"1\">some addition</addition></modifications>";
  //    assertEquals(expected, result);
  //
  //  }

  //TODO REWRITE!
  //  @Test
  //  public void testModificationsViewOmissions() {
  //    final Witness witness = builder.build("some deletion has occurred");
  //    final Segment segment = witness.getFirstSegment();
  //    final Word next = segment.getElementOnWordPosition(3);
  //    final Word previous = segment.getElementOnWordPosition(1);
  //    final Omission omission = new Omission(new BaseContainerPart(segment, 1, 2, 2));
  //    final XMLAlignmentView alignmentView = setupAlignmentView(omission);
  //
  //    final String result = alignmentView.modificationsView(-1);
  //    final String expected = "<modifications><omission position=\"2\">deletion</omission></modifications>";
  //    assertEquals(expected, result);
  //  }

  @Test
  // dummy test for mvn's benefit, doesn't like testfiles without tests
  public void test() {
    Assert.assertTrue(true);
  }
}
