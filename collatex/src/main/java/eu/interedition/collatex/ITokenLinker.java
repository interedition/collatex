package eu.interedition.collatex;

import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;

import java.util.Comparator;
import java.util.Map;


public interface ITokenLinker {

  Map<Token, VariantGraphVertex> link(VariantGraph base, Iterable<Token> witness, Comparator<Token> comparator);

}