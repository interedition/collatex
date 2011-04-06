package eu.interedition.collatex2.experimental;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class MyNewMatcher {

  public ListMultimap<INormalizedToken, INormalizedToken> match(IWitness a, IWitness b) {
    ListMultimap<INormalizedToken, INormalizedToken> matches = ArrayListMultimap.create();
    for (INormalizedToken tokenA : a.getTokens()) {
      for (INormalizedToken tokenB : b.getTokens()) {
        if (tokenA.getNormalized().equals(tokenB.getNormalized())) {
          matches.put(tokenB, tokenA);
        }
      }
    }
    return matches;
  }

}
