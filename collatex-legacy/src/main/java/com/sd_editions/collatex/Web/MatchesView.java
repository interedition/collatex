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

package com.sd_editions.collatex.Web;

import java.util.List;

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.alignment.Gap;
import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.MatchSequence;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class MatchesView {

  private final WitnessBuilder builder = new WitnessBuilder();

  public String renderPermutations(Segment witnessA, Segment witnessB) {
    //    CollateCore collateCore = new CollateCore(witnessA, witnessB);
    //    List<MatchNonMatch> matchNonMatches = collateCore.doCompareWitnesses(witnessA, witnessB);
    //    collateCore.sortPermutationsByVariation(matchNonMatches);

    StringBuilder result = new StringBuilder();
    //    result.append("Witness A: ").append(witnessA.sentence).append('\n');
    //    result.append("Witness B: ").append(witnessB.sentence).append('\n');
    //    for (MatchNonMatch matchNonMatch : matchNonMatches)
    //      result.append(renderPermutation(matchNonMatch));
    return result.toString();
  }

  public String renderPermutation(Alignment<Word> matchNonMatch) {
    StringBuilder result = new StringBuilder();

    result.append("<span class=\"variance\" style=\"display:none\">Permutation: ").append(matchNonMatch.getMatches()).append('\n');
    List<MatchSequence<Word>> sequencesForBase = matchNonMatch.getMatchSequencesOrderedForWitnessA();
    result.append("<ul>");
    for (MatchSequence matchSequence : sequencesForBase) {
      result.append("<li>MatchSequence: ").append(matchSequence);
      List<Match> matches = matchSequence.getMatches();
      result.append("<ul>");
      for (Match match : matches) {
        result.append("<li>Match: ").append(match);
        result.append(": ").append(match.getBaseWord()).append(" -> ").append(match.getWitnessWord());
        result.append(", word distance: ").append(match.wordDistance);
        result.append("</li>");
      }
      result.append("</ul>");
      result.append("</li>");
    }

    for (Gap nonMatch : matchNonMatch.getGaps()) {
      result.append("<li>Non-Match: ").append(nonMatch.getPhraseA()).append(" ~> ").append(nonMatch.getPhraseB());
      result.append("</li>");
    }
    result.append("</ul>");
    result.append("Summary: ").append(sequencesForBase.size()).append(" match sequences,\t");
    result.append(matchNonMatch.getGaps().size()).append(" non-matches,\t");
    result.append(matchNonMatch.getWordDistanceSum()).append(" summarized word distance,\t");
    result.append(matchNonMatch.getVariationMeasure()).append(" variance\n");
    result.append("</span>");
    result.append("<br/>");
    return result.toString();
  }
}
