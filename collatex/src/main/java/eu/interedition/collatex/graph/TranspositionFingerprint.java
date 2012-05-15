package eu.interedition.collatex.graph;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

// TODO: needs more work, transpositionSize doesn't work as expected, so i turned it off for now.
public class TranspositionFingerprint {
  private final boolean hasTranspositions;
  private final int transpositionSize;

  TranspositionFingerprint(VariantGraphVertex v) {
    Iterable<VariantGraphTransposition> transpositions = v.transpositions();
    hasTranspositions = !Iterables.isEmpty(transpositions);
    transpositionSize = Iterables.size(transpositions);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(hasTranspositions/*, transpositionSize*/);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof TranspositionFingerprint)) return false;

    TranspositionFingerprint otherTfp = (TranspositionFingerprint) obj;
    return (otherTfp.hasTranspositions == hasTranspositions /*&& otherTfp.transpositionSize == transpositionSize*/);
  }

  @Override
  public String toString() {
    return "TranscriptionFingerprint: " + hasTranspositions + ";" + transpositionSize;
  }
}