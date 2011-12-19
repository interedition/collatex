package eu.interedition.collatex.lab;

import eu.interedition.collatex.Witness;

import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphEdgeModel {
  private final SortedSet<Witness> witnesses;

  public VariantGraphEdgeModel(SortedSet<Witness> witnesses) {
    this.witnesses = witnesses;
  }

  public SortedSet<Witness> getWitnesses() {
    return witnesses;
  }

}
