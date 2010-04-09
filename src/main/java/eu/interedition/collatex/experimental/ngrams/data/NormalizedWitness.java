package eu.interedition.collatex.experimental.ngrams.data;

import java.util.Iterator;
import java.util.List;

import eu.interedition.collatex2.implementation.input.Phrase;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IWitness;

public class NormalizedWitness implements Iterable<INormalizedToken>, IWitness {
  private final String sigil;
  private final List<INormalizedToken> tokens;

  public NormalizedWitness(final String sigil, final List<INormalizedToken> tokens) {
    this.sigil = sigil;
    this.tokens = tokens;
  }

  // Note: not pleased with this method! implement Iterable!
  /* (non-Javadoc)
   * @see eu.interedition.collatex.experimental.ngrams.data.IRealWitness#getTokens()
   */
  public List<INormalizedToken> getTokens() {
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
  public Iterator<INormalizedToken> iterator() {
    return tokens.iterator();
  }

  /* (non-Javadoc)
  * @see eu.interedition.collatex.experimental.ngrams.data.IRealWitness#size()
  */
  public int size() {
    return tokens.size();
  }

  @Override
  public IPhrase createPhrase(final int startPosition, final int endPosition) {
    return new Phrase(tokens.subList(startPosition - 1, endPosition));
  }

  @Override
  public List<String> findRepeatingTokens() {
    // TODO Auto-generated method stub
    return null;
  }
}
