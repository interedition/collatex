package eu.interedition.collatex.general;

import java.util.Set;

import com.google.common.collect.Sets;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.match.worddistance.WordDistance;

public class RealMatcher {

  // NOTE: this code is specific for Segments/Words!
  // TODO generalize to IWitness etc
  public static Set<Match<Word>> findMatches(final Segment base, final Segment witness, final WordDistance distanceMeasure) {
    final Set<Match<Word>> matchSet = Sets.newLinkedHashSet();
    for (final Word baseWord : base.getWords()) {
      for (final Word witnessWord : witness.getWords()) {
        if (baseWord._normalized.equals(witnessWord._normalized)) {
          matchSet.add(new Match<Word>(baseWord, witnessWord));
        } else {
          final float editDistance = distanceMeasure.distance(baseWord._normalized, witnessWord._normalized);
          if (editDistance < 0.5) matchSet.add(new Match<Word>(baseWord, witnessWord, editDistance));
        }
      }
    }
    return matchSet;
  }
}
