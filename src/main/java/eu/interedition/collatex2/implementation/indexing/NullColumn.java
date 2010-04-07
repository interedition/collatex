package eu.interedition.collatex2.implementation.indexing;

import java.util.List;

import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class NullColumn implements IColumn {

  @Override
  public void addMatch(final INormalizedToken token) {
  // TODO Auto-generated method stub

  }

  @Override
  public void addVariant(final INormalizedToken token) {
  // TODO Auto-generated method stub

  }

  @Override
  public boolean containsWitness(final String sigil) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public int getPosition() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public INormalizedToken getToken(final String sigil) {
    return new NullToken(0, sigil);
  }

  @Override
  public List<INormalizedToken> getVariants() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setPosition(final int position) {
  // TODO Auto-generated method stub

  }

}
