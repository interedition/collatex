package eu.interedition.collatex.dekker;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;


public interface TokenLinker {

  Map<Token, VariantGraphVertex> link(VariantGraph base, SortedSet<Token> witness, Comparator<Token> comparator);

}