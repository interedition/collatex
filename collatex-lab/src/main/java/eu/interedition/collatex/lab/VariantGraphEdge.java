package eu.interedition.collatex.lab;

import eu.interedition.collatex.interfaces.IWitness;

import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphEdge {
  private final SortedSet<IWitness> witnesses;

  public VariantGraphEdge(SortedSet<IWitness> witnesses) {
    this.witnesses = witnesses;
  }

  public SortedSet<IWitness> getWitnesses() {
    return witnesses;
  }

}
