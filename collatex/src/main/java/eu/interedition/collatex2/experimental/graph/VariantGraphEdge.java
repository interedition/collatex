package eu.interedition.collatex2.experimental.graph;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;

import eu.interedition.collatex2.experimental.table.CollateXVertex;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphEdge extends CollateXVertex implements IVariantGraphEdge {
  private final IVariantGraphVertex start;
  private final IVariantGraphVertex end;
  private final Set<IWitness>     witnesses;

  public VariantGraphEdge(IVariantGraphVertex start, IVariantGraphVertex end, IWitness witness, INormalizedToken token) {
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
  public IVariantGraphVertex getBeginVertex() {
    return start;
  }

  @Override
  public IVariantGraphVertex getEndVertex() {
    return end;
  }

  @Override
  public String toString() {
    String splitter = "";
    String to = getBeginVertex().getNormalized() + " -> " + getEndVertex().getNormalized() + ": ";
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
