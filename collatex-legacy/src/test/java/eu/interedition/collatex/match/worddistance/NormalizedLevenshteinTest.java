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

package eu.interedition.collatex.match.worddistance;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sd_editions.collatex.Block.Util;

public class NormalizedLevenshteinTest {

  private static final float THRESHOLD = 0.5f;

  @Test
  public void testThisThoseDistance() {
    NormalizedLevenshtein normalizedLevenshtein = new NormalizedLevenshtein();
    float thisThoseDistance = normalizedLevenshtein.distance("this", "those");
    Util.p("this" + " ~ " + "those" + ":" + thisThoseDistance);
    assertTrue("this ~ those > threshold: " + thisThoseDistance, thisThoseDistance < THRESHOLD);
  }

  @Test
  public void testSomethingElseDistance() {
    NormalizedLevenshtein normalizedLevenshtein = new NormalizedLevenshtein();
    float somethingElseDistance = normalizedLevenshtein.distance("something", "else");
    Util.p("something" + " ~ " + "else" + ":" + somethingElseDistance);
    assertTrue("something ~ else < threshold: " + somethingElseDistance, somethingElseDistance > THRESHOLD);
  }

}
