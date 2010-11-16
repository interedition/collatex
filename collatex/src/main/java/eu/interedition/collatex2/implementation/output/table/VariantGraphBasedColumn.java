package eu.interedition.collatex2.implementation.output.table;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.ColumnState;
import eu.interedition.collatex2.interfaces.IAlignmentTableVisitor;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IInternalColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphBasedColumn implements IColumn, IInternalColumn {
  private final List<IVariantGraphVertex> vertices;
  private final int position;
  
  public VariantGraphBasedColumn(IVariantGraphVertex vertex, int position) {
    this.vertices = Lists.newArrayList();
    this.position = position;
    addVertex(vertex);
  }
  
  @Override
  public ColumnState getState() {
    if (vertices.size()==1) {
      return ColumnState.MATCH;
    }
    return ColumnState.VARIANT;
  }

  @Override
  public INormalizedToken getToken(String sigil) {
    IVariantGraphVertex vertex = findVertexForWitness(sigil);
    if (vertex == null) {
      throw new NoSuchElementException("Witness " + sigil + " is not present in this column");
    }
    IWitness witness = vertex.getWitnessForSigil(sigil);
    return vertex.getToken(witness);
  }

  @Override
  public int getPosition() {
    return position;
  }

  //TODO: rename to getSigla()!
  //TODO: if the method is still used!
  //TODO: see method down below!
  @Override
  public List<String> getSigli() {
    throw new UnsupportedOperationException("NOT IMPLEMENTED!");
  }

  //TODO: add/re-enable test (see parallel segmentation tests)
  @Override
  public List<String> getSigla() {
    List<String> sigla = Lists.newArrayList();
    for (IVariantGraphVertex vertex : vertices) {
      Set<IWitness> witnesses = vertex.getWitnesses();
      for (IWitness witness : witnesses) {
        sigla.add(witness.getSigil());
      }
    }
    return sigla;
  }

  //TODO: make non public!
  @Override
  public void addVertex(IVariantGraphVertex vertex) {
    vertices.add(vertex);
  }

  //TODO: make IInternalColumn and IColumn interface
  //TODO: one and the same!
  @Override
  public IInternalColumn getInternalColumn() {
    return this;
  }

  //NOTE: ONLY USED IN TESTS!
  @Override
  public boolean containsWitness(String sigil) {
    IVariantGraphVertex findVertexForWitness = findVertexForWitness(sigil);
    return findVertexForWitness != null;
  }

  //TODO: No longer supported methods!
  @Override
  public List<INormalizedToken> getVariants() {
    throw new UnsupportedOperationException();
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
  public void setPosition(int position) {
    throw new UnsupportedOperationException();
  }
  
  // should maybe be a map?
  private IVariantGraphVertex findVertexForWitness(String sigil) {
    IVariantGraphVertex found = null;
    for (IVariantGraphVertex vertex : vertices) {
      if (found == null && vertex.containsWitness(sigil)) {
        found = vertex;
      }
    }
    return found;
  }


}
