package eu.interedition.collatex2.experimental.graph;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;

import eu.interedition.collatex2.experimental.table.CollateXVertex;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphArc extends CollateXVertex implements IVariantGraphArc {
  private final IVariantGraphNode start;
  private final IVariantGraphNode end;
  private final Set<IWitness>     witnesses;

  public VariantGraphArc(IVariantGraphNode start, IVariantGraphNode end, IWitness witness, INormalizedToken token) {
    super("DOESNOTMATTER");
    this.start = start;
    this.end = end;
    this.witnesses = Sets.newLinkedHashSet();
    addToken(witness, token);
  }

  public Set<IWitness> getWitnesses() {
    return Collections.unmodifiableSet(witnesses);
  }

  @Override
  public IVariantGraphNode getBeginNode() {
    return start;
  }

  @Override
  public IVariantGraphNode getEndNode() {
    return end;
  }

  @Override
  public String toString() {
    String splitter = "";
    String to = getBeginNode().getNormalized() + " -> " + getEndNode().getNormalized() + ": ";
    for (IWitness witness : witnesses) {
      to += splitter + witness.getSigil();
      splitter = ", ";
    }
    return to;
  }

  @Override
  public void addToken(IWitness witness, INormalizedToken token) {
    witnesses.add(witness); // NOTE: THIS IS DUPLICATION!
    super.addToken(witness, token);
  }
}
