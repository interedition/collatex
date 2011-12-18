package eu.interedition.collatex.interfaces;

import eu.interedition.collatex.implementation.graph.VariantGraph;
import eu.interedition.collatex.implementation.graph.VariantGraphVertex;

import java.util.Comparator;
import java.util.Map;


public interface ITokenLinker {

  Map<Token, VariantGraphVertex> link(VariantGraph base, Iterable<Token> witness, Comparator<Token> comparator);

}