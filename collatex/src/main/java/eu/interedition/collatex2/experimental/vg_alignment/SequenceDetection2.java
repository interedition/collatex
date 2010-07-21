package eu.interedition.collatex2.experimental.vg_alignment;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.matching.PhraseMatch;
import eu.interedition.collatex2.input.Phrase;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.ITokenMatch;

public class SequenceDetection2 {

  private final List<ITokenMatch> tokenMatches;

  public SequenceDetection2(List<ITokenMatch> tokenMatches) {
    this.tokenMatches = tokenMatches;
  }

  //TODO: this implementation is too simple!
  //TODO: transpositions are not yet handeled!
  public List<IMatch2> chainTokenMatches() {
    Map<ITokenMatch, ITokenMatch> previousMatchMapA = buildPreviousMatchMap();
    List<INormalizedToken> tokensA = null;
    List<INormalizedToken> tokensB = null;
    List<IMatch2> matches = Lists.newArrayList();
    for (ITokenMatch tokenMatch : tokenMatches) {
      ITokenMatch previous = previousMatchMapA.get(tokenMatch);
      if (previous == null || tokenMatch.getTokenA().getPosition() - previous.getTokenA().getPosition() != 1) {
        // start a new sequence;
        createAndAddChainedMatch(tokensA, tokensB, matches);
        // clear buffer
        tokensA = Lists.newArrayList();
        tokensB = Lists.newArrayList();
      }
      INormalizedToken tokenA = tokenMatch.getTokenA();
      INormalizedToken tokenB = tokenMatch.getTokenB();
      tokensA.add(tokenA);
      tokensB.add(tokenB);
    }
    createAndAddChainedMatch(tokensA, tokensB, matches);
    return matches;
  }

  private void createAndAddChainedMatch(List<INormalizedToken> tokensA, List<INormalizedToken> tokensB, List<IMatch2> matches) {
    // save current state if necessary
    if (tokensA != null && !tokensA.isEmpty()) {
      IPhrase phraseA = new Phrase(tokensA);
      IPhrase phraseB = new Phrase(tokensB);
      IMatch2 match = new PhraseMatch(phraseA, phraseB);
      matches.add(match);
    }
  }
  
  private Map<ITokenMatch, ITokenMatch> buildPreviousMatchMap() {
    final Map<ITokenMatch, ITokenMatch> previousMatches = Maps.newHashMap();
    ITokenMatch previousMatch = null;
    for (final ITokenMatch tokenMatch : tokenMatches) {
      previousMatches.put(tokenMatch, previousMatch);
      previousMatch = tokenMatch;
    }
    return previousMatches;
  }


}
