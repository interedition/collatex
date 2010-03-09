package eu.interedition.collatex.experimental.ngrams.alignment;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.experimental.ngrams.BiGramIndexGroup;
import eu.interedition.collatex.experimental.ngrams.NGram;
import eu.interedition.collatex.interfaces.IWitness;

public class WitnessSet {

  private final IWitness a;
  private final IWitness b;

  public WitnessSet(final IWitness a, final IWitness b) {
    this.a = a;
    this.b = b;
  }

  // TODO: maybe the alignment should know the witness set instead (top/down etc)
  // TODO: incomplete method!
  protected Alignment align() {
    final List<InternalUncompleteGap> unprocessedGaps = calculateGaps();
    final List<NGram> matches = calculateMatchesBasedOnGaps(unprocessedGaps, a);
    final List<Gap> gaps = mapToGaps(unprocessedGaps, matches);
    //    final List<NGram> bigrams = getUniqueBiGramIndexForWitnessA();
    //    final List<NGram> matches = calculateMatches(aa, bigrams);
    //final List<InternalUncompleteGap> trimmedGaps = trimGaps(gaps);
    //final List<InternalUncompleteGap> filteredGaps = filterUnigrramReplacements(trimmedGaps);
    return new Alignment(matches, gaps);
  }

  // TODO: maybe more tests should be added for nextMatch!
  private List<Gap> mapToGaps(final List<InternalUncompleteGap> unprocessedGaps, final List<NGram> matches) {
    final List<Gap> gaps = Lists.newArrayList();
    for (final InternalUncompleteGap iGap : unprocessedGaps) {
      final NGram nGramA = iGap.getNGramA();
      final NGram nGramB = iGap.getNGramB();
      // find match
      final int position = nGramA.getLastToken().getPosition();
      NGram nextMatch = null;
      for (final NGram match : matches) {
        if (match.getFirstToken().getPosition() == position) {
          nextMatch = match;
        }
      }
      final Gap gap = new Gap(nGramA.trim(), nGramB.trim(), nextMatch);
      gaps.add(gap);
    }
    return filterUnigrramReplacements(gaps);
  }

  private List<InternalUncompleteGap> trimGaps(final List<InternalUncompleteGap> gaps) {
    final List<InternalUncompleteGap> trimmedGaps = Lists.newArrayList();
    for (final InternalUncompleteGap gap : gaps) {
      final InternalUncompleteGap trimmedGap = new InternalUncompleteGap(gap.getNGramA().trim(), gap.getNGramB().trim());
      trimmedGaps.add(trimmedGap);
    }
    final List<InternalUncompleteGap> filteredGaps = filterAwayEmptyBeginAndEndGaps(trimmedGaps);
    // Note; the second filter has the same effect!
    // final List<Gap> filteredGaps = filterUnigrramReplacements(gaps);
    return filteredGaps;
  }

  private List<NGram> calculateMatchesBasedOnGaps(final List<InternalUncompleteGap> gaps, final IWitness aa) {
    int startPosition = 1;
    final List<NGram> matches = Lists.newArrayList();
    for (final InternalUncompleteGap gap : gaps) {
      final NGram gapNGram = gap.getNGramA();
      //System.out.println("NGRAm voor de gap: " + gapNGram.getNormalized());
      final int endPosition = gapNGram.getFirstToken().getPosition();
      final NGram matchNGram = NGram.create(aa, startPosition, endPosition);
      matches.add(matchNGram);
      startPosition = gapNGram.getLastToken().getPosition();
      if (gap.getNGramA().trim().getNormalized().equals(gap.getNGramB().trim().getNormalized())) {
        matches.add(gapNGram.trim());
      }
    }
    final NGram ngram = NGram.create(aa, startPosition, aa.size());
    matches.add(ngram);
    final List<NGram> filteredMatches = filterEmptyBeginAndEndMatches(matches);
    return filteredMatches;
  }

  private List<InternalUncompleteGap> calculateGaps() {
    // TODO: rename method!
    final List<NGram> ngramsA = getUniqueBiGramIndexForWitnessA();
    final List<NGram> ngramsB = getUniqueBiGramIndexForWitnessB();
    final List<InternalUncompleteGap> gaps = Lists.newArrayList();
    for (int i = 0; i < ngramsA.size(); i++) {
      gaps.add(new InternalUncompleteGap(ngramsA.get(i)/*.trim()*/, ngramsB.get(i)/*.trim()*/));
    }

    return gaps/*filteredGaps*/;
  }

  private List<Gap> filterUnigrramReplacements(final List<Gap> gaps) {
    // filter away unigram replacements that are really matches!
    final List<Gap> filteredGaps = Lists.newArrayList();
    for (final Gap gap : gaps) {
      if (!gap.getNGramA().getNormalized().equals(gap.getNGramB().getNormalized())) {
        filteredGaps.add(gap);
      }
    }
    return filteredGaps;
  }

  private List<InternalUncompleteGap> filterAwayEmptyBeginAndEndGaps(final List<InternalUncompleteGap> gaps) {
    // filter away empty begin and end gaps
    final List<InternalUncompleteGap> nonEmptyGaps = Lists.newArrayList();
    for (final InternalUncompleteGap gap : gaps) {
      if (!gap.isEmpty()) {
        nonEmptyGaps.add(gap);
      }
    }
    return nonEmptyGaps;
  }

  private List<NGram> filterEmptyBeginAndEndMatches(final List<NGram> matches) {
    // filter away empty begin and end matches
    final List<NGram> nonEmptyMatches = Lists.newArrayList();
    for (final NGram match : matches) {
      if (!match.isEmpty()) {
        nonEmptyMatches.add(match);
      }
    }
    return nonEmptyMatches;
  }

  // TODO: inline!
  public List<NGram> getUniqueBiGramIndexForWitnessA() {
    final BiGramIndexGroup group = BiGramIndexGroup.create(a, b);
    return group.getUniqueNGramsForWitnessA();
  }

  // TODO: inline!
  public List<NGram> getUniqueBiGramIndexForWitnessB() {
    final BiGramIndexGroup group = BiGramIndexGroup.create(a, b);
    return group.getUniqueNGramsForWitnessB();
  }

}
