package eu.interedition.collatex2.implementation.input;

import java.util.Iterator;
import java.util.List;

import eu.interedition.collatex2.implementation.indexing.NGram;
import eu.interedition.collatex2.interfaces.INGram;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class NormalizedWitness implements Iterable<INormalizedToken>, IWitness {
  private final String sigil;
  private final List<INormalizedToken> tokens;

  public NormalizedWitness(final String sigil, final List<INormalizedToken> tokens) {
    this.sigil = sigil;
    this.tokens = tokens;
  }

  // Note: not pleased with this method! implement Iterable!
  public List<INormalizedToken> getTokens() {
    return tokens;
  }

  public String getSigil() {
    return sigil;
  }

  // TODO: check whether iterator.remove() throws exception!
  @Override
  public Iterator<INormalizedToken> iterator() {
    return tokens.iterator();
  }

  public INGram createNGram(final int startPosition, final int endPosition) {
    return new NGram(tokens.subList(startPosition - 1, endPosition));
  }

  public int size() {
    return tokens.size();
  }

}
