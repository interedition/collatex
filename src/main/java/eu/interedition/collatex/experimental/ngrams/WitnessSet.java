package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import com.google.common.collect.Lists;

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

    int startPosition = 1;

    final BiGramIndex bigrams = getUniqueBiGramIndexForWitnessA();
    final List<NGram> matches = Lists.newArrayList();
    for (final BiGram bigram : bigrams) {
      final int endPosition = bigram.getFirstToken().getPosition();
      System.out.println(startPosition);
      System.out.println(endPosition);
      final NGram ngram = NGram.create(aa, startPosition, endPosition);
      System.out.println(ngram.getNormalized());
      matches.add(ngram);
      startPosition = bigram.getLastToken().getPosition();
    }
    final NGram ngram = NGram.create(aa, startPosition, aa.size());
    matches.add(ngram);
    System.out.println(ngram.getNormalized());
    return new Alignment(matches);
  }

  public BiGramIndex getUniqueBiGramIndexForWitnessA() {
    final BiGramIndexGroup group = BiGramIndexGroup.create(a, b);
    return group.getUniqueBiGramsForWitnessA();
  }

}
