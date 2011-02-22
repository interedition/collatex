package eu.interedition.collatex2.legacy.indexing;

import java.util.List;

import eu.interedition.collatex2.implementation.input.NullToken;
import eu.interedition.collatex2.interfaces.ColumnState;
import eu.interedition.collatex2.interfaces.IInternalColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class NullColumn implements IInternalColumn {

  private final int position;

  public NullColumn(final int position) {
    this.position = position;
  }

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
    return new NullToken();
  }

  @Override
  public ColumnState getState() {
    return null;
  }

   @Override
  public List<String> getSigla() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String toString() {
    return "";
  }

  @Override
  public boolean isMatch(String base, String sigil) {
    throw new RuntimeException("DO NOT CALL THIS METHOD!");
  }

}
