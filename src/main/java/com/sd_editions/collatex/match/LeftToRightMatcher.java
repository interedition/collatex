package com.sd_editions.collatex.match;

import java.util.Set;

import com.google.common.collect.Sets;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.WitnessSegmentPhrases;

// TODO: add replacement tests!
// TODO: add check for alternative matches!
public class LeftToRightMatcher {

  public static Set<Match<Phrase>> match(final WitnessSegmentPhrases pa, final WitnessSegmentPhrases pb) {
    if (pa.size() > pb.size()) {
      final Set<Match<Phrase>> matches_wrong = match2(pb, pa);
      final Set<Match<Phrase>> matches_right = Sets.newLinkedHashSet();
      for (final Match<Phrase> match : matches_wrong) {
        matches_right.add(new Match<Phrase>(match.getWitnessWord(), match.getBaseWord()));
      }
      return matches_right;
    }
    return match2(pa, pb);

  }

  private static Set<Match<Phrase>> match2(final WitnessSegmentPhrases pa, final WitnessSegmentPhrases pb) {
    // take pa as a starting point (depends on the length!)

    final Set<Match<Phrase>> matches = Sets.newLinkedHashSet();
    for (int i = 1; i <= pa.size(); i++) {
      final Phrase phrase = pa.getPhraseOnPosition(i);
      for (int j = 1; j <= pb.size(); j++) {
        final Phrase phrase2 = pb.getPhraseOnPosition(j);
        final Subsegment subsegment1 = phrase.getSubsegment();
        final Subsegment subsegment2 = phrase2.getSubsegment();
        final boolean exactMatch = subsegment1.getTitle().equals(subsegment2.getTitle());
        if (exactMatch) {
          //          System.out.println(phrase);
          //          System.out.println(phrase2);
          matches.add(new Match<Phrase>(phrase, phrase2));
          break;
        } else {
          //          System.out.println(phrase);
          //          System.out.println(phrase2);
        }
      }
    }
    return matches;
  }
}
