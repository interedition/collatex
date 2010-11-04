package eu.interedition.collatex2.implementation.vg_analysis;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.input.Phrase;
import eu.interedition.collatex2.implementation.vg_alignment.IAlignment2;
import eu.interedition.collatex2.implementation.vg_alignment.Sequence;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.ITokenContainer;
import eu.interedition.collatex2.interfaces.ITokenMatch;

public class SequenceDetection2 {

  private final List<ITokenMatch> tokenMatches;
  private final ITokenContainer base;
  private final ITokenContainer witness;

  public SequenceDetection2(List<ITokenMatch> tokenMatches, ITokenContainer base, ITokenContainer witness) {
    this.tokenMatches = tokenMatches;
    this.base = base;
    this.witness = witness;
  }

  public SequenceDetection2(IAlignment2 alignment, ITokenContainer base, ITokenContainer witness) {
    this.base = base;
    this.witness = witness;
    this.tokenMatches = alignment.getTokenMatches();
  }

  public List<ISequence> chainTokenMatches() {
    // prepare
    Map<ITokenMatch, ITokenMatch> previousMatchMap = buildPreviousMatchMap();
    // chain token matches
    List<INormalizedToken> tokensA = null;
    List<INormalizedToken> tokensB = null;
    List<ISequence> sequences = Lists.newArrayList();
    for (ITokenMatch tokenMatch : tokenMatches) {
      ITokenMatch previous = previousMatchMap.get(tokenMatch);
      if (previous == null || !base.isNear(previous.getBaseToken(), tokenMatch.getBaseToken()) || !witness.isNear(previous.getWitnessToken(), tokenMatch.getWitnessToken())) {
        // start a new sequence;
        createAndAddChainedMatch(tokensA, tokensB, sequences);
        // clear buffer
        tokensA = Lists.newArrayList();
        tokensB = Lists.newArrayList();
      }
      INormalizedToken tokenA = tokenMatch.getTokenA();
      INormalizedToken tokenB = tokenMatch.getTokenB();
      tokensA.add(tokenA);
      tokensB.add(tokenB);
    }
    createAndAddChainedMatch(tokensA, tokensB, sequences);
    return sequences;
  }

  private void createAndAddChainedMatch(List<INormalizedToken> tokensA, List<INormalizedToken> tokensB, List<ISequence> sequences) {
    // save current state if necessary
    if (tokensA != null && !tokensA.isEmpty()) {
      IPhrase phraseA = new Phrase(tokensA);
      IPhrase phraseB = new Phrase(tokensB);
      ISequence sequence = new Sequence(phraseA, phraseB);
      sequences.add(sequence);
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
