package eu.interedition.collatex2.experimental.vg_alignment;

import java.util.List;

import eu.interedition.collatex2.experimental.graph.IVariantGraph;
import eu.interedition.collatex2.experimental.graph.indexing.VariantGraphIndexMatcher;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphAligner {

  private final IVariantGraph graph;

  // This class does the alignment between a variantgraph and a witness
  // Step 1: Token matching
  // purpose: find the matching tokens between the graph and the witness
  // it uses the VariantGraphIndexMatcher class for that 
  // Step 2: Sequence detection
  // purpose: detect sequences in the token matches and combine them
  // together into matches
  public VariantGraphAligner(IVariantGraph graph) {
    this.graph = graph;
  }

  public IAlignment2 align(IWitness witness) {
    VariantGraphIndexMatcher matcher = new VariantGraphIndexMatcher(graph);
    List<ITokenMatch> tokenMatches = matcher.getMatches(witness);
    SequenceDetection2 seqDetection = new SequenceDetection2(tokenMatches);
    List<IMatch2> matches = seqDetection.chainTokenMatches();
    return new Alignment2(matches);
  }

}
