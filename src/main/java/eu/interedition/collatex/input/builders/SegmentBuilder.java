package eu.interedition.collatex.input.builders;

import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSegmentPhrases;

public class SegmentBuilder {

  public static WitnessSegmentPhrases build(String witnessId, String phrasePlain) {
    WitnessBuilder builder = new WitnessBuilder();
    Witness witness = builder.build(witnessId, phrasePlain);
    Segment segment = witness.getFirstSegment();
    Phrase phrase = new Phrase(segment, segment.getWordOnPosition(1), segment.getWordOnPosition(segment.size()));
    WitnessSegmentPhrases p = new WitnessSegmentPhrases(witnessId, phrase);
    return p;
  }
}
