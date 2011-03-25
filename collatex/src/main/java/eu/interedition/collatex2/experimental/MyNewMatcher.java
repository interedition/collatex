package eu.interedition.collatex2.experimental;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.vg_alignment.TokenMatch;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IWitness;

public class MyNewMatcher {

  public List<ITokenMatch> match(IWitness a, IWitness b) {
    List<ITokenMatch> matches = Lists.newArrayList();
    for (INormalizedToken tokenA : a.getTokens()) {
      for (INormalizedToken tokenB : b.getTokens()) {
        if (tokenA.getNormalized().equals(tokenB.getNormalized())) {
          TokenMatch match = new TokenMatch(tokenA, tokenB);
          matches.add(match);
        }
      }
    }
    return matches;
  }

}
