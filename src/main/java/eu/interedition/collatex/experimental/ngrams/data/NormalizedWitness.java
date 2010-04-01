package eu.interedition.collatex.experimental.ngrams.data;

import java.util.Iterator;
import java.util.List;

import eu.interedition.collatex.interfaces.IWitness;

public class NormalizedWitness implements Iterable<NormalizedToken>, IWitness {
  private final String sigil;
  private final List<NormalizedToken> tokens;

  public NormalizedWitness(final String sigil, final List<NormalizedToken> tokens) {
    this.sigil = sigil;
    this.tokens = tokens;
  }

  // Note: not pleased with this method! implement Iterable!
  /* (non-Javadoc)
   * @see eu.interedition.collatex.experimental.ngrams.data.IRealWitness#getTokens()
   */
  public List<NormalizedToken> getTokens() {
    return tokens;
  }

  /* (non-Javadoc)
   * @see eu.interedition.collatex.experimental.ngrams.data.IRealWitness#getSigil()
   */
  public String getSigil() {
    return sigil;
  }

  // TODO check whether iterator.remove() throws exception!
  /* (non-Javadoc)
   * @see eu.interedition.collatex.experimental.ngrams.data.IRealWitness#iterator()
   */
  @Override
  public Iterator<NormalizedToken> iterator() {
    return tokens.iterator();
  }

  //Note: not pleased with this method! reduce visibility?
  /* (non-Javadoc)
   * @see eu.interedition.collatex.experimental.ngrams.data.IRealWitness#getTokens(int, int)
   */
  public List<NormalizedToken> getTokens(final int startPosition, final int endPosition) {
    return tokens.subList(startPosition - 1, endPosition);
  }

  /* (non-Javadoc)
   * @see eu.interedition.collatex.experimental.ngrams.data.IRealWitness#size()
   */
  public int size() {
    return tokens.size();
  }
}
