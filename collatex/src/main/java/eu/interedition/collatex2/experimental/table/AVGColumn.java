package eu.interedition.collatex2.experimental.table;

import java.util.List;

import eu.interedition.collatex2.interfaces.ColumnState;
import eu.interedition.collatex2.interfaces.IAlignmentTableVisitor;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class AVGColumn implements IColumn {
  private final CollateXVertex vertex;

  public AVGColumn(CollateXVertex vertex) {
    this.vertex = vertex;
  }
  
  @Override
  public void accept(IAlignmentTableVisitor visitor) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addMatch(INormalizedToken token) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addVariant(INormalizedToken token) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsWitness(String sigil) {
    return vertex.containsWitness(sigil);
  }

  @Override
  public int getPosition() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<String> getSigli() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ColumnState getState() {
    throw new UnsupportedOperationException();
  }

  @Override
  public INormalizedToken getToken(String sigil) {
    IWitness witness = vertex.getWitnessForSigil(sigil);
    return vertex.getToken(witness);
  }

  @Override
  public List<INormalizedToken> getVariants() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setPosition(int position) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int compareTo(IColumn o) {
    throw new UnsupportedOperationException();
  }

}
