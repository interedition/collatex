package eu.interedition.collatex.input.builders;

import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSegmentPhrases;

public class SegmentBuilder {

  public static WitnessSegmentPhrases build(final String witnessId, final String phrasePlain) {
    final WitnessBuilder builder = new WitnessBuilder();
    final Witness witness = builder.build(witnessId, phrasePlain);
    final Segment segment = witness.getFirstSegment();
    final Phrase phrase = new Phrase(segment, segment.getElementOnWordPosition(1), segment.getElementOnWordPosition(segment.wordSize()), null);
    final WitnessSegmentPhrases p = new WitnessSegmentPhrases(witnessId, phrase);
    return p;
  }
}
