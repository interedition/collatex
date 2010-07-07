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

import org.junit.Test;

import com.sd_editions.collatex.Block.Util;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.builders.WitnessBuilder;

/**
 * not really a test case: maybe refactor into the web application 
 */
public class MatchesView {

  private final WitnessBuilder builder = new WitnessBuilder();

  public String renderPermutations(Witness witnessA, Witness witnessB) {
    //    CollateCore collateCore = new CollateCore(witnessA, witnessB);
    //    List<MatchNonMatch> matchNonMatches = collateCore.doCompareWitnesses(witnessA, witnessB);
    //    collateCore.sortPermutationsByVariation(matchNonMatches);
    //
    StringBuilder result = new StringBuilder();
    //    result.append("Witness A: ").append(witnessA.sentence).append('\n');
    //    result.append("Witness B: ").append(witnessB.sentence).append('\n');
    //    for (MatchNonMatch matchNonMatch : matchNonMatches)
    //      result.append(renderPermutation(matchNonMatch));
    return result.toString();
  }

  @Test
  public void showPermutationsTara() {
    Witness[] witnesses = builder.buildWitnesses("I bought this glass, because it matches those dinner plates.", "I bought those glasses.");
    Util.p(renderPermutations(witnesses[0], witnesses[1]));
  }
}
