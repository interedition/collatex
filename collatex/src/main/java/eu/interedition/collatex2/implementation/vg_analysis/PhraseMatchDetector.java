package eu.interedition.collatex2.implementation.vg_analysis;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.interedition.collatex2.implementation.Tuple;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PhraseMatchDetector {
  
  public List<Tuple<List<INormalizedToken>>> detect(Map<INormalizedToken, INormalizedToken> linkedTokens, IWitness base, IWitness witness) {
    final Map<INormalizedToken, Tuple<INormalizedToken>> tokenIndex = createTokenIndex(linkedTokens);
    final Map<Tuple<INormalizedToken>, Tuple<INormalizedToken>> prevMatchesInBase = mapPrevTokenMatches(base, tokenIndex);
    final Map<Tuple<INormalizedToken>, Tuple<INormalizedToken>> prevMatchesInWitness = mapPrevTokenMatches(witness, tokenIndex);

    final List<Tuple<List<INormalizedToken>>> phraseMatches = Lists.newArrayList();

    // chain token matches
    final List<INormalizedToken> basePhrase = Lists.newArrayList();
    final List<INormalizedToken> witnessPhrase = Lists.newArrayList();
    for (Tuple<INormalizedToken> witnessMatch : prevMatchesInWitness.keySet()) {
      final Tuple<INormalizedToken> prevMatchInBase = prevMatchesInBase.get(witnessMatch);
      final Tuple<INormalizedToken> prevMatchInWitness = prevMatchesInWitness.get(witnessMatch);
      if (prevMatchInBase != prevMatchInWitness) {
        if (!basePhrase.isEmpty()) {
        // start a new sequence
          phraseMatches.add(new Tuple<List<INormalizedToken>>(Lists.newArrayList(basePhrase), Lists.newArrayList(witnessPhrase)));
        }
        // clear buffer
        basePhrase.clear();
        witnessPhrase.clear();
      }
      basePhrase.add(witnessMatch.right);
      witnessPhrase.add(witnessMatch.left);
    }
    if (!basePhrase.isEmpty()) {
      phraseMatches.add(new Tuple<List<INormalizedToken>>(Lists.newArrayList(basePhrase), Lists.newArrayList(witnessPhrase)));
    }

    return phraseMatches;
  }

  private Map<Tuple<INormalizedToken>, Tuple<INormalizedToken>> mapPrevTokenMatches(IWitness in, Map<INormalizedToken, Tuple<INormalizedToken>> tokenIndex) {
    final Map<Tuple<INormalizedToken>, Tuple<INormalizedToken>> predecessors = Maps.newLinkedHashMap();
    Tuple<INormalizedToken> prev = null;
    for (INormalizedToken token : in.getTokens()) {
      if (!tokenIndex.containsKey(token)) {
        // skip non matches
        continue;
      }
      final Tuple<INormalizedToken> next = tokenIndex.get(token);
      predecessors.put(next, prev);
      prev = next;
    }
    return predecessors;
  }

  private Map<INormalizedToken, Tuple<INormalizedToken>> createTokenIndex(Map<INormalizedToken, INormalizedToken> linkedTokens) {
    final Map<INormalizedToken, Tuple<INormalizedToken>> index = Maps.newLinkedHashMap();
    for (Entry<INormalizedToken, INormalizedToken> entry : linkedTokens.entrySet()) {
      final INormalizedToken left = entry.getKey();
      final INormalizedToken right = entry.getValue();
      final Tuple<INormalizedToken> alignedToken = new Tuple<INormalizedToken>(left, right);
      index.put(left, alignedToken);
      index.put(right, alignedToken);
    }
    return index;
  }

}
