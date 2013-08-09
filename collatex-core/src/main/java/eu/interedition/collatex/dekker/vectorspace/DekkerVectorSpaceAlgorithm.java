package eu.interedition.collatex.dekker.vectorspace;

import java.util.Collections;
import java.util.List;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleWitness;

public class DekkerVectorSpaceAlgorithm extends CollationAlgorithm.Base {
  @Override
  public void collate(VariantGraph against, Iterable<Token> witness) {
    throw new RuntimeException("Not yet implemented!");
  }

  @Override
  public void collate(VariantGraph against, Iterable<Token>... witnesses) {
    throw new RuntimeException("Not yet implemented!");
  }

  @Override
  public void collate(VariantGraph against,
      List<? extends Iterable<Token>> witnesses) {
    throw new RuntimeException("Not yet implemented!");
  }

  public void collate(VariantGraph graph, SimpleWitness a, SimpleWitness b) {
    // Step 1: create the vector space and fill it with phrase matches
    VectorSpace s = new VectorSpace();
    s.fill(a, b, new EqualityTokenComparator());
    // Step 2: optimize the alignment... 
    // skipped for now
    // Step 3: build the variant graph from the vector space
    // merge the first witness in
    // there are no alignments
    merge(graph, a, Collections.<Token, VariantGraph.Vertex>emptyMap());
    //TODO: witness b
  }

}
