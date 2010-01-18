package eu.interedition.collatex.experimental.ngrams;

import java.util.Map;

import com.google.common.collect.Maps;

import eu.interedition.collatex.input.Word;

public class Subsegment2 {

  private final String _normalized;
  //Note: it does not have to be a map; could
  //also be a list; but would be slower!
  private final Map<String, WordsTuple> _sigilToPhrase;

  public Subsegment2(final String normalized, final WordsTuple tuple) {
    this._normalized = normalized;
    this._sigilToPhrase = Maps.newLinkedHashMap();
    final String witnessId = tuple.getFirstWord().getWitnessId();
    _sigilToPhrase.put(witnessId, tuple);
  }

  public Subsegment2(final String normalized, final WordsTuple wordsA, final WordsTuple wordsB) {
    this._normalized = normalized;
    this._sigilToPhrase = Maps.newLinkedHashMap();
    _sigilToPhrase.put(wordsA.getFirstWord().getWitnessId(), wordsA);
    _sigilToPhrase.put(wordsB.getFirstWord().getWitnessId(), wordsB);
  }

  public String getNormalized() {
    return _normalized;
  }

  public boolean contains(final String sigil) {
    return _sigilToPhrase.containsKey(sigil);
  }

  public WordsTuple getPhraseFor(final String sigil) {
    if (!contains(sigil)) {
      throw new RuntimeException("This subsegment does not contain a phrase for " + sigil);
    }
    return _sigilToPhrase.get(sigil);
  }

  public Word getFirstWordFor(final String sigil) {
    final WordsTuple phrase = getPhraseFor(sigil);
    return phrase.getFirstWord();
  }

  @Override
  public String toString() {
    final StringBuffer buffer = new StringBuffer();
    buffer.append(_normalized);
    for (final String sigil : _sigilToPhrase.keySet()) {
      final WordsTuple wordsTuple = _sigilToPhrase.get(sigil);
      buffer.append(" ");
      buffer.append(wordsTuple.getFirstWord().getWitnessId());
      buffer.append(": ");
      buffer.append(wordsTuple.getFirstWord().getBeginPosition());
    }
    return buffer.toString();
  }
}
