package eu.interedition.collatex.matching;

import java.util.Set;

import com.google.common.collect.Sets;
import com.sd_editions.collatex.match.Subsegment;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.WitnessSegmentPhrases;

public class LeftToRightMatcher {

  public static Set<Match<Phrase>> match(final WitnessSegmentPhrases pa, final WitnessSegmentPhrases pb) {
    final Set<Match<Phrase>> matches = Sets.newLinkedHashSet();
    // take pa as a starting point (depends on the length!)
    for (int i = 1; i <= pa.size(); i++) {
      final Phrase phrase = pa.getPhraseOnPosition(i);
      final Phrase phrase2 = pb.getPhraseOnPosition(i);
      final Subsegment subsegment1 = phrase.getSubsegment();
      final Subsegment subsegment2 = phrase2.getSubsegment();
      final boolean exactMatch = subsegment1.getTitle().equals(subsegment2.getTitle());
      if (exactMatch) {
        matches.add(new Match<Phrase>(phrase, phrase2));
      }
    }
    return matches;
  }
}
