package eu.interedition.collatex2.implementation.vg_alignment;

import java.util.List;

import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class Alignment2 implements IAlignment2 {

  private final IVariantGraph graph;
  private final IWitness witness;
  private final List<ITokenMatch> tokenMatches;

  public Alignment2(IVariantGraph graph, IWitness witness, List<ITokenMatch> tokenMatches) {
    this.graph = graph;
    this.witness = witness;
    this.tokenMatches = tokenMatches;
  }

  @Override
  public IVariantGraph getGraph() {
    return graph;
  }

  @Override
  public IWitness getWitness() {
    return witness;
  }

  @Override
  public List<ITokenMatch> getTokenMatches() {
    return tokenMatches;
  }
}
