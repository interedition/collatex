package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.experimental.ngrams.alignment.Gap;
import eu.interedition.collatex.experimental.ngrams.data.NormalizedWitness;
import eu.interedition.collatex.experimental.ngrams.data.Witness;
import eu.interedition.collatex.experimental.ngrams.tokenization.NormalizedWitnessBuilder;

public class WitnessSet {

  private final Witness a;
  private final Witness b;

  public WitnessSet(final Witness a, final Witness b) {
    this.a = a;
    this.b = b;
  }

  // TODO: maybe the alignment should know the witness set instead (top/down etc)
  // TODO: incomplete method!
  public Alignment align() {
    final NormalizedWitness aa = NormalizedWitnessBuilder.create(a);
    //final NormalizedWitness bb = NormalizedWitnessBuilder.create(b);
    final List<Gap> gaps = calculateGaps();
    final List<NGram> matches = calculateMatchesBasedOnGaps(gaps, aa);
    //    final List<NGram> bigrams = getUniqueBiGramIndexForWitnessA();
    //    final List<NGram> matches = calculateMatches(aa, bigrams);
    final List<Gap> trimmedGaps = trimGaps(gaps);
    final List<Gap> filteredGaps = filterUnigrramReplacements(trimmedGaps);
    return new Alignment(matches, filteredGaps);
  }

  private List<Gap> trimGaps(final List<Gap> gaps) {
    final List<Gap> trimmedGaps = Lists.newArrayList();
    for (final Gap gap : gaps) {
      final Gap trimmedGap = new Gap(gap.getNGramA().trim(), gap.getNGramB().trim());
      trimmedGaps.add(trimmedGap);
    }
    final List<Gap> filteredGaps = filterAwayEmptyBeginAndEndGaps(trimmedGaps);
    // Note; the second filter has the same effect!
    // final List<Gap> filteredGaps = filterUnigrramReplacements(gaps);
    return filteredGaps;
  }

  private List<NGram> calculateMatchesBasedOnGaps(final List<Gap> gaps, final NormalizedWitness aa) {
    int startPosition = 1;
    final List<NGram> matches = Lists.newArrayList();
    for (final Gap gap : gaps) {
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

  private List<Gap> calculateGaps() {
    // TODO: rename method!
    final List<NGram> ngramsA = getUniqueBiGramIndexForWitnessA();
    final List<NGram> ngramsB = getUniqueBiGramIndexForWitnessB();
    final List<Gap> gaps = Lists.newArrayList();
    for (int i = 0; i < ngramsA.size(); i++) {
      gaps.add(new Gap(ngramsA.get(i)/*.trim()*/, ngramsB.get(i)/*.trim()*/));
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

  private List<Gap> filterAwayEmptyBeginAndEndGaps(final List<Gap> gaps) {
    // filter away empty begin and end gaps
    final List<Gap> nonEmptyGaps = Lists.newArrayList();
    for (final Gap gap : gaps) {
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
