package eu.interedition.collatex.graph;

import static com.google.common.collect.Iterables.*;
import static java.util.Collections.singleton;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import eu.interedition.collatex.Witness;
import eu.interedition.collatex.simple.SimpleToken;

// TODO: needs more work, transpositionSize doesn't work as expected, so i turned it off for now.
public class TranspositionFingerprint {
  Logger LOG = LoggerFactory.getLogger(this.getClass());
  private final boolean hasTranspositions;
  private VariantGraphVertex otherVertex = null;
  private final Set<Witness> witnesses;
  private int sign = 0;

  TranspositionFingerprint(VariantGraphVertex v) {
    witnesses = v.witnesses();
    Iterable<VariantGraphTransposition> transpositions = v.transpositions();
    hasTranspositions = transpositions.iterator().hasNext();
    if (hasTranspositions) {
      VariantGraphTransposition first = transpositions.iterator().next();
      otherVertex = first.other(v);
      SimpleToken lastToken = getLast(Ordering.natural().sortedCopy(Iterables.filter(v.tokens(singleton(getFirst(witnesses, null))), SimpleToken.class)));
      SimpleToken firstOtherToken = getLast(Ordering.natural().sortedCopy(Iterables.filter(otherVertex.tokens(singleton(getFirst(otherVertex.witnesses(), null))), SimpleToken.class)), null);
      int diff = (lastToken.getIndex() - firstOtherToken.getIndex());
      sign = (diff < 0) ? -1 : 1;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(hasTranspositions, witnesses, sign);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof TranspositionFingerprint)) return false;

    TranspositionFingerprint otherTfp = (TranspositionFingerprint) obj;
    return (otherTfp.hasTranspositions == hasTranspositions && //
        witnesses.equals(otherTfp.witnesses) && //
    sign == otherTfp.sign);
  }

  @Override
  public String toString() {
    return "TranscriptionFingerprint: " + hasTranspositions + ";" + witnesses + ";" + sign;
  }
}