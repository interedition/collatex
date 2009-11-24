package eu.interedition.collatex.experimental.ngrams;

import java.util.Map;

import com.google.common.collect.Maps;

import eu.interedition.collatex.input.Phrase;

public class Subsegment2 {

  private final String _normalized;
  //Note: it does not have to be a map; could
  //also be a list; but would be slower!
  private final Map<String, Phrase> _sigilToPhrase;

  public Subsegment2(final String normalized, final Phrase... a) {
    this._normalized = normalized;
    this._sigilToPhrase = Maps.newLinkedHashMap();
    for (final Phrase p : a) {
      _sigilToPhrase.put(p.getWitnessId(), p);
    }
  }

  public String getNormalized() {
    return _normalized;
  }

  public boolean contains(final String sigil) {
    return _sigilToPhrase.containsKey(sigil);
  }

  public Phrase getPhraseFor(final String sigil) {
    if (!contains(sigil)) {
      throw new RuntimeException("This subsegment does not contain a phrase for " + sigil);
    }
    return _sigilToPhrase.get(sigil);
  }

}
