package eu.interedition.collatex2.implementation.matching;

import java.util.Set;

import com.google.common.collect.Sets;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.implementation.matching.worddistance.WordDistance;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhraseMatch;
import eu.interedition.collatex2.interfaces.IWitness;

public class RealMatcher {

  // NOTE; this code works directly on IWitness
  // There should be an indexing phase and then this should
  // work on indexes!
  public static Set<IPhraseMatch> findMatches(final IWitness base, final IWitness witness, final WordDistance distanceMeasure) {
    final Set<IPhraseMatch> matchSet = Sets.newLinkedHashSet();
    for (final INormalizedToken baseWord : base.getTokens()) {
      for (final INormalizedToken witnessWord : witness.getTokens()) {
        if (baseWord.getNormalized().equals(witnessWord.getNormalized())) {
          matchSet.add(Factory.createMatch(baseWord, witnessWord));
        } else {
          // skip the near matches for now
          //          final float editDistance = distanceMeasure.distance(baseWord.getNormalized(), witnessWord.getNormalized());
          //          if (editDistance < 0.5) matchSet.add(Factory.createMatch(baseWord, witnessWord, editDistance));
        }
      }
    }
    return matchSet;
  }
}
