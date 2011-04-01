package eu.interedition.collatex2.experimental;

import java.util.Map;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class MyNewAligner {

  public Map<INormalizedToken, INormalizedToken> align(IWitness a, IWitness b) {
    Map<INormalizedToken, INormalizedToken> alignedTokens = Maps.newLinkedHashMap();
    MyNewMatcher matcher = new MyNewMatcher();
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(a, b);
    for (INormalizedToken token: a.getTokens()) {
      if (matches.keys().count(token)==1) {
        alignedTokens.put(token, matches.get(token).get(0));
      }
    }
    return alignedTokens;
  }

}
