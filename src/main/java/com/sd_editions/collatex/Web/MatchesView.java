package com.sd_editions.collatex.Web;

import java.util.List;

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.alignment.Gap;
import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.MatchSequence;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class MatchesView {

  private final WitnessBuilder builder = new WitnessBuilder();

  public String renderPermutations(Witness witnessA, Witness witnessB) {
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

  public String renderPermutation(Alignment matchNonMatch) {
    StringBuilder result = new StringBuilder();

    result.append("<span class=\"variance\" style=\"display:none\">Permutation: ").append(matchNonMatch.getMatches()).append('\n');
    List<MatchSequence> sequencesForBase = matchNonMatch.getMatchSequencesOrderedForWitnessA();
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
