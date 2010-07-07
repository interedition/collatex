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

package eu.interedition.collatex.alignment.functions;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.alignment.Gap;
import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.MatchSequence;
import eu.interedition.collatex.alignment.UnfixedAlignment;
import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.WitnessSegmentPhrases;

public class NewAligner {

  public static UnfixedAlignment<Phrase> permutate(final UnfixedAlignment<Phrase> matches, final WitnessSegmentPhrases a, final WitnessSegmentPhrases b) {
    if (!matches.hasUnfixedWords()) {
      return matches;
    }
    final Set<Phrase> unfixed = matches.getUnfixedElementsInWitness();
    if (unfixed.size() != 1) {
      throw new RuntimeException("There is more than one unfixed match! Not implemented yet!");
    }

    final Phrase topermutate = unfixed.iterator().next();
    System.out.println(topermutate);
    final Collection<Match<Phrase>> matchesThatLinkTo = matches.getMatchesThatLinkTo(topermutate);
    if (matchesThatLinkTo.size() != 2) {
      throw new RuntimeException("There are more than two alternatives!");
    }
    System.out.println(matchesThatLinkTo);
    final List<UnfixedAlignment<Phrase>> variants = Aligner.getAlignmentsForUnfixedMatches(matches, matchesThatLinkTo);
    final UnfixedAlignment<Phrase> result = selectBestPossibleAlignment(a, b, variants);
    //    final Set<Phrase> unfixedWords = matches.getUnfixedWords();
    //    if (unfixedWords.size() != 2) {
    //      throw new RuntimeException("NOT IMPLEMENTED YET!");
    //    }
    //    Phrase nextBase = unfixedWords.iterator().next();
    //    Collection<Match<Phrase>> matchesThatLinkTo = matches.getMatchesThatLinkTo(nextBase);
    //    if (matchesThatLinkTo.size()!=1) {
    //      throw new RuntimeException("NOT implemented yet2!");
    //    }

    return result;
  }

  // TODO naming here is not cool!
  private static UnfixedAlignment<Phrase> selectBestPossibleAlignment(final WitnessSegmentPhrases a, final WitnessSegmentPhrases b, final List<UnfixedAlignment<Phrase>> alignments) {
    UnfixedAlignment<Phrase> bestAlignment = null;
    Alignment<Phrase> bestCollation = null;

    // TODO add test for lowest number of matchsequences (transpositions)
    // NOTE: this can be done in a nicer way with the min function!
    for (final UnfixedAlignment<Phrase> alignment : alignments) {
      final Alignment<Phrase> collation = Alignment.createPhraseAlignment(alignment, a, b);
      final List<Gap<Phrase>> nonMatches = collation.getGaps();
      final List<MatchSequence<Phrase>> matchSequences = collation.getMatchSequences();
      //      System.out.println(alignment.getFixedMatches().toString() + ":" + matchSequences.size() + ":" + nonMatches.size());
      if (bestAlignment == null || bestCollation == null || matchSequences.size() < bestCollation.getMatchSequences().size() || nonMatches.size() < bestCollation.getGaps().size()) {
        bestAlignment = alignment;
        bestCollation = Alignment.createPhraseAlignment(bestAlignment, a, b);
      }
    }
    if (bestAlignment == null) throw new RuntimeException("Unexpected error!");
    return bestAlignment;
  }

}
