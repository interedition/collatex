package eu.interedition.collatex.implementation.alignment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.interedition.collatex.implementation.Tuple;
import eu.interedition.collatex.interfaces.Token;
import eu.interedition.collatex.interfaces.IWitness;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PhraseMatchDetector {
  
  public List<Tuple<List<Token>>> detect(Map<Token, Token> linkedTokens, IWitness base, IWitness witness) {
    final Map<Token, Tuple<Token>> tokenIndex = createTokenIndex(linkedTokens);
    final Map<Tuple<Token>, Tuple<Token>> prevMatchesInBase = mapPrevTokenMatches(base, tokenIndex);
    final Map<Tuple<Token>, Tuple<Token>> prevMatchesInWitness = mapPrevTokenMatches(witness, tokenIndex);

    final List<Tuple<List<Token>>> phraseMatches = Lists.newArrayList();

    // chain token matches
    final List<Token> basePhrase = Lists.newArrayList();
    final List<Token> witnessPhrase = Lists.newArrayList();
    for (Tuple<Token> witnessMatch : prevMatchesInWitness.keySet()) {
      final Tuple<Token> prevMatchInBase = prevMatchesInBase.get(witnessMatch);
      final Tuple<Token> prevMatchInWitness = prevMatchesInWitness.get(witnessMatch);
      if (prevMatchInBase != prevMatchInWitness) {
        if (!basePhrase.isEmpty()) {
        // start a new sequence
          phraseMatches.add(new Tuple<List<Token>>(Lists.newArrayList(basePhrase), Lists.newArrayList(witnessPhrase)));
        }
        // clear buffer
        basePhrase.clear();
        witnessPhrase.clear();
      }
      basePhrase.add(witnessMatch.right);
      witnessPhrase.add(witnessMatch.left);
    }
    if (!basePhrase.isEmpty()) {
      phraseMatches.add(new Tuple<List<Token>>(Lists.newArrayList(basePhrase), Lists.newArrayList(witnessPhrase)));
    }

    return phraseMatches;
  }

  private Map<Tuple<Token>, Tuple<Token>> mapPrevTokenMatches(IWitness in, Map<Token, Tuple<Token>> tokenIndex) {
    final Map<Tuple<Token>, Tuple<Token>> predecessors = Maps.newLinkedHashMap();
    Tuple<Token> prev = null;
    for (Token token : in.getTokens()) {
      if (!tokenIndex.containsKey(token)) {
        // skip non matches
        continue;
      }
      final Tuple<Token> next = tokenIndex.get(token);
      predecessors.put(next, prev);
      prev = next;
    }
    return predecessors;
  }

  private Map<Token, Tuple<Token>> createTokenIndex(Map<Token, Token> linkedTokens) {
    final Map<Token, Tuple<Token>> index = Maps.newLinkedHashMap();
    for (Entry<Token, Token> entry : linkedTokens.entrySet()) {
      final Token left = entry.getKey();
      final Token right = entry.getValue();
      final Tuple<Token> alignedToken = new Tuple<Token>(left, right);
      index.put(left, alignedToken);
      index.put(right, alignedToken);
    }
    return index;
  }

}
