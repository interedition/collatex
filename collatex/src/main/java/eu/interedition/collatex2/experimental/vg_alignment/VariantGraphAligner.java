package eu.interedition.collatex2.experimental.vg_alignment;

import java.util.List;

import eu.interedition.collatex2.implementation.tokenmatching.TokenIndexMatcher;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphAligner {

  private final IVariantGraph graph;

  // This class does the alignment between a variantgraph and a witness
  // The alignment is done using token matching
  // purpose: find the matching tokens between the graph and the witness
  // it uses the VariantGraphIndexMatcher class for that 
  public VariantGraphAligner(IVariantGraph graph) {
    this.graph = graph;
  }

  public IAlignment2 align(IWitness witness) {
    TokenIndexMatcher matcher = new TokenIndexMatcher(graph);
    List<ITokenMatch> tokenMatches = matcher.getMatches(witness);
    return new Alignment2(tokenMatches);
  }

}
