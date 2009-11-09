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
  public int size() {
    return _phrases.size();
  }

  @Override
  public Phrase getWordOnPosition(final int k) {
    return getPhraseOnPosition(k);
  }
}
