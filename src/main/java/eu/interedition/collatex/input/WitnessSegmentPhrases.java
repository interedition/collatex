package eu.interedition.collatex.input;

import java.util.List;

import com.google.common.collect.Lists;


public class WitnessSegmentPhrases {
  private final String _witnessId;
  private final List<Phrase> _phrases;

  public WitnessSegmentPhrases(String witnessId, Phrase phrase) {
    _witnessId = witnessId;
    _phrases = Lists.newArrayList(phrase);
  }

  public Phrase getPhraseOnPosition(int i) {
    return _phrases.get(i - 1);
  }

}
