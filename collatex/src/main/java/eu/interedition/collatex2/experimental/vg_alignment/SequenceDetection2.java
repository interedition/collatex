package eu.interedition.collatex2.experimental.vg_alignment;

import java.util.List;

import com.google.common.collect.Lists;

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

  //TODO: this implementation is far too simple!
  //Code from the other SequenceDetection class
  //needs to be ported over!
  public List<IMatch2> chainTokenMatches() {
    List<IMatch2> matches = Lists.newArrayList();
    for (ITokenMatch tokenMatch : tokenMatches) {
      INormalizedToken tokenA = tokenMatch.getTokenA();
      INormalizedToken tokenB = tokenMatch.getTokenB();
      List<INormalizedToken> tokensA = Lists.newArrayList(tokenA);
      List<INormalizedToken> tokensB = Lists.newArrayList(tokenB);
      IPhrase phraseA = new Phrase(tokensA);
      IPhrase phraseB = new Phrase(tokensB);
      IMatch2 match = new PhraseMatch(phraseA, phraseB);
      matches.add(match);
    }
    return matches;
  }

}
