package eu.interedition.collatex.graph;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TranspositionFingerprint {

  private final Set<VariantGraphTransposition> fingerprint;

  public TranspositionFingerprint(VariantGraphVertex vertex) {
    this.fingerprint = Sets.newHashSet(vertex.transpositions());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof TranspositionFingerprint) {
      return fingerprint.equals(((TranspositionFingerprint) obj).fingerprint);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return fingerprint.hashCode();
  }
}
