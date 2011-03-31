package eu.interedition.collatex2.experimental;

import java.util.List;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.containers.witness.Witness;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;


public class WitnessAfgeleide {

  public IWitness calculateAfgeleide(IWitness b, ListMultimap<INormalizedToken, INormalizedToken> matches) {
    List<INormalizedToken> matchingTokens = Lists.newArrayList();
    for (INormalizedToken token : b.getTokens()) {
      if (matches.containsValue(token)) {
        matchingTokens.add(token);
      }  
    }
    final Witness afgeleide = new Witness("afgeleide", matchingTokens);
    return afgeleide;
  }

}
