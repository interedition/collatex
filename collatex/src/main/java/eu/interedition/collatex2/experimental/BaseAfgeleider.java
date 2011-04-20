package eu.interedition.collatex2.experimental;

import java.util.List;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;


public class BaseAfgeleider {

  public List<INormalizedToken> calculateAfgeleide(IWitness a, ListMultimap<INormalizedToken, INormalizedToken> matches) {
    List<INormalizedToken> matchingTokens = Lists.newArrayList();
    for (INormalizedToken token : a.getTokens()) {
      if (matches.containsValue(token)) {
        matchingTokens.add(token);
      }  
    }
    return matchingTokens;
  }
}
