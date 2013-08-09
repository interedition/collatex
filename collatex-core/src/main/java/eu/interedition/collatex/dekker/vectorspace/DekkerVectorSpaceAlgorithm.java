package eu.interedition.collatex.dekker.vectorspace;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.vectorspace.VectorSpace.Vector;
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
  public void collate(VariantGraph against, List<? extends Iterable<Token>> witnesses) {
    throw new RuntimeException("Not yet implemented!");
  }

  public void collate(VariantGraph graph, SimpleWitness a, SimpleWitness b) {
    // Step 1: create the vector space and fill it with phrase matches
    VectorSpace s = new VectorSpace();
    s.fill(a, b, new EqualityTokenComparator());

    // Step 2: optimize the alignment...
    // TODO: skipped for now

    // Step 3: build the variant graph from the vector space
    // merge the first witness in
    // there are no alignments
    merge(graph, a, Collections.<Token, VariantGraph.Vertex> emptyMap());

    // convert vectors to <Token, Token> (Witness, other)
    Map<Token, Token> alignments = Maps.newHashMap();
    for (VectorSpace.Vector v : s.getVectors()) {
      List<Token> tokensDimension1 = getTokensFromVector(v, 0, a);
      List<Token> tokensDimension2 = getTokensFromVector(v, 1, b);
      for (int i = 0; i < v.length; i++) {
        Token witnessTokenToAdd = tokensDimension2.get(i);
        Token tokenAlreadyInGraph = tokensDimension1.get(i);
        alignments.put(witnessTokenToAdd, tokenAlreadyInGraph);
      }
    }

    // now construct vertices/edges for witness b
    mergeTokens(graph, b, alignments);
  }

  // dimension 0 = x
  // dimension 1 = y
  protected List<Token> getTokensFromVector(Vector v, int dimension, Iterable<Token> a) {
    int start = v.startCoordinate[dimension];
    // iterate over the witness until the start position is reached.
    Iterator<Token> it = a.iterator();
    for (int i = 1; i < start; i++) {
      it.next();
    }
    // fetch the tokens in the range of the vector from the witness
    List<Token> tokens = Lists.newArrayListWithCapacity(v.length);
    for (int i = 1; i <= v.length; i++) {
      Token t = it.next();
      tokens.add(t);
    }
    return tokens;
  }
}
