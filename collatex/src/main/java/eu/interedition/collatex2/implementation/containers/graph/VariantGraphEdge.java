package eu.interedition.collatex2.implementation.containers.graph;

import java.util.Collections;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;

import com.google.common.collect.Sets;

import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IWitness;

@SuppressWarnings("serial")
public class VariantGraphEdge extends DefaultWeightedEdge implements IVariantGraphEdge {
  private final Set<IWitness> witnesses;

  //TODO: remove params!
  public VariantGraphEdge(IWitness witness) {
    this.witnesses = Sets.newLinkedHashSet();
    addWitness(witness);
  }

  @Override
  public Set<IWitness> getWitnesses() {
    return Collections.unmodifiableSet(witnesses);
  }

  @Override
  public String toString() {
    String splitter = "";
    StringBuilder to = new StringBuilder().append(": ");
    for (IWitness witness : witnesses) {
      to.append(splitter).append(witness.getSigil());
      splitter = ", ";
    }
    return to.toString();
  }

  @Override
  public void addWitness(IWitness witness) {
    witnesses.add(witness);
  }

  @Override
  public boolean containsWitness(IWitness witness) {
    return witnesses.contains(witness);
  }

}
