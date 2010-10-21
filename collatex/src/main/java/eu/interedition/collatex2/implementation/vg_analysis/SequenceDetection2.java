package eu.interedition.collatex2.implementation.vg_analysis;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.tokenmatching.PhraseMatch;
import eu.interedition.collatex2.implementation.vg_alignment.IAlignment2;
import eu.interedition.collatex2.input.Phrase;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.ITokenMatch;

public class SequenceDetection2 {

  private final List<ITokenMatch> tokenMatches;

  public SequenceDetection2(List<ITokenMatch> tokenMatches) {
    this.tokenMatches = tokenMatches;
  }

  public SequenceDetection2(IAlignment2 alignment) {
    this.tokenMatches = alignment.getTokenMatches();
  }

  public List<ISequence> chainTokenMatches() {
    // prepare
    final List<ITokenMatch> tokenMatchesSortedForB = sortTokenMatchesForWitness();
    Map<ITokenMatch, ITokenMatch> previousMatchMapA = buildPreviousMatchMap();
    Map<ITokenMatch, ITokenMatch> previousMatchMapB = buildPreviousMatchMap(tokenMatchesSortedForB);
    // chain token matches
    List<INormalizedToken> tokensA = null;
    List<INormalizedToken> tokensB = null;
    List<ISequence> matches = Lists.newArrayList();
    for (ITokenMatch tokenMatch : tokenMatches) {
      ITokenMatch previousA = previousMatchMapA.get(tokenMatch);
      ITokenMatch previousB = previousMatchMapB.get(tokenMatch);
      // TODO: distance tokenB?
      if (previousA == null || previousA != previousB || tokenMatch.getTokenA().getPosition() - previousA.getTokenA().getPosition() != 1) {
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

  private List<ITokenMatch> sortTokenMatchesForWitness() {
    final List<ITokenMatch> matchesForWitness = Lists.newArrayList(tokenMatches);
    Collections.sort(matchesForWitness, SORT_MATCHES_ON_POSITION_WITNESS);
    return matchesForWitness;
  }
  
  final Comparator<ITokenMatch> SORT_MATCHES_ON_POSITION_WITNESS = new Comparator<ITokenMatch>() {
    @Override
    public int compare(final ITokenMatch o1, final ITokenMatch o2) {
      return o1.getTokenB().getPosition() - o2.getTokenB().getPosition();
    }
  };

  private void createAndAddChainedMatch(List<INormalizedToken> tokensA, List<INormalizedToken> tokensB, List<ISequence> matches) {
    // save current state if necessary
    if (tokensA != null && !tokensA.isEmpty()) {
      IPhrase phraseA = new Phrase(tokensA);
      IPhrase phraseB = new Phrase(tokensB);
      ISequence match = new PhraseMatch(phraseA, phraseB);
      matches.add(match);
    }
  }
  
  private Map<ITokenMatch, ITokenMatch> buildPreviousMatchMap() {
    return buildPreviousMatchMap(tokenMatches);
  }

  private Map<ITokenMatch, ITokenMatch> buildPreviousMatchMap(List<ITokenMatch> tokenMatches) {
    final Map<ITokenMatch, ITokenMatch> previousMatches = Maps.newHashMap();
    ITokenMatch previousMatch = null;
    for (final ITokenMatch tokenMatch : tokenMatches) {
      previousMatches.put(tokenMatch, previousMatch);
      previousMatch = tokenMatch;
    }
    return previousMatches;
  }


}
