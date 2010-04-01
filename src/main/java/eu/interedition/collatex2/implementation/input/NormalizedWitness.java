package eu.interedition.collatex2.implementation.input;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

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

  public NormalizedWitness(final String sigil) {
    this.sigil = sigil;
    this.tokens = Lists.newArrayList();
  }

  // Note: not pleased with this method! implement Iterable!
  public List<INormalizedToken> getTokens() {
    return tokens;
  }

  public String getSigil() {
    return sigil;
  }

  // TODO check whether iterator.remove() throws exception!
  @Override
  public Iterator<INormalizedToken> iterator() {
    return tokens.iterator();
  }

  public IPhrase createPhrase(final int startPosition, final int endPosition) {
    // TODO this problemCase shouldn't occur
    final boolean problemCase = (startPosition - 1 > endPosition);
    final List<INormalizedToken> subList = problemCase ? new ArrayList<INormalizedToken>() : tokens.subList(startPosition - 1, endPosition);
    return new Phrase(subList);
  }

  public int size() {
    return tokens.size();
  }

}
