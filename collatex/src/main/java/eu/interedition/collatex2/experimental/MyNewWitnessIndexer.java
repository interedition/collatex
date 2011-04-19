package eu.interedition.collatex2.experimental;

import java.util.List;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class MyNewWitnessIndexer {
  public IWitnessIndex index(IWitness witness, ListMultimap<INormalizedToken, INormalizedToken> matches, IMatchResult result) {
    List<ITokenSequence> tokenSequences = Lists.newArrayList();
    // here we try to do the mapping
    // we lopen alle woorden uit de witness af
    // daarna kijken we in de matches map
    // drie mogelijkheden... geen match, enkele match, multiple match
    // we skippen de woorden met geen of een enkele match
    INormalizedToken previous =  new StartToken();
    for (INormalizedToken token : witness.getTokens()) {
      if (result.getUnsureTokens().contains(token)) {
        tokenSequences.add(new TokenSequence(previous, token));
      }
      int count = matches.keys().count(token);
      if (count == 1) {
        previous = token;
      }
    }
    return new NewWitnessIndex(tokenSequences);
  }
}
