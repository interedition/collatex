package eu.interedition.collatex.experimental.ngrams;

import java.util.Map;

import com.google.common.collect.Maps;

import eu.interedition.collatex.experimental.ngrams.data.Token;

public class Subsegment2 {

  private final String _normalized;
  //Note: it does not have to be a map; could
  //also be a list; but would be slower!
  private final Map<String, BiGram> _sigilToPhrase;

  public Subsegment2(final String normalized, final BiGram tuple) {
    this._normalized = normalized;
    this._sigilToPhrase = Maps.newLinkedHashMap();
    final String witnessId = tuple.getFirstToken().getSigil();
    _sigilToPhrase.put(witnessId, tuple);
  }

  public Subsegment2(final String normalized, final BiGram wordsA, final BiGram wordsB) {
    this._normalized = normalized;
    this._sigilToPhrase = Maps.newLinkedHashMap();
    _sigilToPhrase.put(wordsA.getFirstToken().getSigil(), wordsA);
    _sigilToPhrase.put(wordsB.getFirstToken().getSigil(), wordsB);
  }

  public String getNormalized() {
    return _normalized;
  }

  public boolean contains(final String sigil) {
    return _sigilToPhrase.containsKey(sigil);
  }

  public BiGram getPhraseFor(final String sigil) {
    if (!contains(sigil)) {
      throw new RuntimeException("This subsegment does not contain a phrase for " + sigil);
    }
    return _sigilToPhrase.get(sigil);
  }

  public Token getFirstWordFor(final String sigil) {
    final BiGram phrase = getPhraseFor(sigil);
    return phrase.getFirstToken();
  }

  @Override
  public String toString() {
    final StringBuffer buffer = new StringBuffer();
    buffer.append(_normalized);
    for (final String sigil : _sigilToPhrase.keySet()) {
      final BiGram wordsTuple = _sigilToPhrase.get(sigil);
      buffer.append(" ");
      buffer.append(wordsTuple.getFirstToken().getSigil());
      buffer.append(": ");
      buffer.append(wordsTuple.getFirstToken().getPosition());
    }
    return buffer.toString();
  }
}
