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

package eu.interedition.collatex.matching;

import java.util.Collection;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.UnfixedAlignment;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.input.builders.WitnessBuilder;
import eu.interedition.collatex.match.Matcher;

// TODO rename to unfixed alignment test!
public class PossibleMatchesTest {
  private WitnessBuilder builder;

  @Before
  public void setup() {
    builder = new WitnessBuilder();
  }

  @Test
  public void testExactMatchesAreFixed() {
    Witness a = builder.build("zijn hond liep aan zijn hand");
    Witness b = builder.build("op zijn pad liep zijn hond aan zijn hand");
    UnfixedAlignment unfixedAlignment = Matcher.match(a.getFirstSegment(), b.getFirstSegment());
    Set<Match> exactMatches = unfixedAlignment.getFixedMatches();
    String expected = "[(3->4), (4->7)]";
    Assert.assertEquals(expected, exactMatches.toString());
  }

  @Test
  public void testPossibleMatchesAsAMap() {
    Witness a = builder.build("zijn hond liep aan zijn hand");
    Witness b = builder.build("op zijn pad liep zijn hond aan zijn hand");
    UnfixedAlignment unfixedAlignment = Matcher.match(a.getFirstSegment(), b.getFirstSegment());
    Word zijn = a.getFirstSegment().getElementOnWordPosition(1);
    Collection<Match> linked = unfixedAlignment.getMatchesThatLinkFrom(zijn);
    Assert.assertEquals("[(1->2), (1->5), (1->8)]", linked.toString());

    Word zijnB = b.getFirstSegment().getElementOnWordPosition(2);
    Collection<Match> links = unfixedAlignment.getMatchesThatLinkTo(zijnB);
    Assert.assertEquals("[(1->2), (5->2)]", links.toString());
  }
}
