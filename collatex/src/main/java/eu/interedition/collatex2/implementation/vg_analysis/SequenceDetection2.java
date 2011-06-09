/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex2.implementation.vg_analysis;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.input.Phrase;
import eu.interedition.collatex2.implementation.vg_alignment.IAlignment2;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.ITokenContainer;
import eu.interedition.collatex2.interfaces.ITokenMatch;

public class SequenceDetection2 {

  private final List<ITokenMatch> tokenMatches;
  private final ITokenContainer base;
  private final ITokenContainer witness;

  public SequenceDetection2(IAlignment2 alignment) {
    this.base = alignment.getGraph();
    this.witness = alignment.getWitness();
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
      INormalizedToken tokenA = tokenMatch.getBaseToken();
      INormalizedToken tokenB = tokenMatch.getWitnessToken();
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
