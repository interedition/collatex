package eu.interedition.collatex2.implementation.vg_analysis;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SequenceDetector {
  
  public List<ISequence> detect(Map<INormalizedToken, INormalizedToken> linkedTokens, IWitness superbase, IWitness witness) {
    Map<INormalizedToken, IAlignedToken> alignedTokens = createAlignedTokensMap(linkedTokens);
    Map<IAlignedToken, IAlignedToken> previousMapForBase = buildPreviousMap(superbase, alignedTokens);
    Map<IAlignedToken, IAlignedToken> previousMapForWitness = buildPreviousMap(witness, alignedTokens);
    // chain token matches
    List<INormalizedToken> tokensBase = Lists.newArrayList();
    List<INormalizedToken> tokensWitness = Lists.newArrayList();
    List<ISequence> sequences = Lists.newArrayList();
    for (IAlignedToken tokenMatch : previousMapForWitness.keySet()) {
      IAlignedToken previousBase = previousMapForBase.get(tokenMatch);
      IAlignedToken previousWitness = previousMapForWitness.get(tokenMatch);
      if (previousBase != previousWitness) {
        // start a new sequence;
        createAndAddChainedMatch(tokensBase, tokensWitness, sequences);
        // clear buffer
        tokensBase = Lists.newArrayList();
        tokensWitness = Lists.newArrayList();
      }
      INormalizedToken tokenBase = tokenMatch.getAlignedToken();
      INormalizedToken tokenWitness = tokenMatch.getWitnessToken();
      tokensBase.add(tokenBase);
      tokensWitness.add(tokenWitness);
    }
    createAndAddChainedMatch(tokensBase, tokensWitness, sequences);
    return sequences;
  }

  private void createAndAddChainedMatch(List<INormalizedToken> tokensBase, List<INormalizedToken> tokensB, List<ISequence> sequences) {
    // save current state if necessary
    if (tokensBase != null && !tokensBase.isEmpty()) {
      sequences.add(new Sequence(Lists.<INormalizedToken>newArrayList(tokensBase), Lists.<INormalizedToken>newArrayList(tokensB)));
    }
  }

  public Map<IAlignedToken, IAlignedToken> buildPreviousMap(IWitness superbase, Map<INormalizedToken, IAlignedToken> alignedTokens) {
    Map<IAlignedToken, IAlignedToken> previousAlignedTokenMap = Maps.newLinkedHashMap();
    IAlignedToken previous = null;
    for (INormalizedToken token : superbase.getTokens()) {
      // skip non matches
      if (!alignedTokens.containsKey(token)) {
        continue;
      }
      IAlignedToken next = alignedTokens.get(token);
      previousAlignedTokenMap.put(next, previous);
      previous = next;
    }
    return previousAlignedTokenMap;
  }

  public Map<INormalizedToken, IAlignedToken> createAlignedTokensMap(Map<INormalizedToken, INormalizedToken> linkedTokens) {
    Map<INormalizedToken, IAlignedToken> alignedTokens = Maps.newLinkedHashMap(); 
    for (Entry<INormalizedToken, INormalizedToken> entry : linkedTokens.entrySet()) {
      AlignedToken alignedToken = new AlignedToken(entry.getKey(), entry.getValue());
      alignedTokens.put(entry.getKey(), alignedToken);
      alignedTokens.put(entry.getValue(), alignedToken);
    }
    return alignedTokens;
  }

}
