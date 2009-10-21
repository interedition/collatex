package eu.interedition.collatex.alignment.multiple_witness.segments;

import java.util.List;

import eu.interedition.collatex.input.Word;

// Note: this class looks a bit like Phrase!
public class OldSegment {

  private final List<Word> _words;

  public OldSegment(List<Word> words) {
    this._words = words;
  }

  @Override
  public String toString() {
    String replacementString = "";
    String divider = "";
    for (Word replacement : _words) {
      replacementString += divider + replacement.toString();
      divider = " ";
    }
    return replacementString;
  }

  public List<Word> getWords() {
    return _words;
  }

  public String getWitnessId() {
    return getWords().get(0).getWitnessId();
  }

}
