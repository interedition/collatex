package eu.interedition.collatex2.experimental.table;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.ColumnState;
import eu.interedition.collatex2.interfaces.IAlignmentTableVisitor;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class AVGColumn implements IColumn {
  private final List<CollateXVertex> vertices;
  private final int position;
  
  public AVGColumn(CollateXVertex vertex, int position) {
    this.vertices = Lists.newArrayList();
    this.position = position;
    addVertex(vertex);
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
    CollateXVertex findVertexForWitness = findVertexForWitness(sigil);
    return findVertexForWitness != null;
  }

  // should maybe be a map?
  private CollateXVertex findVertexForWitness(String sigil) {
    CollateXVertex found = null;
    for (CollateXVertex vertex : vertices) {
      if (found == null && vertex.containsWitness(sigil)) {
        found = vertex;
      }
    }
    return found;
  }

  @Override
  public int getPosition() {
    return position;
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
    CollateXVertex vertex = findVertexForWitness(sigil);
    if (vertex == null) {
      throw new RuntimeException("WITNESS "+sigil+" not found in this column!");
    }
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

  @Override
  public void addVertex(CollateXVertex vertex) {
    vertices.add(vertex);
  }
}
