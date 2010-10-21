package eu.interedition.collatex2.implementation.output.jgraph;

import java.util.Set;

import eu.interedition.collatex2.interfaces.IJVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IJVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IWitness;

public class JVariantGraphEdge implements IJVariantGraphEdge {
  private final Set<IWitness> witnesses;
  private final IJVariantGraphVertex beginVertex;
  private final IJVariantGraphVertex endVertex;

  public JVariantGraphEdge(IJVariantGraphVertex beginVertex, IJVariantGraphVertex endVertex, IVariantGraphEdge vgEdge) {
    this.beginVertex = beginVertex;
    this.endVertex = endVertex;
    witnesses = vgEdge.getWitnesses();
  }

  @Override
  public Set<IWitness> getWitnesses() {
    return witnesses;
  }

  @Override
  public String toString() {
    return beginVertex + " --{" + witnesses + "}-> " + endVertex;
  }

}
