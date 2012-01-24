package eu.interedition.collatex.lab;

import eu.interedition.collatex.Witness;

import java.util.Set;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphEdgeModel {
  private final Set<Witness> witnesses;

  public VariantGraphEdgeModel(Set<Witness> witnesses) {
    this.witnesses = witnesses;
  }

  public Set<Witness> getWitnesses() {
    return witnesses;
  }

}
