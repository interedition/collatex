package eu.interedition.collatex2.implementation.output.table;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.ColumnState;
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
    if (vertices.size() == 1) {
      return ColumnState.INVARIANT;
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


  // should maybe be a map?
  protected IVariantGraphVertex findVertexForWitness(String sigil) {
    IVariantGraphVertex found = null;
    for (IVariantGraphVertex vertex : vertices) {
      if (found == null && vertex.containsWitness(sigil)) {
        found = vertex;
      }
    }
    return found;
  }

  //NOTE: base and witness are assumed to exist in the column!
  //NOTE: checks should have been performed before calling this method!
  @Override
  public boolean isMatch(String baseSigil, String witnessSigil) {
    IVariantGraphVertex baseV = findVertexForWitness(baseSigil);
    IVariantGraphVertex witnessV = findVertexForWitness(witnessSigil);
    return baseV == witnessV;
  }

}
