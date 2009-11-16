package eu.interedition.collatex.match;

import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.UnfixedAlignment;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.match.worddistance.NormalizedLevenshtein;
import eu.interedition.collatex.match.worddistance.WordDistance;

////////////////////////////////////////
// 1. Tokenization
// 2. Regularization
// 3. Segmentation
// 4. Matching
// This class does the Matching!
//
public class Matcher {

  // NOTE: maybe rename UnfixedAlignment back to Matches?
  public static UnfixedAlignment<Word> match(final Segment a, final Segment b) {
    final Set<Match<Word>> allMatches = findMatches(a, b, new NormalizedLevenshtein());

    final UnfixedAlignment<Word> unfixedAlignment = separateAllMatchesIntoFixedAndUnfixedMatches(allMatches);
    return unfixedAlignment;
  }

  // NOTE: this code is specific for Segments/Words!
  static Set<Match<Word>> findMatches(final Segment base, final Segment witness, final WordDistance distanceMeasure) {
    final Set<Match<Word>> matchSet = Sets.newLinkedHashSet();
    for (final Word baseWord : base.getWords()) {
      for (final Word witnessWord : witness.getWords()) {
        if (baseWord.normalized.equals(witnessWord.normalized)) {
          matchSet.add(new Match<Word>(baseWord, witnessWord));
        } else {
          final float editDistance = distanceMeasure.distance(baseWord.normalized, witnessWord.normalized);
          if (editDistance < 0.5) matchSet.add(new Match<Word>(baseWord, witnessWord, editDistance));
        }
      }
    }
    return matchSet;
  }

  private static UnfixedAlignment<Word> separateAllMatchesIntoFixedAndUnfixedMatches(final Set<Match<Word>> allMatches) {
    // Note: this code is not the simplest thing that 
    // could possibly work!
    final Set<Match<Word>> exactMatches = Sets.newLinkedHashSet();
    for (final Match<Word> match : allMatches) {
      final Iterable<Match<Word>> alternatives = findAlternatives(allMatches, match);
      if (!alternatives.iterator().hasNext()) {
        exactMatches.add(match);
      }
    }

    final Set<Match<Word>> unfixedMatches = Sets.newLinkedHashSet(allMatches);
    unfixedMatches.removeAll(exactMatches);

    final UnfixedAlignment<Word> unfixedAlignment = new UnfixedAlignment<Word>(exactMatches, unfixedMatches);
    return unfixedAlignment;
  }

  static Iterable<Match<Word>> findAlternatives(final Iterable<Match<Word>> pmatches, final Match<Word> pmatch) {
    final Predicate<Match<Word>> unfixedAlternativeToGivenPMatch = new Predicate<Match<Word>>() {
      public boolean apply(final Match<Word> pm) {
        return pm != pmatch && (pm.getBaseWord().equals(pmatch.getBaseWord()) || pm.getWitnessWord().equals(pmatch.getWitnessWord()));
      }
    };
    return Iterables.filter(pmatches, unfixedAlternativeToGivenPMatch);
  }

}
