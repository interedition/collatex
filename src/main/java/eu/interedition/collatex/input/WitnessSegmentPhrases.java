package eu.interedition.collatex.input;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Join;
import com.google.common.collect.Lists;

public class WitnessSegmentPhrases extends BaseContainer<Phrase> {
  private final String _witnessId;
  private final List<Phrase> _phrases;

  public WitnessSegmentPhrases(final String witnessId, final Phrase... phrases) {
    _witnessId = witnessId;
    _phrases = Lists.newArrayList(phrases);
  }

  public WitnessSegmentPhrases(final String witnessId, final Collection<Phrase> phrases) {
    _witnessId = witnessId;
    _phrases = Lists.newArrayList(phrases);
  }

  public Phrase getPhraseOnPosition(final int i) {
    return _phrases.get(i - 1);
  }

  public String getWitnessId() {
    return _witnessId;
  }

  @Override
  public String toString() {
    return "WitnessSegmentPhrases(" + getWitnessId() + ", '" + Join.join("','", _phrases) + "')";
  }

  @Override
  public int wordSize() {
    return getPhrases().get(getPhrases().size() - 1).getEndPosition();
  }

  public int size() {
    return _phrases.size();
  }

  @Override
  public Phrase getElementOnWordPosition(final int k) {
    for (final Phrase phrase : getPhrases()) {
      if (phrase.getBeginPosition() == k) {
        return phrase;
      }
    }
    throw new RuntimeException("No element found on position: " + k);
  }

  public List<Phrase> getPhrases() {
    return _phrases;
  }

  public String toSpecialString() {
    final StringBuffer buffer = new StringBuffer();
    buffer.append("|");
    for (final Phrase phrase : _phrases) {
      // TODO could become get normalized1
      buffer.append(phrase.getOriginal());
      buffer.append("|");
    }
    return buffer.toString();
  }
}
