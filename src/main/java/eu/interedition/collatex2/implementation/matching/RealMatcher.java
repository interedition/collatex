package eu.interedition.collatex2.implementation.matching;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.implementation.matching.worddistance.WordDistance;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;

public class RealMatcher {

  // NOTE; this code works directly on IWitness
  // There should be an indexing phase and then this should
  // work on indexes!
  public static Set<IMatch> findMatches(final IWitness base, final IWitness witness, final WordDistance distanceMeasure) {
    final Set<IMatch> matchSet = Sets.newLinkedHashSet();
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

  public static Set<IMatch> findMatchesWithIndex(final IWitness base, final IWitness witness, final WordDistance distanceMeasure) {
    final Set<IMatch> matchSet = Sets.newLinkedHashSet();
    final Map<String, IWitnessIndex> witnessIndexMap = Factory.createWitnessIndexMap(base, witness);
    final IWitnessIndex baseIndex = witnessIndexMap.get(base.getSigil());
    final IWitnessIndex witnessIndex = witnessIndexMap.get(witness.getSigil());
    for (final IPhrase basePhrase : baseIndex.getPhrases()) {
      for (final IPhrase witnessPhrase : witnessIndex.getPhrases()) {
        if (basePhrase.getNormalized().equals(witnessPhrase.getNormalized())) {
          matchSet.add(new Match(basePhrase, witnessPhrase));
        } else {
          final float editDistance = distanceMeasure.distance(basePhrase.getNormalized(), witnessPhrase.getNormalized());
          if (editDistance < 0.5) matchSet.add(Factory.createMatch(basePhrase, witnessPhrase, editDistance));
        }
      }
    }
    // en nu opschonen
    return matchSet;
  }
}
