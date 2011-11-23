package eu.interedition.collatex2.implementation.vg_analysis;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.interedition.collatex2.implementation.Tuple;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SequenceDetector {
  
  public List<Tuple<List<INormalizedToken>>> detect(Map<INormalizedToken, INormalizedToken> linkedTokens, IWitness superbase, IWitness witness) {
    Map<INormalizedToken, Tuple<INormalizedToken>> alignedTokens = createAlignedTokensMap(linkedTokens);
    Map<Tuple<INormalizedToken>, Tuple<INormalizedToken>> previousMapForBase = buildPreviousMap(superbase, alignedTokens);
    Map<Tuple<INormalizedToken>, Tuple<INormalizedToken>> previousMapForWitness = buildPreviousMap(witness, alignedTokens);
    // chain token matches
    List<INormalizedToken> tokensBase = Lists.newArrayList();
    List<INormalizedToken> tokensWitness = Lists.newArrayList();
    List<Tuple<List<INormalizedToken>>> sequences = Lists.newArrayList();
    for (Tuple<INormalizedToken> tokenMatch : previousMapForWitness.keySet()) {
      Tuple<INormalizedToken> previousBase = previousMapForBase.get(tokenMatch);
      Tuple<INormalizedToken> previousWitness = previousMapForWitness.get(tokenMatch);
      if (previousBase != previousWitness) {
        // start a new sequence;
        createAndAddChainedMatch(tokensBase, tokensWitness, sequences);
        // clear buffer
        tokensBase = Lists.newArrayList();
        tokensWitness = Lists.newArrayList();
      }
      tokensBase.add(tokenMatch.right);
      tokensWitness.add(tokenMatch.left);
    }
    createAndAddChainedMatch(tokensBase, tokensWitness, sequences);
    return sequences;
  }

  private void createAndAddChainedMatch(List<INormalizedToken> tokensBase, List<INormalizedToken> tokensB, List<Tuple<List<INormalizedToken>>> sequences) {
    // save current state if necessary
    if (tokensBase != null && !tokensBase.isEmpty()) {
      sequences.add(new Tuple<List<INormalizedToken>>(Lists.newArrayList(tokensBase), Lists.newArrayList(tokensB)));
    }
  }

  public Map<Tuple<INormalizedToken>, Tuple<INormalizedToken>> buildPreviousMap(IWitness superbase, Map<INormalizedToken, Tuple<INormalizedToken>> alignedTokens) {
    Map<Tuple<INormalizedToken>, Tuple<INormalizedToken>> previousAlignedTokenMap = Maps.newLinkedHashMap();
    Tuple<INormalizedToken> previous = null;
    for (INormalizedToken token : superbase.getTokens()) {
      // skip non matches
      if (!alignedTokens.containsKey(token)) {
        continue;
      }
      Tuple<INormalizedToken> next = alignedTokens.get(token);
      previousAlignedTokenMap.put(next, previous);
      previous = next;
    }
    return previousAlignedTokenMap;
  }

  public Map<INormalizedToken, Tuple<INormalizedToken>> createAlignedTokensMap(Map<INormalizedToken, INormalizedToken> linkedTokens) {
    Map<INormalizedToken, Tuple<INormalizedToken>> alignedTokens = Maps.newLinkedHashMap();
    for (Entry<INormalizedToken, INormalizedToken> entry : linkedTokens.entrySet()) {
      Tuple<INormalizedToken> alignedToken = new Tuple<INormalizedToken>(entry.getKey(), entry.getValue());
      alignedTokens.put(entry.getKey(), alignedToken);
      alignedTokens.put(entry.getValue(), alignedToken);
    }
    return alignedTokens;
  }

}
