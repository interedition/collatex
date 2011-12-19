package eu.interedition.collatex.lab;

import eu.interedition.collatex.IWitness;

import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphEdgeModel {
  private final SortedSet<IWitness> witnesses;

  public VariantGraphEdgeModel(SortedSet<IWitness> witnesses) {
    this.witnesses = witnesses;
  }

  public SortedSet<IWitness> getWitnesses() {
    return witnesses;
  }

}
