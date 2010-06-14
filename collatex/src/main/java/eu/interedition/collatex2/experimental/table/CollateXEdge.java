package eu.interedition.collatex2.experimental.table;

import java.util.Collections;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;

import com.google.common.collect.Sets;

import eu.interedition.collatex2.interfaces.IWitness;



@SuppressWarnings("serial")
public class CollateXEdge extends DefaultWeightedEdge {
  // TODO: note duplication with VariantGraphEdge class
  // not easy to remove due to single inheritance limitation
  private final Set<IWitness> witnesses;

  public CollateXEdge() {
    this.witnesses = Sets.newLinkedHashSet();
  }

  public Set<IWitness> getWitnesses() {
    return Collections.unmodifiableSet(witnesses);
  }

  public void addWitness(IWitness witness) {
    witnesses.add(witness);
  }

  public boolean containsWitness(IWitness witness) {
    return witnesses.contains(witness);
  }

}
