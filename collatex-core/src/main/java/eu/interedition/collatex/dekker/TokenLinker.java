package eu.interedition.collatex.dekker;

import java.util.Comparator;
import java.util.Map;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.neo4j.Neo4jVariantGraph;
import eu.interedition.collatex.neo4j.Neo4jVariantGraphVertex;


public interface TokenLinker {

  Map<Token, Neo4jVariantGraphVertex> link(Neo4jVariantGraph base, Iterable<Token> witness, Comparator<Token> comparator);

}