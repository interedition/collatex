package eu.interedition.collatex2.experimental.matching;

import java.util.Comparator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class MyNewMatcher {
  private final Comparator<INormalizedToken> matchingFunction;
  
  public MyNewMatcher() {
    this.matchingFunction = new EqualsMatcher();
  }
  
  public ListMultimap<INormalizedToken, INormalizedToken> match(IWitness a, IWitness b) {
    ListMultimap<INormalizedToken, INormalizedToken> matches = ArrayListMultimap.create();
    for (INormalizedToken tokenA : a.getTokens()) {
      for (INormalizedToken tokenB : b.getTokens()) {
        if (this.matchingFunction.compare(tokenA, tokenB)==1) {
          matches.put(tokenB, tokenA);
        }
      }
    }
    return matches;
  }
}
