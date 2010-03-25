package eu.interedition.collatex.alignment.functions;

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.alignment.UnfixedAlignment;
import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.WitnessSegmentPhrases;

public class PhraseAligner {

  // TODO: warning duplicate with Matcher.align!
  public static Alignment<Phrase> align(final WitnessSegmentPhrases a, final WitnessSegmentPhrases b, final UnfixedAlignment<Phrase> u) {
    final UnfixedAlignment<Phrase> temp = u;
    //    while (temp.hasUnfixedWords()) {
    //      temp = Matcher.permutate(a, b, temp);
    //    }
    final Alignment<Phrase> alignment = Alignment.createPhraseAlignment(temp, a, b);
    return alignment;
  }
}
