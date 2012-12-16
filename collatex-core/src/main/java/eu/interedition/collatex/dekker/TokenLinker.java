package eu.interedition.collatex.dekker;

import java.util.Comparator;
import java.util.Map;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;


public interface TokenLinker {

  Map<Token, VariantGraph.Vertex> link(VariantGraph base, Iterable<Token> witness, Comparator<Token> comparator);

}