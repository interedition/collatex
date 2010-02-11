package eu.interedition.collatex.experimental.ngrams.data;

import java.util.Iterator;
import java.util.List;

public class NormalizedWitness implements Iterable<NormalizedToken> {
  private final String sigil;
  private final List<NormalizedToken> tokens;

  public NormalizedWitness(final String sigil, final List<NormalizedToken> tokens) {
    this.sigil = sigil;
    this.tokens = tokens;
  }

  public String getSigil() {
    return sigil;
  }

  // TODO: check whether iterator.remove() throws exception!
  @Override
  public Iterator<NormalizedToken> iterator() {
    return tokens.iterator();
  }

  public int size() {
    return tokens.size();
  }
}
