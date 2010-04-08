package eu.interedition.collatex2.implementation.indexing;

import java.util.List;

import eu.interedition.collatex2.interfaces.ColumnState;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class NullColumn implements IColumn {

  private final int position;

  public NullColumn(final int position) {
    this.position = position;
  }

  @Override
  public void addMatch(final INormalizedToken token) {}

  @Override
  public void addVariant(final INormalizedToken token) {}

  @Override
  public boolean containsWitness(final String sigil) {
    return false;
  }

  @Override
  public int getPosition() {
    return position;
  }

  @Override
  public INormalizedToken getToken(final String sigil) {
    return new NullToken(0, sigil);
  }

  @Override
  public List<INormalizedToken> getVariants() {
    return null;
  }

  @Override
  public void setPosition(final int position) {}

  @Override
  public ColumnState getState() {
    return null;
  }

  @Override
  public int compareTo(final IColumn o) {
    // TODO Auto-generated method stub
    return 0;
  }

}
